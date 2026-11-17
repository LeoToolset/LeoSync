# Texte für die Google-Play-Konsole – LeoSync

**Stand:** 2026-11-15 · **Quelle:** Auftrag SYNC-20261114-348 (Architecture), Konzept SYNC-20261114-342, DEC-286, DEC-287 · **Entwurf:** Der Inhaber reicht die Texte in der Play-Konsole ein. Die Angaben stammen aus den Aufträgen und den Entscheidungen; sie sind nicht im Code oder im Manifest nachgeprüft. Sie gelten für den Stand nach der Umsetzung von SYNC-20261114-346 und SYNC-20261115-351 (Manifest ohne `REQUEST_INSTALL_PACKAGES`, ohne `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`, ohne Standortberechtigungen). Vor dem Einreichen gegen das gebaute Manifest prüfen.

Die Konsole verlangt die Texte meist auf Englisch; die deutsche Fassung dient dem Inhaber zur Kontrolle.

## 1. Vordergrunddienst `dataSync` (Foreground service permission)

**Erklärung (EN):**
LeoSync synchronizes photos, videos and folders between the phone and the user's own PC or NAS over SMB. A transfer can involve many files and files of several gigabytes and may take longer than the system allows for a short background task. It is started by the user (tapping "Sync") or by a schedule or trigger the user set up (for example new photos, or a configured check interval). While it runs, the app shows a foreground notification with a progress indicator so that the user can see what is happening and can pause or cancel it. The service stops as soon as the transfer is finished. The foreground service type `dataSync` is therefore needed so that a started transfer is not interrupted halfway, which could leave partial files. No data is sent to a developer server; the files go only to the SMB share the user configured.

**Erklärung (DE):**
LeoSync gleicht Fotos, Videos und Ordner zwischen Handy und dem eigenen PC oder NAS per SMB ab. Eine Übertragung kann viele und mehrere Gigabyte grosse Dateien umfassen und länger dauern, als das System für eine kurze Hintergrundaufgabe erlaubt. Sie wird vom Benutzer gestartet (Tippen auf «Synchronisieren») oder durch einen vom Benutzer eingerichteten Zeitplan bzw. Auslöser (z. B. neue Fotos oder ein eingestelltes Prüfintervall). Solange sie läuft, zeigt die App eine Vordergrund-Benachrichtigung mit Fortschrittsanzeige, damit der Benutzer sieht, was geschieht, und pausieren oder abbrechen kann. Der Dienst endet, sobald die Übertragung fertig ist. Der Typ `dataSync` ist nötig, damit eine begonnene Übertragung nicht mittendrin abbricht und unvollständige Dateien hinterlässt. Es werden keine Daten an einen Server des Entwicklers gesendet; die Dateien gehen nur an die vom Benutzer eingerichtete SMB-Freigabe.

**Für den Inhaber:** Die Konsole verlangt für den Vordergrunddienst ein kurzes Video (Auslöser, Benachrichtigung mit Fortschritt, Ende der Übertragung). Das Video nimmt der Inhaber auf; die Dokumentation erstellt es nicht. Zum 6-Stunden-Limit von `dataSync` ab Android 15 siehe DECISIONS.md Anhang A Punkt 8 (Entscheid B3 offen bis zur Messung in SYNC-20261114-335).

## 2. Foto- und Videozugriff (`READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`)

**Erklärung (EN):**
LeoSync is a backup and synchronization app for photos and videos. Its core function is to find new photos and videos on the phone automatically and copy them to the user's own PC or NAS, sorted into folders. For this, the app needs ongoing access to all photos and videos in the background, including new ones taken later. The system photo picker is not suitable because it only grants access to individually selected files, one time, and cannot detect new photos on its own, so an automatic backup would not work. The files are processed only on the device and transferred only to the SMB share the user configured; they are not sent to the developer or to any third party.

**Erklärung (DE):**
LeoSync ist eine Sicherungs- und Synchronisierungs-App für Fotos und Videos. Die Kernfunktion ist, neue Fotos und Videos automatisch auf dem Handy zu finden und sortiert auf den eigenen PC oder das NAS zu kopieren. Dafür braucht die App dauerhaften Zugriff auf alle Fotos und Videos im Hintergrund, auch auf später aufgenommene. Die Fotoauswahl des Systems ist ungeeignet: Sie gibt nur einzeln gewählte Dateien einmalig frei und erkennt neue Fotos nicht selbst, sodass keine automatische Sicherung möglich wäre. Die Dateien werden nur auf dem Gerät verarbeitet und nur an die vom Benutzer eingerichtete SMB-Freigabe übertragen; sie gehen weder an den Entwickler noch an Dritte.

