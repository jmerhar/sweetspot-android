# ENTSO-E Transparency Platform REST API

Official docs: https://transparencyplatform.zendesk.com/hc/en-us/articles/15692855254548-Sitemap-for-Restful-API-Integration

## Endpoint

```
GET https://web-api.tp.entsoe.eu/api
```

Single endpoint, all queries via query parameters.

## Authentication

Query parameter: `securityToken=<token>`. Token stored in `local.properties` as `ENTSOE_API_TOKEN`.

## Day-Ahead Prices (documentType A44)

```
GET https://web-api.tp.entsoe.eu/api
  ?securityToken=TOKEN
  &documentType=A44
  &in_Domain=10YNL----------L
  &out_Domain=10YNL----------L
  &periodStart=202603030000
  &periodEnd=202603050000
```

| Parameter | Required | Description |
|-----------|----------|-------------|
| `securityToken` | Yes | API token |
| `documentType` | Yes | `A44` for day-ahead prices |
| `in_Domain` | Yes | Bidding zone EIC code |
| `out_Domain` | Yes | Same as `in_Domain` for day-ahead |
| `periodStart` | Yes | `YYYYMMddHHmm` in UTC |
| `periodEnd` | Yes | `YYYYMMddHHmm` in UTC |

## Bidding Zone EIC Codes

| Zone | EIC Code |
|------|----------|
| NL | `10YNL----------L` |
| BE | `10YBE----------2` |
| FR | `10YFR-RTE------C` |
| DE-LU | `10Y1001A1001A82H` |
| AT | `10YAT-APG------L` |
| CH | `10YCH-SWISSGRIDZ` |
| PL | `10YPL-AREA-----S` |
| DK1 | `10YDK-1--------W` |
| DK2 | `10YDK-2--------M` |
| NO1 | `10YNO-1--------2` |
| NO2 | `10YNO-2--------T` |
| NO3 | `10YNO-3--------J` |
| NO4 | `10YNO-4--------9` |
| NO5 | `10Y1001A1001A48H` |
| SE1 | `10Y1001A1001A44P` |
| SE2 | `10Y1001A1001A45N` |
| SE3 | `10Y1001A1001A46L` |
| SE4 | `10Y1001A1001A47J` |
| FI | `10YFI-1--------U` |

## Response Format

### Success: `Publication_MarketDocument`

```
xmlns="urn:iec62325.351:tc57wg16:451-3:publicationdocument:7:3"
```

Structure: `Publication_MarketDocument` > `TimeSeries` (1+) > `Period` > `Point` (1+)

Key fields per TimeSeries:
- `currency_Unit.name` — `EUR` (or `GBP` for GB)
- `price_Measure_Unit.name` — `MWH` (divide by 1000 for kWh)
- `curveType` — `A01` (sequential fixed) or `A03` (variable sized block)

Key fields per Period:
- `timeInterval/start` — ISO-8601 UTC start
- `timeInterval/end` — ISO-8601 UTC end
- `resolution` — `PT60M` (hourly) or `PT15M` (15-minute)

Key fields per Point:
- `position` — 1-based index within the period
- `price.amount` — price in EUR/MWh

### Timestamp Calculation

```
timestamp = Period.start + (position - 1) * resolution
```

### Curve Type A03 (Variable Sized Block)

When `curveType=A03`, **positions may be skipped**. A missing position means the price carries forward from the previous point. Observed in real NL data: position 7 missing means position 7's price equals position 6's price.

To handle this: iterate positions 1..N (where N = period duration / resolution), and for each position, use the last seen `price.amount`.

### Multiple TimeSeries

A response may contain multiple `TimeSeries` when:
- Query spans multiple days (one TimeSeries per day)
- DST transitions (may split at the transition boundary)

### DST Transitions

- Spring forward: 23h = 23 positions (PT60M) or 92 (PT15M)
- Fall back: 25h = 25 positions (PT60M) or 100 (PT15M)

Always compute from `start` + `position`, never assume 24 hours.

### Error: `Acknowledgement_MarketDocument`

```
xmlns="urn:iec62325.351:tc57wg16:451-1:acknowledgementdocument:7:0"
```

Contains `Reason/code` (always `999`) and `Reason/text` with details.

Common cases:
- "No matching data found" — no data for that period/zone (normal before ~13:00 CET for tomorrow)
- "Unauthorized" — bad token

## HTTP Status Codes

| Code | Meaning |
|------|---------|
| 200 | Success (XML body) |
| 400 | Bad request |
| 401 | Unauthorized |
| 404 | No data (also returns `Acknowledgement_MarketDocument` XML) |
| 409 | Rate limit exceeded |
| 500 | Server error |

## Rate Limits

400 requests per minute per token. HTTP 409 when exceeded.

### Capacity Estimate for SweetSpot

Each "find cheapest window" tap costs at most 1 API request (cache handles the rest).
With a 5-minute cooldown and today+tomorrow coverage, realistic usage is ~2–5 requests/day
per active user.

| Metric | Value |
|--------|-------|
| Rate limit | 400 req/min = 576,000 req/day |
| At 5 req/day per user | ~115,000 DAUs |
| At 10 req/day per user (worst case) | ~57,000 DAUs |

The bottleneck is burst traffic around **13:00–14:00 CET** when next-day prices publish
and caches expire simultaneously. At 400 req/min over a 30-minute burst window, that's
12,000 requests — enough for thousands of concurrent users, but tight for tens of thousands.

A single baked-in token is fine for a small/medium app. At scale, a caching proxy server
would batch zone requests and serve all users from shared cache. We will also implement a
fallback API chain (e.g. Energy-Charts) to handle peak-time overload or ENTSO-E downtime.

## Sample Files

- `entsoe-sample-response.xml` — real NL response (2026-03-03, PT15M, 95 points with A03 gaps)
- `entsoe-sample-error.xml` — "no matching data" error response
