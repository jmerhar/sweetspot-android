#!/usr/bin/env bash
#
# Deploy the stats endpoint to the stats server.
#
# Ships the webroot files and, when it differs from the repo, the Apache vhost.
# The vhost carries the route map, so a stats.php that answers a new route is
# inert until the vhost naming that route is live — they belong in one deploy.
#
# Installing the vhost needs root on the remote, so that step runs over an
# interactive session and prompts for a sudo password. The webroot files do not.
#
# Usage:
#   ./bin/deploy/deploy-stats.sh
#   ./bin/deploy/deploy-stats.sh --check   # report drift, change nothing
#
set -euo pipefail
source "$(dirname "$0")/../lib/log.sh"

REMOTE="aurora"
REMOTE_DIR="/var/www/stats.sweetspot.today"
LOCAL_DIR="server/stats"
VHOST="stats.sweetspot.today.conf"
REMOTE_VHOST="/etc/apache2/sites-available/$VHOST"

CHECK_ONLY=false
[[ "${1:-}" == "--check" ]] && CHECK_ONLY=true

for f in stats.php "$VHOST"; do
    [[ -f "$LOCAL_DIR/$f" ]] || die "$LOCAL_DIR/$f not found"
done

# --- Webroot files ---

if [[ "$CHECK_ONLY" == false ]]; then
    log_info "Deploying stats.php and clear-rate-limit.sh → $REMOTE:$REMOTE_DIR/"
    scp -q "$LOCAL_DIR/stats.php" "$LOCAL_DIR/clear-rate-limit.sh" "$REMOTE:$REMOTE_DIR/"
fi

# --- Vhost ---
#
# Reading the live vhost needs no privileges, so drift is detected before asking
# for a password, and an unchanged vhost costs neither a prompt nor a reload.

log_info "Comparing $VHOST with the live copy"
live=$(ssh "$REMOTE" "cat $REMOTE_VHOST" 2>/dev/null) || die "cannot read $REMOTE:$REMOTE_VHOST"

if diff -q <(printf '%s\n' "$live") "$LOCAL_DIR/$VHOST" >/dev/null; then
    log_success "Vhost already matches — no reload needed."
    [[ "$CHECK_ONLY" == false ]] && log_success "Done."
    exit 0
fi

log_warn "Vhost differs from the live copy:"
diff -u <(printf '%s\n' "$live") "$LOCAL_DIR/$VHOST" || true

if [[ "$CHECK_ONLY" == true ]]; then
    log_warn "--check given; nothing installed."
    exit 0
fi

log_info "Installing vhost (prompts for sudo on $REMOTE)"
scp -q "$LOCAL_DIR/$VHOST" "$REMOTE:/tmp/$VHOST"

# Validated before the reload and rolled back if Apache rejects it, so a bad
# config cannot take the endpoint down: a reload on a broken vhost is refused by
# Apache, but leaving the broken file in place would break the next reload too.
ssh -t "$REMOTE" "sudo sh -c '
    set -e
    cp \"$REMOTE_VHOST\" \"$REMOTE_VHOST.bak\"
    cp \"/tmp/$VHOST\" \"$REMOTE_VHOST\"
    if apache2ctl configtest; then
        systemctl reload apache2
        echo \"vhost installed and Apache reloaded\"
    else
        cp \"$REMOTE_VHOST.bak\" \"$REMOTE_VHOST\"
        echo \"configtest failed — vhost restored, Apache untouched\" >&2
        exit 1
    fi
'" || die "vhost install failed (previous config restored)"

ssh "$REMOTE" "rm -f /tmp/$VHOST"
log_success "Done."
