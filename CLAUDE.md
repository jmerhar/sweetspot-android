# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Is

SweetSpot is an Android app that finds the cheapest contiguous time window for running an appliance, based on dynamic electricity prices. Supports 30 European countries (43 bidding zones) via the ENTSO-E Transparency Platform, with EnergyZero as a fallback for the Netherlands and Spot-Hinta.fi as a fallback for 15 Nordic/Baltic zones. It's a port of a PHP web app. Includes a Wear OS companion app for Pixel Watch and other Wear OS 3+ devices.

## Build & Run

```bash
make build                        # Build debug APKs (phone + watch)
make build-release                # Build signed release APKs
make debug-phone                  # Install debug app on connected phone
make debug-watch                  # Install debug app on connected watch
make install-phone                # Install release APK on connected phone
make install-watch                # Install release APK on connected watch
make test                         # Run all unit tests
make clean                        # Remove all build outputs
```

A `Makefile` wraps common tasks. Helper scripts live in `bin/`:
- **`bin/install.sh`** — Finds a connected phone or watch via ADB and installs the latest release APK. Called by `make install-phone` and `make install-watch`.
- **`bin/release.sh`** — Bumps version, builds, tags, pushes, and creates a GitHub Release.

### Installing the Wear OS app

The watch app must be installed separately via ADB (auto-install only works via Play Store):

1. Enable Developer Options on the watch (Settings > System > About > tap Build Number 7 times)
2. Enable Wi-Fi debugging (Settings > Developer options > Debug over Wi-Fi)
3. Connect: `adb connect <ip>:<port>`
4. Install: `make install-watch` (or manually: `adb -s <watch-serial> install wear/build/outputs/apk/release/wear-release.apk`)

Use `adb devices` to list connected devices when both phone and watch are connected.

## Releasing

```bash
make release VERSION=3.0            # Bump version, build, tag, push, create GitHub Release
make release VERSION=3.0 DRAFT=1    # Same but creates a draft release
```

The release notes file is always `docs/notes/release.md`. The script appends a "Full Changelog" link automatically. Always write meaningful, user-facing release notes describing what changed and why — overwrite `docs/notes/release.md` each release.

The script auto-increments `versionCode`, sets `versionName`, builds signed phone and wear APKs, commits, tags, pushes, and creates a GitHub Release with both APKs attached.

Release signing is configured via `local.properties` (gitignored):
```
RELEASE_STORE_FILE=release.jks
RELEASE_STORE_PASSWORD=...
RELEASE_KEY_ALIAS=sweetspot
RELEASE_KEY_PASSWORD=...
```

## Notes

`docs/notes/` contains design notes, research, and feature ideas:

- **`release.md`** — Current release notes (used by the release script)
- **`multi-zone-next-steps.md`** — Implementation tracker for multi-zone support (mix of done/pending items)
- **`ideas/`** — Feature ideas and implemented features: car charging, low price alerts, all-in pricing, localization, cache management, data source preferences (implemented), appliance power rating (kW)
- **`reference/`** — Research and reference: multi-zone API comparison, Play Store publishing guide

`docs/entsoe/` contains ENTSO-E API documentation and sample XML responses.

## Testing

```bash
./gradlew test                   # Run all unit tests (172 tests)
./gradlew testDebugUnitTest      # Run debug variant only
```

Tests live in `shared/src/test/`, `app/src/test/`, and `wear/src/test/`:
- `data/repository/PriceRepositoryTest` — cache logic, coverage re-fetch, cooldown, filtering (10 tests, in shared)
- `data/api/FallbackPriceFetcherTest` — fallback chain: single, multi, all-fail, empty list (5 tests, in shared)
- `data/api/DataSourceTest` — source registry: defaults per zone type, unique IDs, zone count (8 tests, in shared)
- `data/api/EnergyZeroApiParseTest` — JSON parsing and timezone conversion (5 tests, in shared)
- `data/api/EnergyZeroApiMalformedTest` — malformed/invalid JSON handling (8 tests, in shared)
- `data/api/EnergyZeroApiDstTest` — DST transition parsing: winter, summer, spring-forward, fall-back (5 tests, in shared)
- `data/api/EntsoeApiParseTest` — ENTSO-E XML parsing: PT60M, PT15M native resolution, A03 gaps, multi-TimeSeries, errors, DST (11 tests, in shared)
- `data/api/SpotHintaApiParseTest` — Spot-Hinta.fi JSON parsing, timezone conversion, 15-min slots (7 tests, in shared)
- `data/api/SpotHintaApiMalformedTest` — malformed/invalid JSON handling for Spot-Hinta.fi (7 tests, in shared)
- `data/api/SpotHintaApiDstTest` — DST transition parsing with Europe/Helsinki: winter, summer, spring-forward, fall-back (5 tests, in shared)
- `util/CheapestWindowFinderTest` — sliding window algorithm + breakdown invariants + zero-duration edge case + 15-min slot tests (30 tests, in shared)
- `util/TimeUtilsTest` — relative time formatting (10 tests, in shared)
- `util/FormatUtilsTest` — duration formatting (8 tests, in shared)
- `model/ApplianceIconTest` — icon resolution and unknown-ID fallback (3 tests, in shared)
- `SweetSpotViewModelTest` — ViewModel state, duration, appliance CRUD, timezone, source order, async fetch, rapid-tap cancellation (34 tests, Robolectric, in app)
- `WearViewModelTest` — Wear ViewModel state, appliance tap, source order, async fetch, rapid-tap cancellation, JSON parsing (16 tests, Robolectric, in wear)

