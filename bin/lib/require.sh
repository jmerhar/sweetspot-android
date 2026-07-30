# Dependency-checking helper shared by bin/ scripts.
#
# Source directly (source "$SCRIPT_DIR/../lib/require.sh") or via lib/common.sh.

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
