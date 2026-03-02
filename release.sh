#!/usr/bin/env bash
#
# Build a signed release APK and create a GitHub Release.
#
# Usage:
#   ./release.sh 1.1 -n notes.md          # release with notes from file
#   ./release.sh 1.1 -n notes.md --draft   # same but creates a draft release
#
set -euo pipefail

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
        *) echo "Unknown option: $1"; exit 1 ;;
    esac
done

if [[ -z "$NOTES_FILE" ]]; then
    echo "ERROR: Release notes file is required. Usage: ./release.sh <version> -n <notes-file> [--draft]"
    exit 1
fi

if [[ ! -f "$NOTES_FILE" ]]; then
    echo "ERROR: Notes file not found: $NOTES_FILE"
    exit 1
fi

GRADLE_FILE="app/build.gradle.kts"
WEAR_GRADLE_FILE="wear/build.gradle.kts"
TAG="v${VERSION}"

# --- Bump version ---

# Read current versionCode and increment
CURRENT_CODE=$(sed -n 's/.*versionCode = \([0-9]*\).*/\1/p' "$GRADLE_FILE")
NEW_CODE=$((CURRENT_CODE + 1))

echo "Bumping versionCode $CURRENT_CODE → $NEW_CODE, versionName → $VERSION"

sedi "s/versionCode = $CURRENT_CODE/versionCode = $NEW_CODE/" "$GRADLE_FILE"
sedi "s/versionName = \".*\"/versionName = \"$VERSION\"/" "$GRADLE_FILE"

# Bump wear module to match
WEAR_CURRENT_CODE=$(sed -n 's/.*versionCode = \([0-9]*\).*/\1/p' "$WEAR_GRADLE_FILE")
sedi "s/versionCode = $WEAR_CURRENT_CODE/versionCode = $NEW_CODE/" "$WEAR_GRADLE_FILE"
sedi "s/versionName = \".*\"/versionName = \"$VERSION\"/" "$WEAR_GRADLE_FILE"

# --- Build release APK ---

echo "Building release APK..."
./gradlew assembleRelease

APK_PATH="app/build/outputs/apk/release/app-release.apk"
if [[ ! -f "$APK_PATH" ]]; then
    echo "ERROR: Release APK not found at $APK_PATH"
    exit 1
fi

WEAR_APK_PATH="wear/build/outputs/apk/release/wear-release.apk"
if [[ ! -f "$WEAR_APK_PATH" ]]; then
    echo "ERROR: Wear APK not found at $WEAR_APK_PATH"
    exit 1
fi

# Rename APKs to include version
NAMED_APK="app/build/outputs/apk/release/sweetspot-${VERSION}.apk"
cp "$APK_PATH" "$NAMED_APK"

NAMED_WEAR_APK="wear/build/outputs/apk/release/sweetspot-wear-${VERSION}.apk"
cp "$WEAR_APK_PATH" "$NAMED_WEAR_APK"

# --- Commit and tag ---

git add "$GRADLE_FILE" "$WEAR_GRADLE_FILE"
git commit -m "chore: release v${VERSION}"
git tag -a "$TAG" -m "Release ${VERSION}"

echo "Pushing commit and tag..."
git push
git push origin "$TAG"

# --- Create GitHub Release ---

# Build release body: custom notes + full changelog link
REPO_URL=$(gh repo view --json url -q '.url')
PREV_TAG=$(git tag --sort=-v:refname | sed -n '2p')
NOTES=$(cat "$NOTES_FILE")
BODY="${NOTES}

**Full Changelog**: ${REPO_URL}/compare/${PREV_TAG}...${TAG}"

echo "Creating GitHub Release ${TAG}..."
gh release create "$TAG" "$NAMED_APK" "$NAMED_WEAR_APK" \
    --title "SweetSpot ${VERSION}" \
    --notes "$BODY" \
    $DRAFT_FLAG

echo ""
echo "Done! Release ${TAG} created."
echo "Phone APK: ${NAMED_APK}"
echo "Wear APK:  ${NAMED_WEAR_APK}"
