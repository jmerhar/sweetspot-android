#!/usr/bin/env bash
#
# Smoke tests for the SweetSpot stats endpoint.
#
# Usage:
#   ./test.sh                              # test against production
#   ./test.sh http://localhost/report       # test against a local instance
#
# Before running, clear the rate limiter on the server:
#   ssh yourserver server/stats/clear-rate-limit.sh
#
set -euo pipefail

URL="${1:-https://stats.sweetspot.today/report}"
PASS=0
FAIL=0

# Colours (disabled if not a terminal)
if [ -t 1 ]; then
    GREEN='\033[0;32m' RED='\033[0;31m' RESET='\033[0m'
else
    GREEN='' RED='' RESET=''
fi

assert_code() {
    local name="$1" expected="$2" actual="$3"
    if [ "$actual" -eq "$expected" ]; then
        echo -e "  ${GREEN}PASS${RESET}  $name (HTTP $actual)"
        ((PASS++)) || true
    else
        echo -e "  ${RED}FAIL${RESET}  $name — expected HTTP $expected, got $actual"
        local body server
        # `|| true` on both: under `set -e` with `pipefail`, a grep that matches
        # nothing would otherwise abort the whole run from inside the branch that
        # reports a failure, hiding every later test.
        body=$(last_body)
        [ -n "$body" ] && echo "        body: $body"
        server=$(grep -i '^server:' "$HDR_FILE" 2>/dev/null | tr -d '\r' || true)
        [ -n "$server" ] && echo "        $server"
        ((FAIL++)) || true
    fi
}

assert_contains() {
    local name="$1" needle="$2" haystack="$3"
    if [[ "$haystack" == *"$needle"* ]]; then
        echo -e "  ${GREEN}PASS${RESET}  $name"
        ((PASS++)) || true
    else
        echo -e "  ${RED}FAIL${RESET}  $name — expected body to contain '$needle'"
        echo "        body: $haystack"
        ((FAIL++)) || true
    fi
}

# The request helpers below are called as `CODE=$(post ...)`, which runs them in a
# subshell — so they record the response to these files rather than to variables,
# which would be discarded along with the subshell.
BODY_FILE=$(mktemp)
HDR_FILE=$(mktemp)
trap 'rm -f "$BODY_FILE" "$HDR_FILE"' EXIT

last_body() { cat "$BODY_FILE" 2>/dev/null || true; }

# Marks every request from this suite as synthetic, so the rejections it provokes
# on purpose are tagged as such in the endpoint_reject measurement and excluded
# from the rejection alert. Without it, a single test run looks like the endpoint
# turning away a dozen real reports.
SYNTHETIC_HEADER="X-SweetSpot-Synthetic: 1"

post() {
    local ua="${2:-SweetSpot/0.0.0}"
    curl -s -o "$BODY_FILE" -D "$HDR_FILE" -w '%{http_code}' -X POST "$URL" \
        -H "Content-Type: application/json" \
        -H "User-Agent: $ua" \
        -H "$SYNTHETIC_HEADER" \
        -d "$1"
}

echo "Testing $URL"
echo

# --- Valid payload ---

echo "Valid payload:"

# Test traffic uses the synthetic marker (s="test", z="ZZ", app="0.0.0") so
# successful writes are excluded from the Grafana dashboard automatically — see
# the "Stats Backend & Monitoring" section in CLAUDE.md. Never use a real source
# id here, or these smoke-test writes would pollute the analytics.
CODE=$(post '{
  "v": 1,
  "app": "0.0.0",
  "records": [
    {
      "z": "ZZ",
      "s": "test",
      "d": "phone",
      "r": [
        {"t": 1711700000, "ok": true},
        {"t": 1711703600, "ok": false, "e": "HTTP_503"}
      ]
    },
    {
      "z": "ZZ",
      "s": "test",
      "d": "watch",
      "r": [{"t": 1711700000, "ok": false, "e": "TIMEOUT"}]
    },
    {
      "z": "ZZ",
      "s": "test",
      "d": "phone",
      "r": [{"t": 1711701000, "ok": true}]
    }
  ]
}')
assert_code "multiple groups with success and failure records" 200 "$CODE"

echo

# --- Health probe ---
#
# The route an uptime monitor polls. It must answer with an ordinary GET, with a
# monitor's own User-Agent, and — because it is polled far more often than once
# per rate-limit window — without being rate-limited.

echo "Health probe:"

