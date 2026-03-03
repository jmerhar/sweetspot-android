# Multi-Zone Next Steps

Now that `EntsoeApi` implements `PriceFetcher` for all ENTSO-E bidding zones, here's what's needed to ship multi-zone support in the app.

## 1. Zone Selection UI

The app needs a bidding zone picker in Settings. There are 20 EPEX-coupled zones in `BiddingZone`, but more zones could be added later:

- EE (Estonia), LV (Latvia), LT (Lithuania) — Baltic states
- HU (Hungary)
- ME (Montenegro)
- IE (Ireland), NIR (Northern Ireland) — SEM market

A dropdown or searchable list grouped by country, showing "NL — Netherlands", "DE-LU — Germany/Luxembourg", etc.

## 2. ENTSO-E Token Wiring

The API token needs to get from `local.properties` into `EntsoeApi`:

- Inject `ENTSOE_API_TOKEN` from `local.properties` via `BuildConfig` (like the release keystore)
- A single baked-in token supports ~115K DAUs (see `docs/entsoe/entsoe-api.md` rate limit analysis)

## 3. Sub-Hourly Interval Support

Since October 2025, all ENTSO-E zones return PT15M (15-minute) resolution. `EntsoeApi` currently aggregates these to hourly averages to keep `CheapestWindowFinder` and the UI unchanged. For better accuracy:

- Update `CheapestWindowFinder` to work with variable-length slots (15min, 30min, 60min)
- Update `HourlyPrice` model (rename to `PriceSlot`?) to carry slot duration
- Update the bar chart and time display to handle sub-hourly granularity
- This is a significant change — do it as a separate feature

## 4. PriceFetcherFactory / Zone-Based Fetcher Selection

Currently `PriceRepository` defaults to `EnergyZeroApi`. To support multiple zones:

- Create a `PriceFetcherFactory` that returns the right `PriceFetcher` for a given zone
- NL could keep using EnergyZero (no auth needed) or switch to ENTSO-E
- All other zones use `EntsoeApi` with the appropriate bidding zone code
- The factory could also handle fallback chains (see below)

## 5. Fallback API Chain

Several alternative APIs exist per zone. If the primary source fails, fall back to another:

| Zone | Primary | Fallback(s) |
|------|---------|-------------|
| NL | EnergyZero | ENTSO-E, Energy-Charts |
| DE-LU | ENTSO-E | Energy-Charts, SMARD, aWATTar |
| AT | ENTSO-E | aWATTar, Energy-Charts |
| GB | ENTSO-E | Elexon BMRS (30-min intervals) |
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
