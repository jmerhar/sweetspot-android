#!/usr/bin/env bash
#
# Install an APK on a specific connected device via ADB.
#
# Usage:
#   ./bin/install.sh phone              # Install release phone APK
#   ./bin/install.sh watch              # Install release watch APK
#   ./bin/install.sh phone --debug      # Install debug phone APK
#   ./bin/install.sh watch --debug      # Install debug watch APK
#
set -euo pipefail

TARGET="${1:?Usage: ./bin/install.sh <phone|watch> [--debug]}"
VARIANT="${2:-release}"

# Find adb: Android SDK default location, then PATH
ADB="${HOME}/Library/Android/sdk/platform-tools/adb"
if [[ ! -x "$ADB" ]]; then
    ADB=$(command -v adb 2>/dev/null || true)
fi
if [[ -z "$ADB" ]]; then
    echo "ERROR: adb not found. Install Android SDK platform-tools or add adb to PATH."
    exit 1
fi

# Determine APK pattern based on target and variant
case "$TARGET" in
    phone)
        if [[ "$VARIANT" == "--debug" ]]; then
            APK_PATTERN="app/build/outputs/apk/debug/app-debug.apk"
        else
            APK_PATTERN="app/build/outputs/apk/release/sweetspot-*.apk"
            APK_FALLBACK="app/build/outputs/apk/release/app-release.apk"
        fi
        # Match any device that is NOT a watch
        LINE=$("$ADB" devices -l | grep -v -i 'watch\|wrist' | grep 'device ' | head -1 || true)
        DEVICE_LABEL="phone"
        ;;
    watch)
        if [[ "$VARIANT" == "--debug" ]]; then
            APK_PATTERN="wear/build/outputs/apk/debug/wear-debug.apk"
        else
            APK_PATTERN="wear/build/outputs/apk/release/sweetspot-wear-*.apk"
            APK_FALLBACK="wear/build/outputs/apk/release/wear-release.apk"
        fi
        LINE=$("$ADB" devices -l | grep -i 'watch\|wrist' | head -1 || true)
        DEVICE_LABEL="watch"
        ;;
    *)
        echo "ERROR: Unknown target '$TARGET'. Use 'phone' or 'watch'."
        exit 1
        ;;
esac

# Parse device serial and model
SERIAL=$(echo "$LINE" | awk '{print $1}')
MODEL=$(echo "$LINE" | sed -n 's/.*model:\([^ ]*\).*/\1/p' | tr '_' ' ')

if [[ -z "$SERIAL" ]]; then
    echo "ERROR: No $DEVICE_LABEL found. Connect it via USB or Wi-Fi debugging first."
    exit 1
fi

# Find the APK (newest by modification time, with fallback to default name)
APK_DIR=$(dirname "$APK_PATTERN")
APK_NAME=$(basename "$APK_PATTERN")
APK=$(find "$APK_DIR" -maxdepth 1 -name "$APK_NAME" -exec stat -f '%m %N' {} + 2>/dev/null | sort -rn | head -1 | cut -d' ' -f2-)
if [[ -z "$APK" && -n "${APK_FALLBACK:-}" && -f "$APK_FALLBACK" ]]; then
    APK="$APK_FALLBACK"
fi
if [[ -z "$APK" ]]; then
    echo "ERROR: No APK found matching $APK_PATTERN. Build first."
    exit 1
fi

echo "Installing $APK on $MODEL..."
"$ADB" -s "$SERIAL" install "$APK"
