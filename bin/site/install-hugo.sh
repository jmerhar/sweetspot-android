#!/usr/bin/env bash
#
# Downloads and installs the latest Hugo extended binary from GitHub.
# Used by CI workflows to avoid the deprecated peaceiris/actions-hugo action.

set -euo pipefail
source "$(dirname "$0")/../lib/log.sh"

VERSION=$(curl -fsSL https://api.github.com/repos/gohugoio/hugo/releases/latest \
  | grep '"tag_name"' | sed -E 's/.*"v([^"]+)".*/\1/')

log_info "Installing Hugo extended v${VERSION}..."

curl -fsSL "https://github.com/gohugoio/hugo/releases/download/v${VERSION}/hugo_extended_${VERSION}_linux-amd64.tar.gz" \
  | sudo tar -xzf - -C /usr/local/bin hugo

hugo version
