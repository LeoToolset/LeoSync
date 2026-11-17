package com.sync2pc.app

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sync2pc.app.smb.EncryptionStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** SYNC-20261116-448: Nach dem endgültigen Löschen eines Profils bleiben keine enc_status-Einträge p<ID>/a<ID> */
@RunWith(AndroidJUnit4::class)
class EncryptionStatusForgetTest {
    @Test fun forgetRemovesStatusAndAttemptOfThatProfileOnly() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        EncryptionStatus.init(ctx)
        val prefs = ctx.getSharedPreferences("enc_status", Context.MODE_PRIVATE)
        prefs.edit().putString("p9001", "1|x").putString("a9001", "x").putString("p9002", "1|y").putString("a9002", "y").commit()

        EncryptionStatus.forget(9001L)
        // apply() ist asynchron, aber im selben Prozess sofort lesbar
        assertFalse(prefs.contains("p9001"))
        assertFalse(prefs.contains("a9001"))
        assertTrue(prefs.contains("p9002") && prefs.contains("a9002")) // andere Profile bleiben

        prefs.edit().remove("p9002").remove("a9002").commit()
    }
}
