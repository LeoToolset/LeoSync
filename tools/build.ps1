# ============================================================================
#  LeoSync One-Click-Builder
#  - baut nur, wenn sich der Quellcode seit dem letzten Build geaendert hat
#  - erhoeht die Patch-Version automatisch, falls die aktuelle Version schon gebaut wurde
#  - Release (Play-only, SYNC-20261114-346) legt ab:
#      releases\LeoSync-<version>.aab        (bundleRelease, Upload fuer Google Play)
#      releases\LeoSync-<version>.apk        (assembleRelease, signiert)
#      releases\LeoSync-<version>-debug.apk  (assembleDebug, fuer QA)
#    Bestehende Dateien in releases\ werden nie ueberschrieben oder geloescht.
#
#  Aufruf:  build.bat                (normal)
#           build.bat -Force         (immer bauen)
#           build.bat -SkipTests     (Unit-Tests ueberspringen)
#           build.bat -Debug         (nur Debug-APK)
#           build.bat -NoPause       (am Ende nicht auf eine Taste warten; wertet build.bat aus)
# ============================================================================
param(
    [switch]$Force,
    [switch]$SkipTests,
    [switch]$Debug,
    [switch]$NoPause
)
$ErrorActionPreference = 'Stop'
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root
$Utf8 = New-Object System.Text.UTF8Encoding $false
$StateDir = Join-Path $Root ".build"
New-Item -ItemType Directory -Force $StateDir | Out-Null
New-Item -ItemType Directory -Force (Join-Path $Root "releases") | Out-Null

function Step($t) { Write-Host ""; Write-Host "==> $t" -ForegroundColor Cyan }
function Fail($t) { Write-Host ""; Write-Host "FEHLER: $t" -ForegroundColor Red; exit 1 }

# ---------------------------------------------------------------------------
Step "Pruefe Java (JDK 17 bis 25)"
# Seit 0.1.3: Gradle 9.8 / AGP 9.5 laufen auch mit Java 25 (Android Studio JBR).
# Neuere Java-Versionen -> portables JDK 21 verwenden.
$MinJava = 17
$MaxJava = 25
function Get-JavaMajor($javaExe) {
    if (-not (Test-Path $javaExe)) { return 0 }
    $out = cmd /c "`"$javaExe`" -version 2>&1"
    $m = [regex]::Match(($out -join " "), 'version "(\d+)(\.(\d+))?')
    if (-not $m.Success) { return 0 }
    $major = [int]$m.Groups[1].Value
    if ($major -eq 1) { $major = [int]$m.Groups[3].Value }
    return $major
}
$candidates = @()
if ($env:JAVA_HOME) { $candidates += $env:JAVA_HOME }
$candidates += "$env:ProgramFiles\Android\Android Studio\jbr"
$candidates += "$env:LOCALAPPDATA\Programs\Android Studio\jbr"
$candidates += (Get-ChildItem "$env:ProgramFiles\Java", "$env:ProgramFiles\Eclipse Adoptium", "$env:ProgramFiles\Microsoft" -Directory -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -match 'jdk' } | Sort-Object Name -Descending | ForEach-Object { $_.FullName })
$localJdkRoot = Join-Path $StateDir "jdk21"
$candidates += (Get-ChildItem $localJdkRoot -Directory -ErrorAction SilentlyContinue | ForEach-Object { $_.FullName })
$javaHome = $null
foreach ($c in $candidates) {
    if (-not $c) { continue }
    $major = Get-JavaMajor (Join-Path $c "bin\java.exe")
    if ($major -ge $MinJava -and $major -le $MaxJava) { $javaHome = $c; break }
    if ($major -gt 0) { Write-Host "Uebersprungen (Java $major): $c" }
}
if (-not $javaHome) {
    Write-Host "Kein passendes JDK gefunden -> lade portables JDK 21 (Eclipse Temurin, ca. 190 MB) nach .build\jdk21"
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    $zip = Join-Path $StateDir "jdk21.zip"
    $u = "https://api.adoptium.net/v3/binary/latest/21/ga/windows/x64/jdk/hotspot/normal/eclipse?project=jdk"
    $oldPref = $ProgressPreference; $ProgressPreference = 'SilentlyContinue'  # sonst sehr langsam in PS 5.1
    try { Invoke-WebRequest -Uri $u -OutFile $zip -UseBasicParsing } finally { $ProgressPreference = $oldPref }
    if (Test-Path $localJdkRoot) { Remove-Item -Recurse -Force $localJdkRoot }
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    [System.IO.Compression.ZipFile]::ExtractToDirectory($zip, $localJdkRoot)
    Remove-Item -Force $zip
    $javaHome = Get-ChildItem $localJdkRoot -Directory | Select-Object -First 1 | ForEach-Object { $_.FullName }
    if (-not $javaHome -or (Get-JavaMajor (Join-Path $javaHome "bin\java.exe")) -lt $MinJava) {
        Fail "Portables JDK konnte nicht eingerichtet werden. Bitte JDK 21 manuell installieren und JAVA_HOME setzen. Details: build.md"
    }
}
$env:JAVA_HOME = $javaHome
Write-Host "JAVA_HOME = $javaHome"

# ---------------------------------------------------------------------------
Step "Pruefe Android SDK"
$sdk = $null
foreach ($c in @($env:ANDROID_HOME, $env:ANDROID_SDK_ROOT, "$env:LOCALAPPDATA\Android\Sdk")) {
    if ($c -and (Test-Path (Join-Path $c "platforms"))) { $sdk = $c; break }
}
if (-not $sdk) { Fail "Android SDK nicht gefunden. Android Studio installieren und einmal starten (SDK wird eingerichtet). Details: build.md" }
$localProps = Join-Path $Root "local.properties"
$sdkEscaped = $sdk.Replace('\', '\\').Replace(':', '\:')
[IO.File]::WriteAllText($localProps, "sdk.dir=$sdkEscaped`n", $Utf8)
Write-Host "SDK = $sdk"

# ---------------------------------------------------------------------------
Step "Pruefe Gradle Wrapper"
$wrapperJar = Join-Path $Root "gradle\wrapper\gradle-wrapper.jar"
if (-not (Test-Path $wrapperJar)) {
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    $u = "https://raw.githubusercontent.com/gradle/gradle/v9.8.2/gradle/wrapper/gradle-wrapper.jar"
    Write-Host "Lade gradle-wrapper.jar ($u)"
    Invoke-WebRequest -Uri $u -OutFile $wrapperJar -UseBasicParsing
    Write-Host ("SHA-256: " + (Get-FileHash $wrapperJar -Algorithm SHA256).Hash)
} else { Write-Host "vorhanden" }

# ---------------------------------------------------------------------------
Step "Pruefe Laenderdaten und Icons"
& powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot "fetch-geodata.ps1") -Root $Root
if ($LASTEXITCODE -ne 0) { Write-Warning "Ohne Laenderdaten werden keine Laenderordner angelegt." }

