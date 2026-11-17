package com.sync2pc.app.sync

import android.content.Context

/**
 * Meldungen je Ursache statt je Profil (SYNC-20261115-409, Konzept SYNC-20261115-402). Dieser Teil ist rein (JVM-getestet):
 * Arten und Stufen, Speicherformat, [ProblemStore] und [ProblemGrouping]. Die Gruppe ist eine reine Ansicht; der Zustand bleibt je
 * Profil gespeichert und wird nur gelöscht, wenn das Profil selbst gelingt, gelöscht wird oder die Ursache wechselt.
 */

/** Auf welcher Ebene Profile zu einer Gruppe zusammengefasst werden */
enum class ProblemScope { PC, PC_SHARE, PC_USER, PC_USER_SHARE, DEVICE, PROFILE, PC_TEXT, KIND }

/** Mögliche Aktionen einer Gruppe (die Oberfläche zeigt nur, was zur Art passt) */
enum class ProblemAction { RETRY, ENTER_PASSWORD, OPEN_PROFILE, DECIDE, LOWER_PROTECTION, RESTORE_RETRY, RESTORE_DISCARD }

/**
 * Art der Meldung mit Stufe (1 = am wichtigsten) und Zusammenfassungsebene.
 * Stufen: 1 Datenverlust-Entscheid, 2 Sicherheit, 3 Benutzer muss handeln, 4 Datei-Entscheidungen, 5 vorübergehend.
 */
enum class ProblemKind(val level: Int, val scope: ProblemScope, val actions: Set<ProblemAction>) {
    // Stufe 1: Schutzbremse aller Arten, je Profil
    GUARD(1, ProblemScope.PROFILE, setOf(ProblemAction.DECIDE)),

    // Stufe 2: Sicherheit
    PLAINTEXT(2, ProblemScope.PC, setOf(ProblemAction.RETRY, ProblemAction.OPEN_PROFILE)),
    UNSIGNED(2, ProblemScope.PC, setOf(ProblemAction.RETRY, ProblemAction.OPEN_PROFILE)),
    PC_UNRECOGNISED(2, ProblemScope.PC, setOf(ProblemAction.RETRY, ProblemAction.OPEN_PROFILE)),
    ENC_REFUSED(2, ProblemScope.PC, setOf(ProblemAction.RETRY, ProblemAction.LOWER_PROTECTION, ProblemAction.OPEN_PROFILE)),
    SIGN_REFUSED(2, ProblemScope.PC, setOf(ProblemAction.RETRY, ProblemAction.LOWER_PROTECTION, ProblemAction.OPEN_PROFILE)),

    // Stufe 3: Benutzer muss handeln
    LOGIN(3, ProblemScope.PC_USER, setOf(ProblemAction.ENTER_PASSWORD, ProblemAction.RETRY, ProblemAction.OPEN_PROFILE)),
    ACCESS_DENIED(3, ProblemScope.PC_USER_SHARE, setOf(ProblemAction.RETRY, ProblemAction.OPEN_PROFILE)),
    CREDENTIALS_UNREADABLE(3, ProblemScope.DEVICE, setOf(ProblemAction.OPEN_PROFILE)),
    SHARE_MISSING(3, ProblemScope.PC_SHARE, setOf(ProblemAction.RETRY, ProblemAction.OPEN_PROFILE)),
    ACCESS_UNENCRYPTED(3, ProblemScope.PC_SHARE, setOf(ProblemAction.RETRY, ProblemAction.OPEN_PROFILE)),
    PC_FULL(3, ProblemScope.PC_SHARE, setOf(ProblemAction.RETRY, ProblemAction.OPEN_PROFILE)),
    PHONE_FULL(3, ProblemScope.DEVICE, setOf(ProblemAction.RETRY)),
    GUEST(3, ProblemScope.PC, setOf(ProblemAction.RETRY, ProblemAction.OPEN_PROFILE)),
    NEGOTIATION(3, ProblemScope.PC, setOf(ProblemAction.RETRY, ProblemAction.OPEN_PROFILE)),
    PC_FOLDER_MISSING(3, ProblemScope.PROFILE, setOf(ProblemAction.OPEN_PROFILE)),
    PHONE_FOLDER_LOST(3, ProblemScope.PROFILE, setOf(ProblemAction.OPEN_PROFILE)),
    RESTORE_FAILED(3, ProblemScope.PROFILE, setOf(ProblemAction.RESTORE_RETRY, ProblemAction.RESTORE_DISCARD)),
    UNKNOWN(3, ProblemScope.PC_TEXT, setOf(ProblemAction.RETRY, ProblemAction.OPEN_PROFILE)),

