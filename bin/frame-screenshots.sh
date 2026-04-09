#!/usr/bin/env bash
set -euo pipefail

# Frames raw screenshots with marketing text and coloured backgrounds.
#
# Output: fastlane/metadata/android/<locale>/images/phoneScreenshots/
# Gallery: build/screenshots.html
#
# Layout:
#   Image 1 — Title text + large rotated result phone (extends past right edge)
#   Image 2 — No title. Two phones: home screen (background, upright, higher)
#             + continuation of rotated result phone from image 1 (foreground)
#   Images 3–5 — Title text + single upright phone, centred
#
# Requires ImageMagick 7 (magick) and fonts in fastlane/screenshots/fonts/.

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
source "$SCRIPT_DIR/lib/common.sh"
SCREENSHOTS_DIR="$PROJECT_DIR/fastlane/screenshots"
METADATA_DIR="$PROJECT_DIR/fastlane/metadata/android"
HTML_DIR="$PROJECT_DIR/build"
FONT="$SCREENSHOTS_DIR/fonts/Raleway-SemiBold.ttf"
FONT_GREEK="$SCREENSHOTS_DIR/fonts/NotoSans-SemiBold.ttf"  # Raleway lacks Greek glyphs

# Canvas
CANVAS_W=1080
CANVAS_H=1920
FRAME_RADIUS=60           # Rounded corners on the final framed image

# Phone styling
CORNER_RADIUS=50
SHADOW_OPACITY=30
SHADOW_SIGMA=12
SHADOW_Y=6

# Text styling
TEXT_SIZE=112
TEXT_COLOR="#191C20"
TEXT_MARGIN_X=80
TEXT_MARGIN_Y=120
TEXT_MAX_W=$((CANVAS_W - TEXT_MARGIN_X * 2))

# Background colours
BG_1="#C8E6C9"   # Mint green
BG_2="#BBDEFB"   # Light blue
BG_3="#E1BEE7"   # Lavender
BG_4="#FFF9C4"   # Pale yellow
BG_5="#F8BBD0"   # Light pink

# --- Spanning phone config (images 1 & 2) ---
SPAN_SCALE_PCT=105        # Width as % of canvas (before rotation)
SPAN_ROTATION=-25        # Counter-clockwise
SPAN_VISIBLE_PCT=65      # % of rotated phone visible in image 1
SPAN_X=400               # Horizontal nudge from computed position (px, positive = right)
SPAN_Y=800               # Vertical nudge from computed position (px, positive = down)

# --- Home phone config (image 2 background) ---
HOME_Y=100               # Top edge Y position on canvas (lower = higher on screen)

# --- Play Store gallery gap (between adjacent screenshots) ---
FRAME_GAP=56              # ~5% of canvas width, matching Play Store's ~16dp carousel gap

# --- Single phone config (images 3–5) ---
SINGLE_SCALE_PCT=75      # Width as % of canvas
SINGLE_GAP=80            # Gap between bottom of title text and top of phone (px)

# ──────────────────────────────────────────────
# Helper: prepare a phone image (scale → round corners → shadow)
# ──────────────────────────────────────────────
prepare_phone() {
    local raw="$1" output="$2" scale_pct="$3"
    local phone_w=$((CANVAS_W * scale_pct / 100))
    local tmp
    tmp=$(mktemp -d)

    magick "$raw" -resize "${phone_w}x" \
        \( +clone -alpha extract \
            -fill black -colorize 100 \
            -fill white -draw "roundrectangle 0,0,%[fx:w-1],%[fx:h-1],${CORNER_RADIUS},${CORNER_RADIUS}" \
        \) -alpha off -compose CopyOpacity -composite \
        "$tmp/rounded.png"

    magick "$tmp/rounded.png" \
        \( +clone -background black -shadow "${SHADOW_OPACITY}x${SHADOW_SIGMA}+0+${SHADOW_Y}" \) \
        +swap -background none -layers merge +repage \
        "$output"

    rm -rf "$tmp"
}

# ──────────────────────────────────────────────
# Helper: format geometry string (handles negative offsets)
# ──────────────────────────────────────────────
geom() {
    local x="$1" y="$2"
    local gx gy
    if (( x >= 0 )); then gx="+${x}"; else gx="${x}"; fi
    if (( y >= 0 )); then gy="+${y}"; else gy="${y}"; fi
    echo "${gx}${gy}"
}

