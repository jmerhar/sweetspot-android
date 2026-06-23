# Car Charging Support

## Idea

Add a dedicated screen (or tab) for EV charging that lets users enter their current and
target state of charge instead of a raw duration. The app calculates the required charging
time from the battery parameters and charger power, then runs the existing cheapest-window
algorithm.

## User flow

1. User enters **current SoC** (e.g. 20%) and **target SoC** (e.g. 80%)
2. App calculates charging duration from battery capacity and charger power:
   `hours = (targetSoC - currentSoC) / 100 * batteryCapacityKwh / chargerPowerKw`
3. Runs `findCheapestWindow()` with the calculated duration
4. Shows the same result screen (start/end time, cost breakdown, bar chart)

## Settings

- **Charger power (kW)** — home charger output (e.g. 3.7, 7.4, 11, 22 kW). Stored in
  `SettingsRepository`, synced to watch via Data Layer.
- **Battery capacity (kWh)** — total usable capacity of the EV battery (e.g. 60 kWh for
  a typical mid-range EV). Could offer presets for popular models or just a numeric input.

## UI

- New screen/tab alongside the existing duration picker, or a dedicated "EV" appliance
  type that opens the SoC input instead of the duration picker.
- Two sliders or number inputs for current and target SoC (0–100%).
- Display the calculated duration before searching so the user can verify it looks right.
- Could show estimated cost at current electricity prices as a preview.

## Considerations

- Charging is not perfectly linear — efficiency drops near 100% SoC. A simple linear
  calculation is a good starting point but may underestimate time for high target SoC
  values. Could add an efficiency factor (e.g. 90%) or warn when target > 80%.
- Some users may want to set a "ready by" deadline (e.g. charge to 80% by 7:00 AM).
  This would constrain the search window rather than searching all available prices.
- The charger power and battery capacity could also be saved per-vehicle if the user
  has multiple EVs.
- Watch app: could show a simplified version — tap an EV "appliance" that uses the
  last-used SoC values, or just show the duration-based result.

## EV / PHEV Database Research (April 2026)

Instead of making users type in battery capacity and charger power manually, we could
let them pick their vehicle from a database. Research into free, open EV databases:

### 1. Kilowatt / Open EV Data (RECOMMENDED PRIMARY)

- **URL:** https://github.com/KilowattApp/open-ev-data
- **Search tool:** https://kilowattapp.github.io/open-ev-data/search
- **Vehicles:** 1,321 from 76 brands (2010–2025)
- **Data:** `usable_battery_size` (kWh, 100%), `ac_charger.max_power` (kW, 100%),
  AC phases, port types, `dc_charger.max_power` (kW, 100%), DC charging curve
  (power at SoC percentages), energy consumption (kWh/100km), voltage (400V/800V)
- **BEV + PHEV:** Yes — includes both. The `type` field distinguishes them (`bev`/`phev`).
- **Format:** Single JSON file, fetchable directly from GitHub:
  `https://raw.githubusercontent.com/KilowattApp/open-ev-data/master/data/ev-data.json`
  Also available split by brand (V2 format).
- **License:** MIT with attribution — must visibly credit "Open EV Data" in the app.
- **Updates:** Actively maintained (last commit April 2025), roughly weekly updates.
  Fork of the original Chargeprice open-ev-data that went paid-only.
- **Pros:** Most vehicles, broadest year range, simple flat JSON, 100% coverage of
  both battery capacity and AC charging power, DC charging curves (useful for future
  DC charging support), includes PHEVs.
- **Cons:** No hosted API — just a static JSON file on GitHub. Attribution required.

### 2. OpenEV Data Dataset (RECOMMENDED SUPPLEMENTARY)

- **URL:** https://github.com/open-ev-data/open-ev-data-dataset
- **API repo:** https://github.com/open-ev-data/open-ev-data-api
- **Vehicles:** 1,189 from 65 makes (2023–2025, focus on current models)
- **Data:** Battery net capacity (100%), battery gross capacity (99%), AC max charging
  power (100%), AC phases (93%), DC max charging power (96%), WLTP range (62%),
  EPA range (33%), powertrain, dimensions, charge ports, battery chemistry, V2X support.
