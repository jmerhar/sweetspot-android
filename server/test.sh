#!/usr/bin/env bash
#
# Smoke tests for the SweetSpot stats endpoint.
#
# Usage:
#   ./test.sh                              # test against production
#   ./test.sh http://localhost/report       # test against a local instance
#
# Before running, clear the rate limiter on the server:
#   ssh yourserver server/clear-rate-limit.sh
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
        [ -n "$LAST_BODY" ] && echo "        body: $LAST_BODY"
        local server
        server=$(echo "$LAST_HEADERS" | grep -i '^server:' | tr -d '\r')
        [ -n "$server" ] && echo "        $server"
        ((FAIL++)) || true
    fi
}

LAST_BODY=""
LAST_HEADERS=""

post() {
    local ua="${2:-SweetSpot/4.0}"
    local tmpfile hdrfile code
    tmpfile=$(mktemp)
    hdrfile=$(mktemp)
    code=$(curl -s -o "$tmpfile" -D "$hdrfile" -w '%{http_code}' -X POST "$URL" \
        -H "Content-Type: application/json" \
        -H "User-Agent: $ua" \
        -d "$1")
    LAST_BODY=$(cat "$tmpfile")
    LAST_HEADERS=$(cat "$hdrfile")
    rm -f "$tmpfile" "$hdrfile"
    echo "$code"
}

echo "Testing $URL"
echo

# --- Valid payload ---

echo "Valid payload:"

CODE=$(post '{
  "v": 1,
  "app": "4.0",
  "records": [
    {
      "z": "NL",
      "s": "entsoe",
      "d": "phone",
      "r": [
        {"t": 1711700000, "ok": true},
        {"t": 1711703600, "ok": false, "e": "HTTP_503"}
      ]
    },
    {
      "z": "DE_LU",
      "s": "energycharts",
      "d": "watch",
      "r": [{"t": 1711700000, "ok": false, "e": "TIMEOUT"}]
    },
    {
      "z": "FI",
      "s": "spothinta",
      "d": "phone",
      "r": [{"t": 1711701000, "ok": true}]
    }
  ]
}')
assert_code "multiple groups with success and failure records" 200 "$CODE"

echo

# --- Rejected payloads ---

echo "Rejected payloads:"

CODE=$(curl -s -o /dev/null -w '%{http_code}' -X GET "$URL" \
    -H "User-Agent: SweetSpot/4.0")
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

post '{"v":1,"app":"4.0","records":[{"z":"NL","s":"entsoe","d":"phone","r":[{"t":1711700000,"ok":true}]}]}' > /dev/null 2>&1
CODE=$(post '{"v":1,"app":"4.0","records":[{"z":"NL","s":"entsoe","d":"phone","r":[{"t":1711700000,"ok":true}]}]}')
assert_code "repeated request rate-limited" 429 "$CODE"

echo
echo "---"
echo "$((PASS + FAIL)) tests: $PASS passed, $FAIL failed"
[ "$FAIL" -eq 0 ]