# ──────────────────────────────────────────────
# Helper: round corners of the final framed image
# ──────────────────────────────────────────────
round_frame() {
    local file="$1"
    magick "$file" \
        \( +clone -alpha extract \
            -fill black -colorize 100 \
            -fill white -draw "roundrectangle 0,0,%[fx:w-1],%[fx:h-1],${FRAME_RADIUS},${FRAME_RADIUS}" \
        \) -alpha off -compose CopyOpacity -composite \
        -define png:exclude-chunks=date,time \
        "$file"
}

# ──────────────────────────────────────────────
# Helper: render title text (returns path to text image)
# ──────────────────────────────────────────────
render_text() {
    local text="$1" output="$2"
    local font="$FONT"
    [[ "$CURRENT_LOCALE" == "el-GR" ]] && font="$FONT_GREEK"
    magick -font "$font" -pointsize "$TEXT_SIZE" -fill "$TEXT_COLOR" \
        -background none -size "${TEXT_MAX_W}x" -gravity West caption:"$text" \
        "$output"
}

# ──────────────────────────────────────────────
# Image 1: title + left portion of large rotated result phone
# ──────────────────────────────────────────────
frame_image_1() {
    local spanning_phone="$1" text="$2" bg_color="$3" output="$4"
    local tmp
    tmp=$(mktemp -d)

    local pw ph
    pw=$(magick identify -format "%w" "$spanning_phone")
    ph=$(magick identify -format "%h" "$spanning_phone")

    # Position: show SPAN_VISIBLE_PCT of the phone, rest extends past right edge
    local visible_w=$((pw * SPAN_VISIBLE_PCT / 100))
    local offset_x=$((CANVAS_W - visible_w + SPAN_X))
    local offset_y=$((CANVAS_H - ph + 300 + SPAN_Y))

    magick -size "${CANVAS_W}x${CANVAS_H}" "xc:${bg_color}" "$tmp/canvas.png"
    render_text "$text" "$tmp/text.png"

    magick "$tmp/canvas.png" \
        "$tmp/text.png" -gravity NorthWest -geometry "+${TEXT_MARGIN_X}+${TEXT_MARGIN_Y}" -composite \
        "$spanning_phone" -geometry "$(geom $offset_x $offset_y)" -composite \
        "$output"
    round_frame "$output"

    rm -rf "$tmp"
}

# ──────────────────────────────────────────────
# Image 2: home phone (background, higher) + result phone continuation (foreground)
# ──────────────────────────────────────────────
frame_image_2() {
    local spanning_phone="$1" home_raw="$2" bg_color="$3" output="$4"
    local tmp
    tmp=$(mktemp -d)

    # --- Background phone: home screen, upright, centred, positioned higher ---
    prepare_phone "$home_raw" "$tmp/home.png" "$SINGLE_SCALE_PCT"
    local hw
    hw=$(magick identify -format "%w" "$tmp/home.png")
    local home_x=$(( (CANVAS_W - hw) / 2 ))
    local home_y="$HOME_Y"

    # --- Foreground: continuation of spanning phone from image 1 ---
    local pw ph
    pw=$(magick identify -format "%w" "$spanning_phone")
    ph=$(magick identify -format "%h" "$spanning_phone")

    # The hidden portion in image 1 = (100 - SPAN_VISIBLE_PCT)% of phone width
    local hidden_w=$((pw * (100 - SPAN_VISIBLE_PCT) / 100))
    # Shift phone left so only the hidden portion is now visible from the left edge.
    # Subtract FRAME_GAP to compensate for the gap between frames 1 & 2 in the store.
    local span_x=$(( -(pw - hidden_w) + SPAN_X - FRAME_GAP ))
    local span_y=$((CANVAS_H - ph + 300 + SPAN_Y))   # Same Y as image 1

    magick -size "${CANVAS_W}x${CANVAS_H}" "xc:${bg_color}" \
        "$tmp/home.png" -geometry "$(geom $home_x $home_y)" -composite \
        "$spanning_phone" -geometry "$(geom $span_x $span_y)" -composite \
        "$output"
    round_frame "$output"

    rm -rf "$tmp"
}

