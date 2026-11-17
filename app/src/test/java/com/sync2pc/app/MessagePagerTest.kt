package com.sync2pc.app

import com.sync2pc.app.ui.MessagePager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Blätterkachel «Meldungen» (SYNC-20261115-407): Zähler, Erhalt der gezeigten Seite, Hinweis statt Springen */
class MessagePagerTest {
    private val keys = listOf("a", "b", "c")

    @Test fun shownPageIsKeptByKeyWhenListChanges() {
        assertEquals(1, MessagePager.indexOf(keys, "b"))
        assertEquals(2, MessagePager.indexOf(listOf("x", "a", "b"), "b"))
    }

    @Test fun missingOrNoShownKeyFallsBackToFirst() {
        assertEquals(0, MessagePager.indexOf(keys, "weg"))
        assertEquals(0, MessagePager.indexOf(keys, null))
        assertEquals(0, MessagePager.indexOf(emptyList(), "a"))
    }

    @Test fun stepsStopAtTheEnds() {
        assertEquals(0, MessagePager.step(0, -1, 3))
        assertEquals(2, MessagePager.step(2, 1, 3))
        assertEquals(1, MessagePager.step(0, 1, 3))
        assertFalse(MessagePager.canPrev(0)); assertTrue(MessagePager.canPrev(1))
        assertTrue(MessagePager.canNext(1, 3)); assertFalse(MessagePager.canNext(2, 3))
    }

    @Test fun hintOnlyForNewMoreImportantPage() {
        val levels = mapOf("a" to 3, "b" to 4, "n" to 1, "m" to 5)
        // «n» (Stufe 1) ist neu und wichtiger als die gezeigte «b» (Stufe 4)
        assertEquals("n", MessagePager.moreImportantArrived(listOf("n", "a", "b"), levels, "b", setOf("a", "b")))
        // schon bekannt: kein Hinweis
        assertNull(MessagePager.moreImportantArrived(listOf("n", "a", "b"), levels, "b", setOf("a", "b", "n")))
        // neu, aber weniger wichtig: kein Hinweis
        assertNull(MessagePager.moreImportantArrived(listOf("a", "b", "m"), levels, "a", setOf("a", "b")))
    }

    // SYNC-20261115-416 B: Färbung nach der höchsten Schwere
    @Test fun severityFollowsHighestLevel() {
        assertEquals(MessagePager.Severity.ERROR, MessagePager.severityOf(listOf(5, 3, 6)))
        assertEquals(MessagePager.Severity.ERROR, MessagePager.severityOf(listOf(1)))
        assertEquals(MessagePager.Severity.WARNING, MessagePager.severityOf(listOf(4, 6)))
        assertEquals(MessagePager.Severity.WARNING, MessagePager.severityOf(listOf(5)))
        assertEquals(MessagePager.Severity.INFO, MessagePager.severityOf(listOf(6, 6)))
        assertEquals(MessagePager.Severity.INFO, MessagePager.severityOf(emptyList()))
    }

    // E: zusammengefasste Gruppe erst, wenn kein Ordner-/Verbindungstest-Schritt mehr offen ist
    @Test fun checklistSummaryOnlyWhenNoFolderOrTestStepOpen() {
        val sync = com.sync2pc.app.ui.ChecklistStep.SYNC
        assertTrue(MessagePager.checklistSummaryOnly(listOf(sync, sync)))
        assertFalse(MessagePager.checklistSummaryOnly(listOf(sync, com.sync2pc.app.ui.ChecklistStep.TEST)))
        assertFalse(MessagePager.checklistSummaryOnly(listOf(com.sync2pc.app.ui.ChecklistStep.FOLDER)))
        assertFalse(MessagePager.checklistSummaryOnly(emptyList()))
    }

    @Test fun mostImportantNewPageWins() {
        val levels = mapOf("shown" to 5, "x" to 3, "y" to 2)
        assertEquals("y", MessagePager.moreImportantArrived(listOf("x", "y", "shown"), levels, "shown", setOf("shown")))
    }
}