package com.sync2pc.app.mirror

/**
 * Erkennung von Umbenennen und Verschieben im Ordner-Sync (SYNC-20261114-340, B2), rein und ohne Android (getestet in
 * RenameMatcherTest). Erkannt wird nur, wo heute eine Löschung übernommen würde («Löschungen übernehmen»): Die Planung enthält
 * für den alten Namen eine Löschung der Gegenseite und für den neuen Namen eine Übertragung derselben Richtung.
 *
 * Bezeichnungen: «A» ist die Seite, auf der umbenannt wurde (Handy oder PC), «B» die Gegenseite. Die Inhaltsprüfung
 * (PartialHash.thorough) und die Ausführung macht die Engine; hier nur die Paarbildung.
 */
object RenameMatcher {
    data class Stamp(val size: Long, val modified: Long)

    /** Alte Datei: fehlt auf A, liegt auf B noch unverändert; [aStamp] = Stand von A im Sync-Stand */
    data class Old(val key: String, val rel: String, val aStamp: Stamp)

    /** Neue Datei: gibt es auf A ohne Sync-Stand, auf B fehlt sie; [aNow] = ihre Werte jetzt auf A */
    data class New(val key: String, val rel: String, val aNow: Stamp)

    data class Pair(val old: Old, val new: New)

    /** Ganzer Ordner [from] -> [to] (relative Pfade in Originalschreibweise); [pairs] = alle Dateien darunter */
    data class DirRename(val from: String, val to: String, val pairs: List<Pair>)

    data class Result(val dirs: List<DirRename>, val files: List<Pair>)

    /** Zeit 0 (Speicherort ohne Änderungszeit, R-M1) und leere Dateien bilden nie ein Paar */
    private fun usable(s: Stamp) = s.modified > 0 && s.size > 0

    /**
     * Paare (2a): gleiche Grösse UND gleiche Änderungszeit wie der Sync-Stand der alten Datei auf A, und zwar genau ein Kandidat in
     * beide Richtungen (mehrdeutig = kein Paar). Die übrigen Bedingungen (Gegenseite unverändert, keine Konfliktzeile, Filter) prüft
     * der Aufrufer beim Bilden von [olds] und [news].
     */
    fun match(olds: List<Old>, news: List<New>): List<Pair> {
        val oldsByStamp = olds.filter { usable(it.aStamp) }.groupBy { it.aStamp }
        val newsByStamp = news.filter { usable(it.aNow) }.groupBy { it.aNow }
        val out = ArrayList<Pair>()
        for ((stamp, o) in oldsByStamp) {
            val n = newsByStamp[stamp] ?: continue
            if (o.size == 1 && n.size == 1) out += Pair(o[0], n[0])
        }
        return out.sortedBy { it.new.key }
    }

    /**
     * Ordner (2b): Der Ordner X verschwindet auf A, und ALLE Dateien des Sync-Stands darunter ([stateKeys], Schlüssel in
     * Kleinbuchstaben) sind als Paare zu Dateien unter einem neuen Ordner Y mit demselben relativen Rest erkannt; auf B liegen unter X
     * keine weiteren Dateien ([otherKeys]), und Y gibt es auf B noch nicht ([otherDirExists]). Dann wird der ganze Ordner umbenannt
     * (kürzester passender X); sonst bleiben die Einzeldatei-Paare.
     */
    fun group(
        pairs: List<Pair>, stateKeys: Collection<String>, otherKeys: Collection<String>, otherDirExists: (String) -> Boolean,
    ): Result {
        val byOldKey = pairs.associateBy { it.old.key }
        val dirs = LinkedHashMap<String, DirRename>()
        val covered = HashSet<String>()
        for (p in pairs.sortedBy { it.old.rel.count { c -> c == '/' } }) {
            if (p.old.key in covered) continue
            val parts = p.old.rel.split('/')
            for (depth in 1 until parts.size) {
                val x = parts.take(depth).joinToString("/")
                val rest = parts.drop(depth).joinToString("/")
                val newRel = p.new.rel
                if (!newRel.endsWith("/$rest") || newRel.length <= rest.length + 1) continue
                val y = newRel.substring(0, newRel.length - rest.length - 1)
                if (y.equals(x, ignoreCase = true) || y.lowercase().startsWith(x.lowercase() + "/") ||
                    x.lowercase().startsWith(y.lowercase() + "/")) continue
                val prefix = x.lowercase() + "/"
                val under = stateKeys.filter { it.startsWith(prefix) }
                if (under.isEmpty()) continue
                // alle Dateien des Stands unter X sind Paare mit gleichem Rest unter Y
                val members = under.map { byOldKey[it] ?: return@map null }
                if (members.any { it == null }) continue
                if (members.any { m -> m!!.new.rel != y + "/" + m.old.rel.substring(x.length + 1) }) continue
                // auf B keine weiteren Dateien unter X
                if (otherKeys.any { it.startsWith(prefix) && it !in under }) continue
                if (otherDirExists(y)) continue
                dirs[x] = DirRename(x, y, members.filterNotNull())
                members.forEach { covered += it!!.old.key }
                break
            }
        }
        // Verschachtelte Treffer (kürzeste gewinnt): wer schon unter einem gewählten Ordner liegt, entfällt
        val chosen = dirs.values.filter { d -> dirs.values.none { o -> o !== d && d.from.lowercase().startsWith(o.from.lowercase() + "/") } }
        val coveredKeys = chosen.flatMap { d -> d.pairs.map { it.old.key } }.toSet()
        return Result(chosen, pairs.filter { it.old.key !in coveredKeys })
    }
}
