package com.sync2pc.app.sync

/**
 * Planung des Nachhol-Laufs für zu junge Fotos/Videos (SYNC-20261113-306), rein und ohne Android (getestet in FollowUpPlanTest).
 *
 * Ein Nachhol-Auftrag liegt auf dem Reifezeitpunkt der NEUSTEN jungen Datei, nicht der ältesten: So kommt eine ganze Serie
 * in einem Durchgang auf den PC, statt dass je Lauf nur ein Foto reift (Kette von Läufen). Nichts wird gespeichert: die
 * Zeiten stammen in jedem Lauf aus dem MediaStore, die Profil-IDs trägt der WorkManager-Auftrag (Auftragsdaten und Tags).
 */
object FollowUpPlan {
    /** Tag eines Nachhol-Auftrags je Fotoprofil mit wartenden Dateien (Wert: Profil-ID); dient dem Vereinigen beim Ersetzen */
    const val TAG_PREFIX = "followup-profile:"

    /** Reifezeitpunkte (ms) der jungen Dateien: ältester und neuester; 0 = keine junge Datei */
    data class Young(val oldestAt: Long = 0, val newestAt: Long = 0) {
        val any: Boolean get() = newestAt > 0
    }

    /** Zeitpunkt (ms), ab dem eine Datei das Mindestalter erreicht hat (jüngere der beiden Zeiten zählt) */
    fun readyAtMs(dateAddedSec: Long, dateModifiedSec: Long, minAgeSec: Long): Long =
        (maxOf(dateAddedSec, dateModifiedSec) + minAgeSec) * 1000

    /** Zu junge Dateien ([times] = Paare hinzugefügt/geändert in Sekunden) zum Zeitpunkt [nowSec]; Mindestalter 0 = nie jung */
    fun youngOf(times: List<Pair<Long, Long>>, nowSec: Long, minAgeSec: Long): Young {
        var oldest = 0L
        var newest = 0L
        for ((added, modified) in times) {
            if (nowSec - maxOf(added, modified) >= minAgeSec) continue
            val at = readyAtMs(added, modified, minAgeSec)
            if (oldest == 0L || at < oldest) oldest = at
            if (at > newest) newest = at
        }
        return Young(oldest, newest)
    }

    /** Zwei Bereiche (z. B. verschiedener Profile) zusammenfassen */
    fun combine(a: Young, b: Young): Young = when {
        !a.any -> b
        !b.any -> a
        else -> Young(minOf(a.oldestAt, b.oldestAt), maxOf(a.newestAt, b.newestAt))
    }

    /**
     * Frist des Nachhol-Laufs (ms, 0 = kein Lauf nötig): Reifezeit der neusten jungen Datei, bei während des Laufs entstandenen
     * Dateien ([createdDuringRunAt] = jetzt + Mindestalter, 0 = keine) auch später; nie vor der ältesten wartenden Datei.
     */
    fun dueAt(young: Young, createdDuringRunAt: Long): Long {
        val latest = maxOf(young.newestAt, createdDuringRunAt)
        return if (latest <= 0) 0 else maxOf(latest, young.oldestAt)
    }

    /** Lohnt sich der Nachhol-Lauf? Nur wenn mindestens ein Profil übertragungsbereite Dateien hat (sonst kein Lauf, kein Verlaufseintrag) */
    fun shouldRun(readyPerProfile: List<Int>): Boolean = readyPerProfile.any { it > 0 }

    /** IDs des ersetzten und des neuen Auftrags vereinigen (beim Neuplanen darf keine ID verloren gehen) */
    fun merge(existing: Set<Long>, added: Set<Long>): Set<Long> = existing + added

    fun tagOf(profileId: Long): String = TAG_PREFIX + profileId

    /** Profil-IDs aus den Tags eines Auftrags (fremde Tags und ungültige Werte werden ignoriert) */
    fun idsFromTags(tags: Set<String>): Set<Long> =
        tags.filter { it.startsWith(TAG_PREFIX) }.mapNotNull { it.removePrefix(TAG_PREFIX).toLongOrNull() }.toSet()
}
