# All-In Electricity Pricing Research

## Context

SweetSpot currently shows day-ahead spot prices. Users pay more than the spot price — their total includes energy tax, VAT, supplier surcharge, and grid fees. This note explores what it would take to show approximate all-in consumer prices.

## Price Components

| Component | Varies by hour? | Notes |
|---|---|---|
| **Spot price** | Yes | Already have this from ENTSO-E / EnergyZero |
| **Energy tax / excise** | No (fixed per kWh, set annually) | Per country, sometimes per region |
| **VAT** | Proportional to total | Per country |
| **Supplier surcharge** | No (fixed per kWh per contract) | Per supplier |
| **Grid / transmission fees** | Usually no | Per grid operator. **Denmark and Sweden are exceptions** — time-of-use grid tariffs |
| **Renewable / CHP levies** | No (fixed per kWh) | Per country, varies by year |

**Key insight: adding fixed per-kWh surcharges does NOT change which time window is cheapest.** The sliding window algorithm finds the minimum-cost window based on relative price differences. Adding a constant to every hour shifts all costs equally. All-in pricing is therefore a **display-only feature** — it affects the total cost shown ("Running your dishwasher costs €0.87") but not the recommendation of when to run it.

The exception is Denmark and Sweden, where grid tariffs vary by time of day (see Grid Fees below).

## VAT Rates on Electricity

| Country | VAT | Notes |
|---|---|---|
| NL | 21% | Standard rate |
| BE | 6% | Reduced from 21% since Apr 2022 (extended repeatedly) |
| FR | 20% | Unified to 20% in Aug 2025 (was 5.5% on subscription portion) |
| DE | 19% | Applied to both consumption and basic price |
| AT | 20% | |
| CH | 8.1% | Increased from 7.7% Jan 2024 |
| PL | 23% | Was temporarily 5% (2024) |
| ES | 21% | Returned from temporary 10% rate on 1 Jan 2025 |
| PT | 23% | |
| DK | 25% | |
| NO | 25% | |
| SE | 25% | |
| FI | 25.5% | Increased from 24% Sep 2024 |
| HU | 27% | Highest VAT rate in the EU |
| LU | 8% | Significantly lower than neighbors |

Changes rarely (years between changes, except energy crisis temporary measures). VAT is
almost universally applied last — it's a tax on top of other taxes and grid fees.

## Energy Tax / Excise Duty

Updated rates from Gemini research (2025/2026) and Eurostat data:

| Country | Energy tax (ct/kWh) | Notes |
|---|---|---|
| NL | ~10.88 | Energiebelasting (€108.80/MWh). Highest in EU for small consumers. Offset by ~€600/year tax reduction per connection — marginal cost is high but average cost can look low |
| DK | ~9.37 | Elafgift (€93.73/MWh). Reduced for some heat pump users |
| FR | ~3.09 | Accise/TICFE (€30.85/MWh). Jumped from €1/MWh in 2022 to €30+/MWh in 2025 as crisis measures expired |
| SE | ~3.70 | Energiskatt (~42.8 öre/kWh). Varies slightly by region (lower in north) |
| DE | ~2.05 | Stromsteuer (€20.50/MWh). Plus KWKG, offshore, §19 NEV, Konzessionsabgabe totaling ~5.5 ct |
| BE | ~4.94 | Bijdrage energiefonds. Varies by region |
| AT | ~1.50 | Elektrizitätsabgabe |
| CH | ~2.30 | |
| PL | ~2.00 | Akcyza + RES + CHP levies |
| ES | 5.11% | **Percentage-based** on the sum of power + energy costs (unlike per-kWh everywhere else) |
| NO | ~1.25 | Elavgift + Enova levy. Note: Norway has a government spot-price subsidy — state covers costs above ~73 öre/kWh+VAT, effectively a negative surcharge during price spikes |
| FI | ~2.24 | Sähkövero class I |

Excise duties typically change once a year (Jan 1 in NL, Feb 1 in FR). Spain is unique
with its percentage-based model — requires different calculation logic.

## Grid Fees

