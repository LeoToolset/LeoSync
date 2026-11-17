package com.sync2pc.app.ui

/**
 * Reine Regeln der Blätterkachel «Meldungen» (SYNC-20261115-407, JVM-getestet in MessagePagerTest): Reihenfolge liefert die Datenseite
 * ([com.sync2pc.app.sync.ProblemGrouping]), die Kachel sortiert nicht selbst.
 */
internal object MessagePager {
    /**
     * Färbung der ganzen Kachel nach der höchsten Schwere aller Einträge (SYNC-20261115-416 B): Stufen 1 bis 3 Fehler, 4 und 5 Warnung,
     * 6 (Tipps, Checkliste) Information ohne Färbung. Farbe nie allein: Etikett und Symbol tragen den Zustand als Text.
     */
    enum class Severity { ERROR, WARNING, INFO }

    fun severityOf(levels: List<Int>): Severity = when {
        levels.any { it <= 3 } -> Severity.ERROR
        levels.any { it in 4..5 } -> Severity.WARNING
        else -> Severity.INFO
    }

    /** Import-Checkliste: erst wenn kein Profil mehr einen Ordner- oder Verbindungstest-Schritt offen hat, kommt die zusammengefasste Gruppe */
    fun checklistSummaryOnly(rows: List<ChecklistStep>): Boolean = rows.isNotEmpty() && rows.all { it == ChecklistStep.SYNC }

    /** Seitenindex: die gezeigte Seite bleibt per Schlüssel erhalten; fehlt sie (erledigt), ist es die erste */
    fun indexOf(keys: List<String>, shownKey: String?): Int {
        if (keys.isEmpty()) return 0
        val i = if (shownKey == null) -1 else keys.indexOf(shownKey)
        return if (i >= 0) i else 0
    }

    /** Eine Seite vor/zurück; am Ende bzw. Anfang bleibt der Index (Pfeil dort deaktiviert) */
    fun step(index: Int, delta: Int, count: Int): Int = if (count <= 0) 0 else (index + delta).coerceIn(0, count - 1)

    fun canPrev(index: Int): Boolean = index > 0
    fun canNext(index: Int, count: Int): Boolean = index < count - 1

    /**
     * Ist seit dem letzten Stand eine **wichtigere** Seite dazugekommen als die gezeigte? Dann Hinweis mit «Anzeigen» statt
     * automatischem Springen. [levels] = Stufe je Schlüssel (1 am wichtigsten), [seen] = bereits bekannte Schlüssel.
     * Liefert den Schlüssel der wichtigsten neuen Seite, sonst null.
     */
    fun moreImportantArrived(keys: List<String>, levels: Map<String, Int>, shownKey: String?, seen: Set<String>): String? {
        val shownLevel = shownKey?.let { levels[it] } ?: return null
        return keys.filter { it !in seen && it != shownKey && (levels[it] ?: Int.MAX_VALUE) < shownLevel }
            .minByOrNull { levels[it] ?: Int.MAX_VALUE }
    }
}
