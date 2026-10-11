#!/usr/bin/env python3
"""Regression tests for the pure helpers in android_device_check.py."""

import argparse
import json
import sys
import tempfile
import unittest
from pathlib import Path
from unittest import mock


SCRIPTS = Path(__file__).resolve().parent
if str(SCRIPTS) not in sys.path:
    sys.path.insert(0, str(SCRIPTS))

from android_device_check import (  # noqa: E402
    CheckFailure,
    is_miui_host_denial,
    parse_certificates,
    parse_instrumentation,
    positive_seconds,
)
import android_device_check as device_check  # noqa: E402


PASS = """\
INSTRUMENTATION_STATUS: class=com.desuzed.everyweather.ExampleTest
INSTRUMENTATION_STATUS: test=loadsWeather
INSTRUMENTATION_STATUS: numtests=2
INSTRUMENTATION_STATUS_CODE: 1
INSTRUMENTATION_STATUS: class=com.desuzed.everyweather.ExampleTest
INSTRUMENTATION_STATUS: test=loadsWeather
INSTRUMENTATION_STATUS: numtests=2
INSTRUMENTATION_STATUS_CODE: 0
INSTRUMENTATION_STATUS: class=com.desuzed.everyweather.ExampleTest
INSTRUMENTATION_STATUS: test=showsEmptyState
INSTRUMENTATION_STATUS: numtests=2
INSTRUMENTATION_STATUS_CODE: 1
INSTRUMENTATION_STATUS: class=com.desuzed.everyweather.ExampleTest
INSTRUMENTATION_STATUS: test=showsEmptyState
INSTRUMENTATION_STATUS: numtests=2
INSTRUMENTATION_STATUS_CODE: 0
OK (2 tests)
INSTRUMENTATION_CODE: -1
"""


def _run_with(*case_blocks, summary="OK (2 tests)", finished=True):
    """Build realistic status-start/status-finish output for parser fixtures."""
    output = []
    for class_name, test_name, code, numtests in case_blocks:
        output.extend((
            "INSTRUMENTATION_STATUS: class=" + class_name,
            "INSTRUMENTATION_STATUS: test=" + test_name,
            "INSTRUMENTATION_STATUS: numtests=" + str(numtests),
            "INSTRUMENTATION_STATUS_CODE: 1",
            "INSTRUMENTATION_STATUS: class=" + class_name,
            "INSTRUMENTATION_STATUS: test=" + test_name,
            "INSTRUMENTATION_STATUS: numtests=" + str(numtests),
            "INSTRUMENTATION_STATUS_CODE: " + str(code),
        ))
    output.append(summary)
    if finished:
        output.append("INSTRUMENTATION_CODE: -1")
    return "\n".join(output) + "\n"


class InstrumentationParsingTests(unittest.TestCase):
    def test_complete_run_requires_real_cases_and_reports_counts(self):
        result = parse_instrumentation(PASS)

        self.assertTrue(result["ok"])
        self.assertEqual((result["passed"], result["failed"], result["skipped"]), (2, 0, 0))
        self.assertEqual(result["expected"], 2)
        self.assertEqual([case["test"] for case in result["cases"]],
                         ["loadsWeather", "showsEmptyState"])

    def test_adb_success_or_empty_output_cannot_be_false_green(self):
        self.assertFalse(parse_instrumentation("")["ok"])
        self.assertFalse(parse_instrumentation("INSTRUMENTATION_CODE: -1\n")["ok"])

    def test_assertion_failure_is_failed_even_when_a_case_was_reported(self):
        output = _run_with(("ExampleTest", "loadsWeather", -2, 1))
        result = parse_instrumentation(output)

        self.assertFalse(result["ok"])
        self.assertEqual(result["failed"], 1)
        self.assertEqual(result["cases"][0]["status"], "failed")

    def test_crash_marker_and_short_message_fail_the_run(self):
        for marker in ("INSTRUMENTATION_FAILED: Process crashed.",
                       "INSTRUMENTATION_RESULT: shortMsg=Process crashed."):
            output = PASS + marker + "\n"
            self.assertFalse(parse_instrumentation(output)["ok"], marker)

    def test_zero_tests_all_skipped_and_truncated_runs_are_not_green(self):
        self.assertFalse(parse_instrumentation(
            "OK (0 tests)\nINSTRUMENTATION_CODE: -1\n")["ok"])

        skipped = _run_with(("ExampleTest", "ignored", -3, 1), summary="OK (1 test)")
        skipped_result = parse_instrumentation(skipped)
        self.assertEqual(skipped_result["skipped"], 1)
        self.assertFalse(skipped_result["ok"])

        truncated = _run_with(("ExampleTest", "loadsWeather", 0, 1), finished=False,
                              summary="OK (1 test)")
        self.assertFalse(parse_instrumentation(truncated)["ok"])

    def test_inconsistent_expected_or_summary_and_duplicate_case_fail(self):
        inconsistent = _run_with(("ExampleTest", "loadsWeather", 0, 2), summary="OK (1 test)")
        self.assertFalse(parse_instrumentation(inconsistent)["ok"])

        duplicate = _run_with(("ExampleTest", "loadsWeather", 0, 2),
                              ("ExampleTest", "loadsWeather", 0, 2))
        self.assertFalse(parse_instrumentation(duplicate)["ok"])


