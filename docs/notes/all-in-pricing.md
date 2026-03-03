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
| **Grid / transmission fees** | Usually no | Per grid operator. **Denmark is the exception** — hourly grid tariffs |
| **Renewable / CHP levies** | No (fixed per kWh) | Per country, varies by year |

**Key insight: adding fixed per-kWh surcharges does NOT change which time window is cheapest.** The sliding window algorithm finds the minimum-cost window based on relative price differences. Adding a constant to every hour shifts all costs equally. All-in pricing is therefore a **display-only feature** — it affects the total cost shown ("Running your dishwasher costs €0.87") but not the recommendation of when to run it.

The exception is Denmark, where grid tariffs vary by hour (peak vs off-peak via Energi Data Service API).

## VAT Rates on Electricity

| Country | VAT | Notes |
|---|---|---|
| NL | 21% | Was temporarily 9% (Jul 2022 – Dec 2023) |
| BE | 6% | Reduced from 21% since Apr 2022 (extended repeatedly) |
| FR | 20% | 5.5% on subscription portion only |
| DE | 19% | |
| AT | 20% | |
| CH | 8.1% | Increased from 7.7% Jan 2024 |
| PL | 23% | Was temporarily 5% (2024) |
| GB | 5% | Special reduced rate for domestic energy |
| DK | 25% | |
| NO | 25% | |
| SE | 25% | |
| FI | 25.5% | Increased from 24% Sep 2024 |

Changes rarely (years between changes, except energy crisis temporary measures).

## Energy Tax / Excise Duty

From Eurostat 2024 data (`nrg_pc_204_c`), band DC (2500–5000 kWh/year households):

| Country | Energy tax (ct/kWh) | Notes |
|---|---|---|
| NL | ~9.16 | Energiebelasting. Net negative for avg consumer due to annual reduction (€643) |
| BE | ~4.94 | Bijdrage energiefonds. Varies by region |
| FR | ~1.82 | TICFE: being restored toward 2.25 ct post-crisis |
| DE | ~5.45 | Stromsteuer 2.05 + KWKG ~0.28 + Offshore ~0.82 + §19 NEV ~0.64 + Konzessionsabgabe ~1.66 |
| AT | ~1.50 | Elektrizitätsabgabe |
| CH | ~2.30 | |
| PL | ~2.00 | Akcyza + RES + CHP levies |
| GB | 0 | No per-kWh tax on households (CCL is business-only) |
| DK | ~10.20 | Elafgift ~0.723 DKK/kWh (highest in EU) |
| NO | ~1.25 | Elavgift + Enova levy |
| SE | ~3.73 | Energiskatt ~42.8 öre/kWh |
| FI | ~2.24 | Sähkövero class I |

## Grid Fees

Grid fees are the **most problematic component**:
- 25–35% of total consumer price
- Fixed per kWh in most countries (doesn't affect cheapest window)
- Massive regional variation: Germany has **800+ grid operators**, Norway ~130, Sweden ~170
- Denmark has **hourly** grid tariffs (Energi Data Service API)
- Not feasible to include without per-user grid operator selection

**Recommendation:** Exclude grid fees from the all-in calculation. They don't affect the cheapest window (except DK), and sourcing them per-user is impractical. Mention this in the UI ("excludes grid fees").

## Supplier Surcharges

**No public centralized API or database exists** listing supplier surcharges across Europe.

### Known dynamic tariff suppliers and approximate surcharges

**NL** (most mature market):
- EnergyZero: ~0.5 ct/kWh
- Frank Energie: ~1.0–2.0 ct/kWh
- ANWB Energie: ~1.5–3.0 ct/kWh
- Tibber: ~1.5–2.5 ct/kWh
- EasyEnergy: ~0 ct/kWh
- Zonneplan: ~0 ct/kWh
- Vandebron: ~2–3 ct/kWh

**DE:** Tibber (~2 ct), aWATTar (~1 ct), 1KOMMA5° (~1 ct), Rabot Charge (~0.5 ct)
**NO/SE:** Tibber (dominant), various local suppliers
**GB:** Octopus Energy Agile (variable, API available), So Energy
**AT:** aWATTar (~1 ct), Tibber
**DK:** Barry, Tibber

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
- **Home Assistant integrations:** Spot only, users add template sensors for taxes
- **No app successfully shows all-in prices across multiple countries for arbitrary suppliers**

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
| EnergyZero public | `https://public.api.energyzero.nl/public/v1/prices` | NL all-in hourly |
| Octopus Energy | `https://api.octopus.energy/v1/products/` | GB all-in half-hourly |
| Danish Energi Data Service | `https://api.energidataservice.dk/dataset/DatahubPricelist` | DK hourly grid tariffs |
| BDEW Strompreisanalyse | `https://www.bdew.de/service/daten-und-grafiken/bdew-strompreisanalyse/` | DE breakdown (PDF) |

## Open Questions

- Should we show spot + all-in side by side, or let the user toggle?
- How prominent should the "excludes grid fees" disclaimer be?
- Should the NL implementation use the EnergyZero public API's `all_in_with_vat` field directly instead of computing it?
- Worth integrating the Danish hourly grid tariff API? (only country where grid fees affect cheapest window)
- Remote config from the start, or hardcode first and add remote later?
