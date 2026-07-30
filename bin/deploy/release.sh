#!/usr/bin/env bash
#
# Build a signed release APK and create a GitHub Release.
#
# Usage:
#   ./release.sh 1.1 -n notes.md          # release with notes from file
#   ./release.sh 1.1 -n notes.md --draft   # same but creates a draft release
#
set -euo pipefail
source "$(dirname "$0")/../lib/log.sh"

# --- Pre-flight checks ---

BRANCH=$(git rev-parse --abbrev-ref HEAD)
if [[ "$BRANCH" != "main" ]]; then
    die "Must be on the main branch to release (currently on '$BRANCH')."
fi

if ! git diff --quiet || ! git diff --cached --quiet; then
    die "Working tree has uncommitted changes. Commit or stash them first."
fi

# Portable in-place sed (macOS needs '' after -i, GNU sed does not)
sedi() {
    if sed --version >/dev/null 2>&1; then
        sed -i "$@"
    else
        sed -i '' "$@"
    fi
}

VERSION="${1:?Usage: ./release.sh <version> -n <notes-file> [--draft]}"
shift

NOTES_FILE=""
DRAFT_FLAG=""
while [[ $# -gt 0 ]]; do
    case "$1" in
        -n) NOTES_FILE="${2:?-n requires a file path}"; shift 2 ;;
        --draft) DRAFT_FLAG="--draft"; shift ;;
        *) die "Unknown option: $1" ;;
    esac
done

if [[ -z "$NOTES_FILE" ]]; then
    die "Release notes file is required. Usage: ./release.sh <version> -n <notes-file> [--draft]"
fi

if [[ ! -f "$NOTES_FILE" ]]; then
    die "Notes file not found: $NOTES_FILE"
fi

CONVENTION_FILE="buildSrc/src/main/kotlin/sweetspot-app.gradle.kts"
TAG="v${VERSION}"

# --- Bump version ---

# Read current versionCode and increment
CURRENT_CODE=$(sed -n 's/.*versionCode = \([0-9]*\).*/\1/p' "$CONVENTION_FILE")
NEW_CODE=$((CURRENT_CODE + 1))

log_info "Bumping versionCode $CURRENT_CODE → $NEW_CODE, versionName → $VERSION"

sedi "s/versionCode = $CURRENT_CODE/versionCode = $NEW_CODE/" "$CONVENTION_FILE"
sedi "s/versionName = \".*\"/versionName = \"$VERSION\"/" "$CONVENTION_FILE"

# --- Build release APK ---

log_info "Building release APK and AAB..."
./gradlew assembleRelease bundleRelease

APK_PATH="app/build/outputs/apk/release/sweetspot-release.apk"
if [[ ! -f "$APK_PATH" ]]; then
    die "Release APK not found at $APK_PATH"
fi

WEAR_APK_PATH="wear/build/outputs/apk/release/sweetspot-wear-release.apk"
if [[ ! -f "$WEAR_APK_PATH" ]]; then
    die "Wear APK not found at $WEAR_APK_PATH"
fi

# Rename APKs to include version
NAMED_APK="app/build/outputs/apk/release/sweetspot-${VERSION}.apk"
cp "$APK_PATH" "$NAMED_APK"

NAMED_WEAR_APK="wear/build/outputs/apk/release/sweetspot-wear-${VERSION}.apk"
cp "$WEAR_APK_PATH" "$NAMED_WEAR_APK"

# Copy AABs to build/ for easy access
mkdir -p build
cp app/build/outputs/bundle/release/sweetspot-release.aab "build/sweetspot-phone.aab"
cp wear/build/outputs/bundle/release/sweetspot-wear-release.aab "build/sweetspot-wear.aab"

# --- Commit and tag ---

git add "$CONVENTION_FILE"
git commit -m "chore: release v${VERSION}"
git tag -a "$TAG" -m "Release ${VERSION}"

log_info "Pushing commit and tag..."
git push
git push origin "$TAG"

# --- Create GitHub Release ---

# Build release body: custom notes + full changelog link
REPO_URL=$(gh repo view --json url -q '.url')
PREV_TAG=$(git tag --sort=-v:refname | sed -n '2p')
NOTES=$(cat "$NOTES_FILE")
BODY="${NOTES}

**Full Changelog**: ${REPO_URL}/compare/${PREV_TAG}...${TAG}"

log_info "Creating GitHub Release ${TAG}..."
gh release create "$TAG" "$NAMED_APK" "$NAMED_WEAR_APK" \
    --title "SweetSpot ${VERSION}" \
    --notes "$BODY" \
    $DRAFT_FLAG

echo ""
log_success "Done! Release ${TAG} created."
log_info "Phone APK: ${NAMED_APK}"
log_info "Wear APK:  ${NAMED_WEAR_APK}"
log_info "Phone AAB: build/sweetspot-phone.aab"
log_info "Wear AAB:  build/sweetspot-wear.aab"
