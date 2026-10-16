# Run from any directory after Maven has downloaded the project's existing dependencies.
$ErrorActionPreference = 'Stop'
$projectDirectory = Split-Path $PSScriptRoot -Parent
$repository = Join-Path $env:USERPROFILE '.m2\repository'
$javaExecutable = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin\java.exe' } else { 'java' }
$javafxModules = @('base', 'graphics', 'controls') | ForEach-Object {
    Join-Path $repository "org\openjfx\javafx-$_\25\javafx-$_-25-win.jar"
}
$iconLibraries = @('core', 'javafx', 'feather-pack') | ForEach-Object {
    Join-Path $repository "org\kordamp\ikonli\ikonli-$_\12.4.0\ikonli-$_-12.4.0.jar"
}
foreach ($dependency in @($javafxModules) + @($iconLibraries)) {
    if (-not (Test-Path -LiteralPath $dependency)) {
        throw "Dependency not found: $dependency. Run mvn test first."
    }
}
Push-Location $projectDirectory
try {
    & $javaExecutable --module-path ($javafxModules -join ';') --add-modules javafx.controls `
        --enable-native-access=javafx.graphics -classpath ($iconLibraries -join ';') `
        (Join-Path $PSScriptRoot 'ExportAppIcon.java')
    if ($LASTEXITCODE -ne 0) { throw 'Icon export failed.' }
} finally {
    Pop-Location
}
