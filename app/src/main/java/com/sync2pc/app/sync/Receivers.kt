package com.sync2pc.app.sync

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sync2pc.app.data.AppDatabase
import com.sync2pc.app.data.GlobalSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Vom System aufgerufen, wenn ein WLAN verbunden wird. Prüft das WLAN und startet ggf. den Sync. */
class WifiNetworkReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val ctx = context.applicationContext
        val s = GlobalSettings(ctx)
        // SYNC-20261116-440: Diagnose, damit ein ausbleibender Start nachweisbar ist (Receiver nicht aufgerufen / früh verlassen)
        android.util.Log.i("WifiReceiver", "WLAN-Broadcast: autoSyncOnWifi=${s.autoSyncOnWifi}, manualOnly=${s.manualOnly}, resumeOnWifi=${s.resumeOnWifi}")
        if (!s.autoSyncOnWifi || s.manualOnly) return // "für immer pausiert": nur manuell
        val now = System.currentTimeMillis()
        // Drosselung gegen Mehrfach-Events – aber nie nach einem unterbrochenen Sync (sofort fortsetzen)
        if (SyncDecisions.wifiTriggerThrottled(s.resumeOnWifi, now, s.lastWifiTriggerAt)) { android.util.Log.i("WifiReceiver", "gedrosselt"); return }
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // SYNC-20261116-440: Der Broadcast kommt, sobald das WLAN verfügbar ist; activeNetwork zeigt es oft erst kurz danach
                // (wie -430). Früher verliess der Receiver hier stumm, und im Doze, wo der Auftrag des Aufgabenplaners zurückgehalten
                // wird, blieb der Fortsetzen-Start aus. Bis 6 s warten (goAsync-Fenster), dann erst aufgeben.
                var waited = 0L
                while (!WifiHelper.isOnWifi(ctx) && waited < 6_000) { kotlinx.coroutines.delay(300); waited += 300 }
                if (!WifiHelper.isOnWifi(ctx)) { android.util.Log.i("WifiReceiver", "kein WLAN aktiv, ignoriert"); return@launch }
                android.util.Log.i("WifiReceiver", "WLAN aktiv (nach ${waited} ms), Start wird eingereiht")
                val profiles = AppDatabase.get(ctx).dao().profiles().filter { it.enabled }
                // Jedes WLAN löst aus (SYNC-20261115-351); der Lauf entscheidet über bekannte Netze und die PC-Kennung
                if (profiles.isNotEmpty()) {
                    s.lastWifiTriggerAt = now
                    // Neue WLAN-Verbindung -> PC sofort prüfen, eine laufende 30-Minuten-Wartezeit verfällt
                    s.shareRetryAt = 0; s.shareUnreachable = ""
                    val label = com.sync2pc.app.l10n.Msg.of(if (s.resumeOnWifi) com.sync2pc.app.R.string.trigger_resume else com.sync2pc.app.R.string.trigger_wifi)
                    SyncScheduler.enqueueAuto(ctx, label)
                }
            } finally {
                pending.finish()
            }
        }
    }
}

/** Nach Neustart/App-Aktualisierung (Google Play): WLAN-Callback und Trigger neu registrieren. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Nur die zwei erwarteten System-Broadcasts verarbeiten (R-BG8, -334); alles andere sofort verlassen
        val action = intent.action
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        val ctx = context.applicationContext
        SyncScheduler.setupAll(ctx)
    }
}
