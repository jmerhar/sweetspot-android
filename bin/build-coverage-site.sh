#!/usr/bin/env bash
# Assemble the per-module Kover HTML reports into a single directory for publishing to the
# shared jmerhar/coverage GitHub Pages site. Copies each module's htmlDebug report into
# <out>/<module>/ and generates <out>/index.html (a landing page linking the modules with their
# coverage %, via bin/coverage-report.py).
#
# Usage: bin/build-coverage-site.sh [output-dir]   (default: coverage-site)
# Run from the repo root after `./gradlew koverHtmlReportDebug koverXmlReportDebug`.
set -euo pipefail

out="${1:-coverage-site}"
here="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

rm -rf "$out"
mkdir -p "$out"
for module in shared app wear; do
    cp -r "$module/build/reports/kover/htmlDebug" "$out/$module"
done
python3 "$here/coverage-report.py" --format html > "$out/index.html"

echo "Assembled coverage site in $out/ (modules: shared, app, wear)"
