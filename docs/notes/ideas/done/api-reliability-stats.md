# API Reliability Statistics

Implemented in v4.1 (March 2026).

## Overview

Opt-in anonymous API reliability stats collection. Instruments the fetcher chain to record every API request outcome, stores stats locally in a binary file, syncs watch stats to phone via Data Layer, and reports to a self-hosted PHP+InfluxDB endpoint with Grafana dashboards.

## Architecture

```
Watch App                    Phone App                       Home Server
┌────────────────┐           ┌─────────────────────┐         ┌──────────────────────┐
│ Instrumented    │  Data     │ InstrumentedFetcher  │         │ stats.sweetspot.today│
│ PriceFetcher    │  Layer    │   ↓ records stats    │  HTTPS  │ (Cloudflare proxy)   │
│   ↓             │ ──────→  │ StatsCollector       │ ──────→ │                      │
│ StatsCollector  │ /stats    │   ↓ stores locally   │  POST   │ stats.php            │
│   ↓ binary file │  path     │ Binary file          │         │   ↓                  │
└────────────────┘           │                      │         │ InfluxDB 3 Core      │
                             │ StatsReporter        │         │   ↓                  │
                             │   ↓ sends if opt-in  │         │ Grafana dashboards   │
                             └─────────────────────┘         └──────────────────────┘
```

## What was built

### Shared module (`:shared`)

- **`HttpException`** — Typed exception for non-200 HTTP responses (replaces ad-hoc `RuntimeException`). All 5 API classes (`EntsoeApi`, `EnergyZeroApi`, `SpotHintaApi`, `EnergyChartsApi`, `AwattarApi`) throw `HttpException(code, "HTTP $code")`.
- **`EntsoeException`** — Typed exception for ENTSO-E Acknowledgement_MarketDocument errors (HTTP 200 but error response body). Carries the reason text. Replaces bare `RuntimeException` thrown for ENTSO-E API-level errors.
- **`ErrorCategory.categorise()`** — Maps exceptions to stable category strings: `HTTP_503`, `ENTSOE_ERROR`, `TIMEOUT`, `DNS`, `CONNECTION`, `IO`, `PARSE`, `UNKNOWN`, `EMPTY`.
- **`StatsRecord`** — Data class: timestamp, zone, source, device (phone/watch), success, errorCategory.
- **`StatsCollector`** — Interface: `record()`, `readAll()`, `clear()`.
- **`FileStatsCollector`** — Append-only binary file (`cacheDir/api_stats.bin`). Thread-safe via synchronized block. Resilient to partial corruption (preserves records before the corrupted point).
- **`InstrumentedPriceFetcher`** — Decorator that wraps a `PriceFetcher` and records every outcome. Captures successes, empty results, and failures with categorised errors.

### Phone app (`:app`)

- **`StatsReporter`** — Reads local binary stats, encodes to grouped JSON, POSTs to `stats.sweetspot.today/report` via `HttpURLConnection`. Rate-limited to once per 24 hours. Clears local file on successful submission.
- **`SweetSpotViewModel`** — Implements `DataClient.OnDataChangedListener` to receive watch stats via `/stats` path. Creates `InstrumentedPriceFetcher` wrapping conditionally (only when stats enabled). Reports via `StatsReporter` after successful fetches. Syncs `stats_enabled` to watch via `/settings` Data Layer path.
- **Settings UI** — Toggle in Settings > Advanced ("Share API statistics"). Opt-in prompt dialog after 3+ days of use (only if stats not already enabled).

### Wear app (`:wear`)

- **`WearViewModel`** — Reads `stats_enabled` from phone via Data Layer. Conditionally instruments fetchers. After each fetch, encodes stats to binary and pushes to `/stats` Data Layer path. Awaits `putDataItem` before clearing local stats to prevent data loss.

### Server (`server/`)

- **`stats.php`** — PHP 7.4+ ingestion endpoint. Validates JSON payload, rate-limits per IP (1 req/5 min), sanitises all input with regex whitelists, writes to InfluxDB 3 Core via line protocol (`/api/v3/write_lp`). Behind Apache with Cloudflare proxy.
- **`stats.sweetspot.today.conf`** — Apache vhost with `RewriteRule /report → stats.php`.
- **`grafana-dashboard.json`** — 7-panel dashboard: success rate by source, failures by error, requests by zone, failure heatmap by hour, reliability table, phone vs watch, app versions. Uses FlightSQL data source with SQL queries.
- **`test.sh`** — 14 endpoint tests (1 valid payload, 12 rejected payloads, 1 rate limit).
- **`clear-rate-limit.sh`** — Clears rate limiter files (handles Apache's PrivateTmp).
- **`SETUP.md`** — Step-by-step server setup guide.

### Strings and privacy

- Stats strings added to all 26 app languages.
- Privacy policies updated in all 5 website languages (en, nl, de, fr, sl) with "Optional API Statistics" section.
- Meta descriptions and overview paragraphs updated to accurately reflect the optional stats feature.

## JSON report format

```json
{
  "v": 1,
  "app": "4.1",
  "records": [
    {
      "z": "NL",
      "s": "entsoe",
      "d": "phone",
      "r": [
        {"t": 1711700000, "ok": true},
        {"t": 1711703600, "ok": false, "e": "TIMEOUT"}
      ]
    }
  ]
}
```

Records grouped by zone + source + device. Individual timestamps preserved for time-of-day analysis.

## Tests

- `ErrorCategoryTest` — 13 tests covering all exception categories
- `InstrumentedPriceFetcherTest` — 6 tests: success/failure/empty, delegation, clock, accumulation
- `FileStatsCollectorTest` — 8 tests: record, read, clear, append, persistence, corruption
- `StatsReporterTest` — 5 tests: JSON format, grouping, version, error field presence
- `SweetSpotViewModelTest` — 12 stats tests: settings toggle, prompt lifecycle (shown after 3 days, not shown when dismissed/already enabled), watch stats receive/ignore
