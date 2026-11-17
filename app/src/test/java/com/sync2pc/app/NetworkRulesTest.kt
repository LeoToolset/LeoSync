package com.sync2pc.app

import com.sync2pc.app.sync.KnownNets
import com.sync2pc.app.sync.NetworkRules
import com.sync2pc.app.sync.SyncScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Netzwerk-Entscheidungen (DEC-126, 0.11.7): Schalter AUS/EIN × WLAN/Mobil/kein Netz; B7 Roaming; seit 0.19.0 (SYNC-20261115-351)
 * ohne WLAN-Namen, dafür «Bekannte Netze» (Netz-Fingerabdruck steuert nur die Wartezeiten, nie das Senden der Zugangsdaten).
 */
class NetworkRulesTest {

    @Test fun onAllowedNetworkTable() {
        // (isWifi, isMobileOrVpn, schalter) -> erwartet
        val cases = listOf(
            Triple(true, false, false) to true,   // WLAN, Schalter aus
            Triple(true, false, true) to true,    // WLAN, Schalter an
            Triple(false, true, false) to false,  // Mobil, Schalter aus (Verhalten wie vor 0.11.5)
            Triple(false, true, true) to true,    // Mobil, Schalter an
            Triple(false, false, false) to false, // kein Netz
            Triple(false, false, true) to false,  // kein Netz, Schalter an
        )
        for ((c, expected) in cases) {
            assertEquals("$c", expected, NetworkRules.onAllowedNetwork(c.first, c.second, c.third))
        }
    }

    @Test fun offWifiNeedsSwitchAndMobile() {
        assertTrue(NetworkRules.offWifiSyncActive(isMobileOrVpn = true, switchOn = true))
        assertFalse(NetworkRules.offWifiSyncActive(isMobileOrVpn = true, switchOn = false))
        assertFalse(NetworkRules.offWifiSyncActive(isMobileOrVpn = false, switchOn = true))
        assertFalse(NetworkRules.offWifiSyncActive(isMobileOrVpn = false, switchOn = false))
    }

    @Test fun retryMinutes() {
        assertEquals(120L, NetworkRules.retryMinutes(offWifi = true, midRun = false))
        assertEquals(120L, NetworkRules.retryMinutes(offWifi = true, midRun = true)) // auch nach Abbruch mitten im Lauf
        assertEquals(2L, NetworkRules.retryMinutes(offWifi = false, midRun = true))
        assertEquals(SyncScheduler.SHARE_RETRY_MINUTES, NetworkRules.retryMinutes(offWifi = false, midRun = false))
        assertEquals(30L, SyncScheduler.SHARE_RETRY_MINUTES)
    }

    private val full = listOf(0L, 5_000L, 10_000L, 15_000L, 30_000L)

    @Test fun reachabilityWaits() {
        assertEquals(listOf(0L, 3_000L), NetworkRules.reachabilityWaits(manual = true, offWifi = false))
        assertEquals(listOf(0L, 3_000L), NetworkRules.reachabilityWaits(manual = true, offWifi = true)) // manuell unverändert
        assertEquals(listOf(0L, 5_000L), NetworkRules.reachabilityWaits(manual = false, offWifi = true))
        assertEquals(full, NetworkRules.reachabilityWaits(manual = false, offWifi = false))
    }

    // ---- Bekannte Netze: unbekanntes WLAN = ein Versuch, Sicherheitsnetz einmal je 24 h ----
    @Test fun unknownWifiGetsOnlyOneAttemptUnlessSafetyNetIsDue() {
        assertEquals("bekanntes Netz: volle Versuche", full, NetworkRules.reachabilityWaits(manual = false, offWifi = false, knownNet = true))
        assertEquals("unbekanntes Netz: ein Versuch ohne Wartezeit", listOf(0L),
            NetworkRules.reachabilityWaits(manual = false, offWifi = false, knownNet = false, fullAttemptsDue = false))
        assertEquals("Sicherheitsnetz fällig: trotzdem volle Versuche", full,
            NetworkRules.reachabilityWaits(manual = false, offWifi = false, knownNet = false, fullAttemptsDue = true))
    }

    @Test fun manualStartAndMobileIgnoreTheFingerprint() {
        assertEquals(listOf(0L, 3_000L), NetworkRules.reachabilityWaits(manual = true, offWifi = false, knownNet = false))
        assertEquals(listOf(0L, 5_000L), NetworkRules.reachabilityWaits(manual = false, offWifi = true, knownNet = false))
    }

    @Test fun safetyNetDueOnceEvery24Hours() {
        val day = NetworkRules.FULL_ATTEMPTS_INTERVAL_MS
        assertEquals(24 * 3600_000L, day)
        assertTrue("noch nie volle Versuche", NetworkRules.fullAttemptsDue(now = 1_000L, lastFullAt = 0L))
        assertFalse(NetworkRules.fullAttemptsDue(now = 10 * 3600_000L, lastFullAt = 1_000L))
        assertFalse(NetworkRules.fullAttemptsDue(now = day + 999L, lastFullAt = 1_000L))
        assertTrue(NetworkRules.fullAttemptsDue(now = day + 1_000L, lastFullAt = 1_000L))
    }

    // ---- Netz-Fingerabdruck und Speicherregeln (KnownNets) ----
    @Test fun fingerprintIsStableAndOrderIndependent() {
        val a = KnownNets.fingerprint(listOf("192.168.1.1", "192.168.1.2"), "192.168.1.0/24", "fritz.box")
        val b = KnownNets.fingerprint(listOf("192.168.1.2", "192.168.1.1", "192.168.1.1"), "192.168.1.0/24", "FRITZ.BOX ")
        assertEquals(a, b)
    }

