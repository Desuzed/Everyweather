# Functional test matrix

This matrix records the current regression contract for the single `app` module. The latest 2026-10-07 device-check on the authorized Mi 9T passed 41/41 tests: 39 Compose tests plus 2 Room/context integration tests, with 39 assisted launches and no errors or skips. The assisted run is device evidence, not unattended CI.

Current 2026-10-08 phone verification report is
`app/build/outputs/device-check/<run>/report.json`:
8/47 tests passed, with 0 assertion failures. All six Android atomic-store,
Room, and app-context checks passed; the first Compose launch timed out after 15
seconds because the fresh MIUI session was keyguard-locked. The Compose/UI
portion remains pending an unlocked-device retest and must not be marked passed.

Final source verification passed 18 Python checks and 91 JVM tests with 0
failures, errors, or skips; lint has 0 errors, 133 warnings, and 1 hint.
The added Firebase event compatibility JVM test passed. This was a pure logging
refactor: UI flows, persistence, and SDK behavior were unchanged; UI was not
rerun this turn, the prior phone run remains 8/47 with MIUI keyguard locked,
and real-store scenarios remain pending.

| ID | Area and scenario | Setup / action | Expected result | Level | Status |
| --- | --- | --- | --- | --- | --- |
| F-01 | Cold start with no saved query | Clear app data, launch, observe initial weather request | One coherent loading state; no false `NO_DATA`; weather or a visible error follows | UI + network fake | Loading/empty-state Compose checks passed assisted on Mi 9T; full first-launch flow pending |
| F-02 | Cached forecast and refresh | Seed a valid cached forecast, relaunch, refresh | Cached content is shown while refresh runs; successful network content replaces it | ViewModel/integration | JVM coverage passing; UI pending |
| F-03 | Out-of-order requests | Make query A slow, query B fast; select B after A | B remains the displayed and cached result when both complete | ViewModel | Display ordering tested; real cache write ordering pending |
| F-04 | Search and map selection | Open location search, choose a result/map point, return to weather | Selected latitude/longitude and display name produce a weather query; screen returns correctly | UI | Search typing, find, empty, and loading callbacks passed assisted on Mi 9T; full navigation/map UI pending |
| F-05 | Favorite round trip | Save a location, reopen favorites, select it, edit its name, delete it | Stored query retains numeric latitude and longitude; custom name survives edit and deletion removes only that item | Room + UI | Favorite tap/edit/delete callbacks and dialogs passed assisted on Mi 9T; full navigation/CRUD journey pending |
| F-06 | Location permission paths | First launch; deny, grant, approximate-only; retry and timeout/provider failure | Each state has a visible message and retry action; no stale location is used | Platform + UI | Permission explanation passed assisted on Mi 9T; JVM provider coverage passing; real system permission flow pending |
| F-07 | Error snackbar and retry | Force weather/location HTTP error and tap retry | Error snackbar appears with the correct action; retry starts one new request | UI + fake network | Snackbar retry/OK, dismissal, and new-result callbacks passed assisted on Mi 9T; provider/network end-to-end flow pending |
| F-08 | Settings persistence | Change language, temperature, distance, pressure, dark mode; restart process | Values persist and are reflected in labels, weather units, and theme | UI + DataStore | Picker callbacks and isolated DataStore screen rebind passed assisted on Mi 9T; real process-death restart pending |
| F-09 | Nested modal behavior | Open each settings picker and the next-days/details sheet; press Back, swipe, outside | One modal closes at a time; focus, scrim, and keyboard remain usable | UI | Settings picker Back and next-days sheet Back passed assisted on Mi 9T; swipe/outside, focus, IME, and full modal journeys pending |
| F-10 | Forecast mapping boundaries | Supply empty/partial API data, missing astro fields, malformed cached JSON | App shows a recoverable error or empty state; no crash from nullable API data | Unit + integration | Selected mapping and corrupt-cache fallback tests pass; partial/null API responses and UI still pending |
| F-11 | Database migration | Install schema 1 data, upgrade through schema 2 | Existing favorite names and coordinates remain readable; migration completes | Room integration | Passed assisted on Mi 9T |
| F-12 | Lifecycle event delivery | Trigger location/error/update while paused, resume, navigate, and recreate Activity | Event is delivered according to the intended lifecycle contract, without duplicate retry/navigation | UI + platform | Manual |
| F-13 | In-app update states | Use a fake update provider for ready, canceled, failed, downloaded | Dialog/sheet closes or reports state correctly; install cancel/failure keeps `ReadyToInstall` available for settings retry without an extra error alert; retry and resume do not duplicate listeners | ViewModel/UI | Ready/update/install prompt buttons, null fallback, and downloading widgets passed assisted on Mi 9T; install-cancel/complete-failure regressions and failed/downloaded provider end-to-end flow pending |
| F-14 | Insets and keyboard | Combine offline banner, sheet, IME, and gesture/3-button navigation | Content and actions remain visible and tappable in light/dark themes | UI device | Manual |
| F-15 | Release smoke | Install/update on min and current supported API with valid controlled keys | App launches, maps/weather calls work, Room data survives update, and no debug-only behavior leaks | Release device | Manual; release pipeline pending |
| F-16 | Update telemetry persistence | Drive fake Play SDK check/flow/download/install events, vary install source, restart with pending marker | Event stages and outcomes remain distinct; origin/requested/from versions survive restart; pending, observed, expired, rollback, and storage-error markers are deterministic | JVM + Android storage | Firebase event compatibility JVM test passed; six Android atomic-store tests passed in the 2026-10-08 device run; UI retest pending |
| F-17 | Multi-store update attribution | Controlled Play→Play, Galaxy→Play eligible, and Galaxy→Play unavailable/error installs with same application ID, controlled signatures, and higher Play version | Positive flows remain distinguishable from `APP_NOT_OWNED`/signature or availability errors; source is context, not diagnosis | Manual multi-store | Pending; no real store installation has been verified |

