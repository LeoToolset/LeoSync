package com.sync2pc.app.sync

/**
 * Reine Entscheidungslogik zum Netzwerk (ohne Android-Klassen, daher als JVM-Test prüfbar).
 * [WifiHelper] liest die Netzeigenschaften und ruft diese Regeln auf (0.11.7, DEC-126).
 *
 * Seit 0.19.0 (SYNC-20261115-351) gibt es keine Prüfung des WLAN-Namens mehr (und keine Standort-Berechtigung): ob der PC der
 * richtige ist, entscheidet allein die PC-Kennung (vor der Anmeldung). Der Netz-Fingerabdruck («Bekannte Netze», [KnownNets])
 * steuert nur den Aufwand (Wartezeiten), nie das Senden der Zugangsdaten.
 */
object NetworkRules {
    /** Wartezeit nach "PC nicht erreichbar" bzw. Verbindungsabbruch, solange nur mobile Daten/VPN aktiv sind */
    const val OFF_WIFI_RETRY_MINUTES = 120L

    /** Wartezeit nach einem Verbindungsabbruch mitten im Lauf im WLAN (DEC-048) */
    const val MID_RUN_RETRY_MINUTES = 2L

    /**
     * WLAN immer; mobile Daten/VPN nur mit dem Schalter "syncOffWifi" und nie im Roaming (B7, SYNC-20261114-338: automatische Läufe
     * über Mobilfunk im Roaming wären teuer). Ein manueller Start ist davon nicht betroffen (prüft der Aufrufer nicht).
     */
    fun onAllowedNetwork(isWifi: Boolean, isMobileOrVpn: Boolean, switchOn: Boolean, roaming: Boolean = false): Boolean =
        isWifi || (switchOn && isMobileOrVpn && !roaming)

    /** Mobile Daten/VPN ohne WLAN bei eingeschaltetem Schalter und ausserhalb des Roamings (kein Netz-Fingerabdruck, höchstens zwei Versuche) */
    fun offWifiSyncActive(isMobileOrVpn: Boolean, switchOn: Boolean, roaming: Boolean = false): Boolean =
        switchOn && isMobileOrVpn && !roaming

    /** Wird ein automatischer Lauf nur wegen Roaming übersprungen? (für den Hinweis «Roaming …») */
    fun skippedForRoaming(isMobileOrVpn: Boolean, switchOn: Boolean, roaming: Boolean): Boolean =
        switchOn && isMobileOrVpn && roaming

    /**
     * Minuten bis zum nächsten Versuch, wenn der PC nicht erreichbar ist.
     * Nur mobile Daten/VPN: lange Wartezeit (schont Akku und Datenvolumen), auch nach Abbruch mitten im Lauf.
     */
    fun retryMinutes(offWifi: Boolean, midRun: Boolean): Long = when {
        offWifi -> OFF_WIFI_RETRY_MINUTES
        midRun -> MID_RUN_RETRY_MINUTES
        else -> SyncScheduler.SHARE_RETRY_MINUTES
    }

    /** Sicherheitsnetz «Bekannte Netze»: in einem unbekannten WLAN höchstens einmal je 24 h und Profil trotzdem alle Versuche */
    const val FULL_ATTEMPTS_INTERVAL_MS = 24 * 3600_000L

    /**
     * Wartezeiten (ms) vor jedem Erreichbarkeitsversuch; der erste Versuch läuft sofort.
     * [knownNet] = das aktuelle WLAN ist für das Profil ein bekanntes Netz (oder es gilt kein Fingerabdruck: mobile Daten/VPN, manueller
     * Start). Automatisch in einem unbekannten WLAN: nur ein Versuch ohne Wartezeit (Akku in fremden Netzen), ausser das Sicherheitsnetz
     * [fullAttemptsDue] greift (höchstens einmal je 24 h, sonst würde nach einem Router-Tausch nie neu gelernt).
     */
    fun reachabilityWaits(manual: Boolean, offWifi: Boolean, knownNet: Boolean = true, fullAttemptsDue: Boolean = false): List<Long> = when {
        manual -> listOf(0L, 3_000L)
        offWifi -> listOf(0L, 5_000L)
        !knownNet && !fullAttemptsDue -> listOf(0L)
        else -> listOf(0L, 5_000L, 10_000L, 15_000L, 30_000L)
    }

    /** Ist das Sicherheitsnetz (volle Versuche trotz unbekanntem Netz) fällig? [lastFullAt] 0 = noch nie. */
    fun fullAttemptsDue(now: Long, lastFullAt: Long): Boolean = lastFullAt <= 0L || now - lastFullAt >= FULL_ATTEMPTS_INTERVAL_MS
}