    // Stufe 4: Entscheidungen zu Dateien, je Art gesammelt (kommen aus den bestehenden Quellen, nicht aus dem ProblemStore)
    CONFLICTS(4, ProblemScope.KIND, setOf(ProblemAction.DECIDE)),
    MONTH_FOLDERS(4, ProblemScope.KIND, setOf(ProblemAction.DECIDE)),
    DELETE_CONFIRM(4, ProblemScope.KIND, setOf(ProblemAction.DECIDE)),

    // Stufe 5: vorübergehend
    PC_UNREACHABLE(5, ProblemScope.PC, setOf(ProblemAction.RETRY)),
    PARTIAL(5, ProblemScope.PROFILE, setOf(ProblemAction.RETRY, ProblemAction.OPEN_PROFILE));

    /** Zählt für die Überschrift als «Fehler» (Stufen 1 bis 3); Konflikte machen sie ebenfalls zu «Fehler» (siehe [ProblemGrouping.headlineLevel]) */
    val isError: Boolean get() = level <= 3
}

/** Ein gespeicherter Eintrag je Profil (höchstens einer). [detail] = Kennung (Msg-JSON) oder Text; [pcKey] siehe [ProblemGrouping.pcKeyOf] */
data class Problem(
    val profileId: Long,
    val kind: ProblemKind,
    val pcKey: String = "",
    val share: String = "",
    val user: String = "",
    val domain: String = "",
    val detail: String = "",
    val since: Long = 0,
    val last: Long = 0,
) {
    /** Einfaches, ausweichfreies Format (Trennzeichen U+001F); Felder dürfen es nicht enthalten */
    fun encode(): String = listOf(
        kind.name, pcKey, share, user, domain, detail, since.toString(), last.toString(),
    ).joinToString(SEP) { it.replace(SEP, " ") }

    companion object {
        private const val SEP = "\u001F"

        fun decode(profileId: Long, s: String): Problem? {
            val f = s.split(SEP)
            if (f.size < 8) return null
            val kind = ProblemKind.entries.firstOrNull { it.name == f[0] } ?: return null
            return Problem(profileId, kind, f[1], f[2], f[3], f[4], f[5], f[6].toLongOrNull() ?: 0, f[7].toLongOrNull() ?: 0)
        }
    }
}

/** Einfacher Schlüssel-Wert-Speicher (SharedPreferences, in Tests eine Map) */
interface ProblemKv {
    fun all(): Map<String, String>
    fun put(key: String, value: String)
    fun remove(key: String)
}

/** SharedPreferences-Speicher «problems»; nicht in der App-Sicherung (`allowBackup=false`) */
class PrefsProblemKv(context: Context) : ProblemKv {
    private val prefs = context.applicationContext.getSharedPreferences("problems", Context.MODE_PRIVATE)
    @Suppress("UNCHECKED_CAST")
    override fun all(): Map<String, String> = (prefs.all as Map<String, Any?>).mapNotNull { (k, v) -> (v as? String)?.let { k to it } }.toMap()
    override fun put(key: String, value: String) { prefs.edit().putString(key, value).commit() }
    override fun remove(key: String) { prefs.edit().remove(key).commit() }
}

