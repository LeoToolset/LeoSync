package com.sync2pc.app.ui

import com.sync2pc.app.sync.NetworkRules

/**
 * Überschrift der Statuskarte auf der Startseite (SYNC-20261110-113, A1/A2/A4/A9).
 * Die Überschrift folgt dem Gesamtzustand: «alles gesichert» erscheint nur, wenn nichts offen ist,
 * eine Verbindung besteht und mindestens ein aktives Profil existiert.
 */
internal enum class HomeHeadline {
    /** Synchronisierung läuft */
    RUNNING,
    /** Kein Profil angelegt */
    NOT_CONFIGURED,
    /** Profile vorhanden, aber keines aktiv bzw. vollständig eingerichtet */
    NO_ACTIVE_PROFILE,
    /** Konflikt, Entscheidung, Schutzbremse oder Löschbestätigung offen */
    ATTENTION,
    /** Pausiert (Benutzer, «nur manuell» oder WLAN-Unterbruch) */
    PAUSED,
    /** Soll-WLAN nicht verbunden oder PC nicht erreichbar */
    NO_CONNECTION,
    /** Fehler (Stufen 1 bis 3, fehlende Pflichtberechtigung, offener Konflikt), SYNC-20261115-409 */
    ERRORS,
    /** Nur Meldungen ohne Fehler und Konflikt (Monatsordner, Löschbestätigung, vorübergehende Probleme), SYNC-20261115-409 */
    MESSAGES,
    /** Dateien wurden während des Laufs geändert und folgen beim nächsten Sync (SYNC-20261111-177): weder Fehler noch «aktuell» */
    PENDING,
    /** Alles gesichert und aktuell */
    UP_TO_DATE,
    /** Sonst: neutral, wartet */
    IDLE,
}

/** Eingaben der Statusermittlung (alles bereits gelesen, damit die Logik rein und testbar bleibt) */
internal data class HomeStatusInput(
    val isRunning: Boolean,
    val hasProfiles: Boolean,
    /** Mindestens ein aktives, vollständig eingerichtetes Profil */
    val hasActiveProfile: Boolean,
    /** Anzahl offener Entscheidungen (Konflikte, Ordnerzuordnung, Schutzbremse, Löschbestätigung) */
    val attentionCount: Int,
    val noConnection: Boolean,
    val userPaused: Boolean,
    val wifiPaused: Boolean,
    val lastRunHadErrors: Boolean,
    /** Letzter Lauf ohne Probleme und nichts mehr offen (wie bisher berechnet) */
    val allUpToDate: Boolean,
    /** Dateien, die laut dem letzten Lauf je aktivem Ordner-Profil beim nächsten Sync folgen (SYNC-20261111-177) */
    val pendingFiles: Int = 0,
    /** Ein aktives Profil wurde noch nie erfolgreich gestartet (SYNC-20261111-188 B2): nie «alles gesichert» */
    val awaitingFirstStart: Boolean = false,
    /** Der letzte Start (auto oder manuell) wurde wegen PC/WLAN nicht ausgeführt und ist nicht älter als 24 h (B1) */
    val recentSkip: Boolean = false,
    /** Ein Sync-Auftrag ist fällig, kann aber nicht starten (Queue nicht leer) */
    val queuedWork: Boolean = false,
    /** Anzahl Gruppen der Stufen 1 bis 3 plus fehlende Pflichtberechtigung (bei irgendeinem aktiven Profil); Konflikte zählen nicht mehr */
    val errorCount: Int = 0,
    /** Anzahl übriger Gruppen (Stufe 5); Stufe 4 steht in [attentionCount] */
    val messageCount: Int = 0,
)

internal object HomeStatus {
    /** Seit 409: läuft > nichts eingerichtet > Fehler > Meldungen > kein aktives Profil > pausiert > keine Verbindung > ausstehende Dateien > gesichert > neutral (die Zeilen darunter gelten für die älteren Eingaben weiter). Früher: läuft > nichts eingerichtet > wartet auf dich > pausiert > keine Verbindung > Fehler > Start nicht ausgeführt (zählt als keine Verbindung) > ausstehende Dateien > gesichert (nur wenn jedes aktive Profil schon einmal lief) > neutral */
    fun headline(i: HomeStatusInput): HomeHeadline = when {
        i.isRunning -> HomeHeadline.RUNNING
        !i.hasProfiles -> HomeHeadline.NOT_CONFIGURED
        i.errorCount > 0 -> HomeHeadline.ERRORS
        // SYNC-20261115-416 A: «Wartet auf dich» (nur Stufe 4: Konflikt, Entscheidung, Löschbestätigung), Konflikte sind kein Fehler mehr
        i.attentionCount > 0 -> HomeHeadline.ATTENTION
        i.messageCount > 0 -> HomeHeadline.MESSAGES
        !i.hasActiveProfile -> HomeHeadline.NO_ACTIVE_PROFILE
        i.userPaused || i.wifiPaused -> HomeHeadline.PAUSED
        i.noConnection -> HomeHeadline.NO_CONNECTION
        // Arbeit in der Queue, Start wegen PC/WLAN nicht ausgeführt: wartend (sitzender Hund, blau), nicht Fehler (rot)
        i.recentSkip && (i.pendingFiles > 0 || i.queuedWork) -> HomeHeadline.NO_CONNECTION
        i.lastRunHadErrors -> HomeHeadline.ERRORS
        i.recentSkip -> HomeHeadline.NO_CONNECTION
        i.pendingFiles > 0 -> HomeHeadline.PENDING
        i.allUpToDate && !i.awaitingFirstStart -> HomeHeadline.UP_TO_DATE
        else -> HomeHeadline.IDLE
    }

    /** Hundebild zur Überschrift */
    fun dogFor(h: HomeHeadline, userPaused: Boolean): DogState = when (h) {
        HomeHeadline.RUNNING -> DogState.RUNNING
        HomeHeadline.ATTENTION, HomeHeadline.ERRORS -> DogState.ATTENTION
        HomeHeadline.PAUSED -> if (userPaused) DogState.PAUSED else DogState.WAITING
        HomeHeadline.UP_TO_DATE -> DogState.SLEEPING
        HomeHeadline.NOT_CONFIGURED, HomeHeadline.NO_ACTIVE_PROFILE, HomeHeadline.MESSAGES,
        HomeHeadline.NO_CONNECTION, HomeHeadline.IDLE, HomeHeadline.PENDING -> DogState.WAITING
    }

    /** Symbol (Warnzeichen) vor der Überschrift: nie nur Farbe (A4) */
    fun showsWarning(h: HomeHeadline): Boolean = when (h) {
        HomeHeadline.ATTENTION, HomeHeadline.NO_CONNECTION, HomeHeadline.ERRORS, HomeHeadline.NO_ACTIVE_PROFILE -> true
        else -> false
    }

    /** Kann das Profil im aktuellen Netz laufen? Seit 0.19.0 hängt das nur von der erlaubten Netzart ab (kein WLAN-Name mehr). */
    fun profileCanRunNow(onAllowedNetwork: Boolean): Boolean = onAllowedNetwork

    /** Keine Verbindung: PC wurde zuletzt nicht erreicht oder kein aktives Profil kann im aktuellen Netz laufen */
    fun noConnection(activeProfileCount: Int, anyCanRunNow: Boolean, pcUnreachable: Boolean): Boolean =
        activeProfileCount > 0 && (pcUnreachable || !anyCanRunNow)
}
