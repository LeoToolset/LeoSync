package com.sync2pc.app.sync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** SYNC-20261115-409: ProblemStore, ProblemGrouping (Schlüssel, Stufen, Sortierung, 24-h-Schwelle) */
class ProblemsTest {
    private class MapKv : ProblemKv {
        val m = LinkedHashMap<String, String>()
        override fun all(): Map<String, String> = m.toMap()
        override fun put(key: String, value: String) { m[key] = value }
        override fun remove(key: String) { m.remove(key) }
    }

    private val H = 3600_000L
    private val names = mapOf(1L to "Alpha", 2L to "Beta", 3L to "Gamma", 4L to "Delta")
    private fun pr(id: Long, kind: ProblemKind, pc: String = "host:nas", share: String = "", user: String = "", domain: String = "", detail: String = "", since: Long = 1000) =
        Problem(id, kind, pc, share, user, domain, detail, since, since)

    private fun group(list: List<Problem>, now: Long = 10 * 24 * H, lastOk: Map<Long, Long?> = emptyMap(), c: CollectedItems = CollectedItems()) =
        ProblemGrouping.group(list, names, lastOk, c, now)

    // ---- Speicher ----
    @Test fun storeRoundTripAndPerProfile() {
        val s = ProblemStore(MapKv())
        s.put(pr(1, ProblemKind.LOGIN, user = "ann", detail = "x"), 5000)
        s.put(pr(2, ProblemKind.PC_FOLDER_MISSING), 6000)
        assertEquals(ProblemKind.LOGIN, s.get(1)?.kind)
        assertEquals("ann", s.get(1)?.user)
        assertEquals(2, s.all().size)
    }

    @Test fun errorOfAStaysWhenBSucceeds() {
        val s = ProblemStore(MapKv())
        s.put(pr(1, ProblemKind.LOGIN), 1000)
        s.put(pr(2, ProblemKind.LOGIN), 1000)
        s.clear(2) // B läuft fehlerfrei
        assertNotNull(s.get(1))
        assertNull(s.get(2))
    }

    @Test fun sameKindKeepsSinceChangedKindResets() {
        val s = ProblemStore(MapKv())
        s.put(pr(1, ProblemKind.LOGIN), 1000)
        s.put(pr(1, ProblemKind.LOGIN), 9000)
        assertEquals(1000L, s.get(1)?.since)
        assertEquals(9000L, s.get(1)?.last)
        s.put(pr(1, ProblemKind.SHARE_MISSING), 9500)
        assertEquals(9500L, s.get(1)?.since)
    }

    @Test fun clearKindsOnlyRemovesMatching() {
        val s = ProblemStore(MapKv())
        s.put(pr(1, ProblemKind.PC_UNREACHABLE), 1000)
        s.clearKinds(1, setOf(ProblemKind.LOGIN)); assertNotNull(s.get(1))
        s.clearKinds(1, setOf(ProblemKind.PC_UNREACHABLE)); assertNull(s.get(1))
    }

    @Test fun decodeIgnoresBrokenEntries() {
        assertNull(Problem.decode(1, "kaputt"))
        assertNull(Problem.decode(1, "NOPE\u001F\u001F\u001F\u001F\u001F\u001F1\u001F1"))
    }

    // ---- Schlüssel ----
    @Test fun pcKeyPrefersGuidAndIsCaseInsensitive() {
        assertEquals("guid:abc", ProblemGrouping.pcKeyOf("ABC", "nas"))
        assertEquals(ProblemGrouping.pcKeyOf("ABC", "nas"), ProblemGrouping.pcKeyOf("abc", "192.168.1.5"))
        assertEquals("host:nas", ProblemGrouping.pcKeyOf("", "NAS"))
    }

    @Test fun scopePcGroupsAcrossUsersAndShares() {
        val g = group(listOf(pr(1, ProblemKind.PLAINTEXT, user = "a", share = "x"), pr(2, ProblemKind.PLAINTEXT, user = "b", share = "y")))
        assertEquals(1, g.size); assertEquals(2, g[0].profiles.size)
    }

