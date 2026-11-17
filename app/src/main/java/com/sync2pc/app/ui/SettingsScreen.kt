package com.sync2pc.app.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import com.sync2pc.app.BuildConfig
import com.sync2pc.app.R
import com.sync2pc.app.data.GlobalSettings
import com.sync2pc.app.diag.Diagnostics
import com.sync2pc.app.diag.VendorHints
import com.sync2pc.app.geo.CountryLookup
import com.sync2pc.app.l10n.AppLanguage
import com.sync2pc.app.l10n.str
import com.sync2pc.app.sync.StaleCheck
import com.sync2pc.app.sync.SyncScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/**
 * Rubriken der Einstellungen (0.4.0). Das Zahnrad auf der Startseite öffnet ein Menü mit diesen Punkten;
 * jede Rubrik ist eine eigene Seite.
 */
enum class SettingsSection(@StringRes val title: Int) {
    NOTIFICATIONS(R.string.settings_notifications),
    TRANSFER(R.string.settings_transfer),
    PERMISSIONS(R.string.settings_permissions),
    APP(R.string.settings_app),
    DIAGNOSE(R.string.settings_diagnose),
}

@Composable
fun SettingsScreen(section: SettingsSection, onBack: () -> Unit) {
    ScreenScaffold(title = str(section.title), onBack = onBack) { pad ->
        LazyColumn(
            Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (section) {
                SettingsSection.NOTIFICATIONS -> notificationsPage()
                SettingsSection.TRANSFER -> transferPage()
                SettingsSection.PERMISSIONS -> permissionsPage()
                SettingsSection.APP -> appPage()
                SettingsSection.DIAGNOSE -> diagnosePage()
            }
        }
    }
}

// ---- 1. Benachrichtigungen ---------------------------------------------------------------
private fun LazyListScope.notificationsPage() {
    item {
        val ctx = LocalContext.current
        val s = remember { GlobalSettings(ctx) }
        var summary by remember { mutableStateOf(s.notifySummary) }
        var hints by remember { mutableStateOf(s.notifyHints) }
        // Ohne eigene Überschrift: die Seite heisst schon "Benachrichtigungen" (0.9.0)
        SectionCard {
            SwitchRow(str(R.string.settings_notify_summary), summary, subtitle = str(R.string.settings_notify_summary_sub)) {
                summary = it; s.notifySummary = it
            }
            SwitchRow(str(R.string.settings_notify_hints), hints, subtitle = str(R.string.settings_notify_hints_sub)) {
                hints = it; s.notifyHints = it
            }
            HintMore(str(R.string.settings_notify_fixed))
            OutlinedButton(onClick = {
                try {
                    ctx.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName))
                } catch (ignored: Exception) {}
            }) { Text(str(R.string.settings_notify_android)) }
        }
    }
    item {
        val ctx = LocalContext.current
        val s = remember { GlobalSettings(ctx) }
        var staleOn by remember { mutableStateOf(s.staleWarnEnabled) }
        var staleDays by remember { mutableStateOf(s.staleWarnDays.toString()) }
        SectionCard(str(R.string.settings_stale)) {
            SwitchRow(str(R.string.settings_stale_switch), staleOn,
                subtitle = str(R.string.settings_stale_sub, StaleCheck.REPEAT_DAYS)) {
                staleOn = it; s.staleWarnEnabled = it; SyncScheduler.setupAll(ctx)
            }
            if (staleOn) RangeNumberField(
                staleDays, { staleDays = it }, str(R.string.settings_stale_days), 1..365, R.string.settings_err_days, maxDigits = 3,
            ) { s.staleWarnDays = it }
        }
    }
}