class CertificateParsingTests(unittest.TestCase):
    def test_certificates_are_lowercased_and_preserve_multisigner_set(self):
        first = "A" * 64
        second = "b" * 64
        text = ("Signer #1 certificate SHA-256 digest: " + first + "\n"
                "Signer #2 certificate SHA-256 digest: " + second + "\n")

        self.assertEqual(parse_certificates(text), frozenset({first.lower(), second}))

    def test_empty_or_malformed_certificate_output_is_rejected(self):
        for text in ("", "Signer #1 certificate SHA-256 digest: too-short"):
            with self.assertRaises(CheckFailure):
                parse_certificates(text)


class MiuiDenialTests(unittest.TestCase):
    def test_real_miui_log_with_space_before_component_brace_matches(self):
        line = ("10-07 15:00:37.828 2014 4635 D ActivityTaskManagerServiceInjector: "
                "MIUILOG- Permission Denied Activity : Intent { "
                "act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER] "
                "flg=0x10008000 cmp=com.desuzed.everyweather.debug/"
                "androidx.activity.ComponentActivity } pkg : com.desuzed.everyweather.debug "
                "uid : 10298 tuid : 10097")
        self.assertTrue(is_miui_host_denial(line))

    def test_component_name_with_appended_suffix_does_not_match(self):
        line = ("MIUILOG- Permission Denied Activity : Intent { "
                "act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER] "
                "cmp=com.desuzed.everyweather.debug/"
                "androidx.activity.ComponentActivityOther } pkg : com.desuzed.everyweather.debug")
        self.assertFalse(is_miui_host_denial(line))

    def test_only_debug_component_main_launcher_denial_matches(self):
        line = ("MIUILOG- Permission Denied Activity: "
                "cmp=com.desuzed.everyweather.debug/androidx.activity.ComponentActivity} "
                "act=android.intent.action.MAIN "
                "cat=[android.intent.category.LAUNCHER]")
        self.assertTrue(is_miui_host_denial(line))

    def test_other_app_activity_or_intent_does_not_match(self):
        base = ("MIUILOG- Permission Denied Activity: "
                "cmp=com.desuzed.everyweather.debug/androidx.activity.ComponentActivity} ")
        for suffix in (
                "act=android.intent.action.VIEW cat=[android.intent.category.LAUNCHER]",
                "act=android.intent.action.MAIN cat=[android.intent.category.DEFAULT]",
                "act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER]",
        ):
            line = base + suffix
            if suffix.endswith("LAUNCHER]"):
                line = line.replace("com.desuzed.everyweather.debug/", "other.app/")
            self.assertFalse(is_miui_host_denial(line), line)

        other_activity = base.replace("androidx.activity.ComponentActivity", "MainActivity")
        self.assertFalse(is_miui_host_denial(
            other_activity + "act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER]"))


class PositiveSecondsTests(unittest.TestCase):
    def test_accepts_finite_positive_values(self):
        self.assertEqual(positive_seconds("0.25"), 0.25)
        self.assertEqual(positive_seconds("10"), 10.0)

    def test_rejects_zero_negative_and_non_finite_values(self):
        for value in ("0", "-1", "nan", "inf", "-inf"):
            with self.assertRaises(argparse.ArgumentTypeError):
                positive_seconds(value)


