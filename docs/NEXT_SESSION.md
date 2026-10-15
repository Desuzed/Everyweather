# Next session handoff

The local Android harness, CI workflow, debug signing separation, JVM regression suite, and initial instrumentation suite are present in the working tree. Existing dirty changes belong to the user and must be preserved. Do not commit `AGENTS.md` or the audit file; both are locally ignored and intentionally outside the tracked handoff.

Final source verification ran `./scripts/android.sh check :app:assembleDebugAndroidTest --max-workers=2` successfully in 26 seconds: 18 Python checks and 91 JVM tests passed with 0 failures, errors, or skips; lint has 0 errors, 133 warnings, and 1 hint. The Firebase event compatibility JVM test passed. The three install-cancel/complete-failure regressions were reproduced as failures and fixed, covering task failure plus listener `FAILED` and `CANCELED`. This was a pure logging refactor; UI flows, persistence, and SDK behavior were unchanged, and UI was not rerun this turn. Device report `app/build/outputs/device-check/<run>/report.json` recorded 8/47 tests passed with 0 assertion failures: all six Android atomic-store, Room, and app-context checks passed, while the first Compose launch timed out after 15 seconds because MIUI was keyguard-locked. No unlock response was available, so the UI suite and real-store scenarios remain pending. Feature code is complete; Firebase custom dimensions still require account-side configuration, and release publishing has not been performed.

Latest evidence (2026-10-07; historic 2026-09-11 results are retained in the labeled section below):

- The 2026-10-07 `passed_assisted` 41/41 run remains a historical UI baseline at `app/build/outputs/device-check/<run>/report.json`; it does not replace or invalidate the current 2026-10-08 8/47 environment-limited run above, whose remaining UI coverage still needs an unlocked-device retest.
- The immediately preceding 40-test attempt reported 37 passes and 3 fixture failures; those were corrected by using the actual save-button semantics, isolating the DataStore test in a unique target-app cache directory, and explicitly showing the sheet fixture.
- Final prior `./scripts/android.sh check --max-workers=2` exited 0: all 18 Python device-check tests passed, and Gradle assemble, JVM tests, and lint completed successfully with cached/up-to-date JVM results (50 tests reported). Lint reported 0 errors, 133 warnings, and 1 hint.
- Earlier assisted evidence remains historical: the 10/10 run is at `app/build/outputs/device-check/<run>/report.json`, and the preceding 90-second keyguard-locked installation timeout (0 tests) is at `app/build/outputs/device-check/<run>/report.json`.
- The run was assisted because MIUI returned `MIUILOG Permission Denied Activity` for each test Activity launch. Starting `com.desuzed.everyweather.debug/androidx.activity.ComponentActivity` with the matching MAIN/LAUNCHER intent when the denial appeared allowed the ActivityScenario intent to match. No permanent system setting changed and no production app was launched. This is assisted evidence, not a green `connectedDebugAndroidTest` or unattended CI run. Log: `app/build/outputs/androidTest-results/manual/mi9t-20261007.log`.
- The stale sample assertion was fixed separately to use `BuildConfig.APPLICATION_ID`; it is no longer a pending failure. The earlier 9/10 assisted logs remain historical evidence only.
- An earlier device-check run timed out at the 120-second instrumentation limit after 2 of 10 tests (`ok=false`), report `app/build/outputs/device-check/<run>/report.json`. Instrumentation, MIUI, app logcat, window, and CLI screenshot artifacts were saved; CLI layout helper installation was rejected by MIUI, while the adb XML fallback succeeded. The timeout led to a real-log regression test and a fix for an overly strict MIUI component-space matcher. The 18 Python device-check tests then passed.
- Earlier setup attempts had `INSTALL_FAILED_UPDATE_INCOMPATIBLE` and `INSTALL_FAILED_USER_RESTRICTED`; they are superseded by the successful device-check. Preserve release app/data. No overnight schedules were created.

Historic evidence (2026-09-11):