Grid fees are the **most problematic component**:
- 25–35% of total consumer price
- Fixed per kWh in most countries (doesn't affect cheapest window)
- Massive regional variation: Germany has **800+ grid operators**, Norway ~130, Sweden ~170
- Not feasible to include without per-user grid operator selection

**Recommendation:** Exclude grid fees from the all-in calculation for most countries. They
don't affect the cheapest window (except DK and SE), and sourcing them per-user is
impractical. Mention this in the UI ("excludes grid fees").

### Countries with time-of-use grid tariffs

These countries have grid fees that vary by time of day, which **does** affect the cheapest
window and should ideally be integrated:

**Denmark (Energinet):**
- Summer (Apr–Sep) and Winter (Oct–Mar) seasons
- Three daily zones: Peak (17:00–21:00), High (06:00–17:00 + 21:00–24:00), Low (00:00–06:00)
- Winter peak fee can be **10x** the summer low fee
- Data available via Energi Data Service API (`api.energidataservice.dk/dataset/DatahubPricelist`)

**Sweden (Ellevio and other DSOs):**
- Power-based tariffs: charged on average of 3 highest consumption hours per month
- Grid fees halved between 22:00 and 06:00 to incentivize nighttime EV charging
- Varies by DSO

## Supplier Surcharges

**No public centralized API or database exists** listing supplier surcharges across Europe.

### Known dynamic tariff suppliers and approximate surcharges

| Supplier | Markets | Monthly fee | Variable surcharge |
|---|---|---|---|
| Tibber | SE, NO, DE, NL | 49 SEK / €3.99–4.99 | ~0–2.5 ct/kWh |
| Frank Energie | NL, BE, ES | €6.00 | ~1.8 ct/kWh |
| EnergyZero | NL | — | ~0.5 ct/kWh |
| EasyEnergy | NL | — | ~0 ct/kWh |
| Zonneplan | NL | — | ~0 ct/kWh |
| Vandebron | NL | — | ~2–3 ct/kWh |
| ANWB Energie | NL | — | ~1.5–3.0 ct/kWh |
| aWATTar | AT, DE | <€5.00 | ~0.3 ct/kWh (>10 MWh/yr) |
| smartENERGY | AT | €1.20–2.50 | ~1.44 ct/kWh |
| 1KOMMA5° | DE | — | ~1 ct/kWh |
| Rabot Charge | DE | — | ~0.5 ct/kWh |
| Octopus Energy | GB, DE, ES | Variable | Agile ToU formulas |
| Barry | DK | — | — |

Dynamic suppliers typically don't profit on high usage — their revenue comes from the
fixed monthly fee. This aligns their interests with the consumer's desire to shift load.

### Supplier APIs that provide all-in rates directly

| API | Coverage | Auth? | Returns |
|---|---|---|---|
| EnergyZero public API | NL | No | `base`, `all_in`, `all_in_with_vat` |
| EasyEnergy | NL | No | Spot + their markup |
| Octopus Energy | GB (14 regions) | No | `value_inc_vat` (true all-in) |
| Tibber GraphQL | Multi-country | Token (customers only) | `total`, `energy`, `tax` |
| aWATTar | AT, DE | No | Spot only |

## How Other Apps Handle This

- **Tibber app:** All-in prices, but only for Tibber customers (uses their own API)
- **EPEX Spot app / Electricity Maps:** Spot only
- **Stroomprijzen (NL):** Users select supplier to add markup
- **Home Assistant (ha_epex_spot):** Users configure percentage surcharge, absolute surcharge, and VAT. Community shares presets for smartENERGY.at, aWATTar, etc.
- **evcc:** Time-dependent grid fee formulas for DK, NL, DE, CH. Good reference for implementation patterns.
- **No app successfully shows all-in prices across multiple countries for arbitrary suppliers**

## Price Formula

Standard model for total retail price per kWh:

```
total = (spot × (1 + margin_pct) + margin_abs + grid_fee + excise) × (1 + vat) + fixed_fees_prorated
```

Where:
- `spot` = wholesale day-ahead price
- `margin_pct` = supplier's percentage-based margin (usually 0)
- `margin_abs` = supplier's fixed per-kWh surcharge
- `grid_fee` = grid/network charge (usually flat, time-varying in DK/SE)
- `excise` = energy tax / excise duty
- `vat` = value-added tax rate (applied last, on everything including other taxes)
- `fixed_fees_prorated` = monthly service fees divided by estimated monthly kWh

For Spain, replace `excise` with `(spot + grid_fee) × 0.0511` since it's percentage-based.

### Negative price caveat

During negative spot price events (e.g. Finland had negative prices ~8% of hours in 2024),
the spot component can be negative. But once excise (e.g. NL €0.1088) + grid fees + VAT
are added, the complete price almost always stays positive. Showing only spot prices is
misleading during these events — users may think they're being paid to consume, but their
actual cost is still above zero.

## Recommended Approach

### Tier 1: Hardcoded taxes + user-input surcharge (start here)

Hardcode VAT + energy tax per country (~12 entries). One settings field for "Supplier surcharge (ct/kWh)" defaulting to 0.

Formula: `all_in = (spot + energy_tax + supplier_surcharge) × (1 + vat_rate)`

```kotlin
data class CountryTaxConfig(
    val vatRate: Double,         // e.g. 0.21
    val energyTaxPerKwh: Double, // EUR/kWh, e.g. 0.0916
    val lastUpdated: String      // "2026-01-01"
)
```

**Pros:** Simple, covers ~60–90% of non-spot costs, changes at most once/year.
**Cons:** Grid fees excluded. Surcharge requires user input.

### Tier 2: Supplier presets (future)

Curated list of ~5–10 suppliers per country with their surcharge. User picks from dropdown or enters custom value. Updated with each app release.

### Tier 3: Remote config (optional)

Host tax + supplier tables in a JSON file on GitHub. App fetches periodically. Avoids requiring app updates for annual tax rate changes.

## Data Sources

| Source | URL | What |
|---|---|---|
| Eurostat API | `https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data/nrg_pc_204_c` | Tax components, all EU + NO, annual |
| VAT Sense API | `https://vatsense.com/vat-rates-api` | Current VAT/GST rates, EU + UK, by country code |
| API-Ninjas VAT API | `https://api-ninjas.com/api/vat` | Historical + current VAT rates, all EU countries |
| EnergyZero public | `https://public.api.energyzero.nl/public/v1/prices` | NL all-in hourly |
| Octopus Energy | `https://api.octopus.energy/v1/products/` | GB all-in half-hourly |
| Danish Energi Data Service | `https://api.energidataservice.dk/dataset/DatahubPricelist` | DK hourly grid tariffs |
| Prezio | `https://www.prezio.eu/` | Commercial: standardized tariffs for 10K+ European tariffs, JSON with subtotal breakdowns (retailer, grid, taxes, VAT) |
| BDEW Strompreisanalyse | `https://www.bdew.de/service/daten-und-grafiken/bdew-strompreisanalyse/` | DE breakdown (PDF) |
| evcc tariff docs | `https://docs.evcc.io/en/docs/tariffs` | Community-maintained Go formulas for DK, NL, DE, CH grid fees |
| ha_epex_spot | `https://github.com/mampfes/ha_epex_spot` | Home Assistant integration with configurable surcharges |

## Open Questions

- Should we show spot + all-in side by side, or let the user toggle?
- How prominent should the "excludes grid fees" disclaimer be?
- Should the NL implementation use the EnergyZero public API's `all_in_with_vat` field directly instead of computing it?
- Worth integrating Danish hourly grid tariffs and Swedish ToU rates? These are the only countries where grid fees affect the cheapest window.
- Spain's percentage-based excise needs special calculation logic — worth the complexity for one country?
- Should the Norwegian spot-price subsidy be modeled? It effectively caps consumer costs during price spikes.
- Remote config from the start, or hardcode first and add remote later?
- Use VAT Sense / API-Ninjas for automated VAT lookups, or hardcode (simpler, fewer dependencies)?
