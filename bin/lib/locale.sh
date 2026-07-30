# Locale mapping helpers shared by bin/ scripts.
#
# Source directly (source "$SCRIPT_DIR/../lib/locale.sh") or via lib/common.sh.

# ──────────────────────────────────────────────
# Map a Play Console metadata locale to its display name.
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
# Map a Screengrab device BCP 47 locale to its Play Console metadata locale.
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
# Map a website language code to its Play Console metadata locale.
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
