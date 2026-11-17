package com.sync2pc.app.smb

import android.content.Context
import com.sync2pc.app.data.AppDatabase
import com.sync2pc.app.data.SmbEncryption

/**
 * Automatische Umstellung der Verschlüsselungsstufe (SYNC-20261114-338, B11a): Ist ein Profil auf «Wenn möglich» (PREFERRED) und die
 * Sitzung tatsächlich verschlüsselt, stellt die App das Profil einmalig auf «Erforderlich» (REQUIRED) um. Damit greift die
 * bestehende Pflichtprüfung (Dialekte 3.x, Vorab- und Nachprüfung in [PcLogin]): Ein Angreifer im LAN kann die Verschlüsselung
 * nicht mehr unbemerkt herabstufen. Ändert der Benutzer die Stufe selbst, ist das endgültig: dort stellt die App nie mehr um
 * (Merker je Profil-ID in SharedPreferences, nicht in der Sicherung). «Aus» wird nie automatisch geändert.
 */
object EncryptionAuto {
    private const val PREFS = "enc_auto"

    /** Reine Entscheidung: umstellen? Nur PREFERRED, tatsächlich verschlüsselt, Benutzer hat die Stufe nie selbst geändert, gespeichertes Profil */
    fun shouldUpgrade(profileId: Long, level: SmbEncryption, sessionEncrypted: Boolean, userChose: Boolean): Boolean =
        profileId != 0L && level == SmbEncryption.PREFERRED && sessionEncrypted && !userChose

    /** Ergebnis beim Speichern im Profil-Editor: welche Stufe gilt, und hat der Benutzer sie selbst geändert? */
    data class SaveLevel(val level: SmbEncryption, val userChose: Boolean)

    /**
     * Stufe beim Speichern (Wettlauf mit der automatischen Umstellung): Hat der Benutzer die Stufe im Editor nicht angefasst
     * ([editor] == [baseline]), gilt die aktuelle Stufe aus der Datenbank ([db]); sonst die gewählte, und sie ist endgültig.
     * Neues Profil ([db] null): die gewählte, kein Merker.
     */
    fun levelOnSave(baseline: SmbEncryption?, editor: SmbEncryption, db: SmbEncryption?): SaveLevel = when {
        db == null -> SaveLevel(editor, false)
        baseline != null && editor == baseline -> SaveLevel(db, false)
        else -> SaveLevel(editor, true)
    }

    /**
     * Zustand «Verschlüsselung abgelehnt» (B4, -338), von der Oberfläche gelesen (rotes Symbol, Profilmeldung): Die Stufe ist
     * «Erforderlich», und der letzte Lauf des Profils scheiterte, weil der PC (oder ein Angreifer im LAN) keine Verschlüsselung
     * anbot. Er endet, sobald ein Lauf des Profils gelingt oder der Benutzer [allowUnencrypted] wählt bzw. die Stufe ändert.
     */
    fun rejected(level: SmbEncryption, failedForEncryption: Boolean): Boolean = level == SmbEncryption.REQUIRED && failedForEncryption

    fun isRejected(context: Context, profile: com.sync2pc.app.data.Profile): Boolean =
        profile.id != 0L && rejected(profile.encryption(), com.sync2pc.app.data.GlobalSettings(context).encNotified(profile.id))

    /**
     * Aktion «Unverschlüsselte Verbindung erlauben»: Stufe zurück auf «Wenn möglich» und Merker «vom Benutzer gewählt» (danach stellt
     * die App für dieses Profil nie mehr automatisch um). true = umgestellt. Der Aufrufer zeigt die Rückmeldung.
     */
    suspend fun allowUnencrypted(context: Context, profileId: Long): Boolean {
        if (profileId == 0L) return false
        if (AppDatabase.get(context).dao().allowUnencrypted(profileId) != 1) return false
        setUserChose(context, profileId)
        com.sync2pc.app.data.GlobalSettings(context).setEncNotified(profileId, false)
        return true
    }

    fun userChose(context: Context, profileId: Long): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("user$profileId", false)

    fun setUserChose(context: Context, profileId: Long) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("user$profileId", true).commit()
    }

    /** Beim Löschen eines Profils */
    fun forget(context: Context, profileId: Long) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove("user$profileId").remove("info$profileId").commit()
        SigningStatus.forget(profileId)
    }

    /** Einmalige Info-Zeile im Profil «automatisch auf Erforderlich gestellt» (SYNC-20261115-352), bleibt bis «OK» */
    fun autoInfoPending(context: Context, profileId: Long): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("info$profileId", false)

    fun setAutoInfo(context: Context, profileId: Long, pending: Boolean) {
        val e = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        (if (pending) e.putBoolean("info$profileId", true) else e.remove("info$profileId")).commit()
    }

    /** Aktion «Auf Wenn möglich stellen» der Karte «Verschlüsselung nicht verfügbar»: wie [allowUnencrypted] */
    suspend fun lowerToPreferred(context: Context, profileId: Long): Boolean {
        val ok = allowUnencrypted(context, profileId)
        if (ok) setAutoInfo(context, profileId, false)
        return ok
    }

    /**
     * Nach erfolgreicher Anmeldung aufrufen. true = das Profil wurde jetzt auf REQUIRED umgestellt (der Aufrufer schreibt den
     * Verlaufseintrag). Atomar in der Datenbank (nur wenn sie noch PREFERRED enthält: eine gleichzeitige Änderung des Benutzers
     * wird nie still rückgängig gemacht). Der Status wird direkt mit dem neuen Fingerabdruck geschrieben (Symbol flackert nicht).
     */
    suspend fun afterOpen(context: Context, profile: com.sync2pc.app.data.Profile, session: SmbSession): Boolean {
        if (!shouldUpgrade(profile.id, profile.encryption(), session.isEncrypted, userChose(context, profile.id))) return false
        val dao = AppDatabase.get(context).dao()
        if (dao.upgradeEncryption(profile.id) != 1) return false
        EncryptionStatus.record(profile.copy(smbEncryption = SmbEncryption.REQUIRED.name), true)
        setAutoInfo(context, profile.id, true)
        return true
    }
}