#!/usr/bin/env bash
# Deploy phone and/or wear AABs with localised release notes to the Play Store.
#
# Reads version codes from Gradle, extracts the latest changelog entry from
# each website translation, writes Fastlane changelog files, and uploads
# AABs via the deploy Fastlane lane.
#
# Usage:
#   ./bin/deploy.sh                    # Deploy phone to alpha (default; wear skipped on alpha)
#   APP=phone ./bin/deploy.sh          # Deploy phone only
#   APP=wear TRACK=production ./bin/deploy.sh   # Deploy Wear OS to production
#   TRACK=production ./bin/deploy.sh   # Deploy both to production

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
source "$SCRIPT_DIR/lib/common.sh"

TRACK="${TRACK:-alpha}"
APP="${APP:-both}"

if [[ "$APP" != "phone" && "$APP" != "wear" && "$APP" != "both" ]]; then
    echo "Error: APP must be 'phone', 'wear', or 'both' (got '$APP')." >&2
    exit 1
fi

# Wear OS closed testing tracks are not supported by the Play Store, so
# skip wear when deploying to the alpha track.
if [[ "$TRACK" == "alpha" && "$APP" == "wear" ]]; then
    echo "Wear OS cannot be deployed to the alpha track. Nothing to do."
    exit 0
elif [[ "$TRACK" == "alpha" && "$APP" == "both" ]]; then
    echo "Skipping Wear OS (closed testing not supported for Wear). Deploying phone only."
    APP="phone"
fi

# ── Read version from Gradle ─────────────────────────────────

echo "Reading version from Gradle..."
VERSION_NAME=$("$ROOT_DIR/gradlew" -q -p "$ROOT_DIR" app:printVersionName | head -1)
echo "  Version: $VERSION_NAME"

if [[ "$APP" != "wear" ]]; then
    PHONE_CODE=$("$ROOT_DIR/gradlew" -q -p "$ROOT_DIR" app:printVersionCode | head -1)
    echo "  Phone:   $PHONE_CODE"
fi

if [[ "$APP" != "phone" ]]; then
    WEAR_CODE=$("$ROOT_DIR/gradlew" -q -p "$ROOT_DIR" wear:printVersionCode | head -1)
    echo "  Wear:    $WEAR_CODE"
fi

# ── Verify AABs exist ───────────────────────────────────────

PHONE_AAB="$ROOT_DIR/build/sweetspot-phone.aab"
WEAR_AAB="$ROOT_DIR/build/sweetspot-wear.aab"

if [[ "$APP" != "wear" && ! -f "$PHONE_AAB" ]]; then
    echo "Error: Phone AAB not found at $PHONE_AAB." >&2
    echo "  Run 'make bundle' or 'make release' first." >&2
    exit 1
fi

if [[ "$APP" != "phone" && ! -f "$WEAR_AAB" ]]; then
    echo "Error: Wear AAB not found at $WEAR_AAB." >&2
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

    # Write changelog for relevant version codes
    dir="$ROOT_DIR/fastlane/metadata/android/$locale/changelogs"
    mkdir -p "$dir"
    if [[ "$APP" != "wear" ]]; then
        echo "$text" > "$dir/${PHONE_CODE}.txt"
    fi
    if [[ "$APP" != "phone" ]]; then
        echo "$text" > "$dir/${WEAR_CODE}.txt"
    fi

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
echo "Deploying $APP to $TRACK..."
cd "$ROOT_DIR"

FASTLANE_ARGS="track:$TRACK"
if [[ "$APP" != "wear" ]]; then
    FASTLANE_ARGS="$FASTLANE_ARGS phone_code:$PHONE_CODE"
fi
if [[ "$APP" != "phone" ]]; then
    FASTLANE_ARGS="$FASTLANE_ARGS wear_code:$WEAR_CODE"
fi
if [[ "$APP" == "phone" ]]; then
    FASTLANE_ARGS="$FASTLANE_ARGS skip_wear:true"
elif [[ "$APP" == "wear" ]]; then
    FASTLANE_ARGS="$FASTLANE_ARGS skip_phone:true"
fi

# shellcheck disable=SC2086
bundle exec fastlane deploy $FASTLANE_ARGS

# ── Summary ─────────────────────────────────────────────────

echo ""
echo "Deploy complete:"
echo "  App:         $APP"
echo "  Track:       $TRACK"
if [[ "$APP" != "wear" ]]; then
    echo "  Phone code:  $PHONE_CODE"
fi
if [[ "$APP" != "phone" ]]; then
    echo "  Wear code:   $WEAR_CODE"
fi
echo "  Changelogs:  $LOCALE_COUNT locales"
