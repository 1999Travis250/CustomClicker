# Windows release packaging

The release installer is built with JDK 25 `jlink`/`jpackage` and WiX 3.14.1.
It installs per user, includes a Java/JavaFX runtime, and creates desktop and
Start Menu shortcuts. Users do not need to install Java separately.

The installed application is `CustomClicker.exe`; the downloadable installer is
`CustomClicker-1.0.0.exe`. Do not distribute the launcher alone: it needs its
adjacent `app` and `runtime` directories.

## Build

Use a Windows x64 Eclipse Temurin JDK 25 and the WiX 3.14.1 portable build tools.
Both can be kept under ignored `target/build-tools/`, without system installation.
From the project directory:

```powershell
.\tools\package-windows.ps1 `
  -JdkHome '.\target\build-tools\temurin\jdk-25.0.4.1+1' `
  -WixHome '.\target\build-tools\wix'
```

The script runs unit tests, builds the JAR, links the runtime, generates an app
image, and runs `VerifyPackage` using that image's runtime and isolated settings.
The smoke test loads FXML, CSS, icons, the mouse engine, and the native hook;
it renders a preview without showing a window or generating clicks or keystrokes.
It then creates the installer and a SHA256 checksum under `target/releases/v1.0.0/`.
Existing installers are never overwritten; use `-OutputDirectory` for another build.

The script retains upstream license/attribution documents and runtime notices.
Its stable upgrade UUID must remain the same for future releases of this app.
Increment `pom.xml`'s version when making a new release. Build outputs and downloaded
tools are ignored by Git; source changes should be committed before tagging a release.

## Verify before publishing

Install the EXE and check launch, window/tray/shortcut icons, global hotkeys,
clicking, settings persistence, and uninstall. Verify on a clean Windows machine
without Java when available. The build-time smoke test does not test installation,
actual input delivery, or shortcuts. The installer has not been code-signed.

## GitHub download assets

Attach the installer EXE and its `.sha256` file to a GitHub Release for tag `v1.0.0`
once the matching source is committed and installation testing is complete.
Do not commit the binary installer to the source repository. Building locally does
not upload or publish a release. No README files are changed by the packaging script.
