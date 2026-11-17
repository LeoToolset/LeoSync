package com.sync2pc.app.smb

import android.content.Context
import com.hierynomus.mssmb2.SMB2PacketData
import com.hierynomus.mssmb2.SMB2MessageFlag
import com.hierynomus.protocol.transport.TransportException
import com.hierynomus.smb.SMBPacketData
import com.hierynomus.smbj.connection.Connection
import com.hierynomus.smbj.connection.packet.AbstractIncomingPacketHandler
import com.hierynomus.smbj.connection.packet.IncomingPacketHandler
import com.hierynomus.smbj.connection.packet.SMB2CompoundedPacketHandler
import com.sync2pc.app.data.Profile
import com.sync2pc.app.data.SmbEncryption

/**
 * Antwortprüfung in SMB-Verbindungen (SYNC-20261115-365, korrigiert in -371). smbj 0.13.0 nimmt in verschlüsselten Sitzungen ohne
 * Signierpflicht Klartext-Antworten an und ordnet Antworten nur über die MessageId zu; die eigene Signierprüfung lässt
 * SessionId 0 und unbekannte SessionIds ungeprüft durch. Darum gilt die Regel **je Verbindung**, nicht je Sitzung: ab der Anmeldung
 * wird jedes eingehende Paket (auch jeder Teil eines zusammengesetzten Pakets, der Guard sitzt HINTER dem Compound-Handler)
 * geprüft. Die Regeln sind rein (getestet in PacketPolicyTest).
 */
object PacketPolicy {
    enum class Verdict { PASS, PLAINTEXT_IN_ENCRYPTED_SESSION, UNSIGNED_IN_SIGNED_SESSION }

    /** Zustand der Verbindung: vor der Anmeldung NONE, danach ENCRYPTED oder SIGNED(sessionId) */
    sealed class Mode {
        object None : Mode()
        object Encrypted : Mode()
        data class Signed(val sessionId: Long) : Mode()
    }

    /**
     * @param plainSmb2 ein SMB2-Paket, das im Klartext ankam (nicht aus einem entschlüsselten Transform-Paket); false auch für
     *   nicht-SMB2-Pakete NICHT, diese setzt der Aufrufer über [isSmb2] = false
     * @param oplockBreak Oplock-/Lease-Break-Benachrichtigung (MessageId 0xFFFFFFFFFFFFFFFF und Befehl OPLOCK_BREAK)
     * @param intermediateAsync Zwischenantwort STATUS_PENDING (darf unsigniert sein, wie in smbj)
     */
    fun decide(
        mode: Mode, isSmb2: Boolean, decrypted: Boolean, oplockBreak: Boolean, sessionId: Long, signedFlag: Boolean, intermediateAsync: Boolean,
    ): Verdict = when (mode) {
        Mode.None -> Verdict.PASS
        // Nur entschlüsselte Pakete oder eine Oplock-Break-Benachrichtigung; sonst jede Klartext-Nachricht abweisen (auch SESSION_SETUP,
        // SessionId 0, fremde SessionId, mit oder ohne Signatur-Flag)
        Mode.Encrypted -> if (isSmb2 && (decrypted || oplockBreak)) Verdict.PASS else Verdict.PLAINTEXT_IN_ENCRYPTED_SESSION
        // Zulässig: Oplock-Break oder (eigene SessionId und signiert bzw. Zwischenantwort); die Signatur selbst prüft danach smbj
        is Mode.Signed ->
            if (isSmb2 && (oplockBreak || (sessionId == mode.sessionId && (signedFlag || intermediateAsync)))) Verdict.PASS
            else Verdict.UNSIGNED_IN_SIGNED_SESSION
    }

    /**
     * Lässt sich der Guard nicht einsetzen (smbj-Update, Reflection-Fehler): bei «Erforderlich» die Anmeldung ablehnen wie
     * «Verschlüsselung nicht verfügbar», bei einem Signier-Merker ebenfalls (fail-closed), sonst fortfahren.
     */
    fun mustAbortWithoutGuard(level: SmbEncryption, signingRemembered: Boolean, installed: Boolean): Boolean =
        !installed && (level == SmbEncryption.REQUIRED || signingRemembered)

