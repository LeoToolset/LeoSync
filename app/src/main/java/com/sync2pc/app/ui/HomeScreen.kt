package com.sync2pc.app.ui

import com.sync2pc.app.data.RunStatus

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.alpha
import android.widget.Toast
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.sync2pc.app.BuildConfig
import com.sync2pc.app.R
import com.sync2pc.app.l10n.Texts
import com.sync2pc.app.l10n.plural
import com.sync2pc.app.l10n.str
import com.sync2pc.app.data.AppDatabase
import com.sync2pc.app.data.GlobalSettings
import com.sync2pc.app.data.Profile
import com.sync2pc.app.data.ProfileType
import com.sync2pc.app.data.SyncRun
import com.sync2pc.app.mirror.Guard
import com.sync2pc.app.mirror.GuardKind
import com.sync2pc.app.mirror.GuardStore
import com.sync2pc.app.mirror.MirrorGuard
import com.sync2pc.app.mirror.PurgeStore
import com.sync2pc.app.mirror.RestoreFailures
import com.sync2pc.app.mirror.RestoreManager
import com.sync2pc.app.sync.CollectedItems
import com.sync2pc.app.sync.Notifications
import com.sync2pc.app.sync.ProblemFactory
import com.sync2pc.app.sync.ProblemGrouping
import com.sync2pc.app.sync.ProblemGroup
import com.sync2pc.app.sync.ProblemKind
import com.sync2pc.app.sync.ProblemStore
import com.sync2pc.app.sync.SyncProgress
import com.sync2pc.app.sync.StartResult
import com.sync2pc.app.sync.SyncScheduler
import com.sync2pc.app.sync.SyncWorker
import com.sync2pc.app.sync.WifiHelper
import kotlinx.coroutines.flow.combine

/** Erhöht sich bei jedem onResume -> Berechtigungen/WLAN neu einlesen */
@Composable
fun rememberResumeTick(): Int {
    var tick by remember { mutableIntStateOf(0) }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val obs = LifecycleEventObserver { _, e -> if (e == Lifecycle.Event.ON_RESUME) tick++ }
        owner.lifecycle.addObserver(obs)
        onDispose { owner.lifecycle.removeObserver(obs) }
    }
    return tick
}

/**
 * Erhöht sich, wenn das Standardnetz wechselt oder wegfällt (WLAN an/aus, anderes WLAN, mobile Daten), damit die Zeile
 * «WLAN: …» nicht veraltet stehen bleibt (SYNC-20261111-188 B5). Nur Verfügbarkeit, nicht jede Änderung der Signalstärke.
 */