## Stack

- Kotlin 2.3, AGP 9, Gradle 9.2 with version catalog (`gradle/libs.versions.toml`)
- minSdk 26 (phone) / 30 (wear), targetSdk 36, compileSdk 36
- `buildSrc` convention plugin (`sweetspot-app.gradle.kts`) for shared build config across `:app` and `:wear`
- Jetpack Compose with Material 3 (dynamic color on SDK 31+)
- Wear Compose with Material for the watch app
- MVVM: `SweetSpotViewModel` (phone) and `WearViewModel` (watch) with `StateFlow`
- OkHttp 5 for HTTP, kotlinx-serialization for JSON
- Wearable Data Layer API for phone-to-watch appliance and settings sync
- Material Icons Extended for appliance icon picker
- JUnit 4 + Robolectric for unit tests (172 tests)
- GitHub Actions CI (`.github/workflows/test.yml`) runs tests on push and PRs
- No frameworks, no DI, no database — SharedPreferences + file cache only
- Licensed under GPL v3

## Architecture

Three Gradle modules:

- **`:shared`** — Android Library (`si.merhar.sweetspot.shared`). Data, model, and util layers used by both phone and watch. Source: `shared/src/main/java/si/merhar/sweetspot/`.
- **`:app`** — Phone app (`si.merhar.sweetspot`). UI, ViewModel, and Data Layer push. Source: `app/src/main/java/si/merhar/sweetspot/`.
- **`:wear`** — Wear OS app (`si.merhar.sweetspot.wear`). Watch UI, ViewModel, and Data Layer read. Source: `wear/src/main/java/si/merhar/sweetspot/wear/`.

**Data flow (phone):** Duration picker (hours + minutes) → `PriceRepository` (cache or API) → `findCheapestWindow()` sliding window → `UiState` update → Compose UI reacts.

**Data flow (watch):** Data Layer listener → appliance list → user taps chip → `PriceRepository` → `findCheapestWindow()` → `WearUiState` → Wear Compose UI.

**Appliance sync:** Phone pushes appliance JSON to `/appliances` path via `PutDataMapRequest` after every CRUD operation. Zone settings (country code, price zone ID, source order) are pushed to `/settings` path. Watch reads on init and listens for live updates via `DataClient.OnDataChangedListener`.

### Shared module (`:shared`)

The data layer is organized into three subpackages under `data/`:

**`data/api/`** — API implementations and fetcher infrastructure:
- **`DataSource` / `DataSources`** — Registry of all supported price data sources (ENTSO-E, EnergyZero, Spot-Hinta.fi). `DataSources.defaultsForZone(zoneId)` returns available sources in default priority order per zone. Also contains `SPOT_HINTA_ZONES` set.
- **`PriceFetcher`** — Interface with a single `fetchPrices(from, to, timeZoneId)` method returning `FetchResult` (prices + source name). `FetchResult` pairs a `List<PriceSlot>` with the data source name (e.g. "ENTSO-E", "EnergyZero"). Decouples `PriceRepository` from a specific API provider.
- **`FallbackPriceFetcher`** — `PriceFetcher` that tries a list of fetchers in order and returns the first successful result. If all fail, throws the last exception. Used for NL (ENTSO-E primary, EnergyZero fallback) and Nordic/Baltic zones (ENTSO-E primary, Spot-Hinta.fi fallback).
- **`PriceFetcherFactory`** — `fun interface` that returns the right `PriceFetcher` for a given `PriceZone`. `defaultPriceFetcherFactory(entsoeToken, sourceOrder)` builds the fetcher chain dynamically from the user's source order preference (or zone defaults when `null`). Always wraps in `FallbackPriceFetcher`.
- **`EnergyZeroApi`** — `PriceFetcher` singleton for the EnergyZero API (NL-only). Returns JSON, parses with kotlinx-serialization. Also exposes `fetchRaw()` and `parse()` directly for tests.
- **`SpotHintaApi`** — `PriceFetcher` for the Spot-Hinta.fi API (15 Nordic/Baltic zones). Returns JSON (top-level array), parses with kotlinx-serialization. Prices are already EUR/kWh, 15-minute resolution. Region parameter maps directly to zone IDs. Also exposes `fetchRaw()` and `parse()` directly for tests.
- **`EntsoeApi`** — `PriceFetcher` for the ENTSO-E Transparency Platform (all European bidding zones). Parses XML with `XmlPullParser`, handles A03 curve type gaps, returns prices at native resolution (PT15M or PT60M), converts EUR/MWh to EUR/kWh. Also exposes `fetchRaw()` and `parse()` directly for tests.
- **`BiddingZone`** — Object with EIC code constants for 43 European bidding zones. EIC codes are a European-wide standard used across ENTSO-E, EPEX SPOT, Nord Pool, etc.

