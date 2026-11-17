package com.sync2pc.app.sync

import com.sync2pc.app.data.NumberInput

/**
 * Globales Prüfintervall «Auf Änderungen prüfen» der Ordner-Profile (SYNC-20261113-306), rein und ohne Android
 * (getestet in FolderCheckTest). Bisher hatte jedes Ordner-Profil ein eigenes Intervall, wirksam war ohnehin das kürzeste
 * aller aktiven Profile.
 */
object FolderCheck {
    /** Standard (Minuten), wenn es keine Ordner-Profile gab */
    const val DEFAULT_MINUTES = 60

    /** Wert in den zulässigen Bereich bringen (Bereich: [NumberInput.POLL_MINUTES]) */
    fun clamp(minutes: Int): Int = minutes.coerceIn(NumberInput.POLL_MINUTES.first, NumberInput.POLL_MINUTES.last)

    /**
     * Startwert beim ersten Start nach dem Update (und beim Einspielen einer Sicherung ohne den Wert): das kürzeste bisherige
     * Intervall der aktiven Ordner-Profile ([profileMinutes]), ohne solche [DEFAULT_MINUTES].
     */
    fun startValue(profileMinutes: List<Int>): Int = clamp(profileMinutes.minOrNull() ?: DEFAULT_MINUTES)

    /** Enthält die Sicherung [settings] das globale Prüfintervall nicht (ältere Sicherung), gilt beim Einspielen der Startwert */
    fun needsStartValue(settings: Map<String, Any>): Boolean = settings["folderCheckMinutes"] !is Number
}