    @Test fun differentNetworksHaveDifferentFingerprints() {
        val home = KnownNets.fingerprint(listOf("192.168.1.1"), "192.168.1.0/24", "fritz.box")
        assertNotEquals(home, KnownNets.fingerprint(listOf("192.168.0.1"), "192.168.0.0/24", "fritz.box"))
        assertNotEquals("anderes Gateway", home, KnownNets.fingerprint(listOf("192.168.1.254"), "192.168.1.0/24", "fritz.box"))
        assertNotEquals("andere Suchdomäne", home, KnownNets.fingerprint(listOf("192.168.1.1"), "192.168.1.0/24", "lan"))
    }

    @Test fun nothingReadableMeansNoFingerprint() {
        assertNull(KnownNets.fingerprint(emptyList(), null, "fritz.box"))
        assertNull(KnownNets.fingerprint(listOf(" "), "", null))
        assertTrue("Präfix allein genügt", KnownNets.fingerprint(emptyList(), "10.0.0.0/8", null) != null)
    }

    @Test fun ipv4NetworkIsMaskedToThePrefix() {
        assertEquals("192.168.1.0/24", KnownNets.ipv4Network(byteArrayOf(192.toByte(), 168.toByte(), 1, 57), 24))
        assertEquals("10.0.0.0/8", KnownNets.ipv4Network(byteArrayOf(10, 4, 5, 6), 8))
        assertEquals("192.168.1.32/27", KnownNets.ipv4Network(byteArrayOf(192.toByte(), 168.toByte(), 1, 57), 27))
        assertEquals("0.0.0.0/0", KnownNets.ipv4Network(byteArrayOf(1, 2, 3, 4), 0))
        assertEquals("1.2.3.4/32", KnownNets.ipv4Network(byteArrayOf(1, 2, 3, 4), 32))
        assertNull("IPv6-Adresse", KnownNets.ipv4Network(ByteArray(16), 64))
        assertNull(KnownNets.ipv4Network(byteArrayOf(1, 2, 3, 4), 33))
    }

    @Test fun learnedKeepsAtMostFiveAndDropsTheOldest() {
        var list = emptyList<String>()
        for (i in 1..7) list = KnownNets.learned(list, "n$i")
        assertEquals(listOf("n3", "n4", "n5", "n6", "n7"), list)
        // ein bekanntes Netz rückt ans Ende und wird nie doppelt gespeichert
        list = KnownNets.learned(list, "n3")
        assertEquals(listOf("n4", "n5", "n6", "n7", "n3"), list)
        assertEquals(KnownNets.MAX, list.size)
    }

    @Test fun learnDecision() {
        assertEquals("kein Fingerabdruck: nichts tun", KnownNets.Learn.NONE, KnownNets.learnDecision(listOf("a"), null))
        assertEquals("bekannt: nichts tun", KnownNets.Learn.NONE, KnownNets.learnDecision(listOf("a", "b"), "b"))
        assertEquals("erstes Netz: still", KnownNets.Learn.SILENT_FIRST, KnownNets.learnDecision(emptyList(), "a"))
        assertEquals("weiteres Netz: mit Verlaufseintrag", KnownNets.Learn.CHANGED, KnownNets.learnDecision(listOf("a"), "b"))
    }

    @Test fun isKnownNeverTrueWithoutFingerprint() {
        assertFalse(KnownNets.isKnown(listOf("a"), null))
        assertTrue(KnownNets.isKnown(listOf("a"), "a"))
        assertFalse(KnownNets.isKnown(emptyList(), "a"))
    }

    // ---- B7 (SYNC-20261114-338): kein automatischer Lauf über Mobilfunk im Roaming ----
    @Test fun roamingBlocksMobileButNeverWifi() {
        assertFalse(NetworkRules.onAllowedNetwork(isWifi = false, isMobileOrVpn = true, switchOn = true, roaming = true))
        assertTrue("ohne Roaming wie bisher", NetworkRules.onAllowedNetwork(isWifi = false, isMobileOrVpn = true, switchOn = true, roaming = false))
        assertTrue("WLAN bleibt unverändert", NetworkRules.onAllowedNetwork(isWifi = true, isMobileOrVpn = false, switchOn = true, roaming = true))
        assertTrue("WLAN mit Roaming-Flag eines anderen Netzes", NetworkRules.onAllowedNetwork(isWifi = true, isMobileOrVpn = false, switchOn = false, roaming = true))
    }

    @Test fun roamingDisablesOffWifiSyncAndRetries() {
        assertFalse(NetworkRules.offWifiSyncActive(isMobileOrVpn = true, switchOn = true, roaming = true))
        assertTrue(NetworkRules.offWifiSyncActive(isMobileOrVpn = true, switchOn = true, roaming = false))
        assertFalse(NetworkRules.offWifiSyncActive(isMobileOrVpn = false, switchOn = true, roaming = false))
    }

    @Test fun roamingNoteOnlyWhenTheSwitchWouldOtherwiseAllowIt() {
        assertTrue(NetworkRules.skippedForRoaming(isMobileOrVpn = true, switchOn = true, roaming = true))
        assertFalse("Schalter aus: kein Roaming-Hinweis", NetworkRules.skippedForRoaming(isMobileOrVpn = true, switchOn = false, roaming = true))
        assertFalse(NetworkRules.skippedForRoaming(isMobileOrVpn = true, switchOn = true, roaming = false))
        assertFalse(NetworkRules.skippedForRoaming(isMobileOrVpn = false, switchOn = true, roaming = true))
    }
}
