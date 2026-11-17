package com.sync2pc.app.mirror

import com.sync2pc.app.data.RunStatus

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import com.sync2pc.app.R
import com.sync2pc.app.media.AndroidMediaProbe
import com.sync2pc.app.media.BudgetedSource
import com.sync2pc.app.media.MediaDims
import com.sync2pc.app.media.MediaProbe
import com.sync2pc.app.data.AppDatabase
import com.sync2pc.app.l10n.Msg
import com.sync2pc.app.l10n.plural
import com.sync2pc.app.l10n.str
import com.sync2pc.app.data.ConflictMode
import com.sync2pc.app.data.ConflictResolution
import com.sync2pc.app.data.LogEntry
import com.sync2pc.app.data.MirrorConflict
import com.sync2pc.app.data.MirrorState
import com.sync2pc.app.data.Profile
import com.sync2pc.app.data.SyncDirection
import com.sync2pc.app.data.SyncRun
import com.sync2pc.app.smb.EncryptionAuto
import com.sync2pc.app.smb.RemoteEntry
import com.sync2pc.app.smb.SmbSession
import com.sync2pc.app.sync.PartialHash
import com.sync2pc.app.sync.PreviewEntry
import com.sync2pc.app.sync.SyncProgress
import com.sync2pc.app.sync.SyncRunner
import com.sync2pc.app.data.GlobalSettings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Art der Aktion je Datei; die Entscheidungstabelle steht in [MirrorPlanner] (SYNC-20261111-163) */
private typealias Kind = PlanKind

/**
 * Ordner-Spiegelung Handy-Ordner ⇄ PC-Ordner (1:1 mit Unterordnern).
 *
 * 3-Wege-Abgleich je Datei: Handy-Stand (L), PC-Stand (R) und Stand beim letzten Abgleich (S).
 *  - nur auf einer Seite geändert/neu   -> in Sync-Richtung kopieren
 *  - auf beiden Seiten geändert         -> gleich? merken : Konflikt (fragen bzw. neuere gewinnt)
 *  - auf einer Seite gelöscht           -> optional übernehmen (Papierkorb der Gegenseite), sonst wiederherstellen
 * Ersetzte oder gelöschte Dateien landen immer in "@LeoSync-Bin/<Datum>/…". Endgültig gelöscht
 * werden sie erst nach Ablauf der Papierkorb-Frist des Profils (trashRetentionDays, 0 = nie).
 */