**`data/cache/`** — Caching layer:
- **`PriceCache`** — Interface for caching parsed prices, keyed by zone. `readCached(key)` / `write(key, data)` with global cooldown. Returns `CachedPriceData` (prices + source). Abstracts storage so `PriceRepository` can be tested without Android. Also contains `CachedPrice` (data class with `epochSecond`, `durationMinutes`, `price`) and `CachedPriceData` (wrapper with source name).
- **`FilePriceCache`** — `PriceCache` implementation using per-zone binary files (`cacheDir/prices_<key>.bin`). Format v3: version byte + source UTF + count int + N × (epochSecond long + durationMinutes short + price double) = 18 bytes per entry. SharedPreferences `sweetspot_cache` tracks global cooldown. Returns `null` on any format error for graceful migration (including v1/v2 caches).

**`data/repository/`** — Business logic:
- **`PriceRepository`** — Created per-call with current `ZoneId` and `cacheKey`. Returns `PriceResult` (prices + source name). Computes date range (today → day-after-tomorrow), reads typed cache first (maps `CachedPrice` → `PriceSlot` with zone applied), filters to future prices using slot-aware end-time check, re-fetches if coverage is below 12 hours (with 5-minute cooldown). Threads the data source name from `FetchResult`/cache through to `PriceResult`. Takes injectable `PriceFetcher` and `Clock` for testing.
- **`SettingsRepository`** — SharedPreferences `sweetspot_settings`. Stores country code, price zone ID, timezone override, data source order (JSON list of source IDs), and appliances (JSON-serialized list). Auto-detects country on first access via `CountryDetector`. Country change clears custom source order.
- **`CountryDetector`** — Zero-permission country auto-detection for first launch. Checks SIM → network → timezone → locale → NL fallback.
- **`model/PriceZone`** — Data class representing a bidding zone (`id`, `label`, `eicCode`, `timeZoneId`). `Country` groups zones by country. `Countries` is the registry of all 30 supported countries / 43 zones, with `defaultCountry()` (NL), `findByCode()`, and `findPriceZoneById()`.
- **`model/Appliance`** — `@Serializable` data class with `id`, `name`, `durationHours`, `durationMinutes`, and `icon` (string ID referencing the icon registry).
- **`model/ApplianceIcon`** — Icon registry mapping string IDs to Material `ImageVector`s. Contains 26 curated icons (18 household appliances + 8 generic). `applianceIconFor(id)` resolves an ID to its icon.
- **`util/CheapestWindowFinder`** — Pure function implementing the sliding window algorithm. Works with any slot duration (15min, 30min, 60min). Converts requested duration to "slot units" and multiplies by `slotMinutes / 60.0` for EUR costs. Supports fractional slots. Split into `findBestStartIndex`, `computeWindowCost`, and `buildBreakdown`.
- **`util/FormatUtils`** — `formatDuration()` and `shortTimeFormatter` shared by ViewModel and UI screens.
- **`util/TimeUtils`** — `formatRelative()` helper for "in Xh Ym" display.

### Phone app (`:app`)

- **`SweetSpotViewModel`** — Owns all UI state. Orchestrates duration selection, price fetching via `PriceRepository`, and cheapest-window calculation via `findCheapestWindow()`. Creates `PriceFetcherFactory` dynamically from the current source order preference. CRUD for appliances persisted via `SettingsRepository`. Country/zone selection with auto-detection on first launch. Pushes appliances, zone settings, and source order to Wearable Data Layer after every change via `syncAppliancesToWear()` / `syncSettingsToWear()`. Stores `priceSource` in `UiState` for display in the results disclaimer. Errors use an `AppError` sealed interface (`Validation` for inline errors, `Network` for snackbar errors).

