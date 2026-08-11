# SweetSpot Stats Endpoint — Server Setup

Step-by-step guide for deploying the SweetSpot API-reliability stats endpoint (`stats.php` →
InfluxDB 3 Core, behind Apache + Cloudflare) on a home server. Deploy updates with
`make deploy-stats` (`bin/deploy/deploy-stats.sh`). Architecture overview: `CLAUDE.md` → "Stats Backend &
Monitoring". (The report/feedback Cloudflare Worker is separate — see `../feedback-worker/`.)

## Current deployment (aurora)

The live setup, so it doesn't have to be rediscovered:

- **Endpoint**: `stats.php` deployed to `aurora:/var/www/stats.sweetspot.today/` (with `clear-rate-limit.sh`
  and an `.htaccess` holding `SetEnv INFLUX_TOKEN` + `SetEnv KUMA_PUSH_URL`). Push app changes with
  `make deploy-stats`.
- **InfluxDB** runs as a **Docker container** named `influxdb` (image `influxdb:3-enterprise`) via
  `docker compose` in **`aurora:/opt/monitoring/`** (that stack also runs Grafana, an InfluxDB UI
  `influxdb_ui`, node-red, mosquitto, n8n). The DB is `sweetspot`, measurement `api_fetch`. The write/query
  token is at `aurora:/opt/monitoring/secrets/influxdb_token`. `influxdb3` is **not** on the host PATH — run
  it inside the container (see below).
- **Verify ingestion end-to-end** with a single test-marked POST — a `200 {"ok":true,...}` is returned
  **only after** InfluxDB acknowledges the write with `204` (`stats.php` gates the 200 on the write result),
  so an `ok` response *is* proof the row landed:
  ```bash
  curl -s -X POST https://stats.sweetspot.today/report -H 'Content-Type: application/json' \
    -H 'User-Agent: SweetSpot/0.0.0' \
    -d "{\"v\":1,\"app\":\"0.0.0\",\"records\":[{\"z\":\"ZZ\",\"s\":\"test\",\"d\":\"phone\",\"r\":[{\"t\":$(date +%s),\"ok\":true}]}]}"
  # -> {"ok":true,"records":1}   (source=test / zone=ZZ / app=0.0.0 → auto-excluded from dashboards)
  ```
  The `SweetSpot/` User-Agent is required — the endpoint answers `403 Invalid User-Agent` without it.
- **Or just probe `/health`**, which needs no payload and is what the uptime monitor polls:
  ```bash
  curl -s https://stats.sweetspot.today/health   # -> {"ok":true,"probe":"ingest"}
  ```
- **Query InfluxDB** (read-only) via the container + token file:
  ```bash
  ssh aurora 'docker exec influxdb influxdb3 query --database sweetspot \
    --token "$(cat /opt/monitoring/secrets/influxdb_token)" \
    "SELECT time, zone, source, device, outcome FROM api_fetch WHERE source = '"'"'test'"'"' ORDER BY time DESC LIMIT 5"'
  ```
  `api_fetch` fields: `time, zone, source, device, app, outcome` (ok/fail — **not** `success`), `error`,
  `status`, `duration_ms`, `lang`, `count`. Dashboard/prod queries use `WHERE source != 'test'`.

## Monitoring

Three separate signals, because "no data arrived" has three very different causes and one
heartbeat cannot tell them apart. The endpoint writes to three measurements:

| Measurement | Written when | Watched by |
| --- | --- | --- |
| `api_fetch` | an app report is ingested | the Grafana dashboard |
| `ingest_canary` | `/health` is probed | the ingestion-path monitor |
| `endpoint_reject` | any non-2xx response | the rejection monitor, via `/rejects` |

**1. Is the ingestion path working?** — `GET /health` writes one point and returns 200 only once
InfluxDB acks it, so it covers PHP being broken, the token being wrong, and InfluxDB being down or
refusing writes. Because a monitor drives it rather than app traffic, it stays meaningful when
nobody happens to be reporting. Add it as an Uptime Kuma **keyword** monitor — no push token needed,
and unlike a push monitor it reports latency. Run on aurora (`add` takes a whole monitor object;
`parent: 135` is the `Infrastructure / SweetSpot` group and notification `1` is the shared channel):

