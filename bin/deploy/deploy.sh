#!/usr/bin/env bash
# Deploy phone and/or wear AABs with localised release notes to the Play Store.
#
# Reads version codes from Gradle, extracts the latest changelog entry from
# each website translation, writes Fastlane changelog files, and uploads
# AABs via the deploy Fastlane lane.
#
# Usage:
#   ./bin/deploy/deploy.sh                    # Deploy phone to alpha (default; wear skipped on alpha)
#   APP=phone ./bin/deploy/deploy.sh          # Deploy phone only
#   APP=wear TRACK=production ./bin/deploy/deploy.sh   # Deploy Wear OS to production
#   TRACK=production ./bin/deploy/deploy.sh   # Deploy both to production

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
source "$SCRIPT_DIR/../lib/common.sh"

TRACK="${TRACK:-alpha}"
APP="${APP:-both}"

if [[ "$APP" != "phone" && "$APP" != "wear" && "$APP" != "both" ]]; then
    die "APP must be 'phone', 'wear', or 'both' (got '$APP')."
fi

# Wear OS closed testing tracks are not supported by the Play Store, so
# skip wear when deploying to the alpha track.
if [[ "$TRACK" == "alpha" && "$APP" == "wear" ]]; then
    log_info "Wear OS cannot be deployed to the alpha track. Nothing to do."
    exit 0
elif [[ "$TRACK" == "alpha" && "$APP" == "both" ]]; then
    log_info "Skipping Wear OS (closed testing not supported for Wear). Deploying phone only."
    APP="phone"
fi

# ── Read version from Gradle ─────────────────────────────────

log_info "Reading version from Gradle..."
VERSION_NAME=$("$ROOT_DIR/gradlew" -q -p "$ROOT_DIR" app:printVersionName | head -1)
log_info "  Version: $VERSION_NAME"

if [[ "$APP" != "wear" ]]; then
    PHONE_CODE=$("$ROOT_DIR/gradlew" -q -p "$ROOT_DIR" app:printVersionCode | head -1)
    log_info "  Phone:   $PHONE_CODE"
fi

if [[ "$APP" != "phone" ]]; then
    WEAR_CODE=$("$ROOT_DIR/gradlew" -q -p "$ROOT_DIR" wear:printVersionCode | head -1)
    log_info "  Wear:    $WEAR_CODE"
fi

# ── Verify AABs exist ───────────────────────────────────────

PHONE_AAB="$ROOT_DIR/build/sweetspot-phone.aab"
WEAR_AAB="$ROOT_DIR/build/sweetspot-wear.aab"

if [[ "$APP" != "wear" && ! -f "$PHONE_AAB" ]]; then
    die "Phone AAB not found at $PHONE_AAB.
  Run 'make bundle' or 'make release' first."
fi

if [[ "$APP" != "phone" && ! -f "$WEAR_AAB" ]]; then
    die "Wear AAB not found at $WEAR_AAB.
  Run 'make bundle' or 'make release' first."
fi

# ── Extract changelogs from website content ─────────────────

log_info "Extracting changelogs..."
LOCALE_COUNT=0

for changelog in "$ROOT_DIR"/site/content/*/changelog.md; do
    lang=$(basename "$(dirname "$changelog")")
    locale=$(website_to_metadata "$lang")

    # Extract version from first {{< changelog version="..." >}} line
    changelog_version=$(awk '/^{{< changelog /{gsub(/.*version="/, ""); gsub(/".*/, ""); print; exit}' "$changelog")

    if [[ "$changelog_version" != "$VERSION_NAME" ]]; then
        die "$lang changelog version \"$changelog_version\" does not match app version \"$VERSION_NAME\".
  Update site/content/$lang/changelog.md before deploying."
    fi

    # Extract text between first {{< changelog ... >}} and {{< /changelog >}}
    text=$(awk '/^{{< changelog /{found++; next} found==1 && /^{{< \/changelog >}}/{exit} found==1{print}' "$changelog")

    if [[ -z "$text" ]]; then
        log_warn "no changelog entry found for $lang, skipping"
        continue
    fi

    # Check the Play Store 500-character limit BEFORE writing, so a too-long
    # entry aborts without leaving a stray changelog file behind. Count Unicode
    # code points via python3 (shell ${#var} counts bytes under a C locale, which
    # would over-count accented languages and false-reject a valid entry).
    char_count=$(printf '%s' "$text" | python3 -c 'import sys; print(len(sys.stdin.read()))')
    if (( char_count > 500 )); then
        die "$locale changelog is $char_count chars (limit: 500).
  Shorten site/content/$lang/changelog.md before deploying."
    fi

    # Write changelog for relevant version codes
    dir="$ROOT_DIR/fastlane/metadata/android/$locale/changelogs"
    mkdir -p "$dir"
    # Write without a trailing newline: Play counts it against the 500-char release-notes
    # limit, so `echo` (which appends one) would push a 500-char entry to 501 and be rejected.
    if [[ "$APP" != "wear" ]]; then
        printf '%s' "$text" > "$dir/${PHONE_CODE}.txt"
    fi
    if [[ "$APP" != "phone" ]]; then
        printf '%s' "$text" > "$dir/${WEAR_CODE}.txt"
    fi

    (( LOCALE_COUNT++ )) || true
done

log_info "  Wrote changelogs for $LOCALE_COUNT locales"

# ── Deploy via Fastlane ─────────────────────────────────────

echo ""
log_info "Deploying $APP to $TRACK..."
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
log_success "Deploy complete:"
log_info "  App:         $APP"
log_info "  Track:       $TRACK"
if [[ "$APP" != "wear" ]]; then
    log_info "  Phone code:  $PHONE_CODE"
fi
if [[ "$APP" != "phone" ]]; then
    log_info "  Wear code:   $WEAR_CODE"
fi
log_info "  Changelogs:  $LOCALE_COUNT locales"
