package com.sync2pc.app

import com.sync2pc.app.sync.ForegroundCheck
import com.sync2pc.app.ui.BatteryTip
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** SYNC-20261116-434: Tipp-Karte erscheint nach «Nicht jetzt» höchstens einmal erneut, nämlich nach einem Lauf ohne Vordergrund */
class BatteryTipTest {
    @Test fun neverSkippedShows() {
        assertTrue(BatteryTip.shouldShow(needsBase = true, batteryUnrestricted = false, skipped = false))
    }

    @Test fun skippedOnceWithoutEventStaysHidden() {
        assertFalse(BatteryTip.shouldShow(true, false, skipped = true, eventAfterSkip = false))
    }

    @Test fun skippedOnceWithEventShowsAgain() {
        assertTrue(BatteryTip.shouldShow(true, false, skipped = true, eventAfterSkip = true))
    }

    @Test fun skippedTwiceStaysHiddenEvenWithEvent() {
        assertFalse(BatteryTip.shouldShow(true, false, skipped = true, eventAfterSkip = true, finalSkipped = true))
    }

    @Test fun exceptionSetNeverShows() {
        assertFalse(BatteryTip.shouldShow(true, true, skipped = false))
        assertFalse(BatteryTip.shouldShow(true, true, skipped = true, eventAfterSkip = true))
    }

    @Test fun eventCountsOnlyAfterFirstSkip() {
        assertFalse(BatteryTip.eventAfterSkip(skippedAt = 0, noForegroundAt = 500))   // nie zurückgestellt
        assertFalse(BatteryTip.eventAfterSkip(skippedAt = 1000, noForegroundAt = 500)) // Ereignis davor
        assertFalse(BatteryTip.eventAfterSkip(skippedAt = 1000, noForegroundAt = 0))
        assertTrue(BatteryTip.eventAfterSkip(skippedAt = 1000, noForegroundAt = 1500))
    }

    @Test fun foregroundDetectedOnlyForRunningForegroundService() {
        val wm = "androidx.work.impl.foreground.SystemForegroundService"
        assertTrue(ForegroundCheck.anyForeground(listOf(wm to true)))
        assertFalse(ForegroundCheck.anyForeground(listOf(wm to false)))
        assertFalse(ForegroundCheck.anyForeground(listOf("com.sync2pc.app.sync.PauseService" to true)))
        assertFalse(ForegroundCheck.anyForeground(emptyList()))
    }
}
