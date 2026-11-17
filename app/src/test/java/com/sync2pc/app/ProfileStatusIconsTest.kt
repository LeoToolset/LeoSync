package com.sync2pc.app

import com.sync2pc.app.ui.ProfileIcon
import com.sync2pc.app.ui.ProfileStatusIcons
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Statussymbole der Profilkachel (SYNC-20261112-285, ohne WLAN-Symbol seit SYNC-20261115-382): alle 4 Kombinationen PC/Verschlüsselt,
 * feste Reihenfolge (PC, Schild) und keine Lücke. Nicht abgedeckt: Grösse, Ausrichtung und Farben (nur am Gerät prüfbar).
 */
class ProfileStatusIconsTest {
    private val P = ProfileIcon.PC
    private val E = ProfileIcon.ENCRYPTED

    @Test fun none() = assertEquals(emptyList<ProfileIcon>(), ProfileStatusIcons.visible(false, false))
    @Test fun pcOnly() = assertEquals(listOf(P), ProfileStatusIcons.visible(true, false))
    @Test fun encryptedOnly() = assertEquals(listOf(E), ProfileStatusIcons.visible(false, true))
    @Test fun pcAndEncrypted() = assertEquals(listOf(P, E), ProfileStatusIcons.visible(true, true))
}