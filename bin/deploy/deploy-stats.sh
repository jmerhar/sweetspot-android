#!/usr/bin/env bash
#
# Deploy stats.php to the stats server.
#
# Usage:
#   ./bin/deploy/deploy-stats.sh
#
set -euo pipefail
source "$(dirname "$0")/../lib/log.sh"

REMOTE="aurora"
REMOTE_DIR="/var/www/stats.sweetspot.today"
LOCAL_DIR="server/stats"

if [[ ! -f "$LOCAL_DIR/stats.php" ]]; then
    die "$LOCAL_DIR/stats.php not found"
fi

log_info "Deploying stats.php and clear-rate-limit.sh → $REMOTE:$REMOTE_DIR/"
scp "$LOCAL_DIR/stats.php" "$LOCAL_DIR/clear-rate-limit.sh" "$REMOTE:$REMOTE_DIR/"
log_success "Done."
