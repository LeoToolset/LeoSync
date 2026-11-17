package com.sync2pc.app.ui

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sync2pc.app.R
import com.sync2pc.app.l10n.str

/**
 * Schutzkarten und Hinweise (SYNC-20261115-352): Karte «Verschlüsselung nicht verfügbar», Bestätigungsdialog
 * «Weniger Schutz» und einmalige Hinweise (Google Fotos). Gleiches Muster wie die Schutzbremse ([GuardCard]).
 */

/** Höchstens so viele Schutzkarten gleichzeitig auf der Startseite, der Rest als «n weitere» */
internal const val MAX_PROTECT_CARDS = 3

/** Kleine Merker für einmalige, ausblendbare Hinweise (SharedPreferences, nicht in der Sicherung) */
internal object ProtectNotes {
    private const val PREFS = "protect_notes"
    private const val GPHOTOS = "gphotosTipDone"

    fun gphotosTipDone(ctx: Context): Boolean = try {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(GPHOTOS, false)
    } catch (e: Exception) { false }

    fun setGphotosTipDone(ctx: Context) {
        try { ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(GPHOTOS, true).apply() } catch (e: Exception) { }
    }
}

/**
 * Karte «Verschlüsselung nicht verfügbar» (Stufe «Erforderlich», der PC bietet sie nicht an). Hauptaktion: Verbindung testen
 * (startet einen Lauf, bei Erfolg endet der Zustand); Zweitaktion: auf «Wenn möglich» stellen, mit Bestätigung.
 */
@Composable
internal fun EncRejectedCard(
    profileName: String, onTest: () -> Unit, onLower: () -> Unit,
    // Signierung abgelehnt (B11b) nutzt dieselbe Karte mit eigenen Texten
    titleRes: Int = R.string.prot_enc_title, textRes: Int = R.string.prot_enc_text,
    lowerRes: Int = R.string.prot_enc_lower, confirmTitleRes: Int = R.string.prot_enc_lower_title,
    confirmTextRes: Int = R.string.prot_enc_lower_text, confirmOkRes: Int = R.string.prot_enc_lower_ok,
) {
    val ctx = LocalContext.current
    var confirm by remember { mutableStateOf(false) }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.foundation.Image(androidx.compose.ui.res.painterResource(R.drawable.ic_warn), null, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text(str(titleRes), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Hint(str(R.string.prot_state_decision))
            Text(str(textRes, profileName))
            val full = Modifier.fillMaxWidth()
            Button(onClick = onTest, modifier = full) { Text(str(R.string.prot_enc_test)) }
            OutlinedButton(onClick = { confirm = true }, modifier = full) { Text(str(lowerRes)) }
            TextButton(onClick = {
                ctx.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(encryptionHelpUrl())))
            }) { Text(str(R.string.prot_enc_more)) }
        }
    }
    if (confirm) AlertDialog(
        onDismissRequest = { confirm = false },
        title = { DialogTitleText(str(confirmTitleRes)) },
        text = { Text(str(confirmTextRes)) },
        confirmButton = {
            // Sicherer Weg zuerst und hervorgehoben, die riskante Aktion als Textknopf (SYNC-20261115-392)
            SafeFirstButtons(str(R.string.prot_cancel), str(confirmOkRes), { confirm = false }) { confirm = false; onLower() }
        },
    )
}

/** Bestätigungsknöpfe beim Absenken des Schutzes: «Abbrechen» gefüllt und zuerst, die riskante Aktion als Textknopf darunter */
@Composable
internal fun SafeFirstButtons(safeText: String, riskText: String, onSafe: () -> Unit, onRisk: () -> Unit) {
    DialogButtonColumn {
        Button(onClick = onSafe, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text(safeText, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
        StackedButton(riskText) { onRisk() }
    }
}

/** Meldung oben im Profil, solange der Zustand «Verschlüsselung nicht verfügbar» besteht */
@Composable
internal fun EncRejectedBanner() {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            androidx.compose.foundation.Image(androidx.compose.ui.res.painterResource(R.drawable.ic_warn), null, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(str(R.string.prot_enc_title), fontWeight = FontWeight.SemiBold)
                Text(str(R.string.prot_enc_profile_banner))
            }
        }
    }
}

/** Einmalige Info-Zeile oben im Profil «automatisch auf Erforderlich gestellt», bleibt bis «OK» */
@Composable
internal fun EncAutoInfoCard(onOk: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(str(R.string.prot_auto_info), color = MaterialTheme.colorScheme.onSecondaryContainer)
            TextButton(onClick = onOk) { Text(str(R.string.prot_ok)) }
        }
    }
}

/** Bestätigung beim Herabsetzen von «Erforderlich» (sicherheitskritisch) */
@Composable
internal fun LessProtectionDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { DialogTitleText(str(R.string.prot_less_title)) },
        text = { Text(str(R.string.prot_less_text)) },
        confirmButton = {
            SafeFirstButtons(str(R.string.prot_cancel), str(R.string.prot_less_ok), onDismiss, onConfirm)
        },
    )
}

