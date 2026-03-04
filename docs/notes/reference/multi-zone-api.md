# Multi-Zone EPEX Spot Price API Research

Research date: 2026-03-03

## Background

Research conducted before implementing multi-zone support. SweetSpot originally used only the EnergyZero API for NL hourly prices. The goal was to find APIs covering all EPEX countries. ENTSO-E was chosen as primary; this research is retained as a reference for fallback API options.

## Do prices differ between EPEX zones?

**Yes — significantly.** Market Coupling optimizes cross-border flows via the EUPHEMIA algorithm but does **not** equalize prices. Prices converge only when interconnectors are uncongested, which is infrequent.

Empirical data (February 2026, from EnergyZero + energy-charts.info):

| Zone pair | Same price (< 0.01 ct) | Avg difference | Max difference |
|-----------|------------------------|----------------|----------------|
| NL vs DE-LU | 5.4% of hours | 0.73 ct/kWh | 6.23 ct/kWh |
| NL vs BE | 2.0% of hours | 0.88 ct/kWh | 5.51 ct/kWh |
| NL vs FR | 0.0% of hours | 5.77 ct/kWh | 8.95 ct/kWh |

Using the wrong zone's data gives the wrong cheapest start time 26–63% of days depending on window duration. Per-zone data is essential.

## API Comparison

### 1. ENTSO-E Transparency Platform — Recommended

**URL:** `https://web-api.tp.entsoe.eu/api`

The official EU-mandated data platform. All European TSOs must publish here.

- **Coverage:** All EPEX zones + 40+ other EU/EEA zones — the only single API that covers everything
- **Auth:** Free registration required, get a security token via account settings
- **Format:** XML only (IEC 62325 CIM schema)
- **Rate limit:** 400 requests/minute
- **Resolution:** Hourly; 15-minute for DE-LU and AT
- **Prices:** EUR/MWh (÷ 1000 for EUR/kWh); GB in GBP/MWh
- **Day-ahead:** Available after auction clears (~13:00 CET)

**Day-ahead endpoint:**
```
GET https://web-api.tp.entsoe.eu/api
  ?securityToken=TOKEN
  &documentType=A44
  &in_Domain=10YNL----------L
  &out_Domain=10YNL----------L
  &periodStart=202603030000
  &periodEnd=202603040000
```

**Bidding zone EIC codes:**

| Zone | EIC Code |
|------|----------|
| NL | `10YNL----------L` |
| BE | `10YBE----------2` |
| FR | `10YFR-RTE------C` |
| DE-LU | `10Y1001A1001A82H` |
| AT | `10YAT-APG------L` |
| CH | `10YCH-SWISSGRIDZ` |
| PL | `10YPL-AREA-----S` |
| GB | `10YGB----------A` |
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

**Pros:** Single API for all zones, official/authoritative, free, generous rate limits, 15-min for some zones.
**Cons:** XML only (need XML parser), registration required, verbose schema. API key distribution: ship with app (pragmatic), let users register (poor UX), or proxy via backend (adds infra).

### 2. Energy-Charts (Fraunhofer ISE) — Best no-auth fallback

**URL:** `https://api.energy-charts.info/`

- **Coverage:** Many EPEX zones via `bzn` parameter (DE-LU, AT, FR, BE, NL, CH, PL, Nordic zones) — needs empirical verification for all 20 zones
- **Auth:** None
- **Format:** JSON
- **Rate limit:** Undocumented (research platform, not an API service)
- **Resolution:** Hourly; 15-minute for DE
- **Prices:** EUR/MWh
- **License:** CC BY 4.0 (must attribute Fraunhofer ISE)

**Endpoint:**
```
GET https://api.energy-charts.info/price?bzn=DE-LU&start=2026-03-03T00:00Z&end=2026-03-04T00:00Z
```

**Response:**
```json
{
  "unix_seconds": [1709420400, 1709424000, ...],
  "price": [87.5, 92.3, ...],
  "unit": "EUR/MWh"
}
```

**Pros:** No auth, clean JSON, potentially broad zone coverage, 15-min for DE, permissive license.
**Cons:** No SLA, non-DE zone reliability uncertain, schema could change without notice.

### 3. aWATTar — AT/DE only, simplest integration

**URL:** `https://api.awattar.at/v1/marketdata` (AT), `https://api.awattar.de/v1/marketdata` (DE)

- **Coverage:** Austria and Germany only
- **Auth:** None
- **Format:** JSON
- **Rate limit:** Undocumented (permissive in practice)
- **Resolution:** Hourly only
- **Prices:** EUR/MWh

**Endpoint:**
```
GET https://api.awattar.at/v1/marketdata?start=1709420400000&end=1709506800000
```

**Response:**
```json
{
  "data": [
    {
      "start_timestamp": 1709420400000,
      "end_timestamp": 1709424000000,
      "marketprice": 87.5,
      "unit": "Eur/MWh"
    }
  ]
}
```

**Pros:** Dead simple, no auth, clean JSON, reliable for AT/DE.
**Cons:** Two countries only, no SLA.

### 4. EnergyZero — NL only (current provider)

Already implemented. Keep as-is.

### Not viable

| API | Why not |
|-----|---------|
| **Nord Pool** | No free public API. Commercial agreement required. ToS prohibits scraping. |
| **Tibber** | Account-bound — can only see prices for your own Tibber home. Not zone-selectable. |
| **Frank Energie** | NL only, requires account JWT. |
| **Elering** | Baltic/Nordic only (EE, FI, LV, LT). Overlaps ENTSO-E. |
| **SMARD** | Germany only. |

## Recommendation

**Primary: ENTSO-E Transparency Platform.** It's the only API covering all 20 bidding zones from a single endpoint. The XML parsing trade-off is worth it for complete coverage. Android has built-in XML parsers (`javax.xml.parsers`, `XmlPullParser`). An `EntsoePriceFetcher` implementation would be ~150–200 lines.

**Fallback chain per zone:**
1. NL → ENTSO-E → EnergyZero (already built)
2. AT, DE → aWATTar → Energy-Charts → ENTSO-E
3. All other zones → Energy-Charts (if verified) → ENTSO-E

**Suggested architecture:**
- Create `EntsoePriceFetcher` implementing `PriceFetcher`
- Optionally create `EnergyChartsPriceFetcher` and `AwattarPriceFetcher`
- Create a `PriceFetcherFactory` that picks the best fetcher for the user's zone
- The `PriceFetcher` interface's `fetchRawJson` may need renaming to `fetchPrices` since ENTSO-E returns XML

## Next steps to verify

1. Register for an ENTSO-E API key and test responses for a few zones
2. Test Energy-Charts `bzn` parameter for all 20 EPEX zones
3. Confirm aWATTar endpoints are still live
4. Decide on API key distribution strategy for ENTSO-E
