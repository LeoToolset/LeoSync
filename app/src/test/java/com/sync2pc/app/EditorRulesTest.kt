package com.sync2pc.app

import com.sync2pc.app.backup.BackupCodec
import com.sync2pc.app.data.PasswordKeep
import com.sync2pc.app.ui.BatteryTip
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** SYNC-20261115-377 (unlesbares Passwort im Editor), -384 (Fotos/Videos beide aus), -382 (Akku-Tipp) */
class EditorRulesTest {
    @Test fun unreadablePasswordKeepsOriginalCiphertext() {
        assertEquals("OLD", PasswordKeep.encFor("OLD", true, "") { "ENC($it)" })
    }

    @Test fun typedPasswordIsEncryptedEvenIfOldWasUnreadable() {
        assertEquals("ENC(neu)", PasswordKeep.encFor("OLD", true, "neu") { "ENC($it)" })
    }

    @Test fun readablePasswordIsEncryptedAsBefore() {
        assertEquals("ENC()", PasswordKeep.encFor("OLD", false, "") { "ENC($it)" })
    }

    @Test fun importNormalizesPhotosProfileWithBothOff() {
        assertEquals(true to true, BackupCodec.mediaFlags(false, false, photosProfile = true))
        assertEquals(false to true, BackupCodec.mediaFlags(false, true, photosProfile = true))
        assertEquals(false to false, BackupCodec.mediaFlags(false, false, photosProfile = false))
    }

    @Test fun batteryTipOnlyWhenMissingAndNotSkipped() {
        assertTrue(BatteryTip.shouldShow(needsBase = true, batteryUnrestricted = false, skipped = false))
        assertFalse(BatteryTip.shouldShow(true, false, skipped = true))
        assertFalse(BatteryTip.shouldShow(true, true, false))
        assertFalse(BatteryTip.shouldShow(false, false, false))
    }
}