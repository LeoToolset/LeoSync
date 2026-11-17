package com.sync2pc.app.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import androidx.core.content.ContextCompat

/**
 * Welche Berechtigungen gerade gebraucht werden (0.11.0: erst bei Bedarf fragen).
 * [media] = Fotos-Profil, [base] = Benachrichtigungen + Akku (sobald es Profile gibt).
 * Seit 0.19.0 (SYNC-20261115-351) keine Standort-Berechtigung mehr: die PC-Erkennung braucht keinen WLAN-Namen.
 * Standort in Fotos (ACCESS_MEDIA_LOCATION) gehört nicht zur Einrichtungskarte, er wird erst bei der Wahl der Länderordner erfragt (SYNC-20261115-354).
 */
data class PermNeeds(val media: Boolean, val base: Boolean) {
    companion object {
        val ALL = PermNeeds(media = true, base = true)
        fun of(profiles: List<com.sync2pc.app.data.Profile>) = PermNeeds(
            media = profiles.any { it.type() == com.sync2pc.app.data.ProfileType.PHOTOS },
            base = profiles.isNotEmpty(),
        )
    }
}

/** Fehlt etwas, das [needs] verlangt? */
fun PermStatus.missing(needs: PermNeeds): Boolean =
    (needs.media && !media) ||
        (needs.base && !notifications)

/**
 * Akku-Tipp (SYNC-20261115-382): nur ein Hinweis, keine Pflicht. Die rote Karte «Einrichtung unvollständig» erscheint nur bei
 * fehlenden Pflichtpunkten (Medien, Benachrichtigungen); die fehlende Akku-Ausnahme zeigt die neutrale Tipp-Karte, bis der Benutzer
 * sie mit «Nicht jetzt» zurückstellt (dauerhaft je Installation, kein Zeitablauf). Ist die Ausnahme gesetzt, wird der Merker
 * gelöscht, damit die Karte bei erneutem Fehlen wieder erscheint.
 */
object BatteryTip {
    private const val PREFS = "battery_tip"
    private const val KEY = "skipped"
    /** SYNC-20261116-434: Zeitpunkt der ersten Zurückstellung, des letzten Laufs ohne Vordergrund, endgültige Zurückstellung */
    private const val KEY_SKIPPED_AT = "skippedAt"
    private const val KEY_NO_FG_AT = "noForegroundAt"
    private const val KEY_FINAL = "finalSkipped"

    /**
     * Reine Entscheidung (getestet in BatteryTipTest): Tipp-Karte zeigen? Nie zurückgestellt: ja. Zurückgestellt: nur einmal erneut,
     * wenn danach ein Lauf ohne Vordergrund erkannt wurde ([eventAfterSkip]); nach der zweiten Zurückstellung ([finalSkipped]) nie mehr.
     */
    fun shouldShow(
        needsBase: Boolean, batteryUnrestricted: Boolean, skipped: Boolean,
        eventAfterSkip: Boolean = false, finalSkipped: Boolean = false,
    ): Boolean = needsBase && !batteryUnrestricted && (!skipped || (eventAfterSkip && !finalSkipped))

    /** Lauf ohne Vordergrund nach der ersten Zurückstellung? (Zeitstempel; 0 = nie) */
    fun eventAfterSkip(skippedAt: Long, noForegroundAt: Long): Boolean = skippedAt > 0 && noForegroundAt > skippedAt

    /**
     * SYNC-20261116-445: Öffnet den Ort der Akku-Einstellung von LeoSync. Zuerst die App-Info (führt auf allen Handys, auch mit
     * eigener Akkuverwaltung der Hersteller, zur App-Akkuseite; keine Berechtigung nötig, Play-konform). Rückfall auf die allgemeine
     * Liste der Akkuoptimierung; nie ein Absturz. Gemeinsam für Tipp-Karte, Tipp-Seite und Einstellungen.
     */
    fun openSettings(ctx: Context) {
        try {
            ctx.startActivity(
                android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, android.net.Uri.parse("package:${ctx.packageName}"))
                    .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: Exception) {
            try {
                ctx.startActivity(android.content.Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
            } catch (ignored: Exception) {}
        }
    }

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun skipped(ctx: Context): Boolean = try { prefs(ctx).getBoolean(KEY, false) } catch (e: Exception) { false }

    private fun eventAfterSkip(ctx: Context): Boolean = try {
        eventAfterSkip(prefs(ctx).getLong(KEY_SKIPPED_AT, 0), prefs(ctx).getLong(KEY_NO_FG_AT, 0))
    } catch (e: Exception) { false }

    /** «Nicht jetzt»: das erste Mal zurückstellen, bei der einmaligen Wiederholung (nach einem Lauf ohne Vordergrund) endgültig */
    fun skip(ctx: Context) {
        try {
            val e = prefs(ctx).edit()
            if (skipped(ctx) && eventAfterSkip(ctx)) e.putBoolean(KEY_FINAL, true)
            else e.putBoolean(KEY, true).putLong(KEY_SKIPPED_AT, System.currentTimeMillis())
            e.apply()
        } catch (ignored: Exception) {}
    }

    /** Ein Lauf ist ohne Vordergrund gelaufen (aus dem SyncWorker): Zeitpunkt merken */
    fun noteRanWithoutForeground(ctx: Context) {
        try { prefs(ctx).edit().putLong(KEY_NO_FG_AT, System.currentTimeMillis()).apply() } catch (ignored: Exception) {}
    }

    /** Karte erscheint zum zweiten Mal wegen eines Laufs ohne Vordergrund: Zusatz zum Grund zeigen */
    fun showsReason(ctx: Context): Boolean = skipped(ctx) && eventAfterSkip(ctx)

    /** Zeigt die Tipp-Karte? Löscht dabei die Merker, sobald die Ausnahme gesetzt ist */
    fun visible(ctx: Context, perms: PermStatus, needs: PermNeeds): Boolean {
        if (perms.batteryUnrestricted && (skipped(ctx) || prefs(ctx).contains(KEY_NO_FG_AT))) try { prefs(ctx).edit().clear().apply() } catch (ignored: Exception) {}
        val finalSkipped = try { prefs(ctx).getBoolean(KEY_FINAL, false) } catch (e: Exception) { false }
        return shouldShow(needs.base, perms.batteryUnrestricted, skipped(ctx), eventAfterSkip(ctx), finalSkipped)
    }
}

/** Überblick über alle Berechtigungen, die die App für den Hintergrund-Sync braucht. */
data class PermStatus(
    val media: Boolean,
    val mediaLocation: Boolean,
    val notifications: Boolean,
    val batteryUnrestricted: Boolean,
) {
    val allOk: Boolean
        get() = media && notifications &&
            batteryUnrestricted

    companion object {
        val mediaPermissions: Array<String>
            get() = when {
                Build.VERSION.SDK_INT >= 33 -> arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
                Build.VERSION.SDK_INT == 29 -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

        private fun granted(ctx: Context, p: String) =
            ContextCompat.checkSelfPermission(ctx, p) == PackageManager.PERMISSION_GRANTED

        fun read(ctx: Context): PermStatus = PermStatus(
            media = mediaPermissions.all { granted(ctx, it) },
            mediaLocation = granted(ctx, Manifest.permission.ACCESS_MEDIA_LOCATION),
            notifications = Build.VERSION.SDK_INT < 33 || granted(ctx, Manifest.permission.POST_NOTIFICATIONS),
            batteryUnrestricted = ctx.getSystemService(PowerManager::class.java)
                ?.isIgnoringBatteryOptimizations(ctx.packageName) == true,
        )
    }
}
