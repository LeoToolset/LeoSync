package com.sync2pc.app.sync

import android.content.Context

/**
 * «Bekannte Netze» (SYNC-20261114-338 Teil 3 / SYNC-20261115-351 Teil 4): Netz-Fingerabdruck je Profil, nur zur Aufwandssteuerung
 * (Akku in fremden WLANs). Er schafft kein Vertrauen und ersetzt nie die Prüfung der PC-Kennung (faelschbar, ohne Standort lesbar).
 *
 * Fingerabdruck ohne Zusatzberechtigung aus den LinkProperties des aktiven Netzes: IPv4-Gateway der Standardroute(n), IPv4-Präfix
 * der Schnittstelle (Netzadresse/Länge) und Suchdomäne. Bewusst NICHT enthalten: DNS-Server (wechseln per DHCP/Anbieter oder
 * Privat-DNS und sind kein stabiles Merkmal des Netzes) und der IPv6-Präfix des Anschlusses (wechselt bei manchen Anbietern).
 */
object KnownNets {
    /** Höchstens so viele Fingerabdrücke je Profil (z. B. Heimnetz und VPN); der älteste fällt weg */
    const val MAX = 5

    /**
     * Baut den Fingerabdruck. Null = keine brauchbare Aussage (weder Gateway noch Präfix lesbar): dann gilt kein Fingerabdruck,
     * das Verhalten bleibt wie ohne diese Funktion (volle Versuche, nichts lernen).
     */
    fun fingerprint(gateways: Collection<String>, ipv4Prefix: String?, domains: String?): String? {
        val gw = gateways.map { it.trim().lowercase() }.filter { it.isNotEmpty() }.distinct().sorted()
        val pfx = ipv4Prefix?.trim().orEmpty()
        if (gw.isEmpty() && pfx.isEmpty()) return null
        val dom = domains?.trim()?.lowercase().orEmpty()
        return "gw=" + gw.joinToString(",") + "|pfx=" + pfx + "|dom=" + dom
    }

    /** IPv4-Netzadresse mit Länge ("192.168.1.0/24") aus Adresse und Präfixlänge; null bei ungültiger Eingabe */
    fun ipv4Network(address: ByteArray, prefixLength: Int): String? {
        if (address.size != 4 || prefixLength !in 0..32) return null
        val ip = address.fold(0L) { acc, b -> (acc shl 8) or (b.toLong() and 0xFF) }
        val mask = if (prefixLength == 0) 0L else (0xFFFFFFFFL shl (32 - prefixLength)) and 0xFFFFFFFFL
        val net = ip and mask
        return "${(net shr 24) and 0xFF}.${(net shr 16) and 0xFF}.${(net shr 8) and 0xFF}.${net and 0xFF}/$prefixLength"
    }

    fun isKnown(known: List<String>, fingerprint: String?): Boolean = fingerprint != null && fingerprint in known

    /** Fügt den Fingerabdruck als neusten an (bekannte rücken ans Ende), höchstens [MAX]; der älteste fällt weg */
    fun learned(known: List<String>, fingerprint: String): List<String> =
        (known.filter { it != fingerprint } + fingerprint).takeLast(MAX)

    /** Entscheidung für den Worker: lernen und Verlaufseintrag «Netz geändert, neu gelernt»? */
    enum class Learn { NONE, SILENT_FIRST, CHANGED }

    /**
     * Nach bestätigter PC-Kennung im WLAN: nichts zu tun (bekannt oder kein Fingerabdruck), erstes Netz des Profils (still) oder
     * ein weiteres/neues Netz (Verlaufseintrag, Benutzerentscheid 4a: keine Rückfrage).
     */
    fun learnDecision(known: List<String>, fingerprint: String?): Learn = when {
        fingerprint == null || fingerprint in known -> Learn.NONE
        known.isEmpty() -> Learn.SILENT_FIRST
        else -> Learn.CHANGED
    }
}

/** Speicher der bekannten Netze je Profil (SharedPreferences, nicht in der Sicherung: ein neues Gerät lernt neu) */
object KnownNetStore {
    private const val PREFS = "known_nets"

    private fun prefs(ctx: Context) = ctx.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun list(ctx: Context, profileId: Long): List<String> =
        prefs(ctx).getString("p$profileId", "")?.split('\n')?.filter { it.isNotBlank() }.orEmpty()

    fun count(ctx: Context, profileId: Long): Int = list(ctx, profileId).size

    fun learn(ctx: Context, profileId: Long, fingerprint: String) {
        prefs(ctx).edit().putString("p$profileId", KnownNets.learned(list(ctx, profileId), fingerprint).joinToString("\n")).apply()
    }

    /**
     * Nach bestätigter PC-Kennung im WLAN ([fingerprint] null = kein WLAN oder nicht lesbar): Netz übernehmen, bei einem weiteren Netz
     * den Verlaufseintrag vormerken (der nächste Lauf schreibt ihn). Kein Vertrauensmerkmal.
     */
    fun confirmContact(ctx: Context, profileId: Long, fingerprint: String?) {
        if (fingerprint == null || profileId == 0L) return
        val learn = KnownNets.learnDecision(list(ctx, profileId), fingerprint)
        if (learn == KnownNets.Learn.NONE) return
        learn(ctx, profileId, fingerprint)
        if (learn == KnownNets.Learn.CHANGED) markNote(ctx, profileId)
    }

    /** Aktion «Vergessen» im Profil und beim Löschen des Profils */
    fun forget(ctx: Context, profileId: Long) {
        prefs(ctx).edit().remove("p$profileId").remove("full$profileId").remove("note$profileId").apply()
    }

    /** Verlaufseintrag «Netz geändert, neu gelernt» vormerken; der nächste Lauf des Profils schreibt ihn (vorher gibt es keinen Lauf) */
    fun markNote(ctx: Context, profileId: Long) {
        prefs(ctx).edit().putBoolean("note$profileId", true).apply()
    }

    /** true, wenn ein Verlaufseintrag vorgemerkt war (und löscht den Merker) */
    fun takeNote(ctx: Context, profileId: Long): Boolean {
        val p = prefs(ctx)
        if (!p.getBoolean("note$profileId", false)) return false
        p.edit().remove("note$profileId").apply()
        return true
    }

    /** Zeitpunkt der letzten Läufe mit vollen Versuchen in einem unbekannten Netz (Sicherheitsnetz, 0 = nie) */
    fun lastFullAttempts(ctx: Context, profileId: Long): Long = prefs(ctx).getLong("full$profileId", 0L)

    fun markFullAttempts(ctx: Context, profileId: Long, now: Long) {
        prefs(ctx).edit().putLong("full$profileId", now).apply()
    }
}
