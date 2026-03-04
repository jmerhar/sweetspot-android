# Multi-Zone Next Steps

Now that `EntsoeApi` implements `PriceFetcher` for all ENTSO-E bidding zones, here's what's needed to ship multi-zone support in the app.

## 1. Zone Selection UI ✅

Done. Settings screen has a country picker (30 countries) and a zone picker for multi-zone
countries (DK, IT, NO, SE). `CountryDetector` auto-detects the user's country on first launch
from SIM → network → timezone → locale → NL fallback (zero permissions required).
43 bidding zones across all ENTSO-E member states with published day-ahead prices. Italy has
7 zones (main geographic regions). GB is excluded — ENTSO-E doesn't publish GB day-ahead
prices post-Brexit. Zones without data (AL, BA, CY, MT, TR, XK, 4 Italian limited production
zones) are documented in `entsoe-api.md` for future re-evaluation.

## 2. ENTSO-E Token Wiring ✅

Done. `ENTSOE_API_TOKEN` is injected from `local.properties` via `BuildConfig` (configured
in the `sweetspot-app` convention plugin in `buildSrc/`). Both phone and wear modules
receive the token.

## 3. Sub-Hourly Interval Support ✅

Done. `EntsoeApi` returns prices at native resolution (PT15M or PT60M) — no more
hourly aggregation. `HourlyPrice` renamed to `PriceSlot` with a `durationMinutes` field.
`CheapestWindowFinder` works in "slot units" and multiplies by `slotMinutes / 60.0`
for EUR costs. `CachedPrice` and `FilePriceCache` carry `durationMinutes` (format v2).
`PriceRepository` uses slot-aware coverage checks and filtering. The bar chart groups
sub-hourly slots by hour with labels showing hourly averages and individual bars stacked
within each row.

## 4. PriceFetcherFactory / Zone-Based Fetcher Selection ✅

Done. `PriceFetcherFactory` is a `fun interface` that returns the right `PriceFetcher`
for a given `PriceZone`. `defaultPriceFetcherFactory(entsoeToken)` routes all zones →
`EntsoeApi`, with NL wrapped in `FallbackPriceFetcher` adding EnergyZero as fallback.
Both ViewModels take an injectable factory for testing.

## 5. Fallback API Chain (NL done)

NL now uses ENTSO-E as primary (15-min resolution) with EnergyZero as fallback via
`FallbackPriceFetcher`. The data source name flows through the entire pipeline:
`FetchResult` → `CachedPriceData` (cache v3) → `PriceResult` → `UiState.priceSource`
→ displayed in the results screen disclaimer ("Data source: ENTSO-E").

Other zones use ENTSO-E only (no fallback yet — easy to wrap in `FallbackPriceFetcher`
later with additional API implementations).

Several alternative APIs exist per zone for future fallback chains:

| Zone | Primary | Fallback(s) |
|------|---------|-------------|
| NL | ENTSO-E | EnergyZero ✅, Energy-Charts |
| DE-LU | ENTSO-E | Energy-Charts, SMARD, aWATTar |
| AT | ENTSO-E | aWATTar, Energy-Charts |
| CH | ENTSO-E | Swiss Energy Dashboard |
| PL | ENTSO-E | PSE |
| IE, NIR | ENTSO-E | SEMOpx |
| Nordic/Baltic | ENTSO-E | Energy-Charts |

Notable alternative APIs:
- **Energy-Charts** (energy-charts.info) — No auth, JSON, covers many European zones. Good universal fallback.
- **aWATTar** — AT/DE only, no auth, JSON, hourly
- **SMARD** (smard.de) — DE only, no auth, JSON
- **Elexon BMRS** — GB, 30-minute intervals, requires API key
- **Swiss Energy Dashboard** — CH only
- **PSE** — PL only
- **SEMOpx** — IE/NIR
- **Sourceful** — ENTSO-E data served as JSON (unofficial)
- **Elering/Estfeed** — EE/LV/LT, may require contract (not a simple public API)

### Peak-Time Fallback

The ENTSO-E rate limit is 400 req/min per token (~115K DAUs at 5 req/day). The real
risk is burst traffic around **13:00–14:00 CET** when next-day prices publish and caches
expire simultaneously across all users. At that point a single token could be overwhelmed.

Mitigation plan:
- **Short term:** Implement fallback to Energy-Charts (no auth, JSON) when ENTSO-E returns HTTP 409 (rate limited) or 5xx. The cache refactoring already decoupled the cache from a specific API format, so fallback is transparent.
- **Medium term:** Add jitter to the cache cooldown (e.g. 5min ± random 0–2min) to spread burst requests.
- **Long term:** If user base grows beyond ~50K DAU, stand up a caching proxy that fetches once per zone and serves all users.

## 6. Historical Price Fetching

Currently the app only fetches today+tomorrow. Historical data could be useful for:
- Showing price trends and averages
- Letting users compare current prices to historical ranges
- The ENTSO-E API supports arbitrary date ranges (subject to rate limits)

## 7. Per-Zone Cache Separation ✅

Done. `PriceCache` now stores parsed `CachedPrice` entries per zone key in binary files
(`prices_<key>.bin`). `PriceRepository` takes a `cacheKey` parameter. Cooldown is global
(shared across zones to respect upstream rate limits).