// ---- 2. Übertragung -------------------------------------------------------------------
private fun LazyListScope.transferPage() {
    item {
        val ctx = LocalContext.current
        val s = remember { GlobalSettings(ctx) }
        var autoWifi by remember { mutableStateOf(s.autoSyncOnWifi) }
        var onNew by remember { mutableStateOf(s.syncOnNewMedia) }
        var charging by remember { mutableStateOf(s.onlyWhileCharging) }
        var offWifi by remember { mutableStateOf(s.syncOffWifi) }
        var minAge by remember { mutableStateOf(s.minFileAgeMinutes.toString()) }
        var folderCheck by remember { mutableStateOf(s.folderCheckMinutes.toString()) }
        fun apply() = SyncScheduler.setupAll(ctx)
        SectionCard(str(R.string.settings_auto)) {
            SwitchRow(str(R.string.settings_auto_wifi), autoWifi, subtitle = str(R.string.settings_auto_wifi_sub)) {
                autoWifi = it; s.autoSyncOnWifi = it; apply()
            }
            SwitchRow(str(R.string.settings_auto_new), onNew, subtitle = str(R.string.settings_auto_new_sub)) {
                onNew = it; s.syncOnNewMedia = it; apply()
            }
            SwitchRow(str(R.string.settings_auto_charging), charging, subtitle = str(R.string.settings_auto_charging_sub)) {
                charging = it; s.onlyWhileCharging = it; apply()
            }
            SwitchRow(str(R.string.settings_auto_offwifi), offWifi, subtitle = str(R.string.settings_auto_offwifi_sub)) {
                offWifi = it; s.syncOffWifi = it; apply()
                // Geplante Aufträge und Wartezeit "PC nicht erreichbar" gehören zur alten Netzwerkregel
                SyncScheduler.onOffWifiChanged(ctx)
            }
            // SYNC-20261113-314: bei Schrift 2,0 (fontScale >= 1,75) zusätzlicher Abstand über dem Feld; bei 1,0 und 1,5 unverändert
            if (androidx.compose.ui.platform.LocalDensity.current.fontScale >= 1.75f) androidx.compose.foundation.layout.Spacer(
                androidx.compose.ui.Modifier.height(12.dp))
            RangeNumberField(
                minAge, { minAge = it }, str(R.string.settings_min_age), 0..24 * 60, R.string.settings_err_minutes, maxDigits = 4,
            ) { s.minFileAgeMinutes = it; apply() }
            HintMore(str(R.string.settings_min_age_hint), extraBottom = 8.dp)
            // SYNC-20261113-306: Prüfintervall der Ordner-Profile, global statt je Profil
            RangeNumberField(
                folderCheck, { folderCheck = it }, str(R.string.settings_folder_check), com.sync2pc.app.data.NumberInput.POLL_MINUTES,
                R.string.settings_err_folder_check, maxDigits = 4,
            ) { s.folderCheckMinutes = it; apply() }
            HintMore(str(R.string.settings_folder_check_hint), extraBottom = 8.dp)
        }
    }
    item {
        val ctx = LocalContext.current
        val s = remember { GlobalSettings(ctx) }
        var parallel by remember { mutableStateOf(s.parallelUploads) }
        SectionCard(str(R.string.settings_parallel)) {
            Text(str(R.string.settings_parallel_value, parallel))
            Slider(
                value = parallel.toFloat(),
                onValueChange = { parallel = it.roundToInt().coerceIn(1, 4); s.parallelUploads = parallel },
                valueRange = 1f..4f,
                steps = 2,
            )
            HintMore(str(R.string.settings_parallel_hint))
        }
    }
}

// ---- 3. App-Berechtigungen ------------------------------------------------------------
private fun LazyListScope.permissionsPage() {
    // 0.9.0: jeder Punkt mit Status (✓ erledigt bzw. Knopf), wie die Karte auf der Startseite
    item {
        val ctx = LocalContext.current
        val tick = rememberResumeTick()
        val perms = remember(tick) { PermStatus.read(ctx) }
        SectionCard(str(R.string.settings_perm_title)) {
            HintMore(str(R.string.settings_perm_hint))
            // Nur, was die vorhandenen Profile brauchen (Fotos-Zeile entfällt ohne Fotos-Profil, SYNC-20261115-392)
            val profiles by produceState(emptyList<com.sync2pc.app.data.Profile>(), tick) {
                value = withContext(Dispatchers.IO) { try { com.sync2pc.app.data.AppDatabase.get(ctx).dao().profiles() } catch (e: Exception) { emptyList() } }
            }
            PermissionRows(perms, showDone = true, needs = PermNeeds.of(profiles))
            OutlinedButton(onClick = {
                ctx.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${ctx.packageName}")))
            }) { Text(str(R.string.settings_perm_app_info)) }
        }
    }
    // Die Akku-Anleitung (inkl. Hersteller-Hinweisen und Browser-Link) steht in der Akku-Zeile oben, nicht mehr als eigene Karte
}

// ---- 4. Sprache & Sicherung (bis 0.8.x "App-Einstellungen") ---------------------------------
private fun LazyListScope.appPage() {
    // Reihenfolge (0.9.1): Sprache, Länderordner, Sicherung
    item { LanguageCard() }
    item {
        val ctx = LocalContext.current
        val s = remember { GlobalSettings(ctx) }
        var home by remember { mutableStateOf(s.homeCountryIso) }
        var geoInfo by remember { mutableStateOf(str(R.string.settings_geo_loading)) }
        var lookup by remember { mutableStateOf<CountryLookup?>(null) }
        LaunchedEffect(Unit) {
            geoInfo = withContext(Dispatchers.IO) {
                val c = CountryLookup.get(ctx)
                lookup = c
                if (c.isAvailable) str(R.string.settings_geo_loaded, c.countryCount)
                else str(R.string.settings_geo_missing)
            }
        }
        // Gültig: zwei Buchstaben und (sobald die Länderliste geladen ist) ein Code daraus
        fun validIso(v: String): Boolean = v.length == 2 &&
            (lookup?.takeIf { it.isAvailable }?.let { it.namesOf(v).isNotEmpty() } ?: true)
        val homeInvalid = home.isNotEmpty() && !validIso(home) || home.isEmpty()
        SectionCard(str(R.string.settings_country_folders)) {
            OutlinedTextField(
                home,
                { v -> home = v.filter { it.isLetter() }.take(2).uppercase(); if (validIso(home)) s.homeCountryIso = home },
                label = { Text(str(R.string.settings_home_country)) },
                isError = homeInvalid,
                supportingText = if (homeInvalid) ({ Text(str(R.string.settings_err_country)) }) else null,
                singleLine = true, modifier = Modifier.fillMaxWidth(),
            )
            HintMore(str(R.string.settings_home_country_hint))
            Hint(geoInfo)
        }
    }
    // Sicherung (Export/Import) seit 0.9.0 hier statt unter "Diagnose"
    item { BackupSection() }
}