    /** Modus nach der Anmeldung: verschlüsselt → ENCRYPTED; sonst bei Signierpflicht (Server oder Merker) → SIGNED; sonst NONE */
    fun modeAfterLogin(encrypted: Boolean, signingRequired: Boolean, sessionId: Long): Mode = when {
        encrypted -> Mode.Encrypted
        signingRequired -> Mode.Signed(sessionId)
        else -> Mode.None
    }
}

/** Klartext-Antwort in einer verschlüsselten Verbindung wurde abgewiesen (die Verbindung wird getrennt, kein stilles Verwerfen) */
class SmbPlaintextRejectedException : TransportException("Plaintext reply in encrypted SMB session rejected")

/** Unsignierte oder fremde Antwort in einer Verbindung mit Signierpflicht, oder die Signierung fehlt: der Server signiert nicht mehr */
class SmbSigningRejectedException(cause: Throwable? = null) : TransportException("Unsigned reply in signed SMB session rejected", cause)

/**
 * Prüfstelle in der Handlerkette einer smbj-[Connection], HINTER dem Compound-Handler (jeder Teil eines zusammengesetzten Pakets wird
 * einzeln geprüft). Unzulässig = Ausnahme (smbj beendet dann die Verbindung und alle offenen Anfragen mit Fehler).
 */
class SessionGuard(private val original: IncomingPacketHandler) : IncomingPacketHandler {
    @Volatile private var mode: PacketPolicy.Mode = PacketPolicy.Mode.None

    /** Nach der Anmeldung, vor jeder weiteren Anfrage (TREE_CONNECT, srvsvc) */
    fun setMode(m: PacketPolicy.Mode) { mode = m }

    override fun handle(data: SMBPacketData<*>) {
        val m = mode
        if (m !== PacketPolicy.Mode.None) {
            val p = data as? SMB2PacketData
            val verdict = if (p == null) PacketPolicy.decide(m, false, false, false, 0L, false, false) else {
                val h = p.header
                PacketPolicy.decide(
                    m, isSmb2 = true, decrypted = p.isDecrypted, oplockBreak = p.isOplockBreakNotification,
                    sessionId = h.sessionId, signedFlag = h.isFlagSet(SMB2MessageFlag.SMB2_FLAGS_SIGNED),
                    intermediateAsync = p.isIntermediateAsyncResponse,
                )
            }
            when (verdict) {
                PacketPolicy.Verdict.PASS -> {}
                PacketPolicy.Verdict.PLAINTEXT_IN_ENCRYPTED_SESSION -> throw SmbPlaintextRejectedException()
                PacketPolicy.Verdict.UNSIGNED_IN_SIGNED_SESSION -> throw SmbSigningRejectedException()
            }
        }
        original.handle(data)
    }

    // Die Kette ist beim Einsetzen schon verknüpft; Aufrufe danach gelten der Originalkette
    override fun setNext(next: IncomingPacketHandler): IncomingPacketHandler = original.setNext(next)