/** Nächster nötiger Schritt eines Profils nach dem Import (SYNC-20261115-353) */
internal enum class ChecklistStep { FOLDER, TEST, SYNC }

internal data class ChecklistRow(val profileId: Long, val name: String, val step: ChecklistStep)

/** Reine Regel: welcher Schritt steht als Nächstes an? Ordnerzugriff fehlt → Ordner; PC noch nicht erkannt → Test; sonst Start */
internal fun nextChecklistStep(folderAccessLost: Boolean, pcKnown: Boolean): ChecklistStep = when {
    folderAccessLost -> ChecklistStep.FOLDER
    !pcKnown -> ChecklistStep.TEST
    else -> ChecklistStep.SYNC
}

/**
 * Checklistenkarte «Nach dem Import» oben auf der Startseite: eine Zeile je Profil mit dem nächsten Schritt. Nicht wegwischbar
 * (sicherheitsrelevant), nur einklappbar.
 */
@Composable
internal fun ImportChecklistCard(rows: List<ChecklistRow>, onAct: (ChecklistRow) -> Unit) {
    var open by remember { mutableStateOf(true) }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                if (rows.size == 1) str(R.string.chk_title_one) else str(R.string.chk_title_many, rows.size),
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            if (open) {
                Text(str(R.string.chk_text), color = MaterialTheme.colorScheme.onSecondaryContainer)
                rows.forEach { r ->
                    val stepText = str(when (r.step) {
                        ChecklistStep.FOLDER -> R.string.chk_step_folder
                        ChecklistStep.TEST -> R.string.chk_step_test
                        ChecklistStep.SYNC -> R.string.chk_step_sync
                    })
                    val btn = str(when (r.step) {
                        ChecklistStep.FOLDER -> R.string.chk_btn_folder
                        ChecklistStep.TEST -> R.string.chk_btn_test
                        ChecklistStep.SYNC -> R.string.chk_btn_sync
                    })
                    Text(str(R.string.chk_row, r.name, stepText), color = MaterialTheme.colorScheme.onSecondaryContainer)
                    OutlinedButton(onClick = { onAct(r) }, modifier = Modifier.fillMaxWidth()) { Text(btn) }
                }
            }
            TextButton(onClick = { open = !open }) { Text(str(if (open) R.string.chk_collapse else R.string.chk_expand)) }
        }
    }
}

/** Info-Hinweis (nicht rot) oben im Papierkorb-Bildschirm */
@Composable
internal fun TrashInfoNote(lines: List<String>) {
    if (lines.isEmpty()) return
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            androidx.compose.material3.Icon(Icons.Filled.Info, null,
                modifier = Modifier.size(20.dp).padding(top = 2.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Spacer(Modifier.width(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                lines.forEach { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer) }
            }
        }
    }
}

/** «Mehr dazu»: aufklappbare Schritte zum Einschalten der Verschlüsselung (Fehlermeldung «nicht verfügbar») */
@Composable
internal fun EncMoreSteps() {
    var open by remember { mutableStateOf(false) }
    TextButton(onClick = { open = !open }) { Text(str(R.string.enc_more)) }
    if (open) Hint(str(R.string.enc_more_steps))
}

/**
 * Karte auf der leeren Startseite: Umzug von der alten LeoSync-App (com.sync2pc.app) über Export/Import (SYNC-20261115-354).
 * «Umzug starten» zeigt die Schritte mit Warnung und Sicherheitshinweis; dort führt eine Schaltfläche zur bestehenden Import-Aktion.
 */
@Composable
internal fun MoveFromOldCard(onStart: () -> Unit, onFresh: () -> Unit) {
    var steps by remember { mutableStateOf(false) }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(str(R.string.move_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer)
            Text(str(R.string.move_text), color = MaterialTheme.colorScheme.onSecondaryContainer)
            Button(onClick = { steps = true }, modifier = Modifier.fillMaxWidth()) { Text(str(R.string.move_start)) }
            OutlinedButton(onClick = onFresh, modifier = Modifier.fillMaxWidth()) { Text(str(R.string.move_fresh)) }
        }
    }
    if (steps) AlertDialog(
        onDismissRequest = { steps = false },
        title = { DialogTitleText(str(R.string.move_steps_title)) },
        text = {
            androidx.compose.foundation.rememberScrollState().let { sc ->
                Column(Modifier.verticalScroll(sc), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(str(R.string.move_warn), fontWeight = FontWeight.SemiBold)
                    listOf(R.string.move_step1, R.string.move_step2, R.string.move_step3, R.string.move_step4, R.string.move_step5)
                        .forEach { Text(str(it)) }
                    Hint(str(R.string.move_security))
                }
            }
        },
        confirmButton = {
            DialogButtonColumn {
                StackedButton(str(R.string.move_to_import)) { steps = false; onStart() }
                StackedButton(str(R.string.prot_cancel)) { steps = false }
            }
        },
    )
}