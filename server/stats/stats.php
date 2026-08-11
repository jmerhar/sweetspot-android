<?php
/**
 * SweetSpot API stats ingestion endpoint.
 *
 * Accepts JSON payloads from the SweetSpot Android app containing anonymous
 * API reliability statistics. Validates input, rate-limits by IP, and writes
 * to InfluxDB 3 Core via the HTTP write API (line protocol).
 *
 * Deployed behind Cloudflare proxy at stats.sweetspot.today.
 * Requires PHP 7.4+.
 *
 * Three routes, all mapped to this script by the vhost:
 *
 *   POST /report   — ingest a payload from the app (measurement `api_fetch`).
 *   GET  /health   — liveness probe for an uptime monitor. Writes one point to
 *                    `ingest_canary` and returns 200 only once InfluxDB acks it,
 *                    so it proves the whole path (PHP → InfluxDB auth → write)
 *                    rather than merely that Apache is up. Being driven by a
 *                    monitor instead of by app traffic, it stays meaningful
 *                    through quiet periods when no device happens to report.
 *   GET  /rejects  — reports whether the endpoint has been turning requests away
 *                    recently, for a second monitor. Rejections are invisible to
 *                    a heartbeat that only fires on success: a validation rule
 *                    the app has outgrown returns 400, the app discards 4xx as
 *                    corrupt and never retries, and the data is lost silently.
 *
 * Every non-2xx response is recorded to `endpoint_reject`, which is what
 * /rejects counts. Note that /health is blind to this class of fault by
 * construction — its payload is always valid — which is why both probes exist.
 *
 * Expected payload (POST, Content-Type: application/json):
 * {
 *   "v": 2,
 *   "app": "5.1.2",
 *   "lang": "nl",
 *   "status": "trial",
 *   "records": [
 *     {
 *       "z": "NL",
 *       "s": "entsoe",
 *       "d": "phone",
 *       "r": [
 *         {"t": 1711700000, "ok": true},
 *         {"t": 1711703600, "ok": false, "e": "TIMEOUT"}
 *       ]
 *     }
 *   ]
 * }
 */

// --- Configuration ---
// Overridable so the endpoint can be exercised against a stub instead of a live
// InfluxDB. Unset in production, where the default points at the local instance.
define('INFLUXDB_URL', getenv('INFLUX_URL') ?: ($_SERVER['INFLUX_URL'] ?? 'http://localhost:8181/api/v3/write_lp?db=sweetspot&precision=second'));
define('INFLUXDB_QUERY_URL', getenv('INFLUX_QUERY_URL') ?: ($_SERVER['INFLUX_QUERY_URL'] ?? 'http://localhost:8181/api/v3/query_sql'));
define('INFLUXDB_TOKEN', getenv('INFLUX_TOKEN') ?: ($_SERVER['INFLUX_TOKEN'] ?? ''));
// Window the rejection probe counts over. Also the period an alert stays raised
// after the last rejection, since it clears itself once the window moves past.
define('REJECT_WINDOW_HOURS', 6);
// Where to see the rejections a raised alert is about. Kuma truncates a response
// body to 47 characters in its failure message, far too short for a URL, so the
// probe reports the count there and carries the link in its body and in the
// monitor's own description.
define('GRAFANA_REJECT_URL', 'https://grafana.aurora.merhar.si/d/sweetspot-api-stats/sweetspot-api-reliability?viewPanel=panel-12&from=now-6h&to=now');
// Optional Uptime Kuma push monitor URL. When set (via SetEnv in .htaccess),
// each successful ingestion pings this URL so Kuma alerts on missing heartbeats.
// Empty disables the feature. Any query string is ignored; only the base is used.
define('KUMA_PUSH_URL', getenv('KUMA_PUSH_URL') ?: ($_SERVER['KUMA_PUSH_URL'] ?? ''));
define('RATE_LIMIT_DIR', '/tmp/sweetspot_rate');
define('RATE_LIMIT_SECONDS', 300); // 5 minutes per IP
define('MAX_BODY_SIZE', 65536); // 64 KB
define('MAX_RECORDS', 500);
define('INFLUX_WRITE_TIMEOUT', 10);
// Monitoring writes (health canary, rejection record) are capped much shorter than
// the ingest write: they are side effects of a request whose outcome is already
// decided, so they must not stretch its latency if InfluxDB is slow or down.
define('INFLUX_MONITOR_WRITE_TIMEOUT', 3);
// Requests carrying this header are tagged `synthetic=true` in `endpoint_reject`,
// so the smoke-test suite's deliberate rejections can be excluded from alerting
// the same way `source="test"` excludes its writes from the Grafana dashboard.
// Suppressing one's own alerts is the only thing spoofing it achieves.
define('SYNTHETIC_HEADER', 'HTTP_X_SWEETSPOT_SYNTHETIC');