# ──────────────────────────────────────────────
# Images 3–5: title + single upright phone, centred
# ──────────────────────────────────────────────
frame_single() {
    local raw="$1" text="$2" bg_color="$3" output="$4"
    local tmp
    tmp=$(mktemp -d)

    prepare_phone "$raw" "$tmp/phone.png" "$SINGLE_SCALE_PCT"
    local pw ph
    pw=$(magick identify -format "%w" "$tmp/phone.png")
    ph=$(magick identify -format "%h" "$tmp/phone.png")

    magick -size "${CANVAS_W}x${CANVAS_H}" "xc:${bg_color}" "$tmp/canvas.png"
    render_text "$text" "$tmp/text.png"

    # Position phone below the title text with a configurable gap
    local text_h
    text_h=$(magick identify -format "%h" "$tmp/text.png")
    local offset_x=$(( (CANVAS_W - pw) / 2 ))
    local offset_y=$(( TEXT_MARGIN_Y + text_h + SINGLE_GAP ))

    magick "$tmp/canvas.png" \
        "$tmp/text.png" -gravity NorthWest -geometry "+${TEXT_MARGIN_X}+${TEXT_MARGIN_Y}" -composite \
        "$tmp/phone.png" -geometry "$(geom $offset_x $offset_y)" -composite \
        "$output"
    round_frame "$output"

    rm -rf "$tmp"
}

# ──────────────────────────────────────────────
# Parse marketing text from title.strings
# ──────────────────────────────────────────────
read_title() {
    local title_file="$1" filter="$2"
    grep "\"${filter}\"" "$title_file" | sed 's/.*= "//;s/";$//'
}

# ──────────────────────────────────────────────
# Find raw screenshot for a filter (excludes *_framed.png)
# ──────────────────────────────────────────────
find_raw() {
    local img_dir="$1" filter="$2"
    find "$img_dir" -name "${filter}_*.png" ! -name "*_framed.png" -print -quit 2>/dev/null || true
}

# ──────────────────────────────────────────────
main() {
    require_command magick "brew install imagemagick"
    [[ -f "$FONT" ]] || { echo "Error: Font not found: $FONT" >&2; exit 1; }

    # Clean existing framed screenshots
    if [[ -n "${LOCALE:-}" ]]; then
        local clean_locale
        clean_locale=$(metadata_locale "$LOCALE")
        rm -f "$METADATA_DIR/$clean_locale/images/phoneScreenshots"/*.png 2>/dev/null || true
    else
        for d in "$METADATA_DIR"/*/images/phoneScreenshots; do
            rm -f "$d"/*.png 2>/dev/null || true
        done
    fi

    local count=0
    for locale_dir in "$SCREENSHOTS_DIR"/*/; do
        local locale
        locale=$(basename "$locale_dir")
        [[ "$locale" == "fonts" ]] && continue
        [[ -n "${LOCALE:-}" && "$locale" != "$LOCALE" ]] && continue

        local title_file="$locale_dir/title.strings"
        [[ -f "$title_file" ]] || continue

        local img_dir="$locale_dir/images/phoneScreenshots"
        [[ -d "$img_dir" ]] || continue

        local out_locale
        out_locale=$(metadata_locale "$locale")
        local out_dir="$METADATA_DIR/$out_locale/images/phoneScreenshots"
        mkdir -p "$out_dir"

        echo "Framing $locale..."
        CURRENT_LOCALE="$locale"

        # Find raw screenshots
        local raw_result raw_home raw_prices raw_settings raw_languages
        raw_result=$(find_raw "$img_dir" "1_result")
        raw_home=$(find_raw "$img_dir" "2_home")
        raw_prices=$(find_raw "$img_dir" "3_prices")
        raw_settings=$(find_raw "$img_dir" "4_settings")
        raw_languages=$(find_raw "$img_dir" "5_languages")

        # --- Generate spanning phone (used by images 1 & 2) ---
        local spanning_phone=""
        if [[ -n "$raw_result" && -f "$raw_result" ]]; then
            spanning_phone=$(mktemp /tmp/spanning_XXXXXX.png)
            local tmp_pre
            tmp_pre=$(mktemp /tmp/pre_XXXXXX.png)
            prepare_phone "$raw_result" "$tmp_pre" "$SPAN_SCALE_PCT"
            magick "$tmp_pre" -background none -rotate "$SPAN_ROTATION" "$spanning_phone"
            rm -f "$tmp_pre"
        fi

        # --- Image 1: title + spanning phone ---
        if [[ -n "$spanning_phone" ]]; then
            local text_1
            text_1=$(read_title "$title_file" "1_result")
            if [[ -n "$text_1" ]]; then
                frame_image_1 "$spanning_phone" "$text_1" "$BG_1" "$out_dir/1_result.png"
                count=$((count + 1))
            fi
        fi

        # --- Image 2: home phone (bg) + spanning phone continuation (fg) ---
        if [[ -n "$spanning_phone" && -n "$raw_home" && -f "$raw_home" ]]; then
            frame_image_2 "$spanning_phone" "$raw_home" "$BG_2" "$out_dir/2_home.png"
            count=$((count + 1))
        fi

        # Clean up spanning phone
        [[ -n "$spanning_phone" ]] && rm -f "$spanning_phone"

        # --- Images 3–5: single phone ---
        local filter raw bg text
        for filter in 3_prices 4_settings 5_languages; do
            case "$filter" in
                3_prices)    raw="$raw_prices";    bg="$BG_3" ;;
                4_settings)  raw="$raw_settings";  bg="$BG_4" ;;
                5_languages) raw="$raw_languages"; bg="$BG_5" ;;
            esac
            [[ -n "$raw" && -f "$raw" ]] || continue
            text=$(read_title "$title_file" "$filter")
            [[ -n "$text" ]] || continue
            frame_single "$raw" "$text" "$bg" "$out_dir/${filter}.png"
            count=$((count + 1))
        done
    done

    generate_html
    echo "Framed $count screenshots in fastlane/metadata/android/"
}

