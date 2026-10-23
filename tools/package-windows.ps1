param(
    [Parameter(Mandatory = $true)]
    [string]$JdkHome,
    [Parameter(Mandatory = $true)]
    [string]$WixHome,
    [string]$OutputDirectory
)

$ErrorActionPreference = 'Stop'
$projectDirectory = Split-Path $PSScriptRoot -Parent
$jdkDirectory = (Resolve-Path -LiteralPath $JdkHome).Path
$wixDirectory = (Resolve-Path -LiteralPath $WixHome).Path
foreach ($tool in @('java.exe', 'javac.exe', 'jlink.exe', 'jpackage.exe')) {
    if (-not (Test-Path -LiteralPath (Join-Path $jdkDirectory "bin\$tool"))) {
        throw "Missing JDK tool: $tool"
    }
}
foreach ($tool in @('candle.exe', 'light.exe')) {
    if (-not (Test-Path -LiteralPath (Join-Path $wixDirectory $tool))) {
        throw "Missing WiX tool: $tool"
    }
}

[xml]$pom = Get-Content (Join-Path $projectDirectory 'pom.xml') -Raw
$version = [string]$pom.project.version
$javafxVersion = [string]$pom.project.properties.'javafx.version'
if ($version -notmatch '^\d+\.\d+\.\d+$') { throw 'Packaging requires a numeric release version.' }
if (-not $OutputDirectory) { $OutputDirectory = Join-Path $projectDirectory "target\releases\v$version" }
$OutputDirectory = [System.IO.Path]::GetFullPath($OutputDirectory)
$installer = Join-Path $OutputDirectory "CustomClicker-$version.exe"
if (Test-Path -LiteralPath $installer) { throw "Installer already exists: $installer" }

