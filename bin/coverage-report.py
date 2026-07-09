#!/usr/bin/env python3
"""Summarise per-module Kover coverage for CI.

Reads each module's Kover XML report (``<module>/build/reports/kover/reportDebug.xml``) and prints,
depending on ``--format``:

  --format md         GitHub-flavored Markdown table, for the Actions run summary
                      ($GITHUB_STEP_SUMMARY).
  --format reports    A JSON "reports" array (the manifest consumed by the jmerhar/coverage site's
                      bin/make-meta.py): one entry per module with its line/branch coverage. The
                      site build itself lives in the coverage repo — this only emits the numbers.

Modules whose report is missing are skipped. Run from the repo root after
``./gradlew koverXmlReportDebug``.
"""
import argparse
import json
import xml.etree.ElementTree as ET

MODULES = ("shared", "app", "wear")


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


def main():
    parser = argparse.ArgumentParser(description="Print a per-module Kover coverage summary.")
    parser.add_argument("--format", choices=("md", "reports"), default="md")
    args = parser.parse_args()
    print(render_markdown() if args.format == "md" else render_reports())


if __name__ == "__main__":
    main()
