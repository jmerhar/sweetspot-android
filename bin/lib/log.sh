# Logging helpers shared by bin/ scripts, for consistent status and error output.
#
# Colours are emitted only when the target stream is a TTY, so piped output and CI logs
# stay plain. Informational output goes to stdout; warnings and errors go to stderr.
#
# Source directly (source "$SCRIPT_DIR/../lib/log.sh") or via lib/common.sh.
#
#   log_info    "msg"   # progress / status  -> stdout
#   log_success "msg"   # completion (green) -> stdout
#   log_warn    "msg"   # non-fatal warning  -> stderr
#   log_error   "msg"   # error, no exit     -> stderr
#   die         "msg"   # error, then exit 1 -> stderr

if [[ -t 1 ]]; then _LOG_GREEN=$'\033[0;32m'; _LOG_RESET1=$'\033[0m'; else _LOG_GREEN=''; _LOG_RESET1=''; fi
if [[ -t 2 ]]; then _LOG_RED=$'\033[0;31m'; _LOG_YELLOW=$'\033[0;33m'; _LOG_RESET2=$'\033[0m'; else _LOG_RED=''; _LOG_YELLOW=''; _LOG_RESET2=''; fi

log_info()    { printf '%s\n' "$*"; }
log_success() { printf '%s%s%s\n' "$_LOG_GREEN" "$*" "$_LOG_RESET1"; }
log_warn()    { printf '%sWARNING:%s %s\n' "$_LOG_YELLOW" "$_LOG_RESET2" "$*" >&2; }
log_error()   { printf '%sERROR:%s %s\n' "$_LOG_RED" "$_LOG_RESET2" "$*" >&2; }
die()         { log_error "$*"; exit 1; }
