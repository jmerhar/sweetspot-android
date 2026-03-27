#!/usr/bin/env bash

# inspect.sh
# Summarises Android Studio inspection results exported to inspect/xml/.
#
# Workflow:
#   1. In Android Studio: Code → Inspect Code (whole project, default profile)
#   2. In the results panel, click the export button (↑)
#   3. Choose inspect/xml/ as the destination
#   4. Run: make inspect

set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
XML_DIR="$PROJECT_DIR/inspect/xml"

if [[ ! -d "$XML_DIR" ]]; then
    mkdir -p "$XML_DIR"
fi

# Remove metadata files that aren't inspection results.
rm -f "$XML_DIR/.descriptions.xml"

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
