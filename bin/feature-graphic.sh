#!/usr/bin/env bash
set -euo pipefail

# Generates localised Play Store feature graphics (1024x500) for all languages.
#
# Output: fastlane/metadata/android/<locale>/images/featureGraphic.png
# Gallery: build/feature-graphics.html
#
# Layout: blue vertical gradient, app icon on near-white rounded-rect badge (left),
# "SweetSpot" title + translated tagline (right). Both in Avenir Next Bold — title
# in white, tagline in light blue-white.
#
# Usage:
#   ./bin/feature-graphic.sh                # Generate for all languages
#   LOCALE=nl-NL ./bin/feature-graphic.sh   # Generate for one language
#
# Requires ImageMagick 7 (magick) and Python 3 (for TTC font extraction).

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
METADATA_DIR="$PROJECT_DIR/fastlane/metadata/android"
ICON_SRC="$PROJECT_DIR/fastlane/metadata/android/en-US/images/icon.png"

# Fonts — Avenir Next Bold extracted from macOS TTC at runtime
AVENIR_NEXT_TTC="/System/Library/Fonts/Avenir Next.ttc"
FONT_TITLE=""       # Set by extract_fonts()

# Canvas
CANVAS_W=1024
CANVAS_H=500

# Gradient (vertical, top to bottom)
GRADIENT_TOP="#4A90D9"
GRADIENT_BOTTOM="#2E6DAF"

# Icon badge (near-white rounded-rect behind the app icon)
BADGE_SIZE=160
BADGE_RADIUS=24
BADGE_COLOR="#F5F7FA"
BADGE_X=120             # Left edge of badge
BADGE_Y=170             # Top edge of badge

# Text
TITLE_TEXT="SweetSpot"
TITLE_SIZE=64
TAGLINE_SIZE=28
TITLE_COLOR="white"
TAGLINE_COLOR="#E8F0FF"
TEXT_X=319              # Left edge of text area
TEXT_GAP=13             # Gap between title label bottom and tagline label top

# All supported locales (iteration order)
LOCALES="en-US nl-NL de-DE fr-FR sl-SI bg-BG cs-CZ da-DK el-GR es-ES et-EE fi-FI hr-HR hu-HU it-IT lt-LT lv-LV mk-MK nb-NO pl-PL pt-PT ro-RO sk-SK sr-RS sv-SE"

# ──────────────────────────────────────────────
# Extract Avenir Next Bold from the macOS TTC (TrueType Collection) file.
# ImageMagick cannot select a face index from TTC files, so we extract
# the Bold face (index 0) to a standalone TTF.
# ──────────────────────────────────────────────
extract_fonts() {
    local font_dir
    font_dir=$(mktemp -d)
    FONT_DIR="$font_dir"

    python3 -c "
import struct, sys

def extract_face(data, face_index, output_path):
    num_fonts = struct.unpack('>I', data[8:12])[0]
    if face_index >= num_fonts:
        print(f'Face index {face_index} out of range (max {num_fonts-1})', file=sys.stderr)
        sys.exit(1)
    offsets = struct.unpack(f'>{num_fonts}I', data[12:12+4*num_fonts])
    offset = offsets[face_index]

    sf_version = data[offset:offset+4]
    num_tables = struct.unpack('>H', data[offset+4:offset+6])[0]

    tables = []
    for t in range(num_tables):
        rec = offset + 12 + t * 16
        tag = data[rec:rec+4]
        checksum, tbl_offset, tbl_length = struct.unpack('>III', data[rec+4:rec+16])
        tables.append((tag, checksum, tbl_offset, tbl_length))

    # Build standalone TTF
    header_size = 12 + num_tables * 16
    padded_header = (header_size + 3) & ~3

    search_range = 16
    entry_selector = 0
    p = 1
    while p * 2 <= num_tables:
        p *= 2
        search_range = p * 16
        entry_selector += 1
    range_shift = num_tables * 16 - search_range

    out = bytearray()
    out += sf_version
    out += struct.pack('>HHH', num_tables, search_range, entry_selector)
    out += struct.pack('>H', range_shift)

    current = padded_header
    new_tables = []
    for tag, checksum, _, length in tables:
        new_tables.append((tag, checksum, current, length))
        current += (length + 3) & ~3

    for tag, checksum, new_offset, length in new_tables:
        out += tag + struct.pack('>III', checksum, new_offset, length)

    while len(out) < padded_header:
        out += b'\x00'

    for (_, _, old_offset, length), _ in zip(tables, new_tables):
        out += data[old_offset:old_offset+length]
        out += b'\x00' * (((length + 3) & ~3) - length)

    with open(output_path, 'wb') as f:
        f.write(out)

with open('$AVENIR_NEXT_TTC', 'rb') as f:
    data = f.read()

extract_face(data, 0, '$font_dir/AvenirNextBold.ttf')
" || { echo "Error: Failed to extract Avenir Next Bold from TTC." >&2; exit 1; }

    FONT_TITLE="$font_dir/AvenirNextBold.ttf"
}

