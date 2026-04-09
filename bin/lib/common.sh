# Shared shell library for bin/ scripts.
#
# Provides locale utilities, dependency checking, and HTML gallery helpers
# used by feature-graphic.sh and frame-screenshots.sh.
#
# Usage: source "$SCRIPT_DIR/lib/common.sh"

# ──────────────────────────────────────────────
# Map locale code to Play Console display name
# ──────────────────────────────────────────────
locale_name() {
    case "$1" in
        bg)    echo "Bulgarian" ;;
        cs-CZ) echo "Czech" ;;
        da-DK) echo "Danish" ;;
        de-DE) echo "German" ;;
        el-GR) echo "Greek" ;;
        en-GB) echo "English" ;;
        es-ES) echo "Spanish (Spain)" ;;
        et)    echo "Estonian" ;;
        fi-FI) echo "Finnish" ;;
        fr-FR) echo "French (France)" ;;
        hr)    echo "Croatian" ;;
        hu-HU) echo "Hungarian" ;;
        it-IT) echo "Italian" ;;
        lt)    echo "Lithuanian" ;;
        lv)    echo "Latvian" ;;
        mk-MK) echo "Macedonian" ;;
        no-NO) echo "Norwegian (Bokmål)" ;;
        nl-NL) echo "Dutch" ;;
        pl-PL) echo "Polish" ;;
        pt-PT) echo "Portuguese (Portugal)" ;;
        ro)    echo "Romanian" ;;
        sk)    echo "Slovak" ;;
        sl)    echo "Slovenian" ;;
        sr)    echo "Serbian" ;;
        sv-SE) echo "Swedish" ;;
        *)     echo "$1" ;;
    esac
}

# ──────────────────────────────────────────────
# Map screengrab locale (device BCP 47) to Play Console metadata locale
# ──────────────────────────────────────────────
metadata_locale() {
    case "$1" in
        bg-BG) echo "bg"    ;;
        et-EE) echo "et"    ;;
        hr-HR) echo "hr"    ;;
        lt-LT) echo "lt"    ;;
        lv-LV) echo "lv"    ;;
        nb-NO) echo "no-NO" ;;
        ro-RO) echo "ro"    ;;
        sk-SK) echo "sk"    ;;
        sl-SI) echo "sl"    ;;
        sr-RS) echo "sr"    ;;
        *)     echo "$1"    ;;
    esac
}

# ──────────────────────────────────────────────
# Map website language code to Play Console metadata locale
# ──────────────────────────────────────────────
website_to_metadata() {
    case "$1" in
        cs) echo "cs-CZ" ;; da) echo "da-DK" ;; de) echo "de-DE" ;;
        el) echo "el-GR" ;; en) echo "en-GB" ;; es) echo "es-ES" ;;
        fi) echo "fi-FI" ;; fr) echo "fr-FR" ;; hu) echo "hu-HU" ;;
        it) echo "it-IT" ;; mk) echo "mk-MK" ;; nb) echo "no-NO" ;;
        nl) echo "nl-NL" ;; pl) echo "pl-PL" ;; pt) echo "pt-PT" ;;
        sv) echo "sv-SE" ;;
        *)  echo "$1" ;;   # bg, et, hr, lt, lv, ro, sk, sl, sr pass through
    esac
}

# ──────────────────────────────────────────────
# Check that a CLI tool is available, exit with an error if not
#
# Usage: require_command <cmd> [install_hint]
# ──────────────────────────────────────────────
require_command() {
    local cmd="$1" hint="${2:-}"
    if ! command -v "$cmd" &>/dev/null; then
        echo "Error: $cmd is required." >&2
        [[ -n "$hint" ]] && echo "  Install: $hint" >&2
        exit 1
    fi
}

# ──────────────────────────────────────────────
# Write the HTML head for a gallery page (doctype, shared CSS, lightbox)
#
# Usage: gallery_header <file> <title> [extra_css]
# ──────────────────────────────────────────────
gallery_header() {
    local file="$1" title="$2" extra_css="${3:-}"
    cat > "$file" <<EOF
<!DOCTYPE html>
<html>
  <head>
    <title>${title}</title>
    <meta charset="UTF-8">
    <style>
      * {
        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
        font-weight: 300;
      }
      body { margin: 20px 40px; }
      h1 { font-size: 24px; font-weight: 400; margin: 0; padding: 16px 0 12px; }
      hr { border: none; border-top: 1px solid #DDD; margin: 32px 0 0; }
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
${extra_css}
    </style>
  </head>
  <body>
    <div id="overlay" onclick="this.style.display='none'">
      <img id="lightbox">
    </div>
EOF
}

# ──────────────────────────────────────────────
# Echo a single <img> tag with lightbox onclick handler
#
# Usage: gallery_img <src>
# ──────────────────────────────────────────────
gallery_img() {
    echo "<img src=\"$1\" onclick=\"document.getElementById('lightbox').src=this.src;document.getElementById('overlay').style.display='block'\">"
}

# ──────────────────────────────────────────────
# Write the HTML footer, echo the gallery path, and open in browser
#
# Usage: gallery_footer <file>
# ──────────────────────────────────────────────
gallery_footer() {
    local file="$1"
    cat >> "$file" <<'FOOTER'
  </body>
</html>
FOOTER
    echo "Gallery: $file"
    open "$file"
}
