#!/usr/bin/env bash
# Deploy phone and wear AABs with localised release notes to the Play Store.
#
# Reads version codes from Gradle, extracts the latest changelog entry from
# each website translation, writes Fastlane changelog files, and uploads
# both AABs via the deploy Fastlane lane.
#
# Usage:
#   ./bin/deploy.sh                    # Deploy to alpha (default)
#   TRACK=production ./bin/deploy.sh   # Deploy to production

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
source "$SCRIPT_DIR/lib/common.sh"

TRACK="${TRACK:-alpha}"

# ── Read version from Gradle ─────────────────────────────────

echo "Reading version from Gradle..."
PHONE_CODE=$("$ROOT_DIR/gradlew" -q -p "$ROOT_DIR" app:printVersionCode)
WEAR_CODE=$("$ROOT_DIR/gradlew" -q -p "$ROOT_DIR" wear:printVersionCode)
VERSION_NAME=$("$ROOT_DIR/gradlew" -q -p "$ROOT_DIR" app:printVersionName)
echo "  Version: $VERSION_NAME"
echo "  Phone:   $PHONE_CODE"
echo "  Wear:    $WEAR_CODE"

# ── Verify AABs exist ───────────────────────────────────────

PHONE_AAB="$ROOT_DIR/build/sweetspot-phone.aab"
WEAR_AAB="$ROOT_DIR/build/sweetspot-wear.aab"

if [[ ! -f "$PHONE_AAB" || ! -f "$WEAR_AAB" ]]; then
    echo "Error: AABs not found in build/." >&2
    echo "  Run 'make bundle' or 'make release' first." >&2
    exit 1
fi

# ── Extract changelogs from website content ─────────────────

echo "Extracting changelogs..."
LOCALE_COUNT=0

for changelog in "$ROOT_DIR"/site/content/*/changelog.md; do
    lang=$(basename "$(dirname "$changelog")")
    locale=$(website_to_metadata "$lang")

    # Extract version from first {{< changelog version="..." >}} line
    changelog_version=$(awk '/^{{< changelog /{gsub(/.*version="/, ""); gsub(/".*/, ""); print; exit}' "$changelog")

    if [[ "$changelog_version" != "$VERSION_NAME" ]]; then
        echo "Error: $lang changelog version \"$changelog_version\" does not match app version \"$VERSION_NAME\"." >&2
        echo "  Update site/content/$lang/changelog.md before deploying." >&2
        exit 1
    fi

    # Extract text between first {{< changelog ... >}} and {{< /changelog >}}
    text=$(awk '/^{{< changelog /{found++; next} found==1 && /^{{< \/changelog >}}/{exit} found==1{print}' "$changelog")

    if [[ -z "$text" ]]; then
        echo "  Warning: no changelog entry found for $lang, skipping"
        continue
    fi

    # Write changelog for both phone and wear version codes
    dir="$ROOT_DIR/fastlane/metadata/android/$locale/changelogs"
    mkdir -p "$dir"
    echo "$text" > "$dir/${PHONE_CODE}.txt"
    cp "$dir/${PHONE_CODE}.txt" "$dir/${WEAR_CODE}.txt"

    # Check Play Store 500-character limit
    char_count=${#text}
    if (( char_count > 500 )); then
        echo "Error: $locale changelog is $char_count chars (limit: 500)." >&2
        echo "  Shorten site/content/$lang/changelog.md before deploying." >&2
        exit 1
    fi

    (( LOCALE_COUNT++ )) || true
done

echo "  Wrote changelogs for $LOCALE_COUNT locales"

# ── Deploy via Fastlane ─────────────────────────────────────

echo ""
echo "Deploying to $TRACK..."
cd "$ROOT_DIR"
bundle exec fastlane deploy track:"$TRACK" phone_code:"$PHONE_CODE" wear_code:"$WEAR_CODE"

# ── Summary ─────────────────────────────────────────────────

echo ""
echo "Deploy complete:"
echo "  Track:       $TRACK"
echo "  Phone code:  $PHONE_CODE"
echo "  Wear code:   $WEAR_CODE"
echo "  Changelogs:  $LOCALE_COUNT locales"
