package com.sync2pc.app.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.sync2pc.app.R
import com.sync2pc.app.l10n.Msg
import com.sync2pc.app.l10n.plural
import com.sync2pc.app.l10n.str
import com.sync2pc.app.mirror.Guard
import com.sync2pc.app.mirror.MirrorGuard
import com.sync2pc.app.sync.ProblemAction
import com.sync2pc.app.sync.ProblemGroup
import com.sync2pc.app.sync.ProblemKind

/**
 * Sammelkachel «Meldungen» als Blätterkachel (SYNC-20261115-407, UX-Entwurf -401): eine Seite zur Zeit, Zähler «1 von 3», Pfeile
 * (je mindestens 48 dp, am Ende deaktiviert), Wischen und Pfeiltasten als Zusatz. Reihenfolge und Gruppen liefert die Datenseite
 * ([com.sync2pc.app.sync.ProblemGrouping]); die Kachel sortiert nicht selbst. Die gezeigte Seite bleibt per Schlüssel erhalten; kommt
 * eine wichtigere dazu, erscheint ein Hinweis mit «Anzeigen» statt automatischem Springen. Zustand nie nur über Farbe: jede Seite
 * trägt ein Etikett als Text.
 */
internal sealed class HomePage(val key: String, val level: Int) {
    class Problem(val group: ProblemGroup) : HomePage(group.key, group.level)
    class Checklist(val rows: List<ChecklistRow>) : HomePage("tip:checklist", 6)
    object BatteryTip : HomePage("tip:battery", 6)
    object PullTip : HomePage("tip:pull", 6)
}

/** Aktionen, die die Seiten auslösen (HomeScreen verdrahtet sie) */
internal class MessageActions(
    val retry: (Set<Long>) -> Unit,
    val openProfile: (Long) -> Unit,
    val enterPassword: (ProblemGroup) -> Unit,
    val openConflicts: () -> Unit,
    val openDecisions: () -> Unit,
    val confirmDeletes: () -> Unit,
    val lowerProtection: (ProblemKind, Long) -> Unit,
    val restoreRetry: () -> Unit,
    val restoreDiscard: () -> Unit,
    val guardChoice: (Long, MirrorGuard.Choice) -> Unit,
    val dismissBatteryTip: () -> Unit,
    val dismissPullTip: () -> Unit,
    val startAll: () -> Unit,
    val checklistRow: (ChecklistRow) -> Unit,
)

/** Daten, die Seiten zusätzlich brauchen */
internal class MessageEnv(
    val guardFor: (Long) -> Guard?,
    /** Text und Knopfbeschriftung der Stufe-4-Seiten (Konflikte, Monatsordner, Löschbestätigung) */
    val collectedText: (ProblemKind) -> Pair<String, String>?,
    /** Profile, für die gerade ein «Erneut versuchen» eingereiht ist oder läuft */
    val retrying: Set<Long>,
    val restoreText: String,
    val actions: MessageActions,
)

@StringRes
internal fun problemTitleRes(k: ProblemKind): Int = when (k) {
    ProblemKind.GUARD -> R.string.prob_title_guard
    ProblemKind.PLAINTEXT -> R.string.prob_title_plaintext
    ProblemKind.UNSIGNED -> R.string.prob_title_unsigned
    ProblemKind.PC_UNRECOGNISED -> R.string.prob_title_pc_unrecognised
    ProblemKind.ENC_REFUSED -> R.string.prob_title_enc_refused
    ProblemKind.SIGN_REFUSED -> R.string.prob_title_sign_refused
    ProblemKind.LOGIN -> R.string.prob_title_login
    ProblemKind.ACCESS_DENIED -> R.string.prob_title_access_denied
    ProblemKind.CREDENTIALS_UNREADABLE -> R.string.prob_title_credentials_unreadable
    ProblemKind.SHARE_MISSING -> R.string.prob_title_share_missing
    ProblemKind.ACCESS_UNENCRYPTED -> R.string.prob_title_access_unencrypted
    ProblemKind.PC_FULL -> R.string.prob_title_pc_full
    ProblemKind.PHONE_FULL -> R.string.prob_title_phone_full
    ProblemKind.GUEST -> R.string.prob_title_guest
    ProblemKind.NEGOTIATION -> R.string.prob_title_negotiation
    ProblemKind.PC_FOLDER_MISSING -> R.string.prob_title_pc_folder_missing
    ProblemKind.PHONE_FOLDER_LOST -> R.string.prob_title_phone_folder_lost
    ProblemKind.RESTORE_FAILED -> R.string.prob_title_restore_failed
    ProblemKind.UNKNOWN -> R.string.prob_title_unknown
    ProblemKind.CONFLICTS -> R.string.prob_title_conflicts
    ProblemKind.MONTH_FOLDERS -> R.string.prob_title_month_folders
    ProblemKind.DELETE_CONFIRM -> R.string.prob_title_delete_confirm
    ProblemKind.PC_UNREACHABLE -> R.string.prob_title_pc_unreachable
    ProblemKind.PARTIAL -> R.string.prob_title_partial
}

