#!/usr/bin/env python3
"""Summarise per-module Kover coverage for CI.

Reads each module's Kover XML report (``<module>/build/reports/kover/reportDebug.xml``) and prints,
depending on ``--format``:

  --format md         GitHub-flavored Markdown table, for the Actions run summary
                      ($GITHUB_STEP_SUMMARY).
  --format reports    A JSON "reports" array (the manifest consumed by the jmerhar/coverage site's
                      bin/make-meta.py): one entry per module with its line/branch coverage. The
                      site build itself lives in the coverage repo — this only emits the numbers.

With ``--gate`` it instead checks each module's LINE coverage against the per-module thresholds in
``GATES`` and exits non-zero if any module is below its bound (or has no report). This is the CI
coverage gate. It reads the Kover XML reports rather than using ``koverVerifyDebug`` because Kover
0.9.8's verification task does not reliably apply wildcard ``classes(...)`` excludes (e.g.
``today.sweetspot.ui.*``) — behaviour that also differs by JDK — whereas the XML reports apply them
correctly and consistently.

Modules whose report is missing are skipped (for the summaries) or fail the gate. Run from the repo
root after ``./gradlew koverXmlReportDebug``.
"""
import argparse
import json
import xml.etree.ElementTree as ET

MODULES = ("shared", "app", "wear")

# CI line-coverage gate per module (percent). Each bound sits ~2 points under the module's actual
# number — tight enough to catch a real regression while tolerating defensive/DI/edge lines that
# survive the presentation exclusions. Single source of truth for the gate.
GATES = {"shared": 98.0, "app": 97.0, "wear": 93.0}


def percentages(module):
    """Return {LINE, INSTRUCTION, BRANCH} coverage strings for a module, or None if no report."""
    path = f"{module}/build/reports/kover/reportDebug.xml"
    try:
        counters = {c.get("type"): c for c in ET.parse(path).getroot().findall("counter")}
    except (FileNotFoundError, ET.ParseError):
        return None

    def pct(kind):
        c = counters.get(kind)
        if c is None:
            return "n/a"
        covered, missed = int(c.get("covered")), int(c.get("missed"))
        total = covered + missed
        return f"{covered / total * 100:.1f}%" if total else "n/a"

    return {kind: pct(kind) for kind in ("LINE", "INSTRUCTION", "BRANCH")}


def _rows():
    """Yield (module, stats-dict) for every module that has a report."""
    for module in MODULES:
        stats = percentages(module)
        if stats is not None:
            yield module, stats


def render_markdown():
    lines = ["## Coverage (debug unit tests)", "", "| Module | Line | Instruction | Branch |", "|---|---|---|---|"]
    for module, s in _rows():
        lines.append(f"| `:{module}` | {s['LINE']} | {s['INSTRUCTION']} | {s['BRANCH']} |")
    return "\n".join(lines)


def render_reports():
    """The manifest for the coverage site: one entry per module, path = module subdirectory."""
    reports = [
        {"name": module, "path": module, "metrics": {"line": s["LINE"], "branch": s["BRANCH"]}}
        for module, s in _rows()
    ]
    return json.dumps(reports, indent=2)


def line_percent(module):
    """Return a module's LINE coverage as a float percent, or None if there is no report."""
    stats = percentages(module)
    if stats is None or stats["LINE"] == "n/a":
        return None
    return float(stats["LINE"].rstrip("%"))


def run_gate():
    """Check each module's LINE coverage against GATES. Returns a process exit code (0 = all pass)."""
    failures = []
    for module, threshold in GATES.items():
        pct = line_percent(module)
        if pct is None:
            print(f"✗ :{module}  no coverage report found")
            failures.append(module)
            continue
        # +0.05 tolerance so a value that rounds to the bound (report shows one decimal) still passes.
        ok = pct + 0.05 >= threshold
        print(f"{'✓' if ok else '✗'} :{module}  line {pct:.1f}%  (gate ≥ {threshold:.0f}%)")
        if not ok:
            failures.append(module)
    if failures:
        print(f"\nCoverage gate FAILED for: {', '.join(':' + m for m in failures)}")
        return 1
    print("\nAll module coverage gates passed.")
    return 0


def main():
    parser = argparse.ArgumentParser(description="Summarise or gate per-module Kover coverage.")
    parser.add_argument("--format", choices=("md", "reports"), default="md")
    parser.add_argument(
        "--gate",
        action="store_true",
        help="Check per-module LINE coverage against GATES; exit non-zero on any failure.",
    )
    args = parser.parse_args()
    if args.gate:
        raise SystemExit(run_gate())
    print(render_markdown() if args.format == "md" else render_reports())


if __name__ == "__main__":
    main()
