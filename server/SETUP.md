# Server Setup Guide

Step-by-step guide for deploying the SweetSpot stats endpoint on a home server.

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

The `INFLUX_TOKEN` environment variable must be set for the Apache/PHP process.
Add it to the Apache envvars or the vhost config:

```bash
echo 'export INFLUX_TOKEN="your-token-here"' | sudo tee -a /etc/apache2/envvars
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

Verify the data landed in InfluxDB:

```bash
influxdb3 query --database=sweetspot "SELECT * FROM api_fetch ORDER BY time DESC LIMIT 5"
```

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
   - Query language: **InfluxQL** (or SQL if supported by your Grafana InfluxDB plugin version)
   - URL: `http://localhost:8086`
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