# Each build uses a new staging directory; existing builds and user data are never removed.
$buildDirectory = Join-Path $projectDirectory ('target\installer-build\' + [guid]::NewGuid().ToString('N'))
$dependencies = Join-Path $buildDirectory 'dependencies'
$inputDirectory = Join-Path $buildDirectory 'input'
$modulesDirectory = Join-Path $buildDirectory 'javafx-modules'
$runtimeDirectory = Join-Path $buildDirectory 'runtime'
$imageDirectory = Join-Path $buildDirectory 'image'
New-Item -ItemType Directory -Path $inputDirectory, $modulesDirectory, $OutputDirectory -Force | Out-Null

$originalJavaHome = $env:JAVA_HOME
$originalPath = $env:PATH
Push-Location $projectDirectory
try {
    $env:JAVA_HOME = $jdkDirectory
    $env:PATH = (Join-Path $jdkDirectory 'bin') + ';' + $wixDirectory + ';' + $originalPath

    # Run all tests, build the application JAR, and stage runtime dependencies only.
    & mvn --batch-mode --no-transfer-progress package `
        org.apache.maven.plugins:maven-dependency-plugin:3.7.0:copy-dependencies `
        '-DincludeScope=runtime' "-DoutputDirectory=$dependencies"
    if ($LASTEXITCODE -ne 0) { throw 'Maven build failed.' }
    Copy-Item -LiteralPath (Join-Path $projectDirectory 'target\CustomClicker.jar') -Destination $inputDirectory
    foreach ($dependency in Get-ChildItem -LiteralPath $dependencies -Filter '*.jar') {
        if ($dependency.Name -like 'javafx-*-win.jar') {
            Copy-Item -LiteralPath $dependency.FullName -Destination $modulesDirectory
        } elseif ($dependency.Name -notlike 'javafx-*') {
            Copy-Item -LiteralPath $dependency.FullName -Destination $inputDirectory
        }
    }
    foreach ($module in @('base', 'graphics', 'controls', 'fxml')) {
        if (-not (Test-Path -LiteralPath (Join-Path $modulesDirectory "javafx-$module-$javafxVersion-win.jar"))) {
            throw "Missing Windows JavaFX module: $module"
        }
    }

    # Retain upstream attribution alongside the runtime's own module license files.
    $legalDirectory = Join-Path $inputDirectory 'legal'
    New-Item -ItemType Directory -Path $legalDirectory -Force | Out-Null
    Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'packaging-licenses') -Destination $legalDirectory -Recurse
    Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'THIRD-PARTY-NOTICES.txt') -Destination $legalDirectory
    if (Test-Path -LiteralPath (Join-Path $jdkDirectory 'NOTICE')) {
        Copy-Item -LiteralPath (Join-Path $jdkDirectory 'NOTICE') -Destination (Join-Path $legalDirectory 'Temurin-NOTICE.txt')
    }
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    foreach ($dependency in Get-ChildItem -LiteralPath $dependencies -Filter '*.jar') {
        $archive = [System.IO.Compression.ZipFile]::OpenRead($dependency.FullName)
        try {
            foreach ($entry in $archive.Entries | Where-Object FullName -Match '(^|/)(LICENSE|NOTICE|COPYING)(\.[^/]*)?$') {
                $destination = Join-Path $legalDirectory ($dependency.BaseName + '-' + $entry.Name)
                [System.IO.Compression.ZipFileExtensions]::ExtractToFile($entry, $destination, $true)
            }
        } finally { $archive.Dispose() }
    }

    # Link JavaFX into the runtime and retain support needed by AWT, Gson, and native hooks.
    $runtimeModules = 'javafx.controls,javafx.fxml,java.desktop,java.logging,java.prefs,java.net.http,jdk.unsupported,jdk.crypto.ec'
    & (Join-Path $jdkDirectory 'bin\jlink.exe') `
        --module-path ((Join-Path $jdkDirectory 'jmods') + ';' + $modulesDirectory) `
        --add-modules $runtimeModules --strip-debug --no-header-files --no-man-pages `
        --output $runtimeDirectory
    if ($LASTEXITCODE -ne 0) { throw 'Runtime creation failed.' }

    & (Join-Path $jdkDirectory 'bin\jpackage.exe') --type app-image `
        --name CustomClicker --app-version $version --vendor 'Travis Morin' `
        --description 'CustomClicker auto clicker for Windows' `
        --input $inputDirectory --main-jar CustomClicker.jar `
        --main-class com.travis.customclicker.Launcher --runtime-image $runtimeDirectory `
        --icon (Join-Path $projectDirectory 'src\main\resources\icons\customclicker.ico') `
        --java-options '--add-modules=javafx.controls,javafx.fxml,jdk.unsupported' `
        --java-options '--enable-native-access=javafx.graphics,ALL-UNNAMED' `
        --dest $imageDirectory
    if ($LASTEXITCODE -ne 0) { throw 'Application image creation failed.' }

    # Verify with the packaged runtime and disposable settings before producing the installer.
    $verificationDirectory = Join-Path $buildDirectory 'verification'
    New-Item -ItemType Directory -Path $verificationDirectory -Force | Out-Null
    $packagedImage = Join-Path $imageDirectory 'CustomClicker'
    & (Join-Path $jdkDirectory 'bin\javac.exe') --system (Join-Path $packagedImage 'runtime') `
        --add-modules javafx.controls,javafx.fxml -classpath (Join-Path $packagedImage 'app\*') `
        -d $verificationDirectory (Join-Path $PSScriptRoot 'VerifyPackage.java')
    if ($LASTEXITCODE -ne 0) { throw 'Packaged verification compilation failed.' }
    $previousAppData = $env:APPDATA
    try {
        $env:APPDATA = Join-Path $verificationDirectory 'test-settings'
        & (Join-Path $packagedImage 'runtime\bin\java.exe') `
            --add-modules javafx.controls,javafx.fxml,jdk.unsupported `
            --enable-native-access=javafx.graphics,ALL-UNNAMED `
            -classpath ($verificationDirectory + ';' + (Join-Path $packagedImage 'app\*')) `
            VerifyPackage (Join-Path $verificationDirectory 'packaged-preview.png') $version
        if ($LASTEXITCODE -ne 0) { throw 'Packaged runtime verification failed.' }
    } finally { $env:APPDATA = $previousAppData }

    # A stable upgrade UUID identifies future versions as updates to this same application.
    & (Join-Path $jdkDirectory 'bin\jpackage.exe') --type exe `
        --name CustomClicker --app-version $version --vendor 'Travis Morin' `
        --description 'CustomClicker auto clicker for Windows' `
        --app-image (Join-Path $imageDirectory 'CustomClicker') `
        --win-per-user-install --win-dir-chooser --win-shortcut --win-menu `
        --win-menu-group CustomClicker --install-dir CustomClicker `
        --win-upgrade-uuid 'b60640f6-c563-4d1c-8c77-d8e3bbb7850f' `
        --dest $OutputDirectory
    if ($LASTEXITCODE -ne 0) { throw 'Installer creation failed.' }

    $checksum = Get-FileHash -LiteralPath $installer -Algorithm SHA256
    ($checksum.Hash.ToLowerInvariant() + '  ' + [System.IO.Path]::GetFileName($installer)) |
        Set-Content -LiteralPath ($installer + '.sha256') -Encoding ascii
    Write-Output "Installer: $installer"
    Write-Output "Application image for testing: $(Join-Path $imageDirectory 'CustomClicker')"
    Write-Output "SHA256: $($checksum.Hash)"
} finally {
    $env:JAVA_HOME = $originalJavaHome
    $env:PATH = $originalPath
    Pop-Location
}
