package com.sync2pc.app

import com.sync2pc.app.mirror.Guard
import com.sync2pc.app.mirror.GuardKind
import com.sync2pc.app.mirror.VolumeCheck
import com.sync2pc.app.mirror.VolumeCheck.Action
import com.sync2pc.app.mirror.VolumeStamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** B12 (SYNC-20261114-341, Variante B): Kennzeichen des Laufwerks = Erstellungszeit des Freigabe-Stamms */
class VolumeCheckTest {
    private val fp = VolumeStamp.fingerprint("\\\\NAS\\Daten", "")
    private val t = 1_700_000_000L

    @Test fun firstTimeRemembersSilently() {
        assertEquals(Action.REMEMBER, VolumeCheck.decide(null, null, fp, t))
    }

    @Test fun sameStampProceeds() {
        assertEquals(Action.PROCEED, VolumeCheck.decide(fp, t, fp, t))
    }

    @Test fun differentStampHalts() {
        assertEquals(Action.HALT, VolumeCheck.decide(fp, t, fp, t + 5))
    }

    @Test fun pathOrShareChangeRemembersAgainInsteadOfHalting() {
        val other = VolumeStamp.fingerprint("\\\\NAS\\Andere", "")
        assertEquals(Action.REMEMBER, VolumeCheck.decide(fp, t, other, t + 999))
    }

    @Test fun noStatementNeverBlocksNorRemembers() {
        assertEquals(Action.PROCEED, VolumeCheck.decide(null, null, fp, null))
        assertEquals(Action.PROCEED, VolumeCheck.decide(fp, t, fp, null))
    }

    @Test fun unusableStampsAreNoStatement() {
        assertNull(VolumeStamp.fromMillis(null))
        assertNull(VolumeStamp.fromMillis(0))
        assertNull("Windows-Nullzeit 1601", VolumeStamp.fromMillis(-11_644_473_600_000L))
        assertNull("vor 1980", VolumeStamp.fromMillis(100_000L))
        assertEquals(1_700_000_000L, VolumeStamp.fromMillis(1_700_000_000_999L))
    }

    @Test fun fingerprintIgnoresCaseAndSlashesAndSeparatesPaths() {
        assertEquals(VolumeStamp.fingerprint("\\\\NAS\\Daten", "Fotos/"), VolumeStamp.fingerprint("\\\\nas\\daten ", "\\fotos"))
        assertNotEquals(VolumeStamp.fingerprint("\\\\nas\\daten", "a"), VolumeStamp.fingerprint("\\\\nas\\daten", "b"))
    }

    @Test fun guardKindIsStorable() {
        assertEquals(GuardKind.VOLUME_CHANGED, GuardKind.valueOf(Guard(1, GuardKind.VOLUME_CHANGED).kind.name))
    }
}