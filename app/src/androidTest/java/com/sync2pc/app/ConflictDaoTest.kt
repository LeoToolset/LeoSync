package com.sync2pc.app

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sync2pc.app.data.AppDatabase
import com.sync2pc.app.data.MirrorConflict
import com.sync2pc.app.data.SyncedFile
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * SYNC-20261114-332 Punkt 1: Konflikt-Auflösung und Sync-Stand gegen eine echte In-Memory-Room-Datenbank (Vorbild PcGuidDaoTest).
 * DEC-232: `undoResolution` und `deleteConflictIf` liefern 0, wenn die Auflösung inzwischen geändert oder entfernt wurde
 * (dann darf die Aktion nicht laufen), und 1 bei passender Auflösung. Läuft auf Gerät/Emulator (`connectedDebugAndroidTest`).
 */
@RunWith(AndroidJUnit4::class)
class ConflictDaoTest {
    private lateinit var db: AppDatabase

    @Before fun open() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java).allowMainThreadQueries().build()
    }

    @After fun close() { db.close() }

    private fun conflict(profile: Long = 1, rel: String = "a.txt", resolution: String? = "KEEP_BOTH") =
        MirrorConflict(profile, rel, 1, 2, 3, 4, 5, resolution)

    private fun synced(profile: Long = 1, key: String = "DCIM/a.jpg") =
        SyncedFile(profile, key, "content://x/1", 123, 456, "abc", "Fotos\\a.jpg", 789, true)

    @Test fun undoResolutionResetsOnlyAMatchingResolution() = runBlocking {
        val dao = db.dao()
        dao.upsertConflict(conflict(resolution = "KEEP_BOTH"))
        assertEquals("andere Auflösung: nichts zurückgesetzt", 0, dao.undoResolution(1, "a.txt", "UPLOAD"))
        assertEquals("KEEP_BOTH", dao.conflicts(1).single().resolution)
        assertEquals(1, dao.undoResolution(1, "a.txt", "KEEP_BOTH"))
        assertNull(dao.conflicts(1).single().resolution)
        assertEquals("zweiter Versuch: schon offen", 0, dao.undoResolution(1, "a.txt", "KEEP_BOTH"))
    }

    @Test fun undoResolutionNeverRevivesARemovedRow() = runBlocking {
        val dao = db.dao()
        assertEquals(0, dao.undoResolution(1, "gone.txt", "KEEP_BOTH"))
        assertEquals(emptyList<MirrorConflict>(), dao.conflicts(1))
    }

    @Test fun deleteConflictIfDeletesOnlyWithTheExpectedResolution() = runBlocking {
        val dao = db.dao()
        dao.upsertConflict(conflict(resolution = "DOWNLOAD"))
        assertEquals("Benutzer hat inzwischen anders entschieden: Aktion darf nicht laufen", 0, dao.deleteConflictIf(1, "a.txt", "UPLOAD"))
        assertEquals(1, dao.conflicts(1).size)
        assertEquals(1, dao.deleteConflictIf(1, "a.txt", "DOWNLOAD"))
        assertEquals(0, dao.conflicts(1).size)
    }

    @Test fun deleteConflictIfIsZeroAfterTheUserUndid() = runBlocking {
        val dao = db.dao()
        dao.upsertConflict(conflict(resolution = "UPLOAD"))
        assertEquals(1, dao.undoResolution(1, "a.txt", "UPLOAD")) // Benutzer macht rückgängig
        assertEquals(0, dao.deleteConflictIf(1, "a.txt", "UPLOAD")) // Lauf findet «offen» vor: nicht ausführen
        assertEquals("Konflikt bleibt offen erhalten", 1, dao.conflicts(1).size)
    }

    @Test fun conflictOperationsTouchOnlyTheGivenProfileAndPath() = runBlocking {
        val dao = db.dao()
        dao.upsertConflict(conflict(profile = 1, rel = "a.txt"))
        dao.upsertConflict(conflict(profile = 2, rel = "a.txt"))
        dao.upsertConflict(conflict(profile = 1, rel = "b.txt"))
        assertEquals(1, dao.deleteConflictIf(1, "a.txt", "KEEP_BOTH"))
        assertEquals(1, dao.conflicts(2).size)
        assertEquals(listOf("b.txt"), dao.conflicts(1).map { it.relPath })
    }

    @Test fun deleteSyncedRemovesOnlyThatRow() = runBlocking {
        val dao = db.dao()
        dao.upsertSynced(synced(1, "DCIM/a.jpg"))
        dao.upsertSynced(synced(1, "DCIM/b.jpg"))
        dao.upsertSynced(synced(2, "DCIM/a.jpg"))
        dao.deleteSynced(1, "DCIM/a.jpg")
        assertNull(dao.synced(1, "DCIM/a.jpg"))
        assertNotNull(dao.synced(1, "DCIM/b.jpg"))
        assertNotNull("anderes Profil bleibt", dao.synced(2, "DCIM/a.jpg"))
        dao.deleteSynced(1, "DCIM/gibt-es-nicht.jpg") // kein Fehler
    }
}
