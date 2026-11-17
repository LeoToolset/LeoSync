package com.sync2pc.app.smb

import com.hierynomus.smb.SMBPacketData
import com.hierynomus.smbj.connection.Connection
import com.hierynomus.smbj.connection.packet.AbstractIncomingPacketHandler
import com.hierynomus.smbj.connection.packet.IncomingPacketHandler
import com.hierynomus.smbj.connection.packet.SMB2CompoundedPacketHandler
import com.sync2pc.app.data.SmbEncryption
import com.sync2pc.app.smb.PacketPolicy.Mode
import com.sync2pc.app.smb.PacketPolicy.Verdict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/** Antwortprüfung je Verbindung (SYNC-20261115-365, korrigiert in -371) und Signier-Merker (B11b) */
class PacketPolicyTest {
    private val signed = Mode.Signed(9L)

    private fun decide(
        mode: Mode, smb2: Boolean = true, decrypted: Boolean = false, oplock: Boolean = false, session: Long = 9L,
        flag: Boolean = false, pending: Boolean = false,
    ) = PacketPolicy.decide(mode, smb2, decrypted, oplock, session, flag, pending)

    // --- vor der Anmeldung ---
    @Test fun noneLetsEverythingPass() {
        assertEquals(Verdict.PASS, decide(Mode.None))
        assertEquals(Verdict.PASS, decide(Mode.None, smb2 = false))
        assertEquals(Verdict.PASS, decide(Mode.None, session = 0L))
    }

    // --- verschlüsselte Verbindung: jede Klartext-Nachricht wird abgewiesen ---
    @Test fun encryptedRejectsPlaintextWhateverTheSession() {
        assertEquals(Verdict.PLAINTEXT_IN_ENCRYPTED_SESSION, decide(Mode.Encrypted, session = 9L))
        // Umgehung aus -365: SessionId 0 und fremde SessionId
        assertEquals(Verdict.PLAINTEXT_IN_ENCRYPTED_SESSION, decide(Mode.Encrypted, session = 0L))
        assertEquals(Verdict.PLAINTEXT_IN_ENCRYPTED_SESSION, decide(Mode.Encrypted, session = 12345L))
        // mit Signatur-Flag, als Zwischenantwort, nicht-SMB2: immer noch Klartext
        assertEquals(Verdict.PLAINTEXT_IN_ENCRYPTED_SESSION, decide(Mode.Encrypted, flag = true))
        assertEquals(Verdict.PLAINTEXT_IN_ENCRYPTED_SESSION, decide(Mode.Encrypted, pending = true))
        assertEquals(Verdict.PLAINTEXT_IN_ENCRYPTED_SESSION, decide(Mode.Encrypted, smb2 = false))
    }

    @Test fun encryptedRejectsPlaintextSessionSetupAfterLogin() {
        // SESSION_SETUP wird nach der Anmeldung nicht mehr ausgenommen (die Regel kennt keinen Befehl mehr)
        assertEquals(Verdict.PLAINTEXT_IN_ENCRYPTED_SESSION, decide(Mode.Encrypted, session = 0L, flag = false))
    }

    @Test fun encryptedAllowsDecryptedAndOplockBreak() {
        assertEquals(Verdict.PASS, decide(Mode.Encrypted, decrypted = true))
        assertEquals(Verdict.PASS, decide(Mode.Encrypted, decrypted = true, session = 0L, pending = true))
        assertEquals(Verdict.PASS, decide(Mode.Encrypted, oplock = true, session = 0L))
    }

    // --- Verbindung mit Signierpflicht ---
    @Test fun signedAllowsOnlyOwnSessionWithFlagOrIntermediate() {
        assertEquals(Verdict.PASS, decide(signed, session = 9L, flag = true))
        assertEquals(Verdict.PASS, decide(signed, session = 9L, flag = false, pending = true)) // Zwischenantwort
        assertEquals(Verdict.UNSIGNED_IN_SIGNED_SESSION, decide(signed, session = 9L, flag = false))
    }

    @Test fun signedRejectsForeignAndZeroSessionEvenWithFlag() {
        assertEquals(Verdict.UNSIGNED_IN_SIGNED_SESSION, decide(signed, session = 0L, flag = true))
        assertEquals(Verdict.UNSIGNED_IN_SIGNED_SESSION, decide(signed, session = 5L, flag = true))
        assertEquals(Verdict.UNSIGNED_IN_SIGNED_SESSION, decide(signed, session = 0L, flag = false))
        // auch eine fremde Zwischenantwort
        assertEquals(Verdict.UNSIGNED_IN_SIGNED_SESSION, decide(signed, session = 5L, pending = true))
        assertEquals(Verdict.UNSIGNED_IN_SIGNED_SESSION, decide(signed, smb2 = false, flag = true))
    }

