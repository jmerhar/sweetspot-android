#!/usr/bin/env python3
"""Summarise per-module Kover coverage for CI.

Reads each module's Kover XML report (``<module>/build/reports/kover/reportDebug.xml``) and prints
a per-module coverage table in one of two formats:

  --format md    GitHub-flavored Markdown, for the Actions run summary ($GITHUB_STEP_SUMMARY).
  --format html  A standalone HTML landing page, for the GitHub Pages site (links each module).

Modules whose report is missing are skipped. Run from the repo root after
``./gradlew koverXmlReportDebug`` (and, for the HTML links to resolve, ``koverHtmlReportDebug``).
"""
import argparse
import xml.etree.ElementTree as ET
from datetime import datetime, timezone

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


def render_html():
    rows = "".join(
        '<tr><td><a href="%s/index.html">:%s</a></td><td>%s</td><td>%s</td></tr>'
        % (module, module, s["LINE"], s["BRANCH"])
        for module, s in _rows()
    )
    ts = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M UTC")
    return (
        "<!doctype html><html lang=en><meta charset=utf-8>"
        "<title>sweetspot-android coverage</title>"
        "<style>body{font-family:system-ui,sans-serif;max-width:640px;margin:3rem auto;padding:0 1rem}"
        "table{border-collapse:collapse;width:100%}th,td{text-align:left;padding:.5rem .75rem;border-bottom:1px solid #ddd}"
        "a{color:#4A90D9}small{color:#666}</style>"
        "<h1>sweetspot-android — Kover coverage</h1>"
        "<p>Per-module line-by-line reports (debug unit tests).</p>"
        "<table><tr><th>Module</th><th>Line</th><th>Branch</th></tr>" + rows + "</table>"
        "<p><small>Generated " + ts + "</small></p>"
    )


def main():
    parser = argparse.ArgumentParser(description="Print a per-module Kover coverage summary.")
    parser.add_argument("--format", choices=("md", "html"), default="md")
    args = parser.parse_args()
    print(render_markdown() if args.format == "md" else render_html())


if __name__ == "__main__":
    main()