// --- Helpers ---

/** Raw request body, captured once for error logging. */
$_rawBody = null;

/**
 * Sends a JSON error response, logs the reason and payload, then exits.
 *
 * Every rejection is also recorded to InfluxDB, since this is the one point all
 * of them pass through.
 */
function error_response(int $code, string $message): void {
    global $_rawBody;
    $truncated = $_rawBody !== null ? substr($_rawBody, 0, 4096) : '(not read yet)';
    error_log("SweetSpot stats $code: $message | payload: $truncated");
    record_rejection($code, $message);
    http_response_code($code);
    header('Content-Type: application/json');
    echo json_encode(['error' => $message]);
    exit;
}

/**
 * Records a rejected request to the `endpoint_reject` measurement.
 *
 * Tags carry only the HTTP status and this script's own reason literal — never
 * anything taken from the request — so a crafted payload cannot inject line
 * protocol, and the tag set stays low-cardinality.
 *
 * Best-effort by design: failures are swallowed and the timeout is short, so
 * recording a rejection can neither change nor delay the response the client
 * gets. Storing every non-2xx (not just 4xx) keeps the policy in the alert rule,
 * which is where it can be tuned without a deploy.
 */
function record_rejection(int $code, string $reason): void {
    $escape = static function (string $v): string {
        return str_replace([',', ' ', '='], ['\\,', '\\ ', '\\='], $v);
    };
    $synthetic = (($_SERVER[SYNTHETIC_HEADER] ?? '') === '1') ? 'true' : 'false';
    $line = "endpoint_reject,code={$code},reason={$escape($reason)},synthetic={$synthetic} count=1i " . time();
    write_to_influxdb($line, INFLUX_MONITOR_WRITE_TIMEOUT);
}

/**
 * Validates a string against a regex whitelist.
 */
function validate_string(string $value, string $pattern, int $maxLen = 64): bool {
    return strlen($value) <= $maxLen && preg_match($pattern, $value) === 1;
}

/**
 * Checks and enforces per-IP rate limiting using file timestamps.
 */
function check_rate_limit(): void {
    if (!is_dir(RATE_LIMIT_DIR)) {
        mkdir(RATE_LIMIT_DIR, 0755, true);
    }

    // mod_remoteip rewrites REMOTE_ADDR to the real client IP taken from Cloudflare's
    // CF-Connecting-IP, but only for connections from Cloudflare's edge (see the
    // RemoteIPTrustedProxy allowlist in the vhost). Reading REMOTE_ADDR rather than the raw
    // header means a client reaching the origin directly cannot spoof its IP past the limiter.
    $ip = $_SERVER['REMOTE_ADDR'] ?? 'unknown';
    $hash = hash('sha256', $ip);
    $file = RATE_LIMIT_DIR . '/' . $hash;

    if (file_exists($file)) {
        $lastTime = (int)file_get_contents($file);
        if (time() - $lastTime < RATE_LIMIT_SECONDS) {
            error_response(429, 'Rate limit exceeded');
        }
    }

    file_put_contents($file, (string)time());

    // Clean up old rate limit files (>1 hour old)
    foreach (glob(RATE_LIMIT_DIR . '/*') as $f) {
        if (time() - filemtime($f) > 3600) {
            unlink($f);
        }
    }
}

/**
 * Converts validated stats records to InfluxDB line protocol.
 *
 * Measurement: api_fetch
 * Tags: zone, source, device, app, outcome, error, lang, status
 * Fields: count=1i, duration_ms=<ms>i
 * Timestamp: epoch seconds (precision=second is set in the URL)
 */
