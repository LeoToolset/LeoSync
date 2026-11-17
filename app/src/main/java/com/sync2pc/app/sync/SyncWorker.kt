package com.sync2pc.app.sync

import com.sync2pc.app.data.RunStatus

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.sync2pc.app.R
import com.sync2pc.app.data.AppDatabase
import com.sync2pc.app.data.messageText
import com.sync2pc.app.l10n.Msg
import com.sync2pc.app.l10n.Texts
import com.sync2pc.app.l10n.plural
import com.sync2pc.app.l10n.str
import com.sync2pc.app.data.GlobalSettings
import com.sync2pc.app.data.Profile
import com.sync2pc.app.data.ProfileType
import com.sync2pc.app.mirror.MirrorEngine
import com.sync2pc.app.media.MediaScanner
import com.sync2pc.app.smb.PcSeen
import com.sync2pc.app.smb.SmbSession
import com.sync2pc.app.smb.SmbTarget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** Führt den Sync für alle passenden Profile aus (läuft im Hintergrund, ggf. als Vordergrunddienst). */
class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        /** Meldungen, die eine bestandene Vorprüfung (PC erreichbar, erkannt, ausgehandelt) erledigt (SYNC-20261115-409) */
        val PRECHECK_KINDS = setOf(ProblemKind.PC_UNREACHABLE, ProblemKind.PC_UNRECOGNISED, ProblemKind.NEGOTIATION)

        // Verhinderung paralleler Läufe (Auto, Manuell, Nachholen): SyncScheduler.gate (SyncGate), siehe doWork

        const val P_PROFILE = "profile"
        const val P_PHASE = "phase"
        const val P_DONE = "done"
        const val P_TOTAL = "total"
        const val P_DONE_BYTES = "doneBytes"
        const val P_TOTAL_BYTES = "totalBytes"
        const val P_FILE = "file"

        fun progressFrom(d: Data): SyncProgress? {
            val profile = d.getString(P_PROFILE) ?: return null
            return SyncProgress(
                profile, d.getString(P_PHASE) ?: "", d.getInt(P_DONE, 0), d.getInt(P_TOTAL, 0),
                d.getLong(P_DONE_BYTES, 0), d.getLong(P_TOTAL_BYTES, 0), d.getString(P_FILE)
            )
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo =
        Notifications.foregroundInfo(applicationContext, null, null)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val ctx = applicationContext
        val gate = SyncScheduler.gate(ctx)
        // R-N1: Bekommt dieser Auftrag die Sperre nicht, ist ein Lauf aktiv. Der Start wird dann nicht mehr stumm verworfen,
        // sondern atomar vorgemerkt (Merker im Speicher und in GlobalSettings); der Lauf-Inhaber reiht nach seiner Freigabe
        // genau einen Nachlauf ein. Die Oberfläche sieht es an deferred = true bzw. an SyncScheduler.deferredState.
        // SYNC-20261111-172 (6): Sonderaufträge (Fortsetzen, Wiederholung, Folgelauf) wandeln sich nicht in einen
        // Nachlauf-Merker um (der hätte weder KEY_RESUME noch KEY_RETRY): bei belegter Sperre bleibt der Auftrag mit seinen
        // Eingabedaten bei WorkManager und versucht es nach dem Backoff erneut.
        // SYNC-20261116-438: Diagnose: jeder Start und jede Abweisung durch die Sperre steht im Log (Nachweis, ob eine hängende Sperre
        // eines früheren Laufs den Fortsetzen-Start verhindert)
        android.util.Log.i("SyncWorker", "Start: resume=$isResumeRun, special=${SyncScheduler.isSpecialRun(inputData)}, sperre belegt=${gate.isRunning}")
        if (SyncScheduler.isSpecialRun(inputData)) {
            if (!gate.tryAcquire()) { android.util.Log.w("SyncWorker", "Sperre belegt: Sonderauftrag versucht es später erneut"); return@withContext Result.retry() }
        } else if (!gate.tryAcquireOrDefer(manual = inputData.getBoolean(SyncScheduler.KEY_MANUAL, false))) {
            android.util.Log.w("SyncWorker", "Sperre belegt: Start vorgemerkt (Nachlauf)")
            return@withContext Result.success(workDataOf(SyncScheduler.KEY_DEFERRED to true))
        }
        // Zwischen tryAcquireOrDefer und try darf nichts stehen: die Sperre muss in jedem Fall wieder frei werden (R-BG4)
        // -157: Scheitert work() UND der Abschluss, bleibt die Ursache aus work() erhalten (die zweite Ausnahme wird nur
        // angehängt); ein Ausdruck try/finally würde sie überschreiben.
        var workFailure: Throwable? = null
        // SYNC-20261116-425: Wird der Lauf von Android gestoppt (WLAN weg), den Unterbruch SO FRÜH WIE MÖGLICH markieren: direkt wenn die
        // Coroutine abgebrochen wird, unabhängig vom Abbau der SMB-Verbindungen im Engine-finally (der bei toter Verbindung blockieren
        // kann, bis der Prozess eingefroren oder beendet wird, bevor der Abschluss unten erreicht wird). Idempotent über [interruptMarker].
        var normalEnd = false
        val watcher = launch {
            try { awaitCancellation() } finally {
                if (!normalEnd) withContext(NonCancellable) { markInterruptedEarly() }
            }
        }
        val workResult: Result? = try { work() } catch (t: Throwable) { workFailure = t; null }
        normalEnd = true
        watcher.cancel()
        try {
            try {
                // finishRun: Aufräumen darf scheitern, die Sperre wird trotzdem freigegeben und ein vorgemerkter Nachlauf
                // eingereiht. Freigabe NACH markInterrupted (Zustand "Pausiert" gehört noch zu diesem Lauf); ein sofort
                // startender Fortsetzen-Auftrag (R-BG5) wird nicht verworfen, sondern vorgemerkt und nachgeholt.
                gate.finishRun(
                    cleanup = {
                        // Fortschrittsanzeige nie stehen lassen (sie würde sonst nach einem Abbruch "hängen")
                        try { Notifications.cancelProgress(ctx) } catch (ignored: Exception) {}
                        // SYNC-20261116-437: regulär beendet oder gestoppt (dann übernimmt der Fortsetzen-Auftrag): Sicherheitsnetz zurücknehmen
                        try { SyncScheduler.disarmRunGuard(ctx) } catch (ignored: Exception) {}
                        // Von Android gestoppt (z. B. WLAN weg -> Netzwerk-Bedingung verletzt): beim Wiederverbinden fortsetzen
                        // ("Abbrechen" durch den Benutzer ist kein Unterbruch -> nicht fortsetzen)
                        if (SyncDecisions.resumeAfterStop(isStopped, stopReasonCompat(), inputData.getBoolean(SyncScheduler.KEY_MANUAL, false)))
                            markInterrupted()
                    },
                    followUp = { kind -> SyncScheduler.enqueueDeferred(ctx, kind) }
                )
            } finally {
                try {
                    // Nur scharf stellen, wenn keiner wartet (KEEP): ein bereits wartender Auslöser behält seine Frist und wird
                    // von einem Lauf ohne neue Dateien nicht erneut um Mindestalter + 30 s nach hinten geschoben.
                    // Neue Medien verlängern die Frist von selbst (Update-Delay des Content-Triggers).
                    SyncScheduler.armContentTrigger(ctx, replace = false)
                } finally {
                    // Sicherheitsnetz: 6 Stunden nach diesem Lauf erneut prüfen (schiebt den Zeitpunkt nach hinten)
                    SyncScheduler.scheduleSafetyNet(ctx, restart = true)
                }
            }
        } catch (t: Throwable) {
            throw SyncDecisions.mergeFailures(workFailure, t)
        }
        workFailure?.let { throw it }
        workResult!!
    }

    private val isResumeRun: Boolean get() = inputData.getBoolean(SyncScheduler.KEY_RESUME, false)

    private val interruptMarker = InterruptMarker()

    /**
     * Früher Aufruf aus dem Abbruch der Coroutine: WorkManager setzt den Stoppgrund erst kurz nach dem Abbruch, deshalb bis zu 2 s auf
     * `isStopped` warten. Benutzerabbruch und manueller Lauf werden durch [SyncDecisions.resumeAfterStop] ausgeschlossen.
     */
    private suspend fun markInterruptedEarly() {
        var waited = 0L
        while (SyncDecisions.stopReasonPending(isStopped, waited, 2_000)) { delay(25); waited += 25 }
        val manual = inputData.getBoolean(SyncScheduler.KEY_MANUAL, false)
        android.util.Log.i("SyncWorker", "Abbruch: isStopped=$isStopped, Grund=${stopReasonCompat()}, manuell=$manual, gewartet=${waited}ms")
        if (SyncDecisions.resumeAfterStop(isStopped, stopReasonCompat(), manual)) markInterrupted()
    }

    /**
     * Stoppgrund (SYNC-20261109-063): `getStopReason()` ist in WorkManager 2.10 mit @RequiresApi(31) markiert, liest
     * aber nur einen intern gesetzten Wert (kein Framework-Aufruf, daher auch unter API 29/30 ohne Absturz).
     * "Abbrechen" durch den Benutzer meldet WorkManager selbst als STOP_REASON_CANCELLED_BY_APP (auf allen API-Stufen);
     * Systemstopps liefern unter API < 31 STOP_REASON_UNKNOWN. Bewusst KEINE SDK_INT-Abfrage: sie würde unter API < 31
     * einen Benutzerabbruch als Unterbruch werten (ungewolltes Fortsetzen).
     */
    @SuppressLint("NewApi")
    private fun stopReasonCompat(): Int = stopReason

    /**
     * Warum Android den Lauf gestoppt hat (für den Verlauf). null = nicht von Android gestoppt bzw.
     * "Abbrechen" durch den Benutzer – das gilt als echter Abbruch.
     */
    private fun interruptReason(): Msg? {
        val t = SyncDecisions.interruptText(isStopped, stopReasonCompat()) ?: return null
        return if (t.arg != null) Msg.of(t.res, t.arg) else Msg.of(t.res)
    }

    /**
     * Sync wurde durch WLAN-Verlust unterbrochen: Fortsetzen auf zwei Wegen sicherstellen
     *  1. WLAN-Broadcast (WifiNetworkReceiver, ohne Drosselung)
     *  2. Auftrag beim Android-Aufgabenplaner (startet die App auch, wenn sie beendet wurde)
     */
    private fun markInterrupted(newInterruption: Boolean = true) {
        // Höchstens einmal je Lauf: früher Aufruf nach dem Stopp und Abschluss lösen nie zweimal Fortsetzen-Auftrag und Dienst aus
        if (!interruptMarker.mark { doMarkInterrupted(newInterruption) }) return
    }

    private fun doMarkInterrupted(newInterruption: Boolean) {
        val ctx = applicationContext
        android.util.Log.i("SyncWorker", "Unterbruch markiert (resumeOnWifi, Fortsetzen-Auftrag, Pausiert-Anzeige)")
        GlobalSettings(ctx).resumeOnWifi = true
        SyncScheduler.enqueueResume(ctx, 0, appendToCurrent = isResumeRun)
        // "Pausiert" in der Statusleiste – als Vordergrunddienst, der die App bis zum WLAN wach hält
        // Ab Android 15: ein Fortsetzen-Lauf im falschen WLAN startet den Dienst nicht neu (PausePolicy, SYNC-20261115-389)
        // SYNC-20261116-434: Zeitlimit-Stopp bei vorhandenem Netz -> der Fortsetzen-Auftrag läuft sofort; nicht «wartet auf WLAN» zeigen
        if (!SyncDecisions.pausedWaitsForWifi(stopReasonCompat(), WifiHelper.onAllowedNetwork(ctx))) {
            Notifications.showPaused(ctx, lastProgress, targetWifi, reason = com.sync2pc.app.l10n.str(R.string.notif_resuming))
            return
        }
        val may = PausePolicy.mayStart(android.os.Build.VERSION.SDK_INT, isResumeRun, midRun = newInterruption)
        if (!may || !PauseService.start(ctx, lastProgress, targetWifi)) Notifications.showPaused(ctx, lastProgress, targetWifi)
    }

    /** Letzter Fortschritt (für die Pausiert-Anzeige) */
    @Volatile private var lastProgress: SyncProgress? = null
    /** Namen der Ziel-WLANs der betroffenen Profile */
    @Volatile private var targetWifi: String = ""

    private suspend fun work(): Result {
        val ctx = applicationContext
        val startedAt = System.currentTimeMillis()
        val manual = inputData.getBoolean(SyncScheduler.KEY_MANUAL, false)
        val trigger = SyncScheduler.triggerOf(inputData, Msg.of(R.string.trigger_auto))
        val dao = AppDatabase.get(ctx).dao()
        dao.closeDanglingRuns()

        val settings = GlobalSettings(ctx)
        val problems = ProblemStore(ctx)
        // Vom Benutzer pausiert: keine automatischen Läufe (ein manueller Start hebt die Pause vorher auf)
        if (!manual && settings.isPaused()) {
            settings.noteSkip(
                if (settings.manualOnly) str(R.string.skip_manual_only)
                else str(R.string.skip_paused_until, com.sync2pc.app.l10n.TimeFormat.dayMonth(settings.pausedUntil) + " " + com.sync2pc.app.l10n.TimeFormat.clock(settings.pausedUntil))
            )
            return Result.success()
        }
        // Neue Profile laufen erst automatisch, nachdem sie einmal von Hand gestartet wurden (Simulation zuerst möglich)
        val awaiting = settings.awaitingFirstStart()
        // Schutzbremse (0.4.1): Profile mit offener Entscheidung ruhen – auch bei "Synchronisieren"
        val blocked = com.sync2pc.app.mirror.GuardStore(ctx).blockedIds()
        val allProfiles = dao.profiles().filter {
            it.enabled && it.isConfigured() && (manual || it.id !in awaiting) && it.id !in blocked &&
                // 0.11.0: Profil wird gerade gelöscht (Rückgängig noch möglich) -> nicht synchronisieren
                it.id != com.sync2pc.app.ui.PendingProfileDelete.pending.value?.id
        }
        // SYNC-20261113-306: ein Nachhol-Auftrag bearbeitet nur die Fotoprofile mit wartenden Dateien (seine IDs); ohne IDs
        // (Auftrag aus einer früheren Version, kein Nachhol-Lauf) laufen wie bisher alle passenden Profile
        val followIds = if (manual) null else SyncScheduler.followUpIds(inputData)
        // SYNC-20261115-409: «Erneut versuchen» einer Meldungsgruppe läuft nur für deren Profile
        val onlyIds = if (manual) SyncScheduler.onlyIds(inputData) else null
        val all = (if (followIds == null) allProfiles else allProfiles.filter { it.type() == ProfileType.PHOTOS && it.id in followIds })
            .let { list -> if (onlyIds == null) list else list.filter { it.id in onlyIds } }
        if (all.isEmpty()) return Result.success()
        // Vorab rein lokal ohne Netzwerk: sind übertragungsbereite Fotos da? Wenn nicht, kein Lauf und kein Verlaufseintrag
        // (Foto-Auslöser und Nachhol-Timer laufen oft kurz nacheinander ab); junge Dateien behalten ihre Frist.
        if (followIds != null && !FollowUpPlan.shouldRun(all.map { PendingCheck.readyFiles(ctx, it, settings.minFileAgeMinutes) })) {
            replanFollowUp(all, settings.minFileAgeMinutes)
            return Result.success()
        }
        // --- Netzart (0.19.0, SYNC-20261115-351): kein WLAN-Name und kein Standort mehr; WLAN immer, mobile Daten/VPN mit Schalter und
        // ausserhalb des Roamings. Ob der PC der richtige ist, entscheidet allein die PC-Kennung (Vorab-Prüfung unten).
        // SYNC-20261116-429: Vordergrund SOFORT anfordern, nicht erst nach den Erreichbarkeitsversuchen. Das Startrecht aus dem
        // Hintergrund (Expedited-Ausnahme) gilt nur kurz; nach ~4 s lehnte Android den Start ab (FGS Disallowed, API 36).
        // Manuelle Läufe starten bei sichtbarer App und brauchen das nicht. Scheitert es, gilt wie bisher die normale Anzeige.
        if (!manual && (isResumeRun || WifiHelper.onAllowedNetwork(ctx))) {
            try {
                setForeground(Notifications.foregroundInfo(ctx, null, WorkManager.getInstance(ctx).createCancelPendingIntent(id)))
            } catch (e: kotlinx.coroutines.CancellationException) { throw e
            } catch (e: Exception) { /* später erneut versucht, sonst normale Benachrichtigung */ }
        }
        // SYNC-20261116-430: Direkt nach dem WLAN-Verbinden meldet Android das Netz als aktiv oft erst Sekundenbruchteile später
        // (Auftrag startet über die Netzwerk-Bedingung, activeNetwork ist aber noch leer). Bis zu 10 s warten, statt den Lauf
        // sofort ohne Sync zu beenden und erst in 15 Minuten wieder zu schauen.
        if (!manual && isResumeRun) {
            var waitedNet = 0L
            while (!WifiHelper.onAllowedNetwork(ctx) && waitedNet < 10_000) { delay(500); waitedNet += 500 }
        }
        val offWifi = WifiHelper.offWifiSyncActive(ctx)
        val onAllowed = WifiHelper.onAllowedNetwork(ctx)
        val eligible = if (manual || onAllowed) all else emptyList()
        // «Bekannte Netze»: Fingerabdruck des aktuellen WLANs (nur Aufwandssteuerung, kein Vertrauen); null = nicht anwendbar
        val netFp = if (!offWifi) WifiHelper.currentNetFingerprint(ctx) else null
        if (eligible.isEmpty()) {
            if (!manual && WifiHelper.skippedForRoaming(ctx)) settings.noteSkip(str(R.string.skip_roaming))
            // Fortsetzen-Auftrag, aber falsches WLAN: in 15 Minuten erneut schauen (max. 24 Stunden lang)
            if (isResumeRun && settings.resumeOnWifi) {
                if (System.currentTimeMillis() - settings.resumeSince < 24 * 3600_000L)
                    SyncScheduler.enqueueResume(ctx, 15, appendToCurrent = true)
                else { settings.resumeOnWifi = false; PauseService.stop(ctx) } // nach 24 h aufgeben
            }
            return Result.success()
        }

        // --- Ist der PC erreichbar? (kurzer TCP-Test, bevor irgendetwas Teures passiert) -----
        val isRetry = inputData.getBoolean(SyncScheduler.KEY_RETRY, false)
        if (!manual && !isRetry && settings.shareRetryAt > System.currentTimeMillis()) {
            // PC war vor Kurzem nicht erreichbar -> Wartezeit einhalten, die erneute Prüfung ist eingeplant
            return Result.success()
        }
        // Direkt nach dem WLAN-Verbinden ist das Netz oft noch nicht bereit (IP, Routing, PC-Firewall).
        // Deshalb mehrere Versuche über ca. 1 Minute, bevor "nicht erreichbar" gilt.
        // Nur mobile Daten/VPN (0.11.7): höchstens 2 Versuche (Akku, Datenvolumen)
        val reach = HashMap<String, Boolean>()
        val unreachable = ArrayList<Profile>()
        val runnable = ArrayList<Profile>()
        val rejected = ArrayList<PcRejection>()
        // Vorab-Prüfung der Kennung je Host und Lauf nur einmal aushandeln (F5, nur im Speicher); entschieden wird je Profil
        val pcSeen = PcSeen()
        for (p in eligible) {
            // Akku (0.3.4): Bildordner ohne übertragungsbereite Dateien brauchen den PC nicht – kein
            // Erreichbarkeitstest, keine Verbindung. SyncEngine meldet dann ohne Netzwerk "Nichts zu tun"
            // (und plant für zu junge Fotos den Nachhol-Sync). Ordner-Profile brauchen den PC immer.
            if (p.type() == ProfileType.PHOTOS &&
                PendingCheck.readyFiles(ctx, p, settings.minFileAgeMinutes) == 0
            ) { runnable += p; continue }
            val host = SmbTarget.parse(p.smbPath)?.host
            if (host == null) { runnable += p; continue } // ungültiger Pfad -> Engine meldet den Fehler
            // «Bekannte Netze»: unbekanntes WLAN = nur ein Versuch, ausser das Sicherheitsnetz (einmal je 24 h und Profil) ist fällig
            val knownNets = KnownNetStore.list(ctx, p.id)
            val knownNet = netFp == null || KnownNets.isKnown(knownNets, netFp)
            val fullDue = !knownNet && NetworkRules.fullAttemptsDue(System.currentTimeMillis(), KnownNetStore.lastFullAttempts(ctx, p.id))
            if (fullDue && !manual) KnownNetStore.markFullAttempts(ctx, p.id, System.currentTimeMillis())
            val waits = NetworkRules.reachabilityWaits(manual, offWifi, knownNet, fullDue)
            val ok = reach[host.lowercase()] ?: run {
                var r = false
                for ((n, w) in waits.withIndex()) {
                    if (w > 0) {
                        if (!manual && !WifiHelper.onAllowedNetwork(ctx)) break // Netz wieder weg -> aufgeben
                        Notifications.updateProgress(
                            ctx, SyncProgress(p.name, str(R.string.wait_for_pc, host, n + 1, waits.size), 0, 0, 0, 0, null), null
                        )
                        delay(w)
                    }
                    if (SmbSession.isReachable(host)) { r = true; break }
                }
                Notifications.cancelProgress(ctx)
                reach[host.lowercase()] = r
                r
            }
            if (!ok) { unreachable += p; problems.put(ProblemFactory.of(p, ProblemKind.PC_UNREACHABLE), System.currentTimeMillis()); continue }
            // PC-Erkennung: Kennung VOR jeder Anmeldung prüfen (nur Aushandeln, keine Zugangsdaten).
            when (val pre = precheckPc(p, manual, pcSeen)) {
                PcPre.Run -> {
                    runnable += p; KnownNetStore.confirmContact(ctx, p.id, netFp)
                    problems.clearKinds(p.id, PRECHECK_KINDS) // Vorprüfung bestanden: diese Meldungen sind erledigt
                }
                PcPre.Unreachable -> { unreachable += p; problems.put(ProblemFactory.of(p, ProblemKind.PC_UNREACHABLE), System.currentTimeMillis()) }
                is PcPre.Rejected -> {
                    rejected += PcRejection(p, pre.decision, host)
                    val rej = PcRejection(p, pre.decision, host)
                    problems.put(ProblemFactory.of(p, ProblemKinds.fromPcDecision(pre.decision), rej.message()), System.currentTimeMillis())
                }
                // F3: Fehler der Aushandlung (nur SMB1, unerwarteter Status, Kennung nicht lesbar) wie «abgewiesen»:
                // eine Meldung, kein Timer, keine Dauerbenachrichtigung, kein stilles Überspringen
                is PcPre.Failed -> {
                    rejected += PcRejection(p, null, host, pre.text)
                    // Verschlüsselung: bestehender Zustand «abgelehnt» (je Profil, siehe unten); übrige Aushandlungsfehler als Meldung
                    if (!pre.encryption) problems.put(ProblemFactory.of(p, pre.kind, pre.text), System.currentTimeMillis())
                    // Gleicher Weg wie nach einem Lauf (engine.encryptionUnavailable): Zustand «Verschlüsselung abgelehnt» setzen
                    if (pre.encryption) settings.setEncNotified(p.id, true)
                }
            }
        }
        // PC erkannt/nicht mehr abgewiesen -> Benachrichtigungsmerker löschen (neue Meldung bei erneutem Wechsel)
        for (p in runnable) settings.setPcNotified(p.id, false)
        if (unreachable.isNotEmpty()) settings.noteSkip(str(R.string.skip_pc_unreachable, unreachable.joinToString(", ") { it.name }), manual)
        if (rejected.isNotEmpty()) {
            // -157: Aushandlungsfehler (z. B. Verschlüsselung erforderlich, aber nicht verfügbar) sind keine «nicht erkannten» PCs
            val failedNeg = rejected.filter { it.decision == null }
            val notRecognised = rejected.filter { it.decision != null }
            settings.noteSkip(listOfNotNull(
                notRecognised.takeIf { it.isNotEmpty() }?.let { str(R.string.skip_pc_not_recognised, it.joinToString(", ") { r -> r.profile.name }) },
                failedNeg.takeIf { it.isNotEmpty() }?.let { str(R.string.skip_pc_failed, it.joinToString(", ") { r -> r.profile.name }) },
            ).joinToString(" · "), manual)
            if (manual) {
                Notifications.summary(ctx, rejected.joinToString("\n") { "${it.profile.name}: ${it.message()}" }, error = true)
            } else for (r in rejected) if (!settings.pcNotified(r.profile.id)) {
                settings.setPcNotified(r.profile.id, true)
                Notifications.summary(ctx, "${r.profile.name}: ${r.message()}", error = true)
            }
        }
        if (runnable.isEmpty()) {
            // F2: abgewiesene Profile setzen keinen Timer (Port 445 antwortet, die Abweisung ändert nur der Benutzer); nicht erreichbare schon
            handleUnreachable(unreachable, manual, isRetry)
            // Fortsetzen-Auftrag: nichts mehr fortzusetzen, wenn alle abgewiesen wurden (Anzeige «Pausiert» nicht stehen lassen)
            if (unreachable.isEmpty() && isResumeRun && settings.resumeOnWifi) { settings.resumeOnWifi = false; PauseService.stop(ctx) }
            return Result.success()
        }
        // Ein unterbrochener Sync wird jetzt fortgesetzt -> Merker, Hinweis und Fortsetzen-Auftrag aufräumen
        if (settings.resumeOnWifi) {
            settings.resumeOnWifi = false
            if (!isResumeRun) WorkManager.getInstance(ctx).cancelUniqueWork(SyncScheduler.WORK_RESUME)
        }
        PauseService.stop(ctx) // "Pausiert" weg, ab jetzt normale Fortschrittsanzeige
        Notifications.cancelInterrupted(ctx)
        if (unreachable.isEmpty() && rejected.isEmpty()) settings.noteSkip("")

        val cancel = WorkManager.getInstance(ctx).createCancelPendingIntent(id)
        try {
            setForeground(Notifications.foregroundInfo(ctx, null, cancel))
        } catch (e: Exception) {
            // Android 12+: Start aus dem Hintergrund evtl. nicht erlaubt -> normale Benachrichtigung
            Notifications.updateProgress(ctx, null, cancel)
        }
        // SYNC-20261116-434: Vordergrund wirklich erteilt? Die Ablehnung erscheint nicht zuverlässig als Ausnahme (sie fällt im
        // WorkManager-Dienst an); deshalb prüfen, ob dessen Vordergrunddienst läuft. Unklar (Fehler beim Abfragen) = keine Meldung.
        val ranWithoutForeground = !ForegroundCheck.granted(ctx)
        if (ranWithoutForeground) com.sync2pc.app.ui.BatteryTip.noteRanWithoutForeground(ctx)
        // SYNC-20261116-437: Sicherheitsnetz gegen Prozess-Kill (nur automatische Läufe; ein manueller Lauf läuft bei sichtbarer App)
        if (!manual) SyncScheduler.armRunGuard(ctx, appendToCurrent = isResumeRun)

        var lastUiUpdateAt = 0L
        var uploaded = 0; var failed = 0; var waiting = 0; var bytes = 0L
        // SYNC-20261115-409: alle fehlgeschlagenen Profile des Laufs (nicht nur das letzte), je Ursache eine Zeile
        val failures = ArrayList<Pair<String, String>>()
        var hardFailure: String? = null
        // Mitten im Lauf verlorene Verbindung (DEC-048): nur diese setzen nach einem Lauf einen Wiederholungs-Timer
        val lostMidRun = ArrayList<Profile>()
        var young = FollowUpPlan.Young()
        val followUpProfiles = HashSet<Long>()
        var conflicts = 0
        var spaceShortage = false
        var interruptedMidRun = false
        var deferredFiles = 0
        var folderChanged = false // SYNC-20261111-213: Handy-Ordner während des Laufs verändert
        var trashedFiles = 0
        // WLAN während der Übertragung wach halten (sonst schaltet Android es im Standby in den Stromsparmodus)
        val locks = TransferLocks.acquire(ctx)
        try {
            for (p in runnable) {
                val shouldContinue: () -> Boolean = {
                    manual || WifiHelper.onAllowedNetwork(ctx)
                }
                val onProgress: (SyncProgress) -> Unit = progress@{ pr ->
                    if (isStopped) return@progress // nach dem Stopp keine Anzeige mehr neu erzeugen
                    if (pr.totalFiles > 0) lastProgress = pr
                    // SYNC-20261116-437: ohne Vordergrund (App gecacht) Binder-Aufrufe begrenzen: Anzeige und Fortschrittsdaten höchstens alle 5 s,
                    // sonst friert Android den Prozess wegen «excessive binder traffic during cached» ein und beendet ihn
                    if (ranWithoutForeground) {
                        val now = android.os.SystemClock.elapsedRealtime()
                        if (now - lastUiUpdateAt < 5_000) return@progress
                        lastUiUpdateAt = now
                    }
                    Notifications.updateProgress(ctx, pr, cancel)
                    setProgressAsync(
                        workDataOf(
                            P_PROFILE to pr.profileName, P_PHASE to pr.phase, P_DONE to pr.doneFiles,
                            P_TOTAL to pr.totalFiles, P_DONE_BYTES to pr.doneBytes,
                            P_TOTAL_BYTES to pr.totalBytes, P_FILE to pr.currentFile
                        )
                    )
                }
                // Bildordner -> sortierte Sicherung, normaler Ordner -> Spiegelung
                val engine: SyncRunner = if (p.type() == ProfileType.MIRROR)
                    MirrorEngine(ctx, p, trigger, shouldContinue = shouldContinue, onProgress = onProgress,
                        interruptReason = ::interruptReason)
                else
                    SyncEngine(ctx, p, trigger, shouldContinue = shouldContinue, onProgress = onProgress,
                        interruptReason = ::interruptReason)
                val r = engine.run()
                // SYNC-20261116-434: Verlaufszeile (Stufe W) in jedem Lauf, der ohne Vordergrund lief; Fehler beim Schreiben ignorieren
                if (ranWithoutForeground && r.id > 0) try {
                    val m = Msg.of(R.string.run_no_foreground)
                    dao.insertLog(com.sync2pc.app.data.LogEntry(runId = r.id, time = System.currentTimeMillis(), level = "W", message = m.text(), code = m.toCode()))
                } catch (ignored: Exception) {}
                if (engine.blocked) {
                    Notifications.guardNeeded(ctx, p.name)
                    continue
                }
                // Erster manueller Start eines neuen Profils -> ab jetzt auch automatisch
                if (manual && p.id in awaiting && r.status != RunStatus.FAILED) settings.setAwaitingFirstStart(p.id, false)
                uploaded += r.uploaded; failed += r.failed; bytes += r.bytes; deferredFiles += r.deferred
                if (engine.changedDuringRun) folderChanged = true
                trashedFiles += engine.trashedCount
                if (p.type() == ProfileType.PHOTOS) waiting += r.waiting
                conflicts += engine.newConflicts
                if (engine.spaceShortage) spaceShortage = true
                if (engine.wifiLost && !manual) markInterrupted()
                // SYNC-20261115-409: Zustand je Profil nachführen (Fehler von A bleibt, wenn B fehlerfrei läuft)
                val nowMs = System.currentTimeMillis()
                when {
                    engine.connectionFailed -> problems.put(ProblemFactory.of(p, ProblemKind.PC_UNREACHABLE), nowMs)
                    engine.encryptionUnavailable -> {} // bestehender Zustand «Verschlüsselung abgelehnt»
                    r.status == RunStatus.FAILED -> {
                        val (kind, detail) = ProblemFactory.fromFailedRun(r.messageCode, r.message)
                        problems.put(ProblemFactory.of(p, kind, detail), nowMs)
                    }
                    r.status == RunStatus.PARTIAL || r.failed > 0 -> problems.put(ProblemFactory.of(p, ProblemKind.PARTIAL, r.messageCode ?: r.message), nowMs)
                    else -> problems.clear(p.id)
                }
                if (engine.connectionFailed) { unreachable += p; lostMidRun += p; interruptedMidRun = true }
                else if (engine.encryptionUnavailable) {
                    // «Verschlüsselung nicht verfügbar»: ändert sich nur durch den Benutzer. Automatische Läufe melden es je Profil
                    // einmal, bis ein Lauf des Profils gelingt; ein manueller Start meldet immer. Der Verlauf bleibt FAILED.
                    if (manual || !settings.encNotified(p.id)) failures += p.name to r.messageText()
                    settings.setEncNotified(p.id, true)
                }
                else if (r.status == RunStatus.FAILED) failures += p.name to r.messageText()
                if (!engine.connectionFailed && !engine.encryptionUnavailable && r.status != RunStatus.FAILED)
                    settings.setEncNotified(p.id, false)
                if (p.type() == ProfileType.PHOTOS && engine.nextYoungFileAt > 0) {
                    young = FollowUpPlan.combine(young, FollowUpPlan.Young(engine.nextYoungFileAt, engine.newestYoungFileAt))
                    followUpProfiles += p.id
                }
            }
        } finally {
            locks.release()
            Notifications.cancelProgress(ctx)
        }

        hardFailure = FailureLines.build(failures).takeIf { it.isNotEmpty() }
        // --- Abschluss-Benachrichtigungen ----------------------------------------
        // Verbindungsabbrüche sind erwartbar: keine Meldung, der Rest folgt automatisch
        when (SyncDecisions.summaryKind(hardFailure != null, interruptedMidRun, uploaded, failed)) {
            SyncDecisions.SummaryKind.FAILED ->
                Notifications.summary(ctx, str(R.string.summary_failed, hardFailure!!), error = true)
            // NONE bei Unterbruch mitten im Lauf: still – Anzeige "Pausiert" bzw. erneuter Versuch übernimmt
            SyncDecisions.SummaryKind.NONE -> {}
            SyncDecisions.SummaryKind.TRANSFERRED -> Notifications.summary(
                ctx,
                plural(R.plurals.summary_transferred, uploaded, uploaded, Notifications.formatBytes(bytes)) +
                    (if (failed > 0) ", " + plural(R.plurals.summary_errors, failed) else ""),
                error = failed > 0
            )
        }
        if (waiting > 0) {
            val groups = dao.decisions().size
            Notifications.decisionNeeded(ctx, groups, waiting)
        }
        if (conflicts > 0) Notifications.conflictsNeeded(ctx, dao.openConflictCount())
        if (spaceShortage) Notifications.summary(ctx, str(R.string.summary_no_space), error = true)
        val toDelete = dao.queuedDeletes().map { it.sourceKey }.distinct().size
        if (toDelete > 0) Notifications.deleteReady(ctx, toDelete)
        // Während des Laufs neu entstandene Fotos/Videos -> nach Mindestalter nachholen
        // SYNC-20261113-306: EIN Nachhol-Lauf nach dem Reifen der NEUSTEN jungen Datei (nicht der ältesten: sonst reift je Lauf
        // nur ein Foto einer Serie und es entsteht eine Kette von Läufen)
        val minAgeMs = GlobalSettings(ctx).minFileAgeMinutes * 60_000L
        var createdAt = 0L
        if (MediaScanner.hasAddedSince(ctx, startedAt / 1000)) {
            createdAt = System.currentTimeMillis() + minAgeMs
            for (p in runnable) if (p.type() == ProfileType.PHOTOS) followUpProfiles += p.id
        }
        val followUpAt = FollowUpPlan.dueAt(young, createdAt)
        if (followUpAt > 0) SyncScheduler.enqueueFollowUp(ctx, followUpAt, followUpProfiles)
        // SYNC-20261111-173: Dateien, die sich während des Laufs änderten, folgen per automatischem Folgelauf (höchstens einer
        // pro Stunde). Pause/«nur manuell» gelten wie bei jedem automatischen Lauf (nicht planen, kein Stundenkontingent verbrauchen).
        // SYNC-20261114-334 (5): leerer Folgelauf -> Sperre für Folgeläufe allein wegen des Ordnerstempels; jede Arbeit hebt sie auf
        val didWork = uploaded > 0 || failed > 0 || deferredFiles > 0 || conflicts > 0 || trashedFiles > 0
        if (!interruptedMidRun) settings.folderFollowUpIdleUntil = SkipFollowUp.idleUntilAfter(
            settings.folderFollowUpIdleUntil, System.currentTimeMillis(), inputData.getBoolean(SyncScheduler.KEY_SKIP_FOLLOWUP, false), didWork)
        SyncScheduler.planSkipFollowUp(ctx, settings, interruptedMidRun, deferredFiles, folderChanged, System.currentTimeMillis())
        // Erfolgreich gesichert -> eine offene Erinnerung "lange nicht gesichert" ist erledigt
        if (SyncDecisions.staleResolved(hardFailure != null, interruptedMidRun, unreachable.isEmpty(), rejected.isEmpty())) {
            Notifications.cancelStale(ctx)
            GlobalSettings(ctx).lastStaleWarnAt = 0
        }

        // F2: Lief mindestens ein Profil, setzt ein nicht erreichbarer PC keinen Timer (ein erreichbares Profil darf die anderen
        // nicht blockieren und der Akku wird geschont); die übrigen werden beim nächsten ohnehin stattfindenden Lauf neu bewertet.
        // Ein Abbruch mitten im Lauf behält die kurze Wiederholung (DEC-048/DEC-126). Abgewiesene Profile setzen nie einen Timer.
        handleUnreachable(lostMidRun, manual, isRetry, midRun = interruptedMidRun)
        return Result.success()
    }

    /**
     * Nachhol-Auftrag ohne übertragungsbereite Fotos (SYNC-20261113-306): verbleiben junge Dateien, wird die Frist nach der neusten
     * von ihnen neu gesetzt (rein lokal, ohne Netzwerk); sonst endet die Kette hier.
     */
    private suspend fun replanFollowUp(profiles: List<Profile>, minAgeMinutes: Int) {
        var young = FollowUpPlan.Young()
        val ids = HashSet<Long>()
        for (p in profiles) {
            val y = PendingCheck.youngFiles(applicationContext, p, minAgeMinutes)
            if (y.any) { young = FollowUpPlan.combine(young, y); ids += p.id }
        }
        val at = FollowUpPlan.dueAt(young, 0)
        if (at > 0) SyncScheduler.enqueueFollowUp(applicationContext, at, ids)
    }

    /**
     * Ein vom Kennungs-Vorabtest abgewiesenes Profil (Zugangsdaten wurden nicht gesendet). [decision] null = Fehler der Aushandlung
     * mit eigenem Text [text] (F3).
     */
    private class PcRejection(val profile: Profile, val decision: PcDecision?, val host: String, val text: String? = null) {
        fun message(): String = text ?: PcIdentityException(decision ?: PcDecision.UNKNOWN, host).message
    }

    private sealed class PcPre {
        object Run : PcPre()
        object Unreachable : PcPre()
        class Rejected(val decision: PcDecision) : PcPre()
        /** [encryption]: scheiterte an «Erforderlich, aber nicht verfügbar» (SYNC-20261115-404): Karte «Verschlüsselung nicht verfügbar» zeigen */
        class Failed(val text: String, val encryption: Boolean = false, val kind: ProblemKind = ProblemKind.NEGOTIATION) : PcPre()
    }

    /**
     * Vorab-Prüfung der PC-Kennung für GUID/BOTH (nur Aushandeln, keine Zugangsdaten, je Host einmal über [seen]). Hier wird nie
     * gelernt (V1): eine unbekannte Kennung ergibt UNKNOWN, auch beim manuellen Start; gelernt wird nur beim Speichern und in
     * «Verbindung testen». Alles ausser ALLOW wird abgewiesen (sicherer Ausfall).
     */
    private fun precheckPc(p: Profile, manual: Boolean, seen: PcSeen): PcPre {
        val access = if (manual) PcAccess.USER else PcAccess.AUTO
        return try {
            when (val d = SmbSession.checkPc(p, access, seen)) {
                PcDecision.ALLOW -> PcPre.Run
                else -> PcPre.Rejected(d)
            }
        } catch (e: PcIdentityException) {
            PcPre.Rejected(e.decision)
        } catch (e: Exception) {
            if (isNetworkError(e)) PcPre.Unreachable else PcPre.Failed(userError(e), isEncryptionUnavailable(e), ProblemKinds.fromKeys(com.sync2pc.app.l10n.Msg.keys(userErrorMsg(e).toCode()).orEmpty()) ?: ProblemKind.NEGOTIATION)
        }
    }

    /**
     * Nicht erreichbare Shares: automatisch -> in 30 Minuten erneut prüfen (solange im WLAN),
     * manuell -> Hinweis. Alles erreichbar -> Wartezustand aufheben.
     */
    private fun handleUnreachable(
        unreachable: List<Profile>, manual: Boolean, isRetry: Boolean, midRun: Boolean = false,
    ) {
        val ctx = applicationContext
        val s = GlobalSettings(ctx)
        // Abgewiesene Profile (PC nicht sicher erkannt, SYNC-20261109-047/-055) kommen nicht hierher: ohne Timer, sie werden beim
        // nächsten ohnehin stattfindenden Lauf neu bewertet (Port 445 antwortet, die Abweisung ändert nur der Benutzer).
        val waiting = unreachable
        val names = unreachable.joinToString(", ") { it.name }
        // Noch im Ziel-WLAN? Dann ist der PC weg -> in 30 Minuten erneut prüfen.
        // WLAN selbst verloren -> kein Timer, sondern sofort beim nächsten Verbinden (WifiNetworkReceiver).
        // Die Entscheidung liegt in SyncDecisions.unreachableAction (SYNC-20261111-207, B4, testbar).
        val action = SyncDecisions.unreachableAction(waiting.isNotEmpty(), manual, midRun) { WifiHelper.onAllowedNetwork(ctx) }
        when (action) {
            SyncDecisions.UnreachableAction.CLEAR_WAIT -> {
                s.shareRetryAt = 0; s.shareUnreachable = ""
                // Die laufende Wiederholung nicht selbst abbrechen
                if (!isRetry) WorkManager.getInstance(ctx).cancelUniqueWork(SyncScheduler.WORK_RETRY)
            }
            // Abbruch mitten in der Übertragung behandeln wie automatisch (auch bei manuellem Start): still fortsetzen
            SyncDecisions.UnreachableAction.NOTIFY_ONLY ->
                Notifications.summary(ctx, str(R.string.summary_pc_unreachable, names), error = true)
            SyncDecisions.UnreachableAction.RETRY_TIMER -> {
                // Im WLAN: Abbruch mitten in der Übertragung = meist kurzer Aussetzer -> bald erneut (2 min).
                // Nur mobile Daten/VPN (0.11.7): immer 2 Stunden. Ein erfolgreicher Lauf (auch von Hand) hebt die
                // Wartezeit auf (CLEAR_WAIT); der manuelle Start ignoriert sie ohnehin.
                val minutes = NetworkRules.retryMinutes(WifiHelper.offWifiSyncActive(ctx), midRun)
                s.shareRetryAt = System.currentTimeMillis() + minutes * 60_000
                s.shareUnreachable = names
                SyncScheduler.enqueueShareRetry(ctx, minutes)
                if (midRun) Notifications.showPaused(ctx, lastProgress, targetWifi, str(R.string.paused_connection_retry, minutes))
            }
            SyncDecisions.UnreachableAction.RESUME_ON_WIFI -> {
                s.shareRetryAt = 0; s.shareUnreachable = ""
                if (!isRetry) WorkManager.getInstance(ctx).cancelUniqueWork(SyncScheduler.WORK_RETRY)
                markInterrupted(newInterruption = midRun)
            }
        }
    }
}