$iconSrc = Join-Path $Root ".claude\icon.png"
$iconHashFile = Join-Path $StateDir "icon.hash"
if (Test-Path $iconSrc) {
    $ih = (Get-FileHash $iconSrc -Algorithm SHA256).Hash
    $old = if (Test-Path $iconHashFile) { (Get-Content $iconHashFile -Raw).Trim() } else { "" }
    $fg = Join-Path $Root "app\src\main\res\mipmap-xxxhdpi\ic_launcher_foreground.png"
    if ($ih -ne $old -or -not (Test-Path $fg)) {
        & powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot "make-icons.ps1") -Root $Root
        [IO.File]::WriteAllText($iconHashFile, $ih, $Utf8)
    } else { Write-Host "Icons aktuell" }
}

# Animation: Die Laufbilder (anim_dog_*.png, notif_dog_flipper.xml) sind feste, von Hand veredelte Quell-Assets
# (eingefroren seit SYNC-20261109-066) und werden vom Bau NIE neu erzeugt. Einmal-Export: tools\export-anim-frames.py.

# Zustandsbilder (wartet / alles aktuell / pausiert / wartet auf dich)
$stateSrcs = @((Join-Path $Root ".claude\wait_cleaned.png"), (Join-Path $Root ".claude\idle_cleaned.png"), (Join-Path $Root ".claude\pause.png"), (Join-Path $Root ".claude\attention.png")) | Where-Object { Test-Path $_ }
if ($stateSrcs.Count -gt 0) {
    $sh = ($stateSrcs | ForEach-Object { (Get-FileHash $_ -Algorithm SHA256).Hash }) -join ""
    $stateHashFile = Join-Path $StateDir "state-images.hash"
    $oldS = if (Test-Path $stateHashFile) { (Get-Content $stateHashFile -Raw).Trim() } else { "" }
    $waitOut = Join-Path $Root "app\src\main\res\drawable-nodpi\dog_wait.png"
    $pauseOut = Join-Path $Root "app\src\main\res\drawable-nodpi\dog_pause.png"
    $attentionOut = Join-Path $Root "app\src\main\res\drawable-nodpi\dog_attention.png"
    if ($sh -ne $oldS -or -not (Test-Path $waitOut) -or -not (Test-Path $pauseOut) -or -not (Test-Path $attentionOut)) {
        & powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot "make-state-images.ps1") -Root $Root
        [IO.File]::WriteAllText($stateHashFile, $sh, $Utf8)
    } else { Write-Host "Zustandsbilder aktuell" }
}

