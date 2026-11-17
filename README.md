# LeoSync

Teil der Leo-Tool-Familie.

Android-App, die Fotos und Videos vom Telefon automatisch auf eine Windows-Freigabe (SMB) überträgt und dort nach **Jahr, Monat und Reiseland** sortiert. Ausserdem synchronisiert sie beliebige Ordner in beide Richtungen.

```
\\fs01.local\Fotos\2026\06\IMG_20260614_101530.jpg
\\fs01.local\Fotos\2026\06 - Ägypten\IMG_20260620_083012.jpg
\\fs01.local\Fotos\2026\06 - Ägypten Hurghada\...   ← vom Benutzer umbenannt, wird weiter genutzt
```

## Funktionen

Es gibt zwei Profiltypen:

| | Fotos & Videos sichern | Ordner synchronisieren |
|---|---|---|
| Ablage auf dem PC | `Jahr\Monat - Land\` (Standard); pro Profil wählbar: `Jahr\[Land\]Monat`, `Jahr\Monat\[Land]` | 1:1 wie auf dem Handy |
| Richtung | Handy → PC | Handy → PC, PC → Handy oder beidseitig |
| Dateitypen | Fotos, Videos | alle |
| Löschen | optional nach geprüftem Upload | optional Löschungen übernehmen (Papierkorb) |

- **Automatisch im Heim-WLAN.** Sobald das Telefon mit einem WLAN verbunden ist, in dem die App deinen PC erkennt, startet der Sync. Neue Fotos werden direkt nachgeladen (nach einer Fotoserie läuft nur ein Nachhol-Lauf, sobald das neuste Foto das Mindestalter erreicht hat). Bei Ordner-Profilen prüft die App in einem global einstellbaren Intervall auf Änderungen (*Einstellungen → Übertragung*, Standard 60 Minuten). Ist der PC aus, prüft die App alle 30 Minuten erneut. Nach einem WLAN-Unterbruch setzt sie sofort beim Wiederverbinden fort.
- **Auch über mobile Daten bzw. VPN** (Schalter in *Einstellungen → Übertragung*, Standard aus). Ausserhalb des WLAN prüft die App bei nicht erreichbarem PC nur alle 2 Stunden erneut. Achtung: Datenverbrauch und mögliche Kosten beim Mobilfunkabo.
- **PC-Kennung.** Die App erkennt deinen PC nur an seiner Kennung (seit 0.19.0; keine WLAN-Namen und keine Standortberechtigung mehr). Die Kennung wird geprüft, **bevor** Benutzername und Passwort gesendet werden. Die Kennung ist nicht geheim: Sie schützt davor, dass die Zugangsdaten an einen fremden Server in einem fremden Netz gehen, nicht vor einem gezielten Angreifer. Nach einem Neuaufsetzen des PCs ändert sich die Kennung; dann das Profil öffnen und «Verbindung testen» ausführen. **Bekannte Netze:** Die App merkt sich je Profil die WLANs, in denen dein PC erkannt wurde; in einem unbekannten WLAN versucht sie den PC nur kurz zu erreichen (Akku). Im Profil siehst du die Anzahl und kannst sie vergessen.
- **Pause:** Das Symbol oben rechts pausiert die Automatik für eine wählbare Zeit oder „für immer (nur manuell)“. Auf der Startseite lässt sich auch nach unten ziehen, um eine Synchronisierung zu starten.
- **Sortierung nach Aufnahmedatum.** Die App liest EXIF bzw. Video-Metadaten, sonst das Datum im Dateinamen, sonst das Änderungsdatum.
- **Länderordner aus GPS, offline.** Es gibt keine Online-Abfrage. Fotos aus dem Heimatland (Standard: Schweiz) bekommen keinen Länderzusatz.
- **Eigene Ordnernamen.** Monatsordner dürfen umbenannt werden, solange die Monatszahl erhalten bleibt (`06 - Ägypten mit Familie`). Bei Mehrdeutigkeit fragt die App einmal pro Monat und Land, nicht pro Bild.
- **Keine Duplikate.** Die App gleicht mit einer lokalen Datenbank und dem Share ab (Grösse plus Teil-Prüfsumme). Ein gleicher Name mit anderem Inhalt wird zu `bild_2.jpg`.
- **Robust und schnell.** Abgebrochene Uploads werden fortgesetzt. Erst nach vollständigem Upload wird umbenannt, es entstehen keine halben Dateien. Bis zu 4 Übertragungen laufen parallel.
- **Original-Datum.** Die Dateien auf dem PC behalten das Änderungsdatum vom Telefon.
- **Statistik:** Anzahl und Speicher pro Jahr und Land.
- **Optional aufräumen.** Nach geprüftem Upload kann die App Dateien in den Papierkorb verschieben oder löschen. Standard ist behalten.
- **Akkuschonend.** Es gibt keinen Dauerdienst. Die App nutzt System-Callbacks und WorkManager und hat eine Option „Nur beim Laden“.
- **Fortschritt** erscheint in der Benachrichtigung und auf dem Sperrbildschirm.
- **Profile** für mehrere Ziele, eine **Simulation** (Trockenlauf) und einen **Verlauf** mit Log.
- **Updates** kommen über Google Play (seit 0.19.0 gibt es kein Selbst-Update mehr).
- **Ordner-Sync:** 3-Wege-Abgleich (Handy, PC, letzter Stand) mit Konflikterkennung. Ersetzte und gelöschte Dateien landen im Ordner `@LeoSync-Bin` auf Handy und PC (ältere Namen `.LeoSync-Bin`, `.LeoSync-Papierkorb` werden automatisch übernommen), nicht im Nirgendwo. Der Papierkorb leert sich nach einer pro Profil einstellbaren Frist (Standard 30 Tage, 0 = nie).
- **Papierkorb:** Gelöschte Dateien und überschriebene Versionen bleiben im Papierkorb. Du holst einzelne Dateien, ganze Ordner oder alles von einem Tag zurück – auf Handy und PC – oder löschst sie endgültig. Die Liste lässt sich nach Seite (Handy und PC / Nur Handy / Nur PC) und Tag filtern, Dateien auf dem Handy lassen sich vor dem Wiederherstellen mit «Öffnen» ansehen. Auf dem Handy legt die App im Papierkorb eine `.nomedia` an, damit die Galerie ihn nicht anzeigt. „Aufräumen“ leert ganz, nach Alter oder einzelne Tage, auf Handy, PC oder beiden. Ist der PC aus, wird das beim nächsten Sync nachgeholt.
- **Schutzbremse:** Fehlt ein Ordner, passt ein neu gewählter Ordner nicht zum bisherigen Stand oder würden mehr als 20 % der Dateien gelöscht, hält der Ordner-Sync an und fragt nach.
- **Sicherung:** Profile, Einstellungen und Sync-Status als Datei exportieren und importieren. SMB-Passwörter werden nur mit Export-Passwort verschlüsselt mitgesichert.
- **Erinnerung**, wenn seit 7 Tagen (einstellbar) nichts gesichert wurde, obwohl neue Fotos warten.
- **Speicher freigeben:** Alte Dateien, die nachweislich auf dem PC liegen, verschwinden vom Handy. Fotos und Videos kommen in den Papierkorb der Galerie. Bei Ordner-Profilen Handy → PC ohne „Löschungen übernehmen“ werden die Dateien direkt gelöscht; auf dem PC bleiben sie erhalten. Vorher wird jede Datei auf dem PC geprüft. Die Auswahl läuft nach Alter, Ordner, Dateityp, Grösse und Ausnahmen, mit Vorschau.
- **Fehlersuche:** Der Verlauf lässt sich über das Android-Teilen-Menü exportieren. *Einstellungen → Diagnose* kopiert Diagnosedaten mit einer zufälligen Geräte-ID in die Zwischenablage; Benutzernamen, IP-Adressen, Rechner-, Freigabe-, WLAN-, Profil- und Ordnernamen, Pfade und Dateinamen sind durch Platzhalter ersetzt, Passwörter sind nie enthalten (der Verlauf-Export ist davon ausgenommen). „Problem melden“ öffnet die Mail-App mit vorbereitetem Text und Diagnose; gesendet wird erst, wenn du selbst absendest. Für OnePlus/Oppo/Realme, Samsung, Xiaomi und Huawei/Honor zeigt die App, welche Hersteller-Einstellungen den Hintergrundbetrieb sichern.
- **Versionsschutz** (Ordner-Profile, Standard an): Die überschriebene Version der Gegenseite bleibt im Papierkorb.
- **Profil löschen** mit „Rückgängig“ (15 Sekunden). **Zugangsdaten übernehmen** aus anderen Profilen über einen Knopf.
- **Hilfe & Produktseite** im Zahnrad-Menü: <https://leotoolset.github.io/leosync/>.
- **Datenschutz:** Siehe [PRIVACY_GER.md](PRIVACY_GER.md) bzw. [PRIVACY_EN.md](PRIVACY_EN.md). Die App hat keine eigene Internetverbindung ausser der SMB-Synchronisierung; Updates laufen über Google Play.
- **Filter:** „Nur diese Dateien“ und „Diese Dateien ausschliessen“ mit Mustern wie `*.pdf` oder `Temp/**`.
- **Prüfung nach dem Hochladen** in Stufen von „Aus“ bis „Komplett“ (Fotos-Profile, Standard „Mittel“).
- **Deutsch und Englisch:** Ist das Handy auf Deutsch eingestellt, erscheint die App auf Deutsch, sonst auf Englisch. Unter *Einstellungen → Sprache & Sicherung → Sprache* lässt sich die Sprache fest wählen (ab Android 13 auch in den Android-Einstellungen der App). Länderordner auf dem PC heissen in der App-Sprache; bestehende Ordner der anderen Sprache werden weiterverwendet.

## Download

Status: **Beta (0.19.x)** auf dem Weg zu 1.0.0. LeoSync wird nur noch über Google Play verteilt (Paket `ch.leo.sync`). Die früheren APKs unter [Releases](https://github.com/Apop85/Sync2PC/releases) (`com.sync2pc.app`) sind Geschichte und werden nicht mehr gepflegt. Den Umzug der Profile von der alten App erklärt die Hilfe («Von der alten LeoSync-App umziehen»).

## Voraussetzungen

- Android 10 oder neuer
- Windows-PC mit freigegebenem Ordner (Windows-Ordnerfreigabe, SMB2/SMB3) und einem Benutzerkonto mit Schreibrecht

## Einrichtung

1. App aus Google Play installieren (Bauen: [build.md](build.md)).
2. Berechtigungen erteilen. Die Startseite fragt nur nach dem, was deine Profile brauchen:
   - **Fotos & Videos** (bei Fotos-Profilen)
   - **Standort in Fotos** wird erst bei der Wahl der Länderordner erfragt; eine Standortberechtigung braucht die App sonst nicht.
   - **Benachrichtigungen** und **Akku-Optimierung aus**
3. Profil anlegen: Freigabe (`\\server\freigabe`), Benutzer und Passwort und Quellordner. Die Quellordner wählst du über die Android-Dateiauswahl, z. B. `DCIM › Camera`.
4. Nach dem Speichern fragt die App, wie es weitergeht: **Simulieren** (zeigt ohne Übertragung, was passieren würde), **Synchronisieren** oder **Später**. Ein neues Profil startet erst automatisch, nachdem es einmal von Hand synchronisiert wurde. Die erste Synchronisierung am besten am Ladegerät ausführen.

## Projektstruktur

```
app/src/main/java/com/sync2pc/app/
  data/    Room-Datenbank, Profile, Einstellungen, Passwort-Verschlüsselung (Android Keystore)
  media/   MediaStore-Scan, EXIF-/Video-Metadaten, Datum aus Dateinamen
  geo/     Offline-Ländererkennung (Punkt-in-Polygon, Natural Earth)
  smb/     SMB-Verbindung (smbj), Upload mit Fortsetzung, Teil-Hash
  sync/    Sync-Engine, Zielordner-Logik, Worker, WLAN-Trigger, Benachrichtigungen, Löschen
  mirror/  Ordner-Spiegelung (Handy-Ordner via Android-Ordnerfreigabe, 3-Wege-Abgleich, Filter)
  (update/  Selbst-Update, mit 0.19.0 entfernt)
  diag/    Diagnosedaten (maskiert), Verlauf-Export, Absturzberichte, Hersteller-Hinweise
  backup/  Sicherung (Export/Import, Verschlüsselung mit Export-Passwort)
  ui/      Jetpack Compose Oberfläche
tools/     Build-Skripte (One-Click-Builder, Icons, Animation, Länderdaten), Übersetzungstabelle
```

## Bauen

Siehe [build.md](build.md). Kurz: `build.bat` doppelklicken.

## Technik

Kotlin · Jetpack Compose · WorkManager · Room · [smbj](https://github.com/hierynomus/smbj) · AndroidX ExifInterface · Natural-Earth-Länderdaten (Public Domain)

## Lizenz

[MIT](LICENSE). Länderdaten: [Natural Earth](https://www.naturalearthdata.com/) (Public Domain).
