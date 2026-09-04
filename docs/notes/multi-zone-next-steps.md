# Multi-Zone Next Steps

## What's Done

All core multi-zone infrastructure is complete:

- ✅ Zone selection UI (30 countries, 43 bidding zones)
- ✅ ENTSO-E token wiring via `BuildConfig`
- ✅ Sub-hourly intervals (15-min resolution support throughout pipeline)
- ✅ `PriceFetcherFactory` with dynamic fallback chain construction
- ✅ Per-zone cache separation (`FilePriceCache` keyed by zone)
- ✅ User-configurable data source order (per zone, persisted in `SettingsRepository`)
- ✅ Data source name flows through entire pipeline to UI ("Data source: ENTSO-E" etc.)
- ✅ Wearable Data Layer sync for zone settings and source order
- ✅ Phase 1 fallback: Spot-Hinta.fi (15 Nordic/Baltic zones, 19 unit tests)
- ✅ EnergyZero fallback for NL
- ✅ Phase 2 fallback: Energy-Charts (30 CC BY 4.0 zones, 22 unit tests)

**Current fallback coverage:** 41/43 zones have at least one fallback (NL via EnergyZero +
Energy-Charts, 15 Nordic/Baltic via Spot-Hinta.fi, 30 European via Energy-Charts — with
overlap on DK1, DK2, NO2, SE4, NL). AT and DE_LU have triple redundancy (ENTSO-E,
Energy-Charts, aWATTar). Only MK and IE_SEM are ENTSO-E only: Energy-Charts answers
HTTP 400 for every `bzn` spelling tried for them, so neither has a usable second source.

## Remaining Work

### 1. Fallback API Chain

ENTSO-E is the only API covering all 43 zones, but it's unreliable (frequent 503s,
rate limits around peak auction time ~13:00 CET). Every zone needs at least one fallback.

### Available Fallback APIs (research: 2026-03-04)

#### Tier 1 — Ready to integrate (no auth, clean JSON, verified working)

**Spot-Hinta.fi** — Best single fallback for Nordic/Baltic region
- URL: `https://api.spot-hinta.fi/TodayAndDayForward?region={ZONE}`
- Coverage: FI, SE1–SE4, DK1–DK2, NO1–NO5, EE, LV, LT (15 zones)
- Resolution: 15 min (96 slots/day)
- Prices: **EUR/kWh** (matches our internal unit — no conversion needed)
- Auth: None
- Format: JSON array, each entry has `DateTime` (ISO 8601 with tz offset), `PriceNoTax`
- Endpoints: `/Today`, `/TodayAndDayForward`, `/DayForward`
- Notes: `PriceNoTax` is the raw day-ahead market price. Also has `PriceWithTax` (Finnish VAT).

**Elering Dashboard** — Baltic + Finland
- URL: `https://dashboard.elering.ee/api/nps/price?start={ISO_UTC}&end={ISO_UTC}`
- Coverage: EE, FI, LV, LT (4 zones, all returned in a single response)
- Resolution: 15 min
- Prices: EUR/MWh (÷ 1000)
- Auth: None
- Format: JSON `{"success": true, "data": {"ee": [...], "fi": [...], ...}}`, each entry has
  `timestamp` (Unix seconds) and `price`

**Hva Koster Strommen** — Norwegian zones
- URL: `https://www.hvakosterstrommen.no/api/v1/prices/{YYYY}/{MM}-{DD}_{ZONE}.json`
- Coverage: NO1–NO5 (5 zones)
- Resolution: 60 min (hourly)
- Prices: **EUR/kWh** (no conversion)
- Auth: None. Static JSON files per day per zone.
- Format: JSON array, each entry has `EUR_per_kWh`, `time_start`, `time_end` (ISO 8601 with tz)
- Attribution: "Strompriser levert av hvakosterstrommen.no"

**aWATTar** — Austria and Germany
- URL: `https://api.awattar.at/v1/marketdata` (AT), `https://api.awattar.de/v1/marketdata` (DE)
- Coverage: AT, DE-LU (2 zones)
- Resolution: 60 min (hourly)
- Prices: EUR/MWh (÷ 1000)
- Auth: None
- Format: JSON `{"data": [{"start_timestamp": ms, "end_timestamp": ms, "marketprice": float}]}`
- Parameters: `start`/`end` in milliseconds epoch

