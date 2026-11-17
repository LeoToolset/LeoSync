package com.sync2pc.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.sync2pc.app.data.GlobalSettings
import com.sync2pc.app.sync.DeferredKind
import com.sync2pc.app.sync.SkipFollowUp
import com.sync2pc.app.sync.SyncScheduler
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * SYNC-20261114-332 Punkt 4: Verdrahtung von Nachlauf-Merker und Folgelauf mit WorkManager (work-testing, Vorbild
 * SyncWorkerGateTest). Nachlauf: ein bei Freigabe vorgemerkter manueller Start wird als `sync-deferred-manual` eingereiht,
 * «Pausieren» verwirft den Merker (DEC-221). Folgelauf: nach einem Lauf mit verschobenen Dateien `sync-skip-followup`,
 * ein zweiter innerhalb einer Stunde nicht (DEC-238, `lastSkipFollowUpAt`). Läuft auf Gerät/Emulator
 * (`gradlew connectedDebugAndroidTest`); die Tests nutzen die echte Prozess-Sperre und werden übersprungen, wenn gerade ein
 * echter Sync läuft oder ein Nachlauf vorgemerkt ist.
 */
@RunWith(AndroidJUnit4::class)
class SchedulerWiringTest {
    private lateinit var ctx: Context
    private lateinit var wm: WorkManager
    private lateinit var settings: GlobalSettings
    private var acquired = false
    private var savedLastSkip = 0L
    private var savedPausedUntil = 0L
    private val gate get() = SyncScheduler.gate(ctx)

    @Before fun setUp() {
        ctx = ApplicationProvider.getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(ctx)
        wm = WorkManager.getInstance(ctx)
        settings = GlobalSettings(ctx)
        assumeTrue("Es läuft bereits ein Sync oder ein Nachlauf ist vorgemerkt: Test übersprungen",
            !gate.isRunning && !gate.state.value.requested)
        savedLastSkip = settings.lastSkipFollowUpAt
        savedPausedUntil = settings.pausedUntil
        settings.pausedUntil = 0
        settings.lastSkipFollowUpAt = 0
    }

    @After fun tearDown() {
        if (acquired) gate.release()
        if (::settings.isInitialized) { settings.lastSkipFollowUpAt = savedLastSkip; settings.pausedUntil = savedPausedUntil }
        if (::wm.isInitialized) wm.cancelAllWork()
    }

    private fun states(name: String): List<WorkInfo.State> = wm.getWorkInfosForUniqueWork(name).get().map { it.state }

    @Test fun deferredManualStartIsEnqueuedOnceOnRelease() {
        assertTrue(gate.tryAcquire()); acquired = true
        assertTrue("Start während des Laufs wird vorgemerkt", gate.requestStart(manual = true))
        val kind = gate.release(); acquired = false
        assertEquals(DeferredKind.MANUAL, kind)
        SyncScheduler.enqueueDeferred(ctx, kind!!)
        assertEquals(1, states(SyncScheduler.WORK_DEFERRED_MANUAL).size)
        assertTrue("der automatische Nachlauf wird nicht zusätzlich eingereiht", states(SyncScheduler.WORK_DEFERRED_AUTO).isEmpty())
        assertEquals("Merker genau einmal: nach der Freigabe leer", null, gate.release())
    }

    @Test fun pauseDiscardsThePendingMarker() {
        assertTrue(gate.tryAcquire()); acquired = true
        assertTrue(gate.requestStart(manual = true))
        assertTrue(gate.state.value.requested)
        SyncScheduler.pause(ctx, 1)
        try {
            assertFalse("«Pausieren» löscht den Merker (DEC-221)", gate.state.value.requested)
            assertEquals(null, gate.release().also { acquired = false })
            assertTrue("nach der Freigabe wird nichts nachgeholt", states(SyncScheduler.WORK_DEFERRED_MANUAL).isEmpty())
        } finally {
            SyncScheduler.clearPause(ctx)
        }
    }

    @Test fun skipFollowUpIsPlannedOnceAndNotWithinTheHour() {
        val now = 10_000_000_000L
        assertTrue(SyncScheduler.planSkipFollowUp(ctx, settings, false, 2, false, now))
        assertEquals(now, settings.lastSkipFollowUpAt)
        assertEquals(1, states(SyncScheduler.WORK_SKIP_FOLLOWUP).size)
        // zweiter Lauf mit verschobenen Dateien innerhalb einer Stunde: nichts Neues, Zeitstempel unverändert
        assertFalse(SyncScheduler.planSkipFollowUp(ctx, settings, false, 3, false, now + 30 * 60_000L))
        assertEquals(now, settings.lastSkipFollowUpAt)
        assertEquals(1, states(SyncScheduler.WORK_SKIP_FOLLOWUP).size)
        // nach einer Stunde wieder möglich
        assertTrue(SyncScheduler.planSkipFollowUp(ctx, settings, false, 3, false, now + SkipFollowUp.MIN_INTERVAL_MS))
    }

    @Test fun skipFollowUpNeedsADeferredFileOrAFolderChange() {
        assertFalse(SyncScheduler.planSkipFollowUp(ctx, settings, false, 0, false, 1_000L))
        assertEquals(0L, settings.lastSkipFollowUpAt)
        assertTrue(SyncScheduler.planSkipFollowUp(ctx, settings, false, 0, true, 1_000L))
    }

    @Test fun skipFollowUpIsNotPlannedAfterAnInterruptionOrDuringAPause() {
        assertFalse(SyncScheduler.planSkipFollowUp(ctx, settings, true, 5, false, 1_000L))
        assertEquals(0L, settings.lastSkipFollowUpAt)
        settings.pausedUntil = System.currentTimeMillis() + 3_600_000L
        assertFalse(SyncScheduler.planSkipFollowUp(ctx, settings, false, 5, false, 1_000L))
        assertEquals("kein Stundenkontingent verbraucht", 0L, settings.lastSkipFollowUpAt)
        assertTrue(states(SyncScheduler.WORK_SKIP_FOLLOWUP).isEmpty())
    }
}