# ──────────────────────────────────────────────
# Tagline for the given locale (feature graphic subtitle)
# ──────────────────────────────────────────────
tagline_for() {
    case "$1" in
        en-US) echo "Find the cheapest time to run your appliances" ;;
        nl-NL) echo "Vind het goedkoopste moment om je apparaten te draaien" ;;
        de-DE) echo "Finde den günstigsten Zeitpunkt für deine Geräte" ;;
        fr-FR) echo "Trouvez le meilleur moment pour vos appareils" ;;
        sl-SI) echo "Poiščite najcenejši čas za vaše aparate" ;;
        bg-BG) echo "Намерете най-евтиното време за вашите уреди" ;;
        cs-CZ) echo "Najděte nejlevnější čas pro vaše spotřebiče" ;;
        da-DK) echo "Find det billigste tidspunkt for dine apparater" ;;
        el-GR) echo "Βρείτε την πιο φθηνή ώρα για τις συσκευές σας" ;;
        es-ES) echo "Encuentra el momento más barato para tus aparatos" ;;
        et-EE) echo "Leidke oma seadmete jaoks soodsaim aeg" ;;
        fi-FI) echo "Löydä halvin aika kodinkoneillesi" ;;
        hr-HR) echo "Pronađite najjeftinije vrijeme za vaše uređaje" ;;
        hu-HU) echo "Találja meg a legolcsóbb időpontot készülékeihez" ;;
        it-IT) echo "Trova il momento più economico per i tuoi elettrodomestici" ;;
        lt-LT) echo "Raskite pigiausią laiką savo prietaisams" ;;
        lv-LV) echo "Atrodiet lētāko laiku savām ierīcēm" ;;
        mk-MK) echo "Најдете го најевтиното време за вашите апарати" ;;
        nb-NO) echo "Finn det billigste tidspunktet for apparatene dine" ;;
        pl-PL) echo "Znajdź najtańszy czas na uruchomienie urządzeń" ;;
        pt-PT) echo "Encontre o momento mais barato para os seus aparelhos" ;;
        ro-RO) echo "Găsiți cel mai ieftin moment pentru aparatele dvs." ;;
        sk-SK) echo "Nájdite najlacnejší čas pre vaše spotrebiče" ;;
        sr-RS) echo "Пронађите најјефтиније време за ваше уређаје" ;;
        sv-SE) echo "Hitta billigaste tiden för dina apparater" ;;
    esac
}

# ──────────────────────────────────────────────
# Generate one feature graphic
# ──────────────────────────────────────────────
generate() {
    local locale="$1" output="$2"
    local tagline
    tagline=$(tagline_for "$locale")
    local tagline_font="$FONT_TITLE"

    local tmp
    tmp=$(mktemp -d)

    # --- Gradient background (vertical, top to bottom) ---
    magick -size "${CANVAS_W}x${CANVAS_H}" -colorspace sRGB \
        "gradient:${GRADIENT_TOP}-${GRADIENT_BOTTOM}" \
        "$tmp/bg.png"

    # --- Icon badge (near-white rounded-rect + app icon, no shadow) ---
    magick -size "${BADGE_SIZE}x${BADGE_SIZE}" xc:none \
        -fill "${BADGE_COLOR}" \
        -draw "roundrectangle 0,0,$((BADGE_SIZE-1)),$((BADGE_SIZE-1)),${BADGE_RADIUS},${BADGE_RADIUS}" \
        "$tmp/badge.png"

    # Composite icon onto badge — icon fills the badge with its own padding
    magick "$tmp/badge.png" \
        \( "$ICON_SRC" -colorspace sRGB -resize "${BADGE_SIZE}x${BADGE_SIZE}" \) \
        -gravity center -composite \
        "$tmp/badge_icon.png"

    # --- Title text (Avenir Next Bold, white) ---
    magick -font "$FONT_TITLE" -pointsize "$TITLE_SIZE" -fill "$TITLE_COLOR" \
        -background none label:"$TITLE_TEXT" \
        "$tmp/title.png"
    local title_h
    title_h=$(magick identify -format "%h" "$tmp/title.png")

    # --- Tagline text (Avenir Next Bold, light blue-white) ---
    local text_max_w=$((CANVAS_W - TEXT_X - 40))
    magick -font "$tagline_font" -pointsize "$TAGLINE_SIZE" -fill "$TAGLINE_COLOR" \
        -background none -size "${text_max_w}x" -gravity West caption:"$tagline" \
        "$tmp/tagline.png"
    local tagline_h
    tagline_h=$(magick identify -format "%h" "$tmp/tagline.png")

    # --- Vertical centering of title + tagline block ---
    local total_h=$((title_h + TEXT_GAP + tagline_h))
    local text_top_y=$(( (CANVAS_H - total_h) / 2 ))
    local tagline_y=$((text_top_y + title_h + TEXT_GAP))

    # --- Composite everything ---
    magick "$tmp/bg.png" \
        "$tmp/badge_icon.png" -geometry "+${BADGE_X}+${BADGE_Y}" -composite \
        "$tmp/title.png"      -geometry "+${TEXT_X}+${text_top_y}" -composite \
        "$tmp/tagline.png"    -geometry "+${TEXT_X}+${tagline_y}" -composite \
        "$output"

    rm -rf "$tmp"
}