    companion object {
        internal const val CHAIN_FIELD = "packetHandlerChain"
        internal const val NEXT_FIELD = "next"

        /**
         * Setzt den Guard hinter den [SMB2CompoundedPacketHandler]: Feld `Connection.packetHandlerChain` (privat) und `next` der
         * Handler (protected), beides per Reflection. Kette: Decrypting → Compounded → [Guard] → IsOutstanding → … null = nicht
         * möglich (Feld oder Handler fehlt, z. B. nach einem smbj-Update; der Wächtertest schlägt dann fehl).
         */
        fun install(connection: Connection): SessionGuard? = try {
            val chainField = Connection::class.java.getDeclaredField(CHAIN_FIELD).apply { isAccessible = true }
            installBehindCompound(chainField.get(connection) as? IncomingPacketHandler)
        } catch (e: Exception) { null }

        /** Kern von [install] auf einer Handlerkette ab [head] (testbar ohne Netzwerkverbindung); idempotent */
        internal fun installBehindCompound(head: IncomingPacketHandler?): SessionGuard? = try {
            val nextField = AbstractIncomingPacketHandler::class.java.getDeclaredField(NEXT_FIELD).apply { isAccessible = true }
            var h: IncomingPacketHandler? = head
            var found: SessionGuard? = null
            var steps = 0
            while (h != null && found == null && steps++ < 16) {
                if (h is SessionGuard) { found = h; break }
                if (h is SMB2CompoundedPacketHandler) {
                    val after = nextField.get(h) as? IncomingPacketHandler
                    if (after == null) break
                    found = if (after is SessionGuard) after else SessionGuard(after).also { nextField.set(h, it) }
                    break
                }
                h = (h as? AbstractIncomingPacketHandler)?.let { nextField.get(it) as? IncomingPacketHandler }
            }
            found
        } catch (e: Exception) { null }
    }
}
/** Reine Entscheidung zum Signier-Merker (B11b), ohne Android (getestet in SigningStatusTest) */
object SigningPolicy {
    /**
     * Merken, dass dieser Server signiert? Nur bei Stufe «Wenn möglich» oder «Aus», wenn die Sitzung NICHT verschlüsselt ist
     * (verschlüsselt schützt schon, und smbj schaltet die Pflicht dort selbst ab), weder Gast noch anonym und der Server die
     * Signierung verlangt.
     */
    fun shouldRemember(level: SmbEncryption, sessionEncrypted: Boolean, guest: Boolean, anonymous: Boolean, signingRequired: Boolean): Boolean =
        level != SmbEncryption.REQUIRED && !sessionEncrypted && !guest && !anonymous && signingRequired
}

/**
 * Signier-Merker je Profil (B11b, SYNC-20261115-365): hat der Server einmal signiert (und es gab keine Verschlüsselung), wird die
 * Signierung künftig verlangt (`withSigningRequired`). Gilt nur für dieselbe Stufe, Freigabe und denselben Unterordner (Fingerabdruck wie
 * [EncryptionStatus]); SharedPreferences, nicht in der Sicherung. Ablehnung: [GlobalSettings.encNotified] (gleicher Weg wie B4).
 */
object SigningStatus {
    private const val PREFS = "sign_status"
    @Volatile private var appContext: Context? = null

    fun init(ctx: Context) { appContext = ctx.applicationContext }

    private fun fingerprint(p: Profile) =
        "${p.encryption().name}|${p.smbPath.trim().lowercase()}|${p.remoteSubfolder.trim('\\', '/').lowercase()}"

    /** Verlangt LeoSync für dieses Profil signierte Antworten? */
    fun required(p: Profile): Boolean {
        val ctx = appContext ?: return false
        if (p.id == 0L) return false
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("p${p.id}", null) == fingerprint(p)
    }

    fun remember(p: Profile) {
        val ctx = appContext ?: return
        if (p.id == 0L) return
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString("p${p.id}", fingerprint(p)).commit()
    }

    /** Beim Löschen eines Profils und bei der bewussten Aktion «Ohne Signierung erlauben» */
    fun forget(profileId: Long) {
        appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)?.edit()?.remove("p$profileId")?.commit()
    }

    /** Zustand «Signierung abgelehnt»: Merker gesetzt, der letzte Lauf scheiterte an der Schutzprüfung, Stufe nicht «Erforderlich» */
    fun isRejected(context: Context, p: Profile): Boolean =
        p.id != 0L && p.encryption() != SmbEncryption.REQUIRED && required(p) &&
            com.sync2pc.app.data.GlobalSettings(context).encNotified(p.id)

    /** Aktion «Ohne Signierung erlauben» */
    fun allow(context: Context, profileId: Long) {
        forget(profileId)
        com.sync2pc.app.data.GlobalSettings(context).setEncNotified(profileId, false)
    }
}
