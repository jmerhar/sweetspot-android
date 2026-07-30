#!/usr/bin/env python3

"""Unit tests for bin/quality/coverage-report.py (stdlib unittest, no network).

This script is the CI coverage gate, so its parsing and threshold math are worth locking down.
Tests build fixture Kover XML reports in a temp dir and exercise the real code paths (XML parse,
percentage math, the rounding tolerance, and the pass/fail gate). Run:
  python3 bin/quality/test_coverage_report.py   (or: make test-coverage-report)
"""

import importlib.util
import os
import tempfile
import unittest

_SPEC = importlib.util.spec_from_file_location(
    "coverage_report", os.path.join(os.path.dirname(os.path.abspath(__file__)), "coverage-report.py"))
cr = importlib.util.module_from_spec(_SPEC)
_SPEC.loader.exec_module(cr)


def _write_report(module, line, branch=(9, 1), instruction=(98, 2)):
    """Write a fixture Kover reportDebug.xml for `module` at the path the script reads."""
    d = os.path.join(module, "build", "reports", "kover")
    os.makedirs(d, exist_ok=True)
    counters = "".join(
        f'<counter type="{t}" covered="{c}" missed="{m}"/>'
        for t, (c, m) in (("LINE", line), ("BRANCH", branch), ("INSTRUCTION", instruction))
    )
    with open(os.path.join(d, "reportDebug.xml"), "w", encoding="utf-8") as f:
        f.write(f"<report>{counters}</report>")


class CoverageReportTest(unittest.TestCase):
    def setUp(self):
        self._cwd = os.getcwd()
        self._tmp = tempfile.TemporaryDirectory()
        os.chdir(self._tmp.name)

    def tearDown(self):
        os.chdir(self._cwd)
        self._tmp.cleanup()

    def test_percentages_and_line_percent(self):
        _write_report("shared", line=(98, 2))
        self.assertEqual(cr.percentages("shared")["LINE"], "98.0%")
        self.assertEqual(cr.line_percent("shared"), 98.0)

    def test_missing_report_is_none(self):
        self.assertIsNone(cr.percentages("app"))
        self.assertIsNone(cr.line_percent("app"))

    def test_malformed_xml_is_none(self):
        d = os.path.join("wear", "build", "reports", "kover")
        os.makedirs(d, exist_ok=True)
        with open(os.path.join(d, "reportDebug.xml"), "w", encoding="utf-8") as f:
            f.write("<not-closed")
        self.assertIsNone(cr.percentages("wear"))

    def test_zero_total_is_na(self):
        _write_report("shared", line=(0, 0))
        self.assertEqual(cr.percentages("shared")["LINE"], "n/a")
        self.assertIsNone(cr.line_percent("shared"))

    def test_total_metrics_combines_modules(self):
        _write_report("shared", line=(90, 10))   # 90/100
        _write_report("app", line=(60, 40))       # 60/100
        # Combined line = 150/200 = 75.0%
        self.assertEqual(cr.total_metrics()["line"], "75.0%")

    def test_gate_passes_when_all_modules_meet_threshold(self):
        _write_report("shared", line=(99, 1))   # 99.0 >= 98
        _write_report("app", line=(98, 2))      # 98.0 >= 97
        _write_report("wear", line=(95, 5))     # 95.0 >= 93
        self.assertEqual(cr.run_gate(), 0)

    def test_gate_fails_when_a_module_is_below(self):
        _write_report("shared", line=(99, 1))
        _write_report("app", line=(98, 2))
        _write_report("wear", line=(90, 10))    # 90.0 < 93
        self.assertEqual(cr.run_gate(), 1)

    def test_gate_fails_when_a_report_is_missing(self):
        _write_report("shared", line=(99, 1))
        _write_report("app", line=(98, 2))
        # wear report absent
        self.assertEqual(cr.run_gate(), 1)

    def test_rounding_tolerance_lets_a_value_that_rounds_to_the_bound_pass(self):
        _write_report("shared", line=(99, 1))
        _write_report("app", line=(2424, 76))   # 96.96% -> formats "97.0%" -> passes the 97 gate
        _write_report("wear", line=(95, 5))
        self.assertEqual(cr.run_gate(), 0)

    def test_render_markdown_and_reports_smoke(self):
        _write_report("shared", line=(98, 2))
        md = cr.render_markdown()
        self.assertIn("`:shared`", md)
        self.assertIn("98.0%", md)
        reports = cr.render_reports()
        self.assertIn('"total"', reports)
        self.assertIn('"shared"', reports)


if __name__ == "__main__":
    unittest.main(verbosity=2)
