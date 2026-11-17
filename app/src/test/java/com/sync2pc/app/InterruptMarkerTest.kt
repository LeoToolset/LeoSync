package com.sync2pc.app

import androidx.work.WorkInfo
import com.sync2pc.app.sync.InterruptMarker
import com.sync2pc.app.sync.SyncDecisions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** SYNC-20261116-425: Unterbruch nach einem Stopp früh und genau einmal markieren */
class InterruptMarkerTest {
    @Test fun networkConstraintStopMarks() {
        assertTrue(SyncDecisions.resumeAfterStop(true, WorkInfo.STOP_REASON_CONSTRAINT_CONNECTIVITY, manual = false))
    }

    @Test fun userCancelAndManualRunDoNotMark() {
        assertFalse(SyncDecisions.resumeAfterStop(true, WorkInfo.STOP_REASON_CANCELLED_BY_APP, manual = false))
        assertFalse(SyncDecisions.resumeAfterStop(true, WorkInfo.STOP_REASON_CONSTRAINT_CONNECTIVITY, manual = true))
    }

    @Test fun notStoppedNeverMarks() {
        assertFalse(SyncDecisions.resumeAfterStop(false, WorkInfo.STOP_REASON_CONSTRAINT_CONNECTIVITY, manual = false))
    }

    @Test fun markRunsOnlyOncePerRun() {
        val m = InterruptMarker()
        var calls = 0
        assertTrue(m.mark { calls++ })
        assertFalse(m.mark { calls++ }) // zweiter Aufruf aus dem Abschluss: kein zweiter Fortsetzen-Auftrag, kein zweiter Dienststart
        assertEquals(1, calls)
        assertTrue(m.marked)
    }

    @Test fun waitsForStopReasonOnlyBriefly() {
        assertTrue(SyncDecisions.stopReasonPending(isStopped = false, waitedMs = 0, maxMs = 2_000))
        assertFalse(SyncDecisions.stopReasonPending(isStopped = true, waitedMs = 0, maxMs = 2_000))
        assertFalse(SyncDecisions.stopReasonPending(isStopped = false, waitedMs = 2_000, maxMs = 2_000))
    }
}