package com.sync2pc.app

import com.sync2pc.app.smb.EncryptionStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** SYNC-20261116-446: Prüfverbindung für den Verschlüsselungsstatus nur bei unbekanntem Status und höchstens einmal je Profilstand */
class EncryptionStatusTest {
    private val fp = "REQUIRED|\\\\192.168.3.3\\fotos|"

    @Test fun unknownAndNeverTriedVerifies() {
        assertTrue(EncryptionStatus.shouldVerify(lastKnown = null, attemptedFingerprint = null, fingerprint = fp))
    }

    @Test fun knownStatusNeverVerifies() {
        assertFalse(EncryptionStatus.shouldVerify(true, null, fp))
        assertFalse(EncryptionStatus.shouldVerify(false, null, fp)) // zuletzt unverschlüsselt: gilt, nicht erneut anmelden
    }

    @Test fun failedAttemptNotRepeatedForSameProfileState() {
        assertFalse(EncryptionStatus.shouldVerify(null, attemptedFingerprint = fp, fingerprint = fp))
    }

    @Test fun profileChangeAllowsNewAttempt() {
        assertTrue(EncryptionStatus.shouldVerify(null, attemptedFingerprint = "AUTO|x|", fingerprint = fp))
    }
}
