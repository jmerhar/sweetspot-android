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
 * Expected payload (POST, Content-Type: application/json):
 * {
 *   "v": 1,
 *   "app": "4.0",
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
define('INFLUXDB_URL', 'http://localhost:8181/api/v3/write_lp?db=sweetspot&precision=second');
define('INFLUXDB_TOKEN', getenv('INFLUX_TOKEN') ?: ($_SERVER['INFLUX_TOKEN'] ?? ''));
define('RATE_LIMIT_DIR', '/tmp/sweetspot_rate');
define('RATE_LIMIT_SECONDS', 300); // 5 minutes per IP
define('MAX_BODY_SIZE', 65536); // 64 KB
define('MAX_RECORDS', 500);

// --- Helpers ---

/**
 * Sends a JSON error response and exits.
 */
function error_response(int $code, string $message): void {
    http_response_code($code);
    header('Content-Type: application/json');
    echo json_encode(['error' => $message]);
    exit;
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

    // Use CF-Connecting-IP if behind Cloudflare, otherwise REMOTE_ADDR
    $ip = $_SERVER['HTTP_CF_CONNECTING_IP'] ?? $_SERVER['REMOTE_ADDR'] ?? 'unknown';
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
 * Tags: zone, source, device, app, outcome, error
 * Field: count=1i
 * Timestamp: epoch seconds (precision=second is set in the URL)
 */
function to_line_protocol(array $data): string {
    $lines = [];
    $app = $data['app'];

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
            $escapedZone = str_replace([',', ' ', '='], ['\\,', '\\ ', '\\='], $zone);
            $escapedSource = str_replace([',', ' ', '='], ['\\,', '\\ ', '\\='], $source);
            $escapedDevice = str_replace([',', ' ', '='], ['\\,', '\\ ', '\\='], $device);
            $escapedApp = str_replace([',', ' ', '='], ['\\,', '\\ ', '\\='], $app);
            $escapedError = str_replace([',', ' ', '='], ['\\,', '\\ ', '\\='], $error);

            $lines[] = "api_fetch,zone={$escapedZone},source={$escapedSource},device={$escapedDevice},app={$escapedApp},outcome={$outcome},error={$escapedError} count=1i {$timestamp}";
        }
    }

    return implode("\n", $lines);
}

/**
 * Writes line protocol data to InfluxDB.
 *
 * @return array{ok: bool, http_code: int, curl_error: string, response: string}
 */
function write_to_influxdb(string $lineProtocol): array {
    $ch = curl_init(INFLUXDB_URL);
    curl_setopt_array($ch, [
        CURLOPT_POST => true,
        CURLOPT_POSTFIELDS => $lineProtocol,
        CURLOPT_HTTPHEADER => [
            'Content-Type: text/plain',
            'Authorization: Bearer ' . INFLUXDB_TOKEN,
        ],
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_TIMEOUT => 10,
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

// --- Main ---

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
if (strlen($body) > MAX_BODY_SIZE) {
    error_response(413, 'Payload too large');
}

$data = json_decode($body, true);
if ($data === null) {
    error_response(400, 'Invalid JSON');
}

// Validate structure
if (!isset($data['v']) || $data['v'] !== 1) {
    error_response(400, 'Unsupported version');
}

if (!isset($data['app']) || !validate_string($data['app'], '/^[\d.]+$/', 16)) {
    error_response(400, 'Invalid app version');
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
$writeResult = write_to_influxdb($lineProtocol);
if (!$writeResult['ok']) {
    error_log('SweetSpot stats: InfluxDB write failed: HTTP ' . $writeResult['http_code']
        . ($writeResult['curl_error'] ? ', curl: ' . $writeResult['curl_error'] : '')
        . ($writeResult['response'] ? ', response: ' . $writeResult['response'] : ''));
    error_response(502, 'Storage write failed');
}

// Success
http_response_code(200);
header('Content-Type: application/json');
echo json_encode(['ok' => true, 'records' => $totalRecords]);