/**
 * Speicher der Meldungen: je Profil höchstens ein Eintrag. Schreiben überschreibt (die Ursache wechselt); bleibt die Art gleich, bleibt
 * «seit». Löschen nur gezielt je Profil: ein Fehler von A bleibt, wenn B fehlerfrei läuft.
 */
class ProblemStore(private val kv: ProblemKv) {
    constructor(context: Context) : this(PrefsProblemKv(context))

    @Synchronized
    fun put(p: Problem, now: Long) {
        val old = get(p.profileId)
        val since = if (old != null && old.kind == p.kind) old.since else now
        kv.put(key(p.profileId), p.copy(since = since, last = now).encode())
    }

    @Synchronized
    fun get(profileId: Long): Problem? = kv.all()[key(profileId)]?.let { Problem.decode(profileId, it) }

    /** Alle Einträge */
    @Synchronized
    fun all(): List<Problem> = kv.all().mapNotNull { (k, v) ->
        if (!k.startsWith(PREFIX)) null else k.removePrefix(PREFIX).toLongOrNull()?.let { Problem.decode(it, v) }
    }

    /** Erfolgreicher Lauf bzw. bestandene Prüfung dieses Profils, Profil gelöscht */
    @Synchronized
    fun clear(profileId: Long) { kv.remove(key(profileId)) }

    /** Nur löschen, wenn der Eintrag eine der [kinds] ist (z. B. «nicht erreichbar» bei bestandener Erreichbarkeit) */
    @Synchronized
    fun clearKinds(profileId: Long, kinds: Set<ProblemKind>) {
        val cur = get(profileId) ?: return
        if (cur.kind in kinds) clear(profileId)
    }

    private fun key(id: Long) = PREFIX + id
    private companion object { const val PREFIX = "p" }
}

/** Zeilen der Sammelmeldung eines Laufs: je Ursache eine Zeile mit allen betroffenen Profilen (SYNC-20261115-409 Punkt 5) */
object FailureLines {
    /** [failures] = (Profilname, Meldungstext); gleiche Texte werden zusammengefasst, die Reihenfolge der ersten Nennung bleibt */
    fun build(failures: List<Pair<String, String>>): String =
        failures.groupBy({ it.second }, { it.first }).entries
            .joinToString("\n") { (text, names) -> names.distinct().joinToString(", ") + ": " + text }
}

/** Zuordnung von Fehlerursachen zu Arten (rein, ohne Android) */
object ProblemKinds {
    /** Art aus den Ressourcen-Namen einer Fehlerkennung ([com.sync2pc.app.l10n.Msg.keys]); null = keine bekannte Art */
    fun fromKeys(keys: Set<String>): ProblemKind? = when {
        "err_friendly_plaintext" in keys -> ProblemKind.PLAINTEXT
        "err_friendly_signing" in keys -> ProblemKind.UNSIGNED
        "err_friendly_login" in keys -> ProblemKind.LOGIN
        "err_friendly_credential_unreadable" in keys || "err_friendly_credential_unavailable" in keys -> ProblemKind.CREDENTIALS_UNREADABLE
        "err_friendly_guest_session" in keys -> ProblemKind.GUEST
        "err_friendly_access_unencrypted" in keys -> ProblemKind.ACCESS_UNENCRYPTED
        "err_friendly_access" in keys -> ProblemKind.ACCESS_DENIED
        "err_friendly_share_missing" in keys -> ProblemKind.SHARE_MISSING
        "err_friendly_folder_missing" in keys -> ProblemKind.PC_FOLDER_MISSING
        "err_friendly_pc_full" in keys -> ProblemKind.PC_FULL
        "err_friendly_phone_full" in keys -> ProblemKind.PHONE_FULL
        "err_friendly_unknown_host" in keys || "err_friendly_unreachable" in keys -> ProblemKind.PC_UNREACHABLE
        else -> null
    }

