#!/usr/bin/env bash
# Clears the stats endpoint rate limiter.
# Apache's PrivateTmp puts it under /tmp/systemd-private-*/tmp/ instead of /tmp/.
set -euo pipefail
sudo find /tmp -name sweetspot_rate -type d -exec rm -rf {} + 2>/dev/null
echo "Rate limit cleared."
