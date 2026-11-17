# LeoSync - Übersetzungen Deutsch / Englisch

Erzeugt mit `tools/make-translations.ps1` aus `res/values-de` (Deutsch) und `res/values` (Englisch). Korrekturen bitte mit Zeilennummer melden. Platzhalter wie `%1$s` werden in der App ersetzt (Zahl, Name, Datum ...). Zeilen mit `(one)` / `(other)` sind Einzahl / Mehrzahl.

## Allgemein

| Nr. | Schlüssel | Deutsch | Englisch |
|---:|---|---|---|
| 1 | `common_back` | Zurück | Back |
| 2 | `main_files_removed (one)` | %1$s Datei entfernt | %1$s file removed |
| 3 | `main_files_removed (other)` | %1$s Dateien entfernt | %1$s files removed |
| 4 | `main_nothing_to_remove` | Nichts zu entfernen | Nothing to remove |

## Startseite, Schutzbremse, Berechtigungen

| Nr. | Schlüssel | Deutsch | Englisch |
|---:|---|---|---|
| 5 | `common_cancel` | Abbrechen | Cancel |
| 6 | `common_pause` | Pausieren | Pause |
| 7 | `common_sync_now` | Jetzt synchronisieren | Sync now |
| 8 | `common_save` | Speichern | Save |
| 9 | `common_ok` | OK | OK |
| 10 | `common_delete` | Löschen | Delete |
| 11 | `common_more` | Mehr dazu | More |
| 12 | `common_less` | Weniger | Less |
| 13 | `common_details` | Technische Details | Technical details |
| 14 | `common_details_hide` | Details ausblenden | Hide details |
| 15 | `home_profile_deleted` | Profil „%1$s“ gelöscht | Profile “%1$s” deleted |
| 16 | `home_undo` | Rückgängig | Undo |
| 17 | `home_pull_tip` | Ganz oben nach unten ziehen startet ebenfalls eine Synchronisierung. | Pulling down at the very top also starts a sync. |
| 18 | `home_eta` | noch ca. %1$s | about %1$s left |
| 19 | `home_eta_calculating` | Restzeit wird berechnet … | Calculating remaining time… |
| 20 | `eta_minutes (one)` | %1$s Min. | %1$s min |
| 21 | `eta_minutes (other)` | %1$s Min. | %1$s min |
| 22 | `eta_hours` | %1$s Std. %2$s Min. | %1$s h %2$s min |
| 23 | `home_wait_wifi` | Wartet auf das Heim-WLAN. | Waiting for the home Wi-Fi. |
| 24 | `home_wait_charger` | Wartet auf das Ladegerät („Nur beim Laden“ ist eingeschaltet). | Waiting for the charger (“Only while charging” is on). |
| 25 | `home_starting` | Synchronisierung startet gleich… | Sync is starting… |
| 26 | `home_tool_trash_text` | Dateien zurückholen oder endgültig löschen | Restore files or delete them permanently |
| 27 | `home_tool_trash_used` | Belegt: Handy %1$s · PC %2$s | Used: phone %1$s · PC %2$s |
| 28 | `home_guard_note_all` | Wartet auf deine Entscheidung (Schutzbremse) – erst danach geht es weiter. | Waiting for your decision (safety brake) – it continues only after that. |
| 29 | `home_guard_note_some` | Bei einem Profil wartet es auf deine Entscheidung (Schutzbremse). | One profile is waiting for your decision (safety brake). |
| 30 | `home_pc_unreachable` | PC nicht erreichbar (%1$s) – nächste Prüfung um %2$s | PC not reachable (%1$s) – next check at %2$s |
| 31 | `home_cd_paused` | Pausiert – tippen zum Fortsetzen | Paused – tap to resume |
| 32 | `home_cd_running` | Läuft – tippen zum Pausieren | Running – tap to pause |
| 33 | `home_cd_stats` | Statistik | Statistics |
| 34 | `home_cd_log` | Verlauf | History |
| 35 | `home_cd_settings` | Einstellungen | Settings |
| 36 | `home_profile_fallback` | Profil | Profile |
| 37 | `home_conflicts_title` | Konflikte bei der Ordner-Synchronisierung | Folder sync conflicts |
| 38 | `home_conflicts_text (one)` | %1$s Datei wurde auf Handy und PC geändert. Wähle, welche Version gilt. | %1$s file was changed on both phone and PC. Choose which version wins. |
| 39 | `home_conflicts_text (other)` | %1$s Dateien wurden auf Handy und PC geändert. Wähle, welche Version gilt. | %1$s files were changed on both phone and PC. Choose which version wins. |
| 40 | `home_conflicts_button` | Entscheiden | Decide |
| 41 | `home_decisions_title` | Ordnerauswahl nötig | Folder selection needed |
| 42 | `home_decisions_text (one)` | %1$s Datei in %2$s wartet, weil es umbenannte Monatsordner gibt. | %1$s file in %2$s is waiting because there are renamed month folders. |
| 43 | `home_decisions_text (other)` | %1$s Dateien in %2$s warten, weil es umbenannte Monatsordner gibt. | %1$s files in %2$s are waiting because there are renamed month folders. |
| 44 | `home_groups (one)` | %1$s Gruppe | %1$s group |
| 45 | `home_groups (other)` | %1$s Gruppen | %1$s groups |
| 46 | `home_decisions_button` | Zuordnen | Assign |
| 47 | `home_delete_title` | Gesicherte Dateien entfernen | Remove backed-up files |
| 48 | `home_delete_text (one)` | %1$s Datei ist verifiziert auf dem PC. Android fragt einmal zur Bestätigung. | %1$s file is verified on the PC. Android asks once for confirmation. |
| 49 | `home_delete_text (other)` | %1$s Dateien sind verifiziert auf dem PC. Android fragt einmal zur Bestätigung. | %1$s files are verified on the PC. Android asks once for confirmation. |
| 50 | `home_delete_button` | Entfernen bestätigen | Confirm removal |
| 51 | `home_profiles` | Profile | Profiles |
| 52 | `home_add_profile` | Profil hinzufügen | Add profile |
| 53 | `home_no_profile` | Noch kein Profil. Lege ein Profil mit Ziel-Freigabe und Quellordnern an. | No profile yet. Create a profile with a target Share and source folders. |
| 54 | `home_first_profile` | Erstes Profil anlegen | Create first profile |
| 55 | `home_tool_free` | Speicher freigeben | Free up space |
| 56 | `home_tool_free_text` | Alte, auf dem PC gesicherte Dateien vom Handy entfernen | Remove old files from the phone that are backed up on the PC |
| 57 | `home_tool_free_disabled` | Nur bei Fotos-Profilen und Ordner-Profilen Handy → PC | Only for photo profiles and folder profiles phone → PC |
| 58 | `home_tool_trash` | Papierkorb | Trash |
| 59 | `home_tool_trash_disabled` | Nur bei Ordner-Profilen | Only for folder profiles |
| 60 | `home_tool_trash_hint` | Den Papierkorb gibt es nur bei Ordner-Profilen. | The trash is only available for folder profiles. |
| 61 | `home_tools` | Tools | Tools |
| 62 | `home_purge_pending_title` | Papierkorb leeren vorgemerkt | Emptying the trash is scheduled |
| 63 | `home_purge_pending_text` | Der PC-Papierkorb wird bei der nächsten Synchronisierung mit erreichbarem PC geleert. | The PC trash will be emptied at the next sync with a reachable PC. |
| 64 | `home_restore_failed_title` | Wiederherstellen fehlgeschlagen | Restore failed |
| 65 | `home_restore_failed_text (one)` | %1$s Datei konnte nicht wiederhergestellt werden. Es wurde alles zurückgerollt; die nächste Synchronisierung versucht es erneut. Details stehen im Verlauf. | %1$s file could not be restored. Everything was rolled back; the next sync will try again. Details are in the history. |
| 66 | `home_restore_failed_text (other)` | %1$s Dateien konnten nicht wiederhergestellt werden. Es wurde alles zurückgerollt; die nächste Synchronisierung versucht es erneut. Details stehen im Verlauf. | %1$s files could not be restored. Everything was rolled back; the next sync will try again. Details are in the history. |
| 67 | `home_restore_gaveup_text (one)` | %1$s Datei konnte auch nach mehreren Versuchen nicht wiederhergestellt werden. Es wurde alles zurückgerollt und nichts geht verloren: Vormerkung und Papierkorb-Version bleiben erhalten. „Erneut versuchen“ oder „Verwerfen“ wählen. | %1$s file could not be restored after several attempts. Everything was rolled back and nothing is lost: the request and the trash version are kept. Choose “Try again” or “Discard”. |
| 68 | `home_restore_gaveup_text (other)` | %1$s Dateien konnten auch nach mehreren Versuchen nicht wiederhergestellt werden. Es wurde alles zurückgerollt und nichts geht verloren: Vormerkungen und Papierkorb-Versionen bleiben erhalten. „Erneut versuchen“ oder „Verwerfen“ wählen. | %1$s files could not be restored after several attempts. Everything was rolled back and nothing is lost: the requests and the trash versions are kept. Choose “Try again” or “Discard”. |
| 69 | `home_retry` | Erneut versuchen | Try again |
| 70 | `home_discard` | Verwerfen | Discard |
| 71 | `home_restore_pending_title` | Wiederherstellung vorgemerkt | Restore scheduled |
| 72 | `home_restore_pending_text (one)` | %1$s Datei wird bei der nächsten Synchronisierung mit erreichbarem PC wiederhergestellt. | %1$s file will be restored at the next sync with a reachable PC. |
| 73 | `home_restore_pending_text (other)` | %1$s Dateien werden bei der nächsten Synchronisierung mit erreichbarem PC wiederhergestellt. | %1$s files will be restored at the next sync with a reachable PC. |
| 74 | `home_version` | Version %1$s | Version %1$s |
| 75 | `home_tomorrow` | morgen %1$s | tomorrow %1$s |
| 76 | `home_pause_title` | Wie lange pausieren? | Pause for how long? |
| 77 | `home_pause_hint` | Eine laufende Synchronisierung wird angehalten. Bis zum Ende der Pause startet nichts automatisch. | A running sync is stopped. Nothing starts automatically until the pause ends. |
| 78 | `home_pause_hours` | Eigene Anzahl Stunden (1–168) | Custom number of hours (1–168) |
| 79 | `home_pause_forever` | Für immer (nur manuell) | Forever (manual only) |
| 80 | `home_pause_forever_hint` | Schaltet die Automatik ganz ab: Synchronisiert wird nur noch mit „Synchronisieren“, bis du sie über ▶ oben rechts wieder einschaltest. | Turns automatic syncing off completely: syncing only happens with “Sync” until you turn it back on with ▶ at the top right. |
| 81 | `home_pause_choose` | Wähle eine Dauer. | Choose a duration. |
| 82 | `home_status` | Status | Status |
| 83 | `home_all_saved` | ✓ Alles gesichert und aktuell | ✓ Everything backed up and up to date |
| 84 | `home_wifi_none` | nicht verbunden / nicht lesbar | not connected / not readable |
| 85 | `home_progress` | %1$s: %2$s / %3$s Dateien – %4$s von %5$s | %1$s: %2$s / %3$s files – %4$s of %5$s |
| 86 | `home_running` | Synchronisierung läuft… | Sync running… |
| 87 | `home_manual_only` | Pausiert – nur manuell | Paused – manual syncing only |
| 88 | `home_manual_only_hint` | Es wird nur synchronisiert, wenn du „Synchronisieren“ tippst. ▶ oben rechts schaltet die Automatik wieder ein. | Syncing only happens when you tap “Sync”. ▶ at the top right turns automatic syncing back on. |
| 89 | `home_last_sync` | Letzte Synchronisierung: %1$s (%2$s) | Last sync: %1$s (%2$s) |
| 90 | `home_paused_until` | Pausiert bis %1$s | Paused until %1$s |
| 91 | `home_paused_until_hint` | Automatische Synchronisierungen ruhen bis dahin. „Fortsetzen“ oder „Synchronisieren“ beendet die Pause sofort. | Automatic syncs rest until then. “Resume” or “Sync” ends the pause immediately. |
| 92 | `home_paused_wifi` | Wartet auf das Heim-WLAN. | Waiting for the home Wi-Fi. |
| 93 | `home_paused_wifi_hint` | Die Synchronisierung wurde durch einen WLAN-Unterbruch angehalten und läuft automatisch weiter, sobald das WLAN wieder verbunden ist. | The sync was stopped by a Wi-Fi interruption and continues automatically as soon as the Wi-Fi is connected again. |
| 94 | `home_pc_unreachable_hint` | Ist der PC eingeschaltet und die Freigabe aktiv? „Synchronisieren“ prüft sofort. | Is the PC switched on and the Share active? “Sync” checks right away. |
| 95 | `home_nothing_to_do` | Nichts zu tun – alles aktuell | Nothing to do – everything up to date |
| 96 | `home_pc_unreachable_short` | PC nicht erreichbar (%1$s) | PC not reachable (%1$s) |
| 97 | `home_status_paused` | ⏸ Pausiert | ⏸ Paused |
| 98 | `home_status_pending` | ↻ Noch nicht alles gesichert | ↻ Not everything backed up yet |
| 99 | `home_pending_hint (one)` | %1$s Datei wurde während des Laufs geändert und folgt beim nächsten Sync. | %1$s file changed during the run and follows at the next sync. |
| 100 | `home_pending_hint (other)` | %1$s Dateien wurden während des Laufs geändert und folgen beim nächsten Sync. | %1$s files changed during the run and follow at the next sync. |
| 101 | `home_pending_hint_check (one)` | %1$s Datei wurde während des Laufs geändert und folgt mit der nächsten Prüfung. | %1$s file changed during the run and follows with the next check. |
| 102 | `home_pending_hint_check (other)` | %1$s Dateien wurden während des Laufs geändert und folgen mit der nächsten Prüfung. | %1$s files changed during the run and follow with the next check. |
| 103 | `home_profile_access_lost` | ⚠ Handy-Ordner: Zugriff verloren – tippen und neu wählen | ⚠ Phone folder: access lost – tap to choose again |
| 104 | `home_no_sync_yet` | Noch keine Synchronisierung durchgeführt. | No sync yet. |
| 105 | `home_no_success_yet` | Noch keine erfolgreiche Synchronisierung. | No successful sync yet. |
| 106 | `home_next_check` | Nächste Prüfung um %1$s | Next check at %1$s |
| 107 | `home_skipped` | ⚠ Automatischer Start übersprungen – %1$s | ⚠ Automatic start skipped – %1$s |
| 108 | `home_skipped_manual` | ⚠ Start nicht möglich – %1$s | ⚠ Start not possible – %1$s |
| 109 | `home_resume` | Fortsetzen | Resume |
| 110 | `home_sync` | Synchronisieren | Sync |
| 111 | `home_sync_needs_profile` | Lege zuerst ein Profil an. | Create a profile first. |
| 112 | `home_status_attention (one)` | Entscheidung erforderlich | Decision required |
| 113 | `home_status_attention (other)` | %1$s Entscheidungen erforderlich | %1$s decisions required |
| 114 | `home_status_conflict_detail (one)` | %1$s Konflikt wartet auf deine Entscheidung. | %1$s conflict is waiting for your decision. |
| 115 | `home_status_conflict_detail (other)` | %1$s Konflikte warten auf deine Entscheidung. | %1$s conflicts are waiting for your decision. |
| 116 | `home_status_attention_detail` | Etwas wartet auf deine Entscheidung – siehe die Karten auf dieser Seite. | Something is waiting for your decision – see the cards on this page. |
| 117 | `home_status_no_connection` | Keine Verbindung | No connection |
| 118 | `home_no_connection_wifi` | Kein passendes WLAN – Synchronisierung wartet | No matching Wi-Fi – sync is waiting |
| 119 | `home_no_connection_hint` | Verbinde dich mit dem WLAN des Profils. „Synchronisieren“ versucht es trotzdem sofort. | Connect to the Wi-Fi set in the profile. “Sync” still tries right away. |
| 120 | `home_status_errors (one)` | Letzte Synchronisierung: %1$s Fehler | Last sync: %1$s error |
| 121 | `home_status_errors (other)` | Letzte Synchronisierung: %1$s Fehler | Last sync: %1$s errors |
| 122 | `home_status_failed` | Letzte Synchronisierung fehlgeschlagen | Last sync failed |
| 123 | `home_status_errors_hint` | Tippen öffnet den Verlauf. | Tap to open the history. |
| 124 | `home_status_not_configured` | Noch nichts eingerichtet | Nothing set up yet |
| 125 | `home_status_no_active` | Kein aktives Profil | No active profile |
| 126 | `home_no_active_detail` | Aktiviere ein Profil oder schliesse seine Einrichtung ab. | Turn on a profile or finish setting it up. |
| 127 | `home_status_idle` | Bereit | Ready |
| 128 | `home_sync_feedback_wifi` | Kein passendes WLAN – versuche es trotzdem | No matching Wi-Fi – trying anyway |
| 129 | `home_sync_feedback_pc` | PC nicht erreichbar – prüfe erneut | PC not reachable – checking again now |
| 130 | `home_profile_disabled` | deaktiviert | disabled |
| 131 | `home_profile_no_target` | Kein Ziel festgelegt | No target set |
| 132 | `home_profile_no_local` | kein Handy-Ordner | no phone folder |
| 133 | `home_profile_propagate` | Löschungen übernehmen | apply deletions |
| 134 | `home_profile_sources` | Quellen: %1$s | Sources: %1$s |
| 135 | `home_profile_none` | keine | none |
| 136 | `home_profile_counts` | %1$d gesichert · %2$d offen | %1$d backed up · %2$d open |
| 137 | `home_profile_mirror_synced (one)` | %1$s Datei synchronisiert | %1$s file synced |
| 138 | `home_profile_mirror_synced (other)` | %1$s Dateien synchronisiert | %1$s files synced |
| 139 | `home_profile_open_files` | %1$s offen | %1$s open |
| 140 | `home_waiting_files (one)` | %1$s Datei wartet auf die Synchronisierung. | %1$s file is waiting for synchronization. |
| 141 | `home_waiting_files (other)` | %1$s Dateien warten auf die Synchronisierung. | %1$s files are waiting for synchronization. |
| 142 | `home_profile_open_conflicts (one)` | %1$s Konflikt offen | %1$s conflict open |
| 143 | `home_profile_open_conflicts (other)` | %1$s Konflikte offen | %1$s conflicts open |
| 144 | `home_profile_awaiting` | Wartet auf den ersten Start – zuerst im Profil simulieren oder „Synchronisieren“ tippen | Waiting for the first start – simulate in the profile first or tap “Sync” |
| 145 | `guard_choose_new` | Neuen Ordner wählen | Choose new folder |
| 146 | `guard_recheck` | Erneut prüfen (Ordner wiederhergestellt) | Check again (folder restored) |
| 147 | `guard_reset` | Neu anlegen und abgleichen | Create anew and compare |
| 148 | `guard_reset_hint` | „Neu anlegen“ gleicht alles neu ab, ohne etwas zu löschen. Bis du entscheidest, ruht nur dieses Profil. | “Create anew” compares everything again without deleting anything. Until you decide, only this profile rests. |
| 149 | `guard_keep` | Nicht löschen | Don't delete |
| 150 | `guard_delete_anyway` | Trotzdem in den Papierkorb | Move to trash anyway |
| 151 | `guard_title_volume` | Anderes Laufwerk erkannt | Different drive detected |
| 152 | `guard_text_volume` | Hinter der Freigabe von „%1$s“ liegt ein anderes Laufwerk als bisher. Mögliche Ursachen: Festplatte oder NAS getauscht, Laufwerk neu formatiert oder ein anderes Gerät antwortet unter derselben Adresse. Es wurde nichts verändert. Bis du entscheidest, bleibt das Profil angehalten. | Behind the share of “%1$s” there is a different drive than before. Possible causes: hard disk or NAS replaced, drive reformatted, or another device answers at the same address. Nothing was changed. Until you decide, the profile stays paused. |
| 153 | `guard_volume_continue` | Ist das richtige Laufwerk – weiter | It is the right drive – continue |
| 154 | `guard_volume_hint` | Der nächste Lauf prüft zuerst den PC-Ordner gegen den letzten Sync-Stand, bevor etwas kopiert oder gelöscht wird. | The next sync first checks the PC folder against the last synced state before anything is copied or deleted. |
| 155 | `guard_keep_hint` | „Nicht löschen“ behält die Dateien bzw. stellt sie auf der anderen Seite wieder her. | “Don't delete” keeps the files or restores them on the other side. |
| 156 | `guard_choose_other` | Anderen Ordner wählen | Choose another folder |
| 157 | `guard_accept` | Trotzdem übernehmen | Accept anyway |
| 158 | `guard_as_new` | Als neuen Ordner behandeln | Treat as a new folder |
| 159 | `perm_incomplete` | Einrichtung unvollständig | Setup incomplete |
| 160 | `perm_grant` | Erteilen | Grant |
| 161 | `perm_granted` | ✓ erledigt | ✓ done |
| 162 | `perm_open` | Öffnen | Open |
| 163 | `perm_media` | Zugriff auf Fotos & Videos | Access to photos & videos |
| 164 | `perm_media_location` | Standort in Fotos lesen (für Länderordner) | Read location in photos (for country folders) |
| 165 | `perm_notifications` | Benachrichtigungen (Fortschritt) | Notifications (progress) |
| 166 | `perm_battery` | Akku: LeoSync in der Liste auf „Nicht optimieren“ stellen (zuverlässige Hintergrund-Synchronisierung) | Battery: set LeoSync to “Not optimised” in the list (reliable background sync) |
| 167 | `home_sync_deferred_snack` | Wird nach der laufenden Synchronisierung gestartet. | Will start after the current sync. |
| 168 | `home_next_all` | Danach: alle Profile | Next: all profiles |
| 169 | `home_next_auto` | Danach folgt eine weitere Synchronisierung. | Another sync follows. |
| 170 | `perm_blocked_title` | Berechtigung gesperrt | Permission blocked |
| 171 | `perm_blocked_text` | Android fragt nicht mehr. Öffne App-Info → Berechtigungen und erlaube den Zugriff. | Android no longer asks. Open App info → Permissions and allow the access. |

## Einstellungen

