# HTML gallery helpers shared by the image-generating bin/ scripts
# (playstore/feature-graphic.sh, playstore/frame-screenshots.sh).
#
# Source directly (source "$SCRIPT_DIR/../lib/gallery.sh") or via lib/common.sh.

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
