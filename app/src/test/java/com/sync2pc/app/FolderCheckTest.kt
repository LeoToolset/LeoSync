package com.sync2pc.app

import com.sync2pc.app.backup.BackupCodec
import com.sync2pc.app.backup.BackupContent
import com.sync2pc.app.data.Profile
import com.sync2pc.app.sync.FolderCheck
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Globales Prüfintervall der Ordner-Profile: Startwert-Regel und Sicherung alt/neu (SYNC-20261113-306) */
class FolderCheckTest {
    @Test fun startValueIsShortestProfileIntervalOrDefault() {
        assertEquals(30, FolderCheck.startValue(listOf(120, 30, 60)))
        assertEquals(60, FolderCheck.startValue(emptyList())) // ohne Ordner-Profile: 60
        assertEquals(15, FolderCheck.startValue(listOf(5)))   // in den Bereich 15..1440 gebracht
        assertEquals(1440, FolderCheck.startValue(listOf(5000)))
    }

    @Test fun clampKeepsRange() {
        assertEquals(15, FolderCheck.clamp(0))
        assertEquals(1440, FolderCheck.clamp(99999))
        assertEquals(90, FolderCheck.clamp(90))
    }

    @Test fun oldBackupWithoutValueNeedsStartValueNewBackupDoesNot() {
        val old = mapOf<String, Any>("syncOffWifi" to true)
        val new = mapOf<String, Any>("syncOffWifi" to true, "folderCheckMinutes" to 45)
        assertTrue(FolderCheck.needsStartValue(old))
        assertFalse(FolderCheck.needsStartValue(new))
        assertTrue(FolderCheck.needsStartValue(mapOf("folderCheckMinutes" to "kein Wert")))
    }

    @Test fun backupRoundTripKeepsGlobalValueAndProfileInterval() {
        // Neue Sicherung: globaler Wert bleibt erhalten; das Profilfeld wird weiter geschrieben und gelesen (Startwert-Regel)
        val c = BackupContent(
            appVersion = "0.17.0", createdAt = 1L, settings = mapOf("folderCheckMinutes" to 45),
            profiles = listOf(Profile(id = 1, name = "Ordner", profileType = "MIRROR", remotePollMinutes = 30)),
            passwords = emptyMap(), mappings = emptyList(), synced = emptyList(), mirrorStates = emptyList(),
        )
        val p = BackupCodec.parse(BackupCodec.encode(c, exportPassword = null, iterations = 1_000), null)
        assertEquals(45, (p.content.settings["folderCheckMinutes"] as Number).toInt())
        assertEquals(30, p.content.profiles.single().remotePollMinutes)
        assertFalse(FolderCheck.needsStartValue(p.content.settings))
        // Ältere Sicherung: kein globaler Wert -> Startwert aus den Profilen der Sicherung
        val old = BackupCodec.parse(BackupCodec.encode(c.copy(settings = emptyMap()), null, 1_000), null)
        assertTrue(FolderCheck.needsStartValue(old.content.settings))
        assertEquals(30, FolderCheck.startValue(old.content.profiles.map { it.remotePollMinutes }))
    }
}