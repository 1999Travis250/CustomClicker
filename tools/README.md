# Application icon

The icon uses the same white `fth-mouse-pointer` glyph and `.about-app-icon` styling
as the sidebar and About page, rendered in the default green accent (`#16e889`).
It is exported directly with the existing JavaFX and Ikonli dependencies.

Generated assets are in `src/main/resources/icons/`:

- `customclicker.ico`: Windows application/desktop shortcut icon, containing
  16, 20, 24, 32, 40, 48, 64, 128, and 256 pixel frames.
- `customclicker-<size>.png`: matching transparent PNGs for the JavaFX window and
  AWT system tray, rendered separately at each resolution.

The exported icons have a fixed green accent; changing the application's theme
does not replace desktop, window, or tray icons.

## Regenerate

Run `mvn test` first so the existing project dependencies are available locally.
With JDK 25 selected in `JAVA_HOME`, run from the project directory:

```powershell
.\tools\export-app-icon.ps1
```

The exporter reads the application's actual CSS and icon font. It does not launch
CustomClicker, register hotkeys, or read/write user settings. No new project
dependencies are required. If JavaFX or Ikonli versions change in `pom.xml`, update
the matching versions in `export-app-icon.ps1` before regenerating.

## Windows packaging

When constructing the Windows `jpackage` command, include:

```powershell
--icon "src/main/resources/icons/customclicker.ico"
```

Use `--win-shortcut` when building an installer that should create a desktop
shortcut. These are options to add to the complete packaging command, not a
standalone packaging command. Packaging itself is a separate release step.

The JavaFX window and tray already load the PNG assets. The executable and
desktop shortcut receive the ICO through packaging. A pinned taskbar shortcut
may retain Windows' cached old icon until it is unpinned and pinned again after
installing the packaged application.