generate_html() {
    mkdir -p "$HTML_DIR"
    local html="$HTML_DIR/screenshots.html"

    gallery_header "$html" "Framed Screenshots" \
"      /* Gap must equal FRAME_GAP/CANVAS_W of each image's width for spanning
         phone alignment. With N equal images: gap% = ratio / (N + N*ratio - ratio)
         where ratio = FRAME_GAP/CANVAS_W ≈ 0.0519.
         5 imgs → 1%, 6 → 0.84%, 7 → 0.72%, 8 → 0.63%, 9 → 0.56%, 10 → 0.5% */
      .screenshots { display: flex; gap: 1%; }
      .screenshots img {
        flex: 1;
        min-width: 0;
        cursor: pointer;
        border-radius: 4px;
      }"

    # Collect locales and sort by display name
    local sorted_locales=()
    for locale_dir in "$METADATA_DIR"/*/images/phoneScreenshots; do
        [[ -d "$locale_dir" ]] || continue
        local locale
        locale=$(basename "$(dirname "$(dirname "$locale_dir")")")
        sorted_locales+=("$(locale_name "$locale")|$locale")
    done
    mapfile -t sorted_locales < <(sort <<< "${sorted_locales[*]}")

    for entry in "${sorted_locales[@]}"; do
        local locale="${entry#*|}"
        local locale_dir="$METADATA_DIR/$locale/images/phoneScreenshots"
        local display_name
        display_name="$(locale_name "$locale") – $locale"

        # Collect PNGs in sorted order
        local images=()
        for img in "$locale_dir"/*.png; do
            [[ -f "$img" ]] || continue
            images+=("$(basename "$img")")
        done
        [[ ${#images[@]} -eq 0 ]] && continue

        # Relative path from build/ to fastlane/metadata/android/<locale>/images/phoneScreenshots/
        local rel_dir="../fastlane/metadata/android/$locale/images/phoneScreenshots"

        cat >> "$html" <<EOF
    <hr>
    <h1>${display_name}</h1>
    <div class="screenshots">
EOF
        for img in "${images[@]}"; do
            echo "      $(gallery_img "$rel_dir/$img")" >> "$html"
        done
        cat >> "$html" <<'EOF'
    </div>
EOF
    done

    gallery_footer "$html"
}

main "$@"
