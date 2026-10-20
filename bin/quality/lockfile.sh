#!/usr/bin/env bash
#
# Regenerate server/feedback-worker/package-lock.json.
#
# Two things this does that a plain `npm install` does not:
#
#  1. It installs inside a Linux container, matching CI, so the lockfile records the native
#     packages a Linux install needs rather than only the ones a macOS install pulled in.
#  2. It rewrites every resolved URL to the public registry. On a network that proxies npm, the
#     lockfile otherwise records that proxy's hostname on every line, and this repository is public.
#
# Requires Docker. Run via `make lockfile`.
#
set -euo pipefail
source "$(dirname "$0")/../lib/log.sh"

dir="$(cd "$(dirname "$0")/../../server/feedback-worker" && pwd)"
public="https://registry.npmjs.org/"

# The registry npm is configured to reach outside this project, whose .npmrc names the public one:
# the install has to go through whatever this machine can reach.
registry=$(cd / && npm config get registry 2>/dev/null || echo "$public")

log_info "Installing through: $registry"
log_info "Recording:          $public"

rm -f "$dir/package-lock.json"
# The project's .npmrc goes in for its other settings (the minimum release age); the registry given
# on the command line overrides its registry line.
docker run --rm \
    -v "$dir/package.json:/app/package.json:ro" \
    -v "$dir/.npmrc:/app/.npmrc:ro" \
    -v "$dir:/output" \
    -w /app \
    node:24-alpine \
    sh -c "npm install --registry='$registry' --no-fund --no-audit --loglevel=error && cp package-lock.json /output/package-lock.json"

# A proxied registry records URLs of the form https://<host>/artifactory/api/npm/<repo>/<pkg>.
# The pattern is shaped around the path rather than any particular hostname.
sed -E 's|https://[^"]+/artifactory/api/npm/[^/]+/|'"$public"'|g' "$dir/package-lock.json" > "$dir/package-lock.json.tmp"
mv "$dir/package-lock.json.tmp" "$dir/package-lock.json"

"$(dirname "$0")/check-lockfile.sh" "$dir/package-lock.json"
log_success "Lockfile regenerated."
