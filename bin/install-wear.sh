#!/usr/bin/env bash
#
# Install the latest release wear APK on a connected watch via ADB.
#
# Usage:
#   ./bin/install-wear.sh
#
set -euo pipefail

# Find adb: Android SDK default location, then PATH
ADB="${HOME}/Library/Android/sdk/platform-tools/adb"
if [[ ! -x "$ADB" ]]; then
    ADB=$(command -v adb 2>/dev/null || true)
fi
if [[ -z "$ADB" ]]; then
    echo "ERROR: adb not found. Install Android SDK platform-tools or add adb to PATH."
    exit 1
fi

# Find the watch
LINE=$("$ADB" devices -l | grep -i 'watch\|wrist' | head -1 || true)
WATCH=$(echo "$LINE" | awk '{print $1}')
MODEL=$(echo "$LINE" | sed -n 's/.*model:\([^ ]*\).*/\1/p' | tr '_' ' ')

if [[ -z "$WATCH" ]]; then
    echo "ERROR: No watch found. Connect via Wi-Fi debugging first."
    exit 1
fi

# Find the latest release APK
APK=$(ls -t wear/build/outputs/apk/release/sweetspot-wear-*.apk 2>/dev/null | head -1)
if [[ -z "$APK" ]]; then
    echo "ERROR: No release APK found. Run 'make release' first."
    exit 1
fi

echo "Installing $APK on $MODEL..."
"$ADB" -s "$WATCH" install "$APK"
