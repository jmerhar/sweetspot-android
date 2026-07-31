#!/usr/bin/env bash
#
# Guardrail: verify every locale's Play Store listing text stays within Google's
# per-field length limits, so a translation can never grow past a limit and get
# silently rejected at upload time. Counts Unicode code points via python3 (the
# unit the Store measures in; shell ${#var} counts bytes under a C locale).
#
# Run by CI (test.yml) and `make check-listing`.
#
set -euo pipefail
source "$(dirname "$0")/../lib/log.sh"

ROOT_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
META_DIR="$ROOT_DIR/fastlane/metadata/android"

fail=0

# Google Play limits per listing field.
check_field() {
    local file="$1" limit="$2" dir path n
    for dir in "$META_DIR"/*/; do
        path="$dir$file"
        [[ -f "$path" ]] || continue
        n=$(python3 -c 'import sys; print(len(open(sys.argv[1], encoding="utf-8").read().rstrip("\n")))' "$path")
        if (( n > limit )); then
            log_error "$(basename "$dir")/$file: $n chars (limit $limit)"
            fail=1
        fi
    done
}

check_field title.txt 30
check_field short_description.txt 80
check_field full_description.txt 4000

if (( fail )); then
    die "One or more listing fields exceed the Play Store limit (see above)."
fi
log_success "All listing fields are within Play Store limits."