function to_line_protocol(array $data): string {
    $lines = [];
    $app = $data['app'];
    $lang = $data['lang'] ?? '';
    $status = $data['status'] ?? 'unknown';

    foreach ($data['records'] as $group) {
        $zone = $group['z'];
        $source = $group['s'];
        $device = $group['d'];

        foreach ($group['r'] as $record) {
            $timestamp = $record['t'];
            $ok = $record['ok'];
            $outcome = $ok ? 'ok' : 'fail';
            $error = $ok ? 'none' : ($record['e'] ?? 'unknown');

            // Escape tag values (commas, spaces, equals)
            $escape = function($v) { return str_replace([',', ' ', '='], ['\\,', '\\ ', '\\='], $v); };

            $tagParts = [
                "zone={$escape($zone)}",
                "source={$escape($source)}",
                "device={$escape($device)}",
                "app={$escape($app)}",
                "outcome={$outcome}",
                "error={$escape($error)}",
            ];
            if ($lang !== '') {
                $tagParts[] = "lang={$escape($lang)}";
            }
            if ($status !== 'unknown') {
                $tagParts[] = "status={$escape($status)}";
            }

            $tags = implode(',', $tagParts);
            $durationMs = $record['ms'] ?? 0;
            $lines[] = "api_fetch,{$tags} count=1i,duration_ms={$durationMs}i {$timestamp}";
        }
    }

    return implode("\n", $lines);
}

/**
 * Writes line protocol data to InfluxDB.
 *
 * @param int $timeout Seconds to allow for the write. Callers on the ingest path
 *                     pass INFLUX_WRITE_TIMEOUT; monitoring side-writes pass the
 *                     shorter INFLUX_MONITOR_WRITE_TIMEOUT.
 * @return array{ok: bool, http_code: int, curl_error: string, response: string}
 */
function write_to_influxdb(string $lineProtocol, int $timeout): array {
    $ch = curl_init(INFLUXDB_URL);
    curl_setopt_array($ch, [
        CURLOPT_POST => true,
        CURLOPT_POSTFIELDS => $lineProtocol,
        CURLOPT_HTTPHEADER => [
            'Content-Type: text/plain',
            'Authorization: Bearer ' . INFLUXDB_TOKEN,
        ],
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_TIMEOUT => $timeout,
    ]);

    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    $curlError = curl_error($ch);
    curl_close($ch);

    if ($httpCode !== 204) {
        error_log("InfluxDB write failed: HTTP $httpCode, curl_error='$curlError', response='$response', url='" . INFLUXDB_URL . "'");
    }

    return ['ok' => $httpCode === 204, 'http_code' => $httpCode, 'curl_error' => $curlError, 'response' => $response ?: ''];
}

/**
 * Sends a best-effort heartbeat to an Uptime Kuma push monitor.
 *
 * Called after a successful InfluxDB write so Kuma can alert if no successful
 * stats ingestion occurs within its configured heartbeat window — catching
 * silent pipeline breakage (validation rejects, write failures, app-side bugs).
 *
 * Deliberately fault-tolerant: a short timeout caps added latency, and all
 * errors (Kuma down, bad URL) are swallowed so the heartbeat can never affect
 * the response returned to the app. No-op when KUMA_PUSH_URL is unset.
 *
 * @param int $records Number of records ingested, sent as the Kuma status message.
 */
function ping_kuma(int $records): void {
    if (KUMA_PUSH_URL === '') {
        return;
    }

    // Normalise to the bare push URL and build our own query, so a pasted
    // default Kuma URL (which includes ?status=up&msg=OK&ping=) doesn't produce
    // duplicate query parameters.
    $base = strtok(KUMA_PUSH_URL, '?');
    $url = $base . '?status=up&msg=' . rawurlencode("ingested {$records} records");

    $ch = curl_init($url);
    curl_setopt_array($ch, [
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_CONNECTTIMEOUT => 2,
        CURLOPT_TIMEOUT => 3,
        CURLOPT_NOSIGNAL => true,
    ]);
    curl_exec($ch);
    curl_close($ch);
}

/**
 * Runs a read-only SQL query against InfluxDB.
 *
 * @return array{ok: bool, rows: array} `rows` is the decoded result set, empty
 *                                      when the query failed.
 */
