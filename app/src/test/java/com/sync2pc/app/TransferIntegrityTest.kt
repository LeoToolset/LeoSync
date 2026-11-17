package com.sync2pc.app

import com.sync2pc.app.mirror.LocalEntry
import com.sync2pc.app.mirror.TransferIntegrity
import com.sync2pc.app.mirror.TransferIntegrity.Stamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** SYNC-20261114-333: reine Entscheidungen zur Übertragungsintegrität (Fortsetzen, Hash-Pflicht, Zielprüfung, Kopie, Konflikt) */
class TransferIntegrityTest {
    private val cur = Stamp(1000, 5_000)

    // ---- R-T1/R-I6: Fortsetzen nur mit gespeicherter, identischer Identität ----
    @Test fun resumesOnlyWithMatchingStoredIdentity() {
        assertTrue(TransferIntegrity.mayResume(400, cur, cur))
        assertFalse("kein Eintrag = neu", TransferIntegrity.mayResume(400, null, cur))
        assertFalse("PC-Datei seither geändert (Zeit)", TransferIntegrity.mayResume(400, Stamp(1000, 4_000), cur))
        assertFalse("PC-Datei seither geändert (Grösse)", TransferIntegrity.mayResume(400, Stamp(900, 5_000), cur))
    }

    @Test fun clockOfThePhoneIsNoLongerInvolved() {
        // Gleiche Identität genügt, egal welche Zeit die Temp-Datei hat: der Vergleich mit der Handy-Uhr entfällt
        assertTrue(TransferIntegrity.mayResume(1, cur, cur))
        assertTrue(TransferIntegrity.mayResume(999, cur, cur))
    }

    @Test fun tmpSizeMustBePartial() {
        assertFalse("leer", TransferIntegrity.mayResume(0, cur, cur))
        assertFalse("schon vollständig", TransferIntegrity.mayResume(1000, cur, cur))
        assertFalse("grösser als das Original", TransferIntegrity.mayResume(1500, cur, cur))
    }

    @Test fun unknownTimeNeverResumes() {
        val noTime = Stamp(1000, 0)
        assertFalse(TransferIntegrity.mayResume(400, noTime, noTime))
    }

    @Test fun identityRoundTripsAndBadValuesAreNoIdentity() {
        assertEquals(cur, TransferIntegrity.decode(TransferIntegrity.encode(cur)))
        for (bad in listOf(null, "", "x", "1|2|3", "a|b", "1|", "|2")) assertNull("«$bad»", TransferIntegrity.decode(bad))
    }

    @Test fun identityKeyIsProfileAndLowercasePath() {
        assertEquals("7|dir/datei.txt", TransferIntegrity.identityKey(7, "Dir/Datei.TXT"))
        assertFalse(TransferIntegrity.identityKey(7, "a") == TransferIntegrity.identityKey(8, "a"))
    }

    // ---- R-T2: Teil-Hash ab 4 MiB und nach jedem Fortsetzen ----
    @Test fun hashCheckAfterResumeAndFromFourMiB() {
        val four = TransferIntegrity.HASH_MIN_BYTES
        assertEquals(4L * 1024 * 1024, four)
        assertTrue(TransferIntegrity.needsHashCheck(10, resumed = true))
        assertTrue(TransferIntegrity.needsHashCheck(four, resumed = false))
        assertTrue(TransferIntegrity.needsHashCheck(four + 1, resumed = false))
        assertFalse(TransferIntegrity.needsHashCheck(four - 1, resumed = false))
        assertFalse(TransferIntegrity.needsHashCheck(0, resumed = false))
    }

    // ---- R-I5: Ziel unmittelbar vor dem Ersetzen ----
    @Test fun targetMustBeAsExpected() {
        assertTrue(TransferIntegrity.targetUnchanged(cur, cur))
        assertTrue("sollte fehlen und fehlt", TransferIntegrity.targetUnchanged(null, null))
        assertFalse("neu entstanden", TransferIntegrity.targetUnchanged(null, cur))
        assertFalse("verschwunden", TransferIntegrity.targetUnchanged(cur, null))
        assertFalse("geändert", TransferIntegrity.targetUnchanged(cur, Stamp(1000, 6_000)))
        assertFalse("grösser", TransferIntegrity.targetUnchanged(cur, Stamp(1001, 5_000)))
    }

    /** KEEP_BOTH-Verlustpfad: der Originalname ist nach dem Umbenennen frei; eine dort neu angelegte Datei darf nie ersetzt werden */
    @Test fun keepBothDoesNotReplaceAFileCreatedAtTheOriginalName() {
        val created = TransferIntegrity.stampOf(LocalEntry("id", "a.txt", 5, 99, false))
        assertFalse(TransferIntegrity.targetUnchanged(null, created))
        assertTrue(TransferIntegrity.targetUnchanged(null, null))
    }

    // ---- R-I7: Kopie vor dem Löschen des Originals ----
    @Test fun copyMustExistAndHaveTheExactSize() {
        assertTrue(TransferIntegrity.copyIsComplete(500, 500))
        assertFalse(TransferIntegrity.copyIsComplete(null, 500))
        assertFalse(TransferIntegrity.copyIsComplete(499, 500))
        assertFalse(TransferIntegrity.copyIsComplete(501, 500))
        assertTrue("leere Datei", TransferIntegrity.copyIsComplete(0, 0))
    }

    // ---- R-K3: Konfliktentscheidung nur für die entschiedenen Versionen ----
    @Test fun conflictDecisionHoldsOnlyForTheDecidedVersions() {
        val l = Stamp(10, 100); val r = Stamp(20, 200)
        assertTrue(TransferIntegrity.conflictStillCurrent(10, 100, 20, 200, l, r))
        assertFalse("Handy geändert", TransferIntegrity.conflictStillCurrent(10, 100, 20, 200, Stamp(10, 101), r))
        assertFalse("PC geändert", TransferIntegrity.conflictStillCurrent(10, 100, 20, 200, l, Stamp(21, 200)))
        assertFalse("beide geändert", TransferIntegrity.conflictStillCurrent(10, 100, 20, 200, Stamp(11, 100), Stamp(20, 201)))
    }
}
