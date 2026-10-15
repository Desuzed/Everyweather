#!/usr/bin/env bash
set -euo pipefail

# Small, deliberately boring Android project harness. It never installs SDK
# components, wipes a device, or prints values from local.properties.

ROOT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
GRADLEW="$ROOT_DIR/gradlew"
PACKAGE_NAME="com.desuzed.everyweather.debug"

die() { echo "android.sh: $*" >&2; exit 1; }
need_cmd() { command -v "$1" >/dev/null 2>&1 || die "missing command: $1"; }

sdk_from_local_properties() {
  local file="$ROOT_DIR/local.properties"
  [[ -f "$file" ]] || return 0
  awk -F= '$1 == "sdk.dir" { print substr($0, index($0, "=") + 1); exit }' "$file"
}

find_sdk() {
  local candidate
  candidate="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}"
  [[ -n "$candidate" ]] || candidate="$(sdk_from_local_properties)"
  [[ -n "$candidate" ]] || return 1
  candidate=${candidate//\\:/:}
  [[ -d "$candidate" ]] || return 1
  (cd "$candidate" && pwd)
}

sdk_tool() {
  local sdk="$1" tool="$2"
  if [[ -x "$sdk/platform-tools/$tool" ]]; then
    echo "$sdk/platform-tools/$tool"
  elif [[ -x "$sdk/emulator/$tool" ]]; then
    echo "$sdk/emulator/$tool"
  elif [[ -x "$sdk/cmdline-tools/latest/bin/$tool" ]]; then
    echo "$sdk/cmdline-tools/latest/bin/$tool"
  else
    local candidate
    for candidate in "$sdk"/cmdline-tools/*/bin/"$tool"; do
      if [[ -x "$candidate" ]]; then
        echo "$candidate"
        return 0
      fi
    done
    if command -v "$tool" >/dev/null 2>&1; then
      command -v "$tool"
    else
      return 1
    fi
  fi
}

require_sdk_tool() {
  local tool="$1" sdk
  sdk="$(find_sdk)" || die "Android SDK not found; set ANDROID_SDK_ROOT or sdk.dir in local.properties"
  sdk_tool "$sdk" "$tool" || die "missing Android SDK tool: $tool"
}

require_device() {
  [[ -n "${ANDROID_SERIAL:-}" ]] || die "set ANDROID_SERIAL to one explicit device (run: $0 devices)"
  local adb
  adb="$(require_sdk_tool adb)"
  "$adb" -s "$ANDROID_SERIAL" get-state 2>/dev/null | grep -qx device \
    || die "ANDROID_SERIAL=$ANDROID_SERIAL is not an online device"
  echo "$adb"
}

gradle() {
  [[ -x "$GRADLEW" ]] || die "Gradle wrapper is not executable: $GRADLEW"
  (cd "$ROOT_DIR" && "$GRADLEW" "$@")
}

doctor() {
  local sdk="" tool
  echo "project: $ROOT_DIR"
  if sdk=$(find_sdk); then echo "sdk: $sdk"; else echo "sdk: MISSING"; fi
  for tool in adb emulator sdkmanager; do
    if [[ -n "$sdk" ]] && tool_path=$(sdk_tool "$sdk" "$tool" 2>/dev/null); then
      echo "$tool: $tool_path"
    else
      echo "$tool: MISSING"
    fi
  done
  if command -v java >/dev/null 2>&1; then
    echo "java: $(command -v java)"
    java -version 2>&1 | head -n 1
  else echo "java: MISSING"; fi
  [[ -x "$GRADLEW" ]] && echo "gradle-wrapper: ready" || echo "gradle-wrapper: MISSING"
  if command -v python3 >/dev/null 2>&1; then
    python3 --version
  else echo "python3: MISSING (required for check/device-check)"; fi
  if [[ -n "$sdk" ]]; then
    local adb_path
    if adb_path=$(sdk_tool "$sdk" adb 2>/dev/null); then "$adb_path" version | head -n 1; fi
  fi
}

devices() {
  local adb
  adb="$(require_sdk_tool adb)"
  "$adb" devices -l
}

check() {
  need_cmd python3
  PYTHONDONTWRITEBYTECODE=1 python3 -m unittest discover -s "$ROOT_DIR/scripts" \
    -p 'test_android_*.py' -b
  gradle :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --console=plain "$@"
}

ui() {
  local adb
  adb="$(require_device)"
  echo "instrumentation device: $ANDROID_SERIAL"
  ANDROID_SERIAL="$ANDROID_SERIAL" gradle :app:connectedDebugAndroidTest --console=plain
  "$adb" -s "$ANDROID_SERIAL" get-state >/dev/null
}

device_check() {
  local adb sdk android_cli=""
  need_cmd python3
  adb="$(require_device)"
  sdk="$(find_sdk)"
  if [[ -n "${ANDROID_CLI:-}" ]]; then
    [[ -x "$ANDROID_CLI" ]] || die "ANDROID_CLI must point to an executable"
    android_cli="$ANDROID_CLI"
  elif command -v android >/dev/null 2>&1; then
    android_cli="$(command -v android)"
  elif [[ -x "$ROOT_DIR/app/build/tools/android-cli/android" ]]; then
    android_cli="$ROOT_DIR/app/build/tools/android-cli/android"
  fi
  python3 "$ROOT_DIR/scripts/android_device_check.py" --root "$ROOT_DIR" \
    --sdk "$sdk" --adb "$adb" --serial "$ANDROID_SERIAL" \
    --android-cli "$android_cli" "$@"
}

screenshot() {
  local adb out_dir out
  adb="$(require_device)"
  out_dir="$ROOT_DIR/app/build/outputs/screenshots"
  mkdir -p "$out_dir"
  out="$out_dir/${ANDROID_SERIAL}-$(date +%Y%m%d-%H%M%S).png"
  "$adb" -s "$ANDROID_SERIAL" exec-out screencap -p > "$out"
  echo "$out"
}

logs() {
  local adb pid
  adb="$(require_device)"
  pid=$("$adb" -s "$ANDROID_SERIAL" shell pidof -s "$PACKAGE_NAME" 2>/dev/null | tr -d '\r' || true)
  if [[ -n "$pid" ]]; then
    "$adb" -s "$ANDROID_SERIAL" logcat --pid "$pid" "$@"
  else
    echo "no running $PACKAGE_NAME process; filtering logcat by package name" >&2
    "$adb" -s "$ANDROID_SERIAL" logcat "$@" | awk -v package="$PACKAGE_NAME" 'index($0, package) || /Everyweather/ { print; fflush() }'
  fi
}

usage() {
  cat <<'EOF'
Usage: scripts/android.sh <doctor|check|ui|device-check|devices|screenshot|logs> [args...]

device-check: build, verify signatures, install and test; save a JSON report.
  --timeout <seconds>        instrumentation timeout (default 120)
  --build-timeout <seconds>  build timeout (default 600)
  --miui-host               assist blocked Compose hosts, labeled in report

Environment:
  ANDROID_SDK_ROOT or ANDROID_HOME, or sdk.dir in local.properties
  ANDROID_SERIAL is required for ui, device-check, screenshot, and logs
  ANDROID_CLI optionally selects the Android CLI executable for diagnostics
EOF
}

case "${1:-}" in
  doctor) doctor ;;
  check) shift; check "$@" ;;
  ui) ui ;;
  device-check) shift; device_check "$@" ;;
  devices) devices ;;
  screenshot) screenshot ;;
  logs) shift; logs "$@" ;;
  *) usage; exit 2 ;;
esac
