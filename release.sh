#!/usr/bin/env bash
#
# Build a signed release APK and create a GitHub Release.
#
# Usage:
#   ./release.sh 1.1          # sets versionName=1.1, auto-increments versionCode
#   ./release.sh 1.1 --draft  # creates a draft release
#
set -euo pipefail

VERSION="${1:?Usage: ./release.sh <version> [--draft]}"
DRAFT_FLAG=""
if [[ "${2:-}" == "--draft" ]]; then
    DRAFT_FLAG="--draft"
fi

GRADLE_FILE="app/build.gradle.kts"
TAG="v${VERSION}"

# --- Bump version ---

# Read current versionCode and increment
CURRENT_CODE=$(sed -n 's/.*versionCode = \([0-9]*\).*/\1/p' "$GRADLE_FILE")
NEW_CODE=$((CURRENT_CODE + 1))

echo "Bumping versionCode $CURRENT_CODE → $NEW_CODE, versionName → $VERSION"

sed -i '' "s/versionCode = $CURRENT_CODE/versionCode = $NEW_CODE/" "$GRADLE_FILE"
sed -i '' "s/versionName = \".*\"/versionName = \"$VERSION\"/" "$GRADLE_FILE"

# --- Build release APK ---

echo "Building release APK..."
./gradlew assembleRelease

APK_PATH="app/build/outputs/apk/release/app-release.apk"
if [[ ! -f "$APK_PATH" ]]; then
    echo "ERROR: Release APK not found at $APK_PATH"
    exit 1
fi

# Rename APK to include version
NAMED_APK="app/build/outputs/apk/release/sweetspot-${VERSION}.apk"
cp "$APK_PATH" "$NAMED_APK"

# --- Commit and tag ---

git add "$GRADLE_FILE"
git commit -m "chore: release v${VERSION}"
git tag -a "$TAG" -m "Release ${VERSION}"

echo "Pushing commit and tag..."
git push
git push origin "$TAG"

# --- Create GitHub Release ---

echo "Creating GitHub Release ${TAG}..."
gh release create "$TAG" "$NAMED_APK" \
    --title "SweetSpot ${VERSION}" \
    --generate-notes \
    $DRAFT_FLAG

echo ""
echo "Done! Release ${TAG} created."
echo "APK: ${NAMED_APK}"