class MirrorEngine(
    private val context: Context,
    private val profile: Profile,
    private val trigger: Msg,
    private val dryRun: Boolean = false,
    private val shouldContinue: () -> Boolean = { true },
    private val onProgress: (SyncProgress) -> Unit = {},
    /** Bei einem Stopp: Grund, wenn Android unterbrochen hat (z. B. "WLAN getrennt"); null = vom Benutzer abgebrochen */
    private val interruptReason: () -> Msg? = { null },
    /** Simulation (dryRun): höchstens so viele Einträge anzeigen */
    private val previewLimit: Int = Int.MAX_VALUE,
) : SyncRunner {

    companion object {
        /**
         * Papierkorb auf Handy und PC, für alle Ordnertypen (SYNC-20261111-244). Ohne Punkt am Anfang, weil MediaStore
         * Punkt-Ordner nicht listet (SYNC-20261111-229) und der Ordner so im Explorer sichtbar ist.
         */
        const val TRASH_DIR = "@LeoSync-Bin"
        /**
         * Papierkorb-Name von 0.8.0 bis 0.16.x: wird weiter erkannt (Liste, Wiederherstellen, Leeren) und von der
         * [TrashMigration] nach [TRASH_DIR] übernommen; auf dem PC auch der Ausweichname, wenn [TRASH_DIR] dort nicht
         * angelegt werden kann ([PcTrashMover])
         */
        const val DOT_TRASH_DIR = ".LeoSync-Bin"
        /** Name des Papierkorbs von 0.0.12 bis 0.7.x – wird weiter erkannt (Liste, Wiederherstellen, Leeren) */
        const val LEGACY_TRASH_DIR = ".LeoSync-Papierkorb"
        /** Papierkorb-Name bis 0.0.11 – wird weiterhin nie synchronisiert */
        const val OLD_TRASH_DIR = ".Sync2PC-Papierkorb"
        /** Alle Papierkorb-Namen: der aktuelle zuerst (dorthin wird neu verschoben) */
        val TRASH_DIRS = listOf(TRASH_DIR, DOT_TRASH_DIR, LEGACY_TRASH_DIR, OLD_TRASH_DIR)
        fun isTrashDir(name: String): Boolean = TRASH_DIRS.any { it.equals(name, ignoreCase = true) }
        /**
         * Liegt [path] (Trenner "/", z. B. MediaStore-RELATIVE_PATH "DCIM/@LeoSync-Bin/2026-11-03/") an beliebiger Stelle in
         * einem Papierkorb? Nur ganze Segmente zählen ("x@LeoSync-Bin" oder "@LeoSync-Bin-notes" sind keine Treffer).
         * Für Fotoprofil und «Speicher freigeben» (SYNC-20261112-249): sie lesen den MediaStore, nicht den Ordner.
         */
        fun isInTrash(path: String): Boolean = path.split('/').any { it.isNotEmpty() && isTrashDir(it) }
        /** Marker-Datei (0 Byte), die den Handy-Papierkorb vor der Galerie versteckt (SYNC-20261112-249); kein Papierkorb-Inhalt */
        const val NOMEDIA = ".nomedia"
        /** Zusatz beim Behalten beider Versionen (bis 0.7.x "_Konflikt-Handy") */
        const val CONFLICT_SUFFIX = ConflictName.SUFFIX
        /** Mindestens so viel Speicher frei lassen */
        private const val RESERVE_BYTES = 300L * 1024 * 1024
    }

    override val preview = ArrayList<PreviewEntry>()
    // @Volatile: werden seit 0.3.0 von parallelen Übertragungen gesetzt
    @Volatile override var connectionFailed = false
        private set
    @Volatile override var wifiLost = false
        private set
    override var newConflicts = 0
        private set
    @Volatile override var spaceShortage = false
        private set
    override var blocked = false
        private set
    @Volatile override var encryptionUnavailable = false
        private set

    private val dao = AppDatabase.get(context).dao()
    private val guards = GuardStore(context)
    private val identities = DownloadIdentities(context)
    private val dir: SyncDirection = profile.dir()
    /** Löschungen übernehmen? (einmal aus nach "Nicht löschen" bei der Schutzbremse) */
    private var propagate = profile.propagateDeletes
    private val today = SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY).format(Date())


    /** key = relativer Pfad in Kleinbuchstaben, rel = Originalschreibweise */
    private data class Action(val key: String, val rel: String, val kind: Kind, val trashOther: Boolean = false)

    /** Erledigter Konflikt; [expected] = die Entscheidung, die der Plan ausführt (null = Seite fehlt, unbedingt löschen) */
    private data class DoneConflict(val key: String, val relPath: String, val expected: String?)

    private lateinit var tree: LocalTree
    private var smb: SmbSession? = null
    private var local: Map<String, Pair<String, LocalEntry>> = emptyMap()
    private var remote: Map<String, Pair<String, RemoteEntry>> = emptyMap()
    /** Ein Inhaltsvergleich wurde abgebrochen (WLAN weg): Der Lauf endet ohne etwas auszuführen (R-I3) */
    private var hashAborted = false
    private var lastCompareEmit = 0L

    private var run = SyncRun(
        profileId = profile.id, profileName = profile.name, trigger = trigger.text(), triggerCode = trigger.toCode(),
        dryRun = dryRun, startedAt = System.currentTimeMillis()
    )

    /** Log-Zeile als Kennung (DEC-159): Text in der Schreibsprache als Rückfall, Kennung für die Anzeige */
    private suspend fun log(level: String, msg: Msg) {
        if (dryRun) return
        dao.insertLog(LogEntry(runId = run.id, time = System.currentTimeMillis(), level = level,
            message = msg.text(), code = msg.toCode()))
    }

    /** Wörtliche Zeile (Symbole, Dateinamen, Fehlertexte): bleibt wie sie ist, ohne Kennung */
    private suspend fun log(level: String, text: String) = log(level, Msg.lit(text))

    // Zähler (threadsicher, parallele Übertragungen)
    private val up = AtomicInteger(); private val down = AtomicInteger(); private val failed = AtomicInteger()
    private val bytes = AtomicLong(); private val trashed = AtomicInteger()
    /** Dateien, die während des Laufs geändert wurden und deshalb erst beim nächsten Sync folgen (SYNC-20261111-173) */
    private val deferred = AtomicInteger()
    /** Der Lauf hat selbst auf dem Handy geschrieben (Download, Papierkorb, Umbenennen, Aufräumen): kein Folgelauf wegen Ordneränderung (SYNC-20261111-213) */
    @Volatile private var localTouched = false
    override val trashedCount: Int get() = trashed.get()
    @Volatile override var changedDuringRun = false
        private set

    /** Zwischen den parallelen Übertragungen geteilter Zustand */
    private class Shared(val totalFiles: Int, val totalBytes: Long, budget: Long) {
        val done = AtomicInteger()
        /** Bytes vollständig übertragener Dateien */
        val bytesDone = AtomicLong()
        /** Bytes der gerade laufenden Übertragungen (Schlüssel -> Bytes) */
        val inflight = ConcurrentHashMap<String, Long>()
        val stop = AtomicBoolean(false)
        /** Noch verfügbarer Speicher auf dem Handy für Downloads */
        val budget = AtomicLong(budget)
        @Volatile var lastEmit = 0L
    }

    /** Echte Läufe sperren Eingriffe von Hand in den Papierkorb (0.7.0, [MirrorLock]); die Simulation nicht */
    override suspend fun run(): SyncRun = if (dryRun) runInner() else MirrorLock.mutex.withLock { runInner() }

    private suspend fun runInner(): SyncRun {
        run = run.copy(id = dao.insertRun(run))
        var status = RunStatus.OK
        var message: Msg? = null
        var massWarn: Msg? = null
        try {
            if (!dryRun) {
                val day = 24L * 3600 * 1000
                val now = System.currentTimeMillis()
                dao.pruneLogs(now - 30 * day); dao.pruneRuns(now - 90 * day)
            }
            log("I", Msg.of(R.string.eng_start_mirror, trigger, profile.name, dir.labelMsg))
            if (!dryRun && com.sync2pc.app.sync.KnownNetStore.takeNote(context, profile.id)) log("I", Msg.of(R.string.eng_net_relearned))
            // Offene Entscheidung der Schutzbremse -> nichts tun, bis der Benutzer entschieden hat
            if (!dryRun) guards.get(profile.id)?.let { return block(it) }
            emit(str(R.string.eng_reading_folders), 0, 0, 0, 0, null)

            // --- 1. Beide Seiten einlesen ---------------------------------------
            val treeUri = Uri.parse(profile.localTreeUri)
            if (!LocalTree.hasPermission(context, treeUri))
                throw IOException(str(R.string.err_local_access_lost))
            tree = LocalTree(context, treeUri)
            var states = dao.mirrorStates(profile.id).associateBy { it.relPath }
            // Handy-Ordner gelöscht oder verschoben? (Schutzbremse statt "alles gelöscht")
            val localRootOk = try { tree.stat(tree.rootDocId) != null } catch (e: Exception) { false }
            if (!localRootOk) {
                if (states.isNotEmpty()) return block(Guard(profile.id, GuardKind.LOCAL_MISSING))
                throw IOException(str(R.string.eng_local_missing))
            }
            val filter = FileFilter(profile.includePatterns, profile.excludePatterns)
            val skipTrash: (String) -> Boolean = { isTrashDir(it) }
            // Papierkorb-Übernahme ".LeoSync-Bin" -> "@LeoSync-Bin" (SYNC-20261111-244, Stufe B): vor dem Einlesen der Bäume, unter
            // der Sperre dieses Laufs, nie in der Simulation. Nur ohne offene Wiederherstellung, ohne vorgemerktes Leeren und
            // ohne ausstehende Ordner-Bestätigung: sonst stimmten gespeicherte Papierkorb-Pfade nicht mehr. Handy-Seite
            // unabhängig vom PC; die PC-Seite folgt, sobald die Verbindung steht und der PC-Ordner bestätigt vorhanden ist.
            val trashMigrationAllowed = !dryRun && TrashMigration.mayRun(
                dao.pendingRestores(profile.id).size, PurgeStore(context).hasPending(profile.id), guards.pendingCheck(profile.id).isNotEmpty())
            if (trashMigrationAllowed) migrateTrash(PhoneTrashSide(tree), phone = true)
            // `.nomedia` im bestehenden Handy-Papierkorb sicherstellen (SYNC-20261112-249: repariert ältere Papierkörbe und deckt die
            // Übernahme ab); nie in der Simulation, nie ein Fehler des Laufs, höchstens einmal je Lauf
            if (!dryRun) tree.ensureTrashNoMedia(create = false)
            smb = try {
                SmbSession.open(profile)
            } catch (e: Exception) {
                if (dryRun) { message = Msg.of(R.string.eng_offline_preview, com.sync2pc.app.sync.userErrorMsg(e)); null }
                else { if (isNetworkError(e)) connectionFailed = true; throw e }
            }
            val s = smb
            if (!dryRun && s != null && EncryptionAuto.afterOpen(context, profile, s)) log("I", Msg.of(R.string.eng_enc_auto_required))
            // B12 (SYNC-20261114-341): liegt hinter der Freigabe noch dasselbe Laufwerk? VOR Wiederherstellungen, Papierkorb-Leeren,
            // Einlesen und jeder Dateioperation. Ohne Aussage (Abfrage scheitert) nie blockieren. Die Simulation meldet nur.
            if (s != null && profile.id != 0L) {
                val fp = VolumeStamp.fingerprint(profile.smbPath, profile.remoteSubfolder)
                val stamp = s.volumeStamp()
                val stored = guards.volume(profile.id)
                val accept = !dryRun && guards.takeVolumeAccept(profile.id)
                val action = if (accept && stamp != null) VolumeCheck.Action.REMEMBER
                    else VolumeCheck.decide(stored?.first, stored?.second, fp, stamp)
                when (action) {
                    VolumeCheck.Action.HALT -> return block(Guard(profile.id, GuardKind.VOLUME_CHANGED))
                    VolumeCheck.Action.REMEMBER -> if (!dryRun && stamp != null) guards.rememberVolume(profile.id, fp, stamp)
                    VolumeCheck.Action.PROCEED -> {}
                }
            }
            var baseExists = false
            if (s != null) {
                val base = s.path()
                baseExists = s.dirExists(base)
                if (!baseExists && states.isNotEmpty()) {
                    // Schon synchronisiert, aber der PC-Ordner fehlt: nicht leer neu anlegen (sonst gälte alles als gelöscht)
                    return block(Guard(profile.id, GuardKind.REMOTE_MISSING))
                }
                if (!baseExists && !dryRun) { s.ensureDir(base); baseExists = true } // Erstabgleich: Ordner anlegen
                if (trashMigrationAllowed && baseExists && !pcRejectsNewTrashName) migrateTrash(PcTrashSide(s), phone = false)
            }
            // Vorgemerkte Wiederherstellungen (0.5.0) vor dem Einlesen: Der Abgleich sieht danach den neuen Stand
            // (nicht, solange nach einem Ordnerwechsel die Stichprobe aussteht – erst muss der Ordner bestätigt sein)
            var purgesRan = false
            if (!dryRun && s != null && guards.pendingCheck(profile.id).isEmpty()) {
                if (runRestores(s)) states = dao.mirrorStates(profile.id).associateBy { it.relPath }
                // Danach vorgemerktes Leeren des PC-Papierkorbs (0.6.0) – nach dem Wiederherstellen, damit nichts
                // gelöscht wird, was gerade zurückgeholt werden soll
                runPendingPurges(s)
                purgesRan = true
            }

            // recover: liegengebliebene ".s2pold" aufräumen bzw. zurückholen (nicht in der Simulation)
            val localListed = tree.listRecursive(skipTrash, recover = !dryRun).filterKeys { filter.accepts(it) }
            local = localListed.entries.associate { it.key.lowercase() to (it.key to it.value) }
            var remoteListed = emptyMap<String, RemoteEntry>()
            if (s != null) {
                remoteListed = if (baseExists) s.listRecursive(s.path(), skipTrash).filterKeys { filter.accepts(it) } else emptyMap()
                remote = remoteListed.entries.associate { it.key.lowercase() to (it.key to it.value) }
            }
            // SYNC-20261114-337 (B5/B6): nur melden, die Planung bleibt unverändert
            reportNameCollisions(localListed.keys, R.string.eng_on_phone)
            reportNameCollisions(remoteListed.keys, R.string.eng_on_pc)
            reportMissingTimes(localListed.values.map { it.modified })
            val conflicts = dao.conflicts(profile.id).associateBy { it.relPath.lowercase() }
            log("I", Msg.of(R.string.eng_counts, local.size, remote.size))

            // Nach einem Ordnerwechsel: Stichprobe, ob der neue Ordner zum Sync-Stand passt
            val pending = guards.pendingCheck(profile.id)
            if (pending.isNotEmpty() && s != null) {
                if (states.isNotEmpty() && !folderMatches(pending, states.values.toList()))
                    return block(Guard(profile.id, GuardKind.CHECK_FOLDER))
                if (!dryRun) {
                    guards.setPendingCheck(profile.id, emptySet())
                    // 0.11.10 (-034): Vorgemerktes PC-Leeren jetzt ausführen, noch vor Planung und Übertragung. Sonst
                    // legt dieser Lauf ersetzte Versionen in den PC-Papierkorb, und der nächste Lauf würde sie bei
                    // einer Regel "Alles"/"Tage" mit heutigem Datum ungesehen mitlöschen. Bewusst nur das Leeren,
                    // keine Wiederherstellungen (local/remote sind schon eingelesen; Papierkorb-Ordner sind davon
                    // ausgeschlossen, das Leeren ändert die Listen also nicht). Netzfehler: Vormerkung bleibt, Abbruch.
                    if (!purgesRan) { runPendingPurges(s); purgesRan = true }
                }
            }
            // "Nicht löschen" bei der Schutzbremse: dieser Lauf übernimmt keine Löschungen
            if (!dryRun && guards.takeKeepFiles(profile.id)) {
                propagate = false
                log("I", Msg.of(R.string.eng_keep_files))
            }
            if (!dryRun) purgeTrash()

            // --- 2. Planen -------------------------------------------------------
            emit(str(R.string.eng_comparing), 0, 0, 0, 0, null)
            val actions = ArrayList<Action>()
            // Erledigte Konflikte erst nach der Schutzbremse entfernen (sonst ginge die Entscheidung verloren)
            val doneConflicts = ArrayList<DoneConflict>()
            // SYNC-20261115-399: Konflikte für Pfade, die der Filter jetzt ausschliesst (z. B. .thumbnails): erledigt, es wird nichts übertragen
            // oder gelöscht (die Pfade fehlen in beiden Listen; mit Sync-Stand plant der Planer nur DROP)
            for (c in conflicts.values) if (!filter.accepts(c.relPath)) doneConflicts += DoneConflict(c.relPath.lowercase(), c.relPath, null)
            val keys = (local.keys + remote.keys + states.keys).toSortedSet()
            for (k in keys) {
                currentCoroutineContext().ensureActive()
                val l = local[k]; val r = remote[k]; val st = states[k]
                val rel = l?.first ?: r?.first ?: k
                val c = conflicts[k]
                if (c != null) {
                    if (l != null && r != null) {
                        // R-K3 (-333): Die Entscheidung gilt nur für die Versionen, über die der Benutzer entschieden hat. Hat sich eine
                        // Seite seither geändert, wird der Konflikt mit den neuen Werten wieder geöffnet (nichts wird übertragen).
                        if (c.resolution != null && !TransferIntegrity.conflictStillCurrent(
                                c.localSize, c.localModified, c.remoteSize, c.remoteModified,
                                TransferIntegrity.stampOf(l.second), TransferIntegrity.Stamp(r.second.size, r.second.modified))) {
                            if (!dryRun) {
                                newConflicts++
                                dao.upsertConflict(MirrorConflict(profile.id, c.relPath, l.second.size, l.second.modified,
                                    r.second.size, r.second.modified, System.currentTimeMillis()))
                            }
                            log("W", Msg.of(R.string.eng_conflict_reopened, c.relPath))
                            continue
                        }
                        when (c.resolution) {
                            null -> continue // wartet weiterhin auf Entscheidung
                            ConflictResolution.KEEP_LOCAL -> actions += Action(k, rel, Kind.UPLOAD, trashOther = true)
                            ConflictResolution.KEEP_REMOTE -> actions += Action(k, rel, Kind.DOWNLOAD, trashOther = true)
                            else -> actions += Action(k, rel, Kind.KEEP_BOTH)
                        }
                        doneConflicts += DoneConflict(k, c.relPath, c.resolution)
                        continue
                    }
                    // Eine Seite ist inzwischen weg -> Konflikt erledigt, normal weiter (unbedingt löschen)
                    doneConflicts += DoneConflict(k, c.relPath, null)
                }
                val planned = plan(k, rel, l?.second, r?.second, st)
                // Vergleich wegen WLAN-Verlust abgebrochen: Ergebnis wäre "nicht gleich" ohne Beweis -> nichts ausführen
                if (hashAborted) break
                planned?.let { actions += it }
            }
            skipInvalidWindowsNames(actions)
            // B2 (-340): Umbenennen/Verschieben erkennen; Paare zählen nicht als Löschung (Schutzbremse) und nicht als Übertragung
            val renamePlans = if (dryRun && smb == null) emptyList() else detectRenames(actions, states, conflicts)
            if (hashAborted) {
                wifiLost = true
                log("W", Msg.of(R.string.eng_wifi_lost))
                return finish(RunStatus.PARTIAL, Msg.of(R.string.eng_wifi_lost))
            }

            // Schutzbremse: zu viele Löschungen (Anteil pro Profil, ab 100 Dateien immer) oder ein ganzer Unterordner
            val trashRels = actions.filter { it.kind == Kind.TRASH_LOCAL || it.kind == Kind.TRASH_REMOTE }.map { it.rel }
            val toTrash = trashRels.size
            val allowed = !dryRun && guards.takeAllowDelete(profile.id) // einmalige Freigabe immer verbrauchen
            val vanished = GuardPolicy.vanishedFolder(trashRels, states.keys)
            if ((GuardPolicy.massDelete(toTrash, states.size, profile.massDeletePercent) || vanished != null) && !allowed) {
                val g = Guard(profile.id, GuardKind.MASS_DELETE, toTrash, states.size,
                    folder = if (GuardPolicy.massDelete(toTrash, states.size, profile.massDeletePercent)) "" else vanished.orEmpty())
                if (!dryRun) return block(g)
                massWarn = Msg.of(R.string.eng_would_stop,
                    if (g.folder.isNotEmpty()) Msg.of(R.string.eng_would_trash_folder, g.folder)
                    else Msg.of(R.string.eng_would_trash_files, toTrash, states.size))
            }
            if (!dryRun) for (d in doneConflicts) {
                if (d.expected == null) { dao.deleteConflict(profile.id, d.relPath); continue }
                // Entscheidung des Benutzers: nur löschen, wenn sie noch genau so gespeichert ist. Hat er inzwischen
                // «Rückgängig» gedrückt, wird die Aktion nicht ausgeführt (SYNC-20261111-172)
                if (dao.deleteConflictIf(profile.id, d.relPath, d.expected) == 0) {
                    actions.removeAll { it.key == d.key }
                    log("I", Msg.of(R.string.eng_conflict_undone, d.relPath))
                }
            }

            // --- 3. Ausführen -----------------------------------------------------
            // Phase A (nacheinander): Verwaltung – Papierkorb, Stand merken, Konflikte, Aufräumen
            // Phase B (parallel):     Übertragungen, kleine Dateien zuerst
            val transfers = actions.filter { it.kind == Kind.UPLOAD || it.kind == Kind.DOWNLOAD || it.kind == Kind.KEEP_BOTH }
            if (dryRun) {
                for (rp in renamePlans) { if (preview.size >= previewLimit) break; preview += PreviewEntry(str(R.string.eng_pv_rename), "${rp.from} → ${rp.to}", false) }
                for (a in actions) { if (preview.size >= previewLimit) break; previewOf(a) }
                val visible = actions.count { it.kind != Kind.RECORD && it.kind != Kind.DROP }
                if (message == null) message = Msg.of(if (preview.size < visible) R.string.eng_actions_first else R.string.eng_actions_all,
                    visible, preview.size)
                massWarn?.let { w -> message = Msg.join("\n", listOfNotNull(w, message)) }
            } else {
                if (renamePlans.isNotEmpty()) executeRenames(renamePlans, smb!!)
                val admin = actions.filter { it !in transfers }
                for ((index, a) in admin.withIndex()) {
                    currentCoroutineContext().ensureActive()
                    if (index % 10 == 0 && !shouldContinue()) { wifiLost = true; break }
                    try {
                        executeAdmin(a, smb!!)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        // Verbindungsabbruch ist erwartbar -> Lauf unterbrechen, kein Datei-Fehler
                        if (isNetworkError(e)) { connectionFailed = true; throw e }
                        failed.incrementAndGet()
                        log("E", "✗ ${a.rel}: ${com.sync2pc.app.sync.shortError(e)}")
                    }
                }
                if (!wifiLost && transfers.isNotEmpty()) runTransfers(transfers)
                if (wifiLost) {
                    status = RunStatus.PARTIAL; message = Msg.of(R.string.eng_wifi_lost)
                    log("W", Msg.of(R.string.eng_wifi_lost))
                }
            }

            if (status == RunStatus.OK && (failed.get() > 0 || spaceShortage)) status = RunStatus.PARTIAL
            val summary = message ?: com.sync2pc.app.sync.RunSummary.mirror(
                up.get(), down.get(), trashed.get(), newConflicts, failed.get(), spaceShortage, deferred.get(), renamed.get()
            )
            return finish(status, summary)
        } catch (e: CancellationException) {
            withContext(NonCancellable) {
                val reason = interruptReason()
                if (reason != null) {
                    log("W", Msg.of(R.string.eng_interrupted_log, reason))
                    finish(RunStatus.INTERRUPTED, Msg.of(R.string.eng_interrupted_mirror, reason, up.get(), down.get()))
                } else {
                    log("W", Msg.of(R.string.eng_paused_log))
                    finish(RunStatus.CANCELLED, Msg.of(R.string.eng_paused_log))
                }
            }
            throw e
        } catch (e: Exception) {
            if (isNetworkError(e)) {
                connectionFailed = true
                log("W", Msg.of(R.string.eng_connection_log_mirror, com.sync2pc.app.sync.shortError(e)))
                return finish(RunStatus.INTERRUPTED, Msg.of(R.string.eng_connection_mirror, up.get(), down.get()))
            }
            if (com.sync2pc.app.sync.isEncryptionUnavailable(e)) encryptionUnavailable = true
            log("E", Msg.of(R.string.eng_run_failed, com.sync2pc.app.sync.shortError(e)))
            return finish(RunStatus.FAILED, com.sync2pc.app.sync.userErrorMsg(e))
        } finally {
            smb?.close()
        }
    }

    // ---- Planung einer Datei ---------------------------------------------------------
    private suspend fun plan(k: String, rel: String, l: LocalEntry?, r: RemoteEntry?, st: MirrorState?): Action? =
        MirrorPlanner.decide(
            l, r, st, dir, propagate,
            conflictNewer = profile.conflict() == ConflictMode.NEWER, keepVersions = profile.keepVersions,
            sameContent = { sameContent(l!!, rel, r!!) },
        )?.let {
            // R-D1 (-337): Löschung nicht übernommen, weil die Datei auf der anderen Seite geändert wurde
            if (it.overruledDeletion) log("I", Msg.of(
                if (it.kind == PlanKind.UPLOAD) R.string.eng_deletion_overruled_phone else R.string.eng_deletion_overruled_pc, rel))
            Action(k, rel, it.kind, it.trashOther)
        }

    /** Namenskollisionen einer Seite (nur Gross-/Kleinschreibung bzw. Unicode-Schreibweise) einmal je Paar und Lauf melden */
    private suspend fun reportNameCollisions(paths: Collection<String>, whereRes: Int) {
        for (c in NameCollisions.find(paths)) log("W", Msg.of(
            if (c.kind == NameCollisions.Kind.CASE_ONLY) R.string.eng_name_collision_case else R.string.eng_name_collision_unicode,
            c.a, c.b, Msg.of(whereRes)))
    }

    /** R-M1: Speicherort liefert keine Änderungszeiten -> einmal je Profil und Ordner ein Hinweis (nicht in der Simulation) */
    private suspend fun reportMissingTimes(times: Collection<Long>) {
        if (dryRun || !ZeroTimeRule.applies(times)) return
        val prefs = context.getSharedPreferences("mirror_hints", Context.MODE_PRIVATE)
        val key = "zerotime|${profile.id}|${profile.localTreeUri}"
        if (prefs.getBoolean(key, false)) return
        log("W", Msg.of(R.string.eng_zero_time))
        prefs.edit().putBoolean(key, true).commit()
    }

    /**
     * B5 (-337): Namen, die ein Windows-PC nicht erlaubt, werden nie hochgeladen und nie still umbenannt: überspringen mit Hinweis
     * (einmal je ungültigem Ordner bzw. je Datei). Betrifft nur Übertragungen zum PC; kein Fehler, kein Netzwerkproblem.
     */
    private suspend fun skipInvalidWindowsNames(actions: MutableList<Action>) {
        val reported = HashSet<String>()
        val skipped = ArrayList<Action>()
        for (a in actions) {
            if (a.kind != Kind.UPLOAD && a.kind != Kind.KEEP_BOTH) continue
            val bad = WindowsNames.invalidPart(a.rel) ?: continue
            // Pfad bis einschliesslich des ungültigen Teils (Ordner nur einmal melden)
            val upTo = a.rel.split('/').let { parts -> parts.take(parts.indexOf(bad.first) + 1).joinToString("/") }
            if (reported.add(upTo.lowercase())) log("W", Msg.of(R.string.eng_name_invalid_pc, upTo))
            skipped += a
        }
        actions.removeAll(skipped.toSet())
    }

    /**
     * Gleicher Inhalt? Nur beim Erstabgleich, bei auf beiden Seiten geänderten Dateien und bei einseitig geänderten
     * Dateien gleicher Grösse nötig, darum gründlich: immer die GANZE Datei beider Seiten (R-I3, SYNC-20261111-142,
     * vorher über 32 MB nur 32 Stichproben: eine Änderung dazwischen wurde nie übertragen). Das Lesen ist abbrechbar
     * (Coroutine, WLAN weg) und meldet bei grossen Dateien den Fortschritt. Lesefehler oder Abbruch ergeben
     * "nicht gleich" (siehe [ContentCompare]); bei WLAN-Verlust endet der Lauf, bevor etwas ausgeführt wird.
     */
    private suspend fun sameContent(l: LocalEntry, rel: String, r: RemoteEntry): Boolean {
        val s = smb ?: return false
        if (l.size != r.size) return false
        val ctx = currentCoroutineContext()
        var lastNetCheck = 0L
        val check: () -> Unit = {
            ctx.ensureActive()
            val now = System.currentTimeMillis()
            if (now - lastNetCheck > 2_000) {
                lastNetCheck = now
                if (!shouldContinue()) throw HashAbortedException()
            }
        }
        val big = l.size > PartialHash.FULL_LIMIT
        var doneLocal = 0L
        var doneRemote = 0L
        fun report() {
            val now = System.currentTimeMillis()
            if (now - lastCompareEmit < 700) return
            lastCompareEmit = now
            emit(str(R.string.eng_comparing), 0, 0, doneLocal + doneRemote, l.size * 2, rel)
        }
        return try {
            ContentCompare.same(l.size, r.size,
                { tree.contentHash(l.docId, l.size, check) { b -> if (big) { doneLocal = b; report() } } },
                { s.contentHash(remotePath(rel), r.size, check) { b -> if (big) { doneRemote = b; report() } } })
        } catch (e: HashAbortedException) {
            hashAborted = true
            false
        }
    }

    /** Stichprobe nach einem Ordnerwechsel: passt der Ordner auf den gewechselten Seiten zum Sync-Stand? */
    private fun folderMatches(sides: Set<Side>, states: List<MirrorState>): Boolean {
        val sample = GuardPolicy.sample(states)
        val dirs = GuardPolicy.topDirs(states.map { it.relPath })
        return sides.all { side ->
            val sizes: Map<String, Long> =
                if (side == Side.LOCAL) local.mapValues { it.value.second.size } else remote.mapValues { it.value.second.size }
            val existingDirs = sizes.keys.filter { '/' in it }.map { it.substringBefore('/') }.toSet()
            GuardPolicy.matches(sample, dirs, side, { sizes[it] }, { it in existingDirs })
        }
    }

    /**
     * Schutzbremse: Lauf anhalten und auf den Benutzer warten (Startseite + Hinweis).
     * In der Simulation wird nur gemeldet, dass der Lauf anhalten würde.
     */
    private suspend fun block(g: Guard): SyncRun {
        val msg = if (dryRun) Msg.of(R.string.eng_would_stop, MirrorGuard.titleMsg(g))
        else Msg.of(R.string.eng_waiting_decision, MirrorGuard.titleMsg(g))
        if (!dryRun) { guards.set(g); blocked = true }
        log("W", msg)
        return finish(RunStatus.WAITING, msg)
    }

    /** Ist die PC-Datei noch so wie beim Einlesen? (beide null = weiterhin nicht vorhanden) */
    private fun unchanged(now: RemoteEntry?, listed: RemoteEntry?): Boolean =
        if (now == null || listed == null) now == null && listed == null
        else now.size == listed.size && now.modified == listed.modified

    private fun unchanged(now: LocalEntry?, listed: LocalEntry?): Boolean =
        if (now == null || listed == null) now == null && listed == null
        else now.size == listed.size && now.modified == listed.modified

    /**
     * Handy-Datei jetzt, per Ordnerliste wie beim Einlesen; null = verschwunden. Lässt sich der Ordner nicht lesen, gilt die Datei
     * als unverändert ([fallback]): dann entscheidet wie bisher die Bytezahl (SYNC-20261111-202).
     */
    private fun findNow(rel: String, fallback: LocalEntry): LocalEntry? =
        try { tree.find(rel) } catch (e: CancellationException) { throw e } catch (e: Exception) { fallback }

    // ---- Ausführung ------------------------------------------------------------------
    private fun remotePath(s: SmbSession, rel: String): String = s.path(rel.replace('/', '\\'))
    private fun remotePath(rel: String): String = remotePath(smb!!, rel)

    /** Grösse einer Übertragung (für Reihenfolge, Fortschritt und Speicherbudget) */
    private fun sizeOf(a: Action): Long = when (a.kind) {
        Kind.UPLOAD -> local[a.key]?.second?.size ?: 0
        Kind.DOWNLOAD -> remote[a.key]?.second?.size ?: 0
        else -> (local[a.key]?.second?.size ?: 0) + (remote[a.key]?.second?.size ?: 0)
    }

    // ---- Umbenennen und Verschieben erkennen (SYNC-20261114-340, B2) ---------------------------------------------

    /** Geplante Umbenennung/Verschiebung: [onPc] = auf dem PC ausführen (Handy wurde umbenannt), sonst auf dem Handy */
    private class RenamePlan(val onPc: Boolean, val from: String, val to: String, val isDir: Boolean, val pairs: List<RenameMatcher.Pair>)

    private val renamed = AtomicInteger()

    private fun renameOffKey() = "renameOff|${profile.id}|${profile.localTreeUri}"

    /**
     * Sucht in der Plan-Liste Paare «alter Name gelöscht + neuer Name neu» und ersetzt sie durch Umbenennungen. Nur mit «Löschungen
     * übernehmen» (sonst bleiben Dubletten gewollt). Die Paare verschwinden aus [actions] und zählen damit nicht für die Schutzbremse.
     * Jede Inhaltsprüfung (PartialHash.thorough, grosse Dateien nur Stichproben) muss bestätigen; jede Unsicherheit = kein Paar.
     */
    private suspend fun detectRenames(
        actions: MutableList<Action>, states: Map<String, MirrorState>, conflicts: Map<String, MirrorConflict>,
    ): List<RenamePlan> {
        val s = smb ?: return emptyList()
        if (!propagate) return emptyList()
        if (context.getSharedPreferences("mirror_hints", Context.MODE_PRIVATE).getBoolean(renameOffKey(), false)) return emptyList()
        val plans = ArrayList<RenamePlan>()
        val removeKeys = HashSet<String>()

        suspend fun run(onPc: Boolean) {
            val trashKind = if (onPc) Kind.TRASH_REMOTE else Kind.TRASH_LOCAL
            val moveKind = if (onPc) Kind.UPLOAD else Kind.DOWNLOAD
            val olds = ArrayList<RenameMatcher.Old>()
            for (a in actions) {
                if (a.kind != trashKind || a.key in removeKeys || conflicts[a.key] != null) continue
                val st = states[a.key] ?: continue
                if (onPc) { // Handy-Datei fehlt, PC-Datei noch wie im Stand
                    val r = remote[a.key]?.second ?: continue
                    if (local[a.key] != null || r.size != st.remoteSize || r.modified != st.remoteModified) continue
                    olds += RenameMatcher.Old(a.key, a.rel, RenameMatcher.Stamp(st.localSize, st.localModified))
                } else { // PC-Datei fehlt, Handy-Datei noch wie im Stand
                    val l = local[a.key]?.second ?: continue
                    if (remote[a.key] != null || l.size != st.localSize || l.modified != st.localModified) continue
                    olds += RenameMatcher.Old(a.key, a.rel, RenameMatcher.Stamp(st.remoteSize, st.remoteModified))
                }
            }
            val news = ArrayList<RenameMatcher.New>()
            for (a in actions) {
                if (a.kind != moveKind || a.trashOther || a.key in removeKeys || conflicts[a.key] != null || states[a.key] != null) continue
                if (onPc) {
                    val l = local[a.key]?.second ?: continue
                    if (remote[a.key] != null) continue
                    news += RenameMatcher.New(a.key, a.rel, RenameMatcher.Stamp(l.size, l.modified))
                } else {
                    val r = remote[a.key]?.second ?: continue
                    if (local[a.key] != null) continue
                    news += RenameMatcher.New(a.key, a.rel, RenameMatcher.Stamp(r.size, r.modified))
                }
            }
            if (olds.isEmpty() || news.isEmpty()) return
            // Inhaltsprüfung: neue Datei auf A gegen die alte Datei auf B (gründlich)
            val verified = RenameMatcher.match(olds, news).filter { p ->
                currentCoroutineContext().ensureActive()
                try {
                    if (onPc) {
                        val l = local[p.new.key]!!.second; val r = remote[p.old.key]!!.second
                        l.size == r.size && PartialHash.thorough(l.size) { off, len -> tree.readAt(l.docId, off, len) } ==
                            s.thoroughHash(remotePath(p.old.rel), r.size)
                    } else {
                        val l = local[p.old.key]!!.second; val r = remote[p.new.key]!!.second
                        l.size == r.size && PartialHash.thorough(l.size) { off, len -> tree.readAt(l.docId, off, len) } ==
                            s.thoroughHash(remotePath(p.new.rel), r.size)
                    }
                } catch (e: CancellationException) { throw e } catch (e: Exception) { false }
            }
            if (verified.isEmpty()) return
            val grouped = RenameMatcher.group(
                verified, stateKeys = states.keys,
                otherKeys = if (onPc) remote.keys else local.keys,
                otherDirExists = { dir -> if (onPc) s.dirExists(remotePath(dir)) else tree.findDir(dir) != null },
            )
            for (d in grouped.dirs) {
                plans += RenamePlan(onPc, d.from, d.to, true, d.pairs)
                d.pairs.forEach { removeKeys += it.old.key; removeKeys += it.new.key }
            }
            for (p in grouped.files) {
                plans += RenamePlan(onPc, p.old.rel, p.new.rel, false, listOf(p))
                removeKeys += p.old.key; removeKeys += p.new.key
            }
        }
        if (dir.up) run(onPc = true)
        if (dir.down) run(onPc = false)
        if (removeKeys.isNotEmpty()) actions.removeAll { it.key in removeKeys && (it.kind == Kind.TRASH_REMOTE || it.kind == Kind.TRASH_LOCAL || it.kind == Kind.UPLOAD || it.kind == Kind.DOWNLOAD) }
        return plans
    }

    /**
     * Führt die erkannten Umbenennungen aus (Phase A, vor den Übertragungen). Unmittelbar vorher wird geprüft, dass Quelle und Ziel noch
     * so sind wie beim Einlesen; jeder Fehler lässt das Paar unberührt (nie halb ausgeführt, der nächste Lauf entscheidet). Kann der
     * Speicherort auf dem Handy nicht verschieben, wird die Erkennung für dieses Profil abgeschaltet (kein Kopieren).
     */
    private suspend fun executeRenames(plans: List<RenamePlan>, s: SmbSession) {
        for (plan in plans) {
            currentCoroutineContext().ensureActive()
            try {
                val fromPath = remotePath(s, plan.from); val toPath = remotePath(s, plan.to)
                if (plan.onPc) {
                    // Handy wurde umbenannt: auf dem PC dasselbe tun
                    val sourceOk = plan.pairs.all { p -> unchanged(s.stat(remotePath(s, p.old.rel)), remote[p.old.key]?.second) }
                    val phoneOk = plan.pairs.all { p -> local[p.new.key]?.second?.let { unchanged(findNow(p.new.rel, it), it) } == true }
                    val targetFree = if (plan.isDir) !s.dirExists(toPath) else s.stat(toPath) == null
                    if (!sourceOk || !phoneOk || !targetFree) { log("W", Msg.of(R.string.eng_rename_skipped, plan.from, plan.to)); continue }
                    s.ensureDir(toPath.substringBeforeLast('\\', ""))
                    if (plan.isDir) s.renameDir(fromPath, toPath) else s.move(fromPath, toPath)
                    for (p in plan.pairs) {
                        val rNow = s.stat(remotePath(s, p.new.rel)) ?: throw IOException(str(R.string.eng_err_after_upload_missing))
                        dao.deleteMirrorState(profile.id, p.old.key)
                        record(p.new.key, local[p.new.key]!!.second, rNow)
                    }
                    log("I", Msg.of(R.string.eng_renamed_pc, plan.from, plan.to))
                } else {
                    // PC wurde umbenannt: auf dem Handy dasselbe tun (nie kopieren)
                    val sourceOk = plan.pairs.all { p -> local[p.old.key]?.second?.let { unchanged(findNow(p.old.rel, it), it) } == true }
                    val pcOk = plan.pairs.all { p -> unchanged(s.stat(remotePath(s, p.new.rel)), remote[p.new.key]?.second) }
                    val targetFree = if (plan.isDir) tree.findDir(plan.to) == null else tree.find(plan.to) == null
                    if (!sourceOk || !pcOk || !targetFree) { log("W", Msg.of(R.string.eng_rename_skipped, plan.from, plan.to)); continue }
                    val docId = if (plan.isDir) tree.findDir(plan.from) else local[plan.pairs[0].old.key]?.second?.docId
                    localTouched = true
                    val moved = docId?.let { tree.relocate(plan.from, it, plan.to) }
                    if (moved == null) {
                        context.getSharedPreferences("mirror_hints", Context.MODE_PRIVATE).edit().putBoolean(renameOffKey(), true).commit()
                        log("W", Msg.of(R.string.eng_rename_unsupported))
                        return
                    }
                    for (p in plan.pairs) {
                        val ls = (try { tree.find(p.new.rel) } catch (e: Exception) { null })
                            ?: throw IOException(str(R.string.eng_err_after_download_missing))
                        dao.deleteMirrorState(profile.id, p.old.key)
                        record(p.new.key, ls, remote[p.new.key]!!.second)
                    }
                    log("I", Msg.of(R.string.eng_renamed_phone, plan.from, plan.to))
                }
                renamed.addAndGet(plan.pairs.size)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (isNetworkError(e)) { connectionFailed = true; throw e }
                log("W", Msg.of(R.string.eng_rename_failed, plan.from, plan.to, com.sync2pc.app.sync.shortError(e)))
            }
        }
    }
    /** Phase A: Verwaltungsschritte (nacheinander, Haupt-Verbindung) */
    /** Auflösung/Dauer einer PC-Mediendatei lesen (höchstens 1 MiB, 5 s); jede Ausnahme = unbekannt, Abbruch des Laufs wird weitergereicht */
    private suspend fun probeRemote(s: SmbSession, rel: String, size: Long): MediaDims? {
        if (!MediaProbe.isMedia(rel)) return null
        return try {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                s.withRange(remotePath(s, rel), size) { AndroidMediaProbe.read(rel, BudgetedSource(it)) }
            }
        } catch (e: kotlin.coroutines.cancellation.CancellationException) { throw e }
        catch (e: Exception) { null }
    }

    private suspend fun executeAdmin(a: Action, s: SmbSession) {
        val l = local[a.key]?.second
        val r = remote[a.key]?.second
        when (a.kind) {
            Kind.TRASH_LOCAL -> {
                l ?: return
                // Nur löschen, was noch dem Einlesestand entspricht (R-I7, -333); sonst zurückstellen, Stand bleibt
                if (!unchanged(findNow(a.rel, l), l)) return skipChanged(a.rel, Msg.of(R.string.eng_on_phone))
                localTouched = true; tree.moveToTrash(a.rel, l, today); trashed.incrementAndGet()
                dao.deleteMirrorState(profile.id, a.key)
                log("I", Msg.of(R.string.eng_trash_local, a.rel))
            }
            Kind.TRASH_REMOTE -> {
                if (r != null && !unchanged(s.stat(remotePath(s, a.rel)), r)) return skipChanged(a.rel, Msg.of(R.string.eng_on_pc))
                trashRemote(s, a.rel)
                dao.deleteMirrorState(profile.id, a.key)
                log("I", Msg.of(R.string.eng_trash_remote, a.rel))
            }
            Kind.RECORD -> if (l != null && r != null) record(a.key, l, r)
            Kind.DROP -> dao.deleteMirrorState(profile.id, a.key)
            Kind.CONFLICT -> {
                if (l == null || r == null) return
                newConflicts++
                // Auflösung/Dauer der PC-Seite (SYNC-20261115-367): nur lesen, wenn neu, geändert oder fehlend (Fehlversuch = 0, nicht wiederholt)
                val old = dao.conflicts(profile.id).firstOrNull { it.relPath == a.rel }
                val reuse = old != null && old.remoteSize == r.size && old.remoteModified == r.modified && old.remoteWidth != null
                val dims = if (reuse) null else probeRemote(s, a.rel, r.size)
                dao.upsertConflict(
                    MirrorConflict(
                        profile.id, a.rel, l.size, l.modified, r.size, r.modified, System.currentTimeMillis(),
                        remoteWidth = if (reuse) old.remoteWidth else if (MediaProbe.isMedia(a.rel)) (dims?.width ?: 0) else null,
                        remoteHeight = if (reuse) old.remoteHeight else if (MediaProbe.isMedia(a.rel)) (dims?.height ?: 0) else null,
                        remoteDurationMs = if (reuse) old.remoteDurationMs else dims?.durationMs,
                    )
                )
                log("W", Msg.of(R.string.eng_conflict, a.rel))
            }
            Kind.UPLOAD, Kind.DOWNLOAD, Kind.KEEP_BOTH -> {} // Phase B
        }
    }

    /**
     * Phase B: Übertragungen parallel (Einstellung "Parallele Übertragungen"), kleine Dateien zuerst.
     * Jede Übertragung hat ihre eigene SMB-Verbindung. Bei Verbindungsabbruch einmal neu verbinden und
     * wiederholen; ist das Netz weg, wird der ganze Lauf unterbrochen (Rest folgt beim nächsten Sync).
     */
    private suspend fun runTransfers(transfers: List<Action>) {
        val ordered = TransferPlan.smallFirst(transfers) { sizeOf(it) }
        val sh = Shared(ordered.size, ordered.sumOf { sizeOf(it) }, freeSpace() - RESERVE_BYTES)
        val queue = ConcurrentLinkedQueue(ordered)
        val parallel = GlobalSettings(context).parallelUploads.coerceIn(1, 4).coerceAtMost(ordered.size)
        log("I", Msg.plural(R.plurals.eng_transfer_mirror, ordered.size, ordered.size, parallel))
        emitShared(sh, null, force = true)
        coroutineScope {
            repeat(parallel) { launch { transferWorker(queue, sh) } }
        }
    }

    /** Schlüssel der Dateien, die dieser Lauf selbst schon in den Papierkorb verschoben hat (für die Wiederholung, -166) */
    private val trashedByRun: MutableSet<String> = java.util.concurrent.ConcurrentHashMap.newKeySet()

    /** «Beide behalten», von diesem Lauf schon umbenannt: Schlüssel -> (neuer relativer Pfad, neue Dokument-ID) (-171) */
    private val renamedByRun = ConcurrentHashMap<String, Pair<String, String>>()

    /** «Beide behalten», Konfliktkopie schon hochgeladen: bei einer Wiederholung nur noch der Download (-171) */
    private val uploadedByRun: MutableSet<String> = ConcurrentHashMap.newKeySet()

    private suspend fun transferWorker(queue: ConcurrentLinkedQueue<Action>, sh: Shared) {
        var session: SmbSession? = null
        try {
            while (!sh.stop.get()) {
                val a = queue.poll() ?: break
                currentCoroutineContext().ensureActive()
                if (sh.done.get() % 10 == 0 && !shouldContinue()) {
                    if (sh.stop.compareAndSet(false, true)) wifiLost = true
                    break
                }
                val size = sizeOf(a)
                // Speicher auf dem Handy für Downloads gemeinsam verrechnen
                val needs = if (a.kind == Kind.DOWNLOAD || a.kind == Kind.KEEP_BOTH) remote[a.key]?.second?.size ?: 0 else 0
                if (needs > 0 && sh.budget.addAndGet(-needs) < 0) {
                    sh.budget.addAndGet(needs)
                    spaceShortage = true
                    log("W", Msg.of(R.string.eng_no_space, a.rel))
                    countDone(sh, size)
                    continue
                }
                var attempt = 0
                while (true) {
                    try {
                        // Tote Verbindung (Leerlauf-Trennung, kurzer WLAN-Aussetzer) -> neu aufbauen
                        val s = session?.takeIf { it.isAlive() }
                            ?: SmbSession.open(profile).also { try { session?.close() } catch (ignored: Exception) {}; session = it }
                        executeTransfer(a, s) { b ->
                            sh.inflight[a.key] = b
                            emitShared(sh, a.rel)
                        }
                        break
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        if (isNetworkError(e)) {
                            try { session?.close() } catch (ignored: Exception) {}
                            session = null
                            if (attempt++ == 0) { delay(2_000); continue } // Upload/Download setzt an der Abbruchstelle fort
                            sh.stop.set(true)
                            connectionFailed = true
                            throw e // Netz wirklich weg -> Lauf unterbrechen
                        }
                        failed.incrementAndGet()
                        log("E", "✗ ${a.rel}: ${com.sync2pc.app.sync.shortError(e)}")
                        break
                    }
                }
                sh.inflight.remove(a.key)
                countDone(sh, size)
            }
        } finally {
            session?.close()
        }
    }

    private suspend fun executeTransfer(a: Action, s: SmbSession, onBytes: (Long) -> Unit) {
        val l = local[a.key]?.second
        val r = remote[a.key]?.second
        when (a.kind) {
            Kind.UPLOAD -> {
                l ?: return
                // Handy-Datei seit dem Einlesen schon vor dem Start geändert (oder verschwunden)? Dann zurückstellen, und zwar
                // vor jedem Eingriff am PC (Papierkorb) (SYNC-20261111-202). Per Ordnerliste lesen wie beim Einlesen.
                if (!unchanged(findNow(a.rel, l), l)) return skipChanged(a.rel, Msg.of(R.string.eng_on_phone))
                // Während des Laufs auf dem PC geändert? Dann nicht überschreiben – der nächste Lauf entscheidet.
                // Wiederholung nach Verbindungsabbruch: Hat dieser Lauf die PC-Datei schon in den Papierkorb verschoben,
                // fehlt sie erwartungsgemäss (SYNC-20261111-166, siehe TransferResume)
                val before = a.key in trashedByRun
                val nowRemote = s.stat(remotePath(s, a.rel))
                when (TransferResume.step(a.trashOther, r != null, before, unchanged(nowRemote, r), nowRemote == null)) {
                    TransferStep.SKIP_CHANGED -> return skipChanged(a.rel, Msg.of(R.string.eng_on_pc))
                    TransferStep.TRASH_THEN_TRANSFER -> {
                        // Zuerst den Sync-Stand vergessen: Scheitert der Upload, gilt die Datei beim nächsten Lauf
                        // als neu (wird übertragen) statt als "auf dem PC gelöscht" (würde sonst auch das Handy löschen)
                        dao.deleteMirrorState(profile.id, a.key)
                        trashRemote(s, a.rel)
                        trashedByRun.add(a.key)
                    }
                    TransferStep.TRANSFER -> {}
                }
                try {
                    // Erwartung am Ziel: die PC-Datei beim Einlesen; hat dieser Lauf sie selbst in den Papierkorb verschoben, gibt es sie nicht
                    upload(s, a.key, a.rel, l, checkTarget = true, expectedRemote = if (a.key in trashedByRun) null else r, onBytes = onBytes)
                } catch (e: TargetChangedException) {
                    // PC-Datei während der Übertragung geändert: Temp-Datei ist gelöscht, nichts überschrieben, der nächste Lauf entscheidet
                    return skipChanged(a.rel, Msg.of(R.string.eng_on_pc))
                } catch (e: SourceChangedException) {
                    // Handy-Datei während der Übertragung geändert: Temp-Datei ist gelöscht, kein Sync-Stand für diese Version
                    // geschrieben -> der nächste Lauf überträgt die neue Version (SYNC-20261111-202)
                    return skipChanged(a.rel, Msg.of(R.string.eng_on_phone))
                }
            }
            Kind.DOWNLOAD -> {
                r ?: return
                // Vorhandene Handy-Datei seit dem Einlesen geändert? (Neu entstandene prüft writeFileResumable)
                // Wiederholung nach Verbindungsabbruch: Hat dieser Lauf die Handy-Datei schon in den Papierkorb verschoben,
                // fehlt sie erwartungsgemäss (SYNC-20261111-166, siehe TransferResume)
                val before = a.key in trashedByRun
                // Per Ordnerliste lesen wie beim Einlesen (stat() liefert bei manchen Speicherorten andere Werte)
                val nowLocal = if (l != null || before) try { tree.find(a.rel) } catch (e: Exception) { null } else null
                when (TransferResume.step(a.trashOther, l != null, before, l == null || unchanged(nowLocal, l), nowLocal == null)) {
                    TransferStep.SKIP_CHANGED -> return skipChanged(a.rel, Msg.of(R.string.eng_on_phone))
                    TransferStep.TRASH_THEN_TRANSFER -> {
                        dao.deleteMirrorState(profile.id, a.key)
                        localTouched = true; tree.moveToTrash(a.rel, l!!, today); trashed.incrementAndGet()
                        trashedByRun.add(a.key)
                    }
                    TransferStep.TRANSFER -> {}
                }
                download(s, a.key, a.rel, r, expectNewAfterOwnTrash = before, onBytes = onBytes)
            }
            Kind.KEEP_BOTH -> {
                if (l == null || r == null) return
                // Handy-Version umbenennen und hochladen, PC-Version unter Originalnamen holen (SYNC-20261111-171):
                // freier Name (nie eine vorhandene Datei), Upload ohne Überschreiben, Wiederholung ohne zweites Umbenennen
                val parent = a.rel.substringBeforeLast('/', "")
                val name = a.rel.substringAfterLast('/')
                fun relOf(n: String) = if (parent.isEmpty()) n else "$parent/$n"
                val prior = renamedByRun[a.key]
                val newRel: String
                val newDoc: String
                if (prior != null) {
                    // Wiederholung nach Verbindungsabbruch: Die alte Dokument-ID gilt nicht mehr, nur der Merker
                    newRel = prior.first; newDoc = prior.second
                } else {
                    val newName = ConflictName.free(name) { c ->
                        val rel = relOf(c)
                        local.containsKey(rel.lowercase()) || remote.containsKey(rel.lowercase()) ||
                            (try { tree.find(rel) } catch (e: Exception) { null }) != null ||
                            s.stat(remotePath(s, rel)) != null
                    } ?: throw IOException(str(R.string.eng_err_conflict_name_exhausted, a.rel))
                    newRel = relOf(newName)
                    localTouched = true; newDoc = tree.rename(l.docId, newName)
                    // Sofort vergessen (Review -171 H1): Scheitert eine der folgenden Prüfungen oder stirbt der Prozess, gilt die
                    // Datei im nächsten Lauf als neu. Mit altem Stand sähe der Planer «auf dem Handy gelöscht» und
                    // würde die PC-Version in den Papierkorb verschieben.
                    dao.deleteMirrorState(profile.id, a.key)
                    // Der Anbieter darf den Namen nicht eigenmächtig ändern (z. B. « (1)» anhängen): sonst nicht hochladen.
                    // Die Datei bleibt umbenannt erhalten; der nächste Lauf überträgt sie als neue Datei.
                    val actual = try { tree.stat(newDoc)?.name } catch (e: Exception) { null }
                    if (!ConflictName.sameName(newName, actual))
                        throw IOException(str(R.string.eng_err_conflict_name_differs, newName, actual ?: "?"))
                    renamedByRun[a.key] = newRel to newDoc
                }
                // Hochgeladen wird nur die umbenannte Datei selbst, nie eine zufällig gleichnamige andere
                val listed = try { tree.find(newRel) } catch (e: Exception) { null }
                val ls = listed?.takeIf { it.docId == newDoc } ?: tree.stat(newDoc)
                    ?: throw IOException(str(R.string.eng_err_renamed_missing))
                dao.deleteMirrorState(profile.id, a.key)
                // Wiederholung nach einem Abbruch im Download: Der Upload ist schon fertig und darf nicht erneut laufen
                // (die Zieldatei existiert, ohne Überschreiben gäbe es einen Fehler; Review -171 M1)
                if (a.key !in uploadedByRun) {
                    try {
                        // Vergleichsbasis nur, wenn ls aus der Ordnerliste stammt (stat() liefert z. B. andere Zeitstempel)
                        upload(s, newRel.lowercase(), newRel, ls, replaceExisting = false, watchSource = listed?.docId == newDoc, onBytes = onBytes)
                    } catch (e: SourceChangedException) {
                        // Konfliktkopie während der Übertragung geändert: nichts hochgeladen; der Sync-Stand ist schon vergessen,
                        // der nächste Lauf überträgt sie als neue Datei und holt die PC-Version (SYNC-20261111-202)
                        return skipChanged(a.rel, Msg.of(R.string.eng_on_phone))
                    }
                    uploadedByRun.add(a.key)
                }
                // Der Originalname ist nach dem Umbenennen frei: eine dort inzwischen neu angelegte Handy-Datei darf nie ersetzt
                // werden (-333 Teil 5; vorher lief der Download ohne diese Erwartung und löschte sie über .s2pold)
                download(s, a.key, a.rel, r, expectNewAfterOwnTrash = true) { b -> onBytes(ls.size + b) }
            }
            else -> {} // Phase A
        }
    }

    private suspend fun upload(
        s: SmbSession, key: String, rel: String, l: LocalEntry, replaceExisting: Boolean = true, watchSource: Boolean = true,
        /** Zieldatei unmittelbar vor dem Ersetzen prüfen (-333): erwartet wird die PC-Datei beim Einlesen, null = es gab keine */
        checkTarget: Boolean = false, expectedRemote: RemoteEntry? = null,
        onBytes: (Long) -> Unit,
    ) {
        val path = remotePath(s, rel)
        s.ensureDir(path.substringBeforeLast('\\', ""))
        val ctx = currentCoroutineContext()
        s.upload(
            finalPath = path, size = l.size,
            openInput = { off -> tree.openInput(l.docId, off) },
            onProgress = onBytes, checkCancel = { ctx.ensureActive() },
            modifiedMillis = l.modified, replaceExisting = replaceExisting,
            // Fortsetzen einer angefangenen .s2ptmp nur mit Prüfung gegen die Handy-Datei (0.4.1)
            expectedHash = { PartialHash.compute(l.size) { off, len -> tree.readAt(l.docId, off, len) } },
            // Handy-Datei während der Übertragung geändert/gewachsen? Dann Temp-Datei verwerfen, zurückstellen (-202)
            sourceChanged = if (watchSource) ({ !unchanged(findNow(rel, l), l) }) else null,
            targetUnchanged = if (checkTarget) ({ unchanged(s.stat(path), expectedRemote) }) else null,
        )
        val rs = s.stat(path) ?: throw IOException(str(R.string.eng_err_after_upload_missing))
        if (rs.size != l.size) throw IOException(str(R.string.eng_err_after_upload_size))
        record(key, l, rs)
        up.incrementAndGet(); bytes.addAndGet(l.size)
        log("I", "↑ $rel")
    }

    private suspend fun download(
        s: SmbSession, key: String, rel: String, r: RemoteEntry, expectNewAfterOwnTrash: Boolean = false, onBytes: (Long) -> Unit,
    ) {
        val ctx = currentCoroutineContext()
        // Abgebrochene Downloads setzen an der Abbruchstelle fort (angefangene .s2ptmp auf dem Handy)
        // Gab es die Datei beim Einlesen nicht, ist sie inzwischen aber auf dem Handy entstanden -> nicht überschreiben
        // (Wiederholung nach eigenem Verschieben in den Papierkorb: die Datei fehlt jetzt, es darf nichts Neues dort liegen)
        // Hat dieser Lauf die Handy-Datei gerade selbst in den Papierkorb verschoben (auch kurz zuvor in executeTransfer), gibt es sie nicht mehr
        val expectedLocal = if (expectNewAfterOwnTrash || key in trashedByRun) null else local[key]?.second
        // Fortsetzen nur mit gespeicherter Identität der PC-Datei vom Neubeginn (R-T1/R-I6, -333), kein Uhrenvergleich
        val idKey = TransferIntegrity.identityKey(profile.id, rel)
        val current = TransferIntegrity.Stamp(r.size, r.modified)
        val path = remotePath(s, rel)
        val (docId, resumedAt) = try {
            localTouched = true; tree.writeFileResumable(
                rel, r.size, expectedLocal,
                mayResume = { tmpSize -> TransferIntegrity.mayResume(tmpSize, identities.get(idKey), current) },
                onFresh = { if (!identities.put(idKey, current)) throw IOException(str(R.string.eng_err_identity_store)) },
                verify = { tmpId, resumed ->
                    !TransferIntegrity.needsHashCheck(r.size, resumed) ||
                        PartialHash.compute(r.size) { off, len -> tree.readAt(tmpId, off, len) } == s.partialHash(path, r.size)
                },
            ) { out, offset ->
                s.download(path, out, onBytes, { ctx.ensureActive() }, startOffset = offset, expectedSize = r.size)
            }
        } catch (e: ChangedDuringRunException) {
            return skipChanged(rel, Msg.of(R.string.eng_on_phone))
        }
        identities.remove(idKey)
        // Stand so lesen wie beim nächsten Einlesen (Ordnerliste statt Einzelabfrage): Manche Speicherorte (z. B.
        // "Downloads") liefern sonst andere Zeitstempel -> der nächste Lauf hielte die Datei für geändert (0.5.1)
        val ls = (try { tree.find(rel) } catch (e: Exception) { null }) ?: tree.stat(docId)
            ?: throw IOException(str(R.string.eng_err_after_download_missing))
        if (ls.size != r.size) throw IOException(str(R.string.eng_err_after_download_size))
        record(key, ls, r)
        down.incrementAndGet(); bytes.addAndGet(r.size)
        log("I", if (resumedAt > 0) Msg.join("", listOf(Msg.lit("↓ $rel "),
            Msg.of(R.string.eng_resumed_at, com.sync2pc.app.sync.Notifications.formatBytes(resumedAt)))) else Msg.lit("↓ $rel"))
    }

    /**
     * Vorgemerkte Wiederherstellungen ausführen (0.5.0): aktuelle Version auf beiden Seiten in den Papierkorb,
     * alte Version an den ursprünglichen Ort zurück und auf die andere Seite kopieren, Sync-Stand setzen.
     * Netzwerkfehler -> Vormerkung bleibt, der Lauf wird wie üblich unterbrochen. Liefert true, wenn etwas geschah.
     */
    private suspend fun runRestores(s: SmbSession): Boolean {
        val failures = RestoreFailures(context)
        // Ablauf, Zurückrollen und Fehlerbehandlung: RestoreExecutor (SYNC-20261111-163 Stufe 2)
        return RestoreExecutor(tree, s, object : RestoreHost {
            override val today: String get() = this@MirrorEngine.today
            override suspend fun pendingRestores() = dao.pendingRestores(profile.id)
            override suspend fun mirrorStates() = dao.mirrorStates(profile.id)
            override suspend fun deleteMirrorState(key: String) = dao.deleteMirrorState(profile.id, key)
            override suspend fun upsertMirrorState(state: MirrorState) = dao.upsertMirrorState(state)
            override suspend fun deleteConflict(relPath: String) = dao.deleteConflict(profile.id, relPath)
            override suspend fun deleteRestore(id: Long) = dao.deleteRestore(id)
            override fun isExhausted(restoreId: Long) = failures.isExhausted(restoreId)
            override fun markFailure(restoreId: Long) = failures.mark(profile.id, restoreId)
            override fun clearFailure(restoreId: Long) = failures.clear(restoreId)
            override suspend fun log(level: String, msg: Msg) = this@MirrorEngine.log(level, msg)
            override fun warn(what: String, e: Exception) { android.util.Log.w("MirrorEngine", what, e) }
            override fun countTrashed() { trashed.incrementAndGet() }
            override fun countFailed() { failed.incrementAndGet() }
            override fun markConnectionFailed() { connectionFailed = true }
            override fun trashRemote(rel: String) = this@MirrorEngine.trashRemote(s, rel)
            override suspend fun upload(key: String, rel: String, local: LocalEntry) = this@MirrorEngine.upload(s, key, rel, local, watchSource = false) {}
            override suspend fun download(key: String, rel: String, remote: RemoteEntry) = this@MirrorEngine.download(s, key, rel, remote, expectNewAfterOwnTrash = true) {} // die aktuelle Handy-Version ging eben in den Papierkorb
        }).run()
    }

    /** Vorgemerktes Leeren des PC-Papierkorbs (PC war beim Auslösen nicht erreichbar) */
    private suspend fun runPendingPurges(s: SmbSession) {
        val store = PurgeStore(context)
        val rules = store.rules(profile.id)
        val files = store.files(profile.id)
        if (rules.isEmpty() && files.isEmpty()) return
        // Noch vorgemerkte Wiederherstellungen (z. B. fehlgeschlagene) nie mitlöschen (0.7.0)
        val keep = TrashPurge.reservedPaths(dao.pendingRestores(profile.id), Side.REMOTE)
        try {
            var dirs = 0
            for (r in rules) dirs += TrashCleaner.purgeRemote(s, r, keep)
            var n = 0
            for (f in files) if (f.lowercase() !in keep && TrashCleaner.deleteRemoteFile(s, f)) n++
            store.clear(profile.id)
            log("I", Msg.of(R.string.eng_purged_pc, dirs, n))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (isNetworkError(e)) { connectionFailed = true; throw e } // bleibt vorgemerkt
            store.clear(profile.id)
            log("E", Msg.of(R.string.eng_purge_pc_failed, com.sync2pc.app.sync.shortError(e)))
        }
    }

    /** Datei wurde seit dem Einlesen geändert -> nicht überschreiben; der nächste Lauf sieht beide Stände */
    private suspend fun skipChanged(rel: String, where: Msg) {
        deferred.incrementAndGet()
        log("W", Msg.of(R.string.eng_changed_during_run, rel, where))
    }

    /** Grund des Rückfalls auf ".LeoSync-Bin" auf dem PC (erster Fehler); wird am Ende des Laufs einmal als Warnung protokolliert */
    private val pcTrashFallback = java.util.concurrent.atomic.AtomicReference<Exception?>(null)
    /** Merker «der PC lehnt @LeoSync-Bin ab» (Review -244 M1): dann gleich der Ausweichname, keine wiederholten Warnungen */
    private val trashNames = PcTrashNameMemory(context)
    private val pcRejectsNewTrashName = trashNames.isRejected(profile)
    private val pcTrash = PcTrashMover(startWithFallback = pcRejectsNewTrashName) { e ->
        pcTrashFallback.compareAndSet(null, e); trashNames.remember(profile)
    }

    /**
     * PC-Datei nach "<Basis>\@LeoSync-Bin\<Datum>\<Pfad>" verschieben; gleicher Name dort -> Uhrzeit anhängen
     * ("a (14-32-05).txt", 0.7.0); Rückfall auf ".LeoSync-Bin" siehe [PcTrashMover]. Liefert den vollen Pfad im Papierkorb.
     */
    private fun trashRemote(s: SmbSession, rel: String): String {
        val full = pcTrash.trash(s, rel, today)
        trashed.incrementAndGet()
        return full
    }

    /**
     * Papierkorb-Übernahme ".LeoSync-Bin" -> "@LeoSync-Bin" einer Seite (SYNC-20261111-244, Stufe B). Fehler sind nie ein
     * Grund, den Lauf abzubrechen (nur Warnung); auf dem PC gehen Netzwerkfehler wie im restlichen Lauf nach aussen.
     */
    private suspend fun migrateTrash(side: TrashSide, phone: Boolean) {
        val ctx = currentCoroutineContext()
        val r = TrashMigration.run(side, check = { ctx.ensureActive() }, propagateNetworkErrors = !phone)
        when (r.status) {
            TrashMigration.Status.MIGRATED ->
                log("I", Msg.of(if (phone) R.string.eng_trash_migrated_phone else R.string.eng_trash_migrated_pc, TRASH_DIR))
            TrashMigration.Status.FAILED -> {
                val err = r.error ?: IOException("?")
                if (!phone && PcTrashMover.nameInvalid(err)) {
                    // Der PC lehnt den Namen ab: merken, ab sofort Ausweichname, eine einzige Warnung am Laufende (Review -244 M1)
                    trashNames.remember(profile); pcTrash.useFallback(); pcTrashFallback.compareAndSet(null, err)
                } else log("W", Msg.of(if (phone) R.string.eng_trash_migrate_failed_phone else R.string.eng_trash_migrate_failed_pc,
                    TRASH_DIR, com.sync2pc.app.sync.shortError(err)))
            }
            TrashMigration.Status.UNSUPPORTED, TrashMigration.Status.INCOMPLETE ->
                // Kein Fehler: der Altname bleibt les- und wiederherstellbar, der nächste Lauf versucht es erneut
                android.util.Log.i("MirrorEngine", "Papierkorb-Übernahme ${if (phone) "Handy" else "PC"}: ${r.status}, ${r.moved} verschoben")
            TrashMigration.Status.NOTHING -> {}
        }
    }

    private fun countDone(sh: Shared, size: Long) {
        sh.bytesDone.addAndGet(size)
        val done = sh.done.incrementAndGet()
        emitShared(sh, null, force = done == sh.totalFiles)
    }

    private fun emitShared(sh: Shared, file: String?, force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && now - sh.lastEmit < 700) return
        sh.lastEmit = now
        val b = (sh.bytesDone.get() + sh.inflight.values.sum()).coerceAtMost(sh.totalBytes)
        emit(str(R.string.eng_syncing), sh.done.get(), sh.totalFiles, b, sh.totalBytes, file)
    }

    /** Abgelaufene Datumsordner im Papierkorb auf beiden Seiten endgültig löschen */
    private suspend fun purgeTrash() {
        val days = profile.trashRetentionDays
        if (days <= 0) return
        val rule = PurgeRule(PurgeRule.Mode.OLDER, before = TrashPolicy.cutoff(days))
        // Vorgemerkte Wiederherstellungen bleiben auch nach Ablauf der Frist liegen (0.7.0)
        val pending = dao.pendingRestores(profile.id)
        var n = 0
        try {
            localTouched = true; n += tree.purgeTrash(rule, TrashPurge.reservedPaths(pending, Side.LOCAL))
        } catch (e: Exception) {
            log("W", Msg.of(R.string.eng_purge_local_failed, com.sync2pc.app.sync.shortError(e)))
        }
        val s = smb
        if (s != null) {
            try {
                n += TrashCleaner.purgeRemote(s, rule, TrashPurge.reservedPaths(pending, Side.REMOTE))
            } catch (e: Exception) {
                if (isNetworkError(e)) throw e
                log("W", Msg.of(R.string.eng_purge_remote_failed, com.sync2pc.app.sync.shortError(e)))
            }
        }
        if (n > 0) log("I", Msg.of(R.string.eng_purged_expired, n, days))
    }

    private suspend fun record(key: String, l: LocalEntry, r: RemoteEntry) {
        dao.upsertMirrorState(
            MirrorState(profile.id, key, l.size, l.modified, r.size, r.modified, System.currentTimeMillis())
        )
    }

    private fun previewOf(a: Action) {
        val label = when (a.kind) {
            Kind.UPLOAD -> str(if (a.trashOther) R.string.eng_pv_upload_trash else R.string.eng_pv_upload)
            Kind.DOWNLOAD -> str(if (a.trashOther) R.string.eng_pv_download_trash else R.string.eng_pv_download)
            Kind.TRASH_LOCAL -> str(R.string.eng_pv_trash_local)
            Kind.TRASH_REMOTE -> str(R.string.eng_pv_trash_remote)
            Kind.CONFLICT -> str(R.string.eng_pv_conflict)
            Kind.KEEP_BOTH -> str(R.string.eng_pv_keep_both)
            Kind.RECORD, Kind.DROP -> return
        }
        preview += PreviewEntry(label, a.rel, a.kind == Kind.CONFLICT)
    }

    private fun freeSpace(): Long = try {
        @Suppress("DEPRECATION")
        StatFs(Environment.getExternalStorageDirectory().path).availableBytes
    } catch (e: Exception) { Long.MAX_VALUE / 2 }

    private suspend fun finish(status: String, message: Msg): SyncRun {
        pcTrashFallback.getAndSet(null)?.let {
            log("W", Msg.of(R.string.eng_trash_pc_fallback, TRASH_DIR, DOT_TRASH_DIR, com.sync2pc.app.sync.shortError(it)))
        }
        val text = message.text()
        run = run.copy(
            finishedAt = System.currentTimeMillis(), status = status, message = text, messageCode = message.toCode(),
            uploaded = up.get() + down.get(), skipped = 0, failed = failed.get(), waiting = newConflicts, bytes = bytes.get(), deferred = deferred.get()
        )
        dao.updateRun(run)
        // SYNC-20261111-213: Datei während des Laufs neu entstanden/gelöscht -> Folgelauf (billiger Ordner-Stempelvergleich)
        if (!dryRun && status != RunStatus.FAILED && ::tree.isInitialized) {
            changedDuringRun = withContext(Dispatchers.IO) { tree.changedSinceListing(localTouched) }
        }
        if (!dryRun) log("I", Msg.of(R.string.eng_end, message))
        return run
    }

    private fun emit(phase: String, done: Int, total: Int, doneBytes: Long, totalBytes: Long, file: String?) =
        onProgress(SyncProgress(profile.name, phase, done, total, doneBytes, totalBytes, file))

    private fun isNetworkError(e: Throwable): Boolean = com.sync2pc.app.sync.isNetworkError(e)
}
