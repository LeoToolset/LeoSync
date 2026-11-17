package com.sync2pc.app.sync

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.IBinder
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.sync2pc.app.data.GlobalSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * "Pausiert"-Zustand nach einem WLAN-Unterbruch.
 *
 * Läuft als Vordergrunddienst mit der Anzeige "Pausiert – wartet auf WLAN …" in der Statusleiste.
 * So hält Android die App sicher am Leben (manche Hersteller beenden Hintergrund-Apps sonst so
 * gründlich, dass weder WLAN-Broadcast noch Aufgabenplaner sie wieder wecken).
 * Der Dienst wartet nur (kein Rechnen, kein Netzwerkverkehr) und startet den Sync, sobald
 * eines der Ziel-WLANs wieder verbunden ist.
 *
 * Ab Android 15 (API 35) beendet sich der Dienst nach [PausePolicy.LIMIT_MILLIS] (30 min) selbst (SYNC-20261115-389, R-BG1):
 * Android begrenzt alle dataSync-Vordergrunddienste der App zusammen auf 6 h je 24 h, und eine stundenlange Pause würde das
 * Budget verbrauchen, das der Nachhol-Lauf braucht. Danach gilt wie bei [onTimeout]: normale «Pausiert»-Anzeige, der
 * Fortsetzen-Auftrag und der WLAN-Broadcast setzen fort. Android 14 und älter: keine eigene Grenze.
 */
class PauseService : Service() {

    companion object {
        private const val EXTRA_PROFILE = "profile"
        private const val EXTRA_DONE = "done"
        private const val EXTRA_TOTAL = "total"
        private const val EXTRA_DONE_BYTES = "doneBytes"
        private const val EXTRA_TOTAL_BYTES = "totalBytes"
        private const val EXTRA_WIFI = "wifi"

        /** true = Dienst gestartet; false = Android erlaubt den Start gerade nicht (Fallback nötig) */
        fun start(context: Context, last: SyncProgress?, wifiNames: String): Boolean {
            val i = Intent(context, PauseService::class.java).apply {
                putExtra(EXTRA_WIFI, wifiNames)
                if (last != null) {
                    putExtra(EXTRA_PROFILE, last.profileName)
                    putExtra(EXTRA_DONE, last.doneFiles); putExtra(EXTRA_TOTAL, last.totalFiles)
                    putExtra(EXTRA_DONE_BYTES, last.doneBytes); putExtra(EXTRA_TOTAL_BYTES, last.totalBytes)
                }
            }
            return try {
                ContextCompat.startForegroundService(context, i); true
            } catch (e: Exception) {
                false // z. B. ForegroundServiceStartNotAllowedException (Android 12+, App im Hintergrund)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, PauseService::class.java))
            Notifications.cancelPaused(context)
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var callback: ConnectivityManager.NetworkCallback? = null
    private var checkJob: Job? = null
    /** Letzter Stand aus onStartCommand: für die normale Anzeige, wenn Android das Zeitlimit des Dienstes erreicht (R-BG3, -334) */
    private var lastProgress: SyncProgress? = null
    private var wifiNames: String = ""
    /** Läuft ab dem ersten Start; wird bei weiteren onStartCommand derselben Laufzeit nicht zurückgesetzt */
    private var limitJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val last = intent?.getStringExtra(EXTRA_PROFILE)?.let {
            SyncProgress(
                it, com.sync2pc.app.l10n.str(com.sync2pc.app.R.string.log_status_paused), intent.getIntExtra(EXTRA_DONE, 0), intent.getIntExtra(EXTRA_TOTAL, 0),
                intent.getLongExtra(EXTRA_DONE_BYTES, 0), intent.getLongExtra(EXTRA_TOTAL_BYTES, 0), null
            )
        }
        val wifi = intent?.getStringExtra(EXTRA_WIFI) ?: ""
        lastProgress = last; wifiNames = wifi
        try {
            ServiceCompat.startForeground(
                this, Notifications.ID_PAUSED, Notifications.pausedNotification(this, last, wifi),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } catch (e: Exception) {
            // Vordergrund nicht erlaubt -> normale Anzeige, Fortsetzen übernimmt der Aufgabenplaner
            Notifications.showPaused(this, last, wifi)
            stopSelf()
            return START_NOT_STICKY
        }
        registerWifiWatch()
        val limit = PausePolicy.limitMillis(android.os.Build.VERSION.SDK_INT)
        if (limit != null && limitJob == null) limitJob = scope.launch { delay(limit); endPause() }
        return START_NOT_STICKY
    }

    private fun registerWifiWatch() {
        if (callback != null) return
        val cm = getSystemService(ConnectivityManager::class.java) ?: return
        val cb = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) = scheduleCheck()
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) = scheduleCheck()
        }
        val req = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        try { cm.registerNetworkCallback(req, cb); callback = cb } catch (ignored: Exception) {}
        scheduleCheck() // falls das WLAN schon wieder da ist
    }

