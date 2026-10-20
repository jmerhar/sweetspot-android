#!/usr/bin/env bash
#
# Guardrail: every package in a committed package-lock.json must resolve to the public npm registry.
#
# Written as a positive assertion rather than a list of hostnames to reject: this repository is
# public, so a check that named the private registries it looked for would put those names in the
# repository itself. `npm install` on a network that proxies npm records the proxy's hostname on
# every line of the lockfile, so this is easy to do by accident and invisible in review.
#
# Usage:
#   ./bin/quality/check-lockfile.sh                 # every package-lock.json tracked by git
#   ./bin/quality/check-lockfile.sh <lockfile>...   # only the named files (used by its tests)
#
# Run by CI (test.yml) and `make check-lockfile`.
#
set -euo pipefail
source "$(dirname "$0")/../lib/log.sh"

cd "$(dirname "$0")/../.."
public="https://registry.npmjs.org/"

if (( $# )); then
    lockfiles=("$@")
else
    mapfile -t lockfiles < <(git ls-files -- '*package-lock.json')
fi

fail=0
for lockfile in "${lockfiles[@]}"; do
    if [[ ! -f "$lockfile" ]]; then
        log_error "$lockfile is missing"
        fail=1
        continue
    fi
    # Every "resolved" URL that does not start with the public registry, compared as a literal
    # prefix so a lookalike host (registry.npmjs.org.example) cannot pass.
    foreign=()
    while IFS= read -r url; do
        [[ "$url" == "$public"* ]] || foreign+=("$url")
    done < <(grep -oE '"resolved": *"[^"]+"' "$lockfile" | sed -E 's/"resolved": *"//; s/"$//')
    if (( ${#foreign[@]} )); then
        log_error "$lockfile resolves packages somewhere other than the public registry:"
        # Only the scheme and host, once each: enough to act on without echoing every path.
        printf '%s\n' "${foreign[@]}" | sed -E 's|^([a-z+]+://[^/]+).*|  \1/…|' | sort -u >&2
        fail=1
    fi
done

if (( fail )); then
    die "Regenerate the lockfile with 'make lockfile', which rewrites its URLs to $public"
fi
log_success "${#lockfiles[@]} lockfile(s) resolve only to the public registry."
