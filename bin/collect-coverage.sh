#!/usr/bin/env bash
# Collect the per-module Kover HTML reports + a reports.json manifest into <out>, ready to publish
# to the jmerhar/coverage site via that repo's bin/add-report.sh. The site itself (indexes,
# cross-linking) is built in the coverage repo — this only assembles this project's raw report.
#
# Usage: bin/collect-coverage.sh [output-dir]   (default: coverage-upload)
# Run from the repo root after `./gradlew koverHtmlReportDebug koverXmlReportDebug`.
set -euo pipefail

out="${1:-coverage-upload}"
here="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

rm -rf "$out"
mkdir -p "$out"
for module in shared app wear; do
    cp -r "$module/build/reports/kover/htmlDebug" "$out/$module"
done
python3 "$here/coverage-report.py" --format reports > "$out/reports.json"

echo "Collected coverage upload in $out/ (modules: shared, app, wear)"