HEALTH_URL="${URL%/report}/health"
KUMA_UA="Uptime-Kuma/1.23"

health_get() {
    curl -s -o "$BODY_FILE" -D "$HDR_FILE" -w '%{http_code}' -X GET "$HEALTH_URL" \
        -H "User-Agent: $KUMA_UA"
}

CODE=$(health_get)
assert_code "health probe returns 200" 200 "$CODE"
assert_contains "health probe reports ok" '"ok":true' "$(last_body)"

CODE=$(health_get)
assert_code "health probe is not rate-limited" 200 "$CODE"

echo

# --- Rejection probe ---
#
# The second monitored route. Reports whether the endpoint has been turning
# requests away; must also answer a plain GET and be exempt from rate limiting.

echo "Rejection probe:"

REJECTS_URL="${URL%/report}/rejects"

rejects_get() {
    curl -s -o "$BODY_FILE" -D "$HDR_FILE" -w '%{http_code}' -X GET "$REJECTS_URL" \
        -H "User-Agent: $KUMA_UA"
}

CODE=$(rejects_get)
assert_code "rejection probe returns 200" 200 "$CODE"
assert_contains "rejection probe reports a count" '"rejects":' "$(last_body)"
assert_contains "rejection probe links to the details" 'grafana' "$(last_body)"

CODE=$(rejects_get)
assert_code "rejection probe is not rate-limited" 200 "$CODE"

echo

# --- Rejected payloads ---

echo "Rejected payloads:"

CODE=$(curl -s -o /dev/null -w '%{http_code}' -X GET "$URL" \
    -H "User-Agent: SweetSpot/0.0.0" -H "$SYNTHETIC_HEADER")
assert_code "GET method rejected" 405 "$CODE"

CODE=$(post '{"v":1,"app":"4.0","records":[]}' "Mozilla/5.0")
assert_code "wrong User-Agent" 403 "$CODE"

CODE=$(post 'not json')
assert_code "invalid JSON" 400 "$CODE"

CODE=$(post '{"v":2,"app":"4.0","records":[]}')
assert_code "unsupported version" 400 "$CODE"

CODE=$(post '{"v":1,"records":[]}')
assert_code "missing app version" 400 "$CODE"

CODE=$(post '{"v":1,"app":"4.0"}')
assert_code "missing records" 400 "$CODE"

CODE=$(post '{"v":1,"app":"4.0","records":[{"z":"NL","s":"entsoe","d":"phone","r":[]}]}')
assert_code "empty records array" 400 "$CODE"

CODE=$(post '{
  "v": 1,
  "app": "4.0",
  "records": [{"z":"nl","s":"entsoe","d":"phone","r":[{"t":1711700000,"ok":true}]}]
}')
assert_code "lowercase zone rejected" 400 "$CODE"

CODE=$(post '{
  "v": 1,
  "app": "4.0",
  "records": [{"z":"NL","s":"ENTSOE","d":"phone","r":[{"t":1711700000,"ok":true}]}]
}')
assert_code "uppercase source rejected" 400 "$CODE"

CODE=$(post '{
  "v": 1,
  "app": "4.0",
  "records": [{"z":"NL","s":"entsoe","d":"tablet","r":[{"t":1711700000,"ok":true}]}]
}')
assert_code "invalid device rejected" 400 "$CODE"

CODE=$(post '{
  "v": 1,
  "app": "4.0",
  "records": [{"z":"NL","s":"entsoe","d":"phone","r":[{"t":1000000000,"ok":true}]}]
}')
assert_code "timestamp too old rejected" 400 "$CODE"

CODE=$(post '{
  "v": 1,
  "app": "4.0",
  "records": [{"z":"NL","s":"entsoe","d":"phone","r":[{"t":1711700000,"ok":false}]}]
}')
assert_code "failure without error category rejected" 400 "$CODE"

echo

# --- Rate limiting ---

echo "Rate limiting:"

post '{"v":1,"app":"0.0.0","records":[{"z":"ZZ","s":"test","d":"phone","r":[{"t":1711700000,"ok":true}]}]}' > /dev/null 2>&1
CODE=$(post '{"v":1,"app":"0.0.0","records":[{"z":"ZZ","s":"test","d":"phone","r":[{"t":1711700000,"ok":true}]}]}')
assert_code "repeated request rate-limited" 429 "$CODE"

echo
echo "---"
echo "$((PASS + FAIL)) tests: $PASS passed, $FAIL failed"
[ "$FAIL" -eq 0 ]