#### Tier 2 — Usable with custom parsing

**OMIE** — Iberian Peninsula
- URL: `https://www.omie.es/sites/default/files/dados/NUEVA_SECCION/INT_PBC_EV_H_ACUM.TXT`
- Coverage: ES, PT (2 zones)
- Resolution: 60 min (hourly file) or 15 min (quarterly file available)
- Prices: EUR/MWh (÷ 1000)
- Auth: None. Public static file download.
- Format: Semicolon-delimited CSV. European decimal format (comma = decimal point).
  Latin-1 encoding. Predictable daily file URLs.
- Notes: Hourly accumulated file is simpler. Per-day files also available at
  `/dados/AGNO_{YYYY}/MES_{MM}/TXT/INT_PBC_EV_H_1_{DD}_{MM}_{YYYY}_{DD}_{MM}_{YYYY}.TXT`

**OTE** — Czech Republic
- URL: `https://www.ote-cr.cz/en/short-term-markets/electricity/day-ahead-market/@@chart-data?report_date={YYYY-MM-DD}`
- Coverage: CZ (1 zone)
- Resolution: 15 min (96 data points as index 1–96)
- Prices: EUR/MWh (÷ 1000)
- Auth: None
- Format: JSON chart data. `dataLine[1]` has 15-min prices. Timestamps computed from
  report_date + index.

#### Tier 3 — Partial coverage, licensing concerns

**Energy-Charts** (Fraunhofer ISE)
- URL: `https://api.energy-charts.info/price?bzn={ZONE}&start={ISO}&end={ISO}`
- Coverage: 41/43 zones (missing IE_SEM, MK). 15-min resolution (CH hourly).
- Auth: None. Rate limit undocumented, ~30 rapid requests triggers 429.
- Prices: EUR/MWh (÷ 1000). JSON format.
- **License split:** Only 15 zones are CC BY 4.0 (free for any use with attribution to
  "Bundesnetzagentur | SMARD.de"). The remaining 26 zones are restricted to
  "private and internal use only" — not usable in a distributed app.
- CC BY 4.0 zones: AT, BE, CH, CZ, DE-LU, DK1, DK2, FR, HU, IT-North, NL, NO2, PL, SE4, SI
- Private-only zones: all others (BG, HR, EE, ES, FI, GR, IT (6 non-North zones), LT, LV,
  ME, NO1/3/4/5, PT, RO, RS, SE1/2/3, SK)

#### Not viable

| API | Reason |
|-----|--------|
| Nord Pool | No free public API, commercial agreement required |
| GME (Italy) | Requires formal registration with identity verification |
| BSP SouthPool (SI) | Paid subscription |
| HUPX (HU) | No public API |
| OPCOM (RO) | No public API |
| SEEPEX (RS) | No public API |
| Energi Data Service (DK) | Dataset appears stale since Sept 2025 |
| Tibber | Account-bound, not zone-selectable |
| Frank Energie | NL only, requires account JWT |

### Zone Coverage Matrix

Current state and target after implementing all viable fallback APIs:

| Zone | Primary | Current Fallback | Planned Additional Fallbacks |
|------|---------|-----------------|------------------------------|
| **AT** | ENTSO-E | Energy-Charts ✅ | aWATTar ✅ |
| **BE** | ENTSO-E | Energy-Charts ✅ | |
| **BG** | ENTSO-E | _(none)_ | _(none known)_ |
| **CH** | ENTSO-E | Energy-Charts ✅ | |
| **CZ** | ENTSO-E | Energy-Charts ✅ | OTE |
| **DE_LU** | ENTSO-E | Energy-Charts ✅ | aWATTar ✅ |
| **DK1** | ENTSO-E | Spot-Hinta.fi ✅ | Energy-Charts ✅ |
| **DK2** | ENTSO-E | Spot-Hinta.fi ✅ | Energy-Charts ✅ |
| **EE** | ENTSO-E | Spot-Hinta.fi ✅ | Elering |
| **ES** | ENTSO-E | _(none)_ | OMIE |
| **FI** | ENTSO-E | Spot-Hinta.fi ✅ | Elering |
| **FR** | ENTSO-E | Energy-Charts ✅ | |
| **GR** | ENTSO-E | _(none)_ | _(none known)_ |
| **HR** | ENTSO-E | _(none)_ | _(none known)_ |
| **HU** | ENTSO-E | Energy-Charts ✅ | |
| **IE_SEM** | ENTSO-E | _(none)_ | _(none known)_ |
| **IT_NORD** | ENTSO-E | Energy-Charts ✅ | |
| **IT_CNOR** | ENTSO-E | _(none)_ | _(none known)_ |
| **IT_CSUD** | ENTSO-E | _(none)_ | _(none known)_ |
| **IT_SUD** | ENTSO-E | _(none)_ | _(none known)_ |
| **IT_CALA** | ENTSO-E | _(none)_ | _(none known)_ |
| **IT_SICI** | ENTSO-E | _(none)_ | _(none known)_ |
| **IT_SARD** | ENTSO-E | _(none)_ | _(none known)_ |
| **LT** | ENTSO-E | Spot-Hinta.fi ✅ | Elering |
| **LV** | ENTSO-E | Spot-Hinta.fi ✅ | Elering |
| **ME** | ENTSO-E | _(none)_ | _(none known)_ |
| **MK** | ENTSO-E | _(none)_ | _(none known)_ |
| **NL** | ENTSO-E | EnergyZero ✅ | Energy-Charts ✅ |
| **NO1** | ENTSO-E | Spot-Hinta.fi ✅ | HvaKosterStrommen |
| **NO2** | ENTSO-E | Spot-Hinta.fi ✅ | Energy-Charts ✅, HvaKosterStrommen |
| **NO3** | ENTSO-E | Spot-Hinta.fi ✅ | HvaKosterStrommen |
| **NO4** | ENTSO-E | Spot-Hinta.fi ✅ | HvaKosterStrommen |
| **NO5** | ENTSO-E | Spot-Hinta.fi ✅ | HvaKosterStrommen |
| **PL** | ENTSO-E | Energy-Charts ✅ | |
| **PT** | ENTSO-E | _(none)_ | OMIE |
| **RO** | ENTSO-E | _(none)_ | _(none known)_ |
| **RS** | ENTSO-E | _(none)_ | _(none known)_ |
| **SE1** | ENTSO-E | Spot-Hinta.fi ✅ | |
| **SE2** | ENTSO-E | Spot-Hinta.fi ✅ | |
| **SE3** | ENTSO-E | Spot-Hinta.fi ✅ | |
| **SE4** | ENTSO-E | Spot-Hinta.fi ✅ | Energy-Charts ✅ |
| **SI** | ENTSO-E | Energy-Charts ✅ | |
| **SK** | ENTSO-E | _(none)_ | _(none known)_ |

¹ Energy-Charts CC BY 4.0 zones only (licensed for app distribution).

**Current:** 26/43 zones have at least one fallback (NL + 15 Nordic/Baltic + 15 via Energy-Charts,
with 4 zones having both Spot-Hinta.fi and Energy-Charts, and AT + DE_LU having triple
redundancy via Energy-Charts + aWATTar).

**After all phases:** 28/43 zones get at least one fallback. 15 zones remain ENTSO-E
only (BG, GR, HR, IE_SEM, IT_CNOR–IT_SARD, ME, MK, RO, RS, SK) — no free public
APIs are known for these zones.

### Implementation Plan

Each phase adds one `PriceFetcher` implementation in `data/api/` and wires it into `PriceFetcherFactory`.
The existing `FallbackPriceFetcher` handles the chain — no new infrastructure needed.

#### Phase 1: Spot-Hinta.fi (15 zones) ✅ Done

Biggest impact. Covers all Nordic/Baltic zones with 15-min resolution.
Already returns EUR/kWh — no unit conversion needed.

- ✅ Created `SpotHintaApi` implementing `PriceFetcher` in `data/api/`
- ✅ Parses JSON array: `DateTime` (ISO 8601) → epoch, `PriceNoTax` → price
- ✅ Uses `/TodayAndDayForward?region={zone}` endpoint
- ✅ Zone mapping: our zone IDs match their region codes exactly (FI, SE1, DK1, NO1, EE, etc.)
- ✅ Wired into `PriceFetcherFactory`: ENTSO-E + SpotHintaApi in `FallbackPriceFetcher`
  for FI, SE1–4, DK1–2, NO1–5, EE, LV, LT
- ✅ Unit tests: parse (7), malformed (7), DST (5) — 19 tests total

