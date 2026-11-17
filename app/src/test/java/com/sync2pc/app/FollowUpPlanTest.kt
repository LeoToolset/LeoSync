package com.sync2pc.app

import com.sync2pc.app.sync.FollowUpPlan
import com.sync2pc.app.sync.FollowUpPlan.Young
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Fristberechnung des Nachhol-Laufs für zu junge Fotos (SYNC-20261113-306) */
class FollowUpPlanTest {
    private val min = 10 * 60L // Mindestalter 10 Minuten
    private val now = 1_000_000L

    @Test fun seriesYieldsOldestAndNewestReadyTime() {
        // Serie: drei Fotos vor 8, 5 und 1 Minute(n) hinzugefügt -> reifen nach 2, 5 und 9 Minuten
        val times = listOf(now - 8 * 60 to now - 8 * 60, now - 5 * 60 to now - 5 * 60, now - 60 to now - 60)
        val y = FollowUpPlan.youngOf(times, now, min)
        assertEquals((now - 8 * 60 + min) * 1000, y.oldestAt)
        assertEquals((now - 60 + min) * 1000, y.newestAt)
        // Der Nachhol-Lauf liegt auf der NEUSTEN: dann sind alle drei reif (ein Lauf statt dreien)
        assertEquals(y.newestAt, FollowUpPlan.dueAt(y, 0))
    }

    @Test fun readyFilesAreNotYoung() {
        val y = FollowUpPlan.youngOf(listOf(now - 20 * 60 to now - 20 * 60), now, min)
        assertFalse(y.any)
        assertEquals(0L, FollowUpPlan.dueAt(y, 0))
    }

    @Test fun laterOfAddedAndModifiedCounts() {
        // Vor 30 Minuten hinzugefügt, vor 1 Minute bearbeitet -> noch jung, reift 9 Minuten später
        val y = FollowUpPlan.youngOf(listOf(now - 30 * 60 to now - 60), now, min)
        assertEquals((now - 60 + min) * 1000, y.newestAt)
        assertEquals(FollowUpPlan.readyAtMs(now - 30 * 60, now - 60, min), y.newestAt)
    }

    @Test fun minimumAgeZeroMeansNothingIsYoung() {
        val y = FollowUpPlan.youngOf(listOf(now to now, now - 5 to now - 5), now, 0)
        assertFalse(y.any)
        assertEquals(0L, FollowUpPlan.dueAt(y, 0))
    }

    @Test fun filesCreatedDuringTheRunPushTheDueTimeToNowPlusMinimumAge() {
        val y = FollowUpPlan.youngOf(listOf(now - 8 * 60 to now - 8 * 60), now, min)
        val created = (now + min) * 1000 // während des Laufs entstanden: jetzt + Mindestalter
        assertEquals(created, FollowUpPlan.dueAt(y, created))
        // Ohne wartende junge Dateien zählt allein das während des Laufs Entstandene
        assertEquals(created, FollowUpPlan.dueAt(Young(), created))
    }

    @Test fun dueTimeIsNeverBeforeTheOldestWaitingFile() {
        val y = Young(oldestAt = 5_000, newestAt = 9_000)
        assertTrue(FollowUpPlan.dueAt(y, 1_000) >= y.oldestAt)
        assertEquals(9_000, FollowUpPlan.dueAt(y, 1_000))
    }

    @Test fun combineTakesEarliestOldestAndLatestNewest() {
        val a = Young(2_000, 4_000)
        val b = Young(1_000, 3_000)
        assertEquals(Young(1_000, 4_000), FollowUpPlan.combine(a, b))
        assertEquals(a, FollowUpPlan.combine(a, Young()))
        assertEquals(b, FollowUpPlan.combine(Young(), b))
        assertFalse(FollowUpPlan.combine(Young(), Young()).any)
    }

    @Test fun replacingAFollowUpKeepsAllProfileIds() {
        val old = FollowUpPlan.idsFromTags(setOf(FollowUpPlan.tagOf(3), FollowUpPlan.tagOf(5), "fremd", FollowUpPlan.TAG_PREFIX + "x"))
        assertEquals(setOf(3L, 5L), old)
        assertEquals(setOf(3L, 5L, 8L), FollowUpPlan.merge(old, setOf(5L, 8L)))
        assertEquals(setOf(3L), FollowUpPlan.merge(emptySet(), setOf(3L)))
    }

    @Test fun followUpWithoutReadyPhotosDoesNotRun() {
        assertFalse(FollowUpPlan.shouldRun(emptyList()))
        assertFalse(FollowUpPlan.shouldRun(listOf(0, 0)))
        assertTrue(FollowUpPlan.shouldRun(listOf(0, 2)))
    }
}