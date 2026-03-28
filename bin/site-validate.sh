#!/usr/bin/env bash

# site-validate.sh
# Validates the Hugo site by building it and running checks against the output.
#
# Checks:
#   1. Hugo build succeeds (catches template errors, broken shortcodes, bad i18n calls)
#   2. All expected output pages and assets exist
#   3. Internal links in HTML files resolve to existing files
#   4. Every HTML page is at least 500 bytes (catches empty/broken renders)
#   5. All i18n TOML files have the same keys as en.toml
#
# Usage: ./bin/site-validate.sh
#   Or:  make site-validate

set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
SITE_DIR="$PROJECT_DIR/site"
PUBLIC_DIR="$SITE_DIR/public"
I18N_DIR="$SITE_DIR/i18n"

FAILURES=0

fail() {
    echo "FAIL: $1"
    FAILURES=$((FAILURES + 1))
}

# --- 1. Hugo build ---

echo "==> Building site..."
if ! hugo --source "$SITE_DIR" --minify; then
    echo ""
    echo "FAIL: Hugo build failed"
    exit 1
fi
echo ""

# --- 2. Expected pages and assets exist ---

echo "==> Checking expected pages and assets..."

LANGUAGES="en nl de fr sl"
PAGES="index.html faq/index.html changelog/index.html privacy/index.html"

# English pages live at the root (default language, no subdirectory)
for page in $PAGES; do
    if [[ ! -f "$PUBLIC_DIR/$page" ]]; then
        fail "Missing EN page: $page"
    fi
done

# 404 is only generated once at the root
if [[ ! -f "$PUBLIC_DIR/404.html" ]]; then
    fail "Missing page: 404.html"
fi

# Other languages live under their prefix
for lang in $LANGUAGES; do
    [[ "$lang" == "en" ]] && continue
    for page in $PAGES; do
        if [[ ! -f "$PUBLIC_DIR/$lang/$page" ]]; then
            fail "Missing $lang page: $lang/$page"
        fi
    done
done

# Shared assets
ASSETS="css/style.css js/main.js images/icon.svg robots.txt sitemap.xml CNAME"
for asset in $ASSETS; do
    if [[ ! -f "$PUBLIC_DIR/$asset" ]]; then
        fail "Missing asset: $asset"
    fi
done

# Badge images (one per language)
for lang in $LANGUAGES; do
    if [[ ! -f "$PUBLIC_DIR/images/badges/$lang.png" ]]; then
        fail "Missing badge: images/badges/$lang.png"
    fi
done

echo ""

# --- 3. Internal links resolve ---

echo "==> Checking internal links..."

# Extract href="/..." and src="/..." from all HTML files, verify targets exist.
# Skip external URLs, fragments-only, and data: URIs.
while IFS= read -r html_file; do
    # Extract paths from href="/" and src="/" attributes
    links=$(grep -oE '(href|src)="(/[^"]*)"' "$html_file" | \
        sed -E 's/^(href|src)="(.*)"/\2/' || true)
    [[ -z "$links" ]] && continue

    while IFS= read -r raw_path; do
        [[ -z "$raw_path" ]] && continue
        # Strip query strings and fragments
        path="${raw_path%%\?*}"
        path="${path%%#*}"

        # Skip empty paths (just "/" is fine — maps to index.html)
        [[ -z "$path" || "$path" == "/" ]] && continue

        # Resolve the path in public/
        target="$PUBLIC_DIR$path"

        # If path ends with /, look for index.html inside
        if [[ "$path" == */ ]]; then
            target="${target}index.html"
        fi

        if [[ ! -f "$target" && ! -d "$target" ]]; then
            rel_html="${html_file#"$PUBLIC_DIR"/}"
            fail "Broken link in $rel_html: $raw_path (target not found: ${target#"$PUBLIC_DIR"/})"
        fi
    done <<< "$links"
done < <(find "$PUBLIC_DIR" -name '*.html' -type f)

echo ""

# --- 4. Page size floor ---

echo "==> Checking page sizes..."

MIN_SIZE=500
while IFS= read -r html_file; do
    # Skip Hugo redirect aliases (small files with meta http-equiv=refresh)
    if grep -q 'http-equiv=refresh' "$html_file" 2>/dev/null; then
        continue
    fi
    size=$(wc -c < "$html_file")
    if [[ "$size" -lt "$MIN_SIZE" ]]; then
        rel="${html_file#"$PUBLIC_DIR"/}"
        fail "Page too small ($size bytes < $MIN_SIZE): $rel"
    fi
done < <(find "$PUBLIC_DIR" -name '*.html' -type f)

echo ""

# --- 5. i18n key parity ---

echo "==> Checking i18n key parity..."

EN_TOML="$I18N_DIR/en.toml"
if [[ ! -f "$EN_TOML" ]]; then
    fail "Missing reference i18n file: en.toml"
else
    # Extract sorted keys from en.toml (lines matching [key_name])
    en_keys=$(grep -oE '^\[([a-zA-Z0-9_]+)\]' "$EN_TOML" | sort)

    for toml_file in "$I18N_DIR"/*.toml; do
        [[ "$(basename "$toml_file")" == "en.toml" ]] && continue
        lang=$(basename "$toml_file" .toml)
        lang_keys=$(grep -oE '^\[([a-zA-Z0-9_]+)\]' "$toml_file" | sort)

        # Keys in en.toml but missing from this language
        missing=$(comm -23 <(echo "$en_keys") <(echo "$lang_keys"))
        if [[ -n "$missing" ]]; then
            for key in $missing; do
                fail "i18n key $key missing from $lang.toml"
            done
        fi

        # Keys in this language but not in en.toml (extra/stale)
        extra=$(comm -13 <(echo "$en_keys") <(echo "$lang_keys"))
        if [[ -n "$extra" ]]; then
            for key in $extra; do
                fail "Extra i18n key $key in $lang.toml (not in en.toml)"
            done
        fi
    done
fi

echo ""

# --- Summary ---

if [[ "$FAILURES" -gt 0 ]]; then
    echo "Validation failed with $FAILURES issue(s)."
    exit 1
else
    echo "All checks passed."
    exit 0
fi