function query_influxdb(string $sql, int $timeout): array {
    $ch = curl_init(INFLUXDB_QUERY_URL);
    curl_setopt_array($ch, [
        CURLOPT_POST => true,
        CURLOPT_POSTFIELDS => json_encode(['db' => 'sweetspot', 'q' => $sql, 'format' => 'json']),
        CURLOPT_HTTPHEADER => [
            'Content-Type: application/json',
            'Authorization: Bearer ' . INFLUXDB_TOKEN,
        ],
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_TIMEOUT => $timeout,
    ]);

    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    $curlError = curl_error($ch);
    curl_close($ch);

    if ($httpCode !== 200) {
        error_log("InfluxDB query failed: HTTP $httpCode, curl_error='$curlError', response='$response'");
        return ['ok' => false, 'rows' => []];
    }

    $rows = json_decode((string)$response, true);
    if (!is_array($rows)) {
        error_log("InfluxDB query returned unparseable JSON: '$response'");
        return ['ok' => false, 'rows' => []];
    }

    return ['ok' => true, 'rows' => $rows];
}

/**
 * Answers the rejection probe and exits.
 *
 * Counts the requests the endpoint turned away over the recent window, ignoring
 * synthetic (smoke-test) traffic and 429s, which are expected rather than a
 * fault. An uptime monitor watches for `"clean":true`, so rejections raise an
 * alert that clears itself once the window moves past them.
 *
 * The time range is bounded rather than open-ended: an unbounded aggregate over
 * a table with recent writes can take minutes to plan on InfluxDB 3 Core, and
 * repeated probes would then stack up and pin the CPU.
 *
 * Fails closed with 503 if the query itself fails, since reporting "clean"
 * because the count could not be read would hide the very thing being watched.
 */
function handle_rejects(): void {
    $sql = "SELECT count(*) AS n FROM endpoint_reject"
        . " WHERE time >= now() - INTERVAL '" . REJECT_WINDOW_HOURS . " hours'"
        . " AND synthetic = 'false' AND code != '429'";
    $result = query_influxdb($sql, INFLUX_MONITOR_WRITE_TIMEOUT);

    header('Content-Type: application/json');
    header('Cache-Control: no-store, max-age=0');

    if (!$result['ok']) {
        http_response_code(503);
        echo json_encode(['clean' => false, 'error' => 'Rejection query failed']);
        exit;
    }

    $count = (int)($result['rows'][0]['n'] ?? 0);
    http_response_code(200);
    // `clean` and `rejects` lead so both survive the monitor's truncation of the
    // body into its alert text.
    echo json_encode([
        'clean' => $count === 0,
        'rejects' => $count,
        'window_hours' => REJECT_WINDOW_HOURS,
        'details' => GRAFANA_REJECT_URL,
    ]);
    exit;
}

/**
 * Path of the current request, without query string.
 *
 * The vhost maps several paths onto this one script, and mod_rewrite leaves
 * REQUEST_URI as the client sent it, so this is what distinguishes them.
 */
function request_path(): string {
    return parse_url($_SERVER['REQUEST_URI'] ?? '/', PHP_URL_PATH) ?: '/';
}

/**
 * Answers the health probe and exits.
 *
 * Writes a single point and reports success only once InfluxDB acks it, so a
 * monitor watching this route sees the same breakage an app report would hit:
 * PHP failing, the token being wrong, or InfluxDB being down or refusing writes.
 *
 * The canary lands in its own measurement rather than `api_fetch` so it cannot
 * skew the reliability figures, and so it can be dropped wholesale — InfluxDB 3
 * Core has no row DELETE, but whole tables can be dropped.
 *
 * Deliberately does not use error_response(): a failing probe is not a client
 * rejection, and recording it as one would double-report the same outage.
 */
function handle_health(): void {
    $result = write_to_influxdb('ingest_canary,probe=health count=1i ' . time(), INFLUX_MONITOR_WRITE_TIMEOUT);

    header('Content-Type: application/json');
    // Being a GET, this response is cacheable by default, and a cached 200 served
    // while the pipeline is down would leave the monitor reporting healthy through
    // the whole outage — the exact blindness the probe exists to remove.
    header('Cache-Control: no-store, max-age=0');
    if (!$result['ok']) {
        http_response_code(503);
        echo json_encode(['ok' => false, 'error' => 'Storage write failed', 'influx_code' => $result['http_code']]);
        exit;
    }
    http_response_code(200);
    echo json_encode(['ok' => true, 'probe' => 'ingest']);
    exit;
}

// --- Main ---

// The monitored probes are answered before the POST and User-Agent checks below,
// so an uptime monitor can reach them with an ordinary GET and its own
// User-Agent, and before the rate limiter, which would otherwise 429 a monitor
// polling more often than once per RATE_LIMIT_SECONDS.
switch (request_path()) {
    case '/health':
        handle_health();
        break;
    case '/rejects':
        handle_rejects();
        break;
}

