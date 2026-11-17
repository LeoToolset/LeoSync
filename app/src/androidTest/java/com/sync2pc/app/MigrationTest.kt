package com.sync2pc.app

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sync2pc.app.data.AppDatabase
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Datenbank-Migrationen (0.9.4). Läuft auf einem Gerät bzw. Emulator:
 * `gradlew connectedDebugAndroidTest` (der Builder führt ihn nicht aus).
 * Prüft, dass jede Migration zum exportierten Schema passt und bestehende Daten richtig übernommen werden.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val db = "migration-test"
    private val all = arrayOf(
        AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4,
        AppDatabase.MIGRATION_4_5, AppDatabase.MIGRATION_5_6, AppDatabase.MIGRATION_6_7,
        AppDatabase.MIGRATION_7_8, AppDatabase.MIGRATION_8_9,
        AppDatabase.MIGRATION_9_10, AppDatabase.MIGRATION_10_11, AppDatabase.MIGRATION_11_12,
    )

    @get:Rule
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), AppDatabase::class.java)

    /**
     * SYNC-20261114-332 Punkt 7: Kette 1 -> 12 mit Zeilen in allen Daten-Tabellen, je in der Version befüllt, in der die Tabelle
     * entsteht (synced_files und folder_mappings in 1, mirror_state und mirror_conflicts in 2, pending_restores in 5). Nach jedem
     * Schritt wird gegen das exportierte Schema geprüft, am Ende ist alles unverändert lesbar.
     */
    @Test fun dataTablesSurviveTheWholeChain() {
        helper.createDatabase(db, 1).apply {
            execSQL(
                "INSERT INTO synced_files (profileId, sourceKey, mediaUri, size, dateModified, partialHash, remotePath, syncedAt, verified, deleteState) " +
                    "VALUES (1, 'DCIM/Camera/a.jpg', 'content://media/1', 123, 456, 'abc', 'Fotos\\2026\\10\\a.jpg', 789, 1, 0)"
            )
            execSQL("INSERT INTO folder_mappings (profileId, groupKey, folderName, seen) VALUES (1, '2026/10/EG', '10 - Ägypten', 1)")
            close()
        }
        helper.runMigrationsAndValidate(db, 2, true, AppDatabase.MIGRATION_1_2).apply {
            execSQL("INSERT INTO mirror_state (profileId, relPath, localSize, localModified, remoteSize, remoteModified, syncedAt) VALUES (1, 'doc.pdf', 1, 2, 3, 4, 5)")
            execSQL("INSERT INTO mirror_conflicts (profileId, relPath, localSize, localModified, remoteSize, remoteModified, detectedAt, resolution) VALUES (1, 'x.txt', 6, 7, 8, 9, 10, NULL)")
            close()
        }
        for ((to, mig) in listOf(3 to AppDatabase.MIGRATION_2_3, 4 to AppDatabase.MIGRATION_3_4, 5 to AppDatabase.MIGRATION_4_5)) {
            helper.runMigrationsAndValidate(db, to, true, mig).close()
        }
        helper.runMigrationsAndValidate(db, 5, true).apply {
            execSQL("INSERT INTO pending_restores (profileId, side, trashPath, origPath, requestedAt) VALUES (1, 'LOCAL', '@LeoSync-Bin/2026-11-01/a.txt', 'a.txt', 99)")
            close()
        }
        val d = helper.runMigrationsAndValidate(db, 12, true, *all)
        d.query("SELECT sourceKey, mediaUri, size, dateModified, partialHash, remotePath, syncedAt, verified FROM synced_files").use { c ->
            assertEquals(1, c.count); c.moveToFirst()
            assertEquals("DCIM/Camera/a.jpg", c.getString(0)); assertEquals("content://media/1", c.getString(1))
            assertEquals(123L, c.getLong(2)); assertEquals(456L, c.getLong(3)); assertEquals("abc", c.getString(4))
            assertEquals("Fotos\\2026\\10\\a.jpg", c.getString(5)); assertEquals(789L, c.getLong(6)); assertEquals(1, c.getInt(7))
        }
        d.query("SELECT groupKey, folderName, seen FROM folder_mappings").use { c ->
            assertEquals(1, c.count); c.moveToFirst()
            assertEquals("2026/10/EG", c.getString(0)); assertEquals("10 - Ägypten", c.getString(1)); assertEquals(1, c.getInt(2))
        }
        d.query("SELECT relPath, localSize, localModified, remoteSize, remoteModified, syncedAt FROM mirror_state").use { c ->
            assertEquals(1, c.count); c.moveToFirst()
            assertEquals("doc.pdf", c.getString(0))
            assertEquals(listOf(1L, 2L, 3L, 4L, 5L), (1..5).map { c.getLong(it) })
        }
        d.query("SELECT relPath, localSize, remoteModified, detectedAt, resolution FROM mirror_conflicts").use { c ->
            assertEquals(1, c.count); c.moveToFirst()
            assertEquals("x.txt", c.getString(0)); assertEquals(6L, c.getLong(1)); assertEquals(9L, c.getLong(2))
            assertEquals(10L, c.getLong(3)); assertEquals(true, c.isNull(4))
        }
        d.query("SELECT side, trashPath, origPath, requestedAt FROM pending_restores").use { c ->
            assertEquals(1, c.count); c.moveToFirst()
            assertEquals("LOCAL", c.getString(0)); assertEquals("@LeoSync-Bin/2026-11-01/a.txt", c.getString(1))
            assertEquals("a.txt", c.getString(2)); assertEquals(99L, c.getLong(3))
        }
        d.close()
    }

    /** Profil aus Version 1 (0.0.x) übersteht alle Migrationen bis zur aktuellen Version */
    @Test fun fromVersion1ToLatest() {
        helper.createDatabase(db, 1).apply {
            execSQL(
                "INSERT INTO profiles (id, name, enabled, smbPath, smbUser, smbDomain, smbPasswordEnc, sourceFolders, " +
                    "wifiSsids, includeImages, includeVideos, includeSubfolders, deleteMode, verifyUpload) " +
                    "VALUES (1, ' Fotos ', 1, '\\\\pc\\Fotos', 'leo', '', '', 'DCIM/Camera/', 'heim', 1, 1, 1, 'KEEP', 1)"
            )
            close()
        }
        val d = helper.runMigrationsAndValidate(db, 12, true, *all)
        d.query("SELECT name, profileType, trashRetentionDays, massDeletePercent, keepVersions, verifyLevel, folderLayout, smbEncryption FROM profiles").use { c ->
            c.moveToFirst()
            assertEquals("Fotos", c.getString(0))
            assertEquals("PHOTOS", c.getString(1))
            assertEquals(30, c.getInt(2))
            assertEquals(20, c.getInt(3))
            assertEquals(1, c.getInt(4))
            assertEquals(1, c.getInt(5)) // Prüfung an -> "Wenig" wie bisher
            assertEquals("MONTH_SUFFIX", c.getString(6)) // Ordnerstruktur V1 wie bisher
            assertEquals("PREFERRED", c.getString(7)) // SMB3-Verschlüsselung Standard
        }
        d.close()
    }

    /** 9 -> 10 (SYNC-20261111-144): SMB3-Verschlüsselung; bestehende Profile behalten alle Daten und bekommen PREFERRED, Sync-Stand bleibt */
    @Test fun smbEncryptionFromVersion9() {
        helper.createDatabase(db, 9).apply {
            execSQL(
                "INSERT INTO profiles (id, name, enabled, smbPath, smbUser, smbDomain, smbPasswordEnc, sourceFolders, " +
                    "wifiSsids, includeImages, includeVideos, includeSubfolders, deleteMode, verifyUpload, profileType, " +
                    "direction, localTreeUri, remoteSubfolder, propagateDeletes, conflictMode, includePatterns, " +
                    "excludePatterns, remotePollMinutes, trashRetentionDays, massDeletePercent, keepVersions, verifyLevel, " +
                    "pcCheck, pcGuid, pcGuidHost, folderLayout) VALUES " +
                    "(1, 'Fotos', 1, '\\\\pc\\Fotos', 'leo', '', 'enc', 'DCIM/Camera/', 'heim', 1, 1, 1, 'KEEP', 1, 'PHOTOS', " +
                    "'UPLOAD', '', '', 0, 'ASK', '', '', 60, 30, 20, 1, 2, 'WIFI', '', '', 'COUNTRY_SUBFOLDER')"
            )
            execSQL("INSERT INTO folder_mappings (profileId, groupKey, folderName, seen) VALUES (1, '2026/10/EG', '10 - Ägypten', 1)")
            close()
        }
        val d = helper.runMigrationsAndValidate(db, 10, true, AppDatabase.MIGRATION_9_10)
        d.query("SELECT name, smbPath, smbPasswordEnc, wifiSsids, folderLayout, smbEncryption FROM profiles WHERE id = 1").use { c ->
            c.moveToFirst()
            assertEquals("Fotos", c.getString(0))
            assertEquals("\\\\pc\\Fotos", c.getString(1))
            assertEquals("enc", c.getString(2))
            assertEquals("heim", c.getString(3))
            assertEquals("COUNTRY_SUBFOLDER", c.getString(4))
            assertEquals("PREFERRED", c.getString(5))
        }
        d.query("SELECT folderName FROM folder_mappings WHERE profileId = 1").use { c ->
            c.moveToFirst()
            assertEquals("10 - Ägypten", c.getString(0))
        }
        d.close()
    }

    /** 11 -> 12 (SYNC-20261115-367): Auflösung und Dauer der PC-Seite im Konflikt; bestehende Konflikte bleiben, die neuen Spalten sind NULL */
    @Test fun conflictResolutionFromVersion11() {
        helper.createDatabase(db, 11).apply {
            execSQL("INSERT INTO mirror_conflicts (profileId, relPath, localSize, localModified, remoteSize, remoteModified, detectedAt, resolution) VALUES (1, 'a.jpg', 6, 7, 8, 9, 10, NULL)")
            close()
        }
        val d = helper.runMigrationsAndValidate(db, 12, true, AppDatabase.MIGRATION_11_12)
        d.query("SELECT localSize, remoteSize, detectedAt, resolution, remoteWidth, remoteHeight, remoteDurationMs FROM mirror_conflicts WHERE relPath = 'a.jpg'").use { c ->
            assertEquals(true, c.moveToFirst())
            assertEquals(6L, c.getLong(0)); assertEquals(8L, c.getLong(1)); assertEquals(10L, c.getLong(2))
            assertEquals(true, c.isNull(3)); assertEquals(true, c.isNull(4)); assertEquals(true, c.isNull(5)); assertEquals(true, c.isNull(6))
        }
        d.execSQL("UPDATE mirror_conflicts SET remoteWidth = 4000, remoteHeight = 3000, remoteDurationMs = 42000 WHERE relPath = 'a.jpg'")
        d.close()
    }
    /** 10 -> 11 (SYNC-20261111-173): Zähler «verschoben»; bestehende Läufe behalten alle Werte und bekommen deferred = 0 */
    @Test fun deferredFromVersion10() {
        helper.createDatabase(db, 10).apply {
            execSQL(
                "INSERT INTO sync_runs (id, profileId, profileName, `trigger`, dryRun, startedAt, finishedAt, status, " +
                    "uploaded, skipped, failed, waiting, bytes, message, messageCode, triggerCode) VALUES " +
                    "(1, 1, 'Fotos', 'WLAN verbunden', 0, 1000, 2000, 'OK', 3, 2, 1, 0, 4096, '3 übertragen', '{\"k\":\"run_nothing\"}', NULL)"
            )
            execSQL("INSERT INTO log_entries (id, runId, time, level, message) VALUES (1, 1, 1500, 'I', 'a.jpg')")
            close()
        }
        val d = helper.runMigrationsAndValidate(db, 11, true, AppDatabase.MIGRATION_10_11)
        d.query("SELECT uploaded, skipped, failed, waiting, bytes, message, messageCode, deferred FROM sync_runs WHERE id = 1").use { c ->
            assertEquals(true, c.moveToFirst())
            assertEquals(3, c.getInt(0))
            assertEquals(2, c.getInt(1))
            assertEquals(1, c.getInt(2))
            assertEquals(0, c.getInt(3))
            assertEquals(4096L, c.getLong(4))
            assertEquals("3 übertragen", c.getString(5))
            assertEquals("{\"k\":\"run_nothing\"}", c.getString(6))
            assertEquals(0, c.getInt(7)) // neue Spalte: Standard 0
        }
        d.query("SELECT message FROM log_entries WHERE runId = 1").use { c ->
            assertEquals(true, c.moveToFirst())
            assertEquals("a.jpg", c.getString(0))
        }
        // Neue Läufe lassen sich mit deferred schreiben
        d.execSQL("UPDATE sync_runs SET deferred = 2 WHERE id = 1")
        d.close()
    }

    /** 5 -> 6: Prüfstufe aus verifyUpload und deleteMode */
    @Test fun verifyLevelFromVersion5() {
        helper.createDatabase(db, 5).apply {
            fun insert(id: Int, deleteMode: String, verify: Int) = execSQL(
                "INSERT INTO profiles (id, name, enabled, smbPath, smbUser, smbDomain, smbPasswordEnc, sourceFolders, " +
                    "wifiSsids, includeImages, includeVideos, includeSubfolders, deleteMode, verifyUpload, profileType, " +
                    "direction, localTreeUri, remoteSubfolder, propagateDeletes, conflictMode, includePatterns, " +
                    "excludePatterns, remotePollMinutes, trashRetentionDays, massDeletePercent, keepVersions) VALUES " +
                    "($id, 'P$id', 1, '', '', '', '', '', '', 1, 1, 1, '$deleteMode', $verify, 'PHOTOS', 'UPLOAD', '', '', " +
                    "0, 'ASK', '', '', 60, 30, 20, 1)"
            )
            insert(1, "KEEP", 0)   // aus und nichts entfernt -> Aus
            insert(2, "TRASH", 0)  // aus, aber Fotos werden entfernt -> Wenig (Prüfung war Pflicht)
            insert(3, "KEEP", 1)   // an -> Wenig
            close()
        }
        val d = helper.runMigrationsAndValidate(db, 6, true, AppDatabase.MIGRATION_5_6)
        d.query("SELECT id, verifyLevel FROM profiles ORDER BY id").use { c ->
            val levels = buildMap { while (c.moveToNext()) put(c.getInt(0), c.getInt(1)) }
            assertEquals(mapOf(1 to 0, 2 to 1, 3 to 1), levels)
        }
        d.close()
    }

    /** 6 -> 7 (0.12.0, DEC-159): Kennungs-Spalten; bestehende Läufe und Log-Zeilen behalten ihren Text, Kennung NULL */
    @Test fun runCodesFromVersion6() {
        helper.createDatabase(db, 6).apply {
            execSQL(
                "INSERT INTO sync_runs (id, profileId, profileName, `trigger`, dryRun, startedAt, finishedAt, status, " +
                    "uploaded, skipped, failed, waiting, bytes, message) VALUES " +
                    "(1, 1, 'Fotos', 'WLAN verbunden', 0, 1000, 2000, 'OK', 3, 0, 0, 0, 0, '3 übertragen')"
            )
            execSQL("INSERT INTO log_entries (id, runId, time, level, message) VALUES (1, 1, 1500, 'I', '↑ a.jpg → 2026\\06')")
            close()
        }
        val d = helper.runMigrationsAndValidate(db, 7, true, AppDatabase.MIGRATION_6_7)
        d.query("SELECT `trigger`, message, messageCode, triggerCode FROM sync_runs WHERE id = 1").use { c ->
            c.moveToFirst()
            assertEquals("WLAN verbunden", c.getString(0))
            assertEquals("3 übertragen", c.getString(1))
            assertEquals(true, c.isNull(2))
            assertEquals(true, c.isNull(3))
        }
        d.query("SELECT message, code FROM log_entries WHERE id = 1").use { c ->
            c.moveToFirst()
            assertEquals("↑ a.jpg → 2026\\06", c.getString(0))
            assertEquals(true, c.isNull(1))
        }
        // Neue Zeilen mit Kennung lassen sich schreiben
        d.execSQL("UPDATE sync_runs SET messageCode = '{\"k\":\"run_nothing\"}' WHERE id = 1")
        d.close()
    }

    /** 7 -> 8 (SYNC-20261109-047): PC-Erkennung; bestehende Profile behalten ihre Daten, Standard WLAN, keine Kennung */
    @Test fun pcCheckFromVersion7() {
        helper.createDatabase(db, 7).apply {
            execSQL(
                "INSERT INTO profiles (id, name, enabled, smbPath, smbUser, smbDomain, smbPasswordEnc, sourceFolders, " +
                    "wifiSsids, includeImages, includeVideos, includeSubfolders, deleteMode, verifyUpload, profileType, " +
                    "direction, localTreeUri, remoteSubfolder, propagateDeletes, conflictMode, includePatterns, " +
                    "excludePatterns, remotePollMinutes, trashRetentionDays, massDeletePercent, keepVersions, verifyLevel) VALUES " +
                    "(1, 'Fotos', 1, '\\\\pc\\Fotos', 'leo', '', 'enc', 'DCIM/Camera/', 'heim', 1, 1, 1, 'KEEP', 1, 'PHOTOS', " +
                    "'UPLOAD', '', '', 0, 'ASK', '', '', 60, 30, 20, 1, 2)"
            )
            close()
        }
        val d = helper.runMigrationsAndValidate(db, 8, true, AppDatabase.MIGRATION_7_8)
        d.query("SELECT name, smbPath, smbPasswordEnc, wifiSsids, pcCheck, pcGuid, pcGuidHost FROM profiles WHERE id = 1").use { c ->
            c.moveToFirst()
            assertEquals("Fotos", c.getString(0))
            assertEquals("\\\\pc\\Fotos", c.getString(1))
            assertEquals("enc", c.getString(2))
            assertEquals("heim", c.getString(3))
            assertEquals("WIFI", c.getString(4))
            assertEquals("", c.getString(5))
            assertEquals("", c.getString(6))
        }
        d.close()
    }

    /** 8 -> 9 (SYNC-20261109-083): Ordnerstruktur; bestehende Profile behalten alle Daten und bekommen V1, Sync-Stand bleibt */
    @Test fun folderLayoutFromVersion8() {
        helper.createDatabase(db, 8).apply {
            execSQL(
                "INSERT INTO profiles (id, name, enabled, smbPath, smbUser, smbDomain, smbPasswordEnc, sourceFolders, " +
                    "wifiSsids, includeImages, includeVideos, includeSubfolders, deleteMode, verifyUpload, profileType, " +
                    "direction, localTreeUri, remoteSubfolder, propagateDeletes, conflictMode, includePatterns, " +
                    "excludePatterns, remotePollMinutes, trashRetentionDays, massDeletePercent, keepVersions, verifyLevel, " +
                    "pcCheck, pcGuid, pcGuidHost) VALUES " +
                    "(1, 'Fotos', 1, '\\\\pc\\Fotos', 'leo', '', 'enc', 'DCIM/Camera/', 'heim', 1, 1, 1, 'KEEP', 1, 'PHOTOS', " +
                    "'UPLOAD', '', '', 0, 'ASK', '', '', 60, 30, 20, 1, 2, 'WIFI', '', '')"
            )
            execSQL("INSERT INTO folder_mappings (profileId, groupKey, folderName, seen) VALUES (1, '2026/10/EG', '10 - Ägypten', 1)")
            close()
        }
        val d = helper.runMigrationsAndValidate(db, 9, true, AppDatabase.MIGRATION_8_9)
        d.query("SELECT name, smbPath, smbPasswordEnc, wifiSsids, pcCheck, folderLayout FROM profiles WHERE id = 1").use { c ->
            c.moveToFirst()
            assertEquals("Fotos", c.getString(0))
            assertEquals("\\\\pc\\Fotos", c.getString(1))
            assertEquals("enc", c.getString(2))
            assertEquals("heim", c.getString(3))
            assertEquals("WIFI", c.getString(4))
            assertEquals("MONTH_SUFFIX", c.getString(5))
        }
        d.query("SELECT folderName FROM folder_mappings WHERE profileId = 1").use { c ->
            c.moveToFirst()
            assertEquals("10 - Ägypten", c.getString(0))
        }
        d.close()
    }
}