    @Test fun signedAllowsOplockBreak() {
        assertEquals(Verdict.PASS, decide(signed, oplock = true, session = 0L, flag = false))
    }

    // --- Modus nach der Anmeldung ---
    @Test fun modeAfterLogin() {
        assertEquals(Mode.Encrypted, PacketPolicy.modeAfterLogin(true, true, 9L)) // verschlüsselt: Signierpflicht entfällt
        assertEquals(Mode.Signed(9L), PacketPolicy.modeAfterLogin(false, true, 9L))
        assertEquals(Mode.None, PacketPolicy.modeAfterLogin(false, false, 9L))
    }

    @Test fun failClosedWithoutGuard() {
        assertTrue(PacketPolicy.mustAbortWithoutGuard(SmbEncryption.REQUIRED, signingRemembered = false, installed = false))
        assertTrue(PacketPolicy.mustAbortWithoutGuard(SmbEncryption.PREFERRED, signingRemembered = true, installed = false))
        assertTrue(PacketPolicy.mustAbortWithoutGuard(SmbEncryption.OFF, signingRemembered = true, installed = false))
        assertFalse(PacketPolicy.mustAbortWithoutGuard(SmbEncryption.PREFERRED, signingRemembered = false, installed = false))
        assertFalse(PacketPolicy.mustAbortWithoutGuard(SmbEncryption.OFF, signingRemembered = false, installed = false))
        assertFalse(PacketPolicy.mustAbortWithoutGuard(SmbEncryption.REQUIRED, signingRemembered = true, installed = true))
    }

    // --- Einbau in die smbj-Kette ---
    private class Recorder : AbstractIncomingPacketHandler() {
        override fun canHandle(data: SMBPacketData<*>): Boolean = true
        override fun doHandle(data: SMBPacketData<*>) {}
    }

    /** Wächtertest: schlägt bei einem smbj-Update fehl, wenn Feld oder Handlerklassen sich ändern */
    @Test fun handlerChainFieldsAreReachableByReflection() {
        val chain = Connection::class.java.getDeclaredField(SessionGuard.CHAIN_FIELD)
        assertTrue(IncomingPacketHandler::class.java.isAssignableFrom(chain.type))
        assertFalse(java.lang.reflect.Modifier.isFinal(chain.modifiers))
        val next = AbstractIncomingPacketHandler::class.java.getDeclaredField(SessionGuard.NEXT_FIELD)
        assertTrue(IncomingPacketHandler::class.java.isAssignableFrom(next.type))
        assertFalse(java.lang.reflect.Modifier.isFinal(next.modifiers))
        assertTrue(AbstractIncomingPacketHandler::class.java.isAssignableFrom(SMB2CompoundedPacketHandler::class.java))
    }

    @Test fun guardIsInsertedBehindCompoundHandlerAndIdempotent() {
        val compound = SMB2CompoundedPacketHandler()
        val after = Recorder()
        compound.setNext(after)
        val g = SessionGuard.installBehindCompound(compound)
        assertNotNull(g)
        val nextField = AbstractIncomingPacketHandler::class.java.getDeclaredField("next").apply { isAccessible = true }
        assertSame(g, nextField.get(compound))
        // zweiter Einbau liefert denselben Guard und keinen zweiten
        assertSame(g, SessionGuard.installBehindCompound(compound))
        assertSame(g, nextField.get(compound))
    }

    @Test fun installFailsWithoutCompoundHandler() {
        assertNull(SessionGuard.installBehindCompound(Recorder()))
        assertNull(SessionGuard.installBehindCompound(null))
    }

    // --- B11b ---
    @Test fun signingIsRememberedOnlyWhenUnencryptedAndRealUser() {
        val remember = { level: SmbEncryption, enc: Boolean, guest: Boolean, anon: Boolean, req: Boolean ->
            SigningPolicy.shouldRemember(level, enc, guest, anon, req)
        }
        assertTrue(remember(SmbEncryption.PREFERRED, false, false, false, true))
        assertTrue(remember(SmbEncryption.OFF, false, false, false, true))
        assertFalse(remember(SmbEncryption.PREFERRED, true, false, false, true))
        assertFalse(remember(SmbEncryption.REQUIRED, false, false, false, true))
        assertFalse(remember(SmbEncryption.PREFERRED, false, true, false, true))
        assertFalse(remember(SmbEncryption.PREFERRED, false, false, true, true))
        assertFalse(remember(SmbEncryption.PREFERRED, false, false, false, false))
    }

    @Test fun signingRequiredIsPassedToConfig() {
        assertTrue(smbConfigFor(SmbEncryption.PREFERRED, 30, signingRequired = true).isSigningRequired)
        assertFalse(smbConfigFor(SmbEncryption.PREFERRED, 30).isSigningRequired)
    }
}