# ---------------------------------------------------------------------------
Step "Pruefe auf Quellcode-Aenderungen"
$inputs = @("app\src", "app\build.gradle.kts", "app\proguard-rules.pro", "build.gradle.kts",
            "settings.gradle.kts", "gradle.properties", "gradle\wrapper\gradle-wrapper.properties")
$files = foreach ($i in $inputs) {
    $p = Join-Path $Root $i
    if (Test-Path $p -PathType Container) { Get-ChildItem $p -Recurse -File } elseif (Test-Path $p) { Get-Item $p }
}
$sb = New-Object System.Text.StringBuilder
foreach ($f in ($files | Sort-Object FullName)) {
    $rel = $f.FullName.Substring($Root.Length + 1)
    [void]$sb.Append($rel).Append('|').Append((Get-FileHash $f.FullName -Algorithm SHA256).Hash).Append("`n")
}
$sha = [System.Security.Cryptography.SHA256]::Create()
$srcHash = [BitConverter]::ToString($sha.ComputeHash($Utf8.GetBytes($sb.ToString()))).Replace("-", "")
$hashFile = Join-Path $StateDir "last-build.hash"
$lastHash = if (Test-Path $hashFile) { (Get-Content $hashFile -Raw).Trim() } else { "" }

$verFile = Join-Path $Root "version.properties"
function Read-Version {
    $t = [IO.File]::ReadAllText($verFile, $Utf8)
    $g = { param($k) [int]([regex]::Match($t, "(?m)^$k=(\d+)").Groups[1].Value) }
    return @((& $g "VERSION_MAJOR"), (& $g "VERSION_MINOR"), (& $g "VERSION_PATCH"))
}
$v = Read-Version
$version = "$($v[0]).$($v[1]).$($v[2])"
$suffix = if ($Debug) { "-debug" } else { "" }
$apkOut = Join-Path $Root "releases\LeoSync-$version$suffix.apk"
# Alle Ausgabedateien des Laufs: Existiert eine davon schon, gilt die Version als gebaut (nie ueberschreiben)
function Get-OutputPaths {
    param([string]$Ver)
    if ($Debug) { return @((Join-Path $Root "releases\LeoSync-$Ver-debug.apk")) }
    return @((Join-Path $Root "releases\LeoSync-$Ver.aab"), (Join-Path $Root "releases\LeoSync-$Ver.apk"), (Join-Path $Root "releases\LeoSync-$Ver-debug.apk"))
}
$outPaths = Get-OutputPaths -Ver $version
$alreadyBuilt = (@($outPaths | Where-Object { Test-Path $_ })).Count -gt 0

if (-not $Force -and $srcHash -eq $lastHash -and $alreadyBuilt) {
    Write-Host "Keine Aenderungen seit dem letzten Build ($version). Nichts zu tun." -ForegroundColor Green
    Write-Host "Erzwingen mit: build.bat -Force"
    exit 0
}

# ---------------------------------------------------------------------------
if ($alreadyBuilt) {
    Step "Version $version wurde bereits gebaut -> Patch-Version erhoehen"
    $v[2] = $v[2] + 1
    $version = "$($v[0]).$($v[1]).$($v[2])"
    $t = [IO.File]::ReadAllText($verFile, $Utf8)
    $t = [regex]::Replace($t, "(?m)^VERSION_PATCH=\d+", "VERSION_PATCH=$($v[2])")
    [IO.File]::WriteAllText($verFile, $t, $Utf8)
    $apkOut = Join-Path $Root "releases\LeoSync-$version$suffix.apk"
    $outPaths = Get-OutputPaths -Ver $version

    $pn =Join-Path $Root "PATCHNOTES.md"
    if (Test-Path $pn) {
        $notes = [IO.File]::ReadAllText($pn, $Utf8)
        if ($notes -notmatch [regex]::Escape("## $version")) {
            $date = Get-Date -Format "yyyy-MM-dd"
            $entry = "## $version ($date)`n`n- Automatischer Build: Quellcode geaendert, Patchnotes bitte ergaenzen.`n`n"
            $idx = $notes.IndexOf("## ")
            if ($idx -ge 0) { $notes = $notes.Insert($idx, $entry) } else { $notes += "`n$entry" }
            [IO.File]::WriteAllText($pn, $notes, $Utf8)
        }
    }
}
Write-Host "Version: $version"

