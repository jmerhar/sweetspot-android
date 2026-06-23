#!/usr/bin/env bash

# site-screenshots.sh
# Generates the per-language framed screenshots used on the website landing page.
#
# The framed marketing screenshots already live in fastlane/metadata/android/<play-locale>/
# (committed, also uploaded to the Play Store). This script derives lightweight WebP copies,
# one set per Hugo site language, into site/static/images/screenshots/<lang>/{1..6}.webp.
#
# These outputs are NOT committed (see .gitignore) — they are regenerated on demand locally
# (make site / make site-validate) and in CI (deploy-site workflow) before the Hugo build.
#
# Usage: ./bin/site-screenshots.sh   (or: make site-screenshots)

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
SRC_DIR="$ROOT_DIR/fastlane/metadata/android"
OUT_DIR="$ROOT_DIR/site/static/images/screenshots"

WIDTH=563        # 2x the 500px display height; matches the 1080x1920 framed aspect
HEIGHT=1000
QUALITY=82

# Framed screenshot filenames (Play Store order) → emitted as 1.webp … 6.webp.
SHOTS="1_result 2_home 3_prices 4_settings 5_ev_charging 6_languages"

# Hugo site languages (content/<lang>) handled by the landing page.
LANGS="en bg cs da de el es et fi fr hr hu it lt lv mk nb nl pl pt ro sk sl sr sv"

# Maps a Hugo language code to its Play Store metadata locale directory.
metadata_dir() {
    case "$1" in
        en) echo "en-GB" ;;
        cs) echo "cs-CZ" ;;
        da) echo "da-DK" ;;
        de) echo "de-DE" ;;
        el) echo "el-GR" ;;
        es) echo "es-ES" ;;
        fi) echo "fi-FI" ;;
        fr) echo "fr-FR" ;;
        hu) echo "hu-HU" ;;
        it) echo "it-IT" ;;
        mk) echo "mk-MK" ;;
        nb) echo "no-NO" ;;
        nl) echo "nl-NL" ;;
        pl) echo "pl-PL" ;;
        pt) echo "pt-PT" ;;
        sv) echo "sv-SE" ;;
        *)  echo "$1" ;;   # bg, et, hr, lt, lv, ro, sk, sl, sr map to themselves
    esac
}

# Encodes a PNG to a resized WebP, preferring cwebp (CI installs the `webp` package) and
# falling back to ImageMagick.
to_webp() {
    local src="$1" out="$2"
    if command -v cwebp >/dev/null 2>&1; then
        cwebp -quiet -q "$QUALITY" -resize "$WIDTH" "$HEIGHT" "$src" -o "$out"
    else
        magick "$src" -resize "${WIDTH}x${HEIGHT}!" -quality "$QUALITY" "$out"
    fi
}

count=0
for lang in $LANGS; do
    src_loc="$SRC_DIR/$(metadata_dir "$lang")/images/phoneScreenshots"
    out_loc="$OUT_DIR/$lang"
    mkdir -p "$out_loc"
    i=1
    for shot in $SHOTS; do
        src="$src_loc/$shot.png"
        if [[ -f "$src" ]]; then
            to_webp "$src" "$out_loc/${i}.webp"
            count=$((count + 1))
        else
            echo "WARNING: missing $src" >&2
        fi
        i=$((i + 1))
    done
done

echo "Generated $count website screenshots in site/static/images/screenshots/"
