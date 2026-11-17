package com.sync2pc.app

import com.sync2pc.app.mirror.NameCollisions
import com.sync2pc.app.mirror.NameCollisions.Kind
import com.sync2pc.app.mirror.WindowsNames
import com.sync2pc.app.mirror.WindowsNames.Reason
import com.sync2pc.app.mirror.ZeroTimeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** SYNC-20261114-337 (B5, B6): Windows-Namen, Namenskollisionen, Schwelle für Speicherorte ohne Änderungszeit */
class NameRulesTest {
    @Test fun ordinaryNamesAreValid() {
        for (n in listOf("a.txt", "Foto 1.jpg", "Ägypten", "über-uns.pdf", "a b", ".hidden", "file.tar.gz", "COM10", "CONSOLE", "nulled.txt", "x.nul"))
            assertNull("«$n»", WindowsNames.invalidReason(n))
    }

    @Test fun forbiddenCharactersAndControlCodes() {
        for (c in "<>:\"\\|?*") assertEquals("$c", Reason.INVALID_CHAR, WindowsNames.invalidReason("a${c}b"))
        assertEquals(Reason.INVALID_CHAR, WindowsNames.invalidReason("a\u0001b"))
        assertEquals(Reason.INVALID_CHAR, WindowsNames.invalidReason("a\u001Fb"))
        assertNull("Zeichen 32 (Leerzeichen) mitten im Namen ist erlaubt", WindowsNames.invalidReason("a b"))
    }

    @Test fun trailingDotOrSpaceIsInvalid() {
        assertEquals(Reason.TRAILING_DOT_OR_SPACE, WindowsNames.invalidReason("name."))
        assertEquals(Reason.TRAILING_DOT_OR_SPACE, WindowsNames.invalidReason("name "))
        assertEquals(Reason.TRAILING_DOT_OR_SPACE, WindowsNames.invalidReason("a.txt."))
    }

    @Test fun reservedDeviceNamesAlsoWithExtensionAndAnyCase() {
        for (n in listOf("CON", "con", "Prn", "AUX", "nul", "NUL.txt", "com1", "COM9.log", "LPT1", "lpt9.x", "con .txt", "Aux.tar.gz"))
            assertEquals("«$n»", Reason.RESERVED, WindowsNames.invalidReason(n))
        assertNull(WindowsNames.invalidReason("COM0"))
        assertNull(WindowsNames.invalidReason("LPT10"))
    }

    @Test fun invalidPartFindsTheFirstBadSegmentOfAPath() {
        assertNull(WindowsNames.invalidPart("Ordner/Unter/a.txt"))
        assertEquals("Aux" to Reason.RESERVED, WindowsNames.invalidPart("Ordner/Aux/a.txt"))
        assertEquals("x:y" to Reason.INVALID_CHAR, WindowsNames.invalidPart("x:y/a.txt"))
        assertEquals("a." to Reason.TRAILING_DOT_OR_SPACE, WindowsNames.invalidPart("Ordner\\a.\\b.txt"))
        assertNull(WindowsNames.invalidPart(""))
    }

    // ---- Kollisionen ----
    @Test fun caseOnlyDifferencesAreReportedOncePerPair() {
        val c = NameCollisions.find(listOf("Foto.jpg", "foto.jpg", "andere.jpg"))
        assertEquals(listOf(NameCollisions.Collision("Foto.jpg", "foto.jpg", Kind.CASE_ONLY)), c)
    }

    @Test fun nfcAndNfdSpellingsCollide() {
        val nfc = "Ägypten.txt"          // Ä als ein Zeichen
        val nfd = "Ägypten.txt"         // A + kombinierender Umlautpunkt
        val c = NameCollisions.find(listOf(nfc, nfd))
        assertEquals(1, c.size)
        assertEquals(Kind.NORMALIZATION, c[0].kind)
    }

    @Test fun identicalAndUnrelatedPathsDoNotCollide() {
        assertTrue(NameCollisions.find(listOf("a.txt", "a.txt", "b.txt")).isEmpty())
        assertTrue(NameCollisions.find(emptyList()).isEmpty())
    }

    @Test fun threeWayCaseGroupChainsPairs() {
        assertEquals(2, NameCollisions.find(listOf("ab", "Ab", "AB")).count { it.kind == Kind.CASE_ONLY })
    }

    // ---- R-M1 ----
    @Test fun zeroTimeThreshold() {
        assertFalse("zu wenige Dateien", ZeroTimeRule.applies(listOf(0, 0, 0, 0)))
        assertTrue(ZeroTimeRule.applies(List(5) { 0L }))
        assertTrue("9 von 10 = 90 %", ZeroTimeRule.applies(List(9) { 0L } + 123L))
        assertFalse("8 von 10", ZeroTimeRule.applies(List(8) { 0L } + listOf(1L, 2L)))
        assertFalse("alle mit Zeit", ZeroTimeRule.applies(List(20) { 100L }))
        assertFalse(ZeroTimeRule.applies(emptyList()))
    }
}