@StringRes
internal fun problemLevelRes(level: Int): Int = when (level) {
    1 -> R.string.prob_level_1
    2 -> R.string.prob_level_2
    3 -> R.string.prob_level_3
    4 -> R.string.prob_level_4
    5 -> R.string.prob_level_5
    else -> R.string.prob_level_tip
}

@Composable
internal fun MessagesCard(pages: List<HomePage>, env: MessageEnv) {
    if (pages.isEmpty()) return
    val keys = pages.map { it.key }
    var shownKey by rememberSaveable { mutableStateOf<String?>(null) }
    val index = MessagePager.indexOf(keys, shownKey)
    val current = pages[index]
    LaunchedEffect(current.key) { shownKey = current.key }
    // Bereits bekannte Seiten: nur eine neue, wichtigere Seite löst den Hinweis aus (kein Springen)
    var seen by remember { mutableStateOf<Set<String>?>(null) }
    LaunchedEffect(Unit) { if (seen == null) seen = keys.toSet() }
    val levels = pages.associate { it.key to it.level }
    val arrived = seen?.let { MessagePager.moreImportantArrived(keys, levels, current.key, it) }
    fun go(delta: Int) {
        shownKey = keys[MessagePager.step(index, delta, keys.size)]
        seen = keys.toSet()
    }

    val region = str(R.string.msg_region, index + 1, pages.size)
    var drag by remember { mutableStateOf(0f) }
    // Färbung nach der höchsten Schwere aller Einträge, nicht je Seite (SYNC-20261115-416 B)
    val dark = androidx.compose.foundation.isSystemInDarkTheme()
    val colors = when (MessagePager.severityOf(pages.map { it.level })) {
        MessagePager.Severity.ERROR -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
        MessagePager.Severity.WARNING -> CardDefaults.cardColors(
            containerColor = if (dark) androidx.compose.ui.graphics.Color(0xFF4A3F10) else androidx.compose.ui.graphics.Color(0xFFFFF3C4),
            contentColor = if (dark) androidx.compose.ui.graphics.Color(0xFFFFF3C4) else androidx.compose.ui.graphics.Color(0xFF3A2E00))
        MessagePager.Severity.INFO -> CardDefaults.cardColors()
    }
    Card(
        colors = colors,
        modifier = Modifier.fillMaxWidth()
            .semantics { contentDescription = region }
            .focusable()
            .onKeyEvent { e ->
                if (e.type != KeyEventType.KeyDown) false else when (e.key) {
                    Key.DirectionLeft -> { if (MessagePager.canPrev(index)) go(-1); true }
                    Key.DirectionRight -> { if (MessagePager.canNext(index, keys.size)) go(1); true }
                    else -> false
                }
            }
            .pointerInput(index, keys.size) {
                detectHorizontalDragGestures(
                    onDragStart = { drag = 0f },
                    onDragEnd = {
                        if (drag > 120f && MessagePager.canPrev(index)) go(-1)
                        else if (drag < -120f && MessagePager.canNext(index, keys.size)) go(1)
                    },
                    onHorizontalDrag = { _, amount -> drag += amount },
                )
            },
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Kopf: Titel, darunter Zähler und Pfeile auf eigener Zeile (bei grosser Schrift nichts abgeschnitten)
            Text(str(R.string.msg_title_messages),
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(str(R.string.msg_counter, index + 1, pages.size), modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge)
                IconButton(onClick = { go(-1) }, enabled = MessagePager.canPrev(index), modifier = Modifier.size(48.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, str(R.string.msg_prev))
                }
                IconButton(onClick = { go(1) }, enabled = MessagePager.canNext(index, keys.size), modifier = Modifier.size(48.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, str(R.string.msg_next))
                }
            }
            if (arrived != null) Row(verticalAlignment = Alignment.CenterVertically) {
                Text(str(R.string.msg_new_important), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = { shownKey = arrived; seen = keys.toSet() }) { Text(str(R.string.msg_show)) }
            }
            // Etikett und Titel der Seite werden beim Blättern angesagt
            Column(Modifier.semantics { liveRegion = LiveRegionMode.Polite }, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                when (current) {
                    is HomePage.Problem -> ProblemPage(current.group, env)
                    is HomePage.Checklist -> ChecklistPage(current.rows, env)
                    HomePage.BatteryTip -> BatteryTipPage(env)
                    HomePage.PullTip -> PullTipPage(env)
                }
            }
        }
    }
}

