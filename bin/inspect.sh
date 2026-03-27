#!/usr/bin/env bash

# inspect.sh
# Summarises Android Studio inspection results exported to inspect/xml/.
#
# Workflow:
#   1. In Android Studio: Code → Inspect Code (whole project, default profile)
#   2. Export results to inspect/xml/ (overwrite existing files)
#   3. Run: make inspect

set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
XML_DIR="$PROJECT_DIR/inspect/xml"

# Metadata files and headless-only false positives to exclude (basenames without .xml).
IGNORED=".descriptions"

if [[ ! -d "$XML_DIR" ]]; then
    mkdir -p "$XML_DIR"
fi

# --- Remove ignored files ---
for name in $IGNORED; do
    rm -f "$XML_DIR/$name.xml"
done

# --- Check for exported results ---
ISSUE_FILES=("$XML_DIR"/*.xml)
if [[ ! -e "${ISSUE_FILES[0]}" ]]; then
    echo "No inspection XML files found in inspect/xml/."
    echo ""
    echo "To export from Android Studio:"
    echo "  1. Code → Inspect Code (whole project, default profile)"
    echo "  2. In the results panel, click the export button (↑)"
    echo "  3. Choose inspect/xml/ as the destination"
    echo "  4. Run: make inspect"
    exit 1
fi

TOTAL=0
for f in "${ISSUE_FILES[@]}"; do
    count=$(grep -c '<problem>' "$f" || true)
    TOTAL=$((TOTAL + count))
done

echo "Found ${#ISSUE_FILES[@]} inspection(s) with $TOTAL total issue(s):"
echo ""
for f in "${ISSUE_FILES[@]}"; do
    count=$(grep -c '<problem>' "$f" || true)
    name=$(basename "$f" .xml)
    printf "  %4d  %s\n" "$count" "$name"
done
exit 1