    @Test fun scopeShareSplitsDifferentShares() {
        val g = group(listOf(pr(1, ProblemKind.SHARE_MISSING, share = "X"), pr(2, ProblemKind.SHARE_MISSING, share = "x"), pr(3, ProblemKind.SHARE_MISSING, share = "Y")))
        assertEquals(2, g.size)
        assertEquals(setOf(1L, 2L), g.first { it.profiles.size == 2 }.profiles.map { it.id }.toSet())
    }

    @Test fun differentUsersAreSeparateLogins() {
        val g = group(listOf(pr(1, ProblemKind.LOGIN, user = "ann", domain = "D"), pr(2, ProblemKind.LOGIN, user = "ANN", domain = "d"), pr(3, ProblemKind.LOGIN, user = "bob", domain = "D")))
        assertEquals(2, g.size)
        assertEquals(2, g.first { it.profiles.size == 2 }.profiles.size)
    }

    @Test fun differentPcsAreSeparate() {
        val g = group(listOf(pr(1, ProblemKind.LOGIN, pc = "host:a", user = "u"), pr(2, ProblemKind.LOGIN, pc = "host:b", user = "u")))
        assertEquals(2, g.size)
    }

    @Test fun deviceScopeJoinsAllPcs() {
        val g = group(listOf(pr(1, ProblemKind.PHONE_FULL, pc = "host:a"), pr(2, ProblemKind.PHONE_FULL, pc = "host:b")))
        assertEquals(1, g.size)
    }

    @Test fun profileScopeNeverJoins() {
        val g = group(listOf(pr(1, ProblemKind.GUARD), pr(2, ProblemKind.GUARD), pr(3, ProblemKind.PC_FOLDER_MISSING), pr(4, ProblemKind.PC_FOLDER_MISSING)))
        assertEquals(4, g.size)
    }

    @Test fun unknownErrorJoinsOnlyWithSameTextAndPc() {
        val g = group(listOf(pr(1, ProblemKind.UNKNOWN, detail = "Boom"), pr(2, ProblemKind.UNKNOWN, detail = "boom"), pr(3, ProblemKind.UNKNOWN, detail = "Other")))
        assertEquals(2, g.size)
    }

    @Test fun keyIsStableAcrossMembersTimeAndOrder() {
        val a = pr(1, ProblemKind.LOGIN, user = "ann", since = 1)
        val b = pr(2, ProblemKind.LOGIN, user = "ann", since = 99)
        val k1 = group(listOf(a)).single().key
        val k2 = group(listOf(a, b)).single().key
        val k3 = group(listOf(b, a), now = 20 * 24 * H).single().key
        assertEquals(k1, k2); assertEquals(k1, k3)
        assertEquals(ProblemGrouping.keyOf(a), ProblemGrouping.keyOf(b.copy(profileId = 7, since = 5, detail = "andere Meldung")))
    }

    // ---- Sortierung ----
    @Test fun sortsByLevelThenCountThenAgeThenName() {
        val list = listOf(
            pr(1, ProblemKind.PC_UNREACHABLE, since = 1),
            pr(2, ProblemKind.LOGIN, user = "a", since = 50),
            pr(3, ProblemKind.PLAINTEXT, since = 80),
            pr(4, ProblemKind.GUARD, since = 90),
            pr(1, ProblemKind.PC_FOLDER_MISSING, since = 10).copy(profileId = 3),
        )
        val g = group(list, c = CollectedItems(conflicts = 2, since = 5))
        assertEquals(listOf(1, 2, 3, 3, 4, 5), g.map { it.level })
        assertEquals(ProblemKind.GUARD, g[0].kind)
        assertEquals(ProblemKind.PLAINTEXT, g[1].kind)
        assertEquals(ProblemKind.CONFLICTS, g[4].kind)
    }

    @Test fun moreProfilesFirstWithinLevel() {
        val g = group(listOf(
            pr(1, ProblemKind.LOGIN, user = "single", since = 1),
            pr(2, ProblemKind.SHARE_MISSING, share = "S", since = 50), pr(3, ProblemKind.SHARE_MISSING, share = "S", since = 60),
        ))
        assertEquals(ProblemKind.SHARE_MISSING, g[0].kind)
    }