class DeviceCheckSafetyTests(unittest.TestCase):
    """Exercise pipeline outcomes with fake commands and a temporary project root."""

    def make_checker(self, root):
        args = type("Args", (), {
            "root": str(root), "sdk": str(root), "adb": "adb", "serial": "SERIAL",
            "android_cli": "", "timeout": 1, "build_timeout": 1, "miui_host": False,
        })()
        return device_check.DeviceCheck(args)

    def test_signing_conflict_stops_before_install_and_cleans_staged_apk(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            checker = self.make_checker(root)
            checker.apks = lambda: setattr(checker, "artifacts", [
                (device_check.PACKAGE, root / "debug.apk"),
                (device_check.PACKAGE + ".test", root / "test.apk"),
            ])
            signer = "a" * 64
            checker.certificates = mock.Mock(side_effect=[
                frozenset({signer}), frozenset({signer}), frozenset({"b" * 64}),
            ])
            checker.shell = mock.Mock(return_value="package:/data/app/base.apk")
            def fake_command(command, **kwargs):
                if "pull" in command:
                    Path(command[-1]).write_bytes(b"pulled apk")
                return ""
            checker.command = mock.Mock(side_effect=fake_command)
            checker.preflight = checker.build = lambda: None
            checker.install = mock.Mock()
            checker.run_tests = mock.Mock()
            checker.diagnostics = mock.Mock()
            real_temporary_directory = tempfile.TemporaryDirectory
            with mock.patch.object(
                    device_check.tempfile, "TemporaryDirectory",
                    side_effect=lambda **kwargs: real_temporary_directory(dir=directory, **kwargs)):
                with mock.patch("builtins.print"):
                    self.assertEqual(checker.run(), 1)

            self.assertEqual(checker.report["signing"][device_check.PACKAGE], "conflict")
            self.assertEqual(checker.report["status"], "failed")
            checker.install.assert_not_called()
            checker.run_tests.assert_not_called()
            self.assertFalse((root / "debug.apk").exists())
            self.assertFalse((root / "test.apk").exists())
            self.assertFalse(any(root.rglob("*.apk")), "pulled APK leaked into project output")

    def test_malformed_instrumentation_produces_failed_report_and_preserves_logs(self):
        with tempfile.TemporaryDirectory() as directory:
            checker = self.make_checker(Path(directory))
            def malformed_run():
                checker.target_started = True
                checker.instrument = mock.Mock()
                checker.instrument.poll.return_value = 1
                (checker.out / "instrumentation.log").write_text(
                    "INSTRUMENTATION_CODE: -1\n")
                raise CheckFailure("Instrumentation failed")
            checker.preflight = checker.build = checker.signing = checker.install = lambda: None
            checker.run_tests = malformed_run
            checker.diagnostics = mock.Mock()

            self.assertEqual(checker.run(), 1)
            report = json.loads((checker.out / "report.json").read_text())
            self.assertEqual(report["status"], "failed")
            self.assertFalse(report["tests"]["ok"])
            self.assertTrue((checker.out / "instrumentation.log").is_file())

    def test_timeout_cleanup_only_force_stops_debug_and_keeps_report_and_log(self):
        with tempfile.TemporaryDirectory() as directory:
            checker = self.make_checker(Path(directory))
            def timed_out_run():
                checker.report["status"] = "timeout"
                checker.target_started = True
                checker.instrument = mock.Mock()
                checker.instrument.poll.return_value = None
                (checker.out / "instrumentation.log").write_text(
                    "INSTRUMENTATION_STATUS: numtests=1\n")
                raise CheckFailure("Instrumentation exceeded the timeout")
            checker.preflight = checker.build = checker.signing = checker.install = lambda: None
            checker.run_tests = timed_out_run
            checker.shell = mock.Mock(return_value="")
            checker.diagnostics = mock.Mock()
            with mock.patch.object(device_check.DeviceCheck, "stop"):
                self.assertEqual(checker.run(), 1)

            checker.shell.assert_called_once_with("am", "force-stop", device_check.PACKAGE,
                                                   timeout=10)
            self.assertTrue((checker.out / "instrumentation.log").is_file())
            report_path = checker.out / "report.json"
            self.assertTrue(report_path.is_file())
            self.assertEqual(json.loads(report_path.read_text())["status"], "timeout")

    def test_interruption_cleanup_preserves_log_and_marks_report_interrupted(self):
        with tempfile.TemporaryDirectory() as directory:
            checker = self.make_checker(Path(directory))
            def interrupted_run():
                checker.target_started = True
                checker.instrument = mock.Mock()
                checker.instrument.poll.return_value = None
                (checker.out / "instrumentation.log").write_text("partial output\n")
                raise KeyboardInterrupt
            checker.preflight = checker.build = checker.signing = checker.install = lambda: None
            checker.run_tests = interrupted_run
            checker.shell = mock.Mock(return_value="")
            checker.diagnostics = mock.Mock()
            with mock.patch.object(device_check.DeviceCheck, "stop"):
                self.assertEqual(checker.run(), 130)

            checker.shell.assert_called_once_with("am", "force-stop", device_check.PACKAGE,
                                                   timeout=10)
            self.assertTrue((checker.out / "instrumentation.log").is_file())
            report = json.loads((checker.out / "report.json").read_text())
            self.assertEqual(report["status"], "interrupted")


if __name__ == "__main__":
    unittest.main()
