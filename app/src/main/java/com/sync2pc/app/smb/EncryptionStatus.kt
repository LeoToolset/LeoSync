package com.sync2pc.app.smb

import android.content.Context
import com.sync2pc.app.data.Profile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * SYNC-20261111-225: Tatsächlicher Verschlüsselungszustand je Profil, nicht die Einstellung. Nach jeder erfolgreichen
 * Anmeldung wird festgehalten, ob die Sitzung wirklich verschlüsselt ist (smbSessionEncrypted). Der Wert gilt nur für
 * dieselbe Einstellung, Freigabe und denselben Unterordner: ändert sich eines davon, ist der Zustand wieder unbekannt
 * (kein Icon), bis die nächste Verbindung ihn bestätigt. Gespeichert in SharedPreferences (kein DB-Schema).
 */
object EncryptionStatus {
    private const val PREFS = "enc_status"
    @Volatile private var appContext: Context? = null
    private val tick = MutableStateFlow(0)
    /** Zähler, der sich bei jeder Aufzeichnung erhöht (für die Anzeige) */
    val changes: StateFlow<Int> get() = tick

    fun init(ctx: Context) { appContext = ctx.applicationContext }

    private fun fingerprint(p: Profile) =
        "${p.encryption().name}|${p.smbPath.trim().lowercase()}|${p.remoteSubfolder.trim('\\', '/').lowercase()}"

    /** Nach erfolgreicher Anmeldung aufrufen. Profile ohne ID (ungespeicherter Editor) werden nicht festgehalten. */
    fun record(p: Profile, encrypted: Boolean) {
        val ctx = appContext ?: return
        if (p.id == 0L) return
        val v = (if (encrypted) "1" else "0") + "|" + fingerprint(p)
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getString("p${p.id}", null) == v) return
        prefs.edit().putString("p${p.id}", v).apply()
        tick.value++
    }

    /** true = zuletzt wirklich verschlüsselt; false = zuletzt unverschlüsselt; null = unbekannt oder Profil seither geändert */
    fun lastKnown(p: Profile): Boolean? {
        val ctx = appContext ?: return null
        val v = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("p${p.id}", null) ?: return null
        val sep = v.indexOf('|')
        if (sep != 1 || v.substring(sep + 1) != fingerprint(p)) return null
        return v[0] == '1'
    }

    /**
     * SYNC-20261116-446: Reine Entscheidung (getestet): Soll der Status durch eine Prüfverbindung ermittelt werden? Ja, wenn er
     * unbekannt ist (nie aufgezeichnet oder Profil seither geändert) und für diesen Profilstand noch kein Versuch lief. Ein
     * gescheiterter Versuch wird für denselben Profilstand nicht wiederholt (keine wiederholten Anmeldeversuche mit falschem Passwort).
     */
    fun shouldVerify(lastKnown: Boolean?, attemptedFingerprint: String?, fingerprint: String): Boolean =
        lastKnown == null && attemptedFingerprint != fingerprint

    /**
     * Ein Fotoprofil ohne Arbeit verbindet sich nicht und frischt den Status nie auf: nach einer Profiländerung blieb das Symbol weg,
     * obwohl die Verschlüsselung erzwungen ist. Dann einmal prüfen, ob die Verbindung wirklich verschlüsselt ist (das Symbol sagt
     * nur dann etwas aus, wenn es von einer echten Sitzung stammt). [verify] öffnet die Sitzung (zeichnet den Status selbst auf).
     */
    fun verifyIfUnknown(p: Profile, verify: () -> Unit) {
        val ctx = appContext ?: return
        if (p.id == 0L) return
        val fp = fingerprint(p)
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!shouldVerify(lastKnown(p), prefs.getString("a${p.id}", null), fp)) return
        prefs.edit().putString("a${p.id}", fp).apply() // vor dem Versuch: auch ein Abbruch zählt als Versuch
        try { verify() } catch (ignored: Exception) {}
    }

    /** Beim Löschen eines Profils */
    fun forget(p: Profile) = forget(p.id)

    /** Status (p<ID>) und Prüfversuch (a<ID>) eines endgültig gelöschten Profils entfernen (SYNC-20261116-448) */
    fun forget(profileId: Long) {
        appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)?.edit()?.remove("p$profileId")?.remove("a$profileId")?.apply()
    }
}