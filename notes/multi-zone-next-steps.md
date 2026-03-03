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

The API token needs to get from `local.properties` (or user settings) into `EntsoeApi`:

- **Build-time:** Inject `ENTSOE_API_TOKEN` from `local.properties` via `BuildConfig` (like the release keystore)
- **Run-time:** Let users enter their own token in Settings (ENTSO-E tokens are free but require registration)
- Consider a default token baked in for convenience, with an option to override

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

## 6. Historical Price Fetching

Currently the app only fetches today+tomorrow. Historical data could be useful for:
- Showing price trends and averages
- Letting users compare current prices to historical ranges
- The ENTSO-E API supports arbitrary date ranges (subject to rate limits)

## 7. Per-Zone Cache Separation

When supporting multiple zones, `FilePriceCache` should cache per zone:
- Use zone-specific cache files (e.g., `prices_cache_NL.json`)
- Separate cooldown tracking per zone
- Clean up stale cache files when the user switches zones
