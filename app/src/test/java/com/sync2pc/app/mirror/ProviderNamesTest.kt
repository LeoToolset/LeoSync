package com.sync2pc.app.mirror

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Regressionsschutz gegen Ordner-Duplikate («Testsync (1)», SYNC-20261115-374) */
class ProviderNamesTest {
    @Test fun renamedDetectsProviderSuffix() {
        assertTrue(ProviderNames.renamed("Testsync", "Testsync (1)"))
        assertFalse(ProviderNames.renamed("Testsync", "testsync"))
        assertFalse(ProviderNames.renamed("Testsync", null))
    }

    @Test fun childIdByPathForPathBasedIds() {
        assertEquals("primary:Download/A/B", ProviderNames.childIdByPath("primary:Download/A", "B"))
        assertEquals("primary:B", ProviderNames.childIdByPath("primary:", "B"))
        assertEquals("raw:/storage/emulated/0/Download/B", ProviderNames.childIdByPath("raw:/storage/emulated/0/Download", "B"))
    }

    @Test fun noPathForNumberIds() {
        assertNull(ProviderNames.childIdByPath("msd:12", "B"))
        assertNull(ProviderNames.childIdByPath("msf:690", "B"))
        assertNull(ProviderNames.childIdByPath("nocolon", "B"))
    }
}