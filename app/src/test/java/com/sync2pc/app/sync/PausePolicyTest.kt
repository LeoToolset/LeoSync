package com.sync2pc.app.sync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Eigene Laufzeitgrenze des Pausendienstes ab Android 15 (SYNC-20261115-389) */
class PausePolicyTest {
    @Test fun noLimitBelowAndroid15() {
        assertNull(PausePolicy.limitMillis(34))
        assertNull(PausePolicy.limitMillis(29))
    }

    @Test fun thirtyMinutesFromAndroid15() {
        assertEquals(30 * 60_000L, PausePolicy.limitMillis(35))
        assertEquals(30 * 60_000L, PausePolicy.limitMillis(36))
    }

    @Test fun resumeRunInWrongWifiDoesNotRestartFromAndroid15() {
        assertFalse(PausePolicy.mayStart(35, resumeRun = true, midRun = false))
    }

    @Test fun newInterruptionAlwaysStarts() {
        assertTrue(PausePolicy.mayStart(35, resumeRun = true, midRun = true))
        assertTrue(PausePolicy.mayStart(35, resumeRun = false, midRun = false))
        assertTrue(PausePolicy.mayStart(35, resumeRun = false, midRun = true))
    }

    @Test fun android14UnchangedAlwaysStarts() {
        assertTrue(PausePolicy.mayStart(34, resumeRun = true, midRun = false))
    }
}