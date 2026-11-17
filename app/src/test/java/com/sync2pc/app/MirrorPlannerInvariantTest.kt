package com.sync2pc.app

import com.sync2pc.app.data.MirrorState
import com.sync2pc.app.data.SyncDirection
import com.sync2pc.app.mirror.LocalEntry
import com.sync2pc.app.mirror.MirrorPlanner
import com.sync2pc.app.mirror.PlanKind
import com.sync2pc.app.smb.RemoteEntry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * SYNC-20261114-332 Punkt 3: Invarianten des 3-Wege-Abgleichs über ALLE Kombinationen
 * (Handy / PC je fehlt, unverändert, geändert; Stand vorhanden oder nicht) × Richtung × propagate × conflictNewer ×
 * keepVersions × Inhaltsgleichheit. Die Einzelfälle stehen in [MirrorPlannerTest] und werden hier nicht wiederholt.
 */
class MirrorPlannerInvariantTest {
    private enum class Side { MISSING, UNCHANGED, CHANGED }

    private val base = MirrorState(1, "a.txt", 10, 100, 10, 100, 0)

    private fun local(s: Side): LocalEntry? = when (s) {
        Side.MISSING -> null
        Side.UNCHANGED -> LocalEntry("id", "a.txt", 10, 100, false)
        Side.CHANGED -> LocalEntry("id", "a.txt", 20, 300, false)
    }

    private fun remote(s: Side): RemoteEntry? = when (s) {
        Side.MISSING -> null
        Side.UNCHANGED -> RemoteEntry("a.txt", 10, false, 100)
        Side.CHANGED -> RemoteEntry("a.txt", 30, false, 200)
    }

    private class Case(
        val l: Side, val r: Side, val hasState: Boolean, val dir: SyncDirection,
        val propagate: Boolean, val newer: Boolean, val keep: Boolean, val same: Boolean,
    ) {
        override fun toString() = "L=$l R=$r state=$hasState dir=$dir propagate=$propagate newer=$newer keep=$keep same=$same"
    }

    private fun allCases(): List<Case> {
        val out = ArrayList<Case>()
        for (l in Side.entries) for (r in Side.entries) for (st in listOf(false, true))
            for (dir in SyncDirection.entries) for (p in listOf(false, true)) for (n in listOf(false, true))
                for (k in listOf(false, true)) for (same in listOf(false, true)) out += Case(l, r, st, dir, p, n, k, same)
        return out
    }

    private fun decide(c: Case): MirrorPlanner.Decision? = runBlocking {
        MirrorPlanner.decide(local(c.l), remote(c.r), if (c.hasState) base else null, c.dir, c.propagate, c.newer, c.keep) { c.same }
    }

    @Test fun everyCombinationYieldsADecisionWithoutException() {
        var n = 0
        for (c in allCases()) { decide(c); n++ }
        assertEquals(3 * 3 * 2 * 3 * 2 * 2 * 2 * 2, n)
    }

    @Test fun deletingNeedsStatePropagationAndTheRightDirection() {
        for (c in allCases()) {
            val k = decide(c)?.kind
            when (k) {
                PlanKind.TRASH_LOCAL -> {
                    assertTrue("$c", c.hasState && c.propagate && c.l != Side.MISSING && c.r == Side.MISSING && c.dir.down)
                }
                PlanKind.TRASH_REMOTE -> {
                    assertTrue("$c", c.hasState && c.propagate && c.r != Side.MISSING && c.l == Side.MISSING && c.dir.up)
                }
                else -> {}
            }
        }
    }

    @Test fun withoutStateNothingIsDeletedOrDropped() {
        for (c in allCases().filter { !it.hasState }) {
            val k = decide(c)?.kind
            assertFalse("$c -> $k", k == PlanKind.TRASH_LOCAL || k == PlanKind.TRASH_REMOTE || k == PlanKind.DROP)
        }
    }