@Composable
private fun PageLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun ProblemPage(g: ProblemGroup, env: MessageEnv) {
    val a = env.actions
    // Schutzbremse: Inhalt direkt in der Seite, ohne eigene Kartenfläche; Etikett statt zweier Zeilen (SYNC-20261115-416 C)
    if (g.kind == ProblemKind.GUARD) {
        PageLabel(str(R.string.msg_label_guard))
        val p = g.profiles.firstOrNull()
        val guard = p?.let { env.guardFor(it.id) }
        if (p != null && guard != null) GuardBody(guard, p.name, onChooseFolder = { a.openProfile(p.id) }, onChoice = { a.guardChoice(p.id, it) })
        return
    }
    PageLabel(str(problemLevelRes(g.level)))
    Text(
        str(problemTitleRes(g.kind)) + if (g.pcLabel.isNotBlank()) " – " + g.pcLabel else "",
        style = MaterialTheme.typography.titleSmall,
    )
    if (g.target.isNotBlank()) Hint(g.target)
    val names = g.profiles.joinToString(", ") { it.name }
    val body: String? = when (g.kind) {
        ProblemKind.CONFLICTS, ProblemKind.MONTH_FOLDERS, ProblemKind.DELETE_CONFIRM -> env.collectedText(g.kind)?.first
        ProblemKind.ENC_REFUSED -> str(R.string.prot_enc_text, names)
        ProblemKind.SIGN_REFUSED -> str(R.string.sign_text, names)
        ProblemKind.PHONE_FOLDER_LOST -> str(R.string.home_profile_access_lost)
        ProblemKind.RESTORE_FAILED -> env.restoreText
        ProblemKind.PC_UNREACHABLE -> str(R.string.msg_unreachable_text)
        else -> g.detail.takeIf { it.isNotBlank() }?.let { Msg.render(it, it) }
    }
    if (!body.isNullOrBlank()) Text(body)
    // Betroffene Profile: bis zu drei Namen, der Rest aufklappbar
    if (g.profiles.isNotEmpty() && g.kind.level <= 3 || g.kind == ProblemKind.PC_UNREACHABLE || g.kind == ProblemKind.PARTIAL) {
        var all by remember(g.key) { mutableStateOf(false) }
        val shown = if (all) g.profiles else g.profiles.take(3)
        Text(str(R.string.msg_affects, shown.joinToString(", ") { it.name }), style = MaterialTheme.typography.bodyMedium)
        if (g.profiles.size > 3 && !all) TextButton(onClick = { all = true }) { Text(str(R.string.msg_more_profiles, g.profiles.size - 3)) }
    }
    val ids = g.profiles.map { it.id }.toSet()
    if (ids.any { it in env.retrying }) Text(str(R.string.msg_retrying), style = MaterialTheme.typography.bodyMedium)
    // Aktionen untereinander, volle Breite
    val full = Modifier.fillMaxWidth()
    val acts = g.actions
    if (ProblemAction.ENTER_PASSWORD in acts) Button(onClick = { a.enterPassword(g) }, modifier = full) { Text(str(R.string.msg_enter_password)) }
    if (ProblemAction.RETRY in acts && ids.isNotEmpty()) {
        val primary = ProblemAction.ENTER_PASSWORD !in acts
        if (primary) Button(onClick = { a.retry(ids) }, modifier = full) { Text(str(R.string.msg_retry)) }
        else OutlinedButton(onClick = { a.retry(ids) }, modifier = full) { Text(str(R.string.msg_retry)) }
    }
    if (ProblemAction.RESTORE_RETRY in acts) Button(onClick = a.restoreRetry, modifier = full) { Text(str(R.string.msg_retry)) }
    if (ProblemAction.RESTORE_DISCARD in acts) OutlinedButton(onClick = a.restoreDiscard, modifier = full) { Text(str(R.string.home_discard)) }
    if (ProblemAction.DECIDE in acts) {
        val label = env.collectedText(g.kind)?.second ?: str(R.string.msg_decide)
        Button(onClick = {
            when (g.kind) {
                ProblemKind.CONFLICTS -> a.openConflicts()
                ProblemKind.MONTH_FOLDERS -> a.openDecisions()
                ProblemKind.DELETE_CONFIRM -> a.confirmDeletes()
                else -> {}
            }
        }, modifier = full) { Text(label) }
    }
    // Sicherheit senken nur je Profil (Benutzerentscheid 3)
    if (ProblemAction.LOWER_PROTECTION in acts) g.profiles.forEach { p ->
        OutlinedButton(onClick = { a.lowerProtection(g.kind, p.id) }, modifier = full) { Text(str(R.string.msg_lower_named, p.name)) }
    }
    if (ProblemAction.OPEN_PROFILE in acts && g.profiles.isNotEmpty()) {
        if (g.profiles.size == 1) TextButton(onClick = { a.openProfile(g.profiles[0].id) }, modifier = full) { Text(str(R.string.msg_open_profile)) }
        else g.profiles.take(3).forEach { p ->
            TextButton(onClick = { a.openProfile(p.id) }, modifier = full) { Text(str(R.string.msg_open_profile_named, p.name)) }
        }
    }
}