```bash
cat > /tmp/ingest-monitor.json <<'JSON'
{
  "name": "SweetSpot stats ingestion path",
  "type": "keyword",
  "url": "https://stats.sweetspot.today/health",
  "method": "GET",
  "keyword": "\"probe\":\"ingest\"",
  "invertKeyword": false,
  "conditions": [],
  "interval": 300,
  "retryInterval": 300,
  "maxretries": 2,
  "timeout": 48,
  "accepted_statuscodes": ["200-299"],
  "parent": 135,
  "resendInterval": 0,
  "notificationIDList": {"1": true}
}
JSON
docker exec kuma-api python /app/kuma_client.py add "$(cat /tmp/ingest-monitor.json)"
```

The keyword is belt-and-braces: `/health` already fails closed with `503`, which
`accepted_statuscodes` alone would catch. It guards the case where a future change returns `200` with
`"ok":false`. Matching `"probe":"ingest"` rather than `"ok"` matters — the failure body contains
`"ok":false`, so `ok` as a keyword would match a broken endpoint.

A 5-minute interval detects breakage in ~10 minutes instead of days. It also writes ~105k canary
rows a year; that is why the canary has its own measurement, which can be dropped wholesale
(`influxdb3 delete table ingest_canary --database sweetspot`) since InfluxDB 3 Core has no row
`DELETE`.

**2. Are reports still flowing?** — the push monitor (id 122, 3-day heartbeat), pinged by
`ping_kuma()` on each successful ingestion. This one *is* traffic-coupled by design and goes quiet
whenever no device reports, which is not a fault. Its name says so, so that when it does fire the
meaning is unambiguous:

```bash
ssh aurora "docker exec kuma-api python /app/kuma_client.py edit 122 \
  '{\"name\":\"SweetSpot stats reports flowing (traffic-driven)\"}'"
```

It keeps its notification, so a genuinely quiet stretch of three days still alerts — the name is what
distinguishes "nobody reported" from "the pipeline is broken", which is now signal 1's job.

**3. Are reports being rejected?** — the failure mode a success-only heartbeat cannot see: the
endpoint answers 4xx, the app discards 4xx as corrupt and never retries, and the data is lost
silently. A stale `status` whitelist did exactly this for three months. Every non-2xx is written to
`endpoint_reject`, and `GET /rejects` counts them so Kuma can watch it like any other route — keeping
all three signals and their notifications in one system:

```bash
cat > /tmp/rejects-monitor.json <<'JSON'
{
  "name": "SweetSpot stats rejections",
  "description": "Rejections seen in the last 6h. Details: https://grafana.aurora.merhar.si/d/sweetspot-api-stats/sweetspot-api-reliability?viewPanel=panel-12&from=now-6h&to=now",
  "type": "keyword",
  "url": "https://stats.sweetspot.today/rejects",
  "method": "GET",
  "keyword": "\"clean\":true",
  "invertKeyword": false,
  "conditions": [],
  "interval": 900,
  "retryInterval": 900,
  "maxretries": 1,
  "timeout": 48,
  "accepted_statuscodes": ["200-299"],
  "parent": 135,
  "resendInterval": 0,
  "notificationIDList": {"1": true}
}
JSON
docker exec kuma-api python /app/kuma_client.py add "$(cat /tmp/rejects-monitor.json)"
```

`conditions: []` is required — the column is `NOT NULL`, and omitting it fails the insert.

Both monitors sit in the `Infrastructure / SweetSpot` group (135). A **group** monitor beats every 60 s
with `All children up and running` and raises its own notifying heartbeat (`Child monitors down: <name>`)
when a child fails, so one failure alerts once per notifying ancestor as well as from the child
itself. Keep `notificationIDList` empty on group 135 unless a third alert per incident is wanted —
the Infrastructure group above it already relays the child's name.

Kuma's own heartbeat history is the fastest way to confirm what actually alerted, and it is readable
without touching the running instance:

```bash
ssh aurora 'docker exec uptime-kuma sqlite3 -readonly /app/data/kuma.db \
  "SELECT monitor_id, status, important, time, msg FROM heartbeat \
   WHERE monitor_id = 134 ORDER BY id DESC LIMIT 5"'
```

`important = 1` marks the beats that sent a notification; `status` is 0 DOWN / 1 UP / 2 PENDING /
3 MAINTENANCE.

Tag both probe monitors `http` (3) — `docker exec kuma-api python /app/kuma_client.py tag <tag_id>
<monitor_id>`. Deliberately **not**: `heartbeat` (9), which marks the push monitor rather than an
active probe; `critical` (7), reserved for infrastructure the household depends on, which opt-in
analytics is not; and `public` (8), which controls whether a monitor appears on the **public status
page** — not whether the monitored URL is internet-reachable.