The first regression group is F-01, F-03, F-05, F-06, F-07, F-08, and F-13. Passing JVM tests do not establish Android lifecycle, permissions, installation, navigation, or rendering behavior. The current expanded suite has assisted device evidence on the authorized physical Mi 9T (`<device-serial>`); this is not unattended CI evidence. Full routes/navigation, system permission flows, process death, Maps, and insets/IME remain pending. Do not wipe the phone or remove its release app/data to make a run pass.

Earlier 2026-10-07 compilation validation: `./scripts/android.sh check :app:assembleDebugAndroidTest --max-workers=2` completed successfully, including compilation of the added Compose tests. The matrix covers selected units and callbacks for settings, location, weather feedback, forecast details, update prompts, and loading widgets; full routes/navigation, CRUD journeys, real system permissions, Google Maps, process death, insets/IME, and real Play update flows remain manual or pending.

Final 2026-10-07 check: `./scripts/android.sh check --max-workers=2` exited 0. All 18 Python device-check tests passed; Gradle assemble, JVM tests, and lint completed successfully in about 6 seconds with cached/up-to-date JVM results (50 tests reported), 0 lint errors, 133 warnings, and 1 hint.

2026-10-07 current device evidence: `ANDROID_SERIAL=<device-serial> ./scripts/android.sh device-check --miui-host --timeout 240` completed `passed_assisted`: all 41 tests passed with 39 assisted host launches and no errors or skips. Report: `app/build/outputs/device-check/<run>/report.json`. This is assisted physical-device evidence, not unattended CI. The run includes the integrated forecast card-to-details-to-Back flow, isolated DataStore screen rebinding, update/loading widgets, settings/location dialogs, weather feedback, Room migration, and the current Compose suite. A fake Coil image loader prevented test image URLs from reaching the network.

Historical device evidence: the earlier 10/10 assisted run is retained at `app/build/outputs/device-check/<run>/report.json`. The immediately preceding expanded-suite attempt stopped during a 90-second installation timeout while the phone keyguard was locked and executed 0 tests; it was an environment/install timeout, not a test failure. Report: `app/build/outputs/device-check/<run>/report.json`.

The device-check reports failed-stage snapshots only after instrumentation starts; build/signing/install failures retain their report and stage logs. The earlier timeout run (`20261007-120014-<device-serial>-c4b6c6c4`) used the officially bootstrapped Android CLI `1.0.16500706` for screenshot diagnostics; CLI JSON layout was not verified because MIUI rejected the layout helper installation, while the adb XML fallback was saved. The successful run did not need failure diagnostics. No permanent setting changed.