**Hinweis zu «Standort in Fotos» (`ACCESS_MEDIA_LOCATION`):** Die App fragt diese Berechtigung erst, wenn der Benutzer für ein Fotos-Profil Länderordner wählt (DEC-289), und liest den Aufnahmeort (GPS im Foto) nur lokal, um das Land zu bestimmen. Er wird nicht gespeichert und nicht gesendet. Eine eigene Begründung ist laut Auftrag nicht vorgesehen; falls die Konsole sie verlangt, diesen Absatz verwenden.

## 3. Datensicherheit (Data safety)

Grundlage: Datenschutzerklärung `PRIVACY_GER.md` / `PRIVACY_EN.md` (Version 1.2).

| Frage der Konsole | Vorschlag | Begründung |
|---|---|---|
| Erhebt oder teilt die App Nutzerdaten? | **Nein** | Keine Server des Entwicklers, kein Cloud-Dienst, keine Analyse-, Werbe- oder Tracking-SDKs; Dateien gehen nur vom Handy an die vom Benutzer eingerichtete SMB-Freigabe. |
| Standort | **Nicht erhoben** | Keine Standortberechtigung (DEC-287); Fotos-GPS (Länderordner) wird nur lokal ausgewertet, nicht gespeichert, nicht übertragen. |
| Fotos und Videos, Dateien | **Nicht erhoben** | Verarbeitung lokal, Übertragung nur an das Ziel des Benutzers. |
| Zugangsdaten (SMB-Passwort) | **Nicht erhoben** | Lokal im Android-Keystore gespeichert, nie an den Entwickler übertragen. |
| Diagnose | **Nicht erhoben** | Diagnosebericht entsteht nur auf Aktion des Benutzers und wird nur per E-Mail an leotools@protonmail.com gesendet, wenn der Benutzer ihn selbst absendet («Problem melden»); vor der Ausgabe pseudonymisiert. |
| Verschlüsselung bei der Übertragung | Je nach Einstellung (SMB-3-Verschlüsselung, Stufe «Wenn möglich» ist Standard) | Die Konsole fragt nach Verschlüsselung in Transit; die Übertragung zum Ziel des Benutzers ist je nach Stufe verschlüsselt, nicht für Daten zum Entwickler (es gibt keine). Formulierung mit dem Inhaber abstimmen. |
| Möglichkeit, Daten löschen zu lassen | Nicht zutreffend | Der Entwickler speichert keine Nutzerdaten; lokale Daten löscht die Android-App-Verwaltung bzw. die Deinstallation. |

## 4. Standort

Keine Begründung nötig: Die App verwendet seit DEC-287 weder `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` noch `ACCESS_BACKGROUND_LOCATION`. Ein optionaler Hintergrund-Standort würde erst nach einem Konzept und einer Benutzerfreigabe wieder aufgenommen (dann eigene Begründung mit Video nötig); er ist hier nicht dokumentiert.

## 5. Datenschutzerklärung (Link)

Die Konsole verlangt eine öffentliche URL der Datenschutzerklärung. Die Dateien `PRIVACY_GER.md` und `PRIVACY_EN.md` sind dafür inhaltlich vorbereitet; offen sind die Angabe des Verantwortlichen (Abschnitt 1, Platzhalter `[Vor- und Nachname]` bzw. `[Full legal name]`, Sache des Inhabers, DEC-123) und die URL der Veröffentlichung: Die Produktseite liegt neu unter `https://leotoolset.github.io/leosync/` (DEC-293); die alte Adresse `apop85.github.io` leitet weiter. Wo genau die Datenschutzerklärung veröffentlicht wird, legt der Inhaber fest.

## 6. Weitere Angaben, die der Inhaber in der Konsole selbst klärt

- Akku-Optimierung: Die App fragt die Ausnahme nicht per direktem Dialog an, sondern öffnet die Akku-Einstellungen von Android (DEC-286); keine Berechtigungserklärung nötig.
- Selbst-Update: entfällt (DEC-286); `REQUEST_INSTALL_PACKAGES` ist nicht mehr deklariert.
- Geschlossener Test: 12 Tester über 14 Tage, Identitätsprüfung (DEC-109).
- Play App Signing mit neuem Upload-Schlüssel; der Schlüssel bleibt lokal (DEC-286, `build.md`).
