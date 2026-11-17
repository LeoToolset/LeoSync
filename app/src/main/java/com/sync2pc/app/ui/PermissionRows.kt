package com.sync2pc.app.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sync2pc.app.R
import com.sync2pc.app.diag.VendorHints
import com.sync2pc.app.l10n.str

/**
 * Berechtigungen als Zeilen mit Knopf (0.9.0: gemeinsam für die Karte auf der Startseite und die Seite
 * "App-Berechtigungen"). [showDone] = auch erteilte Punkte mit "✓ erledigt" zeigen. [needs] = nur diese Gruppen (0.11.0).
 */
@Composable
fun PermissionRows(perms: PermStatus, showDone: Boolean, needs: PermNeeds = PermNeeds.ALL, showBattery: Boolean = true) {
    val ctx = LocalContext.current
    val multi = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }
    val single = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val grant = str(R.string.perm_grant)
    val open = str(R.string.perm_open)
    var blockedHint by remember { mutableStateOf(false) }
    val askedPrefs = remember { ctx.getSharedPreferences("perm_requests", Context.MODE_PRIVATE) }

    /**
     * B1 (SYNC-20261110-137): Hat der Benutzer schon einmal gefragt und "Nicht mehr fragen" gewählt, zeigt Android
     * keinen Dialog mehr. Dann statt eines wirkungslosen Tipps den Hinweis mit Knopf zur App-Info zeigen.
     */
    fun ask(needed: Array<String>, launch: () -> Unit) {
        val act = ctx as? Activity
        val open = needed.filter { ContextCompat.checkSelfPermission(ctx, it) != PackageManager.PERMISSION_GRANTED }
        val asked = open.any { askedPrefs.getBoolean("asked_$it", false) }
        val canAsk = act == null || open.any { ActivityCompat.shouldShowRequestPermissionRationale(act, it) }
        if (asked && !canAsk) { blockedHint = true; return }
        askedPrefs.edit().apply { open.forEach { putBoolean("asked_$it", true) } }.apply()
        launch()
    }

    if (blockedHint) AlertDialog(
        onDismissRequest = { blockedHint = false },
        title = { Text(str(R.string.perm_blocked_title)) },
        text = { Text(str(R.string.perm_blocked_text)) },
        confirmButton = {
            TextButton(onClick = {
                blockedHint = false
                try {
                    ctx.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${ctx.packageName}")))
                } catch (ignored: Exception) {}
            }) { Text(str(R.string.settings_perm_app_info)) }
        },
        dismissButton = { TextButton(onClick = { blockedHint = false }) { Text(str(R.string.common_cancel)) } },
    )

    @Composable
    fun row(ok: Boolean, text: String, button: String, help: String? = null, extra: (@Composable () -> Unit)? = null, onClick: () -> Unit) {
        if (ok && !showDone) return
        androidx.compose.foundation.layout.Column {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(text, modifier = Modifier.weight(1f))
                if (ok) Text(str(R.string.perm_granted), style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 12.dp))
                else TextButton(onClick = onClick) { Text(button) }
            }
            // SYNC-20261115-354: Kurzerklärung je Zeile (SYNC-20261115-354), bei erledigten Zeilen nicht mehr nötig
            if (!ok && help != null) Hint(help)
            if (!ok && extra != null) extra()
        }
    }

    if (needs.media) {
        row(perms.media, str(R.string.perm_media), grant, str(R.string.setup_media_help)) { ask(PermStatus.mediaPermissions) { multi.launch(PermStatus.mediaPermissions) } }
        // Standort in Fotos (ACCESS_MEDIA_LOCATION) gehört nicht mehr hierher: erst bei der Wahl der Länderordner (SYNC-20261115-354)
    }
    if (needs.base && Build.VERSION.SDK_INT >= 33) row(perms.notifications, str(R.string.perm_notifications), grant, str(R.string.setup_notif_help)) {
        ask(arrayOf(Manifest.permission.POST_NOTIFICATIONS)) { single.launch(Manifest.permission.POST_NOTIFICATIONS) }
    }
    // Akku-Freistellung über die allgemeine Systemliste, mit aufklappbarem «So geht es»; der Haken folgt der Systemabfrage
    if (needs.base && showBattery) row(perms.batteryUnrestricted, str(R.string.perm_battery), str(R.string.setup_battery_btn),
        str(R.string.setup_battery_help) + (if (BatteryTip.skipped(ctx)) " " + str(R.string.perm_battery_skipped) else ""),
        extra = {
            var how by remember { mutableStateOf(false) }
            TextButton(onClick = { how = !how }) { Text(str(R.string.setup_battery_how)) }
            if (how) BatteryHowTo()
        }) {
        BatteryTip.openSettings(ctx)
    }
}

/**
 * «So geht es» der Akku-Einstellung: allgemeine Schritte, Herstellerhinweise und Anleitung im Browser (vorher eigene Karte in den
 * Einstellungen, SYNC-20261115-392). Gemeinsam für Einstellungen und Tipp-Seite (SYNC-20261116-445), eine Textquelle.
 */
@Composable
internal fun BatteryHowTo() {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    Hint(str(R.string.setup_battery_steps))
    val vendor = remember { VendorHints.forDevice() }
    if (vendor != null) {
        Text(str(R.string.settings_vendor_extra, vendor.vendor), style = MaterialTheme.typography.bodyMedium)
        vendor.steps.forEach { Hint("• $it") }
        HintMore(str(R.string.settings_vendor_note))
    }
    TextButton(onClick = {
        try { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(VendorHints.webUrl(vendor)))) } catch (ignored: Exception) {}
    }) { Text(str(R.string.settings_vendor_web)) }
}