# ──────────────────────────────────────────────
# Map locale code to Play Console display name
# ──────────────────────────────────────────────
locale_name() {
    case "$1" in
        bg-BG) echo "Bulgarian" ;;
        cs-CZ) echo "Czech" ;;
        da-DK) echo "Danish" ;;
        de-DE) echo "German" ;;
        el-GR) echo "Greek" ;;
        en-US) echo "English (United States)" ;;
        es-ES) echo "Spanish (Spain)" ;;
        et-EE) echo "Estonian" ;;
        fi-FI) echo "Finnish" ;;
        fr-FR) echo "French (France)" ;;
        hr-HR) echo "Croatian" ;;
        hu-HU) echo "Hungarian" ;;
        it-IT) echo "Italian" ;;
        lt-LT) echo "Lithuanian" ;;
        lv-LV) echo "Latvian" ;;
        mk-MK) echo "Macedonian" ;;
        nb-NO) echo "Norwegian (Bokmål)" ;;
        nl-NL) echo "Dutch" ;;
        pl-PL) echo "Polish" ;;
        pt-PT) echo "Portuguese (Portugal)" ;;
        ro-RO) echo "Romanian" ;;
        sk-SK) echo "Slovak" ;;
        sl-SI) echo "Slovenian" ;;
        sr-RS) echo "Serbian" ;;
        sv-SE) echo "Swedish" ;;
        *)     echo "$1" ;;
    esac
}

# ──────────────────────────────────────────────
# Generate HTML gallery for visual review
# ──────────────────────────────────────────────
generate_html() {
    local html_dir="$PROJECT_DIR/build"
    mkdir -p "$html_dir"
    local html="$html_dir/feature-graphics.html"

    cat > "$html" <<'HEADER'
<!DOCTYPE html>
<html>
  <head>
    <title>Feature Graphics</title>
    <meta charset="UTF-8">
    <style>
      * {
        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
        font-weight: 300;
      }
      body { margin: 20px 40px; }
      h1 { font-size: 24px; font-weight: 400; margin: 0; padding: 16px 0 12px; }
      h2 { font-size: 18px; font-weight: 400; margin: 0; padding: 12px 0 8px; color: #888; }
      hr { border: none; border-top: 1px solid #DDD; margin: 32px 0 0; }
      img {
        max-width: 100%;
        cursor: pointer;
        border-radius: 4px;
      }
      #overlay {
        position: fixed; top: 0; left: 0;
        background: rgba(0,0,0,0.85);
        width: 100%; height: 100%;
        display: none; z-index: 5;
        cursor: zoom-out;
        text-align: center;
      }
      #overlay img {
        max-height: 95vh; max-width: 95vw;
        margin-top: 2.5vh;
      }
    </style>
  </head>
  <body>
    <div id="overlay" onclick="this.style.display='none'">
      <img id="lightbox">
    </div>
HEADER

    for locale in $LOCALES; do
        local img="$METADATA_DIR/$locale/images/featureGraphic.png"
        [[ -f "$img" ]] || continue

        local display_name
        display_name="$(locale_name "$locale") – $locale"
        local rel_path="../fastlane/metadata/android/$locale/images/featureGraphic.png"

        cat >> "$html" <<EOF
    <hr>
    <h1>${display_name}</h1>
    <img src="${rel_path}" onclick="document.getElementById('lightbox').src=this.src;document.getElementById('overlay').style.display='block'">
EOF
    done

    cat >> "$html" <<'FOOTER'
  </body>
</html>
FOOTER
    echo "Gallery: $html"
    open "$html"
}

# ──────────────────────────────────────────────
main() {
    if ! command -v magick &>/dev/null; then
        echo "Error: ImageMagick 7 (magick) is required." >&2
        echo "  Install: brew install imagemagick" >&2
        exit 1
    fi
    if ! command -v python3 &>/dev/null; then
        echo "Error: Python 3 is required (for TTC font extraction)." >&2
        exit 1
    fi
    [[ -f "$AVENIR_NEXT_TTC" ]] || { echo "Error: Avenir Next font not found: $AVENIR_NEXT_TTC" >&2; exit 1; }
    [[ -f "$ICON_SRC" ]]       || { echo "Error: Icon not found: $ICON_SRC" >&2; exit 1; }

    echo "Extracting Avenir Next Bold..."
    extract_fonts

    local count=0
    for locale in $LOCALES; do
        [[ -n "${LOCALE:-}" && "$locale" != "$LOCALE" ]] && continue

        local out_dir="$METADATA_DIR/$locale/images"
        mkdir -p "$out_dir"

        echo "Generating $locale..."
        generate "$locale" "$out_dir/featureGraphic.png"
        count=$((count + 1))
    done

    # Clean up extracted fonts
    [[ -n "${FONT_DIR:-}" ]] && rm -rf "$FONT_DIR"

    generate_html
    echo "Generated $count feature graphics."
}

main "$@"
