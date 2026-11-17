package com.sync2pc.app.mirror

import java.text.Normalizer

/**
 * Namensregeln des Ordner-Syncs (SYNC-20261114-337, B5/B6), rein und ohne Android (getestet in NameRulesTest).
 * Grundsatz: nie still umbenennen; ungültige Namen werden übersprungen und gemeldet, Kollisionen nur gemeldet.
 */
object WindowsNames {
    enum class Reason { INVALID_CHAR, TRAILING_DOT_OR_SPACE, RESERVED }

    private val RESERVED = setOf("CON", "PRN", "AUX", "NUL") +
        (1..9).map { "COM$it" } + (1..9).map { "LPT$it" }
    private const val FORBIDDEN = "<>:\"/\\|?*"

    /** Warum ist [name] (ein einzelner Pfadteil) auf einem Windows-PC nicht erlaubt? null = erlaubt */
    fun invalidReason(name: String): Reason? {
        if (name.isEmpty()) return null
        if (name.any { it.code < 32 || it in FORBIDDEN }) return Reason.INVALID_CHAR
        if (name.endsWith(' ') || name.endsWith('.')) return Reason.TRAILING_DOT_OR_SPACE
        // Reservierte Gerätenamen gelten auch mit Endung («nul.txt») und mit Leerzeichen vor dem Punkt
        val base = name.substringBefore('.').trimEnd(' ').uppercase()
        if (base in RESERVED) return Reason.RESERVED
        return null
    }

    /** Erster ungültiger Teil eines relativen Pfads ("/" oder "\" als Trenner) und der Grund; null = alle Teile erlaubt */
    fun invalidPart(rel: String): Pair<String, Reason>? {
        for (part in rel.split('/', '\\')) invalidReason(part)?.let { return part to it }
        return null
    }
}

/** Namenskollisionen innerhalb einer Seite (nur melden, die Planung bleibt unverändert; keine Normalisierung der Schlüssel) */
object NameCollisions {
    enum class Kind { CASE_ONLY, NORMALIZATION }
    data class Collision(val a: String, val b: String, val kind: Kind)

    /**
     * Paare von Pfaden, die sich nur in Gross-/Kleinschreibung unterscheiden, bzw. nach Unicode-NFC gleich, aber verschieden
     * geschrieben sind (NFC/NFD). Ergebnis sortiert; je Gruppe aufeinanderfolgende Paare.
     */
    fun find(paths: Collection<String>): List<Collision> {
        val out = ArrayList<Collision>()
        val distinct = paths.toSortedSet()
        fun pairs(groups: Collection<List<String>>, kind: Kind) {
            for (g in groups) if (g.size > 1) for (i in 1 until g.size) out += Collision(g[i - 1], g[i], kind)
        }
        pairs(distinct.groupBy { it.lowercase() }.values, Kind.CASE_ONLY)
        // NFC-Gleichheit ohne die reinen Gross-/Kleinschreibungs-Paare (schon oben gemeldet): Gruppe nach exakter NFC-Form
        pairs(distinct.groupBy { Normalizer.normalize(it, Normalizer.Form.NFC) }.values, Kind.NORMALIZATION)
        return out
    }
}

/** Speicherorte ohne Änderungszeit (R-M1): Schwelle für den einmaligen Hinweis je Profil */
object ZeroTimeRule {
    const val MIN_FILES = 5
    const val SHARE_PERCENT = 90

    /** true, wenn mindestens [MIN_FILES] Dateien gelesen wurden und mindestens [SHARE_PERCENT] % davon die Zeit 0 haben */
    fun applies(modifiedTimes: Collection<Long>): Boolean {
        if (modifiedTimes.size < MIN_FILES) return false
        val zero = modifiedTimes.count { it <= 0L }
        return zero * 100 >= modifiedTimes.size * SHARE_PERCENT
    }
}