    /** Kennung PC nicht (sicher) erkannt: alle Entscheidungen ausser ALLOW */
    @Suppress("UNUSED_PARAMETER")
    fun fromPcDecision(d: PcDecision): ProblemKind = ProblemKind.PC_UNRECOGNISED
}

/** Erzeugt Einträge aus einem Profil (Ziel: PC, Freigabe, Benutzer, Domäne) */
object ProblemFactory {
    fun of(p: com.sync2pc.app.data.Profile, kind: ProblemKind, detail: String = ""): Problem {
        val t = com.sync2pc.app.smb.SmbTarget.parse(p.smbPath)
        val host = t?.host ?: com.sync2pc.app.smb.SmbTarget.hostOf(p.smbPath).orEmpty()
        return Problem(p.id, kind, ProblemGrouping.pcKeyOf(p.pcGuid, host), t?.share.orEmpty(), p.smbUser, p.smbDomain, detail)
    }

    /** Art und Detail eines fehlgeschlagenen Laufs aus der gespeicherten Kennung; unbekannt = [ProblemKind.UNKNOWN] mit Text */
    fun fromFailedRun(messageCode: String?, message: String): Pair<ProblemKind, String> {
        val keys = com.sync2pc.app.l10n.Msg.keys(messageCode)
        val kind = keys?.let { ProblemKinds.fromKeys(it) } ?: ProblemKind.UNKNOWN
        return kind to (messageCode ?: message)
    }
}

/** Anzeigebereite Gruppe (Ergebnis von [ProblemGrouping.group]) */
data class ProblemProfile(val id: Long, val name: String)

data class ProblemGroup(
    val key: String,
    val kind: ProblemKind,
    val level: Int,
    /** PC-Bezeichnung (Host) oder leer */
    val pcLabel: String,
    /** Freigabe bzw. Benutzer der Ebene, sonst leer */
    val target: String,
    val profiles: List<ProblemProfile>,
    val since: Long,
    val detail: String,
    /** Nur bei Stufe 4: Anzahl der Einträge (Konflikte, Gruppen, Dateien) */
    val count: Int = 0,
) {
    val actions: Set<ProblemAction> get() = kind.actions
}

/** Bereits gelesene Einträge der Stufe 4 aus den bestehenden Quellen (je Art eine Zahl, 0 = keine) */
data class CollectedItems(val conflicts: Int = 0, val monthFolders: Int = 0, val deleteConfirm: Int = 0, val since: Long = 0)

object ProblemGrouping {
    /** «PC nicht erreichbar» wird erst Gruppe, wenn das Profil so lange keinen erfolgreichen Lauf hatte (SYNC-20261115-409 Punkt 3) */
    const val UNREACHABLE_AFTER_MS = 24L * 3600_000L

    /** PC-Schlüssel: gespeicherte Kennung des PCs, sonst Host in Kleinbuchstaben */
    fun pcKeyOf(pcGuid: String, host: String): String =
        if (pcGuid.isNotBlank()) "guid:" + pcGuid.trim().lowercase() else "host:" + host.trim().lowercase()

    /**
     * Gruppenschlüssel: hängt nur von Art und konfiguriertem Ziel ab, nicht von Mitgliedern, Zeit oder Reihenfolge (Zusicherung
     * SYNC-20261115-406 d). Bleibt über Läufe gleich, solange Art und Profilziel gleich sind.
     */
    fun keyOf(p: Problem): String = p.kind.name + "|" + when (p.kind.scope) {
        ProblemScope.PC -> p.pcKey
        ProblemScope.PC_SHARE -> p.pcKey + "|" + p.share.trim().lowercase()
        ProblemScope.PC_USER -> p.pcKey + "|" + p.user.trim().lowercase() + "|" + p.domain.trim().lowercase()
        ProblemScope.PC_USER_SHARE -> p.pcKey + "|" + p.user.trim().lowercase() + "|" + p.domain.trim().lowercase() + "|" + p.share.trim().lowercase()
        ProblemScope.DEVICE -> "device"
        ProblemScope.PROFILE -> "p" + p.profileId
        ProblemScope.PC_TEXT -> p.pcKey + "|" + p.detail.trim().lowercase()
        ProblemScope.KIND -> "all"
    }

