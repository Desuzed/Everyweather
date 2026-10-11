#!/usr/bin/env python3
"""Bounded, data-preserving device checks using the project's debug APKs."""

import argparse
import datetime as dt
import fcntl
import json
import math
import os
from pathlib import Path
import queue
import re
import signal
import subprocess
import tempfile
import threading
import time
import uuid


PACKAGE = "com.desuzed.everyweather.debug"
HOST = PACKAGE + "/androidx.activity.ComponentActivity"


class CheckFailure(Exception):
    pass


def parse_certificates(text):
    certificates = frozenset(re.findall(
        r"Signer #\d+ certificate SHA-256 digest: ([0-9a-fA-F]{64})", text
    ))
    if not certificates:
        raise CheckFailure("Could not read APK signing certificates")
    return frozenset(value.lower() for value in certificates)


def is_miui_host_denial(line):
    return ("MIUILOG- Permission Denied Activity" in line
            and re.search(r"\bcmp=" + re.escape(HOST) + r"(?=\s|})", line) is not None
            and "act=android.intent.action.MAIN" in line
            and "cat=[android.intent.category.LAUNCHER]" in line)


def parse_instrumentation(text):
    """A successful adb exit alone does not mean tests passed or even ran."""
    cases, block = [], {}
    expected = None
    for line in text.splitlines():
        if line.startswith("INSTRUMENTATION_STATUS: "):
            key, separator, value = line[24:].partition("=")
            if separator:
                block[key] = value
        elif line.startswith("INSTRUMENTATION_STATUS_CODE: "):
            code = int(line.split(": ", 1)[1])
            if "numtests" in block:
                expected = int(block["numtests"])
            if code in (0, -1, -2, -3, -4) and "test" in block:
                cases.append({"class": block.get("class", ""), "test": block["test"],
                              "status": "passed" if code == 0 else
                              "skipped" if code in (-3, -4) else "failed",
                              "message": block.get("stack", "")})
            block = {}
    passed = sum(case["status"] == "passed" for case in cases)
    failed = sum(case["status"] == "failed" for case in cases)
    skipped = sum(case["status"] == "skipped" for case in cases)
    summary = re.search(r"^OK \((\d+) tests?\)\s*$", text, re.MULTILINE)
    finished = re.search(r"^INSTRUMENTATION_CODE: -1\s*$", text, re.MULTILINE)
    identities = [(case["class"], case["test"]) for case in cases]
    ok = bool(summary and finished and passed > 0 and failed == 0
              and len(cases) == expected == int(summary.group(1))
              and len(set(identities)) == len(cases)
              and "INSTRUMENTATION_FAILED:" not in text
              and "INSTRUMENTATION_RESULT: shortMsg=" not in text)
    return {"passed": passed, "failed": failed, "skipped": skipped,
            "expected": expected, "cases": cases, "ok": ok}


def positive_seconds(value):
    number = float(value)
    if not math.isfinite(number) or number <= 0:
        raise argparse.ArgumentTypeError("timeout must be a finite positive number")
    return number


