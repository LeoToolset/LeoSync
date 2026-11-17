package com.sync2pc.app

import com.sync2pc.app.data.SmbEncryption.OFF
import com.sync2pc.app.data.SmbEncryption.PREFERRED
import com.sync2pc.app.data.SmbEncryption.REQUIRED
import com.sync2pc.app.smb.EncryptionAuto
import com.sync2pc.app.smb.EncryptionAuto.SaveLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** SYNC-20261114-338 B11a: automatische Umstellung «Wenn möglich» -> «Erforderlich» */
class EncryptionAutoTest {
    @Test fun upgradesOnlyPreferredWithEncryptedSessionAndNoUserChoice() {
        assertTrue(EncryptionAuto.shouldUpgrade(5, PREFERRED, sessionEncrypted = true, userChose = false))
        assertFalse("unverschlüsselte Sitzung", EncryptionAuto.shouldUpgrade(5, PREFERRED, false, false))
        assertFalse("Benutzer hat selbst gewählt", EncryptionAuto.shouldUpgrade(5, PREFERRED, true, true))
        assertFalse("«Aus» wird nie automatisch geändert", EncryptionAuto.shouldUpgrade(5, OFF, true, false))
        assertFalse("schon «Erforderlich»", EncryptionAuto.shouldUpgrade(5, REQUIRED, true, false))
        assertFalse("ungespeichertes Profil", EncryptionAuto.shouldUpgrade(0, PREFERRED, true, false))
    }

    /** B4: Zustand «Verschlüsselung abgelehnt» nur bei «Erforderlich» und gescheitertem Lauf; sonst nie */
    @Test fun rejectedOnlyForRequiredAfterAnEncryptionFailure() {
        assertTrue(EncryptionAuto.rejected(REQUIRED, failedForEncryption = true))
        assertFalse("kein Fehlschlag", EncryptionAuto.rejected(REQUIRED, false))
        assertFalse("«Wenn möglich» lehnt nie ab", EncryptionAuto.rejected(PREFERRED, true))
        assertFalse("«Aus» lehnt nie ab", EncryptionAuto.rejected(OFF, true))
    }

    @Test fun untouchedLevelInEditorKeepsTheAutomaticUpgrade() {
        // Editor lud PREFERRED, die App stellte inzwischen auf REQUIRED um, der Benutzer ändert nur etwas anderes
        assertEquals(SaveLevel(REQUIRED, false), EncryptionAuto.levelOnSave(PREFERRED, PREFERRED, REQUIRED))
        assertEquals(SaveLevel(PREFERRED, false), EncryptionAuto.levelOnSave(PREFERRED, PREFERRED, PREFERRED))
    }

    @Test fun deliberateChangeIsFinalEvenAgainstTheUpgrade() {
        // Benutzer stellt nach der Umstellung zurück auf «Wenn möglich» oder «Aus»
        assertEquals(SaveLevel(PREFERRED, true), EncryptionAuto.levelOnSave(REQUIRED, PREFERRED, REQUIRED))
        assertEquals(SaveLevel(OFF, true), EncryptionAuto.levelOnSave(PREFERRED, OFF, REQUIRED))
        // Benutzer wählt «Erforderlich» selbst, während die App parallel umstellte: gleiche Stufe, trotzdem bewusst
        assertEquals(SaveLevel(REQUIRED, true), EncryptionAuto.levelOnSave(PREFERRED, REQUIRED, REQUIRED))
    }

    @Test fun newProfileTakesTheChosenLevelWithoutMarker() {
        assertEquals(SaveLevel(OFF, false), EncryptionAuto.levelOnSave(null, OFF, null))
        assertEquals(SaveLevel(PREFERRED, false), EncryptionAuto.levelOnSave(null, PREFERRED, null))
    }

    @Test fun existingProfileWithoutBaselineCountsAsChosen() {
        assertEquals(SaveLevel(OFF, true), EncryptionAuto.levelOnSave(null, OFF, PREFERRED))
    }
}