### Wear app (`:wear`)

- **`WearViewModel`** — Reads appliances, zone settings, and source order from Data Layer on init, listens for live updates. On appliance tap, creates `PriceFetcherFactory` dynamically from source order, fetches prices via `PriceRepository` (using the phone's zone) and runs `findCheapestWindow()`. Prices are cached locally on the watch.
- **`WearActivity`** — `SwipeDismissableNavHost` with two routes: `"appliances"` (start) and `"result"`.
- **`ui/ApplianceListScreen`** — `Scaffold` with `PositionIndicator`, `TimeText`, `ScalingLazyColumn` of appliance `Chip`s (icon + name + duration), empty state, loading overlay.
- **`ui/ResultScreen`** — `ScalingLazyColumn` centered on the appliance label, with start/end times in HH:mm and relative display that auto-refreshes every 60 seconds. Scrollable for long labels on round watch faces.
- **`ui/WearTheme`** — Wear Material theme wrapper.

### Phone navigation

State-based in `MainActivity`, no navigation library:
- `UiState.showSettings` toggles between `SweetSpotScreen` and `SettingsScreen`
- `UiState.result != null` switches `SweetSpotScreen` from the form view to a dedicated results screen (separate `Scaffold` with back arrow and result label in the top bar)
- System back and the top-bar back arrow both call `onClearResult()` to return to the form

### Main Screen

The form view (`DurationInput` card) contains:
- **Appliance chips** (top) — `AssistChip` buttons with configurable icons for user-defined appliances; tapping fills duration and triggers search. If no appliances exist, a CTA links to settings.
- **Quick-duration row** (below) — 6 equal-width `SuggestionChip` buttons (1h–6h) using `Row` with `weight(1f)` so they fill the row on any screen width.
- **Duration picker** — two-column scroll wheel (`DurationPicker`) for hours (0–24) and minutes (0–55 in 5-min steps) with snap-to-item behavior.
- **Find button** — disabled when duration is 0h 0m.

### Theme

`SweetSpotTheme` wraps Material 3 with dynamic color. Bar chart colors (blue normal, green optimal) use `CompositionLocal` to stay fixed regardless of dynamic color.

## External APIs

- **ENTSO-E Transparency Platform** (primary for all zones) — 43 European bidding zones, 15-min resolution. API docs: https://transparencyplatform.zendesk.com/hc/en-us/articles/15692855254548-Sitemap-for-Restful-API-Integration. Token stored in `local.properties` as `ENTSOE_API_TOKEN`, injected via `BuildConfig`.
- **Spot-Hinta.fi** (Nordic/Baltic fallback) — 15 zones (FI, SE1–SE4, DK1–DK2, NO1–NO5, EE, LV, LT), 15-min resolution, prices in EUR/kWh. Endpoint: `https://api.spot-hinta.fi/TodayAndDayForward?region={region}`. No auth required. Used as fallback when ENTSO-E fails for Nordic/Baltic zones.
- **EnergyZero** (NL fallback) — NL-only day-ahead prices: `https://api.energyzero.nl/v1/energyprices`. No auth required. Used as fallback when ENTSO-E fails for NL.

## Key Conventions

- Prices are **EUR per kWh** (Double)
- Price data uses `PriceSlot` with a `durationMinutes` field (e.g. 60 for hourly EnergyZero, 15 for quarter-hourly ENTSO-E). The entire pipeline is resolution-aware — no hardcoded 60-minute assumptions.
- All times use configurable `ZoneId` (defaults to the selected price zone's timezone, overridable in settings)
- `ZoneId` is threaded as a parameter through ViewModel → Repository → API — not stored as a global
- **Naming:** `timeZoneId` for `java.time.ZoneId` / timezone concepts, `priceZone` / `priceZoneId` for `PriceZone` / bidding zone concepts — never bare `zoneId`
- Default country (NL) is defined in one place only: `Countries.defaultCountry()`
- Duration is stored as `durationHours: Int` + `durationMinutes: Int` (no string parsing on the main flow)
- UI text is hardcoded in Composables (no string resources / i18n)
- All classes and functions have KDoc comments — always add KDoc when creating new functions or classes

## Commit Messages

[Conventional Commits](https://www.conventionalcommits.org/): `<type>: <description>` describing the **what** and **why**.

Types: `feat`, `fix`, `refactor`, `style`, `docs`, `chore`.