class DeviceCheck:
    def __init__(self, args):
        self.args = args
        self.root = Path(args.root).resolve()
        self.sdk = Path(args.sdk).resolve()
        self.adb = [args.adb, "-s", args.serial]
        stamp = dt.datetime.now(dt.timezone.utc).strftime("%Y%m%d-%H%M%S")
        serial = re.sub(r"[^a-zA-Z0-9_.-]", "_", args.serial)
        self.out = self.root / "app/build/outputs/device-check" / (
            stamp + "-" + serial + "-" + uuid.uuid4().hex[:8])
        self.out.mkdir(parents=True)
        self.report = {"schema_version": 1, "serial": args.serial, "status": "running",
                       "started_at": dt.datetime.now(dt.timezone.utc).isoformat(),
                       "stages": [], "assisted_launches": 0, "capture_errors": []}
        self.instrument = None
        self.target_started = False
        self.lock = None
        self.save()

    def save(self):
        temporary = self.out / "report.json.tmp"
        temporary.write_text(json.dumps(self.report, indent=2, ensure_ascii=False) + "\n")
        temporary.replace(self.out / "report.json")

    @staticmethod
    def stop(process):
        if process.poll() is None:
            os.killpg(process.pid, signal.SIGTERM)
            try:
                process.wait(timeout=3)
            except subprocess.TimeoutExpired:
                os.killpg(process.pid, signal.SIGKILL)
                process.wait(timeout=3)

    def command(self, command, timeout=20, log=None):
        process = subprocess.Popen(command, stdout=subprocess.PIPE,
                                   stderr=subprocess.STDOUT, start_new_session=True)
        try:
            output, _ = process.communicate(timeout=timeout)
        except (subprocess.TimeoutExpired, KeyboardInterrupt):
            self.stop(process)
            output, _ = process.communicate()
            if log:
                (self.out / log).write_bytes(output)
            raise
        if log:
            (self.out / log).write_bytes(output)
        if process.returncode != 0:
            raise CheckFailure("Command failed" + ("; see " + log if log else ": "
                               + output.decode(errors="replace")[-1500:]))
        return output.decode(errors="replace").strip()

    def shell(self, *args, **kwargs):
        return self.command(self.adb + ["shell", *args], **kwargs)

    def stage(self, name, action):
        print("device-check: " + name, flush=True)
        entry = {"name": name, "status": "running"}
        self.report["stages"].append(entry)
        self.save()
        start = time.monotonic()
        try:
            action()
            entry["status"] = "passed"
        except BaseException:
            entry["status"] = "failed"
            raise
        finally:
            entry["seconds"] = round(time.monotonic() - start, 3)
            self.save()

    def preflight(self):
        if self.command(self.adb + ["get-state"]) != "device":
            raise CheckFailure("Explicit device is not online")
        if self.shell("am", "get-current-user") != "0":
            raise CheckFailure("Use the primary Android profile (user 0)")
        self.report["model"] = self.shell("getprop", "ro.product.model")
        self.report["api"] = int(self.shell("getprop", "ro.build.version.sdk"))
        self.lock = (self.out.parent / (".lock-" + re.sub(
            r"[^a-zA-Z0-9_.-]", "_", self.args.serial))).open("a")
        try:
            fcntl.flock(self.lock, fcntl.LOCK_EX | fcntl.LOCK_NB)
        except BlockingIOError:
            raise CheckFailure("Another device-check is using this device")
        self.report["android_cli"] = self.args.android_cli or None

    def build(self):
        self.command([str(self.root / "gradlew"), ":app:assembleDebug",
                      ":app:assembleDebugAndroidTest", "--console=plain", "--max-workers=2"],
                     timeout=self.args.build_timeout, log="build.log")

    def apks(self):
        self.artifacts = []
        for variant, package in (("debug", PACKAGE), ("androidTest/debug", PACKAGE + ".test")):
            directory = self.root / "app/build/outputs/apk" / variant
            data = json.loads((directory / "output-metadata.json").read_text())
            elements = data["elements"]
            if data["applicationId"] != package or len(elements) != 1 or elements[0]["filters"]:
                raise CheckFailure("Expected one universal APK for " + package)
            apk = (directory / elements[0]["outputFile"]).resolve()
            if apk.parent != directory.resolve() or not apk.is_file():
                raise CheckFailure("Invalid APK output path")
            self.artifacts.append((package, apk))
            if package == PACKAGE:
                self.report["version"] = elements[0].get("versionName", "")
        versions = list((self.sdk / "build-tools").glob("*/apksigner"))
        versions = [path for path in versions if os.access(path, os.X_OK)]
        if not versions:
            raise CheckFailure("apksigner is required for data-preserving installation")
        def version(path):
            return tuple(int(n) for n in re.findall(r"\d+", path.parent.name))
        self.apksigner = str(max(versions, key=version))

    def certificates(self, apk):
        return parse_certificates(self.command([
            self.apksigner, "verify", "--print-certs", "--min-sdk-version", str(self.report["api"]),
            "--max-sdk-version", str(self.report["api"]), str(apk)], timeout=30))

    def signing(self):
        self.apks()
        local_signers = [self.certificates(apk) for _, apk in self.artifacts]
        if local_signers[0] != local_signers[1]:
            raise CheckFailure("App and test APK signatures differ")
        self.report["signing"] = {}
        # Inspect both packages before installing either. Never retain pulled APKs in reports.
        with tempfile.TemporaryDirectory(prefix="everyweather-signing-") as temporary:
            for (package, _), signers in zip(self.artifacts, local_signers):
                paths = self.shell("pm", "path", "--user", "0", package)
                if not paths:
                    self.report["signing"][package] = "not_installed"
                    continue
                path = next((line[8:] for line in paths.splitlines()
                             if line.startswith("package:") and line.endswith("/base.apk")), None)
                if not path:
                    raise CheckFailure("Cannot locate installed base APK for " + package)
                downloaded = Path(temporary) / (package + ".apk")
                self.command(self.adb + ["pull", path, str(downloaded)], timeout=30)
                if self.certificates(downloaded) != signers:
                    self.report["signing"][package] = "conflict"
                    raise CheckFailure("Signature conflict for " + package
                                       + "; installed application and data were preserved")
                self.report["signing"][package] = "matches"

    def install(self):
        for package, apk in self.artifacts:
            print("device-check: approve USB installation on the phone if requested: " + package,
                  flush=True)
            self.command(self.adb + ["install", "--no-streaming", "-r", "-t", str(apk)],
                         timeout=90, log="install-" + package + ".log")
        runners = self.shell("pm", "list", "instrumentation")
        match = re.search(r"^instrumentation:(" + re.escape(PACKAGE + ".test/")
                          + r"\S+) \(target=" + re.escape(PACKAGE) + r"\)$", runners, re.MULTILINE)
        if not match:
            raise CheckFailure("Expected instrumentation runner is not installed")
        self.runner = match.group(1)
        self.report["runner"] = self.runner

    def run_tests(self):
        events = queue.Queue()
        ready = threading.Event()
        marker = uuid.uuid4().hex
        # A timestamp keeps the marker even if logcat attaches a little later.
        # '-T 1' can miss it when unrelated system messages arrive in between.
        device_time = self.shell("date", "+%Y-%m-%dT%H:%M:%S%z")
        since = dt.datetime.strptime(device_time, "%Y-%m-%dT%H:%M:%S%z").strftime(
            "%m-%d %H:%M:%S.000")
        watcher = subprocess.Popen(
            self.adb + ["logcat", "-T", since, "-s", "ActivityTaskManagerServiceInjector",
                        "EveryweatherDeviceCheck"],
            stdout=subprocess.PIPE, stderr=subprocess.DEVNULL, text=True, start_new_session=True)
        def read_events():
            for line in watcher.stdout:
                if marker in line:
                    ready.set()
                elif ready.is_set() and is_miui_host_denial(line):
                    events.put(line)
        reader = threading.Thread(target=read_events, daemon=True)
        reader.start()
        seen = set()
        path = self.out / "instrumentation.log"
        try:
            self.shell("log", "-t", "EveryweatherDeviceCheck", marker)
            if not ready.wait(timeout=5) and self.args.miui_host:
                raise CheckFailure("Cannot observe fresh MIUI activity events")
            with path.open("wb") as output, (self.out / "miui.log").open("w") as miui:
                self.instrument = subprocess.Popen(
                    self.adb + ["shell", "am", "instrument", "-w", "-r", self.runner],
                    stdout=output, stderr=subprocess.STDOUT, start_new_session=True)
                self.target_started = True
                end = time.monotonic() + self.args.timeout
                while self.instrument.poll() is None:
                    if "target_pid" not in self.report and path.stat().st_size:
                        try:
                            pid = self.shell("pidof", "-s", PACKAGE, timeout=5)
                            if pid.isdigit():
                                self.report["target_pid"] = pid
                        except CheckFailure:
                            pass
                    if time.monotonic() >= end:
                        self.report["status"] = "timeout"
                        raise CheckFailure("Instrumentation exceeded the timeout")
                    try:
                        line = events.get(timeout=min(0.5, max(0.01, end - time.monotonic())))
                    except queue.Empty:
                        continue
                    if line in seen:
                        continue
                    seen.add(line)
                    miui.write(line)
                    miui.flush()
                    if self.args.miui_host:
                        self.shell("am", "start", "-W", "-a", "android.intent.action.MAIN",
                                   "-c", "android.intent.category.LAUNCHER", "-f", "0x10008000",
                                   "-n", HOST, timeout=min(15, max(0.1, end - time.monotonic())))
                        self.report["assisted_launches"] += 1
                        print("device-check: MIUI host assistance "
                              + str(self.report["assisted_launches"]), flush=True)
                self.report["tests"] = parse_instrumentation(path.read_text(errors="replace"))
                if self.instrument.returncode != 0 or not self.report["tests"]["ok"]:
                    raise CheckFailure("Instrumentation failed or did not complete all tests; see instrumentation.log")
        finally:
            self.stop(watcher)
            reader.join(timeout=2)
            watcher.stdout.close()

    def cleanup_instrumentation(self):
        if self.instrument is not None and self.instrument.poll() is None:
            # Only our debug process; never the release package or device data.
            try:
                self.shell("am", "force-stop", PACKAGE, timeout=10)
            finally:
                self.stop(self.instrument)
        if self.target_started:
            path = self.out / "instrumentation.log"
            if path.exists():
                self.report["tests"] = parse_instrumentation(path.read_text(errors="replace"))

    def diagnostics(self):
        if not self.target_started:
            return
        def attempt(name, action):
            try:
                action()
            except Exception as error:
                self.report["capture_errors"].append({"artifact": name, "error": str(error)})
        attempt("window", lambda: (self.out / "window.txt").write_text(
            "\n".join(line for line in self.shell("dumpsys", "window").splitlines()
                      if "mCurrentFocus=" in line or "mFocusedApp=" in line) + "\n"))
        def logs():
            pid = self.report.get("target_pid") or self.shell("pidof", "-s", PACKAGE)
            self.command(self.adb + ["logcat", "-d", "-t", "1000", "--pid", pid],
                         log="app.logcat")
        attempt("app.logcat", logs)
        if self.args.android_cli:
            prefix = [self.args.android_cli, "--no-metrics", "--sdk", str(self.sdk)]
            attempt("screenshot.png", lambda: self.command(prefix + [
                "screen", "capture", "--device", self.args.serial, "--output",
                str(self.out / "screenshot.png")], timeout=30, log="screen-capture.log"))
            attempt("layout.json", lambda: self.command(prefix + [
                "layout", "--device", self.args.serial, "--pretty", "--output",
                str(self.out / "layout.json")], timeout=30, log="layout-capture.log"))
        def valid_png():
            try:
                with (self.out / "screenshot.png").open("rb") as source:
                    return source.read(8) == b"\x89PNG\r\n\x1a\n"
            except OSError:
                return False
        def valid_layout():
            try:
                return isinstance(json.loads((self.out / "layout.json").read_text()), (dict, list))
            except (OSError, ValueError):
                return False
        if not valid_png():
            def screenshot():
                with (self.out / "screenshot.png").open("wb") as output:
                    process = subprocess.Popen(self.adb + ["exec-out", "screencap", "-p"],
                                               stdout=output, stderr=subprocess.PIPE, start_new_session=True)
                    try:
                        process.communicate(timeout=15)
                    finally:
                        self.stop(process)
                    if process.returncode != 0:
                        raise CheckFailure("adb screenshot failed")
                if not valid_png():
                    raise CheckFailure("adb did not produce a PNG screenshot")
            attempt("screenshot.png", screenshot)
        if not valid_layout():
            remote = "/data/local/tmp/everyweather-ui-" + uuid.uuid4().hex + ".xml"
            def layout():
                try:
                    self.shell("uiautomator", "dump", remote, timeout=20)
                    self.command(self.adb + ["pull", remote, str(self.out / "layout.xml")])
                finally:
                    self.shell("rm", "-f", remote)
            attempt("layout.xml", layout)

    def run(self):
        code = 1
        try:
            self.stage("preflight", self.preflight)
            self.stage("build", self.build)
            self.stage("signing", self.signing)
            self.stage("install", self.install)
            self.stage("instrumentation", self.run_tests)
            self.report["status"] = "passed_assisted" if self.report["assisted_launches"] else "passed"
            code = 0
        except KeyboardInterrupt:
            self.report["status"] = "interrupted"
            self.report["error"] = "Interrupted by user"
            code = 130
        except Exception as error:
            if self.report["status"] == "running":
                self.report["status"] = "failed"
            self.report["error"] = str(error)
        finally:
            try:
                self.cleanup_instrumentation()
            except Exception as error:
                self.report["capture_errors"].append({"artifact": "cleanup", "error": str(error)})
            if code != 0:
                self.diagnostics()
            self.report["finished_at"] = dt.datetime.now(dt.timezone.utc).isoformat()
            self.save()
            if self.lock:
                self.lock.close()
        tests = self.report.get("tests", {})
        print("device-check: " + self.report["status"] + " (passed=" + str(tests.get("passed", 0))
              + ", failed=" + str(tests.get("failed", 0)) + ")")
        if "error" in self.report:
            print("device-check: " + self.report["error"])
        print("device-check report: " + str(self.out / "report.json"))
        return code


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", required=True)
    parser.add_argument("--sdk", required=True)
    parser.add_argument("--adb", required=True)
    parser.add_argument("--serial", required=True)
    parser.add_argument("--android-cli", default="")
    parser.add_argument("--timeout", type=positive_seconds, default=120)
    parser.add_argument("--build-timeout", type=positive_seconds, default=600)
    parser.add_argument("--miui-host", action="store_true",
                        help="Assist denied Compose host launches; report passed_assisted separately")
    args = parser.parse_args()
    def interrupted(_signum, _frame):
        raise KeyboardInterrupt
    signal.signal(signal.SIGTERM, interrupted)
    os.chdir(args.root)
    raise SystemExit(DeviceCheck(args).run())


if __name__ == "__main__":
    main()