    @Test fun transfersFollowTheDirection() {
        for (c in allCases()) {
            val k = decide(c)?.kind
            if (k == PlanKind.UPLOAD) assertTrue("$c", c.dir.up && c.l != Side.MISSING)
            if (k == PlanKind.DOWNLOAD) assertTrue("$c", c.dir.down && c.r != Side.MISSING)
        }
    }

    @Test fun conflictOnlyWhenBothExistAskingAndBothDirections() {
        for (c in allCases()) {
            val k = decide(c)?.kind
            if (k == PlanKind.CONFLICT) assertTrue("$c", c.dir == SyncDirection.BOTH && !c.newer && c.l != Side.MISSING && c.r != Side.MISSING)
            if (k == PlanKind.KEEP_BOTH) assertTrue("$c: KEEP_BOTH kommt nicht aus dem Planner", false)
        }
    }

    @Test fun recordAndTrashOtherOnlyWhereTheyMakeSense() {
        for (c in allCases()) {
            val d = decide(c) ?: continue
            if (d.kind == PlanKind.RECORD) assertTrue("$c", c.l != Side.MISSING && c.r != Side.MISSING)
            if (d.kind == PlanKind.DROP) assertTrue("$c", c.hasState)
            if (d.trashOther) {
                assertTrue("$c", d.kind == PlanKind.UPLOAD || d.kind == PlanKind.DOWNLOAD)
                assertTrue("$c", c.l != Side.MISSING && c.r != Side.MISSING) // überschrieben wird nur, was es gibt
            }
        }
    }

    @Test fun versionProtectionOnlyOverwritesWithTrashWhenEnabledOrBothChanged() {
        // Eine Seite geändert, Stand vorhanden: Papierkorb der überschriebenen Version genau dann, wenn keepVersions
        for (c in allCases().filter { it.hasState && it.keep.not() }) {
            val oneSided = (c.l == Side.CHANGED) != (c.r == Side.CHANGED) && c.l != Side.MISSING && c.r != Side.MISSING
            if (!oneSided) continue
            val d = decide(c) ?: continue
            assertFalse("$c", d.trashOther)
        }
        for (c in allCases().filter { it.hasState && it.keep }) {
            val oneSided = (c.l == Side.CHANGED) != (c.r == Side.CHANGED) && c.l != Side.MISSING && c.r != Side.MISSING
            if (!oneSided) continue
            val d = decide(c) ?: continue
            if (d.kind == PlanKind.UPLOAD || d.kind == PlanKind.DOWNLOAD) assertTrue("$c", d.trashOther)
        }
    }

    @Test fun unchangedBothSidesNeverDoAnything() {
        for (c in allCases().filter { it.hasState && it.l == Side.UNCHANGED && it.r == Side.UNCHANGED }) {
            assertEquals("$c", null, decide(c))
        }
    }

    @Test fun anExistingDecisionAlwaysHasAKind() {
        for (c in allCases()) decide(c)?.let { assertNotNull("$c", it.kind) }
    }

    /**
     * R-D1 (SYNC-20261114-337 «Änderung schlägt Löschung»): Wurde eine Datei auf einer Seite gelöscht und auf der anderen geändert,
     * wird in der Richtung «beide» nie in den Papierkorb gelöscht, sondern übertragen. Einweg-Richtungen bleiben unverändert (dort
     * schützt der Papierkorb).
     */
    @Test fun deletionOnOneSideNeverTrashesAChangeOnTheOtherSideInBothMode() {
        for (c in allCases().filter { it.hasState && it.dir == SyncDirection.BOTH }) {
            val d = decide(c) ?: continue
            if (d.kind == PlanKind.TRASH_LOCAL) assertFalse("$c", c.l == Side.CHANGED)
            if (d.kind == PlanKind.TRASH_REMOTE) assertFalse("$c", c.r == Side.CHANGED)
            if (d.overruledDeletion) assertTrue("$c", c.propagate && ((c.l == Side.CHANGED && c.r == Side.MISSING) || (c.r == Side.CHANGED && c.l == Side.MISSING)))
        }
    }
}