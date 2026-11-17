package com.sync2pc.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException

/**
 * SYNC-20261114-332 Punkt 5: Die Fehlerinjektion von [FakeShare] (abgelehnter Name, Änderung mitten im Lauf, Abbruch an
 * einer Operation) verhält sich wie vorgesehen und verändert bei einem Fehler nichts.
 */
class FakeShareInjectionTest {
    @Test fun rejectedNameFailsEnsureDirAndMoveWithoutChangingAnything() {
        val share = FakeShare("Fotos").add("a.txt", 10, "h")
        share.rejectedNames += "@LeoSync-Bin"
        try { share.ensureDir(share.path("@LeoSync-Bin")); fail() } catch (e: IOException) { assertTrue(e.message!!.contains("NAME_INVALID")) }
        assertFalse(share.dirExists(share.path("@LeoSync-Bin")))
        share.addDir("@leosync-bin") // Ordner liegt trotzdem schon da (z. B. aus früherem Lauf)
        try { share.move(share.path("a.txt"), share.path("@LeoSync-Bin", "a.txt")); fail() } catch (e: IOException) { /* ok */ }
        assertTrue("Datei bleibt am alten Ort", share.fileAt(share.path("a.txt")))
        assertEquals(emptyList<String>(), share.ops.filter { it.startsWith("move") })
    }

    @Test fun rejectionIsPerNameAndCaseInsensitive() {
        val share = FakeShare("Fotos")
        share.rejectedNames += "Bad"
        try { share.ensureDir(share.path("x", "BAD")); fail() } catch (e: IOException) { /* ok */ }
        share.ensureDir(share.path("x", "gut")) // andere Namen gehen weiter
        assertTrue(share.dirExists(share.path("x", "gut")))
    }

    @Test fun changeSimulatesAModificationDuringTheRun() {
        val share = FakeShare("Fotos").add("a.txt", 10, "h", modified = 100)
        val before = share.stat(share.path("a.txt"))!!
        share.change("a.txt", size = 20, modified = 200)
        val after = share.stat(share.path("a.txt"))!!
        assertEquals(10L, before.size); assertEquals(20L, after.size)
        assertEquals(100L, before.modified); assertEquals(200L, after.modified)
        assertEquals("h", share.partialHash(share.path("a.txt"), 20)) // Inhalts-Kennung bleibt, wenn nicht geändert
        share.change("a.txt", hash = "neu")
        assertEquals("neu", share.partialHash(share.path("a.txt"), 20))
    }

    @Test fun abortAtOperationLeavesTheShareUntouched() {
        val share = FakeShare("Fotos").add("a.txt", 10, "h").addDir("Bin")
        share.onOp = { op, _ -> if (op == "move") throw IOException("Netzabbruch") }
        try { share.move(share.path("a.txt"), share.path("Bin", "a.txt")); fail() } catch (e: IOException) { /* ok */ }
        assertTrue(share.fileAt(share.path("a.txt")))
        assertFalse(share.fileAt(share.path("Bin", "a.txt")))
        share.onOp = null
        share.move(share.path("a.txt"), share.path("Bin", "a.txt")) // Wiederholung gelingt
        assertTrue(share.fileAt(share.path("Bin", "a.txt")))
    }
}