- **BEV + PHEV:** Yes — the `powertrain` field includes both BEV and PHEV types.
- **Format:** JSON, CSV, SQLite, PostgreSQL dump, XML — all available from GitHub
  Releases. Self-hostable REST API (Rust/Axum) with OpenAPI docs.
  Latest: `https://github.com/open-ev-data/open-ev-data-dataset/releases/download/v1.24.0/open-ev-data-v1.24.0.json`
- **License:** CDLA-Permissive-2.0 (data), AGPL-3.0 (API). No attribution requirement
  for the data itself.
- **Updates:** Very active in late 2025 (24 releases in December 2025 alone). Relatively
  new project with structured contribution workflow and JSON schema validation.
- **Pros:** Richer schema (AC phases, charge ports, markets, sources with URLs), SQLite
  format is ideal for embedding in an Android app, permissive license with no attribution.
- **Cons:** Fewer vehicles, only 2023–2025 model years (no older vehicles), no hosted API
  (self-host only).

### 3. US DOE / EPA fueleconomy.gov (NOT SUITABLE)

- **URL:** https://www.fueleconomy.gov/feg/ws/
- **Vehicles:** 900+ EVs (US market only), back to ~2010
- **Data:** Energy consumption (kWh/100mi), EPA range, charge time at 240V (hours).
  Motor power is a text field, not numeric kW. **No battery capacity or AC charging
  power fields.**
- **BEV + PHEV:** Yes — includes both, distinguished by `fuelType` field.
- **Format:** Free REST API (XML, no API key), downloadable CSV.
- **License:** US Government public domain.
- **Why not:** Missing the two key fields we need (battery kWh, AC charging kW). Could
  derive approximate battery capacity from `consumption × range`, but too imprecise.

### 4. ev-database.org (NOT FREE)

- **URL:** https://ev-database.org
- **Data:** Comprehensive specs including battery, charging, pricing, dimensions.
- **BEV + PHEV:** Yes.
- **Access:** Paid API/export only. Free demo with limited vehicles. Business-only
  registration — they reject individual/private applications.
- **Why not:** Not free for programmatic use.

### 5. EV Specifications / evspecifications.com (NOT SUITABLE)

- **URL:** https://www.evspecifications.com
- **Data:** Detailed specs per vehicle.
- **Access:** Website only. No API, no downloadable data.
- **Why not:** No programmatic access. Scraping would be fragile and likely against ToS.

### 6. US EV Battery Warranty Dataset (PARTIAL)

- **URL:** https://github.com/mohitk24/ev-warranty-us-dataset
- **Vehicles:** 1,117 entries from 34 US OEMs (2010–2025)
- **Data:** Battery capacity (gross kWh), DC fast charge max power, battery chemistry,
  warranty terms. **No AC charging power field.**
- **BEV + PHEV:** Unclear — likely BEV-focused given the warranty angle.
- **Format:** Single CSV on GitHub. CC BY 4.0 license.
- **Why not:** Missing AC charging power. US market only. Static snapshot, unclear if
  maintained.

### Recommendation

Use **Kilowatt open-ev-data** as the primary source:
- Fetch `ev-data.json` from GitHub periodically (e.g. on app update or weekly)
- Bundle a snapshot in the APK as a fallback
- 1,321 vehicles with 100% coverage of battery capacity and AC charging power
- Includes both BEVs and PHEVs
- Simple JSON structure, easy to parse with kotlinx-serialization
- MIT license (add "Data: Open EV Data" attribution in settings/about screen)

Optionally cross-reference with **OpenEV Data Dataset** for additional fields (AC phases,
charge port details, battery chemistry) or if we need SQLite for local queries.

### Integration Approach

1. Ship a bundled `ev-data.json` snapshot in `assets/`
2. On first launch (or periodically), fetch the latest version from GitHub
3. Parse into a local data class: `EvVehicle(brand, model, year, type, batteryKwh, acPowerKw)`
4. Show a searchable vehicle picker: Brand → Model → Variant
5. When a vehicle is selected, pre-fill battery capacity and AC charging power
6. User can still override values manually (e.g. if they have a non-standard charger)