    @Test fun olderFirstWithinSameCount() {
        val g = group(listOf(pr(1, ProblemKind.LOGIN, user = "a", since = 500), pr(2, ProblemKind.SHARE_MISSING, share = "S", since = 100)))
        assertEquals(ProblemKind.SHARE_MISSING, g[0].kind)
    }

    // ---- 24-h-Schwelle ----
    @Test fun unreachableOnlyAfter24HoursWithoutSuccess() {
        val now = 100 * H
        val p = pr(1, ProblemKind.PC_UNREACHABLE, since = now - 1 * H)
        assertTrue(group(listOf(p), now = now, lastOk = mapOf(1L to now - 23 * H)).isEmpty())
        assertEquals(1, group(listOf(p), now = now, lastOk = mapOf(1L to now - 25 * H)).size)
    }

    @Test fun unreachableWithoutAnySuccessCountsFromSince() {
        val now = 100 * H
        assertTrue(group(listOf(pr(1, ProblemKind.PC_UNREACHABLE, since = now - 2 * H)), now = now).isEmpty())
        assertEquals(1, group(listOf(pr(1, ProblemKind.PC_UNREACHABLE, since = now - 30 * H)), now = now).size)
    }

    // ---- Überschrift ----
    @Test fun errorOnLevels1To3ButNotConflict() {
        assertTrue(ProblemGrouping.hasError(group(listOf(pr(1, ProblemKind.LOGIN)))))
        assertFalse(ProblemGrouping.hasError(group(emptyList(), c = CollectedItems(conflicts = 1))))
        assertFalse(ProblemGrouping.hasError(group(emptyList(), c = CollectedItems(monthFolders = 1, deleteConfirm = 2))))
    }

    @Test fun waitingForYouOnlyForLevel4() {
        assertTrue(ProblemGrouping.hasAttentionOnly(group(emptyList(), c = CollectedItems(conflicts = 1))))
        assertTrue(ProblemGrouping.hasAttentionOnly(group(emptyList(), c = CollectedItems(deleteConfirm = 1))))
        assertFalse(ProblemGrouping.hasAttentionOnly(group(listOf(pr(1, ProblemKind.LOGIN)), c = CollectedItems(conflicts = 1))))
        assertFalse(ProblemGrouping.hasAttentionOnly(group(listOf(pr(1, ProblemKind.PARTIAL)))))
    }

    @Test fun messagesOnlyForLevel5() {
        assertFalse(ProblemGrouping.hasMessagesOnly(group(emptyList(), c = CollectedItems(monthFolders = 1))))
        assertTrue(ProblemGrouping.hasMessagesOnly(group(listOf(pr(1, ProblemKind.PARTIAL)))))
        assertFalse(ProblemGrouping.hasMessagesOnly(group(emptyList())))
        assertFalse(ProblemGrouping.hasMessagesOnly(group(listOf(pr(1, ProblemKind.PLAINTEXT)))))
    }

    @Test fun inactiveProfilesAreIgnored() {
        assertTrue(ProblemGrouping.group(listOf(pr(99, ProblemKind.LOGIN)), names, emptyMap(), CollectedItems(), 1000).isEmpty())
    }

    // ---- Sammelmeldung ----
    @Test fun failureLinesGroupByCauseAndKeepAllProfiles() {
        val text = FailureLines.build(listOf("A" to "Anmeldung falsch", "B" to "Freigabe fehlt", "C" to "Anmeldung falsch"))
        assertEquals("A, C: Anmeldung falsch\nB: Freigabe fehlt", text)
        assertEquals("", FailureLines.build(emptyList()))
    }

    // ---- Zuordnung ----
    @Test fun kindsFromErrorKeys() {
        assertEquals(ProblemKind.LOGIN, ProblemKinds.fromKeys(setOf("err_friendly_login")))
        assertEquals(ProblemKind.ACCESS_UNENCRYPTED, ProblemKinds.fromKeys(setOf("err_friendly_access_unencrypted")))
        assertEquals(ProblemKind.ACCESS_DENIED, ProblemKinds.fromKeys(setOf("err_friendly_access")))
        assertEquals(ProblemKind.PC_UNREACHABLE, ProblemKinds.fromKeys(setOf("err_friendly_unreachable")))
        assertNull(ProblemKinds.fromKeys(setOf("irgendwas")))
    }
}