The alert **clears itself** once the 6-hour window moves past the last rejection, so nothing needs
acknowledging. `429` is excluded because rate-limiting is expected, and requests carrying
`X-SweetSpot-Synthetic: 1` are tagged `synthetic='true'` and ignored — `test.sh` sends it on every
request so its deliberate rejections don't page anyone, the same convention as `source="test"` for
dashboard writes.

### Getting the details into the alert

Kuma truncates a response body to **47 characters** when it reports a keyword mismatch
(`monitor.js`: `data.substring(0, 47) + "..."`), so the alert text reads:

```
[SweetSpot stats rejections] [DOWN] 200 - OK, but keyword is not in [{"clean":false,"rejects":3,"window_hours":6,"de...]
```

`clean` and `rejects` are ordered first in the JSON deliberately, so the **count survives the
truncation**. A URL cannot fit in what remains, which is why the Grafana link lives in the monitor's
`description` (visible on its page in Kuma) and in the probe's own response body.

To get the link into the Telegram message itself, enable a **template** on the notification: Kuma
renders it with Liquid and exposes the whole monitor object, so `{{ monitorJSON.description }}`
emits the full untruncated URL. A template applies to every monitor sharing that notification, so
guard the optional lines (`{% if monitorJSON.description %}`) and the shared channel stays correct
for monitors that have no description.

Never interpolate `{{ monitorJSON }}` wholesale: it is serialised with `includeSensitiveData = true`,
so it carries `basic_auth_pass`, `bearer_token`, `pushToken`, `oauth_client_secret`,
`databaseConnectionString` and friends. Reference individual fields only.

Note the blind spot: `/health` proves *a* valid payload is accepted, not that the *app's* payloads
are — a validation rule the app has outgrown passes the canary and fails every real report. Signal 3
is what covers that, which is why both exist.

## Prerequisites

- Debian/Ubuntu server with root access
- Apache 2.4 with PHP 8.x (php-curl required)
- Domain `sweetspot.today` on Cloudflare

## 1. Cloudflare DNS

Add an A record in the Cloudflare dashboard:

- **Type:** A
- **Name:** `stats`
- **Content:** your server's public IP
- **Proxy:** enabled (orange cloud)

## 2. Cloudflare SSL

Set the SSL/TLS mode for your domain:

- **SSL/TLS → Overview → Full (strict)**

Generate an Origin CA certificate:

- **SSL/TLS → Origin Server → Create Certificate**
- Hostnames: `stats.sweetspot.today`
- Validity: 15 years
- Save the certificate and key to your server:

```bash
sudo nano /etc/ssl/certs/sweetspot.today.pem    # paste certificate
sudo nano /etc/ssl/private/sweetspot.today.key   # paste private key
sudo chmod 600 /etc/ssl/private/sweetspot.today.key
```

## 3. Cloudflare WAF rule

The default Bot Fight Mode / Browser Integrity Check will block the Android
HTTP client. Add a WAF custom rule to skip it:

- **Security → WAF → Custom rules → Create rule**
- Name: `Allow SweetSpot app`
- Field: `Hostname`, Operator: `equals`, Value: `stats.sweetspot.today`
- **Then:** Skip → select Browser Integrity Check

## 4. Apache

```bash
# Enable required modules
sudo a2enmod rewrite ssl remoteip

# Deploy the PHP script
sudo mkdir -p /var/www/stats.sweetspot.today
sudo cp stats.php /var/www/stats.sweetspot.today/
sudo chown -R www-data:www-data /var/www/stats.sweetspot.today

# Create log directory
sudo mkdir -p /var/log/apache2/stats.sweetspot.today

# Install the vhost
sudo cp stats.sweetspot.today.conf /etc/apache2/sites-available/
sudo a2ensite stats.sweetspot.today

# Test config and reload
sudo apache2ctl configtest
sudo systemctl reload apache2
```

This is the one-time bootstrap. Afterwards `make deploy-stats` handles both the webroot files and the
vhost — it diffs the vhost against the live copy, installs it only when it differs, validates with
`configtest`, and rolls back if that fails. `make deploy-stats-check` reports drift without touching
anything.

`stats.php` reads two environment variables: **`INFLUX_TOKEN`** (required — InfluxDB write auth) and
**`KUMA_PUSH_URL`** (optional — the Uptime Kuma push-monitor heartbeat URL; when set, `stats.php` pings
it after every successful InfluxDB write). Provide them with `SetEnv` in the webroot `.htaccess` — it's
read per request, so changes take effect without an Apache reload. **Do not commit this file** (it holds
the token):