#### Phase 2: Energy-Charts (11 additional zones) ✅ Done

Covers central/western Europe. Only CC BY 4.0 zones (15 zones, 11 previously uncovered).

- ✅ Created `EnergyChartsApi` implementing `PriceFetcher`
- ✅ Parses JSON: parallel `unix_seconds` + `price` arrays, EUR/MWh → EUR/kWh (÷ 1000)
- ✅ Handles `null` price entries (gaps filtered out)
- ✅ Auto-detects resolution from timestamp gaps (15-min or 60-min for CH)
- ✅ Zone mapping via `ZONE_TO_BZN` companion map (e.g. `DE_LU` → `DE-LU`, `IT_NORD` → `IT-North`)
- ✅ Wired into `PriceFetcherFactory` and `DataSources.defaultsForZone()`:
  - NL → ENTSO-E, EnergyZero, Energy-Charts (tertiary)
  - DK1, DK2, NO2, SE4 → ENTSO-E, Spot-Hinta.fi, Energy-Charts (tertiary)
  - AT, BE, CH, CZ, DE_LU, FR, HU, IT_NORD, PL, SI → ENTSO-E, Energy-Charts
- ✅ Unit tests: parse (9), malformed (8), DST (5) — 22 tests total
- ✅ Zone ID validation tests in `DataSourceTest` verify all zone sets against `Countries` registry

#### Phase 3: aWATTar (AT, DE-LU — adds depth) ✅ Done

Redundant fallback for two high-traffic zones. Simple no-auth JSON API with hourly resolution.

- ✅ Created `AwattarApi` implementing `PriceFetcher`
- ✅ Parses JSON: `start_timestamp` (ms) → epoch, `marketprice` EUR/MWh → EUR/kWh (÷ 1000)
- ✅ Two base URLs via `ZONE_TO_BASE_URL` companion map: `api.awattar.at` (AT) and `api.awattar.de` (DE-LU)
- ✅ Computes `durationMinutes` from `end_timestamp - start_timestamp` (should be 60)
- ✅ Wired as third-in-chain after Energy-Charts for AT and DE-LU
- ✅ Registered in `DataSources` with `AWATTAR_ZONES` zone set
- ✅ Unit tests: parse (9), malformed (6), DST (5) — 20 tests total

#### Phase 4: OMIE (ES, PT)

Only free option for Iberian zones. Requires CSV parsing.

- Create `OmieApi` implementing `PriceFetcher`
- Parse semicolon-delimited CSV: handle European decimal format (`,` = decimal point),
  Latin-1 encoding, extract ES/PT rows
- Use the accumulated hourly file (simpler than per-day files)
- Wire as fallback for ES and PT
- Add unit tests

#### Phase 5 (optional): Elering, HvaKosterStrommen, OTE

Additional depth for zones already covered by Spot-Hinta.fi / Energy-Charts.
Lower priority — only implement if the primary fallbacks prove unreliable.

- `EleringApi` for EE, FI, LV, LT (redundant with Spot-Hinta.fi)
- `HvaKosterStrommenApi` for NO1–NO5 (redundant with Spot-Hinta.fi, hourly only)
- `OteApi` for CZ (redundant with Energy-Charts)

### Peak-Time Mitigation

The real risk is burst traffic around **13:00–14:00 CET** when next-day prices publish
and caches expire simultaneously across all users.

- ✅ **Fallback APIs** absorb ENTSO-E failures. `FallbackPriceFetcher` automatically
  tries the next source when ENTSO-E returns 409 or 5xx. Currently covers 26/43
  zones (NL via EnergyZero + Energy-Charts, 15 Nordic/Baltic via Spot-Hinta.fi,
  15 European via Energy-Charts, AT + DE_LU with aWATTar as additional depth).
  Expanding fallback coverage (Phases 4–5) covers ES/PT and adds depth to
  already-covered zones.
- **Cache cooldown** (5 min) is per-user, starting from each user's tap — naturally
  staggered, no artificial jitter needed.
- ⬜ **Long term:** If user base exceeds ~50K DAU, stand up a caching proxy that
  fetches once per zone and serves all users.

## 2. Historical Price Fetching

Currently the app only fetches today+tomorrow. Historical data could be useful for:
- Showing price trends and averages
- Letting users compare current prices to historical ranges
- The ENTSO-E API supports arbitrary date ranges (subject to rate limits)
