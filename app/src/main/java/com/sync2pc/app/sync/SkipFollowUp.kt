package com.sync2pc.app.sync

/**
 * Folgelauf nach übersprungenen Dateien (SYNC-20261111-173): Wurden Dateien während des Laufs geändert und deshalb
 * übersprungen ([com.sync2pc.app.data.SyncRun.deferred] > 0), folgt nach [DELAY_MINUTES] Minuten ein automatischer Lauf.
 * Gegen Endlosschleifen (Datei ändert sich laufend) höchstens einer pro Stunde; danach übernehmen die regulären Auslöser.
 */
object SkipFollowUp {
    const val DELAY_MINUTES = 5L
    const val MIN_INTERVAL_MS = 3_600_000L

    /**
     * Darf jetzt ein Folgelauf geplant werden? [lastAt] = Zeitpunkt der letzten Planung (0 = nie).
     * [changedDuringRun] (SYNC-20261111-213): der Ordner hat sich während des Laufs verändert (neue Datei, die nicht in der Liste
     * stand) – gleicher Folgelauf, gleiche Begrenzung auf einen pro Stunde.
     */
    fun mayPlan(deferred: Int, now: Long, lastAt: Long, changedDuringRun: Boolean = false, folderIdleUntil: Long = 0): Boolean =
        (deferred > 0 || (changedDuringRun && !folderIdle(now, folderIdleUntil))) &&
            (lastAt <= 0 || now < lastAt || now - lastAt >= MIN_INTERVAL_MS)

    /** Dauer der Sperre für Folgeläufe allein wegen des Ordnerstempels nach einem leeren Folgelauf (SYNC-20261114-334, Punkt 5) */
    const val FOLDER_IDLE_MS = 6L * 3_600_000L

    /**
     * Gilt die Sperre noch? Nur bis [until] und höchstens [FOLDER_IDLE_MS] in der Zukunft (wurde die Uhr zurückgestellt, endet sie).
     * Betrifft nur den Auslöser «Ordner verändert»; übersprungene Dateien (deferred > 0) planen weiter.
     */
    fun folderIdle(now: Long, until: Long): Boolean = until > 0 && now < until && until - now <= FOLDER_IDLE_MS

    /**
     * Neuer Wert der Sperre nach einem Lauf (0 = keine). Hat der Lauf etwas getan ([didWork]: übertragen, Papierkorb, übersprungen,
     * Konflikte, Fehler), endet die Sperre sofort. Ein leerer Folgelauf ([wasSkipFollowUp]) setzt sie; sonst bleibt sie wie sie ist.
     * Grund: Apps, die im Profilordner laufend gefilterte Dateien anlegen und löschen (Caches), lösten sonst stündlich leere Läufe aus.
     */
    fun idleUntilAfter(current: Long, now: Long, wasSkipFollowUp: Boolean, didWork: Boolean): Long = when {
        didWork -> 0L
        wasSkipFollowUp -> now + FOLDER_IDLE_MS
        else -> current
    }
}
