#!/usr/bin/env bash
set -euo pipefail

# Generates localised Play Store feature graphics (1024x500) for all languages.
#
# Layout: blue vertical gradient, app icon on near-white rounded-rect badge (left),
# "SweetSpot" title + translated tagline (right). Title in Futura Bold (white),
# tagline in Futura Medium (light blue-white). Cyrillic/Greek taglines use Noto Sans.
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

# Fonts — Futura Bold/Medium extracted from macOS TTC at runtime
FUTURA_TTC="/System/Library/Fonts/Supplemental/Futura.ttc"
FONT_NOTO="$PROJECT_DIR/fastlane/screenshots/fonts/NotoSans-SemiBold.ttf"
FONT_TITLE=""       # Set by extract_fonts()
FONT_TAGLINE=""     # Set by extract_fonts()

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
TITLE_SIZE=59
TAGLINE_SIZE=30
TITLE_COLOR="white"
TAGLINE_COLOR="#E8F0FF"
TEXT_X=319              # Left edge of text area
TEXT_GAP=13             # Gap between title label bottom and tagline label top

# All supported locales (iteration order)
LOCALES="en-US nl-NL de-DE fr-FR sl-SI bg-BG cs-CZ da-DK el-GR es-ES et-EE fi-FI hr-HR hu-HU it-IT lt-LT lv-LV mk-MK nb-NO pl-PL pt-PT ro-RO sk-SK sr-RS sv-SE"

# ──────────────────────────────────────────────
# Extract individual font faces from a TTC (TrueType Collection) file.
# ImageMagick cannot select a face index from TTC files, so we extract
# the needed faces (Bold = index 2, Medium = index 0) to standalone TTFs.
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

with open('$FUTURA_TTC', 'rb') as f:
    data = f.read()

extract_face(data, 2, '$font_dir/FuturaBold.ttf')
extract_face(data, 0, '$font_dir/FuturaMedium.ttf')
" || { echo "Error: Failed to extract Futura fonts from TTC." >&2; exit 1; }

    FONT_TITLE="$font_dir/FuturaBold.ttf"
    FONT_TAGLINE="$font_dir/FuturaMedium.ttf"
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
# Select tagline font for the current locale (Noto Sans for Cyrillic/Greek)
# ──────────────────────────────────────────────
tagline_font_for() {
    case "$1" in
        bg-BG|el-GR|mk-MK|sr-RS) echo "$FONT_NOTO" ;;
        *) echo "$FONT_TAGLINE" ;;
    esac
}

# ──────────────────────────────────────────────
# Generate one feature graphic
# ──────────────────────────────────────────────
generate() {
    local locale="$1" output="$2"
    local tagline
    tagline=$(tagline_for "$locale")
    local tagline_font
    tagline_font=$(tagline_font_for "$locale")

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

    # --- Title text (Futura Bold, white) ---
    magick -font "$FONT_TITLE" -pointsize "$TITLE_SIZE" -fill "$TITLE_COLOR" \
        -background none label:"$TITLE_TEXT" \
        "$tmp/title.png"
    local title_h
    title_h=$(magick identify -format "%h" "$tmp/title.png")

    # --- Tagline text (Futura Medium or Noto Sans, light blue-white) ---
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
    [[ -f "$FUTURA_TTC" ]] || { echo "Error: Futura font not found: $FUTURA_TTC" >&2; exit 1; }
    [[ -f "$FONT_NOTO" ]]  || { echo "Error: Font not found: $FONT_NOTO" >&2; exit 1; }
    [[ -f "$ICON_SRC" ]]   || { echo "Error: Icon not found: $ICON_SRC" >&2; exit 1; }

    echo "Extracting Futura fonts..."
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

    echo "Generated $count feature graphics."
}

main "$@"
