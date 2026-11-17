package com.sync2pc.app.sync

import android.app.ActivityManager
import android.content.Context
import kotlinx.coroutines.delay

/**
 * SYNC-20261116-434: Hat der Lauf den Vordergrund tatsächlich erhalten?
 *
 * `setForeground` meldet die Ablehnung durch Android (Start aus dem Hintergrund, API 31+) nicht zuverlässig als Ausnahme; sie fällt im
 * WorkManager-Dienst an. Nachweis deshalb über die eigenen laufenden Dienste: [ActivityManager.getRunningServices] liefert ab API 26
 * nur die Dienste der eigenen App, [ActivityManager.RunningServiceInfo.foreground] ist wahr, sobald der Dienst `startForeground` erreicht hat.
 */
object ForegroundCheck {
    private const val SERVICE_SUFFIX = "SystemForegroundService"

    /** Reine Auswertung (getestet): Gibt es einen laufenden Dienst mit diesem Namen im Vordergrund? */
    fun anyForeground(services: List<Pair<String, Boolean>>): Boolean =
        services.any { (name, fg) -> name.endsWith(SERVICE_SUFFIX) && fg }

    /**
     * Wartet bis zu [maxMs] auf den Vordergrund (der Dienst startet asynchron). true = erteilt oder nicht feststellbar (dann keine
     * Fehlmeldung), false = nachweislich nicht im Vordergrund.
     */
    suspend fun granted(ctx: Context, maxMs: Long = 3_000, stepMs: Long = 250): Boolean {
        val am = ctx.getSystemService(ActivityManager::class.java) ?: return true
        var waited = 0L
        while (true) {
            val services = try {
                @Suppress("DEPRECATION")
                am.getRunningServices(Int.MAX_VALUE).map { it.service.className to it.foreground }
            } catch (e: Exception) { return true }
            if (anyForeground(services)) return true
            if (waited >= maxMs) return false
            delay(stepMs); waited += stepMs
        }
    }
}