/**
 * Zahlenfeld mit Bereichsprüfung (E1, SYNC-20261110-133): ungültige Eingabe zeigt am Feld einen Fehlertext;
 * [onValid] wird nur für gültige Werte aufgerufen. Beim Wiedereintritt zeigt das Feld wieder den gespeicherten Wert.
 */
@Composable
private fun RangeNumberField(
    text: String, onText: (String) -> Unit, label: String, range: IntRange, @StringRes errorRes: Int,
    maxDigits: Int, onValid: (Int) -> Unit,
) {
    val value = text.toIntOrNull()?.takeIf { it in range }
    OutlinedTextField(
        text,
        { v ->
            val digits = v.filter { it.isDigit() }.take(maxDigits)
            onText(digits)
            digits.toIntOrNull()?.takeIf { it in range }?.let(onValid)
        },
        label = { Text(label) },
        isError = value == null,
        supportingText = if (value == null) ({ Text(str(errorRes)) }) else null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true, modifier = Modifier.fillMaxWidth(),
    )
}

/** Sprachwahl (0.8.0): Systemsprache / Deutsch / English. Die Sprachnamen stehen bewusst in der eigenen Sprache. */
@Composable
private fun LanguageCard() {
    val ctx = LocalContext.current
    val current = remember { AppLanguage.current(ctx) }
    val options = listOf(
        AppLanguage.SYSTEM to str(R.string.settings_language_system),
        AppLanguage.GERMAN to "Deutsch",
        AppLanguage.ENGLISH to "English",
    )
    SectionCard(str(R.string.settings_language)) {
        options.forEach { (tag, label) ->
            Row(
                Modifier.fillMaxWidth().clickable {
                    if (tag != current) (ctx as? Activity)?.let {
                        MainActivity.returnToSection = SettingsSection.APP
                        AppLanguage.set(it, tag)
                    }
                },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = tag == current, onClick = null)
                Text(label, Modifier.padding(start = 8.dp, top = 8.dp, bottom = 8.dp))
            }
        }
        HintMore(str(R.string.settings_language_hint))
    }
}

// ---- 6. Diagnose ----------------------------------------------------------------------
private fun LazyListScope.diagnosePage() {
    item {
        val ctx = LocalContext.current
        val s = remember { GlobalSettings(ctx) }
        val scope = rememberCoroutineScope()
        var diagBusy by remember { mutableStateOf(false) }
        SectionCard(str(R.string.settings_diag_title)) {
            HintMore(str(R.string.settings_diag_hint, s.deviceId))
            OutlinedButton(enabled = !diagBusy, onClick = {
                diagBusy = true
                scope.launch {
                    try {
                        Diagnostics.copyToClipboard(ctx, Diagnostics.build(ctx))
                        android.widget.Toast.makeText(ctx, str(R.string.settings_diag_copied), android.widget.Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(ctx, str(R.string.settings_diag_failed, e.message ?: ""), android.widget.Toast.LENGTH_LONG).show()
                    } finally {
                        diagBusy = false
                    }
                }
            }) { Text(str(if (diagBusy) R.string.settings_diag_busy else R.string.settings_diag_copy)) }
            // 0.11.5 (DEC-121): Mail-App mit Adresse, Betreff und Diagnose als Anhang; der Benutzer sendet selbst
            Hint(str(R.string.settings_report_hint, Diagnostics.SUPPORT_EMAIL))
            OutlinedButton(enabled = !diagBusy, onClick = {
                diagBusy = true
                scope.launch {
                    try {
                        if (!Diagnostics.reportProblem(ctx)) {
                            android.widget.Toast.makeText(ctx, str(R.string.settings_report_no_mail, Diagnostics.SUPPORT_EMAIL), android.widget.Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(ctx, str(R.string.settings_diag_failed, e.message ?: ""), android.widget.Toast.LENGTH_LONG).show()
                    } finally {
                        diagBusy = false
                    }
                }
            }) { Text(str(R.string.settings_report)) }
        }
    }
    item { HintMore(str(R.string.settings_about, BuildConfig.VERSION_NAME)) }
    item { Hint(str(R.string.settings_updates_play)) }
}
