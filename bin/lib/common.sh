# Aggregator for the bin/ shared shell libraries.
#
# Sourcing this pulls in every helper (locale mapping, HTML gallery, dependency checks).
# Scripts that need only one concern may source the specific lib/<name>.sh instead.
#
# Usage: source "$SCRIPT_DIR/../lib/common.sh"

_lib_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$_lib_dir/log.sh"
source "$_lib_dir/locale.sh"
source "$_lib_dir/gallery.sh"
source "$_lib_dir/require.sh"
