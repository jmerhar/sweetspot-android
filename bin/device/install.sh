#!/usr/bin/env bash
#
# Install an APK on a specific connected device via ADB.
#
# Usage:
#   ./bin/device/install.sh phone              # Install release phone APK
#   ./bin/device/install.sh watch              # Install release watch APK
#   ./bin/device/install.sh phone --debug      # Install debug phone APK
#   ./bin/device/install.sh watch --debug      # Install debug watch APK
#
set -euo pipefail
source "$(dirname "$0")/../lib/log.sh"

TARGET="${1:?Usage: ./bin/device/install.sh <phone|watch> [--debug]}"
VARIANT="${2:-release}"

# Find adb: Android SDK default location, then PATH
ADB="${HOME}/Library/Android/sdk/platform-tools/adb"
if [[ ! -x "$ADB" ]]; then
    ADB=$(command -v adb 2>/dev/null || true)
fi
if [[ -z "$ADB" ]]; then
    die "adb not found. Install Android SDK platform-tools or add adb to PATH."
fi

# Determine APK pattern based on target and variant
case "$TARGET" in
    phone)
        if [[ "$VARIANT" == "--debug" ]]; then
            APK_GLOB="app/build/outputs/apk/debug/sweetspot-*.apk"
        else
            APK_GLOB="app/build/outputs/apk/release/sweetspot-*.apk"
        fi
        # Match any device that is NOT a watch
        LINE=$("$ADB" devices -l | grep -v -i 'watch\|wrist' | grep 'device ' | head -1 || true)
        DEVICE_LABEL="phone"
        ;;
    watch)
        if [[ "$VARIANT" == "--debug" ]]; then
            APK_GLOB="wear/build/outputs/apk/debug/sweetspot-wear-*.apk"
        else
            APK_GLOB="wear/build/outputs/apk/release/sweetspot-wear-*.apk"
        fi
        LINE=$("$ADB" devices -l | grep -i 'watch\|wrist' | head -1 || true)
        DEVICE_LABEL="watch"
        ;;
    *)
        die "Unknown target '$TARGET'. Use 'phone' or 'watch'."
        ;;
esac

# Parse device serial and model
SERIAL=$(echo "$LINE" | awk '{print $1}')
MODEL=$(echo "$LINE" | sed -n 's/.*model:\([^ ]*\).*/\1/p' | tr '_' ' ')

if [[ -z "$SERIAL" ]]; then
    die "No $DEVICE_LABEL found. Connect it via USB or Wi-Fi debugging first."
fi

# Find the newest matching APK. `ls -t` is portable across macOS and Linux
# (BSD `stat -f` / GNU `find -printf` are not), and archivesName names every
# variant sweetspot-*.apk, so the glob always matches a real build output.
# shellcheck disable=SC2086
APK=$(ls -t $APK_GLOB 2>/dev/null | head -1)
if [[ -z "$APK" ]]; then
    die "No APK found matching $APK_GLOB. Build first."
fi

log_info "Installing $APK on $MODEL..."
"$ADB" -s "$SERIAL" install "$APK"