    /** Prüft (gebündelt), ob wieder ein WLAN verbunden ist; danach entscheidet der Lauf (PC-Kennung, bekannte Netze). */
    private fun scheduleCheck() {
        if (checkJob?.isActive == true) return
        checkJob = scope.launch {
            val ctx = applicationContext
            if (!GlobalSettings(ctx).resumeOnWifi) { stopSelf(); return@launch } // anderweitig fortgesetzt/abgebrochen
            if (!WifiHelper.isOnWifi(ctx)) return@launch
            // Jedes WLAN löst aus (SYNC-20261115-351): ob der PC dort erreichbar und der richtige ist, prüft der Lauf
            val targets = com.sync2pc.app.data.AppDatabase.get(ctx).dao().profiles().filter { it.enabled }
            if (targets.isNotEmpty()) {
                SyncScheduler.enqueueAuto(ctx, com.sync2pc.app.l10n.Msg.of(com.sync2pc.app.R.string.trigger_resume))
                stopSelf()
            }
        }
    }

    /** Android 15+: Zeitlimit für dataSync-Dienste erreicht -> Aufgabenplaner übernimmt */
    override fun onTimeout(startId: Int, fgsType: Int) {
        endPause()
    }

    /** Gemeinsam für das Android-Zeitlimit und die eigene Grenze: Vordergrund weg, «Pausiert» als normale Benachrichtigung, Dienst beenden */
    private fun endPause() {
        // Die Vordergrund-Anzeige verschwindet mit dem Dienst: dieselbe «Pausiert»-Anzeige als normale Benachrichtigung zeigen
        try { ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE) } catch (ignored: Exception) {}
        Notifications.showPaused(this, lastProgress, wifiNames)
        stopSelf()
    }

    override fun onDestroy() {
        callback?.let { cb ->
            try { getSystemService(ConnectivityManager::class.java)?.unregisterNetworkCallback(cb) } catch (ignored: Exception) {}
        }
        callback = null
        scope.cancel()
        super.onDestroy()
    }
}

/**
 * Reine Entscheidungen zum Pausendienst (SYNC-20261115-389, getestet in PausePolicyTest).
 */
object PausePolicy {
    /** Eigene Laufzeitgrenze ab Android 15 */
    const val LIMIT_MILLIS = 30 * 60_000L

    /** null = keine eigene Grenze (Android 14 und älter), sonst [LIMIT_MILLIS] ab API 35 */
    fun limitMillis(sdkInt: Int): Long? = if (sdkInt >= 35) LIMIT_MILLIS else null

    /**
     * Darf [PauseService] gestartet werden? Ab API 35 nicht für einen Fortsetzen-Lauf, der nur feststellt, dass er noch im falschen
     * WLAN ist ([resumeRun] und nicht [midRun]): sonst startete jeder 15-Minuten-Fortsetzen-Lauf die 30 Minuten neu (Schleife, die das
     * 6-h-Budget verbraucht). Eine neue Unterbrechung eines laufenden Syncs startet ihn immer.
     */
    fun mayStart(sdkInt: Int, resumeRun: Boolean, midRun: Boolean): Boolean =
        limitMillis(sdkInt) == null || !resumeRun || midRun
}