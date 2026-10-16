# v1.0.0 refactoring review

## Completed cleanup

- Organized controller imports, constants, and injected fields; removed the unused
  `maximizeButton` Java field while retaining the button and its FXML ID.
- Added short method comments across application, controller, model, and service code.
- Split service initialization and settings setup into focused controller methods,
  preserving initialization order and listener registration order.
- Consolidated repeated panel visibility, accent-button setup, and UI-scale selection.
- Renamed the private save method to `saveApplicationData` and clarified construction
  of the complete persisted state. Existing save triggers remain in place.
- Expanded model accessors for consistent formatting without renaming serialized
  fields, changing defaults, or changing enum values.
- Extracted tray-image drawing with guaranteed graphics disposal. Artwork, menu
  labels, mouse actions, and callback dispatch remain unchanged.
- Added a package-private persistence constructor for isolated file tests; the public
  constructor still selects the same application data directory and filename.
- Replaced the placeholder test with ten regression tests covering persistence,
  settings copying, and hotkey capture. Tests do not register global hooks or
  generate mouse or keyboard input.

## Findings requiring separate behavior decisions

These findings are recorded, not corrected in this cleanup pass.

| Area | Existing behavior and implication | Suggested follow-up |
| --- | --- | --- |
| Data recovery | A load exception falls back to defaults; initial profile selection then saves, potentially overwriting the unreadable file. | Preserve the original file before recovery and agree on the recovery message/flow. |
| Partial JSON | Null profile entries, missing profile settings, or null/unknown timing enums can fail during profile loading or card creation. | Define compatible recovery rules before normalizing saved data. |
| Input validation | Empty/invalid integer editor text becomes zero; validation only checks reversed random ranges. | Agree on whether invalid inputs should be rejected or clamped. |
| Random CPS | Sampling uses an upper bound of `maxCps + 1`, allowing rates above the configured maximum. | Define the intended continuous range, including equal minimum/maximum values. |
| Hotkey validation | The F1–F12 numeric range includes other keys: F10 is 68, F11 is 87, Num Lock is 69, and Scroll Lock is 70 in the installed library. | Use explicit function-key membership after approval. |
| Profile naming | New-profile numbering restarts at 1, so existing names can be duplicated after restart. | Generate an available name without changing existing names. |
| Thread coordination | Hotkey capture/assignment state is shared across JavaFX and native-hook threads without explicit synchronization. The click service also allows a stopped worker to overlap a direct restart before worker completion. | Review synchronization and lifecycle changes separately, with focused tests. |
| Startup process | `reg.exe` runs synchronously from a UI handler; its output is not drained and interruption is caught by the generic settings handler. | Review process cleanup, interruption handling, and UI responsiveness separately. |
| Error reporting | Several controller handlers catch all exceptions; engine failures reach the worker thread rather than a dedicated error callback. | Agree on failure reporting before narrowing catches or adding callbacks. |

## Validation and remaining interactive checks

- Baseline: `mvn test` passed the original single placeholder test.
- Updated code: `mvn test` compiled production and test sources; ten tests passed.
- `git diff --check` passed. FXML, CSS, Maven configuration, dependencies, and the
  application version were left unchanged.
- No interactive JavaFX smoke test was performed. Before release, check all timing
  modes, mouse buttons, single/double clicks, fixed repeat counts, position picking
  and Escape, profile actions and restart persistence, hotkey reassignment, accents,
  scale/snap/drag, always-on-top, tray close/open/exit, and packaged startup registration.
- Automated coverage does not establish native-hook delivery, real clicking timing,
  visual equivalence, system tray integration, or packaged startup behavior.
