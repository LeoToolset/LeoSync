package com.sync2pc.app.mirror

import android.content.Context

/**
 * Reine Entscheidungen zur Übertragungsintegrität im Ordner-Sync (SYNC-20261114-333), ohne Android (getestet in
 * TransferIntegrityTest). Grundsatz: im Zweifel neu beginnen bzw. zurückstellen, nie fortsetzen oder überschreiben.
 */
object TransferIntegrity {
    /**
     * Ab dieser Grösse wird jede Übertragung (beide Richtungen) vor dem Umbenennen der Temp-Datei per Teil-Hash geprüft
     * (R-T2). Darunter genügt die Grössenprüfung: Bei sehr vielen kleinen Dateien wäre die Zusatzlesung (3 × 64 KB je Datei auf
     * beiden Seiten) teurer als der Nutzen.
     */
    const val HASH_MIN_BYTES = 4L * 1024 * 1024

    /** Grösse und Änderungszeit (ms) einer Datei, z. B. der PC-Datei beim Start eines Downloads */
    data class Stamp(val size: Long, val modified: Long)

    fun stampOf(e: LocalEntry) = Stamp(e.size, e.modified)

    /**
     * Darf eine angefangene Temp-Datei ([tmpSize] Byte) fortgesetzt werden? Nur mit gespeicherter Identität der PC-Datei vom
     * Neubeginn ([stored]), die der aktuellen ([current]) genau entspricht (R-T1/R-I6); der Uhrenvergleich mit der Handy-Uhr
     * entfällt. Keine Zeit (0) = nie fortsetzen.
     */
    fun mayResume(tmpSize: Long, stored: Stamp?, current: Stamp): Boolean =
        stored != null && stored == current && current.modified > 0 && tmpSize > 0 && tmpSize < current.size

    /** Teil-Hash vor dem Umbenennen nötig? Nach jedem Fortsetzen und bei Dateien ab [HASH_MIN_BYTES] */
    fun needsHashCheck(size: Long, resumed: Boolean): Boolean = resumed || size >= HASH_MIN_BYTES

    /**
     * Ist das Ziel unmittelbar vor dem Ersetzen noch so wie beim Einlesen (R-I5)? [expected] = null: die Datei durfte nicht
     * existieren (auch nach dem eigenen Papierkorb-Verschieben dieses Laufs); [now] = null: es gibt sie nicht.
     */
    fun targetUnchanged(expected: Stamp?, now: Stamp?): Boolean =
        if (expected == null || now == null) expected == null && now == null else expected == now

    /** Gilt eine gespeicherte Konfliktentscheidung noch für die aktuellen Versionen (R-K3)? */
    fun conflictStillCurrent(localSize: Long, localModified: Long, remoteSize: Long, remoteModified: Long, l: Stamp, r: Stamp): Boolean =
        localSize == l.size && localModified == l.modified && remoteSize == r.size && remoteModified == r.modified

    /** Schlüssel des Identitätsspeichers: Profil-ID und relativer Pfad in Kleinbuchstaben */
    fun identityKey(profileId: Long, rel: String): String = "$profileId|${rel.lowercase()}"

    fun encode(s: Stamp): String = "${s.size}|${s.modified}"

    /** Unlesbarer oder fremder Wert = keine Identität (sicher: neu beginnen) */
    fun decode(v: String?): Stamp? {
        val p = v?.split('|') ?: return null
        if (p.size != 2) return null
        val size = p[0].toLongOrNull() ?: return null
        val modified = p[1].toLongOrNull() ?: return null
        return Stamp(size, modified)
    }

    /** Kopie ist nur dann ein gültiger Ersatz des Originals, wenn sie existiert und genau [expectedSize] gross ist (R-I7) */
    fun copyIsComplete(copySize: Long?, expectedSize: Long): Boolean = copySize != null && copySize == expectedSize
}

/**
 * Dauerhafte Identität der PC-Datei beim Neubeginn eines Downloads (R-T1): kleiner Speicher in SharedPreferences mit commit(),
 * keine Datenbank. Einträge ohne Temp-Datei sind harmlos und werden beim nächsten Download desselben Pfads überschrieben.
 */
class DownloadIdentities(context: Context) {
    private val prefs = context.getSharedPreferences("download_identity", Context.MODE_PRIVATE)

    fun get(key: String): TransferIntegrity.Stamp? = TransferIntegrity.decode(prefs.getString(key, null))

    /** true = dauerhaft gespeichert; sonst darf nicht neu begonnen werden (Fortsetzen wäre ohne Eintrag ohnehin ausgeschlossen) */
    fun put(key: String, s: TransferIntegrity.Stamp): Boolean = prefs.edit().putString(key, TransferIntegrity.encode(s)).commit()

    fun remove(key: String) { prefs.edit().remove(key).commit() }
}