| Nr. | Schlüssel | Deutsch | Englisch |
|---:|---|---|---|
| 172 | `settings_notifications` | Benachrichtigungen | Notifications |
| 173 | `settings_transfer` | Übertragung | Transfer |
| 174 | `settings_permissions` | App-Berechtigungen | App permissions |
| 175 | `settings_app` | Sprache & Sicherung | Language & backup |
| 176 | `settings_diagnose` | Diagnose | Diagnostics |
| 177 | `settings_help` | Hilfe & Produktseite | Help & product page |
| 178 | `settings_notify_summary` | Meldung nach jeder Synchronisierung | Message after every sync |
| 179 | `settings_notify_summary_sub` | z. B. „3 Dateien übertragen“ | e.g. “3 files transferred” |
| 180 | `settings_notify_hints` | Hinweise | Hints |
| 181 | `settings_notify_hints_sub` | Ordnerauswahl nötig, Konflikte, Dateien entfernen, Fehler. Erscheinen auch ohne Benachrichtigung auf der Startseite. | Folder selection needed, conflicts, removing files, errors. Also shown on the home screen without a notification. |
| 182 | `settings_notify_fixed` | Die Fortschrittsanzeige während einer Synchronisierung und „Pausiert – wartet auf WLAN“ lassen sich nicht abschalten: Android verlangt sie, damit die Synchronisierung im Hintergrund weiterlaufen bzw. fortgesetzt werden kann. | The progress display during a sync and “Paused – waiting for Wi-Fi” cannot be turned off: Android requires them so the sync can keep running or resume in the background. |
| 183 | `settings_notify_android` | Android-Einstellungen für Benachrichtigungen | Android notification settings |
| 184 | `settings_stale` | Erinnerung | Reminder |
| 185 | `settings_stale_switch` | Erinnern, wenn lange nicht gesichert | Remind me when nothing was backed up for a long time |
| 186 | `settings_stale_sub` | Nur wenn neue Fotos/Videos warten. Wiederholt sich alle %1$s Tage. | Only when new photos/videos are waiting. Repeats every %1$s days. |
| 187 | `settings_stale_days` | Nach … Tagen ohne Sicherung | After … days without backup |
| 188 | `settings_auto` | Automatik | Automatic |
| 189 | `settings_auto_wifi` | Synchronisieren beim Verbinden mit Heim-WLAN | Sync when connecting to home Wi-Fi |
| 190 | `settings_auto_wifi_sub` | Startet sofort, wenn ein WLAN eines Profils verbunden wird | Starts as soon as a Wi-Fi network of a profile connects |
| 191 | `settings_auto_offwifi` | Auch über mobile Daten oder VPN synchronisieren | Also sync over mobile data or VPN |
| 192 | `settings_auto_offwifi_sub` | Ausserhalb des WLANs wird der WLAN-Name nicht geprüft. Startet bei neuen Fotos, über das Sicherheitsnetz und die Prüfung der Ordner-Profile. Kann viel Datenvolumen verbrauchen und Kosten beim Mobilfunkabo verursachen (z. B. im Ausland). Der PC muss erreichbar sein (VPN). | Off Wi-Fi the Wi-Fi name is not checked. Starts on new photos, by the safety net and by folder profile checks. May use a lot of mobile data and cause costs with your mobile plan (e.g. roaming). The PC must be reachable (VPN). |
| 193 | `settings_auto_new` | Neue Fotos/Videos sofort übertragen | Transfer new photos/videos right away |
| 194 | `settings_auto_new_sub` | Nur, wenn gerade ein Heim-WLAN verbunden ist | Only while a home Wi-Fi is connected |
| 195 | `settings_auto_charging` | Nur beim Laden | Only while charging |
| 196 | `settings_auto_charging_sub` | Automatische Synchronisierungen nur am Ladegerät (manuelles Synchronisieren immer möglich) | Automatic syncs only on the charger (manual sync always possible) |
| 197 | `settings_min_age` | Mindestalter für Fotos (Minuten) | Minimum photo age (minutes) |
| 198 | `settings_min_age_hint` | Gilt für Fotoprofile. Neue Fotos und Videos werden übertragen, sobald das neuste davon dieses Alter erreicht hat. So kommt eine ganze Serie in einem Durchgang auf den PC, und Fotos, die gerade bearbeitet werden, werden nicht zu früh übertragen. | Applies to photo profiles. New photos and videos are transferred once the newest of them has reached this age. A whole series is sent in one run, and photos still being edited are not transferred too early. |
| 199 | `settings_folder_check` | Ordner-Profile: Prüfintervall (Minuten) | Folder profiles: check interval (minutes) |
| 200 | `settings_folder_check_hint` | Gilt nur für Ordner-Profile: So oft fragt die App den PC nach Änderungen (mindestens 15), solange das Handy im Ziel-WLAN ist, und zusätzlich sofort beim Verbinden. Fotoprofile richten sich nach dem Mindestalter. | Applies only to folder profiles: how often the app asks the PC for changes (at least 15) while the phone is on the target Wi-Fi, and also right when connecting. Photo profiles follow the minimum photo age. |
| 201 | `settings_err_folder_check` | Bitte eine Zahl von 15 bis 1440 eingeben. | Enter a number from 15 to 1440. |
| 202 | `settings_parallel` | Parallele Übertragungen | Parallel transfers |
| 203 | `settings_parallel_value` | Gleichzeitige Übertragungen: %1$s | Simultaneous transfers: %1$s |
| 204 | `settings_parallel_hint` | Mehr Verbindungen beschleunigen viele kleine Dateien. Bei grossen Videos bringt es wenig. Standard: 3 | More connections speed up many small files. For large videos it helps little. Default: 3 |
| 205 | `settings_perm_title` | Berechtigungen | Permissions |
| 206 | `settings_perm_hint` | Diese Berechtigungen braucht LeoSync für die automatische Synchronisierung im Hintergrund. | LeoSync needs these permissions for automatic syncing in the background. |
| 207 | `settings_perm_app_info` | App-Info öffnen | Open app info |
| 208 | `settings_background_hint` | Damit Android die Synchronisierung nicht verzögert oder abbricht, sollte die Akku-Optimierung für LeoSync aus sein. | To keep Android from delaying or stopping the sync, battery optimization should be off for LeoSync. |
| 209 | `settings_battery` | Akku-Optimierungen | Battery optimization |
| 210 | `settings_vendor_extra` | Zusätzlich bei %1$s: | Additionally on %1$s: |
| 211 | `settings_vendor_note` | Die Bezeichnungen können je nach Software-Stand leicht abweichen. | The labels may differ slightly depending on the software version. |
| 212 | `settings_vendor_web` | Anleitung im Browser | Instructions in the browser |
| 213 | `settings_language` | Sprache | Language |
| 214 | `settings_language_system` | Systemsprache | System language |
| 215 | `settings_language_hint` | „Systemsprache“ zeigt Deutsch, wenn das Handy auf Deutsch eingestellt ist, sonst Englisch. | “System language” shows German if the phone is set to German, otherwise English. |
| 216 | `settings_geo_loading` | Lade Länderdaten… | Loading country data… |
| 217 | `settings_geo_loaded` | %1$s Länder geladen (offline) | %1$s countries loaded (offline) |
| 218 | `settings_geo_missing` | Länderdaten fehlen – Ordner werden ohne Land angelegt | Country data missing – folders are created without a country |
| 219 | `settings_country_folders` | Länderordner | Country folders |
| 220 | `settings_home_country` | Heimatland (ISO-Code) | Home country (ISO code) |
| 221 | `settings_home_country_hint` | Fotos aus diesem Land bekommen keinen Länderzusatz (Standard: CH = Schweiz). | Photos from this country get no country suffix (default: CH = Switzerland). |
| 222 | `settings_diag_title` | Beta / Diagnose | Beta / Diagnostics |
| 223 | `settings_diag_hint` | Sammelt Angaben zu Gerät, Hintergrund-Einschränkungen, Berechtigungen, Profilen und den letzten Läufen und kopiert sie in die Zwischenablage. Passwörter, Benutzernamen, IP-Adressen, Rechner-, WLAN- und Ordnernamen, Pfade und genaue Dateinamen fehlen oder sind maskiert. Geräte-ID: %1$s | Collects details about the device, background restrictions, permissions, profiles and the latest runs and copies them to the clipboard. Passwords, user names, IP addresses, computer, Wi-Fi and folder names, paths and exact file names are left out or masked. Device ID: %1$s |
| 224 | `settings_diag_failed` | Diagnose fehlgeschlagen: %1$s | Diagnostics failed: %1$s |
| 225 | `settings_diag_busy` | Sammle Daten… | Collecting data… |
| 226 | `settings_report` | Problem melden | Report a problem |
| 227 | `settings_report_hint` | Öffnet die Mail-App mit einer Nachricht an %1$s und den Diagnosedaten im Anhang. Es wird nichts automatisch gesendet: Nachricht prüfen und selbst senden. Die Daten können Serveradressen und Dateinamen enthalten (keine Passwörter). | Opens your mail app with a message to %1$s and the diagnostic data attached. Nothing is sent automatically: check the message and send it yourself. The data may contain server addresses and file names (no passwords). |
| 228 | `settings_report_no_mail` | Keine Mail-App gefunden. Die Adresse %1$s wurde in die Zwischenablage kopiert. | No mail app found. The address %1$s was copied to the clipboard. |
| 229 | `report_subject` | LeoSync %1$s – Problembericht | LeoSync %1$s – problem report |
| 230 | `report_body` | Bitte beschreibe hier das Problem (was hast du gemacht, was hast du erwartet, was ist passiert?): /  /  | Please describe the problem here (what did you do, what did you expect, what happened?): /  /  |
| 231 | `report_chooser` | Problem melden | Report a problem |
| 232 | `settings_diag_copy` | Diagnosedaten kopieren | Copy diagnostic data |
| 233 | `settings_about` | LeoSync %1$s – Open Source (MIT) | LeoSync %1$s – Open Source (MIT) |
| 234 | `settings_diag_copied` | Diagnosedaten in die Zwischenablage kopiert | Diagnostic data copied to the clipboard |
| 235 | `settings_err_days` | Bitte eine Zahl von 1 bis 365 eingeben. | Enter a number from 1 to 365. |
| 236 | `settings_err_minutes` | Bitte eine Zahl von 0 bis 1440 eingeben. | Enter a number from 0 to 1440. |
| 237 | `settings_err_country` | Bitte einen gültigen Ländercode aus zwei Buchstaben eingeben (z. B. CH). | Enter a valid two-letter country code (e.g. CH). |

## Profil und Assistent