# ---------------------------------------------------------------------------
if (-not $Debug) {
    Step "Pruefe Signatur-Schluessel"
    $ksProps = Join-Path $Root "keystore.properties"
    if (-not (Test-Path $ksProps)) {
        # Play App Signing (SYNC-20261114-346): Der Builder erzeugt oder ueberschreibt keinen Schluessel.
        # Den Upload-Schluessel legt der Inhaber an (keytool, siehe build.md) und tragt ihn in keystore.properties ein.
        Fail ("keystore.properties fehlt. Der Builder legt keinen Schluessel selbst an: Upload-Schluessel fuer Google Play vom Inhaber anlegen " +
              "lassen und in keystore.properties eintragen (storeFile, storePassword, keyAlias, keyPassword; Details: build.md). Nur Debug: build.bat -Debug")
    } else { Write-Host "vorhanden" }
}

# ---------------------------------------------------------------------------
$tasks = @()
if (-not $SkipTests) { $tasks += "testDebugUnitTest" }
if ($Debug) { $tasks += "assembleDebug" } else { $tasks += @("bundleRelease", "assembleRelease", "assembleDebug") }
Step ("Gradle: " + ($tasks -join " "))
# Ausgabe zusaetzlich in .build\last-build.log schreiben (zum Weitergeben an den Agent)
$logFile = Join-Path $StateDir "last-build.log"
$writer = New-Object System.IO.StreamWriter($logFile, $false, $Utf8)
$writer.WriteLine("LeoSync Build $version - " + (Get-Date -Format "yyyy-MM-dd HH:mm:ss") + " - Tasks: " + ($tasks -join " "))
$prevEap = $ErrorActionPreference
$ErrorActionPreference = 'Continue'   # stderr von Gradle darf das Skript nicht abbrechen
try {
    & (Join-Path $Root "gradlew.bat") @tasks --console=plain 2>&1 | ForEach-Object {
        $line = "$_"
        Write-Host $line
        $writer.WriteLine($line)
    }
    $gradleExit = $LASTEXITCODE
} finally {
    $ErrorActionPreference = $prevEap
    $writer.Close()
}
if ($gradleExit -ne 0) { Fail "Gradle-Build fehlgeschlagen. Vollstaendiges Protokoll: $logFile" }

# Quelle -> Ziel; vorhandene Dateien in releases\ werden nie ueberschrieben
$copies = @()
if ($Debug) {
    $copies += ,@("app\build\outputs\apk\debug\app-debug.apk", (Join-Path $Root "releases\LeoSync-$version-debug.apk"))
} else {
    $copies += ,@("app\build\outputs\bundle\release\app-release.aab", (Join-Path $Root "releases\LeoSync-$version.aab"))
    $copies += ,@("app\build\outputs\apk\release\app-release.apk", (Join-Path $Root "releases\LeoSync-$version.apk"))
    $copies += ,@("app\build\outputs\apk\debug\app-debug.apk", (Join-Path $Root "releases\LeoSync-$version-debug.apk"))
}
foreach ($c in $copies) {
    if (-not (Test-Path (Join-Path $Root $c[0]))) { Fail "Build-Ausgabe nicht gefunden: $($c[0])" }
    if (Test-Path $c[1]) { Fail "Zieldatei existiert bereits und wird nie ueberschrieben: $($c[1])" }
}
foreach ($c in $copies) { Copy-Item (Join-Path $Root $c[0]) $c[1] }
[IO.File]::WriteAllText($hashFile, $srcHash, $Utf8)

Step "Fertig"
foreach ($c in $copies) {
    $mb = [math]::Round((Get-Item $c[1]).Length / 1MB, 1)
    Write-Host "$([IO.Path]::GetFileName($c[1])): $($c[1]) ($mb MB)" -ForegroundColor Green
}
if ($Debug) { Write-Host "Installation: adb install -r `"$($copies[0][1])`"" }
else { Write-Host "Google Play: releases\LeoSync-$version.aab in der Play Console hochladen (Upload-Schluessel, Play App Signing)." }
