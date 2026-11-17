package com.sync2pc.app

import com.sync2pc.app.mirror.RenameMatcher
import com.sync2pc.app.mirror.RenameMatcher.New
import com.sync2pc.app.mirror.RenameMatcher.Old
import com.sync2pc.app.mirror.RenameMatcher.Stamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** SYNC-20261114-340: Paarbildung für Umbenennen/Verschieben (Einzeldateien und Ordner) */
class RenameMatcherTest {
    private fun old(rel: String, size: Long = 100, mod: Long = 1000) = Old(rel.lowercase(), rel, Stamp(size, mod))
    private fun new(rel: String, size: Long = 100, mod: Long = 1000) = New(rel.lowercase(), rel, Stamp(size, mod))

    @Test fun uniqueSizeAndTimeFormAPair() {
        val p = RenameMatcher.match(listOf(old("a.txt")), listOf(new("b.txt")))
        assertEquals(1, p.size)
        assertEquals("a.txt", p[0].old.rel); assertEquals("b.txt", p[0].new.rel)
    }

    @Test fun differentSizeOrTimeIsNoPair() {
        assertTrue(RenameMatcher.match(listOf(old("a")), listOf(new("b", size = 101))).isEmpty())
        assertTrue(RenameMatcher.match(listOf(old("a")), listOf(new("b", mod = 1001))).isEmpty())
    }

    @Test fun ambiguousCandidatesFormNoPair() {
        // zwei neue Dateien mit gleichem Stempel (Kopie) oder zwei alte
        assertTrue(RenameMatcher.match(listOf(old("a")), listOf(new("b"), new("c"))).isEmpty())
        assertTrue(RenameMatcher.match(listOf(old("a"), old("x")), listOf(new("b"))).isEmpty())
    }

    @Test fun zeroTimeOrEmptyNeverPairs() {
        assertTrue(RenameMatcher.match(listOf(old("a", mod = 0)), listOf(new("b", mod = 0))).isEmpty())
        assertTrue(RenameMatcher.match(listOf(old("a", size = 0)), listOf(new("b", size = 0))).isEmpty())
    }

    @Test fun movingIntoAnotherFolderIsAPair() {
        val p = RenameMatcher.match(listOf(old("Alt/a.txt")), listOf(new("Neu/Unter/a.txt")))
        assertEquals(1, p.size)
    }

    // ---- Ordner ----
    private fun pairs(vararg r: kotlin.Pair<String, String>) =
        RenameMatcher.match(r.mapIndexed { i, (o, _) -> old(o, mod = 1000L + i) }, r.mapIndexed { i, (_, n) -> new(n, mod = 1000L + i) })

    @Test fun completeFolderBecomesOneDirRename() {
        val ps = pairs("A/x.txt" to "B/x.txt", "A/sub/y.txt" to "B/sub/y.txt")
        val r = RenameMatcher.group(ps, stateKeys = listOf("a/x.txt", "a/sub/y.txt"), otherKeys = listOf("a/x.txt", "a/sub/y.txt"),
            otherDirExists = { false })
        assertEquals(1, r.dirs.size)
        assertEquals("A", r.dirs[0].from); assertEquals("B", r.dirs[0].to)
        assertEquals(2, r.dirs[0].pairs.size)
        assertTrue(r.files.isEmpty())
    }

    @Test fun partialFolderKeepsSingleFilePairs() {
        val ps = pairs("A/x.txt" to "B/x.txt")
        // im Stand liegt unter A noch eine zweite Datei, die nicht als Paar erkannt ist
        val r = RenameMatcher.group(ps, listOf("a/x.txt", "a/z.txt"), listOf("a/x.txt", "a/z.txt"), { false })
        assertTrue(r.dirs.isEmpty())
        assertEquals(1, r.files.size)
    }

    @Test fun targetFolderAlreadyOnOtherSideFallsBackToFiles() {
        val ps = pairs("A/x.txt" to "B/x.txt")
        val r = RenameMatcher.group(ps, listOf("a/x.txt"), listOf("a/x.txt"), otherDirExists = { it == "B" })
        assertTrue(r.dirs.isEmpty()); assertEquals(1, r.files.size)
    }

    @Test fun extraFileOnOtherSideUnderTheOldFolderPreventsDirRename() {
        val ps = pairs("A/x.txt" to "B/x.txt")
        val r = RenameMatcher.group(ps, listOf("a/x.txt"), listOf("a/x.txt", "a/neu-auf-pc.txt"), { false })
        assertTrue(r.dirs.isEmpty()); assertEquals(1, r.files.size)
    }

    @Test fun differentRestPathPreventsDirRename() {
        val ps = pairs("A/x.txt" to "B/umbenannt.txt")
        val r = RenameMatcher.group(ps, listOf("a/x.txt"), listOf("a/x.txt"), { false })
        assertTrue(r.dirs.isEmpty()); assertEquals(1, r.files.size)
    }

    @Test fun shortestFolderWinsForNestedMove() {
        val ps = pairs("A/B/x.txt" to "C/B/x.txt", "A/y.txt" to "C/y.txt")
        val r = RenameMatcher.group(ps, listOf("a/b/x.txt", "a/y.txt"), listOf("a/b/x.txt", "a/y.txt"), { false })
        assertEquals(1, r.dirs.size)
        assertEquals("A", r.dirs[0].from); assertEquals("C", r.dirs[0].to)
        assertTrue(r.files.isEmpty())
    }

    @Test fun folderCannotMoveIntoItself() {
        val ps = pairs("A/x.txt" to "A/B/x.txt")
        val r = RenameMatcher.group(ps, listOf("a/x.txt"), listOf("a/x.txt"), { false })
        assertTrue(r.dirs.isEmpty()); assertEquals(1, r.files.size)
    }
}
