# Development commands

The repository uses the Gradle wrapper (`./gradlew`) and the Android SDK command line tools. The small project harness at [`scripts/android.sh`](../scripts/android.sh) keeps common checks explicit and avoids changing SDKs or devices.

```sh
./scripts/android.sh doctor
./scripts/android.sh check
./scripts/android.sh devices
ANDROID_SERIAL=<one-online-device-id> ./scripts/android.sh ui
ANDROID_SERIAL=<one-online-device-id> ./scripts/android.sh screenshot
ANDROID_SERIAL=<one-online-device-id> ./scripts/android.sh logs
```

`check` runs `:app:assembleDebug`, `:app:testDebugUnitTest`, and `:app:lintDebug`. `ui` runs `:app:connectedDebugAndroidTest` against the one device named by `ANDROID_SERIAL`; it will not guess among multiple devices. `screenshot` writes a PNG under `app/build/outputs/screenshots/` and prints its path. `logs` uses the debug application PID when available and otherwise filters logcat output by the application package/name.

The harness discovers the SDK from `ANDROID_SDK_ROOT`, `ANDROID_HOME`, or `sdk.dir` in `local.properties` (in that order). It reports paths but never prints local property values. It does not install packages, start an emulator, wipe data, or reset a device. Use the official Android CLI tools: `adb` for connected devices, `emulator` to manage an already installed AVD, and `sdkmanager` to inspect or install SDK components when you intentionally choose to do so. The official [`android` CLI](https://developer.android.com/tools/agents/android-cli) also supports Android SDK management, app install/run, and device layout/screen operations. It is not on this environment's `PATH` today, so the existing Gradle/adb wrapper remains suitable here.

`doctor` reports missing tools without attempting repair. A normal setup needs a JDK compatible with the Gradle/AGP versions, the wrapper distribution, Android SDK platform/build tools required by the project, and `platform-tools` for device commands. API keys and signing values belong in the developer's untracked local configuration; do not paste them into shell output or documentation.

When `connectedDebugAndroidTest` installation is canceled or streaming installation is unavailable, an authorized physical device can use the manual non-streaming fallback below. Unlock the phone and approve USB installation first; do not wipe device data or remove a release package. This fallback is a manual diagnostic run and does not replace the wrapper's normal UI check:

```sh
adb -s <device-serial> install --no-streaming -r -t app/build/outputs/apk/debug/app-debug.apk
adb -s <device-serial> install --no-streaming -r -t app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb -s <device-serial> shell am instrument -w -r com.desuzed.everyweather.debug.test/com.desuzed.everyweather.TestRunner
```

On some MIUI devices, each test Activity launch may be denied with `MIUILOG Permission Denied Activity`. During a manual diagnostic run, start the matching launcher Activity with `am start` when the denial appears so ActivityScenario can match its MAIN/LAUNCHER intent. Record this as assisted evidence; do not treat it as unattended CI coverage or change permanent system settings.

```sh
adb -s <device-serial> shell am start -W -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -f 0x10008000 -n com.desuzed.everyweather.debug/androidx.activity.ComponentActivity
```

For a bounded, data-preserving build/install/instrumentation check, use the explicit device serial:

```sh
ANDROID_SERIAL=<device-serial> ./scripts/android.sh device-check
ANDROID_SERIAL=<device-serial> ./scripts/android.sh device-check --miui-host
```

Omit `--miui-host` for the standard unassisted run. The check builds `app-debug.apk` and `app-debug-androidTest.apk`, verifies that both APKs have matching signatures, inspects both installed packages before either install, and installs with `-r -t --no-streaming`. It never uninstalls, wipes data, changes device settings, or targets an implicit device. The default instrumentation timeout is 120 seconds and the build timeout is 600 seconds; both can be overridden with `--timeout` and `--build-timeout`.

Each run writes a unique report to `app/build/outputs/device-check/<timestamp-and-device>/report.json`, along with stage logs and instrumentation output. A fully complete run reports `passed` when no assisted launch occurred, and `passed_assisted` only when one or more assisted launches actually occurred. A nonzero exit is used for `timeout`, `interrupted`, or `failed`: timeout is the instrumentation limit, interrupted is user cancellation, and failed covers signature/install errors, canceled installation, zero tests, incomplete instrumentation, or failed tests. An adb success alone is not treated as test success.

The latest assisted device check completed as `passed_assisted` with 41 of 41 tests passed (39 Compose UI tests, one Room migration test, and one app-context test) and 39 assisted host launches. It used `ANDROID_SERIAL=<device-serial> ./scripts/android.sh device-check --miui-host --timeout 240`; its report is `app/build/outputs/device-check/<run>/report.json`. The expanded UI workflows cover settings selection and update actions, loading and download progress transitions, location flows, and weather content alongside the storage and context checks. The earlier 10-test run remains at `app/build/outputs/device-check/<run>/report.json`, and the prior timeout remains at `app/build/outputs/device-check/<run>/report.json`; its instrumentation, MIUI, app logcat, window, and CLI screenshot artifacts were retained. Failed-stage snapshots are captured only after instrumentation starts; build, signing, and install failures retain their stage logs and report without pretending that a screenshot or layout was captured.

`./scripts/android.sh check` first runs 20 Python standard-library tests: 18 for the device-check helpers and two for SDK bootstrap ordering and action pinning in the CI workflow, then the three Gradle tasks. On a failed device check, diagnostics capture the app logcat and attempt screenshot/layout artifacts. If `ANDROID_CLI` is set, or `android` is on `PATH`, or the ignored local `app/build/tools/android-cli/android` exists, the optional CLI is used with `--no-metrics`; otherwise screenshot and UI XML use adb fallbacks. CLI screenshot capture works on the current device; CLI JSON layout capture is not yet verified because the layout helper installation was rejected by MIUI and the error was recorded in `layout-capture.log`. The adb XML fallback was saved successfully. Capture failures are recorded in `report.json` without hiding the original check failure. The current ignored local CLI was downloaded through the official bootstrap and tested as Android CLI `1.0.16500706`; it may disappear after a Gradle clean. The CLI may be bootstrapped locally at the official [Android CLI installation guide](https://developer.android.com/tools/agents/android-cli/install), and the project does not run `android init` automatically.

Final source verification ran `./scripts/android.sh check :app:assembleDebugAndroidTest --max-workers=2` successfully in 26 seconds: 18 Python checks and 91 JVM tests passed with 0 failures, errors, or skips; lint reported 0 errors, 133 warnings, and 1 hint. The Firebase event compatibility JVM test passed. The three install-cancel/complete-failure regressions were each reproduced as failures and then fixed, covering task failure plus listener `FAILED` and `CANCELED`. This was a pure logging refactor; UI flows, persistence, and SDK behavior were unchanged. UI was not rerun this turn. The 2026-10-08 phone report `app/build/outputs/device-check/<run>/report.json` remains 8/47 with 0 assertion failures; all six atomic-store, Room, and app-context checks passed, while the first Compose launch timed out after 15 seconds with MIUI keyguard locked. The remaining UI suite and real-store scenarios remain pending.

Previous coordinator verification: `./scripts/android.sh check --max-workers=2` exited 0; the 18 Python tests passed and assembleDebug, testDebugUnitTest, and lintDebug completed with `BUILD SUCCESSFUL`. That run reported 50 JVM XML tests with 0 failures, errors, or skips; the count included up-to-date results. The current source verification is recorded above with 91 JVM tests; current lint XML reports 0 errors, 133 warnings, and 1 hint.

Debug uses the standard Android debug certificate and builds without API or release signing credentials. Tests use a plain Application, fakes and local fixtures. Real weather/geocoding/maps need `WEATHER_API_KEY`, `LOCATION_IQ_API` and `MAPS_API_KEY`; set these in environment variables, Gradle properties or untracked `local.properties` (that precedence order). Only these API fields are included in BuildConfig/manifest; signing values are not exported to the app. Switching the debug certificate may require adding its fingerprint to the Maps key restrictions.

Release additionally requires `KEYSTORE_PATH` (defaults to `sign/everyweather_keystore.jks`), `KEY_ALIAS`, `KEYSTORE_PASSWORD` and optionally `KEY_PASSWORD`. Release tasks validate those inputs. No check command increments versions or uploads artifacts.

CI is GitHub Actions: PR/push runs debug assembly, unit tests and lint; successful checks enable emulator UI/migration tests on APIs 23 and 36. Actions are pinned to commit SHAs; reports are uploaded even on failure. Dependabot proposes weekly dependency/action updates, without automatic merging. Workflows only become active after they are committed and pushed; this setup does not publish to a store.

Use JDK 17 (CI), Android platform 36 and build tools 36.0.0. The wrapper pins Gradle and the catalog pins AGP/Kotlin/KSP. The local SDK inspected during setup has adb/emulator and API 34/36 images, but lacks modern `cmdline-tools`/`sdkmanager`; install Android SDK Command-line Tools through SDK Manager when needed. Existing SDKs suffice for local compilation and an existing AVD.

The agent harness includes root `AGENTS.md`, `.codex/config.toml`, and `.codex/agents/{routine,refactor}.toml`: Luna handles bounded routine work, Terra follows agreed refactoring algorithms, and the coordinator integrates and runs verification. The agent instructions and profiles are included in version control at the owner's request. The audit remains local and excluded in this checkout through `.git/info/exclude`. The agent files are not required for CI. New agent sessions load the profiles; current delegation also selects models explicitly.

Official references: [Android SDK CLI](https://developer.android.com/tools), [Android CLI commands](https://developer.android.com/tools/agents/android-cli/commands), [Codex project instructions](https://learn.chatgpt.com/docs/agent-configuration/agents-md), [subagents](https://learn.chatgpt.com/docs/agent-configuration/subagents).

CI initializes the Android SDK with a pinned `android-actions/setup-android` step in both jobs before calling `sdkmanager`. This avoids relying on Android tools being preinstalled on the runner. The two SDK bootstrap regressions reproduced the missing setup before the fix and pass afterward.