```apache
# /var/www/stats.sweetspot.today/.htaccess
SetEnv INFLUX_TOKEN "your-influxdb-token"
SetEnv KUMA_PUSH_URL "https://uptime.example.com/api/push/XXXXXXXX"   # optional
```

## 5. InfluxDB 3 Core

Install InfluxDB 3 Core (not InfluxDB 2.x — different API):

```bash
# Download and install (check https://github.com/influxdata/influxdb for latest)
curl -LO https://github.com/influxdata/influxdb/releases/latest/download/influxdb3-core_linux_amd64.tar.gz
tar xzf influxdb3-core_linux_amd64.tar.gz
sudo mv influxdb3 /usr/local/bin/
```

Start InfluxDB and create the database:

```bash
# Start (first run creates the data directory)
influxdb3 serve --node-id=server01 --data-dir=/var/lib/influxdb3 &

# Create the sweetspot database
influxdb3 create database sweetspot
```

For a systemd service, create `/etc/systemd/system/influxdb3.service`:

```ini
[Unit]
Description=InfluxDB 3 Core
After=network.target

[Service]
ExecStart=/usr/local/bin/influxdb3 serve --node-id=server01 --data-dir=/var/lib/influxdb3
Restart=always
User=influxdb

[Install]
WantedBy=multi-user.target
```

```bash
sudo useradd --system --no-create-home influxdb
sudo mkdir -p /var/lib/influxdb3
sudo chown influxdb:influxdb /var/lib/influxdb3
sudo systemctl daemon-reload
sudo systemctl enable --now influxdb3
```

## 6. Verify the endpoint

Test with curl from the server itself:

```bash
curl -s -X POST https://stats.sweetspot.today/report \
  -H "Content-Type: application/json" \
  -H "User-Agent: SweetSpot/4.0" \
  -d '{
    "v": 1,
    "app": "4.0",
    "records": [{
      "z": "NL",
      "s": "entsoe",
      "d": "phone",
      "r": [{"t": 1711700000, "ok": true}]
    }]
  }'
```

Expected response: `{"ok":true,"records":1}`

Verify the data landed in InfluxDB (bare-binary install):

```bash
influxdb3 query --database=sweetspot "SELECT * FROM api_fetch ORDER BY time DESC LIMIT 5"
```

(On the aurora deployment InfluxDB is containerized — use the container query command in
[Current deployment](#current-deployment-aurora) instead.)

## 7. Grafana (optional)

```bash
# Install Grafana OSS (https://grafana.com/docs/grafana/latest/setup-grafana/installation/debian/)
sudo apt-get install -y adduser libfontconfig1 musl
curl -LO https://dl.grafana.com/oss/release/grafana_11.6.0_amd64.deb
sudo dpkg -i grafana_11.6.0_amd64.deb
sudo systemctl enable --now grafana-server
```

Then at `http://your-server:3000` (default admin/admin):

1. **Connections → Data sources → Add data source → InfluxDB**
   - Query language: **SQL** (InfluxDB 3 Core's query API is SQL; the committed dashboard uses SQL)
   - URL: `http://localhost:8181` (InfluxDB 3 Core — **not** 8086, which is the InfluxDB 2.x default)
   - Database: `sweetspot`
2. **Dashboards → Import → Upload JSON file** → select `grafana-dashboard.json`

## 8. Firewall

If using ufw:

```bash
# Allow HTTPS from Cloudflare IPs only
# Full list: https://www.cloudflare.com/ips/
sudo ufw allow from 173.245.48.0/20 to any port 443
sudo ufw allow from 103.21.244.0/22 to any port 443
sudo ufw allow from 103.22.200.0/22 to any port 443
sudo ufw allow from 103.31.4.0/22 to any port 443
sudo ufw allow from 141.101.64.0/18 to any port 443
sudo ufw allow from 108.162.192.0/18 to any port 443
sudo ufw allow from 190.93.240.0/20 to any port 443
sudo ufw allow from 188.114.96.0/20 to any port 443
sudo ufw allow from 197.234.240.0/22 to any port 443
sudo ufw allow from 198.41.128.0/17 to any port 443
sudo ufw allow from 162.158.0.0/15 to any port 443
sudo ufw allow from 104.16.0.0/13 to any port 443
sudo ufw allow from 104.24.0.0/14 to any port 443
sudo ufw allow from 172.64.0.0/13 to any port 443
sudo ufw allow from 131.0.72.0/22 to any port 443
```

This ensures port 443 is only reachable from Cloudflare, not directly from the internet.