@Composable
fun rememberNetworkTick(): Int {
    val ctx = LocalContext.current
    var tick by remember { mutableIntStateOf(0) }
    DisposableEffect(ctx) {
        val cm = ctx.getSystemService(android.net.ConnectivityManager::class.java)
        val cb = object : android.net.ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) { tick++ }
            override fun onLost(network: android.net.Network) { tick++ }
        }
        try { cm?.registerDefaultNetworkCallback(cb) } catch (e: Exception) { /* Anzeige bleibt wie bisher (nur beim Zurückkehren) */ }
        onDispose { try { cm?.unregisterNetworkCallback(cb) } catch (e: Exception) { /* nicht registriert */ } }
    }
    return tick
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigate: (Screen) -> Unit,
    onConfirmDeletes: () -> Unit,
    /** Snackbar der Startseite; von aussen übergeben, damit der Löschablauf (MainActivity) darin melden kann (R12) */
    snackbar: SnackbarHostState = remember { SnackbarHostState() },
) {
    val ctx = LocalContext.current
    val dao = remember { AppDatabase.get(ctx).dao() }
    val tick = rememberResumeTick()
    // B5: Netz auch bei Netzwechsel neu lesen (nicht nur beim Zurückkehren in die App)
    val netTick = rememberNetworkTick()
    val perms = remember(tick) { PermStatus.read(ctx) }

    val allProfiles by remember { dao.profilesFlow() }.collectAsState(initial = emptyList())
    // 0.11.0: ein Profil, das gerade gelöscht wird (Rückgängig noch möglich), ist ausgeblendet
    val pendingDelete by PendingProfileDelete.pending.collectAsState()
    val profiles = allProfiles.filter { it.id != pendingDelete?.id }
    LaunchedEffect(pendingDelete) {
        val pd = pendingDelete ?: return@LaunchedEffect
        val r = snackbar.showSnackbar(
            str(R.string.home_profile_deleted, pd.name), actionLabel = str(R.string.home_undo),
            duration = SnackbarDuration.Long,
        )
        if (r == SnackbarResult.ActionPerformed) PendingProfileDelete.undo() else PendingProfileDelete.commit(ctx)
    }
    val decisions by remember { dao.decisionsFlow() }.collectAsState(initial = emptyList())
    // 0.11.1: Konflikte eines Profils in der Rückgängig-Frist nicht anzeigen (bleiben gespeichert, kommen bei „Rückgängig“ zurück)
    val allConflicts by remember { dao.openConflictsFlow() }.collectAsState(initial = emptyList())
    val conflicts = allConflicts.filter { it.profileId != pendingDelete?.id }
    val deleteCount by remember { dao.queuedDeleteCount() }.collectAsState(initial = 0)
    val lastRun by remember { dao.lastRunFlow() }.collectAsState(initial = null)
    val lastSuccess by remember { dao.lastCleanRunFlow() }.collectAsState(initial = null)
    // -173/-177: Dateien, die beim nächsten Sync folgen (jeweils letzter echter Lauf je aktivem Profil)
    val pendingFiles by remember { dao.pendingDeferredFlow() }.collectAsState(initial = 0)
    val pendingRestores by remember { dao.pendingRestoreCount() }.collectAsState(initial = 0)

    val wm = remember { WorkManager.getInstance(ctx) }
    val workFlow = remember {
        combine(SyncScheduler.ALL_SYNC_WORK.map { wm.getWorkInfosForUniqueWorkFlow(it) }) { arr -> arr.flatMap { it } }
    }
    val works by workFlow.collectAsState(initial = emptyList())
    // 0.9.5: Während einer Synchronisierung kommen laufend Fortschritts-Updates. Einstellungen werden nur neu
    // gelesen, wenn sich der Zustand eines Auftrags ändert (workKey), nicht bei jedem Fortschritt.
    val workKey = remember(works) { works.map { Triple(it.id, it.state, it.runAttemptCount) } }
    val settings = remember { GlobalSettings(ctx) }
    val running = works.firstOrNull { it.state == WorkInfo.State.RUNNING }
    // Eingeplant heisst nicht immer "wartet": Nachhol-Sync und erneute PC-Prüfung laufen absichtlich später.
    // Fällig (Startzeit erreicht, aber Bedingungen fehlen) vs. zeitversetzt (Startzeit in der Zukunft)
    val now = System.currentTimeMillis()
    val enqueued = works.filter { it.state == WorkInfo.State.ENQUEUED }
    val queued = enqueued.any { it.nextScheduleTimeMillis <= now + 5_000 }
    val nextPlanned = enqueued.map { it.nextScheduleTimeMillis }.filter { it in (now + 5_001) until Long.MAX_VALUE }.minOrNull()
    // Warum wartet ein fälliger Auftrag? (WLAN fehlt / nicht am Ladegerät / startet gleich)
    val waitText = remember(tick, netTick, workKey, profiles) {
        val s = settings
        val onTarget = WifiHelper.onAllowedNetwork(ctx)
        val charging = ctx.getSystemService(android.os.BatteryManager::class.java)?.isCharging == true
        when {
            !onTarget -> str(R.string.home_wait_wifi)
            s.onlyWhileCharging && !charging -> str(R.string.home_wait_charger)
            else -> str(R.string.home_starting)
        }
    }
    val progress = running?.let { SyncWorker.progressFrom(it.progress) }
    // Restzeit (0.11.0): Tempo seit Beginn dieses Laufs. Erst bei belastbarer Rate (etaSeconds, 063), sonst «wird berechnet»
    val runStart = remember(running?.id) { System.currentTimeMillis() to (progress?.doneBytes ?: 0L) }
    val etaText = progress?.let { pr ->
        if (pr.totalBytes <= pr.doneBytes) null
        else {
            val elapsed = System.currentTimeMillis() - runStart.first
            etaSeconds(elapsed, pr.doneBytes - runStart.second, pr.totalBytes - runStart.second)
                ?.let { formatEta(it) } ?: str(R.string.home_eta_calculating)
        }
    }
    // Grund, falls der letzte automatische Start nicht synchronisiert hat (max. 24 h alt)
    val skipInfo = remember(tick, workKey) {
        val s = settings
        if (s.lastSkipInfo.isNotBlank() && System.currentTimeMillis() - s.lastSkipAt < 24 * 3600_000L)
            "${formatClock(s.lastSkipAt)}: ${s.lastSkipInfo}"
        else null
    }
    // B1: stammt der Grund von einem manuellen Start, heisst die Zeile nicht «Automatischer Start übersprungen»
    val skipManual = remember(tick, workKey) { settings.lastSkipManual }
    // Durch WLAN-Unterbruch pausiert?
    val paused = remember(tick, workKey) { settings.resumeOnWifi }
    // Vom Benutzer pausiert (Symbol oben rechts)? pauseVersion erzwingt das Neulesen nach Aktionen
    var pauseVersion by remember { mutableIntStateOf(0) }
    // Long.MAX_VALUE = "für immer" (nur manuelle Syncs)
    val userPausedUntil = remember(tick, workKey, pauseVersion) {
        val s = settings
        if (s.manualOnly) Long.MAX_VALUE else s.pausedUntil.takeIf { it > System.currentTimeMillis() } ?: 0L
    }
    LaunchedEffect(userPausedUntil) {
        // Pause läuft ab, während die Startseite offen ist -> Anzeige aktualisieren
        if (userPausedUntil in 1 until Long.MAX_VALUE) { delay(userPausedUntil - System.currentTimeMillis() + 500); pauseVersion++ }
    }
    var showPauseDialog by remember { mutableStateOf(false) }
    var showChargeWarning by remember { mutableStateOf(false) }
    var settingsMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val awaitingStart = remember(tick, workKey) { settings.awaitingFirstStart() }
    // Schutzbremse (0.4.1): offene Entscheidungen der Ordner-Profile; guardVersion = nach einer Antwort neu lesen
    var guardVersion by remember { mutableIntStateOf(0) }
    val guards = remember(tick, workKey, guardVersion, profiles) {
        val ids = profiles.map { it.id }.toSet()
        GuardStore(ctx).all().filter { it.profileId in ids }
    }
    LaunchedEffect(guards.isEmpty()) { if (guards.isEmpty()) Notifications.cancelGuard(ctx) }
    // SYNC-20261115-352: Profile, deren Verschlüsselung abgelehnt wurde (Stufe «Erforderlich»): eine Karte je Profil
    val encRejected = remember(tick, workKey, guardVersion, profiles) {
        profiles.filter { it.enabled && com.sync2pc.app.smb.EncryptionAuto.isRejected(ctx, it) }
    }
    // SYNC-20261115-353: Zeilen der Import-Checkliste (importierte Ordner-Profile, die noch nie erfolgreich liefen)
    val importRows = remember(tick, workKey, guardVersion, profiles, awaitingStart) {
        profiles.filter { it.enabled && it.id in awaitingStart && it.type() == com.sync2pc.app.data.ProfileType.MIRROR }.map { p ->
            val lost = p.localTreeUri.isBlank() ||
                !com.sync2pc.app.mirror.LocalTree.hasPermission(ctx, android.net.Uri.parse(p.localTreeUri))
            val known = com.sync2pc.app.sync.PcIdentity.storedFor(
                p.pcGuid, p.pcGuidHost, com.sync2pc.app.smb.SmbTarget.hostOf(p.smbPath).orEmpty()) != null
            ChecklistRow(p.id, p.name.trim(), nextChecklistStep(lost, known))
        }
    }
    // Höchstens drei Schutzkarten sichtbar, der Rest als «n weitere»
    val signRejected = remember(tick, workKey, guardVersion, profiles) {
        profiles.filter { it.enabled && com.sync2pc.app.smb.SigningStatus.isRejected(ctx, it) }
    }
    val guardsShown = guards.take(MAX_PROTECT_CARDS)
    val encShown = encRejected.take((MAX_PROTECT_CARDS - guardsShown.size).coerceAtLeast(0))
    val signShown = signRejected.take((MAX_PROTECT_CARDS - guardsShown.size - encShown.size).coerceAtLeast(0))
    val protectHidden = guards.size + encRejected.size + signRejected.size - guardsShown.size - encShown.size - signShown.size
    // Papierkorb (0.6.0): zuletzt gemessene Grösse und vorgemerktes Leeren auf dem PC
    val trashSizeText = remember(tick, workKey, profiles) {
        val store = PurgeStore(ctx)
        val sizes = trashProfiles(profiles).map { store.sizes(it.id) }
        val local = sizes.map { it.first }.filter { it >= 0 }
        val remote = sizes.map { it.second }.filter { it >= 0 }
        str(R.string.home_tool_trash_text) +
            if (local.isEmpty() && remote.isEmpty()) ""
            else "\n" + str(R.string.home_tool_trash_used,
                if (local.isEmpty()) "?" else Notifications.formatBytes(local.sum()),
                if (remote.isEmpty()) "?" else Notifications.formatBytes(remote.sum()))
    }
    val purgeProfilesPending = remember(tick, workKey, profiles) {
        val ids = profiles.map { it.id }.toSet()
        PurgeStore(ctx).pendingProfiles().filter { it in ids }.toSet()
    }
    val pendingPurges = purgeProfilesPending.size
    // 0.7.0: Wiederherstellungen pro Profil, fehlgeschlagene (zurückgerollt) und Wartende hinter der Schutzbremse
    val restoreProfilesPending by remember { dao.pendingRestoreProfiles() }.collectAsState(initial = emptyList())
    var restoreFailVersion by remember { mutableIntStateOf(0) }
    val failedRestores = remember(tick, workKey, profiles, pendingRestores, restoreFailVersion) {
        RestoreFailures(ctx).restoreIds(profiles.map { it.id }).size
    }
    // 0.11.8: davon die, die nach 5 Fehlversuchen nicht mehr automatisch wiederholt werden
    val exhaustedRestores = remember(tick, workKey, profiles, pendingRestores, restoreFailVersion) {
        RestoreFailures(ctx).exhaustedCount(profiles.map { it.id })
    }
    val guardedIds = guards.map { it.profileId }.toSet()
    /** Zusatz, wenn ein Teil der vorgemerkten Arbeit hinter der Schutzbremse wartet */
    fun guardNote(pendingIds: Collection<Long>): String? = when {
        pendingIds.isEmpty() || pendingIds.none { it in guardedIds } -> null
        pendingIds.all { it in guardedIds } -> str(R.string.home_guard_note_all)
        else -> str(R.string.home_guard_note_some)
    }
    // PC im Ziel-WLAN nicht erreichbar -> Hinweis mit Zeitpunkt der nächsten Prüfung
    val shareWait = remember(tick, workKey) {
        val s = settings
        if (s.shareRetryAt > System.currentTimeMillis()) {
            // R7: Nennt die Zeile «Nächste Prüfung um …» (nextPlanned) den Zeitpunkt schon, steht er nicht doppelt im Fehlertext
            if (nextPlanned != null) str(R.string.home_pc_unreachable_short, s.shareUnreachable)
            else str(R.string.home_pc_unreachable, s.shareUnreachable, formatClock(s.shareRetryAt))
        } else null
    }

    // Einmaliger Tipp (0.11.0); weg nach dem ersten Herunterziehen oder nach Antippen
    var pullTipDone by remember { mutableStateOf(settings.pullTipDone) }
    fun finishPullTip() { if (!pullTipDone) { settings.pullTipDone = true; pullTipDone = true } }
    // SYNC-20261115-409: Meldungen je Ursache (gespeicherte Einträge plus abgeleitete aus den bestehenden Quellen), gruppiert und sortiert
    val needs = PermNeeds.of(profiles)
    val storedProblems = remember(tick, workKey, guardVersion, profiles) { ProblemStore(ctx).all() }
    val phoneLost = remember(tick, workKey, guardVersion, profiles) {
        profiles.filter { it.enabled && it.type() == com.sync2pc.app.data.ProfileType.MIRROR && it.localTreeUri.isNotBlank() &&
            !com.sync2pc.app.mirror.LocalTree.hasPermission(ctx, android.net.Uri.parse(it.localTreeUri)) }
    }
    val restoreFailedIds = remember(tick, workKey, profiles, pendingRestores, restoreFailVersion) {
        // SYNC-20261115-420: Profil-IDs (nicht Wiederherstellungs-IDs), sonst fand profileById sie nur bei zufälliger Gleichheit
        RestoreFailures(ctx).failedProfileIds(profiles.map { it.id })
    }
    val activeNames = profiles.filter { it.enabled }.associate { it.id to it.name.trim() }
    val profileById = profiles.associateBy { it.id }
    val lastOk by produceState(emptyMap<Long, Long?>(), profiles, workKey) {
        value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { profiles.associate { it.id to dao.lastSuccessAt(it.id) } }
    }
    val derivedProblems = buildList {
        for (g in guards) profileById[g.profileId]?.let { add(ProblemFactory.of(it, ProblemKind.GUARD, g.kind.name)) }
        for (p in encRejected) add(ProblemFactory.of(p, ProblemKind.ENC_REFUSED))
        for (p in signRejected) add(ProblemFactory.of(p, ProblemKind.SIGN_REFUSED))
        for (p in phoneLost) add(ProblemFactory.of(p, ProblemKind.PHONE_FOLDER_LOST))
        for (id in restoreFailedIds) profileById[id]?.let { add(ProblemFactory.of(it, ProblemKind.RESTORE_FAILED)) }
    }
    val pcLabels = profiles.associate { p ->
        val host = com.sync2pc.app.smb.SmbTarget.hostOf(p.smbPath).orEmpty()
        ProblemGrouping.pcKeyOf(p.pcGuid, host) to host
    }
    val groups = ProblemGrouping.group(
        storedProblems + derivedProblems, activeNames, lastOk,
        CollectedItems(conflicts = conflicts.size, monthFolders = decisions.size, deleteConfirm = deleteCount), System.currentTimeMillis(), pcLabels,
    )
    val permMissing = perms.missing(needs)
    // Profilkarten: höchste Stufe 1 bis 3 je Profil als rote Textzeile (SYNC-20261115-416 F)
    val profileProblem = groups.filter { it.level <= 3 }.flatMap { g -> g.profiles.map { it.id to g } }
        .groupBy({ it.first }, { it.second }).mapValues { (_, gs) -> gs.minByOrNull { it.level }!!.kind }
    // SYNC-20261115-416 A: Fehler = Stufen 1 bis 3 (Konflikte nicht mehr), Wartet auf dich = Stufe 4, Meldungen = Stufe 5
    val errorCount = groups.count { it.kind.isError } + (if (permMissing) 1 else 0)
    val waitingCount = groups.count { it.level == 4 }
    val messageCount = groups.count { it.level == 5 }
    val batteryTip = BatteryTip.visible(ctx, perms, needs)
    val pages = remember(groups, batteryTip, pullTipDone, profiles.isNotEmpty(), importRows) {
        groups.map<ProblemGroup, HomePage> { HomePage.Problem(it) } +
            listOfNotNull(
                HomePage.Checklist(importRows).takeIf { importRows.isNotEmpty() },
                HomePage.BatteryTip.takeIf { batteryTip },
                HomePage.PullTip.takeIf { !pullTipDone && profiles.isNotEmpty() },
            )
    }

    // Gesamtzustand für die Statuskarte (SYNC-20261110-113): Verbindung, Profile, offene Entscheidungen
    val activeProfiles = profiles.filter { it.enabled && it.isConfigured() }
    val onAllowedNet = remember(tick, netTick, workKey) { WifiHelper.onAllowedNetwork(ctx) }
    val noConnection = HomeStatus.noConnection(
        activeProfiles.size,
        anyCanRunNow = activeProfiles.any { HomeStatus.profileCanRunNow(onAllowedNet) },
        pcUnreachable = shareWait != null,
    )
    // Offene Entscheidungen: Konflikte, Ordnerzuordnung, Schutzbremse, Löschbestätigung
    val attentionCount = groups.size
    val lastRunErrors = lastRun?.takeIf { it.status == RunStatus.FAILED || it.failed > 0 }?.failed ?: -1
    val headline = HomeStatus.headline(HomeStatusInput(
        isRunning = running != null,
        hasProfiles = profiles.isNotEmpty(),
        hasActiveProfile = activeProfiles.isNotEmpty(),
        attentionCount = waitingCount,
        errorCount = errorCount,
        messageCount = messageCount,
        noConnection = noConnection,
        userPaused = userPausedUntil > 0,
        wifiPaused = paused,
        lastRunHadErrors = false, // Fehler je Profil kommen über die Meldungen (ProblemStore), nicht mehr über den global letzten Lauf
        // "Alles aktuell": letzter Lauf ohne Probleme und nichts mehr offen
        allUpToDate = lastRun?.status == RunStatus.OK && !queued && skipInfo == null &&
            groups.isEmpty() && pendingFiles == 0,
        pendingFiles = pendingFiles,
        // R4: ohne Standort-Berechtigung ist der WLAN-Name nicht lesbar (automatische Synchronisierung still gestört)
        // B2: ein aktives Profil, das noch nie erfolgreich gestartet wurde, ist nicht «gesichert»
        awaitingFirstStart = activeProfiles.any { it.id in awaitingStart },
        // B1: der letzte Start wurde nicht ausgeführt (PC nicht erreichbar/erkannt, WLAN) -> nicht «Bereit»
        recentSkip = skipInfo != null,
        queuedWork = queued,
    ))
    // -199: Wartende Dateien nur im Zustand «Keine Verbindung» ermitteln (lokal, ohne Netzwerk), nach Zustandswechseln neu
    val waitingFiles by produceState(0, headline == HomeHeadline.NO_CONNECTION, workKey, activeProfiles, pendingFiles) {
        value = if (headline != HomeHeadline.NO_CONNECTION) 0 else {
            delay(800)
            maxOf(pendingFiles, kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                activeProfiles.sumOf { com.sync2pc.app.sync.PendingCheck.openFiles(ctx, it) }
            })
        }
    }
    // SYNC-20261115-416 G: Meldungen und Tipps getrennt zählen (die Kachel zählt beide)
    val tipCount = pages.size - groups.size
    // SYNC-20261116-444: fehlende Pflichtberechtigung (zählt zu «Fehler») in der Zeile nennen, sonst fehlt die Ursache der Überschrift
    val attentionText = listOfNotNull(
        plural(R.plurals.msg_setup_count, 1, 1).takeIf { permMissing },
        plural(R.plurals.msg_status_count, groups.size, groups.size).takeIf { groups.isNotEmpty() },
        plural(R.plurals.msg_tip_count, tipCount, tipCount).takeIf { tipCount > 0 },
    ).joinToString(" · ")

    // -146 Teil A: Start während eines laufenden Syncs wird nachgeholt; beim manuellen Start sofort sagen, dass er vorgemerkt ist
    val deferred by remember { SyncScheduler.deferredState(ctx) }.collectAsState()
    val deferredSnack = str(R.string.home_sync_deferred_snack)
    fun enqueueManualWithFeedback() {
        if (SyncScheduler.enqueueManual(ctx) == StartResult.DEFERRED) scope.launch {
            snackbar.currentSnackbarData?.dismiss() // mehrfaches Tippen: dieselbe Meldung erneut, nichts stapeln
            snackbar.showSnackbar(deferredSnack)
        }
    }

    /** Synchronisieren (Knopf und Herunterziehen); erste Synchronisierung ohne Ladegerät -> vorher warnen */
    val startSync: () -> Unit = {
        // A2: ohne Verbindung kurz sagen, warum es evtl. nicht klappt (der manuelle Lauf prüft den PC trotzdem selbst)
        if (noConnection) scope.launch {
            snackbar.showSnackbar(str(if (shareWait != null) R.string.home_sync_feedback_pc else R.string.home_sync_feedback_wifi))
        }
        scope.launch {
            if (FirstSync.warnNeeded(ctx, profiles)) showChargeWarning = true
            else { enqueueManualWithFeedback(); pauseVersion++ }
        }
    }
    // Herunterziehen (0.10.0): Kreis kurz zeigen, danach übernimmt die Statuskachel
    var pullRefreshing by remember { mutableStateOf(false) }
    LaunchedEffect(pullRefreshing, running != null) {
        if (pullRefreshing) { if (running == null) delay(1_500); pullRefreshing = false }
    }

    // SYNC-20261115-409/-407: Aktionen der Sammelkachel
    var pwGroup by remember { mutableStateOf<ProblemGroup?>(null) }
    var pwBusy by remember { mutableStateOf(false) }
    var pwError by remember { mutableStateOf<String?>(null) }
    var lowerAsk by remember { mutableStateOf<Pair<ProblemKind, Long>?>(null) }
    val retryingIds = works.filter { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING }
        .flatMap { SyncScheduler.retryingIds(it.tags) }.toSet()
    val restoreText = if (failedRestores > exhaustedRestores) plural(R.plurals.home_restore_failed_text, failedRestores - exhaustedRestores)
        else if (exhaustedRestores > 0) plural(R.plurals.home_restore_gaveup_text, exhaustedRestores) else ""
    val collectedTexts = mapOf(
        ProblemKind.CONFLICTS to (plural(R.plurals.home_conflicts_text, conflicts.size) to str(R.string.home_conflicts_button)),
        ProblemKind.MONTH_FOLDERS to decisions.sumOf { it.fileCount }.let { files ->
            plural(R.plurals.home_decisions_text, files, files, plural(R.plurals.home_groups, decisions.size)) to str(R.string.home_decisions_button)
        },
        ProblemKind.DELETE_CONFIRM to (plural(R.plurals.home_delete_text, deleteCount) to str(R.string.home_delete_button)),
    )
    val messageEnv = MessageEnv(
        guardFor = { id -> guards.firstOrNull { it.profileId == id } },
        collectedText = { collectedTexts[it] },
        retrying = retryingIds,
        restoreText = restoreText,
        actions = MessageActions(
            retry = { ids -> SyncScheduler.enqueueManualFor(ctx, ids); pauseVersion++ },
            openProfile = { id -> navigate(Screen.ProfileEdit(id)) },
            enterPassword = { g -> pwError = null; pwGroup = g },
            openConflicts = { navigate(Screen.Conflicts) },
            openDecisions = { navigate(Screen.Decisions) },
            confirmDeletes = onConfirmDeletes,
            lowerProtection = { kind, id -> lowerAsk = kind to id },
            restoreRetry = { RestoreManager.retryFailed(ctx, profiles.map { it.id }); restoreFailVersion++; pauseVersion++ },
            restoreDiscard = { scope.launch { RestoreManager.discardFailed(ctx, profiles.map { it.id }); restoreFailVersion++ } },
            guardChoice = { id, choice -> scope.launch { MirrorGuard.resolve(ctx, id, choice); guardVersion++ } },
            dismissBatteryTip = { BatteryTip.skip(ctx); guardVersion++ },
            dismissPullTip = { finishPullTip() },
            startAll = { SyncScheduler.enqueueManual(ctx); pauseVersion++ },
            checklistRow = { row -> navigate(Screen.ProfileEdit(row.profileId)) },
        ),
    )
    ScreenScaffold(
        title = "LeoSync",
        onBack = null,
        snackbarHost = { LeoSnackbarHost(snackbar) },
        actions = {
            // Symbol = Aktion (0.9.0): Automatik läuft -> ⏸ (tippen = pausieren), pausiert -> ▶ (tippen = fortsetzen)
            if (userPausedUntil > 0) IconButton(onClick = { SyncScheduler.resume(ctx); pauseVersion++ }) {
                Icon(Icons.Filled.PlayArrow, str(R.string.home_cd_paused))
            } else IconButton(onClick = { showPauseDialog = true }) {
                Icon(AppIcons.Pause, str(R.string.home_cd_running))
            }
            IconButton(onClick = { navigate(Screen.Stats) }) { Icon(AppIcons.BarChart, str(R.string.home_cd_stats)) }
            IconButton(onClick = { navigate(Screen.Log) }) { Icon(Icons.AutoMirrored.Filled.List, str(R.string.home_cd_log)) }
            // Zahnrad -> Menü mit den Rubriken der Einstellungen (je eine eigene Seite)
            Box {
                IconButton(onClick = { settingsMenu = true }) { Icon(Icons.Filled.Settings, str(R.string.home_cd_settings)) }
                DropdownMenu(expanded = settingsMenu, onDismissRequest = { settingsMenu = false }) {
                    SettingsSection.entries.forEach { sec ->
                        DropdownMenuItem(text = { Text(str(sec.title)) }, onClick = {
                            settingsMenu = false
                            navigate(Screen.Settings(sec))
                        })
                    }
                    // 0.11.0: Hilfe & Produktseite im Browser
                    DropdownMenuItem(text = { Text(str(R.string.settings_help)) }, onClick = {
                        settingsMenu = false
                        try {
                            ctx.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse(HELP_URL)))
                        } catch (ignored: Exception) {}
                    })
                }
            }
        },
    ) { pad ->
        // Ganz oben weiter nach unten ziehen = Synchronisieren (0.10.0), wie "Neu laden" im Browser.
        // Ohne Profil oder während einer laufenden Synchronisierung passiert nichts.
        PullToRefreshBox(
            isRefreshing = pullRefreshing,
            onRefresh = { if (profiles.isNotEmpty() && running == null) { pullRefreshing = true; finishPullTip(); startSync() } },
            modifier = Modifier.fillMaxSize().padding(pad),
        ) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // SYNC-20261115-407: Reihenfolge: Pflichtberechtigung, Statuskarte, Sammelkachel «Meldungen», Profile, Tools
            if (perms.missing(needs)) item { PermissionCard(perms, needs) }

            item {
                StatusCard(
                    isRunning = running != null,
                    isQueued = queued,
                    waitText = waitText,
                    nextPlanned = nextPlanned,
                    progress = progress,
                    lastRun = lastRun,
                    lastSuccess = lastSuccess,
                    shareWait = shareWait,
                    skipInfo = skipInfo,
                    skipManual = skipManual,
                    paused = paused,
                    userPausedUntil = userPausedUntil,
                    headline = headline,
                    attentionCount = attentionCount,
                    attentionText = attentionText,
                    lastRunErrors = lastRunErrors.coerceAtLeast(0),
                    pendingFiles = pendingFiles,
                    waitingFiles = waitingFiles,
                                onOpenPermissions = { navigate(Screen.Settings(SettingsSection.PERMISSIONS)) },
                    onSync = startSync,
                    onCancel = { SyncScheduler.cancelAll(ctx) },
                    onResume = { SyncScheduler.resume(ctx); pauseVersion++ },
                    onOpenLog = { navigate(Screen.Log) },
                    hasProfiles = profiles.isNotEmpty(),
                    // SYNC-20261115-421 (Benutzerwunsch): Hund bei grosser Schrift nicht mehr verkleinern (früher ab Schrift 1,5, -416 D)
                    compact = false,
                    pendingLines = buildList {
                        if (pendingPurges > 0) add(str(R.string.home_purge_pending_title) + (guardNote(purgeProfilesPending)?.let { " · $it" } ?: ""))
                        if (pendingRestores > failedRestores) add(plural(R.plurals.home_restore_pending_text, pendingRestores - failedRestores) + (guardNote(restoreProfilesPending)?.let { " · $it" } ?: ""))
                    },
                    etaText = etaText,
                    runHint = if (running != null && deferred.requested)
                        str(if (deferred.manual) R.string.home_next_all else R.string.home_next_auto) else null,
                )
            }
            if (pages.isNotEmpty()) item(key = "messages") { MessagesCard(pages, env = messageEnv) }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(str(R.string.home_profiles), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    TextButton(onClick = { navigate(Screen.ProfileEdit(0)) }) {
                        Icon(Icons.Filled.Add, null); Spacer(Modifier.width(4.dp)); Text(str(R.string.home_add_profile))
                    }
                }
            }
            if (profiles.isEmpty()) item {
                SectionCard {
                    Text(str(R.string.home_no_profile))
                    Button(onClick = { navigate(Screen.ProfileEdit(0)) }) { Text(str(R.string.home_first_profile)) }
                }
            }
            // SYNC-20261115-354: Umzug von der alten App (com.sync2pc.app) über Sicherung/Import
            if (profiles.isEmpty()) item(key = "move-old") {
                MoveFromOldCard(onStart = { navigate(Screen.Settings(SettingsSection.APP)) }, onFresh = { navigate(Screen.ProfileEdit(0)) })
            }
            items(profiles, key = { it.id }) { p ->
                ProfileCard(p, awaitingFirstStart = p.id in awaitingStart, problem = profileProblem[p.id]) { navigate(Screen.ProfileEdit(p.id)) }
            }
            // Tools (0.6.3): immer sichtbar; ohne passendes Profil ausgegraut, Tippen zeigt einen Hinweis
            val canFree = freeProfiles(profiles).isNotEmpty()
            val hasMirror = trashProfiles(profiles).isNotEmpty()
            val tools = listOf(
                ToolTile("free", AppIcons.Smartphone, str(R.string.home_tool_free),
                    if (canFree) str(R.string.home_tool_free_text)
                    else str(R.string.home_tool_free_disabled),
                    enabled = canFree,
                    disabledHint = str(R.string.free_hint),
                ) { navigate(Screen.FreeSpace) },
                ToolTile("trash", Icons.Filled.Delete, str(R.string.home_tool_trash),
                    if (hasMirror) trashSizeText else str(R.string.home_tool_trash_disabled),
                    enabled = hasMirror,
                    disabledHint = str(R.string.home_tool_trash_hint),
                ) { navigate(Screen.Trash) },
            )
            run {
                item { Text(str(R.string.home_tools), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 4.dp)) }
                // Ein Tool pro Zeile (0.6.1)
                items(tools, key = { "tool-" + it.id }) { t -> ToolCard(t, Modifier.fillMaxWidth()) }
            }

            item {
                Text(
                    str(R.string.home_version, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
        }
    }

    // «Passwort neu eingeben» einmal je PC und Konto: zuerst ein Profil testen, erst bei Erfolg die übrigen speichern (Kontosperre vermeiden)
    pwGroup?.let { g ->
        val pwFailed = str(R.string.msg_pw_failed)
        PasswordGroupDialog(
            group = g, busy = pwBusy, error = pwError,
            onDismiss = { pwGroup = null },
            onConfirm = { password, selectedIds ->
                pwBusy = true; pwError = null
                scope.launch {
                    val ok = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        val chosen = dao.profiles().filter { it.id in selectedIds }
                        val enc = com.sync2pc.app.data.CredentialCrypto.encrypt(password)
                        val first = chosen.firstOrNull()?.copy(smbPasswordEnc = enc) ?: return@withContext false
                        try {
                            com.sync2pc.app.smb.SmbSession.testDetailed(first, learnNetOk = false, onChangedGuid = { _, _, _ -> false })
                            for (p in chosen) dao.updateProfile(p.copy(smbPasswordEnc = enc))
                            true
                        } catch (e: Exception) { false }
                    }
                    pwBusy = false
                    if (ok) {
                        pwGroup = null
                        SyncScheduler.enqueueManualFor(ctx, selectedIds); pauseVersion++
                        snackbar.showSnackbar(str(R.string.msg_pw_done))
                    } else pwError = pwFailed
                }
            },
        )
    }
    lowerAsk?.let { (kind, id) ->
        val sign = kind == ProblemKind.SIGN_REFUSED
        AlertDialog(
            onDismissRequest = { lowerAsk = null },
            title = { DialogTitleText(str(if (sign) R.string.sign_allow_title else R.string.prot_less_title)) },
            text = { Text(str(if (sign) R.string.sign_allow_text else R.string.prot_less_text)) },
            confirmButton = {
                SafeFirstButtons(str(R.string.prot_cancel), str(if (sign) R.string.sign_allow_ok else R.string.prot_less_ok), { lowerAsk = null }) {
                    lowerAsk = null
                    scope.launch {
                        if (sign) com.sync2pc.app.smb.SigningStatus.allow(ctx, id)
                        else com.sync2pc.app.smb.EncryptionAuto.lowerToPreferred(ctx, id)
                        guardVersion++
                        SyncScheduler.enqueueManualFor(ctx, setOf(id))
                    }
                }
            },
        )
    }
    if (showChargeWarning) ChargeWarningDialog(
        onStart = { showChargeWarning = false; enqueueManualWithFeedback(); pauseVersion++ },
        onDismiss = { showChargeWarning = false },
    )

    if (showPauseDialog) PauseDialog(
        onDismiss = { showPauseDialog = false },
        onPause = { hours ->
            SyncScheduler.pause(ctx, hours)
            showPauseDialog = false
            pauseVersion++
        },
        onForever = {
            SyncScheduler.pauseForever(ctx)
            showPauseDialog = false
            pauseVersion++
        },
    )
}