- `./scripts/android.sh check --max-workers=2`: `BUILD SUCCESSFUL` in 41 seconds; assemble, unit tests, and lint completed after the atomic query-acknowledgement change. The targeted repository regression and full JVM suite passed.
- `/tmp/everyweather-ui.log`: lint analysis completed, but the 2026-09-11 instrumentation run ran 0 tests. Installation stopped at `INSTALL_FAILED_UPDATE_INCOMPATIBLE` because the existing `com.desuzed.everyweather.debug` package was signed with a different certificate.
- This historical device failure was not to be resolved by wiping a user device or touching release data. Current instrumentation target and next action are the authorized physical Mi 9T described above.

Implemented in this working tree:

- Local AGENTS.md plus Luna routine / Terra refactor profiles; Android CLI wrapper and development instructions.
- GitHub Actions checks/UI matrix and weekly dependency PR configuration (not pushed or executed remotely).
- Standard debug signing, explicit API configuration, compatible KSP integration; no release credentials needed for debug.
- Fixed saved longitude, restored geolocation snackbar/Retry and permission-result handling, applied stored theme, removed nested settings modals.
- Cancelled superseded forecast jobs, display network results independently of cache persistence, preserved cancellation and legacy-cache fallback.
- Injectable location/update providers with failure cleanup and regression tests.
- Added Android Compose coverage for settings menus and picker/dialog actions, isolated DataStore rebinding, location search/loading/empty states and favorite dialogs, permission explanation, weather feedback snackbar actions, forecast next-day details and sheet dismissal, update prompts/buttons, and loading widgets. These tests passed in the assisted Mi 9T run. Coverage does not establish full navigation/routes, end-to-end favorite CRUD, real system permission flows, Google Maps, process death, insets/IME, or real Play update states.

Important limits: full cache ordering and lifecycle event delivery still need tests. Existing persisted longitude corruption is not repaired by fixing new writes. The working tree also contains the user's pre-existing build/UI changes and staged deletion of SettingsFragment; do not assume all changes originated in this session. Final verification was incremental, not a clean-room build.

Priorities for the next session:

1. Reproduce the successful instrumentation run unassisted on the authorized physical Mi 9T (`ANDROID_SERIAL=<device-serial>`) and resolve any remaining phone/device configuration issues without wiping the phone or removing release app/data.
2. Build a clean checkout without local secrets and run the actual CI workflow; confirm debug, unit, lint, and UI setup independently on fresh runners.
3. Strengthen cache/query/lifecycle race coverage, then design migration or repair for existing favorites with corrupted longitude values.
4. Build app-level UI journey coverage for navigation, Activity recreation, permissions, settings, sheets, insets, and error/retry states; these journeys are not built yet.
5. Finish the Material 3 migration after behavior is protected by the matrix.
6. Decide target stores, published versions, signing ownership, and key handling before changing release CI.

Separate future backlog item (not an immediate priority): design and implement five light/dark main palettes (current default, winter blue, spring green, autumn yellow/orange, and provisional summer red), with default, automatic-by-season, and manual-any-season settings modes. Keep theme brightness independent, preserve the current palette as the default for existing users, and resolve season boundaries, timezone, and hemisphere policy before product implementation. Colors, token roles, contrast, and the required persistence/restart, fixed-time boundary, light/dark, RU/EN, and accessibility verification remain to be designed; this feature is not implemented.

Useful commands:

```sh
./scripts/android.sh doctor
./scripts/android.sh check --max-workers=2
./gradlew :app:testDebugUnitTest :app:lintDebug --console=plain
ANDROID_SERIAL=<device-serial> ./scripts/android.sh ui
```

Ready-to-paste prompt for the next session:

> Continue Everyweather from `docs/NEXT_SESSION.md`. Preserve the existing dirty worktree and do not commit. First inspect the latest device-check report. Reproduce the successful instrumentation run unassisted on the authorized physical Mi 9T (`ANDROID_SERIAL=<device-serial>`), resolve any remaining device configuration issues without wiping the phone or removing release app/data, then investigate app-level journey gaps and update the functional matrix with evidence. Keep production changes scoped to the failing behavior and report tests and remaining manual gaps.
