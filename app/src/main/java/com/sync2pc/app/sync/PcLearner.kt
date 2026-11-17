package com.sync2pc.app.sync

import android.content.Context
import com.sync2pc.app.data.AppDatabase
import com.sync2pc.app.data.Profile
import com.sync2pc.app.smb.SmbSession
import com.sync2pc.app.smb.SmbTarget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Kennung des PCs nach dem Speichern lernen (SYNC-20261109-055 Punkt 6, V5 aus -058). Läuft NACH dem Schreiben in die Datenbank
 * in einem app-weiten Scope (der Bildschirm kann sofort geschlossen werden), mit kurzen Zeitlimits (30 s je SMB-Vorgang). Speichern gelingt
 * immer, unabhängig vom Ergebnis: Port 445 zu, kein Netz, Anmeldung abgelehnt = nichts gelernt (Status «noch nicht
 * erkannt»). Eine abweichende Kennung wird nicht still übernommen (Neuvertrauen nur in «Verbindung testen»).
 * Gelernt wird nur über denselben Weg wie «Verbindung testen»: aushandeln, Bedingungen prüfen, anmelden, connectShare, dann lernen.
 */
object PcLearner {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _running = MutableStateFlow<Set<Long>>(emptySet())

    /** Profile, für die gerade gelernt wird (Anzeige «PC wird erkannt …») */
    val running: StateFlow<Set<Long>> = _running

    /** Startet das Lernen für das gerade gespeicherte Profil, falls nötig (Entscheidung: [PcEditorRules.learnAfterSave]) */
    fun learnAfterSave(ctx: Context, profileId: Long) {
        val app = ctx.applicationContext
        scope.launch {
            val p = AppDatabase.get(app).dao().profile(profileId) ?: return@launch
            val host = SmbTarget.hostOf(p.smbPath).orEmpty()
            if (!PcEditorRules.learnAfterSave(p.pcMode(), host, p.smbUser.isNotBlank(), p.pcGuid, p.pcGuidHost)) return@launch
            _running.update { it + profileId }
            try {
                SmbSession.open(p, PcAccess.SETUP, onChangedGuid = null, learn = true, quick = true).close()
                // Kontakt mit bestätigter PC-Kennung: das aktuelle WLAN als bekanntes Netz merken (SYNC-20261115-351)
                KnownNetStore.confirmContact(app, profileId, WifiHelper.currentNetFingerprint(app))
            } catch (e: Exception) {
                // nichts gelernt: Status bleibt «noch nicht erkannt» bzw. «anderer PC». Scheiterte es an der Verschlüsselung
                // («Erforderlich», der PC kann nicht verschlüsseln), die Ursache sichtbar machen (SYNC-20261115-373): gleicher
                // Weg wie nach einem Lauf, die Startseite zeigt die Karte «Verschlüsselung nicht verfügbar»
                if (isEncryptionUnavailable(e)) com.sync2pc.app.data.GlobalSettings(app).setEncNotified(profileId, true)
            } finally {
                _running.update { it - profileId }
            }
        }
    }
}
