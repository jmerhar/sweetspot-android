#!/usr/bin/env bash
#
# Deploy stats.php to the stats server.
#
# Usage:
#   ./bin/deploy-stats.sh
#
set -euo pipefail

REMOTE="aurora"
REMOTE_DIR="/var/www/stats.sweetspot.today"
LOCAL_DIR="server"

if [[ ! -f "$LOCAL_DIR/stats.php" ]]; then
    echo "Error: $LOCAL_DIR/stats.php not found" >&2
    exit 1
fi

echo "Deploying stats.php and clear-rate-limit.sh → $REMOTE:$REMOTE_DIR/"
scp "$LOCAL_DIR/stats.php" "$LOCAL_DIR/clear-rate-limit.sh" "$REMOTE:$REMOTE_DIR/"
echo "Done."