    /**
     * Sortierte Gruppen. [problems] = gespeicherte Einträge plus aus den bestehenden Quellen abgeleitete (Schutzbremse, Verschlüsselung,
     * Signierung, Wiederherstellung). [names] = Name je aktivem Profil (Einträge ohne aktives Profil entfallen). [lastOk] = Ende des
     * letzten erfolgreichen Laufs je Profil (für die 24-h-Schwelle). Reihenfolge: Stufe, mehr Profile zuerst, älter zuerst, Name.
     */
    fun group(
        problems: List<Problem>, names: Map<Long, String>, lastOk: Map<Long, Long?>, collected: CollectedItems, now: Long,
        pcLabels: Map<String, String> = emptyMap(),
    ): List<ProblemGroup> {
        val visible = problems.filter { p ->
            p.profileId in names &&
                (p.kind != ProblemKind.PC_UNREACHABLE || now - (lastOk[p.profileId] ?: p.since) >= UNREACHABLE_AFTER_MS)
        }
        val groups = visible.groupBy { keyOf(it) }.map { (key, list) ->
            val first = list.minByOrNull { it.since }!!
            val target = when (first.kind.scope) {
                ProblemScope.PC_SHARE -> first.share
                ProblemScope.PC_USER -> listOf(first.domain, first.user).filter { it.isNotBlank() }.joinToString("\\")
                ProblemScope.PC_USER_SHARE -> listOf(listOf(first.domain, first.user).filter { it.isNotBlank() }.joinToString("\\"), first.share)
                    .filter { it.isNotBlank() }.joinToString(" · ")
                else -> ""
            }
            ProblemGroup(
                key = key, kind = first.kind, level = first.kind.level,
                pcLabel = pcLabels[first.pcKey].orEmpty(), target = target,
                profiles = list.map { ProblemProfile(it.profileId, names[it.profileId].orEmpty()) }
                    .sortedBy { it.name.lowercase() },
                since = list.minOf { it.since }, detail = first.detail,
            )
        }
        val items = buildList {
            if (collected.conflicts > 0) add(collectedGroup(ProblemKind.CONFLICTS, collected.conflicts, collected.since))
            if (collected.monthFolders > 0) add(collectedGroup(ProblemKind.MONTH_FOLDERS, collected.monthFolders, collected.since))
            if (collected.deleteConfirm > 0) add(collectedGroup(ProblemKind.DELETE_CONFIRM, collected.deleteConfirm, collected.since))
        }
        return (groups + items).sortedWith(
            compareBy<ProblemGroup>({ it.level }, { -it.profiles.size }, { it.since }, { it.pcLabel.lowercase() + (it.profiles.firstOrNull()?.name?.lowercase() ?: "") }),
        )
    }

    private fun collectedGroup(kind: ProblemKind, count: Int, since: Long) =
        ProblemGroup(kind.name + "|all", kind, kind.level, "", "", emptyList(), since, "", count)

    /** Stufen 1 bis 3 → «Fehler» (Konflikte sind seit SYNC-20261115-416 kein Fehler mehr) */
    fun hasError(groups: List<ProblemGroup>): Boolean = groups.any { it.kind.isError }

    /** Nur Stufe 4 (ohne Fehler) → «Wartet auf dich» */
    fun hasAttentionOnly(groups: List<ProblemGroup>): Boolean = !hasError(groups) && groups.any { it.level == 4 }

    /** Nur Stufe 5 (ohne Fehler und Stufe 4) → «Meldungen» */
    fun hasMessagesOnly(groups: List<ProblemGroup>): Boolean = groups.isNotEmpty() && !hasError(groups) && groups.none { it.level == 4 }
}
