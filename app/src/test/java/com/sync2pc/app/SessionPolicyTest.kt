package com.sync2pc.app

import com.sync2pc.app.data.CredentialCrypto
import com.sync2pc.app.smb.SessionPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** SYNC-20261114-334: Gast-/Anonymsitzung (Punkt 6) und Einordnung von Keystore-Fehlern (Punkt 1) */
class SessionPolicyTest {
    @Test fun guestOrAnonymousIsRejectedWhenAUserIsConfigured() {
        assertTrue(SessionPolicy.rejectGuest(true, isGuest = true, isAnonymous = false))
        assertTrue(SessionPolicy.rejectGuest(true, isGuest = false, isAnonymous = true))
        assertFalse(SessionPolicy.rejectGuest(true, isGuest = false, isAnonymous = false))
    }

    @Test fun profilesWithoutUserKeepDeliberateGuestAccess() {
        assertFalse(SessionPolicy.rejectGuest(false, isGuest = true, isAnonymous = true))
        assertFalse(SessionPolicy.rejectGuest(false, isGuest = false, isAnonymous = false))
    }

    @Test fun unreadableKinds() {
        for (e in listOf<Throwable>(
            javax.crypto.AEADBadTagException("tag"), javax.crypto.BadPaddingException("x"), javax.crypto.IllegalBlockSizeException("x"),
            java.security.UnrecoverableKeyException("x"), IllegalArgumentException("base64"),
        )) assertTrue(e.javaClass.simpleName, CredentialCrypto.classify(e))
    }

    @Test fun otherKeystoreErrorsAreOnlyUnavailable() {
        for (e in listOf<Throwable>(
            java.security.KeyStoreException("nicht bereit"), java.security.ProviderException("x"), IllegalStateException("x"),
        )) assertFalse(e.javaClass.simpleName, CredentialCrypto.classify(e))
    }
}