| Nr. | Schlüssel | Deutsch | Englisch |
|---:|---|---|---|
| 238 | `profile_default_photos` | Fotos | Photos |
| 239 | `profile_default_folder` | Ordner | Folder |
| 240 | `profile_folder_unusable` | Dieser Ordner kann nicht verwendet werden. Bitte einen Unterordner wählen. | This folder cannot be used. Please choose a subfolder. |
| 241 | `profile_no_persistent_access` | Für diesen Ordner wurde kein dauerhafter Zugriff erteilt. | No permanent access was granted for this folder. |
| 242 | `profile_saved_toast` | Gespeichert | Saved |
| 243 | `profile_title_new` | Neues Profil | New profile |
| 244 | `profile_title_edit` | Profil bearbeiten | Edit profile |
| 245 | `profile_step` | Schritt %1$s von %2$s: %3$s | Step %1$s of %2$s: %3$s |
| 246 | `profile_step_optional` | Schritt %1$s von %2$s: %3$s (optional) | Step %1$s of %2$s: %3$s (optional) |
| 247 | `profile_general` | Allgemein | General |
| 248 | `profile_name` | Profilname | Profile name |
| 249 | `profile_active` | Profil aktiv | Profile active |
| 250 | `profile_type_mirror` | Typ: %1$s – 1:1-Spiegelung mit Unterordnern | Type: %1$s – 1:1 mirror with subfolders |
| 251 | `profile_type_photos` | Typ: %1$s – sortiert nach Jahr und Monat | Type: %1$s – sorted by year and month |
| 252 | `profile_local_folder` | Ordner auf dem Handy | Folder on the phone |
| 253 | `profile_no_folder_yet` | Noch kein Ordner gewählt. | No folder chosen yet. |
| 254 | `profile_access_lost` | Zugriff verloren – bitte erneut wählen. | Access lost – please choose again. |
| 255 | `profile_choose_folder` | Ordner wählen… | Choose folder… |
| 256 | `profile_choose_other_folder` | Anderen Ordner wählen… | Choose another folder… |
| 257 | `profile_local_folder_hint` | Alle Dateien in diesem Ordner und seinen Unterordnern werden mit dem PC abgeglichen. Android fragt einmalig, ob LeoSync auf den Ordner zugreifen darf (nötig für alle Dateitypen). | All files in this folder and its subfolders are compared with the PC. Android asks once whether LeoSync may access the folder (needed for all file types). |
| 258 | `profile_sources` | Quellordner auf dem Handy | Source folders on the phone |
| 259 | `profile_sources_empty` | Noch kein Ordner gewählt (z. B. DCIM/Camera). | No folder chosen yet (e.g. DCIM/Camera). |
| 260 | `profile_remove` | Entfernen | Remove |
| 261 | `profile_add_folder` | Ordner hinzufügen… | Add folder… |
| 262 | `profile_sources_hint` | Öffnet die Android-Dateiauswahl: Ordner wählen und „Diesen Ordner verwenden“ tippen. Kamera-Fotos liegen meist in „DCIM/Camera“, Bildschirmfotos in „Pictures/Screenshots“. | Opens the Android file picker: choose a folder and tap “Use this folder”. Camera photos are usually in “DCIM/Camera”, screenshots in “Pictures/Screenshots”. |
| 263 | `profile_subfolders` | Unterordner einbeziehen | Include subfolders |
| 264 | `profile_subfolders_sub` | Auch Fotos in Ordnern innerhalb der gewählten Ordner sichern | Also back up photos in folders inside the chosen folders |
| 265 | `profile_photos` | Fotos | Photos |
| 266 | `profile_videos` | Videos | Videos |
| 267 | `profile_target` | Ziel (Windows-Freigabe) | Target (Windows Share) |
| 268 | `profile_copy_credentials_button` | Zugangsdaten übernehmen… | Copy credentials… |
| 269 | `profile_copy_credentials_title` | Zugangsdaten übernehmen | Copy credentials |
| 270 | `profile_copy_credentials_hint` | Übernimmt Benutzer, Passwort und Domäne. Der Freigabe-Pfad wird nur eingetragen, wenn er noch leer ist. | Copies user, password and domain. The Share path is only filled in if it is still empty. |
| 271 | `profile_copy_credentials_profiles` | Verwendet in: %1$s | Used in: %1$s |
| 272 | `profile_credentials_copied` | Zugangsdaten von „%1$s“ übernommen | Credentials of “%1$s” copied |
| 273 | `profile_share_path` | Freigabe-Pfad | Share path |
| 274 | `profile_share_example` | Fotos | Photos |
| 275 | `profile_share_format` | Es reicht der PC, z. B. \\192.168.1.20. Freigabe und Ordner wählst du danach mit „Ordner auf dem PC wählen…“. Eintippen geht auch: \\Server\Freigabe\Ordner (IP-Adresse oder Name). | The PC is enough, e.g. \\192.168.1.20. Then pick the Share and folder with “Choose folder on the PC…”. Typing works too: \\Server\Share\Folder (IP address or name). |
| 276 | `profile_pick_pc_folder` | Ordner auf dem PC wählen… | Choose folder on the PC… |
| 277 | `profile_user` | Benutzer | User |
| 278 | `profile_password` | Passwort | Password |
| 279 | `profile_domain` | Domäne / PC-Name (optional) | Domain / PC name (optional) |
| 280 | `profile_domain_hint` | Bei einem lokalen Windows-Konto meist leer lassen. Microsoft-Konto: E-Mail-Adresse als Benutzer. | Usually leave empty for a local Windows account. Microsoft account: use the e-mail address as user. |
| 281 | `profile_test` | Verbindung testen | Test connection |
| 282 | `profile_add` | Hinzufügen | Add |
| 283 | `profile_direction` | Richtung und Änderungen | Direction and changes |
| 284 | `profile_direction_hint` | In welche Richtung wird abgeglichen? | In which direction should it sync? |
| 285 | `profile_propagate` | Löschungen übernehmen | Apply deletions |
| 286 | `profile_propagate_sub` | Wird eine Datei auf einer Seite gelöscht, kommt sie auf der anderen Seite in den Ordner „%1$s“. Aus: gelöschte Dateien werden wiederhergestellt. | If a file is deleted on one side, it goes to the folder “%1$s” on the other side. Off: deleted files are restored. |
| 287 | `profile_keep_versions` | Alte Versionen im Papierkorb behalten | Keep old versions in the trash |
| 288 | `profile_keep_versions_sub` | Wird eine Datei überschrieben, kommt die alte Version in den Ordner „%1$s“ und lässt sich über den Papierkorb zurückholen. Braucht etwas mehr Speicher. | If a file is overwritten, the old version goes to the folder “%1$s” and can be brought back via the trash. Needs a bit more storage. |
| 289 | `profile_trash_days` | Papierkorb leeren nach … Tagen | Empty trash after … days |
| 290 | `profile_trash_days_hint` | Ältere Einträge in „%1$s“ werden auf Handy und PC endgültig gelöscht. 0 = nie. Standard: 30 | Older entries in “%1$s” are permanently deleted on phone and PC. 0 = never. Default: 30 |
| 291 | `profile_conflict_question` | Wenn eine Datei auf beiden Seiten geändert wurde: | If a file was changed on both sides: |
| 292 | `profile_conflict_newer_hint` | Die ältere Version wird in den Papierkorb verschoben, nicht gelöscht. | The older version is moved to the trash, not deleted. |
| 293 | `profile_after_upload` | Nach erfolgreicher Übertragung | After a successful transfer |
| 294 | `profile_after_upload_hint` | Was passiert mit den Fotos auf dem Handy, wenn sie sicher auf dem PC liegen? | What happens to the photos on the phone once they are safely on the PC? |
| 295 | `profile_delete_hint` | Dateien werden nur entfernt, wenn der Upload geprüft wurde (Grösse + Prüfsumme). Android fragt dabei einmal pro Durchgang nach Bestätigung. | Files are only removed if the upload was verified (size + checksum). Android asks for confirmation once per run. |
| 296 | `profile_delete_final_hint` | Achtung: Endgültig gelöschte Fotos kommen nicht in den Papierkorb der Galerie und lassen sich auf dem Handy nicht wiederherstellen. Sie sind danach nur noch auf dem PC. | Caution: permanently deleted photos do not go to the gallery trash and cannot be restored on the phone. Afterwards they only exist on the PC. |
| 297 | `profile_verify` | Prüfung nach dem Hochladen | Check after upload |
| 298 | `profile_verify_hint` | Liest die Datei auf dem PC zurück und vergleicht sie mit dem Original. Gründlicher heisst sicherer, aber langsamer. | Reads the file back from the PC and compares it with the original. More thorough means safer, but slower. |
| 299 | `profile_verify_required` | Mindestens „Wenig“ ist Pflicht, weil Dateien vom Handy entfernt werden. | At least “Low” is required because files are removed from the phone. |
| 300 | `verify_off` | Aus | Off |
| 301 | `verify_off_sub` | Nur die Dateigrösse wird verglichen. Nur möglich, wenn die Fotos auf dem Handy bleiben. | Only the file size is compared. Only possible if the photos stay on the phone. |
| 302 | `verify_low` | Wenig | Low |
| 303 | `verify_low_sub` | Anfang, Mitte und Ende (192 KB pro Datei) | Start, middle and end (192 KB per file) |
| 304 | `verify_medium` | Mittel | Medium |
| 305 | `verify_medium_sub` | Bis 32 MB die ganze Datei, darüber 32 Stichproben (2 MB). Standard | Up to 32 MB the whole file, above that 32 samples (2 MB). Default |
| 306 | `verify_high` | Viel | High |
| 307 | `verify_high_sub` | Bis 256 MB die ganze Datei, darüber 128 Stichproben (8 MB) | Up to 256 MB the whole file, above that 128 samples (8 MB) |
| 308 | `verify_full` | Komplett | Complete |
| 309 | `verify_full_sub` | Die ganze Datei. Braucht deutlich mehr Zeit und viel Akku. | The whole file. Takes much more time and a lot of battery. |
| 310 | `verify_full_title` | Komplett prüfen? | Check completely? |
| 311 | `verify_full_text` | Jede Datei wird nach dem Hochladen vollständig vom PC zurückgelesen. Bei Videos dauert die Sicherung dadurch etwa doppelt so lange und braucht deutlich mehr Akku. | Every file is read back completely from the PC after uploading. For videos the backup takes about twice as long and uses considerably more battery. |
| 312 | `verify_full_confirm` | Komplett prüfen | Check completely |
| 313 | `verify_raised` | Prüfung auf „Mittel“ gestellt: Wer Fotos vom Handy entfernt, braucht eine Prüfung. | Check set to “Medium”: removing photos from the phone requires a check. |
| 314 | `profile_filter` | Filter (optional) | Filter (optional) |
| 315 | `profile_filter_include` | Nur diese Dateien | Only these files |
| 316 | `profile_filter_exclude` | Diese Dateien ausschliessen | Exclude these files |
| 317 | `profile_filter_hint` | Mehrere Muster mit Komma oder Zeilenumbruch trennen. * = beliebig, ? = ein Zeichen, Ordner/** = alles in diesem Ordner. Ist „Nur diese Dateien“ leer, gilt alles. „Ausschliessen“ hat Vorrang. | Separate several patterns with a comma or line break. * = anything, ? = one character, folder/** = everything in this folder. If “Only these files” is empty, everything counts. “Exclude” takes precedence. |
| 318 | `profile_section_unset` | Nicht eingestellt – antippen zum Öffnen | Not set – tap to open |
| 319 | `profile_section_collapse` | Einklappen | Collapse |
| 320 | `profile_section_collapsed_value` | Eingeklappt, Wert: %1$s – antippen zum Öffnen | Collapsed, value: %1$s – tap to open |
| 321 | `profile_guard` | Schutzbremse (optional) | Safety brake (optional) |
| 322 | `profile_guard_percent` | Nachfragen, wenn mehr als … % gelöscht würden | Ask if more than … % would be deleted |
| 323 | `profile_guard_hint` | Sollen auf einmal mehr als so viele Prozent der Dateien in den Papierkorb, hält die Synchronisierung an und fragt dich zuerst. Standard: 20. Unabhängig davon fragt LeoSync immer ab 100 Löschungen auf einmal und wenn ein ganzer Unterordner mit mehr als 5 Dateien verschwinden würde. Bei höchstens 5 Dateien im Profil nur, wenn alle gelöscht würden. | If more than this percentage of files would go to the trash at once, the sync stops and asks you first. Default: 20. Regardless of this, LeoSync always asks from 100 deletions at once and when a whole subfolder with more than 5 files would disappear. With at most 5 files in the profile only if all would be deleted. |
| 324 | `profile_next` | Weiter | Next |
| 325 | `profile_optional_hint` | Die weiteren Schritte sind optional – du kannst jetzt schon speichern. | The remaining steps are optional – you can already save now. |
| 326 | `profile_simulation` | Simulation | Simulation |
| 327 | `profile_simulation_hint` | Zeigt, was beim Synchronisieren mit diesem Profil passieren würde – ohne etwas zu übertragen. | Shows what would happen when syncing with this profile – without transferring anything. |
| 328 | `profile_simulate` | Simulieren | Simulate |
| 329 | `profile_simulation_save_first` | Zuerst speichern – simuliert wird der gespeicherte Stand. | Save first – the saved state is simulated. |
| 330 | `profile_maintenance` | Wartung | Maintenance |
| 331 | `profile_maintenance_hint` | Vergisst, welche Dateien bereits übertragen wurden. Bei der nächsten Synchronisierung wird alles mit der Freigabe abgeglichen (identische Dateien werden nicht erneut übertragen). | Forgets which files were already transferred. The next sync compares everything with the Share (identical files are not transferred again). |
| 332 | `profile_reset` | Abgleichstand zurücksetzen | Reset sync status |
| 333 | `profile_delete_button` | Profil löschen | Delete profile |
| 334 | `profile_maintenance_closed` | Abgleichstand zurücksetzen, Profil löschen – antippen zum Öffnen | Reset sync status, delete profile – tap to open |
| 335 | `profile_delete_section_hint` | Löscht Einstellungen und Abgleichstand dieses Profils. Dateien auf Handy und PC bleiben erhalten. | Deletes the settings and sync status of this profile. Files on the phone and PC are kept. |
| 336 | `profile_delete_title` | Profil löschen? | Delete profile? |
| 337 | `profile_delete_text` | Einstellungen und Abgleichstand dieses Profils werden gelöscht. Dateien auf Handy und PC bleiben erhalten. | Settings and sync status of this profile are deleted. Files on the phone and PC are kept. |
| 338 | `profile_created_title` | Profil gespeichert | Profile saved |
| 339 | `profile_created_text` | Wie weiter? Bis du dieses Profil einmal synchronisierst, startet es nicht automatisch. So kannst du zuerst in Ruhe simulieren. | What next? Until you sync this profile once, it does not start automatically. That way you can simulate first. |
| 340 | `profile_later` | Später | Later |
| 341 | `profile_leave_title` | Änderungen speichern? | Save changes? |
| 342 | `profile_leave_text` | Du hast Änderungen an diesem Profil noch nicht gespeichert. | You have unsaved changes to this profile. |
| 343 | `profile_exists_title` | Profil existiert bereits | Profile already exists |
| 344 | `profile_overlap_title` | Profile überschneiden sich | Profiles overlap |
| 345 | `profile_not_saved` | Das Profil wurde nicht gespeichert. | The profile was not saved. |
| 346 | `profile_layout_blocked_title` | Ordnerstruktur passt nicht | Folder structure does not match |
| 347 | `profile_layout_title` | Ordnerstruktur | Folder structure |
| 348 | `profile_layout_pick_title` | Ordnerstruktur wählen | Choose folder structure |
| 349 | `profile_layout_hint` | Kann nach der ersten Synchronisierung nicht mehr geändert werden. | Cannot be changed after the first synchronisation. |
| 350 | `profile_layout_example_none` | Ohne Land: 2026\06\IMG_1.jpg | Without a country: 2026\06\IMG_1.jpg |
| 351 | `profile_layout_ex_month_suffix` | Beispiel: 2026\06 - Ägypten\IMG_1.jpg | Example: 2026\06 - Egypt\IMG_1.jpg |
| 352 | `profile_layout_ex_country_folder` | Beispiel: 2026\Ägypten\06\IMG_1.jpg | Example: 2026\Egypt\06\IMG_1.jpg |
| 353 | `profile_layout_ex_country_subfolder` | Beispiel: 2026\06\Ägypten\IMG_1.jpg | Example: 2026\06\Egypt\IMG_1.jpg |
| 354 | `profile_layout_adopted` | Von Profil „%1$s“ übernommen, das dasselbe Ziel verwendet. Alle Fotoprofile mit demselben Ziel müssen dieselbe Struktur verwenden. | Taken over from profile “%1$s”, which uses the same target. All photo profiles of one target must use the same structure. |
| 355 | `profile_layout_locked` | Die Struktur ist fest, weil von diesem Profil bereits Dateien synchronisiert wurden. Sie kann nicht mehr geändert werden. | The structure is fixed because files of this profile have already been synchronised. It can no longer be changed. |
| 356 | `profile_layout_running` | Die Struktur kann nicht geändert werden, solange eine Synchronisierung läuft. Versuche es nach deren Ende erneut. | The structure cannot be changed while a synchronisation is running. Try again when it has finished. |
| 357 | `profile_save_anyway` | Trotzdem speichern | Save anyway |
| 358 | `profile_checking_folder` | Prüfe Ordner… | Checking folder… |
| 359 | `profile_checking_folder_text` | Passt der neue Ordner zum bisherigen Abgleichstand? | Does the new folder match the previous sync status? |
| 360 | `profile_folder_warning_title` | Ist das wirklich der richtige Ordner? | Is this really the right folder? |
| 361 | `profile_folder_warning_text` | Der gewählte Ordner passt nicht zum bisherigen Abgleichstand dieses Profils. Nach einer Neuinstallation oder einem Import ist das normal: Wähle dann „Trotzdem übernehmen“. | The chosen folder does not match the previous sync status of this profile. After a reinstall or an import this is expected: choose “Accept anyway”. |
| 362 | `profile_reset_title` | Abgleichstand zurücksetzen? | Reset sync status? |
| 363 | `profile_reset_text` | Die nächste Synchronisierung gleicht alle Dateien erneut mit der Freigabe ab. Das kann bei vielen Dateien länger dauern. | The next sync compares all files with the Share again. With many files this can take a while. |
| 364 | `profile_reset_done` | Zurückgesetzt | Reset |
| 365 | `profile_reset_button` | Zurücksetzen | Reset |
| 366 | `wizard_intro_name` | Wie soll das Profil heissen? Der Name erscheint auf der Startseite, im Verlauf und in den Benachrichtigungen, z. B. „Kamera“ oder „Dokumente“. | What should the profile be called? The name appears on the home screen, in the history and in notifications, e.g. “Camera” or “Documents”. |
| 367 | `wizard_intro_source_mirror` | Welcher Ordner auf dem Handy soll mit dem PC abgeglichen werden? Alle Dateien und Unterordner darin werden synchronisiert. | Which folder on the phone should be compared with the PC? All files and subfolders in it are synced. |
| 368 | `wizard_intro_source_photos` | Welche Fotos und Videos sollen gesichert werden? Wähle die Ordner auf dem Handy, in denen sie liegen. | Which photos and videos should be backed up? Choose the folders on the phone where they are stored. |
| 369 | `wizard_intro_target` | Wohin auf dem PC? Gib die Windows-Freigabe an und melde dich mit dem Windows-Benutzer an, der darauf zugreifen darf. „Verbindung testen“ zeigt, ob alles stimmt. | Where on the PC? Enter the Windows Share and sign in with the Windows user who may access it. “Test connection” shows whether everything is right. |
| 370 | `wizard_intro_options_mirror` | Wann wird synchronisiert, in welche Richtung, und was passiert mit gelöschten oder geänderten Dateien? | When does it sync, in which direction, and what happens to deleted or changed files? |
| 371 | `wizard_intro_options_photos` | Wann wird synchronisiert, und was passiert danach mit den Fotos auf dem Handy? | When does it sync, and what happens to the photos on the phone afterwards? |
| 372 | `wizard_intro_filter` | Optional: Nur bestimmte Dateien synchronisieren oder einzelne ausschliessen. Ohne Angaben wird alles synchronisiert. | Optional: sync only certain files or exclude some. Without entries everything is synced. |
| 373 | `wizard_intro_guard` | Optional: Ab wann LeoSync bei vielen Löschungen zuerst nachfragt. Der Standard passt für die meisten. | Optional: from when LeoSync asks first when there are many deletions. The default suits most people. |
| 374 | `profile_pc_check` | PC-Kennung | PC ID |
| 375 | `profile_known_nets` | Bekannte Netze: %1$d | Known networks: %1$d |
| 376 | `profile_known_nets_forget` | Vergessen | Forget |
| 377 | `profile_known_nets_hint` | LeoSync merkt sich die WLANs, in denen dein PC erkannt wurde, und versucht dort ausdauernder, ihn zu erreichen. Nach „Vergessen“ lernt LeoSync sie neu. Das Netz entscheidet nie darüber, ob Anmeldedaten gesendet werden; das tut allein die PC-Kennung. | LeoSync remembers the Wi-Fi networks where your PC was recognised and tries harder to reach it there. After “Forget” it learns them again. The network never decides whether sign-in data is sent; only the PC ID does. |
| 378 | `profile_pc_check_hint` | LeoSync erkennt deinen PC an seiner Kennung, bevor es die Anmeldedaten sendet. Das funktioniert in jedem WLAN und über VPN. | LeoSync recognises your PC by its ID before it sends the sign-in data. This works in any Wi-Fi and over VPN. |
| 379 | `profile_pc_status_known` | PC erkannt | PC recognised |
| 380 | `profile_pc_status_unknown` | PC noch nicht erkannt. Einmal „Verbindung testen“ tippen. | PC not recognised yet. Tap “Test connection” once. |
| 381 | `profile_pc_status_new` | Der PC wird beim Speichern erkannt, sobald er erreichbar ist. | The PC is recognised when you save, as soon as it is reachable. |
| 382 | `profile_pc_learning` | PC wird erkannt … | Recognising the PC … |
| 383 | `profile_pc_status_changed` | Ein anderer PC hat geantwortet. Mit „Verbindung testen“ prüfen. | A different PC answered. Tap “Test connection” to check it. |
| 384 | `profile_pc_switch_hint` | Die PC-Kennung wird jetzt geprüft: Die Verbindung wird einmal getestet. | The PC ID is checked now: the connection is tested once. |
| 385 | `profile_pc_trust_title` | Anderem PC vertrauen? | Trust a different PC? |
| 386 | `profile_pc_trust_body` | Unter %1$s antwortet ein anderer PC als bisher. Nur fortfahren, wenn du den PC neu installiert oder ersetzt hast. Deine Anmeldedaten werden nur gesendet, wenn du dem neuen PC vertraust. | At %1$s a different PC answers than before. Only continue if you reinstalled the PC or replaced it. Your sign-in data is only sent if you trust the new PC. |
| 387 | `profile_pc_trust_yes` | Neuem PC vertrauen | Trust new PC |
| 388 | `profile_pc_trust_no` | Nicht vertrauen | Do not trust |
| 389 | `profile_host_change_title` | Anderen PC verwenden? | Use a different PC? |
| 390 | `profile_host_change_body` | Du hast den Server von %1$s auf %2$s geändert. Dort ist noch kein PC erkannt. Deine Anmeldedaten werden an diesen PC gesendet, sobald du fortfährst. Nur fortfahren, wenn %2$s dein eigener PC ist. | You changed the server from %1$s to %2$s. No PC is recognised there yet. Your sign-in data will be sent to that PC as soon as you continue. Only continue if %2$s is your own PC. |
| 391 | `profile_host_change_yes` | Anmeldedaten senden | Send sign-in data |
| 392 | `home_profile_pc_known` | PC erkannt | PC recognised |
| 393 | `home_profile_pc_unknown` | PC noch nicht erkannt | PC not recognised yet |
| 394 | `profile_number_range` | Bitte eine Zahl von %1$d bis %2$d eingeben | Please enter a number from %1$d to %2$d |
| 395 | `profile_number_step_blocked` | Bitte zuerst das Zahlenfeld mit Fehler in diesem Schritt korrigieren. | Please correct the number field with an error in this step first. |
| 396 | `profile_number_blocked` | Nicht gespeichert: Bitte zuerst die Zahlenfelder mit Fehler korrigieren. | Not saved: please correct the number fields with errors first. |
| 397 | `profile_test_need_path` | Bitte zuerst den Freigabe-Pfad eingeben, z. B. %1$s. | Please enter the share path first, e.g. %1$s. |
| 398 | `profile_test_bad_input` | Die Eingabe ist ungültig. Bitte Freigabe-Pfad und Benutzer prüfen. | The input is invalid. Please check the share path and user. |
| 399 | `profile_confirm_title` | Änderung bestätigen | Confirm change |
| 400 | `profile_confirm_dir_up` | Die Richtung wird von „%1$s“ auf „%2$s“ geändert. Ab der nächsten Synchronisierung gilt das Handy als Quelle: abweichende Dateien auf dem PC werden durch die Handy-Version ersetzt. Die ersetzte Version kann in den Papierkorb „%3$s“ kommen. | The direction changes from “%1$s” to “%2$s”. From the next sync, the phone is the source: differing files on the PC are replaced by the phone version. The replaced version may go to the trash “%3$s”. |
| 401 | `profile_confirm_dir_down` | Die Richtung wird von „%1$s“ auf „%2$s“ geändert. Ab der nächsten Synchronisierung gilt der PC als Quelle: abweichende Dateien auf dem Handy werden durch die PC-Version ersetzt. Die ersetzte Version kann in den Papierkorb „%3$s“ kommen. | The direction changes from “%1$s” to “%2$s”. From the next sync, the PC is the source: differing files on the phone are replaced by the PC version. The replaced version may go to the trash “%3$s”. |
| 402 | `profile_confirm_dir_both` | Die Richtung wird von „%1$s“ auf „%2$s“ geändert. Ab der nächsten Synchronisierung werden Änderungen in beide Richtungen übertragen. | The direction changes from “%1$s” to “%2$s”. From the next sync, changes are transferred in both directions. |
| 403 | `profile_confirm_propagate` | Gelöschte Dateien werden ab jetzt auf der anderen Seite ebenfalls entfernt (in den Papierkorb „%1$s“). | Files deleted on one side will now also be removed on the other side (to the trash “%1$s”). |
| 404 | `profile_folder_unusable_root` | Der gewählte Ordner ist der oberste Ordner des Speichers und kann nicht verwendet werden. Bitte einen Unterordner wählen. | The selected folder is the top level of the storage and cannot be used. Please choose a subfolder. |
| 405 | `profile_folder_unusable_provider` | Dieser Ordner wird von Android über einen Umweg (z. B. „Downloads“) geliefert und kann für Fotos nicht verwendet werden. Bitte den Ordner direkt im internen Speicher wählen. | Android provides this folder via a detour (e.g. “Downloads”), so it cannot be used for photos. Please choose the folder directly in internal storage. |
| 406 | `profile_filter_include_hint` | Alles andere wird nicht synchronisiert. | Everything else is not synchronized. |
| 407 | `profile_filter_bad` | Ungültiges Muster „%1$s“: Eine Klammer fehlt oder ist zu viel. | Invalid pattern “%1$s”: a bracket is missing or extra. |
| 408 | `profile_filter_summary_include` | Nur: %1$s | Only: %1$s |
| 409 | `profile_filter_summary_exclude` | Ausgeschlossen: %1$d Muster | Excluded: %1$d patterns |
| 410 | `profile_folder_warning_options` | Trotzdem übernehmen: der bisherige Abgleichstand bleibt. / Als neuen Ordner behandeln: der Stand wird zurückgesetzt, alle Dateien werden neu abgeglichen. | Accept anyway: the previous sync status is kept. / Treat as new folder: the state is reset and all files are compared again. |
| 411 | `overlap_nested_remote` | Der Zielordner auf dem PC liegt innerhalb eines Ordners von Profil „%1$s“ (oder umgekehrt). Dieselben Dateien würden von beiden Profilen synchronisiert – das kann zu doppelten Kopien, Konflikten oder ungewollten Löschungen führen. | The target folder on the PC lies inside a folder of profile “%1$s” (or vice versa). The same files would be synchronized by both profiles – this can lead to duplicate copies, conflicts or unwanted deletions. |
| 412 | `overlap_cross_pc_from_photos` | Der Zielordner dieser Fotosicherung liegt im PC-Ordner von Profil „%1$s“ (oder umgekehrt). Das Ordner-Profil behandelt die gesicherten Fotos wie eigene Dateien: Sie können wieder aufs Handy geladen oder bei übernommenen Löschungen in den Papierkorb gelegt werden. | The target folder of this photo backup lies inside the PC folder of profile “%1$s” (or vice versa). The folder profile treats the backed-up photos like its own files: they can be downloaded to the phone again or moved to the recycle bin when deletions are applied. |
| 413 | `overlap_cross_phone_from_photos` | Ein Quellordner dieser Fotosicherung liegt im Handy-Ordner von Profil „%1$s“, das Dateien aufs Handy lädt (oder umgekehrt). Dieses Profil kann dort Dateien ersetzen oder neue Dateien ablegen, die dann als Fotos gesichert würden. | A source folder of this photo backup lies inside the phone folder of profile “%1$s”, which loads files onto the phone (or vice versa). That profile can replace files there or add new files that would then be backed up as photos. |
| 414 | `overlap_cross_phone_from_mirror` | Der Handy-Ordner dieses Profils, das Dateien aufs Handy lädt, liegt in einem Quellordner der Fotosicherung „%1$s“ (oder umgekehrt). Dieses Profil kann dort Dateien ersetzen oder neue Dateien ablegen, die dann als Fotos gesichert würden. | The phone folder of this profile, which loads files onto the phone, lies inside a source folder of photo backup “%1$s” (or vice versa). This profile can replace files there or add new files that would then be backed up as photos. |
| 415 | `overlap_cross_pc_from_mirror` | Der PC-Ordner dieses Profils liegt im Zielordner der Fotosicherung „%1$s“ (oder umgekehrt). Dieses Profil behandelt die gesicherten Fotos wie eigene Dateien: Sie können aufs Handy geladen oder bei übernommenen Löschungen in den Papierkorb gelegt werden. | The PC folder of this profile lies inside the target folder of photo backup “%1$s” (or vice versa). This profile treats the backed-up photos like its own files: they can be downloaded to the phone or moved to the recycle bin when deletions are applied. |
| 416 | `overlap_nested_local` | Der Ordner auf dem Handy liegt innerhalb eines Ordners von Profil „%1$s“ (oder umgekehrt). Dieselben Dateien würden von beiden Profilen synchronisiert – das kann zu doppelten Kopien, Konflikten oder ungewollten Löschungen führen. | The folder on the phone lies inside a folder of profile “%1$s” (or vice versa). The same files would be synchronized by both profiles – this can lead to duplicate copies, conflicts or unwanted deletions. |
| 417 | `home_profile_filter_active` | Filter aktiv | Filter active |
| 418 | `enc_title` | Verschlüsselung | Encryption |
| 419 | `enc_title_value` | %1$s · %2$s | %1$s · %2$s |
| 420 | `enc_intro` | Schützt die Übertragung zum PC, damit niemand im Netz mitlesen kann. | Protects the transfer to the PC so nobody on the network can read along. |
| 421 | `enc_value_off` | Aus | Off |
| 422 | `enc_value_preferred` | Wenn möglich | If possible |
| 423 | `enc_value_required` | Erforderlich | Required |
| 424 | `enc_off` | Aus | Off |
| 425 | `enc_off_sub` | Keine Verschlüsselung. Nur in einem Netz empfohlen, dem du vertraust. | No encryption. Only recommended on a network you trust. |
| 426 | `enc_preferred` | Wenn möglich (empfohlen) | If possible (recommended) |
| 427 | `enc_preferred_sub` | Verschlüsselt, sobald der PC es kann. Sonst läuft die Verbindung unverschlüsselt weiter. | Encrypted as soon as the PC supports it. Otherwise the connection stays unencrypted. |
| 428 | `enc_required` | Erforderlich | Required |
| 429 | `enc_required_sub` | Verbindet nur verschlüsselt. Kann der PC das nicht, kommt keine Verbindung zustande und es wird kein Passwort gesendet. Am sichersten. | Connects only if encrypted. If the PC cannot do that, no connection is made and no password is sent. Most secure. |
| 430 | `enc_test_ok` | ✓ Verschlüsselt (SMB %1$s) | ✓ Encrypted (SMB %1$s) |
| 431 | `enc_test_warn` | ⚠ Nicht verschlüsselt (%1$s). Dein PC oder NAS kann nicht verschlüsseln oder es ist ausgeschaltet. | ⚠ Not encrypted (%1$s). Your PC or NAS cannot encrypt, or it is switched off. |
| 432 | `enc_test_off` | Nicht verschlüsselt, wie eingestellt. | Not encrypted, as set. |
| 433 | `enc_help` | Hilfe: Verschlüsselung einschalten | Help: turn on encryption |
| 434 | `enc_test_required_fail` | ✗ Verbindung nicht möglich: Dein PC oder NAS kann keine verschlüsselte Verbindung herstellen. Es wurden keine Anmeldedaten gesendet. Schalte die Verschlüsselung am PC oder NAS ein (siehe Hilfe) oder wähle «Wenn möglich». | ✗ Connection not possible: Your PC or NAS cannot make an encrypted connection. No sign-in data was sent. Turn on encryption on the PC or NAS (see help) or choose «If possible». |
| 435 | `icon_encrypted` | Verschlüsselt | Encrypted |

## Profil-Optionen, doppelte Profile, Typwahl

| Nr. | Schlüssel | Deutsch | Englisch |
|---:|---|---|---|
| 436 | `delete_mode_keep` | Dateien behalten | Keep files |
| 437 | `delete_mode_trash` | In den Papierkorb verschieben | Move to trash |
| 438 | `delete_mode_delete` | Endgültig löschen | Delete permanently |
| 439 | `profile_type_label_photos` | Fotos & Videos sichern | Back up photos & videos |
| 440 | `profile_type_label_mirror` | Ordner synchronisieren | Sync a folder |
| 441 | `direction_upload` | Handy → PC | Phone → PC |
| 442 | `direction_upload_short` | → PC | → PC |
| 443 | `direction_download` | PC → Handy | PC → Phone |
| 444 | `direction_download_short` | → Handy | → Phone |
| 445 | `direction_both` | Beidseitig | Both ways |
| 446 | `direction_both_short` | ⇄ | ⇄ |
| 447 | `conflict_mode_ask` | Jedes Mal fragen | Ask every time |
| 448 | `conflict_mode_newer` | Immer die neuere Datei übernehmen | Always take the newer file |
| 449 | `wizard_step_name` | Profilname | Profile name |
| 450 | `wizard_step_source` | Quelle | Source |
| 451 | `wizard_step_target` | Ziel | Target |
| 452 | `wizard_step_options` | Synchronisierungsoptionen | Sync options |
| 453 | `wizard_step_filter` | Filter | Filter |
| 454 | `wizard_step_guard` | Schutzbremse | Safety brake |
| 455 | `wizard_missing_name` | Bitte gib dem Profil einen Namen. | Please give the profile a name. |
| 456 | `profile_name_taken` | Dieser Name wird schon von einem anderen Profil verwendet. Wähle einen anderen Namen. | This name is already used by another profile. Choose a different name. |
| 457 | `profile_name_taken_existing` | Ein anderes Profil hat denselben Namen. Benenne dieses Profil bitte um. | Another profile has the same name. Please rename this profile. |
| 458 | `wizard_missing_local` | Bitte wähle den Ordner auf dem Handy. | Please choose the folder on the phone. |
| 459 | `wizard_missing_sources` | Bitte wähle mindestens einen Ordner auf dem Handy. | Please choose at least one folder on the phone. |
| 460 | `wizard_missing_media` | Bitte schalte Fotos, Videos oder beides ein. | Please turn on photos, videos or both. |
| 461 | `wizard_missing_target` | Bitte gib den Freigabe-Pfad ein, z. B. \\192.168.1.20\Fotos. | Please enter the Share path, e.g. \\192.168.1.20\Photos. |
| 462 | `overlap_identical` | Profil „%1$s“ hat bereits dasselbe Ziel und dieselben Quellen. | Profile “%1$s” already has the same target and the same sources. |
| 463 | `overlap_nested` | Ein Ordner dieses Profils liegt innerhalb eines Ordners von Profil „%1$s“ (oder umgekehrt). Dieselben Dateien würden von beiden Profilen synchronisiert – das kann zu doppelten Kopien, Konflikten oder ungewollten Löschungen führen. | A folder of this profile lies inside a folder of profile “%1$s” (or the other way round). The same files would be synced by both profiles – this can lead to duplicate copies, conflicts or unwanted deletions. |
| 464 | `overlap_shared_folders` | Gleiches Ziel wie Profil „%1$s“ und gemeinsame Quellordner: %2$s. Diese Dateien würden von beiden Profilen geprüft und übertragen. | Same target as profile “%1$s” and shared source folders: %2$s. These files would be checked and transferred by both profiles. |
| 465 | `overlap_blocked_layout` | Profil „%1$s“ verwendet dieses Ziel bereits mit einer anderen Ordnerstruktur (%2$s). Alle Fotoprofile mit demselben Ziel müssen dieselbe Struktur verwenden, deshalb kann dieses Profil nicht gespeichert werden. Wähle dieselbe Struktur oder ein anderes Ziel. | Profile “%1$s” already uses this target with a different folder structure (%2$s). All photo profiles of the same target must use the same structure, so this profile cannot be saved. Choose the same structure or another target. |
| 466 | `folder_layout_month_suffix` | Jahr\MM[ - Land] | Year\MM[ - Country] |
| 467 | `folder_layout_country_folder` | Jahr\[Land\]MM | Year\[Country\]MM |
| 468 | `folder_layout_country_subfolder` | Jahr\MM\[Land] | Year\MM\[Country] |
| 469 | `overlap_same_target` | Gleiches Ziel wie Profil „%1$s“. Diese Dateien würden von beiden Profilen geprüft und übertragen. | Same target as profile “%1$s”. These files would be checked and transferred by both profiles. |
| 470 | `type_chooser_title` | Was möchtest du synchronisieren? | What do you want to sync? |
| 471 | `type_chooser_photos` | Fotos und Videos vom Handy auf den PC. Ablage sortiert nach Jahr, Monat und Reiseland, z. B. 2026\06 - Ägypten. Optional danach vom Handy entfernen. | Photos and videos from the phone to the PC. Stored by year, month and travel country, e.g. 2026\06 - Egypt. Optionally remove them from the phone afterwards. |
| 472 | `type_chooser_mirror` | Einen beliebigen Ordner 1:1 spiegeln, mit Unterordnern und allen Dateitypen. Richtung wählbar: Handy → PC, PC → Handy oder beidseitig. | Mirror any folder 1:1, with subfolders and all file types. Direction selectable: phone → PC, PC → phone or both ways. |
| 473 | `remote_picker_title` | Ordner auf der Freigabe | Folder on the Share |
| 474 | `remote_picker_title_shares` | Freigaben auf dem PC | Shares on the PC |
| 475 | `remote_picker_no_shares` | Keine Freigaben gefunden. Ist ein Ordner auf dem PC freigegeben (Eigenschaften → Freigabe)? | No Shares found. Is a folder shared on the PC (Properties → Sharing)? |
| 476 | `remote_picker_empty` | Keine Unterordner | No subfolders |
| 477 | `remote_picker_use` | Diesen Ordner verwenden | Use this folder |
| 478 | `remote_picker_new_folder` | Neuer Ordner | New folder |
| 479 | `remote_picker_new_folder_name` | Ordnername | Folder name |
| 480 | `remote_picker_create` | Anlegen | Create |
| 481 | `remote_picker_name_invalid` | Ungültiger Name. Kein \ / : * ? " < > \|, kein Punkt oder Leerzeichen am Ende. | Invalid name. No \ / : * ? " < > \|, no dot or space at the end. |
| 482 | `remote_picker_name_exists` | Ein Ordner «%1$s» existiert bereits. | A folder named "%1$s" already exists. |
| 483 | `remote_picker_create_failed` | Der Ordner konnte nicht angelegt werden. | The folder could not be created. |
| 484 | `icon_folder` | Ordner | Folder |
| 485 | `icon_camera` | Kamera | Camera |
| 486 | `icon_phone` | Handy | Phone |
| 487 | `icon_pc` | PC | PC |
| 488 | `icon_server` | Server | Server |
| 489 | `icon_add` | Hinzufügen | Add |
| 490 | `icon_up` | Hoch | Up |
| 491 | `icon_down` | Runter | Down |
| 492 | `icon_pause` | Pause | Pause |
| 493 | `icon_play` | Fortsetzen | Play |
| 494 | `icon_ok` | Erledigt | Done |
| 495 | `icon_error` | Fehler | Error |
| 496 | `icon_warn` | Warnung | Warning |
| 497 | `icon_sync` | Synchronisieren | Sync |
| 498 | `icon_bin` | Papierkorb | Trash |
| 499 | `icon_both` | Beidseitig | Both directions |

## Speicher freigeben

| Nr. | Schlüssel | Deutsch | Englisch |
|---:|---|---|---|
| 500 | `free_hint` | Speicher freigeben gibt es nur bei Fotos-Profilen und bei Ordner-Profilen Handy → PC ohne „Löschungen übernehmen“. | Free up space is only available for photo profiles and for folder profiles phone → PC without “Apply deletions”. |
| 501 | `free_no_profile` | Kein passendes Profil. | No suitable profile. |
| 502 | `free_choose_profile` | Für welches Profil möchtest du Speicher freigeben? | For which profile do you want to free up space? |
| 503 | `free_kind_photos` | Fotos & Videos | Photos & videos |
| 504 | `free_kind_folder` | Ordner Handy → PC | Folder phone → PC |
| 505 | `free_read_error` | Fehler beim Einlesen | Error while reading |
| 506 | `free_moved_to_gallery_trash (one)` | %1$s Datei in den Papierkorb der Galerie verschoben. | %1$s file moved to the gallery trash. |
| 507 | `free_moved_to_gallery_trash (other)` | %1$s Dateien in den Papierkorb der Galerie verschoben. | %1$s files moved to the gallery trash. |
| 508 | `free_reading` | Lese Dateien… | Reading files… |
| 509 | `free_checking_pc` | Prüfe auf dem PC… | Checking on the PC… |
| 510 | `free_confirmed (one)` | %1$s Datei auf dem PC bestätigt. | %1$s file confirmed on the PC. |
| 511 | `free_confirmed (other)` | %1$s Dateien auf dem PC bestätigt. | %1$s files confirmed on the PC. |
| 512 | `free_missing (one)` | %1$s wurde auf dem PC nicht gefunden oder ist anders – sie bleibt. | %1$s was not found on the PC or is different – it stays. |
| 513 | `free_missing (other)` | %1$s wurden auf dem PC nicht gefunden oder sind anders – sie bleiben. | %1$s were not found on the PC or are different – they stay. |
| 514 | `free_changed (one)` | %1$s Datei wurde inzwischen auf dem Handy geändert – sie bleibt. | %1$s file was changed on the phone in the meantime – it stays. |
| 515 | `free_changed (other)` | %1$s Dateien wurden inzwischen auf dem Handy geändert – sie bleiben. | %1$s files were changed on the phone in the meantime – they stay. |
| 516 | `free_unreachable_hosts` | PC nicht erreichbar: %1$s. | PC not reachable: %1$s. |
| 517 | `free_other_pc_hosts` | Es antwortet ein anderer oder noch nicht erkannter PC: %1$s. Es wurden keine Anmeldedaten gesendet. | A different or not yet recognised PC answers: %1$s. No sign-in data was sent. |
| 518 | `free_check_failed` | Prüfung fehlgeschlagen: %1$s | Check failed: %1$s |
| 519 | `free_deleted (one)` | %1$s Datei gelöscht, %2$s frei. | %1$s file deleted, %2$s freed. |
| 520 | `free_deleted (other)` | %1$s Dateien gelöscht, %2$s frei. | %1$s files deleted, %2$s freed. |
| 521 | `free_folders_removed (one)` | %1$s leerer Ordner entfernt. | %1$s empty folder removed. |
| 522 | `free_folders_removed (other)` | %1$s leere Ordner entfernt. | %1$s empty folders removed. |
| 523 | `free_kept (one)` | %1$s Datei bleibt (auf dem PC nicht gefunden, anders oder inzwischen geändert). | %1$s file stays (not found on the PC, different or changed in the meantime). |
| 524 | `free_kept (other)` | %1$s Dateien bleiben (auf dem PC nicht gefunden, anders oder inzwischen geändert). | %1$s files stay (not found on the PC, different or changed in the meantime). |
| 525 | `free_pc_lost` | Der PC war nicht (mehr) erreichbar. | The PC was not (or no longer) reachable. |
| 526 | `free_intro_photos` | Verschiebt Fotos und Videos, die nachweislich auf dem PC gesichert sind, in den Papierkorb der Galerie. Vorher prüft LeoSync jede Datei auf dem PC. Was dort fehlt oder anders ist, bleibt. | Moves photos and videos that are verifiably backed up on the PC to the gallery trash. Before that LeoSync checks every file on the PC. Whatever is missing there or different stays. |
| 527 | `free_verify_off_hint` | Die Prüfung nach dem Hochladen steht bei diesem Profil auf „Aus“. Ungeprüfte Fotos werden hier nicht angeboten. Stelle im Profil mindestens „Wenig“ ein; ab der nächsten Synchronisierung erscheinen neu gesicherte Fotos. | The check after upload is set to “Off” for this profile. Unchecked photos are not offered here. Set at least “Low” in the profile; newly backed-up photos appear from the next sync on. |
| 528 | `free_intro_folder` | Löscht Dateien auf dem Handy, die nachweislich unverändert auf dem PC liegen. Auf dem PC bleiben sie erhalten. Ordner-Dateien kommen nicht in einen Papierkorb, sie werden nach der Prüfung direkt gelöscht. | Deletes files on the phone that are verifiably unchanged on the PC. They are kept on the PC. Folder files do not go to a trash, they are deleted directly after the check. |
| 529 | `free_selection` | Auswahl | Selection |
| 530 | `free_profile` | Profil | Profile |
| 531 | `free_older_taken` | Älter als (Aufnahmedatum) | Older than (date taken) |
| 532 | `free_older_modified` | Älter als (Änderungsdatum) | Older than (date modified) |
| 533 | `free_any` | egal | any |
| 534 | `free_folders` | Ordner | Folders |
| 535 | `free_all` | Alle | All |
| 536 | `free_n_folders (one)` | %1$s Ordner | %1$s folder |
| 537 | `free_n_folders (other)` | %1$s Ordner | %1$s folders |
| 538 | `free_less` | Weitere Kriterien ausblenden | Hide more criteria |
| 539 | `free_more` | Weitere Kriterien… | More criteria… |
| 540 | `free_types` | Dateitypen | File types |
| 541 | `free_min_size` | Grösse ab | Minimum size |
| 542 | `free_no_minimum` | Kein Minimum | No minimum |
| 543 | `free_exclude` | Ausnehmen | Exclude |
| 544 | `free_nothing` | Nichts | Nothing |
| 545 | `free_n_patterns (one)` | %1$s Muster | %1$s pattern |
| 546 | `free_n_patterns (other)` | %1$s Muster | %1$s patterns |
| 547 | `free_keep_favorites` | Favoriten behalten | Keep favorites |
| 548 | `free_keep_favorites_sub` | In der Galerie als Favorit markierte Fotos bleiben auf dem Handy. | Photos marked as favorite in the gallery stay on the phone. |
| 549 | `free_favorites_android11` | Favoriten kann Android erst ab Version 11 melden. | Android can only report favorites from version 11. |
| 550 | `free_matching (one)` | Passend: %1$s Datei · %2$s | Matching: %1$s file · %2$s |
| 551 | `free_matching (other)` | Passend: %1$s Dateien · %2$s | Matching: %1$s files · %2$s |
| 552 | `free_deselected` | %1$s in der Vorschau abgewählt. | %1$s deselected in the preview. |
| 553 | `free_not_backed_up (one)` | %1$s passende Datei ist (noch) nicht gesichert und bleibt. | %1$s matching file is not backed up (yet) and stays. |
| 554 | `free_not_backed_up (other)` | %1$s passende Dateien sind (noch) nicht gesichert und bleiben. | %1$s matching files are not backed up (yet) and stay. |
| 555 | `free_favorites_kept (one)` | %1$s Favorit bleibt. | %1$s favorite stays. |
| 556 | `free_favorites_kept (other)` | %1$s Favoriten bleiben. | %1$s favorites stay. |
| 557 | `free_preview_hide` | Vorschau ausblenden | Hide preview |
| 558 | `free_preview` | Vorschau | Preview |
| 559 | `free_run_trash` | Auf dem PC prüfen und in den Papierkorb | Check on the PC and move to trash |
| 560 | `free_run_delete` | Auf dem PC prüfen und löschen | Check on the PC and delete |
| 561 | `free_gallery_trash_hint` | Im Papierkorb der Galerie bleiben die Dateien 30 Tage wiederherstellbar. | Files in the gallery trash can be restored for 30 days. |
| 562 | `free_result` | Ergebnis | Result |
| 563 | `free_main_folder` | (Hauptordner) | (main folder) |
| 564 | `free_live_photos` | Live-Fotos (Bild + Video) werden nur gemeinsam entfernt. | Live photos (image + video) are only removed together. |
| 565 | `free_confirm_title (one)` | %1$s Datei löschen? | Delete %1$s file? |
| 566 | `free_confirm_title (other)` | %1$s Dateien löschen? | Delete %1$s files? |
| 567 | `free_confirm_text` | LeoSync prüft jede Datei auf dem PC (vorhanden, gleiche Grösse, gleicher Inhalt) und löscht sie danach endgültig vom Handy (%1$s). Auf dem PC bleiben sie erhalten. Was dort fehlt oder anders ist, bleibt auf dem Handy. | LeoSync checks every file on the PC (present, same size, same content) and then deletes it permanently from the phone (%1$s). They are kept on the PC. Whatever is missing there or different stays on the phone. |
| 568 | `free_confirm_button` | Prüfen und löschen | Check and delete |
| 569 | `free_direct_title` | Endgültig löschen? | Delete permanently? |
| 570 | `free_direct_text (one)` | Android 10 hat keinen Papierkorb. %1$s Datei wird endgültig vom Handy gelöscht. Sie ist auf dem PC vorhanden. | Android 10 has no trash. %1$s file will be permanently deleted from the phone. It is present on the PC. |
| 571 | `free_direct_text (other)` | Android 10 hat keinen Papierkorb. %1$s Dateien werden endgültig vom Handy gelöscht. Sie sind auf dem PC vorhanden. | Android 10 has no trash. %1$s files will be permanently deleted from the phone. They are present on the PC. |
| 572 | `free_direct_done (one)` | %1$s Datei gelöscht | %1$s file deleted |
| 573 | `free_direct_done (other)` | %1$s Dateien gelöscht | %1$s files deleted |
| 574 | `free_age_title` | Älter als | Older than |
| 575 | `free_apply` | Übernehmen | Apply |
| 576 | `free_age_any` | Alter egal | Any age |
| 577 | `free_age_older` | Älter als … | Older than … |
| 578 | `free_amount` | Anzahl | Amount |
| 579 | `free_all_folders` | Alle Ordner | All folders |
| 580 | `free_no_subfolders` | Keine Unterordner. | No subfolders. |
| 581 | `free_folder_includes_sub` | Ein Ordner schliesst seine Unterordner ein. | A folder includes its subfolders. |
| 582 | `free_all_types` | Alle Typen | All types |
| 583 | `free_own_extensions` | Eigene Endungen | Own extensions |
| 584 | `free_own_extensions_example` | z. B. iso, bak | e.g. iso, bak |
| 585 | `free_other_hint` | „%1$s“ = alles, was in keine der Gruppen passt. | “%1$s” = everything that fits none of the groups. |
| 586 | `free_custom_value` | Eigener Wert | Custom value |
| 587 | `free_exclude_hint` | Ein Muster pro Zeile. Passende Dateien bleiben immer auf dem Handy. | One pattern per line. Matching files always stay on the phone. |
| 588 | `free_exclude_examples` | Beispiele: *.kdbx (alle Dateien mit dieser Endung), Wichtig/** (alles im Ordner „Wichtig“), Thumbs.db | Examples: *.kdbx (all files with this extension), Important/** (everything in the folder “Important”), Thumbs.db |
| 589 | `free_patterns` | Muster | Patterns |
| 590 | `kind_images` | Bilder | Images |
| 591 | `kind_videos` | Videos | Videos |
| 592 | `kind_audio` | Audio | Audio |
| 593 | `kind_documents` | Dokumente | Documents |
| 594 | `kind_archives` | Archive | Archives |
| 595 | `kind_other` | Andere | Other |
| 596 | `unit_days` | Tage | Days |
| 597 | `unit_weeks` | Wochen | Weeks |
| 598 | `unit_months` | Monate | Months |
| 599 | `unit_years` | Jahre | Years |
| 600 | `age_days (one)` | %1$s Tag | %1$s day |
| 601 | `age_days (other)` | %1$s Tage | %1$s days |
| 602 | `age_weeks (one)` | %1$s Woche | %1$s week |
| 603 | `age_weeks (other)` | %1$s Wochen | %1$s weeks |
| 604 | `age_months (one)` | %1$s Monat | %1$s month |
| 605 | `age_months (other)` | %1$s Monate | %1$s months |
| 606 | `age_years (one)` | %1$s Jahr | %1$s year |
| 607 | `age_years (other)` | %1$s Jahre | %1$s years |
| 608 | `free_enter_one_or_more` | Bitte 1 oder mehr eingeben. | Enter 1 or more. |
| 609 | `free_none_match` | Keine Datei erfüllt die Auswahl oder ist bereits auf dem PC gesichert. Auswahl lockern oder zuerst synchronisieren. | No file matches, or none is backed up on the PC yet. Relax the selection or sync first. |
| 610 | `free_no_trash_android10` | Android 10 hat keinen Papierkorb: Dateien werden endgültig vom Handy gelöscht. | Android 10 has no trash: files are permanently deleted from the phone. |
| 611 | `free_deleted_from_mobile (one)` | %1$s Datei auf dem PC bestätigt und vom Handy gelöscht (%2$s frei). | %1$s file confirmed on the PC and deleted from the phone (%2$s freed). |
| 612 | `free_deleted_from_mobile (other)` | %1$s Dateien auf dem PC bestätigt und vom Handy gelöscht (%2$s frei). | %1$s files confirmed on the PC and deleted from the phone (%2$s freed). |
| 613 | `free_partial_result` | %1$d gelöscht, %2$d übersprungen. | %1$d deleted, %2$d skipped. |
| 614 | `free_skipped_note (one)` | %1$s Datei übersprungen (nicht auf dem PC bestätigt) und bleibt auf dem Handy. | %1$s file skipped (not confirmed on the PC) and stays on the phone. |
| 615 | `free_skipped_note (other)` | %1$s Dateien übersprungen (nicht auf dem PC bestätigt) und bleiben auf dem Handy. | %1$s files skipped (not confirmed on the PC) and stay on the phone. |

## Papierkorb

| Nr. | Schlüssel | Deutsch | Englisch |
|---:|---|---|---|
| 616 | `side_phone` | Handy | Phone |
| 617 | `side_pc` | PC | PC |
| 618 | `trash_switch_profile` | Profil wechseln | Switch profile |
| 619 | `trash_choose_profile` | Welchen Papierkorb möchtest du öffnen? | Which trash do you want to open? |
| 620 | `trash_photos_hint` | Fotos-Profile haben keinen eigenen Papierkorb: Entfernte Fotos liegen im Papierkorb der Galerie und werden in der Galerie-App geleert. | Photo profiles have no trash of their own: removed photos are in the gallery trash and are emptied in the gallery app. |
| 621 | `intervention_waiting` | Wird nach der laufenden Synchronisierung ausgeführt. | Will run after the current sync. |
| 622 | `intervention_waiting_hint` | Sie können diese Ansicht verlassen. Beendet das System LeoSync, bevor die Aktion an der Reihe ist, wird nichts gelöscht. | You can leave this view. If the system ends LeoSync before it is the turn of this action, nothing is deleted. |
| 623 | `intervention_running` | Wird ausgeführt… | Running… |
| 624 | `intervention_running_hint` | Sie können diese Ansicht verlassen. Das Ergebnis erscheint hier oder als Hinweis. | You can leave this view. The result is shown here or as a notice. |
| 625 | `intervention_already_pending` | Für dieses Profil wartet oder läuft bereits eine Aktion. | Another action for this profile is already waiting or running. |
| 626 | `intervention_failed` | Fehlgeschlagen: %1$s | Failed: %1$s |
| 627 | `trash_nothing_deleted` | Nichts gelöscht: Die Einträge sind nicht mehr vorhanden, haben sich geändert oder sind zum Wiederherstellen vorgemerkt. | Nothing deleted: the entries are gone, have changed or are reserved for restoring. |
| 628 | `trash_reading` | Lese den Papierkorb auf Handy und PC… | Reading the trash on phone and PC… |
| 629 | `trash_phone_summary (one)` | Handy: %1$s Datei, %2$s | Phone: %1$s file, %2$s |
| 630 | `trash_phone_summary (other)` | Handy: %1$s Dateien, %2$s | Phone: %1$s files, %2$s |
| 631 | `trash_pc_summary (one)` | PC: %1$s Datei, %2$s | PC: %1$s file, %2$s |
| 632 | `trash_pc_summary (other)` | PC: %1$s Dateien, %2$s | PC: %1$s files, %2$s |
| 633 | `trash_pc_unreachable` | PC: nicht erreichbar | PC: not reachable |
| 634 | `trash_pc_unreachable_hint` | Einträge aus dem PC-Papierkorb fehlen in der Liste. Wiederherstellen und Löschen auf dem PC werden vorgemerkt und bei der nächsten Synchronisierung mit erreichbarem PC ausgeführt. | Entries from the PC trash are missing from the list. Restoring and deleting on the PC are scheduled and carried out at the next sync with a reachable PC. |
| 635 | `trash_cleanup_button` | Papierkorb aufräumen… | Clean up trash… |
| 636 | `trash_reload` | Neu einlesen | Reload |
| 637 | `trash_empty` | Der Papierkorb ist leer. | The trash is empty. |
| 638 | `trash_empty_phone_only` | Auf dem Handy ist der Papierkorb leer. | The trash on the phone is empty. |
| 639 | `trash_intro` | Hier liegen gelöschte Dateien und ältere Versionen. Markiere Einträge, um sie wiederherzustellen oder endgültig zu löschen. | Deleted files and older versions are kept here. Mark entries to restore them or delete them permanently. |
| 640 | `trash_day` | Tag | Day |
| 641 | `trash_all_days` | Alle Tage | All days |
| 642 | `trash_select_day` | Alles vom %1$s auswählen | Select everything from %1$s |
| 643 | `trash_whole_folder` | Neueste Version je Datei wählen | Select newest version of each file |
| 644 | `trash_older_version` | ältere Version | older version |
| 645 | `trash_deleted` | gelöscht | deleted |
| 646 | `trash_modified_at` | geändert %1$s | modified %1$s |
| 647 | `trash_deleted_on` | gelöscht am %1$s | deleted on %1$s |
| 648 | `trash_reserved` | zum Wiederherstellen vorgemerkt | scheduled for restore |
| 649 | `trash_selected` | %1$s ausgewählt · %2$s | %1$s selected · %2$s |
| 650 | `trash_clear_selection` | Auswahl aufheben | Clear selection |
| 651 | `trash_restore` | Wiederherstellen | Restore |
| 652 | `trash_purged_pc_pending` | Handy geleert – PC wird bei der nächsten Synchronisierung mit PC geleert | Phone emptied – the PC will be emptied at the next sync with the PC |
| 653 | `trash_purged` | Papierkorb aufgeräumt | Trash cleaned up |
| 654 | `trash_delete_title (one)` | %1$s Datei endgültig löschen? | Delete %1$s file permanently? |
| 655 | `trash_delete_title (other)` | %1$s Dateien endgültig löschen? | Delete %1$s files permanently? |
| 656 | `trash_delete_text` | Die markierten Einträge werden aus dem Papierkorb gelöscht und lassen sich danach nicht mehr wiederherstellen. | The marked entries are deleted from the trash and can no longer be restored afterwards. |
| 657 | `trash_delete_pc_later` | Der PC ist nicht erreichbar: Dort wird bei der nächsten Synchronisierung mit PC gelöscht. | The PC is not reachable: it will be deleted there at the next sync with the PC. |
| 658 | `trash_deleted_pc_pending` | Gelöscht – PC folgt bei der nächsten Synchronisierung mit PC | Deleted – the PC follows at the next sync with the PC |
| 659 | `trash_deleted_done` | Endgültig gelöscht | Permanently deleted |
| 660 | `trash_restore_title (one)` | %1$s Datei wiederherstellen? | Restore %1$s file? |
| 661 | `trash_restore_title (other)` | %1$s Dateien wiederherstellen? | Restore %1$s files? |
| 662 | `trash_restore_text` | Die aktuellen Versionen kommen zuerst in den Papierkorb. Die gewählten Versionen werden auf Handy und PC zurückgeschrieben. | The current versions go to the trash first. The chosen versions are written back on phone and PC. |
| 663 | `trash_restore_now` | Das passiert gleich mit einer Synchronisierung. | This happens right away with a sync. |
| 664 | `trash_restore_later` | Der PC ist nicht erreichbar: Das passiert bei der nächsten Synchronisierung mit PC. | The PC is not reachable: this happens at the next sync with the PC. |
| 665 | `trash_cleanup` | Papierkorb aufräumen | Clean up trash |
| 666 | `trash_cleanup_hint` | Endgültig gelöschte Dateien lassen sich nicht mehr wiederherstellen. | Permanently deleted files can no longer be restored. |
| 667 | `trash_what` | Was? | What? |
| 668 | `trash_mode_older` | Älter als … Tage | Older than … days |
| 669 | `trash_mode_dates` | Einzelne Tage | Single days |
| 670 | `trash_no_days` | Keine Tage im Papierkorb. | No days in the trash. |
| 671 | `trash_none_chosen` | Keine gewählt | None chosen |
| 672 | `trash_days_chosen (one)` | %1$s Tag gewählt | %1$s day chosen |
| 673 | `trash_days_chosen (other)` | %1$s Tage gewählt | %1$s days chosen |
| 674 | `trash_mode_all` | Alles | Everything |
| 675 | `trash_where` | Wo? | Where? |
| 676 | `trash_phone_only_hint` | „%1$s“ schafft Platz auf dem Handy; die Kopien im PC-Papierkorb bleiben erhalten. | “%1$s” frees up space on the phone; the copies in the PC trash are kept. |
| 677 | `trash_choose_what` | Bitte auswählen, was geleert werden soll. | Please choose what should be emptied. |
| 678 | `trash_affects (one)` | Betrifft %1$s Datei (%2$s) | Affects %1$s file (%2$s) |
| 679 | `trash_affects (other)` | Betrifft %1$s Dateien (%2$s) | Affects %1$s files (%2$s) |
| 680 | `trash_affects_pc_later` | – dazu der PC-Papierkorb, sobald der PC erreichbar ist | – plus the PC trash as soon as the PC is reachable |
| 681 | `trash_pc_later_hint` | Der PC ist nicht erreichbar. Das Handy wird sofort geleert, der PC bei der nächsten Synchronisierung mit PC. | The PC is not reachable. The phone is emptied right away, the PC at the next sync with the PC. |
| 682 | `trash_kept_reserved (one)` | %1$s Datei ist zum Wiederherstellen vorgemerkt und bleibt. | %1$s file is scheduled for restore and stays. |
| 683 | `trash_kept_reserved (other)` | %1$s Dateien sind zum Wiederherstellen vorgemerkt und bleiben. | %1$s files are scheduled for restore and stay. |
| 684 | `trash_purge_confirm (one)` | %1$s Datei (%2$s) wird endgültig gelöscht (%3$s) | %1$s file (%2$s) will be permanently deleted (%3$s) |
| 685 | `trash_purge_confirm (other)` | %1$s Dateien (%2$s) werden endgültig gelöscht (%3$s) | %1$s files (%2$s) will be permanently deleted (%3$s) |
| 686 | `trash_purge_confirm_pc` | – auf dem PC bei der nächsten Synchronisierung mit PC | – on the PC at the next sync with the PC |
| 687 | `trash_purge_irreversible` | Das lässt sich nicht rückgängig machen. | This cannot be undone. |
| 688 | `trash_pick_days` | Tage wählen | Choose days |
| 689 | `trash_pick_day` | Tag wählen | Choose day |
| 690 | `trash_none` | Keine | None |
| 691 | `trash_day_files (one)` | %1$s Datei · %2$s | %1$s file · %2$s |
| 692 | `trash_day_files (other)` | %1$s Dateien · %2$s | %1$s files · %2$s |
| 693 | `trash_days_invalid` | Bitte Anzahl Tage eingeben (1 oder mehr). | Enter a number of days (1 or more). |
| 694 | `trash_delete_title_one` | «%1$s» endgültig löschen? | Delete “%1$s” permanently? |
| 695 | `trash_view_label` | Anzeigen | Show |
| 696 | `trash_filter_empty_phone` | Keine Einträge auf dem Handy. | No entries on the phone. |
| 697 | `trash_filter_empty_pc` | Keine Einträge auf dem PC. | No entries on the PC. |
| 698 | `trash_filter_empty_phone_day` | Keine Einträge auf dem Handy an diesem Tag. | No entries on the phone on this day. |
| 699 | `trash_filter_empty_pc_day` | Keine Einträge auf dem PC an diesem Tag. | No entries on the PC on this day. |
| 700 | `trash_filter_pc_unreachable` | PC nicht erreichbar: Einträge aus dem PC-Papierkorb können nicht angezeigt werden. | PC not reachable: PC trash entries cannot be shown. |
| 701 | `trash_filter_reset` | Filter zurücksetzen | Reset filter |
| 702 | `trash_selection_cleared` | Auswahl aufgehoben (Filter geändert). | Selection cleared (filter changed). |
| 703 | `trash_selection_gone` | Auswahl aufgehoben (Einträge nicht mehr vorhanden oder nicht erreichbar). | Selection cleared (entries no longer there or not reachable). |
| 704 | `trash_restore_filter_hint` | Wiederherstellen schreibt immer auf Handy und PC, auch bei Filter «%1$s». | Restoring always writes to phone and PC, even with the filter “%1$s”. |
| 705 | `trash_selected_sides` | %1$s ausgewählt (%2$s) · %3$s | %1$s selected (%2$s) · %3$s |
| 706 | `trash_affects_sides` | Betrifft: %1$s | Affects: %1$s |
| 707 | `trash_restore_writes` | Wird zurückgeschrieben auf: Handy (%1$s) und PC (%1$s). | Will be written back to: phone (%1$s) and PC (%1$s). |
| 708 | `trash_restore_replaces (one)` | Laut Synchronisierungsstand vorhanden: %1$s Datei. Sie wird ersetzt und kommt vorher in den Papierkorb. | Present according to the sync state: %1$s file. It will be replaced and goes to the trash first. |
| 709 | `trash_restore_replaces (other)` | Laut Synchronisierungsstand vorhanden: %1$s Dateien. Sie werden ersetzt und kommen vorher in den Papierkorb. | Present according to the sync state: %1$s files. They will be replaced and go to the trash first. |
| 710 | `trash_restore_missing (one)` | Laut Synchronisierungsstand nicht vorhanden: %1$s Datei. Sie wird neu angelegt. | Not present according to the sync state: %1$s file. It will be created. |
| 711 | `trash_restore_missing (other)` | Laut Synchronisierungsstand nicht vorhanden: %1$s Dateien. Sie werden neu angelegt. | Not present according to the sync state: %1$s files. They will be created. |
| 712 | `trash_restore_names` | Dateien: %1$s | Files: %1$s |
| 713 | `trash_open` | Öffnen | Open |
| 714 | `trash_open_gone` | Die Datei lässt sich nicht mehr öffnen: Sie liegt nicht mehr im Papierkorb oder LeoSync hat den Zugriff auf den Ordner verloren. | The file can no longer be opened: it is no longer in the trash or LeoSync has lost access to the folder. |
| 715 | `trash_open_no_app` | Auf diesem Handy gibt es keine App, die diesen Dateityp öffnen kann. | No app on this phone can open this file type. |

## Ordner-Sync: Seiten

| Nr. | Schlüssel | Deutsch | Englisch |
|---:|---|---|---|
| 716 | `sides_both` | Handy und PC | Phone and PC |
| 717 | `sides_phone_only` | Nur Handy | Phone only |
| 718 | `sides_pc_only` | Nur PC | PC only |
| 719 | `err_local_access_lost` | Kein Zugriff mehr auf den Handy-Ordner – bitte im Profil neu auswählen | No access to the phone folder anymore – please choose it again in the profile |

## Verlauf und Sicherung

| Nr. | Schlüssel | Deutsch | Englisch |
|---:|---|---|---|
| 720 | `log_export_file` | LeoSync-Verlauf_%1$s.txt | LeoSync-History_%1$s.txt |
| 721 | `log_export_failed` | Export fehlgeschlagen: %1$s | Export failed: %1$s |
| 722 | `log_share` | Verlauf teilen | Share history |
| 723 | `log_clear` | Verlauf löschen | Delete history |
| 724 | `log_filter_errors` | Fehler | Errors |
| 725 | `log_filter_transfers` | Übertragungen | Transfers |
| 726 | `log_empty` | Noch keine Einträge. | No entries yet. |
| 727 | `log_empty_both` | Keine Läufe mit Fehlern oder Übertragungen. | No runs with errors or transfers. |
| 728 | `log_empty_errors` | Keine Läufe mit Fehlern. | No runs with errors. |
| 729 | `log_empty_transfers` | Keine Läufe mit Übertragungen. | No runs with transfers. |
| 730 | `log_idle_group` | %1$s× geprüft – nichts zu tun | %1$s× checked – nothing to do |
| 731 | `log_status_partial` | Teilweise | Partial |
| 732 | `log_status_failed` | Fehler | Error |
| 733 | `log_status_paused` | Pausiert | Paused |
| 734 | `log_status_interrupted` | Unterbrochen | Interrupted |
| 735 | `log_status_waiting` | Wartet auf dich | Waiting for you |
| 736 | `log_status_running` | Läuft | Running |
| 737 | `log_trigger` | Auslöser: %1$s | Trigger: %1$s |
| 738 | `backup_exporting` | Exportiere… | Exporting… |
| 739 | `backup_exported (one)` | Gesichert: %1$s Profil | Backed up: %1$s profile |
| 740 | `backup_exported (other)` | Gesichert: %1$s Profile | Backed up: %1$s profiles |
| 741 | `backup_exported_sync (one)` | %1$s Datei Abgleichstand | sync status of %1$s file |
| 742 | `backup_exported_sync (other)` | %1$s Dateien Abgleichstand | sync status of %1$s files |
| 743 | `backup_passwords_encrypted` | Passwörter verschlüsselt | passwords encrypted |
| 744 | `backup_without_passwords` | ohne Passwörter | without passwords |
| 745 | `backup_reading` | Lese Sicherung… | Reading backup… |
| 746 | `backup_unreadable` | Sicherung nicht lesbar | Backup not readable |
| 747 | `backup_file_unreadable` | Datei nicht lesbar: %1$s | File not readable: %1$s |
| 748 | `backup_importing` | Importiere… | Importing… |
| 749 | `backup_imported (one)` | %1$s Profil importiert | %1$s profile imported |
| 750 | `backup_imported (other)` | %1$s Profile importiert | %1$s profiles imported |
| 751 | `backup_imported_sync (one)` | Abgleichstand für %1$s Datei | sync status for %1$s file |
| 752 | `backup_imported_sync (other)` | Abgleichstand für %1$s Dateien | sync status for %1$s files |
| 753 | `backup_skipped_identical` | Übersprungen, weil schon vorhanden (gleiches Ziel und gleiche Quellen): %1$s | Skipped because already present (same target and same sources): %1$s |
| 754 | `backup_overlapping` | Hinweis: Überschneidet sich mit einem bestehenden Profil (gleiches Ziel, gemeinsame Quellordner): %1$s | Note: overlaps with an existing profile (same target, shared source folders): %1$s |
| 755 | `backup_skipped_layout_blocked` | Übersprungen, weil das Ziel schon von einem anderen Fotoprofil mit anderer Ordnerstruktur verwendet wird: %1$s | Skipped because the target is already used by another photo profile with a different folder structure: %1$s |
| 756 | `backup_skipped_layout_unknown` | Nicht importiert, weil die Ordnerstruktur unbekannt ist (Sicherung aus einer neueren Version?): %1$s | Not imported because the folder structure is unknown (backup from a newer version?): %1$s |
| 757 | `backup_without_password` | Passwort neu eingeben bei: %1$s | Enter the password again for: %1$s |
| 758 | `backup_need_folder` | Handy-Ordner neu wählen bei: %1$s (Android überträgt die Ordner-Freigabe nicht). | Choose the phone folder again for: %1$s (Android does not transfer the folder permission). |
| 759 | `backup_import_failed` | Import fehlgeschlagen: %1$s | Import failed: %1$s |
| 760 | `backup_title` | Sicherung | Backup |
| 761 | `backup_hint` | Sichert Profile, Einstellungen, Ordner-Zuordnungen und auf Wunsch den Abgleichstand in eine Datei – z. B. vor einem Handywechsel oder einer Neuinstallation. | Saves profiles, settings, folder assignments and optionally the sync status to a file – e.g. before changing phones or reinstalling. |
| 762 | `backup_export` | Exportieren… | Export… |
| 763 | `backup_import` | Importieren… | Import… |
| 764 | `backup_export_title` | Sicherung exportieren | Export backup |
| 765 | `backup_include_sync` | Abgleichstand mitsichern | Include sync status |
| 766 | `backup_include_sync_sub` | Nach dem Import ist sofort bekannt, welche Dateien schon auf dem PC sind. | After the import it is known right away which files are already on the PC. |
| 767 | `backup_password_optional` | Export-Passwort (optional) | Export password (optional) |
| 768 | `backup_password_repeat` | Export-Passwort wiederholen | Repeat export password |
| 769 | `backup_no_password_hint` | Ohne Export-Passwort enthält die Datei keine SMB-Passwörter; du gibst sie nach dem Import neu ein. | Without an export password the file contains no SMB passwords; you enter them again after the import. |
| 770 | `backup_password_hint` | Die SMB-Passwörter werden mit diesem Passwort verschlüsselt. Ohne es lassen sie sich nicht wiederherstellen. | The SMB passwords are encrypted with this password. Without it they cannot be restored. |
| 771 | `backup_choose_file` | Datei wählen… | Choose file… |
| 772 | `backup_password` | Export-Passwort | Export password |
| 773 | `backup_ask_password_hint` | Die Sicherung enthält verschlüsselte SMB-Passwörter. Ohne Export-Passwort werden die Profile trotzdem importiert, die Passwörter gibst du dann neu ein. | The backup contains encrypted SMB passwords. Without the export password the profiles are imported anyway, you then enter the passwords again. |
| 774 | `backup_without_password_button` | Ohne Passwort | Without password |
| 775 | `backup_import_title` | Sicherung importieren | Import backup |
| 776 | `backup_created` | Erstellt: %1$s (LeoSync %2$s) | Created: %1$s (LeoSync %2$s) |
| 777 | `backup_profiles` | Profile: %1$s | Profiles: %1$s |
| 778 | `backup_sync_status (one)` | Abgleichstand: %1$s Datei | Sync status: %1$s file |
| 779 | `backup_sync_status (other)` | Abgleichstand: %1$s Dateien | Sync status: %1$s files |
| 780 | `backup_pw_taken` | SMB-Passwörter: werden übernommen | SMB passwords: will be taken over |
| 781 | `backup_pw_wrong` | SMB-Passwörter: Export-Passwort fehlt oder ist falsch – Passwörter werden nicht übernommen | SMB passwords: export password missing or wrong – passwords are not taken over |
| 782 | `backup_pw_none` | SMB-Passwörter: nicht enthalten | SMB passwords: not included |
| 783 | `backup_replace_hint` | „Ersetzen“ löscht alle bisherigen Profile samt Abgleichstand und übernimmt auch die Einstellungen (nicht die Sprache der App, sie bleibt unverändert). „Hinzufügen“ legt die Profile zusätzlich an. | “Replace” deletes all existing profiles including sync status and also takes over the settings (not the app language, which stays as it is). “Add” creates the profiles in addition. |
| 784 | `backup_replace` | Ersetzen | Replace |
| 785 | `backup_import_done` | Import abgeschlossen | Import finished |
| 786 | `backup_err_replace_rejected` | Ersetzen abgebrochen, nichts wurde gelöscht: Die Sicherung enthält Profile, die nicht importiert werden können (unbekannte Ordnerstruktur, Sicherung aus einer neueren Version?): %1$s | Replace cancelled, nothing was deleted: the backup contains profiles that cannot be imported (unknown folder structure, backup from a newer version?): %1$s |
| 787 | `log_clear_title` | Gesamten Verlauf löschen? | Delete the whole history? |
| 788 | `log_clear_text` | %1$d Einträge werden gelöscht. Direkt danach kannst du das mit «Rückgängig» zurücknehmen. Mit «Verlauf teilen» kannst du sie vorher sichern. | %1$d entries will be deleted. You can undo this right afterwards with “Undo”. Use “Share history” first to keep a copy. |
| 789 | `log_cleared` | Verlauf gelöscht | History deleted |
| 790 | `log_undo` | Rückgängig | Undo |
| 791 | `log_advice_missing` | Die Datei existiert auf dem Handy nicht mehr. Synchronisiere erneut; bleibt der Fehler, die Dateiliste im Profil prüfen. | The file no longer exists on the phone. Sync again; if the error stays, check the file list in the profile. |
| 792 | `log_advice_connection` | Verbindung zum PC prüfen (WLAN, Freigabe), dann erneut synchronisieren. | Check the connection to the PC (Wi-Fi, share), then sync again. |
| 793 | `log_advice_unknown` | Synchronisiere erneut. Bleibt der Fehler, teile den Verlauf mit dem Entwickler. | Sync again. If the error stays, share the history with the developer. |

## Ordnerauswahl, Konflikte, Simulation, Statistik, Updates

| Nr. | Schlüssel | Deutsch | Englisch |
|---:|---|---|---|
| 794 | `trigger_folder_choice` | Ordnerauswahl | Folder selection |
| 795 | `decision_title` | Ordnerauswahl | Folder selection |
| 796 | `decision_apply_all` | Alle übernehmen | Apply all |
| 797 | `decision_none` | Keine offenen Entscheidungen. | No open decisions. |
| 798 | `decision_hint` | Für diese Monate gibt es auf dem PC bereits umbenannte Ordner. Wähle, wohin die Dateien sollen. Die Wahl gilt für alle Dateien der Gruppe und wird gemerkt. | For these months there are already renamed folders on the PC. Choose where the files should go. The choice applies to all files of the group and is remembered. |
| 799 | `decision_files (one)` | %1$s Datei (%2$s) | %1$s file (%2$s) |
| 800 | `decision_files (other)` | %1$s Dateien (%2$s) | %1$s files (%2$s) |
| 801 | `decision_profile` | Profil: %1$s | Profile: %1$s |
| 802 | `decision_new_folder` | Neuer Ordner: %1$s | New folder: %1$s |
| 803 | `decision_custom_name` | Eigener Name… | Custom name… |
| 804 | `decision_folder_name` | Ordnername (muss %1$s enthalten) | Folder name (must contain %1$s) |
| 805 | `decision_none_hint` | Hier fragt LeoSync, wenn es für einen Monat mehrere umbenannte Ordner auf dem PC gibt. | LeoSync asks here when there are several renamed folders on the PC for one month. |
| 806 | `decision_choose_first` | Wähle einen Ordner, um zu übernehmen. | Choose a folder to apply. |
| 807 | `decision_snack_one (one)` | %1$s Datei wird nach %2$s übertragen | %1$s file will be transferred to %2$s |
| 808 | `decision_snack_one (other)` | %1$s Dateien werden nach %2$s übertragen | %1$s files will be transferred to %2$s |
| 809 | `decision_snack_all (one)` | %1$s Datei wird übertragen (%2$s) | %1$s file will be transferred (%2$s) |
| 810 | `decision_snack_all (other)` | %1$s Dateien werden übertragen (%2$s) | %1$s files will be transferred (%2$s) |
| 811 | `decision_n_groups (one)` | %1$s Gruppe | %1$s group |
| 812 | `decision_n_groups (other)` | %1$s Gruppen | %1$s groups |
| 813 | `decision_n_files (one)` | %1$s Datei | %1$s file |
| 814 | `decision_n_files (other)` | %1$s Dateien | %1$s files |
| 815 | `decision_all_confirm (one)` | %1$s Gruppe mit %2$s wird übernommen. Gruppen ohne Wahl bleiben offen. | %1$s group with %2$s will be applied. Groups without a choice stay open. |
| 816 | `decision_all_confirm (other)` | %1$s Gruppen mit %2$s werden übernommen. Gruppen ohne Wahl bleiben offen. | %1$s groups with %2$s will be applied. Groups without a choice stay open. |
| 817 | `trigger_conflicts` | Konfliktlösung | Conflict resolution |
| 818 | `conflict_title` | Konflikte | Conflicts |
| 819 | `conflict_all_newer` | Alle: neuere | All: newer |
| 820 | `conflict_none` | Keine offenen Konflikte. | No open conflicts. |
| 821 | `conflict_hint` | Diese Dateien wurden auf dem Handy und auf dem PC geändert. Wähle, welche Version gilt. Die andere Version wird in den Ordner „%1$s“ verschoben, nicht gelöscht. Die Auswahl wird bei der nächsten Synchronisierung im WLAN angewendet. | These files were changed on the phone and on the PC. Choose which version wins. The other version is moved to the folder “%1$s”, not deleted. The choice is applied at the next sync on Wi-Fi. |
| 822 | `conflict_newer` | (neuer) | (newer) |
| 823 | `conflict_same_age` | (gleich alt) | (same age) |
| 824 | `conflict_keep_phone` | Handy behalten | Keep phone |
| 825 | `conflict_keep_pc` | PC behalten | Keep PC |
| 826 | `conflict_keep_both` | Beide behalten | Keep both |
| 827 | `conflict_keep_phone_hint` | Handy behalten: Die PC-Version kommt in %1$s. | Keep phone: the PC version goes to %1$s. |
| 828 | `conflict_keep_pc_hint` | PC behalten: Die Handy-Version kommt in %1$s. | Keep PC: the phone version goes to %1$s. |
| 829 | `conflict_keep_both_hint` | Beide behalten: Beide Dateien bleiben; die Handy-Version erhält den Namenszusatz %1$s. | Keep both: both files stay; the phone version gets the name suffix %1$s. |
| 830 | `conflict_snack_phone` | %1$s: Handy-Version behalten (gilt bei der nächsten Synchronisierung im WLAN) | %1$s: phone version kept (applies at the next sync on Wi-Fi) |
| 831 | `conflict_snack_pc` | %1$s: PC-Version behalten (gilt bei der nächsten Synchronisierung im WLAN) | %1$s: PC version kept (applies at the next sync on Wi-Fi) |
| 832 | `conflict_snack_both` | %1$s: beide Versionen behalten (gilt bei der nächsten Synchronisierung im WLAN) | %1$s: both versions kept (applies at the next sync on Wi-Fi) |
| 833 | `conflict_snack_all (one)` | %1$s Konflikt entschieden: neuere Version behalten (gilt bei der nächsten Synchronisierung im WLAN) | %1$s conflict decided: newer version kept (applies at the next sync on Wi-Fi) |
| 834 | `conflict_snack_all (other)` | %1$s Konflikte entschieden: jeweils neuere Version behalten (gilt bei der nächsten Synchronisierung im WLAN) | %1$s conflicts decided: newer version kept (applies at the next sync on Wi-Fi) |
| 835 | `conflict_all_confirm (one)` | %1$s Konflikt: Die neuere Version bleibt, die andere kommt nach %2$s. Fortfahren? | %1$s conflict: the newer version is kept, the other goes to %2$s. Continue? |
| 836 | `conflict_all_confirm (other)` | %1$s Konflikte: Jeweils die neuere Version bleibt, die andere kommt nach %2$s. Fortfahren? | %1$s conflicts: in each case the newer version is kept, the other goes to %2$s. Continue? |
| 837 | `conflict_undo_late` | Nicht mehr rückgängig zu machen: Die Auswahl wurde bereits angewendet. | Can no longer be undone: the choice has already been applied. |
| 838 | `preview_starting` | Starte… | Starting… |
| 839 | `preview_hint` | Zeigt, was beim Synchronisieren passieren würde (Bildordner: Zielordner; Ordner-Synchronisierung: Aktion je Datei). Es wird nichts kopiert, angelegt oder gelöscht. | Shows what would happen when syncing (photo folders: target folder; folder sync: action per file). Nothing is copied, created or deleted. |
| 840 | `preview_only_first` | Nur die ersten | Only the first |
| 841 | `preview_files` | Dateien | files |
| 842 | `preview_all_files` | Alle Dateien | All files |
| 843 | `preview_all_hint` | Bei vielen Fotos dauert „Alle“ länger, weil jedes Foto auf Datum und Ort ausgewertet wird. | With many photos “All” takes longer because every photo is analysed for date and location. |
| 844 | `preview_start` | Simulation starten | Start simulation |
| 845 | `preview_limit_error` | Bitte eine Zahl von 1 bis 100000 eingeben. | Please enter a number from 1 to 100000. |
| 846 | `preview_offline_assumption` | Annahme: Der PC ist nicht bekannt, die Liste geht von einem leeren PC aus. Echte Aktionen können abweichen. | Assumption: the PC is not known, the list assumes an empty PC. Real actions can differ. |
| 847 | `preview_nothing_to_do` | Nichts zu tun – alles aktuell. | Nothing to do – everything is up to date. |
| 848 | `preview_nothing_new` | Nichts zu übertragen – keine neuen Dateien. | Nothing to transfer – no new files. |
| 849 | `preview_group_open` | %1$s (Ordner noch offen) | %1$s (folder still open) |
| 850 | `preview_summary (one)` | %1$s Datei in %2$s | %1$s file in %2$s |
| 851 | `preview_summary (other)` | %1$s Dateien in %2$s | %1$s files in %2$s |
| 852 | `preview_needs_decision` | Entscheidung nötig (Ordnerauswahl bzw. Konflikt) | Decision needed (folder selection or conflict) |
| 853 | `preview_more` | … und %1$s weitere | … and %1$s more |
| 854 | `stats_unknown` | Unbekannt | Unknown |
| 855 | `stats_no_location` | Ohne Standortdaten | Without location data |
| 856 | `stats_no_profile` | Noch kein Profil vorhanden. | No profile yet. |
| 857 | `stats_no_files` | Noch keine Dateien gesichert. | No files backed up yet. |
| 858 | `stats_total` | Gesamt | Total |
| 859 | `stats_photos_videos` | %1$s Fotos · %2$s Videos | %1$s photos · %2$s videos |
| 860 | `stats_per_year` | Pro Jahr | Per year |
| 861 | `stats_bar_hint` | Balkenlänge = Anzahl Dateien | Bar length = number of files |
| 862 | `stats_bucket_value (one)` | %1$s Datei · %2$s | %1$s file · %2$s |
| 863 | `stats_bucket_value (other)` | %1$s Dateien · %2$s | %1$s files · %2$s |
| 864 | `stats_per_country` | Pro Land (aus GPS) | Per country (from GPS) |
| 865 | `first_sync_title` | Erste Synchronisierung | First sync |
| 866 | `first_sync_text` | Die erste Synchronisierung überträgt alle vorhandenen Dateien. Je nach Menge dauert sie lange und braucht viel Akku. Schliesse das Handy am besten ans Ladegerät an. | The first sync transfers all existing files. Depending on the amount it takes a long time and uses a lot of battery. Best connect the phone to the charger. |
| 867 | `first_sync_start` | Trotzdem starten | Start anyway |
| 868 | `dog_waiting` | Wartet | Waiting |
| 869 | `dog_idle` | Alles gesichert | Everything backed up |
| 870 | `dog_syncing` | Synchronisiert | Syncing |
| 871 | `run_dangling_log` | Unterbrochen – die App wurde beendet (z. B. durch ein Update). Der Rest folgt bei der nächsten Synchronisierung. | Interrupted – the app was closed (e.g. by an update). The rest follows at the next sync. |
| 872 | `run_dangling` | Unterbrochen – App wurde beendet (z. B. Update), Rest folgt bei der nächsten Synchronisierung | Interrupted – app was closed (e.g. update), rest follows at the next sync |

## Benachrichtigungen

| Nr. | Schlüssel | Deutsch | Englisch |
|---:|---|---|---|
| 873 | `notif_channel_progress` | Synchronisierung | Sync |
| 874 | `notif_channel_progress_desc` | Fortschritt der Übertragung | Transfer progress |
| 875 | `notif_channel_alerts_desc` | Ordnerauswahl, Löschbestätigung, Fehler | Folder selection, delete confirmation, errors |
| 876 | `notif_progress_title` | %1$s: %2$s / %3$s Dateien | %1$s: %2$s / %3$s files |
| 877 | `notif_progress_text` | %1$s von %2$s (%3$s %%) | %1$s of %2$s (%3$s %%) |
| 878 | `notif_summary_problem` | LeoSync – Problem | LeoSync – problem |
| 879 | `notif_summary_done` | LeoSync – fertig | LeoSync – done |
| 880 | `notif_decision_text (one)` | %1$s Datei in %2$s wartet: Es gibt umbenannte Monatsordner. Tippen zum Zuordnen. | %1$s file in %2$s is waiting: there are renamed month folders. Tap to assign. |
| 881 | `notif_decision_text (other)` | %1$s Dateien in %2$s warten: Es gibt umbenannte Monatsordner. Tippen zum Zuordnen. | %1$s files in %2$s are waiting: there are renamed month folders. Tap to assign. |
| 882 | `notif_delete_text (one)` | %1$s Datei ist verifiziert gesichert. Tippen, um sie vom Handy zu entfernen. | %1$s file is verified as backed up. Tap to remove it from the phone. |
| 883 | `notif_delete_text (other)` | %1$s Dateien sind verifiziert gesichert. Tippen, um sie vom Handy zu entfernen. | %1$s files are verified as backed up. Tap to remove them from the phone. |
| 884 | `notif_wait_wifi` | Wartet auf WLAN | Waiting for Wi-Fi |
| 885 | `notif_resuming` | Wird fortgesetzt … | Resuming … |
| 886 | `notif_wait_wifi_named` | Wartet auf WLAN „%1$s“ | Waiting for Wi-Fi “%1$s” |
| 887 | `notif_paused_files` | %1$s / %2$s Dateien | %1$s / %2$s files |
| 888 | `notif_conflict_text (one)` | %1$s Datei wurde auf Handy und PC geändert. Tippen, um zu entscheiden, welche Version gilt. | %1$s file was changed on phone and PC. Tap to decide which version wins. |
| 889 | `notif_conflict_text (other)` | %1$s Dateien wurden auf Handy und PC geändert. Tippen, um zu entscheiden, welche Version gilt. | %1$s files were changed on phone and PC. Tap to decide which version wins. |
| 890 | `notif_guard_title` | LeoSync wartet auf dich | LeoSync is waiting for you |
| 891 | `notif_guard_text` | Die Ordner-Synchronisierung „%1$s“ wurde zur Sicherheit angehalten. Tippen, um zu entscheiden. | The folder sync “%1$s” was stopped for safety. Tap to decide. |
| 892 | `notif_stale_title (one)` | Seit %1$s Tag nicht gesichert | Not backed up for %1$s day |
| 893 | `notif_stale_title (other)` | Seit %1$s Tagen nicht gesichert | Not backed up for %1$s days |
| 894 | `notif_stale_text` | Ist das Heim-WLAN verbunden und der PC eingeschaltet? Tippen zum Öffnen. | Is the home Wi-Fi connected and the PC switched on? Tap to open. |

## Sync-Ablauf, Verlauf-Texte, Fehlermeldungen

| Nr. | Schlüssel | Deutsch | Englisch |
|---:|---|---|---|
| 895 | `trigger_auto` | Automatisch | Automatic |
| 896 | `trigger_manual` | Manuell | Manual |
| 897 | `trigger_wifi` | WLAN verbunden | Wi-Fi connected |
| 898 | `trigger_resume` | Fortsetzen nach WLAN-Unterbruch | Resume after Wi-Fi interruption |
| 899 | `trigger_retry` | Erneute Prüfung (PC war nicht erreichbar) | Check again (PC was not reachable) |
| 900 | `trigger_poll` | Ordner-Prüfung (Intervall) | Folder check (interval) |
| 901 | `trigger_safety` | Sicherheitsnetz (%1$s h ohne Synchronisierung) | Safety net (%1$s h without sync) |
| 902 | `trigger_followup` | Nachholen (Mindestalter) | Catch up (minimum age) |
| 903 | `trigger_new_media` | Neue Fotos/Videos | New photos/videos |
| 904 | `trigger_pause_end` | Pause beendet | Pause ended |
| 905 | `trigger_resumed` | Fortgesetzt | Resumed |
| 906 | `trigger_deferred` | Nachgeholt (Start während einer laufenden Synchronisierung) | Caught up (start requested during a running sync) |
| 907 | `trigger_skip_followup` | Nachholen geänderter Dateien | Catching up changed files |
| 908 | `trigger_generic` | Auslöser | Trigger |
| 909 | `stop_wifi` | WLAN getrennt | Wi-Fi disconnected |
| 910 | `stop_charger` | nicht mehr am Ladegerät | no longer on the charger |
| 911 | `stop_timeout` | Zeitlimit von Android | Android time limit |
| 912 | `run_no_foreground` | Lief ohne Vordergrund: Android kann die Synchronisierung unterbrechen; sie wird danach fortgesetzt. Zuverlässiger mit Akku-Ausnahme (Einstellungen). | Ran without foreground: Android can interrupt the sync; it is resumed afterwards. More reliable with the battery exception (Settings). |
| 913 | `stop_system` | vom System gestoppt (%1$s) | stopped by the system (%1$s) |
| 914 | `stop_android` | von Android gestoppt (%1$s) | stopped by Android (%1$s) |
| 915 | `skip_manual_only` | Automatische Synchronisierungen ausgeschaltet (nur manuell) | Automatic syncs turned off (manual only) |
| 916 | `skip_paused_until` | Pausiert bis %1$s | Paused until %1$s |
| 917 | `skip_roaming` | Roaming: automatische Synchronisierung über mobile Daten übersprungen | Roaming: automatic sync over mobile data skipped |
| 918 | `skip_pc_unreachable` | PC nicht erreichbar: %1$s | PC not reachable: %1$s |
| 919 | `skip_pc_not_recognised` | PC nicht erkannt: %1$s | PC not recognised: %1$s |
| 920 | `skip_pc_failed` | Verbindung zum PC nicht möglich: %1$s | Connection to the PC not possible: %1$s |
| 921 | `wait_for_pc` | Warte auf PC (%1$s) – Versuch %2$s/%3$s… | Waiting for PC (%1$s) – attempt %2$s/%3$s… |
| 922 | `summary_failed` | Synchronisierung fehlgeschlagen – %1$s | Sync failed – %1$s |
| 923 | `summary_transferred (one)` | %1$s Datei übertragen (%2$s) | %1$s file transferred (%2$s) |
| 924 | `summary_transferred (other)` | %1$s Dateien übertragen (%2$s) | %1$s files transferred (%2$s) |
| 925 | `summary_errors (one)` | %1$s Fehler – Details im Verlauf | %1$s error – details in the history |
| 926 | `summary_errors (other)` | %1$s Fehler – Details im Verlauf | %1$s errors – details in the history |
| 927 | `summary_no_space` | Zu wenig Speicher auf dem Handy – einige Dateien wurden nicht heruntergeladen. | Not enough storage on the phone – some files were not downloaded. |
| 928 | `summary_pc_unreachable` | PC nicht erreichbar (%1$s). Ist er eingeschaltet und die Freigabe aktiv? | PC not reachable (%1$s). Is it switched on and the Share active? |
| 929 | `paused_connection_retry` | Verbindung unterbrochen – neuer Versuch in %1$s Minuten | Connection interrupted – next attempt in %1$s minutes |
| 930 | `run_nothing` | Nichts zu tun | Nothing to do |
| 931 | `run_uploaded` | %1$s übertragen | %1$s transferred |
| 932 | `run_found` | %1$s bereits vorhanden | %1$s already present |
| 933 | `run_failed (one)` | %1$s Fehler | %1$s error |
| 934 | `run_failed (other)` | %1$s Fehler | %1$s errors |
| 935 | `run_waiting (one)` | %1$s wartet auf Ordnerauswahl | %1$s waiting for folder selection |
| 936 | `run_waiting (other)` | %1$s warten auf Ordnerauswahl | %1$s waiting for folder selection |
| 937 | `run_up` | ↑ %1$s hochgeladen | ↑ %1$s uploaded |
| 938 | `run_down` | ↓ %1$s heruntergeladen | ↓ %1$s downloaded |
| 939 | `run_trashed` | %1$s in Papierkorb | %1$s in trash |
| 940 | `run_conflicts (one)` | %1$s Konflikt | %1$s conflict |
| 941 | `run_conflicts (other)` | %1$s Konflikte | %1$s conflicts |
| 942 | `run_space_full` | Speicher voll | storage full |
| 943 | `run_deferred` | %1$s folgen später | %1$s follow later |
| 944 | `guard_title_remote` | PC-Ordner nicht gefunden | PC folder not found |
| 945 | `guard_title_local` | Handy-Ordner nicht gefunden | Phone folder not found |
| 946 | `guard_title_mass` | Viele Dateien würden gelöscht | Many files would be deleted |
| 947 | `guard_text_remote` | Der PC-Ordner von „%1$s“ fehlt oder ist leer, obwohl schon synchronisiert wurde. Wurde er umbenannt oder verschoben? Bis du entscheidest, ruht dieses Profil. | The PC folder of “%1$s” is missing or empty although it was already synced. Was it renamed or moved? Until you decide, this profile rests. |
| 948 | `guard_text_local` | Der Handy-Ordner von „%1$s“ fehlt oder ist leer, obwohl schon synchronisiert wurde. Wurde er umbenannt oder verschoben? Bis du entscheidest, ruht dieses Profil. | The phone folder of “%1$s” is missing or empty although it was already synced. Was it renamed or moved? Until you decide, this profile rests. |
| 949 | `guard_text_folder (one)` | In „%1$s“ würde der ganze Ordner „%2$s“ in den Papierkorb verschoben (insgesamt %3$s Datei). Wurde er wirklich gelöscht? Bis du entscheidest, ruht dieses Profil. | In “%1$s” the whole folder “%2$s” would be moved to the trash (%3$s file in total). Was it really deleted? Until you decide, this profile rests. |
| 950 | `guard_text_folder (other)` | In „%1$s“ würde der ganze Ordner „%2$s“ in den Papierkorb verschoben (insgesamt %3$s Dateien). Wurde er wirklich gelöscht? Bis du entscheidest, ruht dieses Profil. | In “%1$s” the whole folder “%2$s” would be moved to the trash (%3$s files in total). Was it really deleted? Until you decide, this profile rests. |
| 951 | `guard_text_mass` | In „%1$s“ würden %2$s von %3$s Dateien in den Papierkorb verschoben. Wurde wirklich so viel gelöscht? Bis du entscheidest, ruht dieses Profil. | In “%1$s”, %2$s of %3$s files would be moved to the trash. Was that much really deleted? Until you decide, this profile rests. |
| 952 | `guard_text_check` | Der gewählte Ordner von „%1$s“ passt nicht zum bisherigen Abgleichstand. Bis du entscheidest, ruht dieses Profil. | The chosen folder of “%1$s” does not match the previous sync status. Until you decide, this profile rests. |
| 953 | `delete_log_done` | Gelöscht: %1$s | Deleted: %1$s |
| 954 | `delete_log_failed` | Löschen fehlgeschlagen: %1$s – %2$s | Deleting failed: %1$s – %2$s |
| 955 | `delete_log_pc_missing` | Nicht vom Handy entfernt: Die Kopie auf dem PC fehlt oder weicht ab, die Datei wird erneut gesichert: %1$s | Not removed from the phone: the copy on the PC is missing or differs, the file will be backed up again: %1$s |
| 956 | `delete_log_phone_unreadable` | Nicht vom Handy entfernt: Die Datei ist für die Prüfung nicht lesbar: %1$s | Not removed from the phone: the file cannot be read for the check: %1$s |
| 957 | `delete_pc_unreachable` | PC nicht erreichbar – nichts gelöscht. Die Dateien bleiben vorgemerkt. | PC not reachable – nothing deleted. The files stay queued. |
| 958 | `delete_pc_rejected` | PC nicht erkannt – nichts gelöscht. %1$s | PC not recognised – nothing deleted. %1$s |
| 959 | `delete_pc_reset` | %1$d Dateien nicht entfernt: Die Kopie auf dem PC fehlt oder weicht ab. Sie werden erneut gesichert. | %1$d files not removed: the copy on the PC is missing or differs. They will be backed up again. |
| 960 | `err_friendly_login` | Benutzer oder Passwort stimmt nicht. Bitte im Profil prüfen (bei einem Microsoft-Konto die E-Mail-Adresse als Benutzer). | User name or password is wrong. Please check the profile (for a Microsoft account use the e-mail address as user). |
| 961 | `err_friendly_credential_unreadable` | Das gespeicherte Passwort ist nicht mehr lesbar (z. B. nach einem Systemupdate oder einer Wiederherstellung). Bitte im Profil neu eingeben. | The saved password can no longer be read (for example after a system update or a restore). Please enter it again in the profile. |
| 962 | `err_friendly_credential_unavailable` | Der sichere Speicher für das Passwort ist gerade nicht verfügbar. Es wird später erneut versucht. | The secure storage for the password is not available right now. It will be tried again later. |
| 963 | `err_friendly_guest_session` | Der PC hat die Anmeldung nur als Gast angenommen (falsches Passwort oder fremdes Gerät). Es wurden keine Dateien übertragen. | The PC accepted the login only as a guest (wrong password or a different device). No files were transferred. |
| 964 | `err_friendly_share_missing` | Diese Freigabe gibt es auf dem PC nicht. Prüfe die Schreibweise oder wähle sie mit „Ordner auf dem PC wählen…“. | This Share does not exist on the PC. Check the spelling or pick it with “Choose folder on the PC…”. |
| 965 | `err_friendly_folder_missing` | Der Ordner wurde auf dem PC nicht gefunden. Wurde er umbenannt oder verschoben? | The folder was not found on the PC. Was it renamed or moved? |
| 966 | `err_friendly_access` | Kein Zugriff auf diesen Ordner. Erlaube dem Benutzer auf dem PC Lesen und Schreiben (Eigenschaften → Freigabe). | No access to this folder. Allow the user to read and write on the PC (Properties → Sharing). |
| 967 | `err_friendly_access_unencrypted` | Kein Zugriff auf diesen Ordner. Erlaube dem Benutzer auf dem PC Lesen und Schreiben (Eigenschaften → Freigabe). Verlangt die Freigabe Verschlüsselung, stelle die Verschlüsselung dieses Profils auf «Wenn möglich» oder «Erforderlich». | No access to this folder. Allow the user to read and write on the PC (Properties → Sharing). If the share requires encryption, set the encryption level of this profile to “If possible” or “Required”. |
| 968 | `err_friendly_pc_full` | Auf dem PC ist kein Speicherplatz mehr frei. | The PC has no free storage left. |
| 969 | `err_friendly_phone_full` | Auf dem Handy ist kein Speicherplatz mehr frei. | The phone has no free storage left. |
| 970 | `err_friendly_unknown_host` | Der PC-Name wurde nicht gefunden. Trage am besten die IP-Adresse ein, z. B. \\192.168.1.20. | The PC name was not found. Best enter the IP address, e.g. \\192.168.1.20. |
| 971 | `err_friendly_unreachable` | Der PC ist nicht erreichbar. Ist er eingeschaltet und im selben WLAN? | The PC is not reachable. Is it switched on and on the same Wi-Fi? |
| 972 | `smb_err_path` | Ungültiger Freigabe-Pfad: „%1$s“ (erwartet \\server\freigabe) | Invalid Share path: “%1$s” (expected \\server\share) |
| 973 | `smb_err_not_disk` | „%1$s“ ist keine Datei-Freigabe | “%1$s” is not a file Share |
| 974 | `smb_err_incomplete` | Upload unvollständig: %1$s von %2$s Bytes | Upload incomplete: %1$s of %2$s bytes |
| 975 | `smb_err_resume` | Fortgesetzter Upload passt nicht zur Datei – wird neu übertragen | Resumed upload does not match the file – will be transferred again |
| 976 | `smb_err_pc_changed` | Unter %1$s antwortet ein anderer PC als bisher. Es wurden keine Anmeldedaten gesendet. Mit „Verbindung testen“ prüfen und den neuen PC übernehmen. | A different PC answers at %1$s than before. No sign-in data was sent. Use “Test connection” to check it and trust the new PC. |
| 977 | `smb_err_pc_unknown` | Der PC unter %1$s ist noch nicht erkannt. Es wurden keine Anmeldedaten gesendet. Profil öffnen und „Verbindung testen“ tippen (oder das Profil speichern), damit die App den PC kennenlernt. | The PC at %1$s is not recognised yet. No sign-in data was sent. Open the profile and tap “Test connection” (or save the profile) so the app learns the PC. |
| 978 | `smb_err_pc_wrong_net` | Der PC unter %1$s ist noch nicht erkannt, und das aktuelle WLAN steht nicht in der Liste des Profils (oder der WLAN-Name ist nicht lesbar). Es wurden keine Anmeldedaten gesendet. Im eingetragenen WLAN „Verbindung testen“ tippen, den Standortzugriff erlauben oder die PC-Kennung auf „Nur PC-Kennung“ stellen. | The PC at %1$s is not recognised yet, and the current Wi-Fi is not in the profile’s list (or the Wi-Fi name cannot be read). No sign-in data was sent. Tap “Test connection” in the listed Wi-Fi, allow location access, or set the PC ID to “PC ID only”. |
| 979 | `smb_err_pc_no_id` | Der PC unter %1$s liefert keine Kennung und kann nicht erkannt werden. Es wurden keine Anmeldedaten gesendet. Die PC-Kennung auf „Nur WLAN-Name“ stellen. | The PC at %1$s provides no ID, so it cannot be recognised. No sign-in data was sent. Set the PC ID to “Wi-Fi name only”. |
| 980 | `smb_err_encryption_required` | Für dieses Profil ist die Verschlüsselung erforderlich, aber der PC unter %1$s bietet keine SMB-3-Verschlüsselung an. Es wurden keine Anmeldedaten gesendet. Die Verschlüsselung im Profil ändern oder am PC die SMB-Verschlüsselung aktivieren. | Encryption is required for this profile, but the PC at %1$s does not offer SMB 3 encryption. No sign-in data was sent. Change the encryption setting of the profile or enable SMB encryption on the PC. |
| 981 | `smb_err_encryption_not_established` | Für dieses Profil ist die Verschlüsselung erforderlich, aber die Verbindung zu %1$s konnte nicht verschlüsselt werden. Die Verbindung wurde getrennt, es wurden keine Daten übertragen. Die Verschlüsselung im Profil ändern oder am PC die SMB-Verschlüsselung prüfen. | Encryption is required for this profile, but the connection to %1$s could not be encrypted. The connection was closed and no data was transferred. Change the encryption setting of the profile or check SMB encryption on the PC. |
| 982 | `smb_test_missing_folder` | Verbunden, aber Ordner „%1$s“ existiert nicht (wird bei der ersten Synchronisierung angelegt). | Connected, but folder “%1$s” does not exist (will be created at the first sync). |
| 983 | `smb_test_ok (one)` | Verbindung OK – %1$s (%2$s Eintrag) | Connection OK – %1$s (%2$s entry) |
| 984 | `smb_test_ok (other)` | Verbindung OK – %1$s (%2$s Einträge) | Connection OK – %1$s (%2$s entries) |
| 985 | `backup_err_write` | Datei kann nicht geschrieben werden | File cannot be written |
| 986 | `backup_err_read` | Datei kann nicht gelesen werden | File cannot be read |
| 987 | `backup_err_version` | Sicherung hat Format-Version %1$s – diese App kennt bis Version %2$s. Bitte LeoSync aktualisieren. | Backup has format version %1$s – this app knows up to version %2$s. Please update LeoSync. |
| 988 | `backup_err_json` | Die Datei ist keine LeoSync-Sicherung (kein gültiges JSON). | The file is not a LeoSync backup (no valid JSON). |
| 989 | `backup_err_format` | Die Datei ist keine LeoSync-Sicherung. | The file is not a LeoSync backup. |
| 990 | `stale_pending (one)` | %1$s: %2$s neue Datei wartet, letzte Sicherung %3$s | %1$s: %2$s new file waiting, last backup %3$s |
| 991 | `stale_pending (other)` | %1$s: %2$s neue Dateien warten, letzte Sicherung %3$s | %1$s: %2$s new files waiting, last backup %3$s |
| 992 | `stale_last` | %1$s: letzte Sicherung %2$s | %1$s: last backup %2$s |
| 993 | `space_profile_id` | Profil %1$s | Profile %1$s |
| 994 | `tree_err_unreadable` | Handy-Ordner nicht lesbar | Phone folder not readable |
| 995 | `tree_err_mkdir` | Ordner „%1$s“ konnte nicht angelegt werden | Folder “%1$s” could not be created |
| 996 | `tree_err_read` | Datei nicht lesbar | File not readable |
| 997 | `tree_err_write` | Datei nicht beschreibbar | File not writable |
| 998 | `tree_err_rename` | Umbenennen von „%1$s“ fehlgeschlagen | Renaming “%1$s” failed |
| 999 | `tree_err_name_taken` | „%1$s“ gibt es auf dem Handy unter diesem Namen schon, sie wird aber nicht aufgelistet (nicht indiziert); die Kopie wurde nicht behalten, um Duplikate zu vermeiden | “%1$s” already exists on the phone under this name but is not listed (not indexed); the copy was not kept to avoid duplicates |
| 1000 | `tree_err_size` | Grösse nach Download stimmt nicht (%1$s statt %2$s Bytes) | Size after download does not match (%1$s instead of %2$s bytes) |
| 1001 | `smb_err_download_cut` | Download unterbrochen (%1$s von %2$s Bytes) | Download interrupted (%1$s of %2$s bytes) |
| 1002 | `tree_err_move` | Verschieben nicht unterstützt | Moving not supported |
| 1003 | `tree_err_changed` | Datei wurde während des Laufs geändert | File was changed during the run |
| 1004 | `tree_err_create` | Datei „%1$s“ konnte nicht angelegt werden | File “%1$s” could not be created |
| 1005 | `eng_start_mirror` | Start (%1$s) – Ordner-Synchronisierung „%2$s“ (%3$s) | Start (%1$s) – folder sync “%2$s” (%3$s) |
| 1006 | `eng_reading_folders` | Lese Ordner… | Reading folders… |
| 1007 | `eng_local_missing` | Handy-Ordner nicht gefunden – bitte im Profil neu auswählen | Phone folder not found – please choose it again in the profile |
| 1008 | `eng_offline_preview` | Offline-Vorschau (Freigabe nicht erreichbar): %1$s | Offline preview (Share not reachable): %1$s |
| 1009 | `eng_counts` | Handy: %1$s Dateien, PC: %2$s Dateien | Phone: %1$s files, PC: %2$s files |
| 1010 | `eng_keep_files` | Löschungen werden in diesem Lauf nicht übernommen (deine Entscheidung) | Deletions are not applied in this run (your decision) |
| 1011 | `eng_comparing` | Vergleiche… | Comparing… |
| 1012 | `eng_would_stop` | ⚠ Der Lauf würde anhalten und nachfragen: %1$s | ⚠ The run would stop and ask: %1$s |
| 1013 | `eng_would_trash_folder` | Der Ordner „%1$s“ käme ganz in den Papierkorb | The folder “%1$s” would go to the trash completely |
| 1014 | `eng_would_trash_files` | %1$s von %2$s Dateien kämen in den Papierkorb | %1$s of %2$s files would go to the trash |
| 1015 | `eng_actions_first` | %1$s Aktionen – die ersten %2$s simuliert | %1$s actions – the first %2$s simulated |
| 1016 | `eng_actions_all` | %1$s Aktionen simuliert | %1$s actions simulated |
| 1017 | `eng_wifi_lost` | Abgebrochen: WLAN nicht mehr verbunden | Cancelled: Wi-Fi no longer connected |
| 1018 | `eng_interrupted_log` | Unterbrochen (%1$s) – wird automatisch fortgesetzt | Interrupted (%1$s) – resumes automatically |
| 1019 | `eng_interrupted_mirror` | Unterbrochen (%1$s) – ↑ %2$s / ↓ %3$s übertragen, Rest folgt automatisch | Interrupted (%1$s) – ↑ %2$s / ↓ %3$s transferred, the rest follows automatically |
| 1020 | `eng_paused_log` | Pausiert (von dir) – läuft beim nächsten automatischen Start weiter | Paused (by you) – continues at the next automatic start |
| 1021 | `eng_connection_log_mirror` | Verbindung unterbrochen (%1$s) – wird bei der nächsten Synchronisierung fortgesetzt | Connection interrupted (%1$s) – continues at the next sync |
| 1022 | `eng_connection_mirror` | Verbindung unterbrochen – ↑ %1$s / ↓ %2$s übertragen, Rest folgt bei der nächsten Synchronisierung | Connection interrupted – ↑ %1$s / ↓ %2$s transferred, the rest follows at the next sync |
| 1023 | `eng_run_failed` | Lauf fehlgeschlagen: %1$s | Run failed: %1$s |
| 1024 | `eng_waiting_decision` | Wartet auf deine Entscheidung: %1$s | Waiting for your decision: %1$s |
| 1025 | `eng_trash_local` | 🗑 Handy: %1$s (auf dem PC gelöscht) | 🗑 Phone: %1$s (deleted on the PC) |
| 1026 | `eng_trash_remote` | 🗑 PC: %1$s (auf dem Handy gelöscht) | 🗑 PC: %1$s (deleted on the phone) |
| 1027 | `eng_conflict` | ⚠ Konflikt: %1$s wurde auf Handy und PC geändert | ⚠ Conflict: %1$s was changed on phone and PC |
| 1028 | `eng_transfer_mirror (one)` | Übertrage %1$s Datei mit %2$s parallelen Verbindungen | Transferring %1$s file with %2$s parallel connections |
| 1029 | `eng_transfer_mirror (other)` | Übertrage %1$s Dateien mit %2$s parallelen Verbindungen | Transferring %1$s files with %2$s parallel connections |
| 1030 | `eng_no_space` | Zu wenig Speicher auf dem Handy – übersprungen: %1$s | Not enough storage on the phone – skipped: %1$s |
| 1031 | `eng_on_pc` | auf dem PC | on the PC |
| 1032 | `eng_on_phone` | auf dem Handy | on the phone |
| 1033 | `eng_err_renamed_missing` | Umbenannte Datei nicht gefunden | Renamed file not found |
| 1034 | `eng_conflict_undone` | Auswahl für %1$s wurde rückgängig gemacht – der Konflikt bleibt offen | Choice for %1$s was undone – the conflict stays open |
| 1035 | `eng_err_conflict_name_exhausted` | Kein freier Name für die Konfliktkopie von %1$s | No free name for the conflict copy of %1$s |
| 1036 | `eng_err_conflict_name_differs` | Der Speicher-Anbieter hat die Konfliktkopie "%2$s" statt "%1$s" genannt – nicht hochgeladen, die nächste Synchronisierung überträgt sie als neue Datei | The storage app named the conflict copy "%2$s" instead of "%1$s" – not uploaded, the next sync transfers it as a new file |
| 1037 | `eng_err_after_upload_missing` | Datei nach Upload nicht gefunden | File not found after upload |
| 1038 | `eng_err_after_upload_size` | Grösse nach Upload stimmt nicht | Size after upload does not match |
| 1039 | `eng_err_after_download_missing` | Datei nach Download nicht gefunden | File not found after download |
| 1040 | `eng_err_after_download_size` | Grösse nach Download stimmt nicht | Size after download does not match |
| 1041 | `eng_err_identity_store` | Der Stand des Downloads konnte nicht gespeichert werden. Später erneut versuchen. | Could not save the download state. Try again later. |
| 1042 | `eng_conflict_reopened` | Konflikt wieder offen: %1$s wurde seit deiner Entscheidung geändert | Conflict reopened: %1$s changed since your decision |
| 1043 | `run_renamed` | %1$s umbenannt/verschoben | %1$s renamed/moved |
| 1044 | `eng_pv_rename` | ⇄ Umbenannt/verschoben | ⇄ Renamed/moved |
| 1045 | `eng_renamed_pc` | Auf dem PC umbenannt/verschoben: %1$s → %2$s | Renamed/moved on the PC: %1$s → %2$s |
| 1046 | `eng_renamed_phone` | Auf dem Handy umbenannt/verschoben: %1$s → %2$s | Renamed/moved on the phone: %1$s → %2$s |
| 1047 | `eng_rename_skipped` | Umbenennen/Verschieben nicht ausgeführt (inzwischen geändert), wird beim nächsten Lauf normal behandelt: %1$s → %2$s | Rename/move not carried out (changed in the meantime), handled normally next run: %1$s → %2$s |
| 1048 | `eng_rename_failed` | Umbenennen/Verschieben fehlgeschlagen, wird beim nächsten Lauf normal behandelt: %1$s → %2$s (%3$s) | Rename/move failed, handled normally next run: %1$s → %2$s (%3$s) |
| 1049 | `eng_rename_unsupported` | Dieser Speicherort kann Dateien nicht verschieben: Umbenennungen und Verschiebungen werden für dieses Profil nicht mehr erkannt | This storage location cannot move files: renames and moves are no longer detected for this profile |
| 1050 | `eng_net_relearned` | Netz geändert, neu gelernt | Network changed, learned again |
| 1051 | `eng_enc_auto_required` | Verschlüsselung auf ‹Erforderlich› umgestellt, weil der PC verschlüsselt | Encryption set to ‹Required› because the PC encrypts |
| 1052 | `eng_deletion_overruled_phone` | Löschung nicht übernommen: %1$s wurde auf dem Handy geändert | Deletion not applied: %1$s was changed on the phone |
| 1053 | `eng_deletion_overruled_pc` | Löschung nicht übernommen: %1$s wurde auf dem PC geändert | Deletion not applied: %1$s was changed on the PC |
| 1054 | `eng_name_collision_case` | Namen unterscheiden sich nur in Gross-/Kleinschreibung (%3$s): %1$s / %2$s | Names differ only in upper/lower case (%3$s): %1$s / %2$s |
| 1055 | `eng_name_collision_unicode` | Namen unterscheiden sich nur in der Zeichenkodierung (%3$s): %1$s / %2$s | Names differ only in character encoding (%3$s): %1$s / %2$s |
| 1056 | `eng_name_invalid_pc` | Name auf dem PC nicht erlaubt: %1$s | Name not allowed on the PC: %1$s |
| 1057 | `eng_zero_time` | Dieser Speicherort meldet keine Änderungszeiten. Geänderte Dateien gleicher Grösse werden nicht erkannt. Wähle den Ordner wenn möglich über ‹Interner Speicher› bzw. den Datei-Anbieter des Geräts. | This storage location reports no modification times. Changed files of the same size are not detected. If possible, choose the folder via ‹Internal storage› or the device’s file provider. |
| 1058 | `eng_resumed_at` | (fortgesetzt ab %1$s) | (resumed from %1$s) |
| 1059 | `eng_restoring (one)` | Stelle %1$s Datei aus dem Papierkorb wieder her | Restoring %1$s file from the trash |
| 1060 | `eng_restoring (other)` | Stelle %1$s Dateien aus dem Papierkorb wieder her | Restoring %1$s files from the trash |
| 1061 | `eng_restore_gone` | ↺ %1$s: Version im Papierkorb nicht mehr vorhanden – übersprungen | ↺ %1$s: version in the trash no longer present – skipped |
| 1062 | `eng_err_restored_missing` | Wiederhergestellte Datei nicht gefunden | Restored file not found |
| 1063 | `eng_restored` | ↺ %1$s wiederhergestellt (Version vom %2$s) | ↺ %1$s restored (version from %2$s) |
| 1064 | `eng_restore_failed_rolled_back` | ✗ Wiederherstellen %1$s: %2$s – zurückgerollt, wird bei der nächsten Synchronisierung erneut versucht | ✗ Restore %1$s: %2$s – rolled back, will be tried again at the next sync |
| 1065 | `eng_restore_failed_partial` | ✗ Wiederherstellen %1$s: %2$s – nicht vollständig zurückgerollt, siehe Papierkorb | ✗ Restore %1$s: %2$s – not completely rolled back, see trash |
| 1066 | `eng_restore_gave_up` | ✗ Wiederherstellen %1$s: %2$s – zurückgerollt; nach %3$d Versuchen nicht mehr automatisch wiederholt. Vormerkung und Papierkorb-Version bleiben erhalten; auf der Startseite „Erneut versuchen“ oder „Verwerfen“ wählen | ✗ Restore %1$s: %2$s – rolled back; not tried again automatically after %3$d attempts. The request and the trash version are kept; choose “Try again” or “Discard” on the home screen |
| 1067 | `eng_purged_pc` | 🗑 PC-Papierkorb geleert (vorgemerkt): %1$s Tagesordner, %2$s einzelne Dateien endgültig gelöscht | 🗑 PC trash emptied (scheduled): %1$s day folders, %2$s single files permanently deleted |
| 1068 | `eng_purge_pc_failed` | ✗ PC-Papierkorb leeren: %1$s | ✗ Emptying PC trash: %1$s |
| 1069 | `eng_changed_during_run` | ⏭ %1$s: wurde während des Laufs %2$s geändert – folgt bei der nächsten Synchronisierung | ⏭ %1$s: was changed %2$s during the run – follows at the next sync |
| 1070 | `eng_syncing` | Synchronisiere… | Syncing… |
| 1071 | `eng_purge_local_failed` | Papierkorb auf dem Handy nicht geleert: %1$s | Trash on the phone not emptied: %1$s |
| 1072 | `eng_purge_remote_failed` | Papierkorb auf dem PC nicht geleert: %1$s | Trash on the PC not emptied: %1$s |
| 1073 | `eng_purged_expired` | 🗑 Papierkorb: %1$s Ordner älter als %2$s Tage endgültig gelöscht | 🗑 Trash: %1$s folders older than %2$s days permanently deleted |
| 1074 | `eng_pv_upload_trash` | ↑ Handy → PC (PC-Version in Papierkorb) | ↑ Phone → PC (PC version to trash) |
| 1075 | `eng_pv_upload` | ↑ Handy → PC | ↑ Phone → PC |
| 1076 | `eng_pv_download_trash` | ↓ PC → Handy (Handy-Version in Papierkorb) | ↓ PC → Phone (phone version to trash) |
| 1077 | `eng_pv_download` | ↓ PC → Handy | ↓ PC → Phone |
| 1078 | `eng_pv_trash_local` | 🗑 Auf dem Handy in den Papierkorb | 🗑 To the trash on the phone |
| 1079 | `eng_pv_trash_remote` | 🗑 Auf dem PC in den Papierkorb | 🗑 To the trash on the PC |
| 1080 | `eng_pv_conflict` | ⚠ Konflikt – Entscheidung nötig | ⚠ Conflict – decision needed |
| 1081 | `eng_pv_keep_both` | ⇄ Beide Versionen behalten | ⇄ Keep both versions |
| 1082 | `eng_end` | Ende: %1$s | End: %1$s |
| 1083 | `eng_already_in (one)` | = %1$s Datei bereits in %2$s vorhanden | = %1$s file already present in %2$s |
| 1084 | `eng_already_in (other)` | = %1$s Dateien bereits in %2$s vorhanden | = %1$s files already present in %2$s |
| 1085 | `eng_start_photos` | Start (%1$s) – Profil „%2$s“ → %3$s | Start (%1$s) – profile “%2$s” → %3$s |
| 1086 | `eng_searching` | Suche Dateien… | Searching files… |
| 1087 | `eng_filtered (one)` | %1$s Datei durch Filter ausgeschlossen | %1$s file excluded by filter |
| 1088 | `eng_filtered (other)` | %1$s Dateien durch Filter ausgeschlossen | %1$s files excluded by filter |
| 1089 | `eng_found (one)` | %1$s Datei gefunden, %2$s neu, %3$s bereits gesichert | %1$s file found, %2$s new, %3$s already backed up |
| 1090 | `eng_found (other)` | %1$s Dateien gefunden, %2$s neu, %3$s bereits gesichert | %1$s files found, %2$s new, %3$s already backed up |
| 1091 | `eng_too_young` | %1$s noch zu jung (< %2$s min) | %1$s still too young (< %2$s min) |
| 1092 | `eng_connecting` | Verbinde… | Connecting… |
| 1093 | `eng_no_geo` | Länderdaten fehlen – Ordner ohne Länderzusatz | Country data missing – folders without country suffix |
| 1094 | `eng_no_media_location` | Berechtigung „Medien-Standort“ fehlt – GPS wird nicht ausgewertet | Permission “media location” missing – GPS is not evaluated |
| 1095 | `eng_analysing` | Analysiere… | Analysing… |
| 1096 | `eng_new_files_first (one)` | %1$s neue Datei – die ersten %2$s simuliert | %1$s new file – the first %2$s simulated |
| 1097 | `eng_new_files_first (other)` | %1$s neue Dateien – die ersten %2$s simuliert | %1$s new files – the first %2$s simulated |
| 1098 | `eng_new_files_all (one)` | %1$s neue Datei simuliert | %1$s new file simulated |
| 1099 | `eng_new_files_all (other)` | %1$s neue Dateien simuliert | %1$s new files simulated |
| 1100 | `eng_transfer_photos` | Übertrage mit %1$s parallelen Verbindungen | Transferring with %1$s parallel connections |
| 1101 | `eng_analysis_failed` | ✗ %1$s: Analyse fehlgeschlagen: %2$s | ✗ %1$s: analysis failed: %2$s |
| 1102 | `eng_waiting_groups (one)` | %1$s Datei wartet auf Ordnerauswahl (%2$s) | %1$s file waiting for folder selection (%2$s) |
| 1103 | `eng_waiting_groups (other)` | %1$s Dateien warten auf Ordnerauswahl (%2$s) | %1$s files waiting for folder selection (%2$s) |
| 1104 | `eng_interrupted_photos` | Unterbrochen (%1$s) – %2$s übertragen, Rest folgt automatisch | Interrupted (%1$s) – %2$s transferred, the rest follows automatically |
| 1105 | `eng_paused_photos` | Pausiert (von dir) – %1$s übertragen, Rest beim nächsten automatischen Start | Paused (by you) – %1$s transferred, the rest at the next automatic start |
| 1106 | `eng_connection_log_photos` | Verbindung unterbrochen (%1$s) – offene Dateien werden bei der nächsten Synchronisierung übertragen | Connection interrupted (%1$s) – open files are transferred at the next sync |
| 1107 | `eng_connection_photos` | Verbindung unterbrochen – %1$s übertragen, Rest folgt bei der nächsten Synchronisierung | Connection interrupted – %1$s transferred, the rest follows at the next sync |
| 1108 | `eng_transferring` | Übertrage… | Transferring… |
| 1109 | `eng_err_unreadable` | Datei nicht lesbar: %1$s | File not readable: %1$s |
| 1110 | `eng_err_verify` | Prüfung nach Upload fehlgeschlagen – Datei wird beim nächsten Lauf erneut übertragen | Check after upload failed – the file will be transferred again at the next run |
| 1111 | `eng_err_verify_cleanup` | Die fehlerhafte Kopie auf dem PC konnte nicht entfernt werden (%1$s): %2$s | The faulty copy on the PC could not be removed (%1$s): %2$s |
| 1112 | `eng_present_unverified` | Bereits auf dem PC, aber Gleichheit nicht bestätigt – bleibt auf dem Handy: %1$s | Already on the PC, but not confirmed identical – stays on the phone: %1$s |
| 1113 | `tree_sd_card` | SD-Karte | SD card |
| 1114 | `tree_unknown_folder` | Handy-Ordner | Mobile folder |
| 1115 | `eng_trash_migrated_phone` | 🗑 Papierkorb auf dem Handy nach «%1$s» übernommen | 🗑 Trash on the phone moved to “%1$s” |
| 1116 | `eng_trash_migrated_pc` | 🗑 Papierkorb auf dem PC nach «%1$s» übernommen | 🗑 Trash on the PC moved to “%1$s” |
| 1117 | `eng_trash_migrate_failed_phone` | Papierkorb auf dem Handy nicht nach «%1$s» übernommen (%2$s) – nächster Versuch bei der nächsten Synchronisierung, nichts geht verloren | Trash on the phone not moved to “%1$s” (%2$s) – will be tried again at the next sync, nothing is lost |
| 1118 | `eng_trash_migrate_failed_pc` | Papierkorb auf dem PC nicht nach «%1$s» übernommen (%2$s) – nächster Versuch bei der nächsten Synchronisierung, nichts geht verloren | Trash on the PC not moved to “%1$s” (%2$s) – will be tried again at the next sync, nothing is lost |
| 1119 | `eng_trash_pc_fallback` | Der PC nimmt den Papierkorb-Ordner «%1$s» nicht an (%3$s); ersatzweise wird «%2$s» verwendet, nichts geht verloren | The PC does not accept the trash folder “%1$s” (%3$s); “%2$s” is used instead, nothing is lost |
| 1120 | `tree_err_rename_short` | Umbenennen fehlgeschlagen | Renaming failed |
| 1121 | `decision_folder_path` | Ordnerpfad unter dem Jahr, z. B. %1$s (der letzte Ordner muss %2$s enthalten) | Folder path below the year, e.g. %1$s (the last folder must contain %2$s) |
| 1122 | `decision_err_one` | Bitte genau einen Ordnernamen angeben, der %1$s enthält, z. B. „%1$s Urlaub“. | Please enter a single folder name that contains %1$s, e.g. “%1$s Holiday”. |
| 1123 | `decision_err_two` | Bitte <Land>\<MM> angeben, z. B. „%1$s“. Der letzte Ordner muss die Monatszahl enthalten. | Please enter <country>\<MM>, e.g. “%1$s”. The last folder must contain the month number. |
| 1124 | `decision_err_empty` | Ordnernamen dürfen nicht leer sein, „.“ oder „..“ lauten. | Folder names must not be empty, “.” or “..”. |
| 1125 | `decision_err_month` | Der letzte Ordner muss die Monatszahl %1$s enthalten. | The last folder must contain the month number %1$s. |

## Hersteller-Hinweise

| Nr. | Schlüssel | Deutsch | Englisch |
|---:|---|---|---|
| 1126 | `vendor_oplus_1` | App-Info → Akkuverwaltung der App (bzw. Akkunutzung): „Vordergrundaktivität zulassen“, „Hintergrundaktivität erlauben“ und „Automatischer Start“ einschalten | App info → App battery management (or Battery usage): turn on “Allow foreground activity”, “Allow background activity” and “Auto launch” |
| 1127 | `vendor_oplus_2` | In der Übersicht der zuletzt genutzten Apps LeoSync sperren (Karte lange drücken → Sperren) | Lock LeoSync in the recent apps overview (long-press the card → Lock) |
| 1128 | `vendor_samsung_1` | App-Info → Akku: „Nicht eingeschränkt“ wählen | App info → Battery: choose “Unrestricted” |
| 1129 | `vendor_samsung_2` | Einstellungen → Akku → Hintergrundnutzungslimits: LeoSync zu „Nie ruhende Apps“ hinzufügen | Settings → Battery → Background usage limits: add LeoSync to “Never sleeping apps” |
| 1130 | `vendor_xiaomi_1` | App-Info → „Autostart“ einschalten | App info → turn on “Autostart” |
| 1131 | `vendor_xiaomi_2` | App-Info → Energiesparmodus: „Keine Einschränkungen“ wählen | App info → Battery saver: choose “No restrictions” |
| 1132 | `vendor_xiaomi_3` | In der Übersicht der zuletzt genutzten Apps LeoSync sperren | Lock LeoSync in the recent apps overview |
| 1133 | `vendor_huawei_1` | Einstellungen → Akku → App-Start: LeoSync auf „Manuell verwalten“ stellen und alle drei Schalter einschalten | Settings → Battery → App launch: set LeoSync to “Manage manually” and turn on all three switches |

## Schutz, Import-Checkliste, Umzug, Akku-Tipp

| Nr. | Schlüssel | Deutsch | Englisch |
|---:|---|---|---|
| 1134 | `prot_ok` | OK | OK |
| 1135 | `prot_cancel` | Abbrechen | Cancel |
| 1136 | `prot_understood` | Verstanden | Got it |
| 1137 | `prot_state_decision` | Benutzerentscheidung erforderlich | User decision required |
| 1138 | `prot_more_cards (one)` | %1$d weitere offene Entscheidung | %1$d more decision pending |
| 1139 | `prot_more_cards (other)` | %1$d weitere offene Entscheidungen | %1$d more decisions pending |
| 1140 | `prot_enc_title` | Verschlüsselung nicht verfügbar | Encryption not available |
| 1141 | `prot_enc_text` | Für „%1$s“ ist Verschlüsselung vorgeschrieben, aber der PC oder das NAS bietet sie gerade nicht an. Es wird nichts übertragen. Mögliche Ursachen: Die Einstellung am NAS oder PC wurde geändert, oder jemand stört die Verbindung im Netz. | Encryption is required for “%1$s”, but the PC or NAS does not offer it right now. Nothing is transferred. Possible causes: the setting on the NAS or PC was changed, or someone is interfering with the connection on the network. |
| 1142 | `prot_enc_test` | Verbindung testen | Test connection |
| 1143 | `prot_enc_lower` | Auf ‹Wenn möglich› stellen | Set to ‹If possible› |
| 1144 | `prot_enc_more` | Mehr zur Verschlüsselung | More about encryption |
| 1145 | `prot_enc_lower_title` | Auf ‹Wenn möglich› stellen? | Set to ‹If possible›? |
| 1146 | `prot_enc_lower_text` | Das Profil überträgt danach auch ohne Verschlüsselung. Dafür gibt es keine automatische Umstellung mehr. | Afterwards the profile also transfers without encryption. There is no automatic switch to ‹Required› any more. |
| 1147 | `prot_enc_lower_ok` | Schutz verringern | Reduce protection |
| 1148 | `prot_enc_lowered` | Verschlüsselung auf ‹Wenn möglich› gestellt | Encryption set to ‹If possible› |
| 1149 | `prot_enc_card_line` | Verschlüsselung abgelehnt | Encryption refused |
| 1150 | `prot_enc_profile_banner` | Verschlüsselung nicht verfügbar: Der PC oder das NAS bietet sie gerade nicht an. Dieses Profil ist angehalten, es wird nichts übertragen. | Encryption not available: the PC or NAS does not offer it right now. This profile is paused and nothing is transferred. |
| 1151 | `prot_auto_info` | Verschlüsselung automatisch auf ‹Erforderlich› gestellt, weil dein PC verschlüsselt. Das schützt vor einer unbemerkten Herabstufung. | Encryption automatically set to ‹Required› because your PC encrypts. This protects against a silent downgrade. |
| 1152 | `prot_less_title` | Weniger Schutz? | Less protection? |
| 1153 | `prot_less_text` | Die Übertragung dieses Profils ist danach nicht mehr sicher vor Mitlesen. Für dieses Profil stellt LeoSync nie mehr automatisch auf ‹Erforderlich› um. | Afterwards, transfers of this profile are no longer safe from eavesdropping. LeoSync will never again switch this profile to ‹Required› automatically. |
| 1154 | `prot_less_ok` | Schutz verringern | Reduce protection |
| 1155 | `prot_enc_auto_note` | Bei ‹Wenn möglich› und ‹Aus› stellt LeoSync nie automatisch um. | With ‹If possible› and ‹Off›, LeoSync never switches automatically. |
| 1156 | `prot_vol_no` | Nicht synchronisieren | Do not sync |
| 1157 | `prot_vol_stay` | Das Profil bleibt angehalten, bis du weiter wählst. | The profile stays paused until you choose to continue. |
| 1158 | `prot_vol_confirm_title` | Mit diesem Laufwerk weiter? | Continue with this drive? |
| 1159 | `prot_vol_confirm_text` | LeoSync merkt sich das neue Laufwerk und prüft danach den PC-Ordner per Stichprobe. Fahre nur fort, wenn du den Wechsel selbst veranlasst hast. | LeoSync remembers the new drive and then checks the PC folder with a spot check. Only continue if you caused the change yourself. |
| 1160 | `prot_vol_confirm_ok` | Mit diesem Laufwerk fortfahren | Continue with this drive |
| 1161 | `prot_gphotos_tip` | Tipp: Gib Speicherplatz auf dem Handy erst nach einer Synchronisierung frei, am sichersten mit „Speicher freigeben“ in LeoSync. Es löscht nur Fotos, die nachweislich auf dem PC liegen. Google Fotos kann Originale löschen, bevor sie gesichert sind. | Tip: Free up phone storage only after a sync, most safely with “Free up space” in LeoSync. It only deletes photos that are verified to be on the PC. Google Photos can delete originals before they are backed up. |
| 1162 | `chk_title_one` | Nach dem Import: noch ein Schritt | After the import: one step left |
| 1163 | `chk_title_many` | Nach dem Import: noch %1$d Schritte | After the import: %1$d steps left |
| 1164 | `chk_text` | Deine Profile warten auf den ersten Start. Erledige die Schritte, dann läuft alles wie gewohnt. | Your profiles are waiting for their first run. Finish the steps and everything works as before. |
| 1165 | `chk_row` | %1$s · %2$s | %1$s · %2$s |
| 1166 | `chk_step_folder` | Ordner neu verknüpfen | Reconnect folder |
| 1167 | `chk_step_test` | Verbindung testen | Test connection |
| 1168 | `chk_step_sync` | Ersten Abgleich starten | Start first sync |
| 1169 | `chk_btn_folder` | Ordner wählen | Choose folder |
| 1170 | `chk_btn_test` | Verbindung testen | Test connection |
| 1171 | `chk_btn_sync` | Jetzt starten | Start now |
| 1172 | `chk_collapse` | Einklappen | Collapse |
| 1173 | `chk_expand` | Aufklappen | Expand |
| 1174 | `trash_note_migration` | Dein Papierkorb zieht auf den neuen Ordner „@LeoSync-Bin“ um, sobald offene Wiederherstellungen erledigt sind. Bis dahin liegen Dateien noch im alten Ordner „.LeoSync-Bin“. Es geht nichts verloren. | Your trash is moving to the new folder “@LeoSync-Bin” as soon as pending restores are done. Until then, files stay in the old folder “.LeoSync-Bin”. Nothing is lost. |
| 1175 | `trash_note_exhausted` | Eine Wiederherstellung ist mehrfach fehlgeschlagen und hält den Umzug auf. Versuche sie erneut oder verwirf sie. | A restore failed several times and is holding up the move. Try it again or discard it. |
| 1176 | `trash_note_pc_fallback` | Dein PC oder NAS lehnt den Ordnernamen „@LeoSync-Bin“ ab. LeoSync verwendet deshalb dort weiterhin „.LeoSync-Bin“. Das ist normal, deine Dateien sind geschützt. | Your PC or NAS rejects the folder name “@LeoSync-Bin”. LeoSync therefore keeps using “.LeoSync-Bin” there. This is normal; your files are protected. |
| 1177 | `enc_more` | Mehr dazu | More |
| 1178 | `enc_more_steps` | Windows: PowerShell als Administrator öffnen. Eine Freigabe: Set-SmbShare -Name <Freigabename> -EncryptData $true. Alle Freigaben: Set-SmbServerConfiguration -EncryptData $true. Synology und QNAP: in den SMB-Einstellungen SMB 3 und die Verschlüsselung (Transportverschlüsselung) aktivieren. Ältere Geräte oder Linux-Samba ohne SMB 3 können das nicht; dann „Wenn möglich“ wählen. | Windows: open PowerShell as administrator. One share: Set-SmbShare -Name <ShareName> -EncryptData $true. All shares: Set-SmbServerConfiguration -EncryptData $true. Synology and QNAP: turn on SMB 3 and encryption (transport encryption) in the SMB settings. Older devices or Linux Samba without SMB 3 cannot do this; then choose “If possible”. |
| 1179 | `setup_notif_help` | Sonst erfährst du nicht, wenn eine Synchronisierung fertig ist oder deine Entscheidung braucht. | Otherwise you will not know when a sync is done or needs your decision. |
| 1180 | `setup_media_help` | Nötig, damit LeoSync deine Fotos sichern kann. | Needed so LeoSync can back up your photos. |
| 1181 | `setup_battery_help` | Damit LeoSync im Hintergrund zuverlässig läuft. | So LeoSync runs reliably in the background. |
| 1182 | `battery_tip_title` | Zuverlässige Hintergrund-Synchronisierung | Reliable background sync |
| 1183 | `battery_tip_text` | Ohne die Akku-Ausnahme kann Android automatische Synchronisierungen verzögern oder auslassen. | Without the battery exception, Android can delay or skip automatic syncs. |
| 1184 | `battery_tip_reason` | Eine Synchronisierung musste ohne Vordergrund laufen. | A sync had to run without a foreground notification. |
| 1185 | `battery_tip_later` | Nicht jetzt | Not now |
| 1186 | `perm_battery_skipped` | (Zurückgestellt) | (Skipped) |
| 1187 | `setup_battery_btn` | App-Einstellungen öffnen | Open app settings |
| 1188 | `setup_battery_how` | So geht es | How to |
| 1189 | `setup_battery_steps` | 1. „App-Einstellungen öffnen“ tippen (App-Info von LeoSync). 2. „Akku“ bzw. „Akkunutzung“ oder „Akkuverwaltung der App“ wählen. 3. „Uneingeschränkt“ wählen (je nach Handy „Hintergrundaktivität erlauben“ oder „Nicht optimieren“). 4. Zurück zu LeoSync. Die Bezeichnungen hängen vom Hersteller ab; manche Handys brauchen zusätzlich „Automatischer Start“. | 1. Tap “Open app settings” (App info of LeoSync). 2. Choose “Battery”, “Battery usage” or “App battery management”. 3. Choose “Unrestricted” (on some phones “Allow background activity” or “Not optimised”). 4. Return to LeoSync. The labels depend on the manufacturer; some phones also need “Auto launch”. |
| 1190 | `loc_title` | Aufnahmeort der Fotos lesen? | Read where your photos were taken? |
| 1191 | `loc_text` | Für Länderordner liest LeoSync den Aufnahmeort aus deinen Fotos (GPS-Daten im Foto), um den Ordner zu bestimmen. Der Ort wird nur auf deinem Handy ausgewertet; LeoSync speichert und sendet ihn nicht gesondert. Die Fotos selbst werden unverändert übertragen. | For country folders, LeoSync reads the capture location from your photos (GPS data in the photo) to choose the folder. The location is evaluated only on your phone; LeoSync does not store or send it separately. The photos themselves are transferred unchanged. |
| 1192 | `loc_continue` | Weiter | Continue |
| 1193 | `loc_not_now` | Nicht jetzt | Not now |
| 1194 | `loc_missing` | Aufnahmeort: Berechtigung fehlt | Capture location: permission missing |
| 1195 | `loc_grant` | Erteilen | Grant |
| 1196 | `move_title` | Von der alten LeoSync-Version umziehen? | Move from the old LeoSync version? |
| 1197 | `move_text` | Deine Profile ziehst du mit einer Sicherung um. Das geht in wenigen Schritten. | You move your profiles with a backup. It takes a few steps. |
| 1198 | `move_start` | Umzug starten | Start moving |
| 1199 | `move_fresh` | Neu einrichten | Set up fresh |
| 1200 | `move_steps_title` | Von der alten LeoSync-Version umziehen | Move from the old LeoSync version |
| 1201 | `move_step1` | 1. Pausiere in der alten App alle Profile. | 1. Pause all profiles in the old app. |
| 1202 | `move_step2` | 2. In der alten App: Einstellungen → Sicherung exportieren und die Datei speichern. Wähle dabei ein Export-Passwort: Dann werden die SMB-Passwörter verschlüsselt in der Datei gespeichert. Ohne Export-Passwort enthält die Datei keine Passwörter, du gibst sie nach dem Import neu ein. | 2. In the old app: Settings → export a backup and save the file. Choose an export password: then the SMB passwords are saved encrypted in the file. Without one, the file contains no passwords and you enter them again after the import. |
| 1203 | `move_step3` | 3. Hier: „Sicherung importieren“. | 3. Here: “Import backup”. |
| 1204 | `move_step4` | 4. Folge danach der Checkliste „Nach dem Import“ auf der Startseite. | 4. Then follow the “After the import” checklist on the home screen. |
| 1205 | `move_step5` | 5. Deinstalliere die alte App erst, wenn alles läuft. | 5. Uninstall the old app only when everything works. |
| 1206 | `move_warn` | Wichtig: Lass nicht beide Apps gleichzeitig dieselben Ordner synchronisieren. Pausiere in der alten App alle Profile, bevor du hier startest, und deinstalliere sie erst, wenn alles läuft. | Important: do not let both apps sync the same folders at the same time. Pause all profiles in the old app before you start here, and uninstall it only when everything works. |
| 1207 | `move_security` | Sicherheit: Die Sicherungsdatei enthält deine Profileinstellungen und Serveradressen. SMB-Passwörter sind nur enthalten, wenn du ein Export-Passwort wählst, und dann verschlüsselt. Bewahre die Datei sicher auf und lösche sie nach dem Import. | Security: the backup file contains your profile settings and server addresses. SMB passwords are only included if you choose an export password, and then they are encrypted. Keep the file safe and delete it after the import. |
| 1208 | `move_to_import` | Zur Sicherung (Einstellungen) | To backup (Settings) |
| 1209 | `settings_updates_play` | Updates kommen über Google Play. | Updates are delivered through Google Play. |
| 1210 | `err_friendly_plaintext` | Der PC hat unverschlüsselt geantwortet, obwohl die Verbindung verschlüsselt sein sollte. Die Verbindung wurde zur Sicherheit getrennt und es wurde nichts übertragen. Mögliche Ursachen: Jemand stört die Verbindung im Netz, oder der PC ist falsch eingestellt. | The PC replied unencrypted although the connection should be encrypted. The connection was closed for safety and nothing was transferred. Possible causes: someone is interfering with the connection on the network, or the PC is misconfigured. |
| 1211 | `err_friendly_signing` | Der PC signiert seine Antworten nicht mehr, früher tat er es. Die Verbindung wurde zur Sicherheit getrennt und es wurde nichts übertragen. Mögliche Ursachen: Die Einstellung am NAS oder PC wurde geändert, oder jemand stört die Verbindung im Netz. | The PC no longer signs its replies, although it used to. The connection was closed for safety and nothing was transferred. Possible causes: the setting on the NAS or PC was changed, or someone is interfering with the connection on the network. |
| 1212 | `sign_title` | Signierung nicht mehr verfügbar | Signing no longer available |
| 1213 | `sign_text` | Der Server hat die Verbindung von „%1$s“ früher signiert, jetzt nicht mehr. Ohne Signierung kann die Verbindung unbemerkt verändert werden. Mögliche Ursachen: Die Einstellung am NAS oder PC wurde geändert, oder jemand stört die Verbindung im Netz. Bis du entscheidest, bleibt dieses Profil angehalten. | The server used to sign the connection of “%1$s”, now it does not. Without signing, the connection can be altered unnoticed. Possible causes: the setting on the NAS or PC was changed, or someone is interfering with the connection on the network. Until you decide, this profile stays paused. |
| 1214 | `sign_allow` | Ohne Signierung erlauben | Allow without signing |
| 1215 | `sign_allow_title` | Ohne Signierung erlauben? | Allow without signing? |
| 1216 | `sign_allow_text` | Die Verbindung dieses Profils kann danach unbemerkt verändert werden. Wähle das nur, wenn du die Einstellung am NAS oder PC selbst geändert hast. Beim nächsten Lauf merkt sich LeoSync den neuen Stand. | Afterwards, the connection of this profile can be altered unnoticed. Only choose this if you changed the setting on the NAS or PC yourself. On the next run LeoSync remembers the new state. |
| 1217 | `sign_allow_ok` | Erlauben | Allow |
| 1218 | `conflict_res` | %1$d × %2$d px | %1$d × %2$d px |
| 1219 | `conflict_res_video` | %1$d × %2$d px · %3$s | %1$d × %2$d px · %3$s |
| 1220 | `conflict_bigger` | (grösser) | (larger) |
| 1221 | `conflict_res_hint` | Messenger verkleinern Fotos oft. Die Datei mit der höheren Auflösung hat meist die bessere Qualität. | Messengers often shrink photos. The file with the higher resolution usually has the better quality. |

## Meldungen je Ursache, Sammelkachel

| Nr. | Schlüssel | Deutsch | Englisch |
|---:|---|---|---|
| 1222 | `prob_title_guard` | Synchronisierung angehalten – Entscheidung | Sync stopped – decide |
| 1223 | `prob_title_plaintext` | Unverschlüsselte Antwort abgewiesen | Unencrypted reply rejected |
| 1224 | `prob_title_unsigned` | Unsignierte Antwort abgewiesen | Unsigned reply rejected |
| 1225 | `prob_title_pc_unrecognised` | PC nicht erkannt | PC not recognised |
| 1226 | `prob_title_enc_refused` | Verschlüsselung nicht verfügbar | Encryption not available |
| 1227 | `prob_title_sign_refused` | Signierung nicht mehr verfügbar | Signing no longer available |
| 1228 | `prob_title_login` | Anmeldung fehlgeschlagen | Sign-in failed |
| 1229 | `prob_title_access_denied` | Zugriff verweigert | Access denied |
| 1230 | `prob_title_credentials_unreadable` | Gespeicherte Passwörter nicht lesbar | Saved passwords cannot be read |
| 1231 | `prob_title_share_missing` | Freigabe nicht gefunden | Share not found |
| 1232 | `prob_title_access_unencrypted` | Freigabe verlangt Verschlüsselung | Share requires encryption |
| 1233 | `prob_title_pc_full` | PC-Speicher voll | PC storage full |
| 1234 | `prob_title_phone_full` | Handy-Speicher voll | Phone storage full |
| 1235 | `prob_title_guest` | Nur als Gast angemeldet | Signed in only as guest |
| 1236 | `prob_title_negotiation` | Verbindungsaufbau fehlgeschlagen | Connection setup failed |
| 1237 | `prob_title_pc_folder_missing` | Ordner auf dem PC fehlt | Folder on the PC missing |
| 1238 | `prob_title_phone_folder_lost` | Zugriff auf den Handy-Ordner verloren | Access to the phone folder lost |
| 1239 | `prob_title_restore_failed` | Wiederherstellung fehlgeschlagen | Restore failed |
| 1240 | `prob_title_unknown` | Synchronisierung fehlgeschlagen | Sync failed |
| 1241 | `prob_title_conflicts` | Konflikte warten | Conflicts are waiting |
| 1242 | `prob_title_month_folders` | Monatsordner entscheiden | Month folders to decide |
| 1243 | `prob_title_delete_confirm` | Löschungen bestätigen | Confirm deletions |
| 1244 | `prob_title_pc_unreachable` | PC nicht erreichbar | PC not reachable |
| 1245 | `prob_title_partial` | Einzelne Dateien fehlgeschlagen | Some files failed |
| 1246 | `prob_level_1` | Daten gefährdet – Entscheidung | Data at risk – decide |
| 1247 | `prob_level_2` | Sicherheit | Security |
| 1248 | `prob_level_3` | Braucht dein Eingreifen | Needs your action |
| 1249 | `prob_level_4` | Entscheidung zu Dateien | Decision about files |
| 1250 | `prob_level_5` | Vorübergehend | Temporary |
| 1251 | `prob_level_tip` | Tipp | Tip |
| 1252 | `msg_title_errors` | Fehler | Errors |
| 1253 | `msg_title_messages` | Meldungen | Messages |
| 1254 | `msg_counter` | %1$d von %2$d | %1$d of %2$d |
| 1255 | `msg_prev` | Vorherige Meldung | Previous message |
| 1256 | `msg_next` | Nächste Meldung | Next message |
| 1257 | `msg_region` | Meldungen, %1$d von %2$d | Messages, %1$d of %2$d |
| 1258 | `msg_new_important` | Eine wichtigere Meldung ist eingetroffen | A more important message arrived |
| 1259 | `msg_show` | Anzeigen | Show |
| 1260 | `msg_retry` | Erneut versuchen | Try again |
| 1261 | `msg_retrying` | Wird erneut versucht … | Trying again … |
| 1262 | `msg_enter_password` | Passwort neu eingeben | Enter password again |
| 1263 | `msg_open_profile` | Profil öffnen | Open profile |
| 1264 | `msg_open_profile_named` | Profil „%1$s“ öffnen | Open profile “%1$s” |
| 1265 | `msg_more_profiles` | + %1$d weitere | + %1$d more |
| 1266 | `msg_affects` | Betrifft: %1$s | Affects: %1$s |
| 1267 | `msg_decide` | Entscheiden | Decide |
| 1268 | `msg_lower_named` | Schutz verringern: %1$s | Reduce protection: %1$s |
| 1269 | `msg_pw_title` | Passwort neu eingeben | Enter password again |
| 1270 | `msg_pw_text` | Ein Passwort für alle Profile mit diesem Konto. LeoSync probiert zuerst ein Profil, damit das Konto nicht durch wiederholte Fehlversuche gesperrt wird. | One password for all profiles that use this account. LeoSync tries one profile first so the account is not locked by repeated failures. |
| 1271 | `msg_pw_ok` | Speichern und testen | Save and test |
| 1272 | `msg_pw_failed` | Anmeldung fehlgeschlagen. Das Passwort wurde für die anderen Profile nicht gespeichert. | Sign-in failed. The password was not saved for the other profiles. |
| 1273 | `msg_pw_done` | Passwort gespeichert. Wird erneut versucht … | Password saved. Trying again … |
| 1274 | `msg_unreachable_text` | Der PC war seit einem Tag oder länger nicht erreichbar. | The PC could not be reached for a day or more. |
| 1275 | `msg_status_count (one)` | %1$d Meldung | %1$d message |
| 1276 | `msg_status_count (other)` | %1$d Meldungen | %1$d messages |
| 1277 | `home_headline_waiting` | Wartet auf dich | Waiting for you |
| 1278 | `msg_label_guard` | Entscheidung nötig · Synchronisierung angehalten | Decision needed · sync stopped |
| 1279 | `msg_setup_count (one)` | %1$d Einrichtungsproblem | %1$d setup issue |
| 1280 | `msg_setup_count (other)` | %1$d Einrichtungsprobleme | %1$d setup issues |
| 1281 | `msg_tip_count (one)` | %1$d Tipp | %1$d tip |
| 1282 | `msg_tip_count (other)` | %1$d Tipps | %1$d tips |
| 1283 | `chk_waiting_group (one)` | %1$d Profil wartet auf die erste Synchronisierung. | %1$d profile is waiting for its first sync. |
| 1284 | `chk_waiting_group (other)` | %1$d Profile warten auf die erste Synchronisierung. | %1$d profiles are waiting for their first sync. |
| 1285 | `home_headline_errors` | Fehler | Errors |
| 1286 | `home_headline_messages` | Meldungen | Messages |