// Only accept POST
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    error_response(405, 'Method not allowed');
}

// Check User-Agent
$ua = $_SERVER['HTTP_USER_AGENT'] ?? '';
if (strpos($ua, 'SweetSpot/') !== 0) {
    error_response(403, 'Invalid User-Agent');
}

// Read and validate body
$body = file_get_contents('php://input');
$_rawBody = $body;
if (strlen($body) > MAX_BODY_SIZE) {
    error_response(413, 'Payload too large');
}

$data = json_decode($body, true);
if ($data === null) {
    error_response(400, 'Invalid JSON');
}

// Validate structure
if (!isset($data['v']) || !in_array($data['v'], [1, 2], true)) {
    error_response(400, 'Unsupported version');
}

if (!isset($data['app']) || !validate_string($data['app'], '/^[\d.]+$/', 16)) {
    error_response(400, 'Invalid app version');
}

// v2 fields: lang and status (optional for v1 backwards compatibility)
$lang = $data['lang'] ?? '';
$status = $data['status'] ?? 'unknown';

if ($lang !== '' && !validate_string($lang, '/^[a-z]{2,3}(-[A-Za-z]{2,8})?$/', 16)) {
    error_response(400, 'Invalid language');
}
// 'subscribed' is the current app value (v5.x billing migration); 'unlocked' is
// kept for older app versions still deployed in the field.
if (!in_array($status, ['trial', 'subscribed', 'unlocked', 'expired', 'unknown'], true)) {
    error_response(400, 'Invalid status');
}

if (!isset($data['records']) || !is_array($data['records'])) {
    error_response(400, 'Missing records');
}

// Validate and count records
$totalRecords = 0;
foreach ($data['records'] as $group) {
    if (!isset($group['z']) || !validate_string($group['z'], '/^[A-Z][A-Z0-9_]{0,15}$/')) {
        error_response(400, 'Invalid zone');
    }
    if (!isset($group['s']) || !validate_string($group['s'], '/^[a-z][a-z0-9_]{0,31}$/')) {
        error_response(400, 'Invalid source');
    }
    if (!isset($group['d']) || !in_array($group['d'], ['phone', 'watch'], true)) {
        error_response(400, 'Invalid device');
    }
    if (!isset($group['r']) || !is_array($group['r'])) {
        error_response(400, 'Missing records in group');
    }

    foreach ($group['r'] as $record) {
        if (!isset($record['t']) || !is_int($record['t']) || $record['t'] < 1_700_000_000 || $record['t'] > 4_102_444_800) {
            error_response(400, 'Invalid timestamp');
        }
        if (!isset($record['ok']) || !is_bool($record['ok'])) {
            error_response(400, 'Invalid ok field');
        }
        if (!$record['ok']) {
            if (!isset($record['e']) || !validate_string($record['e'], '/^[A-Z][A-Z0-9_]{0,31}$/')) {
                error_response(400, 'Invalid error category');
            }
        }
        // Duration is optional (v2+), default 0
        if (isset($record['ms']) && (!is_int($record['ms']) || $record['ms'] < 0 || $record['ms'] > 300000)) {
            error_response(400, 'Invalid duration');
        }
        $totalRecords++;
    }
}

if ($totalRecords === 0) {
    error_response(400, 'No records');
}
if ($totalRecords > MAX_RECORDS) {
    error_response(400, 'Too many records');
}

// Rate limit (checked after validation so invalid requests don't consume quota)
check_rate_limit();

// Convert to line protocol and write
$lineProtocol = to_line_protocol($data);
$writeResult = write_to_influxdb($lineProtocol, INFLUX_WRITE_TIMEOUT);
if (!$writeResult['ok']) {
    error_log('SweetSpot stats: InfluxDB write failed: HTTP ' . $writeResult['http_code']
        . ($writeResult['curl_error'] ? ', curl: ' . $writeResult['curl_error'] : '')
        . ($writeResult['response'] ? ', response: ' . $writeResult['response'] : ''));
    error_response(502, 'Storage write failed');
}

// Heartbeat: signal a successful ingestion to the Uptime Kuma push monitor.
ping_kuma($totalRecords);

// Success
http_response_code(200);
header('Content-Type: application/json');
echo json_encode(['ok' => true, 'records' => $totalRecords]);
