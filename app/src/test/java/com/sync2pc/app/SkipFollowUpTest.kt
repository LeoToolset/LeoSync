package com.sync2pc.app

import com.sync2pc.app.sync.SkipFollowUp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Begrenzung des Folgelaufs nach übersprungenen Dateien (SYNC-20261111-173) */
class SkipFollowUpTest {
    private val hour = SkipFollowUp.MIN_INTERVAL_MS

    @Test fun nothingDeferredPlansNothing() = assertFalse(SkipFollowUp.mayPlan(0, 10 * hour, 0))

    @Test fun firstTimePlans() = assertTrue(SkipFollowUp.mayPlan(2, 10 * hour, 0))

    @Test fun atMostOnePerHour() {
        val last = 10 * hour
        assertFalse(SkipFollowUp.mayPlan(1, last + 5 * 60_000, last))
        assertFalse(SkipFollowUp.mayPlan(1, last + hour - 1, last))
        assertTrue(SkipFollowUp.mayPlan(1, last + hour, last))
    }

    @Test fun clockMovedBackwardsDoesNotBlockForever() = assertTrue(SkipFollowUp.mayPlan(1, 5 * hour, 10 * hour))

    // ---- SYNC-20261114-334 (5): Sperre für Folgeläufe allein wegen des Ordnerstempels ----
    private val idle = SkipFollowUp.FOLDER_IDLE_MS

    @Test fun folderOnlyTriggerIsBlockedWhileIdleLockHolds() {
        val now = 100 * hour
        assertFalse(SkipFollowUp.mayPlan(0, now, 0, changedDuringRun = true, folderIdleUntil = now + idle / 2))
        assertTrue("ohne Sperre", SkipFollowUp.mayPlan(0, now, 0, changedDuringRun = true, folderIdleUntil = 0))
        assertTrue("Sperre abgelaufen", SkipFollowUp.mayPlan(0, now, 0, changedDuringRun = true, folderIdleUntil = now - 1))
    }

    @Test fun deferredFilesStillPlanDespiteTheLock() {
        val now = 100 * hour
        assertTrue(SkipFollowUp.mayPlan(2, now, 0, changedDuringRun = true, folderIdleUntil = now + idle / 2))
    }

    @Test fun lockFarInTheFutureMeansClockWasMovedAndEnds() {
        val now = 100 * hour
        assertFalse(SkipFollowUp.folderIdle(now, now + idle + 1))
        assertTrue(SkipFollowUp.folderIdle(now, now + idle))
    }

    @Test fun emptyFollowUpSetsTheLockAndAnyWorkClearsIt() {
        val now = 100 * hour
        assertEquals(now + idle, SkipFollowUp.idleUntilAfter(0, now, wasSkipFollowUp = true, didWork = false))
        assertEquals("Arbeit hebt die Sperre auf", 0L, SkipFollowUp.idleUntilAfter(now + idle, now, wasSkipFollowUp = true, didWork = true))
        assertEquals("anderer leerer Lauf lässt sie unverändert", now + 5, SkipFollowUp.idleUntilAfter(now + 5, now, wasSkipFollowUp = false, didWork = false))
        assertEquals("Arbeit eines normalen Laufs hebt sie auf", 0L, SkipFollowUp.idleUntilAfter(now + 5, now, wasSkipFollowUp = false, didWork = true))
    }
}
