#!/usr/bin/env bash
#
# Deploy the feedback Worker (feedback.sweetspot.today) to Cloudflare.
#
# Turns app "Report a problem" / "Send feedback" submissions into GitHub issues, emails opted-in
# reporters on activity, and serves the one-click unsubscribe endpoint. Runs `wrangler deploy` from
# server/feedback-worker/, then health-checks the live endpoint.
#
# Prerequisites (one-time — see server/feedback-worker/README.md):
#   - `wrangler login` (deploys as the account owning the sweetspot.today zone)
#   - the three secrets set via `wrangler secret put` (GITHUB_TOKEN, WEBHOOK_SECRET, BREVO_API_KEY)
#   - the KV namespace id filled into wrangler.jsonc
#
# Usage:
#   ./bin/deploy/deploy-feedback.sh            # deploy
#   ./bin/deploy/deploy-feedback.sh --dry-run  # extra args are forwarded to `wrangler deploy`
#
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
WORKER_DIR="$ROOT/server/feedback-worker"
HEALTH_URL="https://feedback.sweetspot.today/"

if [[ ! -f "$WORKER_DIR/wrangler.jsonc" ]]; then
    echo "Error: $WORKER_DIR/wrangler.jsonc not found" >&2
    exit 1
fi

echo "Deploying feedback Worker from $WORKER_DIR …"
( cd "$WORKER_DIR" && npx --yes wrangler deploy "$@" )

# A --dry-run doesn't publish anything, so skip the live health check for it.
for arg in "$@"; do
    [[ "$arg" == "--dry-run" ]] && { echo "Dry run — skipping health check."; exit 0; }
done

echo "Verifying $HEALTH_URL …"
if body="$(curl -fsS --max-time 15 "$HEALTH_URL")" && [[ "$body" == "ok" ]]; then
    echo "Health check OK."
else
    echo "Warning: health check did not return 'ok' (got: ${body:-<none>}). DNS/propagation may lag; retry shortly." >&2
    exit 1
fi