/** Import-Checkliste als Seite der Stufe 6 (SYNC-20261115-416 E): Zeilen je Profil für Ordner und Verbindungstest, danach «Jetzt starten» zusammengefasst */
@Composable
private fun ChecklistPage(rows: List<ChecklistRow>, env: MessageEnv) {
    PageLabel(str(R.string.prob_level_tip))
    val full = Modifier.fillMaxWidth()
    if (MessagePager.checklistSummaryOnly(rows.map { it.step })) {
        Text(plural(R.plurals.chk_waiting_group, rows.size, rows.size), style = MaterialTheme.typography.titleSmall)
        Button(onClick = env.actions.startAll, modifier = full) { Text(str(R.string.chk_btn_sync)) }
    } else {
        Text(if (rows.size == 1) str(R.string.chk_title_one) else str(R.string.chk_title_many, rows.size), style = MaterialTheme.typography.titleSmall)
        Text(str(R.string.chk_text))
        rows.filter { it.step != ChecklistStep.SYNC }.forEach { r ->
            val stepText = str(if (r.step == ChecklistStep.FOLDER) R.string.chk_step_folder else R.string.chk_step_test)
            val btn = str(if (r.step == ChecklistStep.FOLDER) R.string.chk_btn_folder else R.string.chk_btn_test)
            Text(str(R.string.chk_row, r.name, stepText))
            OutlinedButton(onClick = { env.actions.checklistRow(r) }, modifier = full) { Text(btn) }
        }
    }
}

@Composable
private fun BatteryTipPage(env: MessageEnv) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    var how by remember { mutableStateOf(false) }
    PageLabel(str(R.string.prob_level_tip))
    Text(str(R.string.battery_tip_title), style = MaterialTheme.typography.titleSmall)
    Text(str(R.string.battery_tip_text))
    // SYNC-20261116-434: zweites Erscheinen nach einem Lauf ohne Vordergrund: Grund nennen
    if (BatteryTip.showsReason(ctx)) Text(str(R.string.battery_tip_reason))
    val full = Modifier.fillMaxWidth()
    Button(onClick = { BatteryTip.openSettings(ctx) }, modifier = full) { Text(str(R.string.setup_battery_btn)) }
    OutlinedButton(onClick = { how = !how }, modifier = full) { Text(str(R.string.setup_battery_how)) }
    if (how) BatteryHowTo()
    OutlinedButton(onClick = env.actions.dismissBatteryTip, modifier = full) { Text(str(R.string.battery_tip_later)) }
}

@Composable
private fun PullTipPage(env: MessageEnv) {
    PageLabel(str(R.string.prob_level_tip))
    Text(str(R.string.home_pull_tip))
    OutlinedButton(onClick = env.actions.dismissPullTip, modifier = Modifier.fillMaxWidth()) { Text(str(R.string.prot_ok)) }
}

/**
 * «Passwort neu eingeben» einmal je PC, Benutzer und Domäne (SYNC-20261115-409 Punkt 6): Profilliste abwählbar; das Speichern und der
 * Erstversuch laufen im Aufrufer (zuerst ein Profil, erst bei Erfolg die übrigen, damit kein Konto gesperrt wird).
 */
@Composable
internal fun PasswordGroupDialog(group: ProblemGroup, busy: Boolean, error: String?, onDismiss: () -> Unit, onConfirm: (String, Set<Long>) -> Unit) {
    var password by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(group.profiles.map { it.id }.toSet()) }
    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { DialogTitleText(str(R.string.msg_pw_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(str(R.string.msg_pw_text))
                if (group.target.isNotBlank()) Hint(group.target)
                OutlinedTextField(
                    password, { password = it }, label = { Text(str(R.string.profile_password)) }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                )
                group.profiles.forEach { p ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = p.id in selected, onCheckedChange = { on -> selected = if (on) selected + p.id else selected - p.id })
                        Spacer(Modifier.width(4.dp))
                        Text(p.name)
                    }
                }
                if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            DialogButtonColumn {
                Button(
                    onClick = { onConfirm(password, selected) }, enabled = !busy && password.isNotEmpty() && selected.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) { Text(str(R.string.msg_pw_ok)) }
                StackedButton(str(R.string.prot_cancel)) { if (!busy) onDismiss() }
            }
        },
    )
}
