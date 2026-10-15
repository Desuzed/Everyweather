#!/usr/bin/env python3
"""Regression tests for Android CI SDK bootstrap ordering and pinning."""

import re
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
WORKFLOW = ROOT / ".github" / "workflows" / "android.yml"
SHA = re.compile(r"^[0-9a-f]{40}$")


def _job_lines(workflow, job):
    """Return the indented YAML lines belonging to one top-level job."""
    lines = workflow.splitlines()
    start = next(index for index, line in enumerate(lines)
                 if line == f"  {job}:")
    end = len(lines)
    for index in range(start + 1, len(lines)):
        if re.match(r"^  [A-Za-z0-9_-]+:$", lines[index]):
            end = index
            break
    return lines[start:end]


def _assert_sdk_bootstrap(test_case, job_lines):
    setup = [
        (index, line)
        for index, line in enumerate(job_lines)
        if "uses: android-actions/setup-android@" in line
    ]
    sdkmanager = [
        index for index, line in enumerate(job_lines)
        if "sdkmanager" in line
    ]

    test_case.assertTrue(sdkmanager, "job must install SDK packages with sdkmanager")
    test_case.assertTrue(
        setup,
        "job must initialize the Android SDK with android-actions/setup-android",
    )
    setup_index, setup_line = setup[0]
    test_case.assertLess(
        setup_index,
        sdkmanager[0],
        "setup-android must run before the first sdkmanager command",
    )
    reference = setup_line.split("android-actions/setup-android@", 1)[1].split()[0]
    test_case.assertRegex(
        reference,
        SHA,
        "setup-android must be pinned to a full commit SHA",
    )


class AndroidCiSdkBootstrapTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.workflow = WORKFLOW.read_text(encoding="utf-8")

    def test_checks_job_bootstraps_android_sdk_before_sdkmanager(self):
        _assert_sdk_bootstrap(self, _job_lines(self.workflow, "checks"))

    def test_ui_job_bootstraps_android_sdk_before_sdkmanager(self):
        _assert_sdk_bootstrap(self, _job_lines(self.workflow, "ui"))


if __name__ == "__main__":
    unittest.main()
