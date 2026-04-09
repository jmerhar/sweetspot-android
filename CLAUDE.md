# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Is

SweetSpot is an Android app that finds the cheapest contiguous time window for running an appliance, based on dynamic electricity prices. Supports 30 European countries (43 bidding zones) via the ENTSO-E Transparency Platform, with Spot-Hinta.fi as a fallback for 15 Nordic/Baltic zones, Energy-Charts as a fallback for 15 European zones, EnergyZero as a fallback for the Netherlands, and aWATTar as a fallback for Austria and Germany. Includes a Wear OS companion app for Pixel Watch and other Wear OS 3+ devices.

## Build & Run

```bash
make build                        # Build debug APKs (phone + watch)
make build-release                # Build signed release APKs
make bundle                       # Build signed release AABs for Play Store
make debug-phone                  # Install debug app on connected phone
make debug-watch                  # Install debug app on connected watch
make install-phone                # Install release APK on connected phone
make install-watch                # Install release APK on connected watch
make test                         # Run all unit tests
make inspect                      # Summarise inspection XML files (see Inspections section)
make site-validate                # Validate Hugo site (build, pages, links, i18n)
make screenshots                  # Capture localized screenshots via Screengrab (LOCALE=xx-XX for one)
make frames                       # Frame screenshots with marketing text (LOCALE=xx-XX for one)
make feature-graphic              # Generate localised Play Store feature graphics (LOCALE=xx-XX for one)
make publish                      # Upload metadata, screenshots, and images to Play Store
make clean                        # Remove all build outputs
```

A `Makefile` wraps common tasks. Helper scripts live in `bin/`:
- **`bin/install.sh`** — Finds a connected phone or watch via ADB and installs the latest release APK. Called by `make install-phone` and `make install-watch`.
- **`bin/release.sh`** — Bumps version, builds, tags, pushes, and creates a GitHub Release.
- **`bin/inspect.sh`** — Summarises inspection XML files exported from Android Studio. Does **not** run inspections itself. Called by `make inspect`.
- **`bin/site-validate.sh`** — Validates the Hugo site: builds, checks expected pages/assets exist, verifies internal links resolve, checks page sizes, and ensures i18n key parity across languages. Called by `make site-validate`.
- **`bin/frame-screenshots.sh`** — Frames raw Screengrab screenshots with marketing text and coloured backgrounds. Outputs to `fastlane/metadata/android/<locale>/images/phoneScreenshots/` and generates `build/screenshots.html` gallery. Requires ImageMagick 7. Called by `make frames`.
- **`bin/feature-graphic.sh`** — Generates localised Play Store feature graphics (1024x500) with gradient, app icon, and translated tagline. Outputs to `fastlane/metadata/android/<locale>/images/featureGraphic.png` and generates `build/feature-graphics.html` gallery. Requires ImageMagick 7 and Python 3. Called by `make feature-graphic`.

Fastlane is used for automated screenshot capture and Play Store metadata upload. Requires Ruby 3.3 (managed via `.ruby-version` and rbenv). Lanes are defined in `fastlane/Fastfile`:
- **`screenshots`** — Builds debug APKs and runs Screengrab across all locales (or one with `locale:xx`).
- **`publish`** — Uploads metadata, screenshots, and images to the Play Store via `upload_to_play_store`. Dynamically resolves the latest version code on the alpha track. Runs automatically in CI via `.github/workflows/publish-listing.yml` when metadata changes are pushed to `main`. Requires `PLAY_STORE_SERVICE_ACCOUNT_JSON` GitHub secret.

### Installing the Wear OS app

The watch app must be installed separately via ADB (auto-install only works via Play Store):

1. Enable Developer Options on the watch (Settings > System > About > tap Build Number 7 times)
2. Enable Wi-Fi debugging (Settings > Developer options > Debug over Wi-Fi)
3. Connect: `adb connect <ip>:<port>`
4. Install: `make install-watch` (or manually: `adb -s <watch-serial> install wear/build/outputs/apk/release/sweetspot-wear-release.apk`)

Use `adb devices` to list connected devices when both phone and watch are connected.

## Releasing

```bash
make release VERSION=3.0            # Bump version, build, tag, push, create GitHub Release
make release VERSION=3.0 DRAFT=1    # Same but creates a draft release
```

The release notes file is always `docs/notes/release.md`. The script appends a "Full Changelog" link automatically. Always write meaningful, user-facing release notes describing what changed and why — overwrite `docs/notes/release.md` each release. **Important:** The release script requires a clean working tree, so commit the release notes before running `make release`. **Also:** Update the website changelog (`site/content/<lang>/changelog.md`) for all 25 languages before releasing — see "Updating the Changelog" below.

The script auto-increments `versionCode`, sets `versionName`, builds signed phone and wear APKs and AABs, commits, tags, pushes, and creates a GitHub Release with APKs attached. AABs are built but not uploaded to GitHub — use them for Play Store submission.

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
- **`ideas/`** — Feature ideas (mix of done and pending): website, car charging, low price alerts, all-in pricing, appliance power rating (kW), widget, test coverage CI, API reliability stats, ViewModel locale bug
- **`ideas/done/`** — Implemented features: localisation, cache management, data source preferences
- **`reference/`** — Research and reference: multi-zone API comparison, Play Store publishing guide, country & language coverage audit

`docs/entsoe/` contains ENTSO-E API documentation and sample XML responses.

## Testing

```bash
./gradlew test                   # Run all unit tests (304 tests)
./gradlew testDebugUnitTest      # Run debug variant only
```

Tests live in `shared/src/test/`, `app/src/test/`, and `wear/src/test/`:
- `data/repository/PriceRepositoryTest` — cache logic, coverage re-fetch, cooldown, filtering (10 tests, in shared)
- `data/api/FallbackPriceFetcherTest` — fallback chain: single, multi, all-fail, empty list (5 tests, in shared)
- `data/api/DataSourceTest` — source registry: defaults per zone type, unique IDs, zone ID validation against Countries registry (12 tests, in shared)
- `data/api/EnergyZeroApiParseTest` — JSON parsing and timezone conversion (5 tests, in shared)
- `data/api/EnergyZeroApiMalformedTest` — malformed/invalid JSON handling (8 tests, in shared)
- `data/api/EnergyZeroApiDstTest` — DST transition parsing: winter, summer, spring-forward, fall-back (5 tests, in shared)
- `data/api/EntsoeApiParseTest` — ENTSO-E XML parsing: PT60M, PT15M native resolution, A03 gaps, multi-TimeSeries, overlapping TimeSeries dedup, errors, DST (14 tests, in shared)
- `data/api/SpotHintaApiParseTest` — Spot-Hinta.fi JSON parsing, timezone conversion, 15-min slots (7 tests, in shared)
- `data/api/SpotHintaApiMalformedTest` — malformed/invalid JSON handling for Spot-Hinta.fi (7 tests, in shared)
- `data/api/SpotHintaApiDstTest` — DST transition parsing with Europe/Helsinki: winter, summer, spring-forward, fall-back (5 tests, in shared)
- `data/api/EnergyChartsApiParseTest` — Energy-Charts JSON parsing, EUR/MWh→kWh conversion, resolution detection, timezone conversion (9 tests, in shared)
- `data/api/EnergyChartsApiMalformedTest` — malformed/invalid JSON handling for Energy-Charts (8 tests, in shared)
- `data/api/EnergyChartsApiDstTest` — DST transition parsing with Europe/Berlin: winter, summer, spring-forward, fall-back (5 tests, in shared)
- `data/api/AwattarApiParseTest` — aWATTar JSON parsing, EUR/MWh→kWh conversion, timestamp conversion, duration computation (9 tests, in shared)
- `data/api/AwattarApiMalformedTest` — malformed/invalid JSON handling for aWATTar (6 tests, in shared)
- `data/api/AwattarApiDstTest` — DST transition parsing with Europe/Vienna: winter, summer, spring-forward, fall-back (5 tests, in shared)
- `util/CheapestWindowFinderTest` — sliding window algorithm + breakdown invariants + zero-duration edge case + 15-min slot tests (30 tests, in shared)
- `util/TimeUtilsTest` — relative time formatting (10 tests, in shared)
- `util/FormatUtilsTest` — duration formatting, locale-aware price formatting (12 tests, in shared)
- `model/ApplianceIconTest` — icon resolution and unknown-ID fallback (3 tests, in shared)
- `model/PriceSlotTest` — overlapsWindow interval intersection: inside, before, after, boundary, partial overlap, hourly (8 tests, in shared)
- `SweetSpotViewModelTest` — ViewModel state, duration, appliance CRUD, timezone, source order, async fetch, rapid-tap cancellation, cache management, stats settings and prompt, trial/paywall/billing, developer options (71 tests, Robolectric, in app)
- `WearViewModelTest` — Wear ViewModel state, appliance tap, source order, async fetch, rapid-tap cancellation, JSON parsing, locked state (18 tests, Robolectric, in wear)
- `data/stats/ErrorCategoryTest` — exception → category mapping for all supported exception types (13 tests, in shared)
- `data/stats/InstrumentedPriceFetcherTest` — success/failure/empty recording, delegation, clock, accumulation (6 tests, in shared)
- `data/stats/FileStatsCollectorTest` — record, read, clear, append, persistence, corruption (8 tests, in shared)
- `data/stats/StatsReporterTest` — JSON format, grouping, version field, error field presence (5 tests, in app)

## Inspections

Inspections are run manually in Android Studio and exported as XML — **not** run from the CLI.

**Workflow:**
1. The user runs "Code → Inspect Code" in Android Studio (whole project, default profile)
2. The user exports results to `inspect/xml/` (one XML file per inspection category)
3. Claude reads the XML files, identifies new issues, and fixes them
4. The user re-runs the inspection in Android Studio and re-exports
5. Claude verifies the issues are resolved

**Important:** `make inspect` / `bin/inspect.sh` only **summarises** the existing XML files — it does not run inspections. Do not attempt to run inspections from the CLI. The XML files in `inspect/xml/` are gitignored local artifacts.

## Stack

- Kotlin 2.3, AGP 9, Gradle 9.4 with version catalog (`gradle/libs.versions.toml`)
- minSdk 26 (phone) / 30 (wear), targetSdk 36, compileSdk 36
- `buildSrc` convention plugin (`sweetspot-app.gradle.kts`) for shared build config across `:app` and `:wear`
- Jetpack Compose with Material 3 (dynamic colour on SDK 31+)
- Wear Compose with Material for the watch app
- MVVM: `SweetSpotViewModel` (phone) and `WearViewModel` (watch) with `StateFlow`
- OkHttp 5 for HTTP, kotlinx-serialization for JSON
- Wearable Data Layer API for phone-to-watch appliance and settings sync
- Material Symbols (Outlined, 24px) as XML vector drawables for appliance icons — downloaded from [google/material-design-icons](https://github.com/google/material-design-icons) `symbols/android/` directory
- Play Billing Library (`billing-ktx` 8.3.0) for yearly subscription (phone only)
- JUnit 4 + Robolectric for unit tests (304 tests)
- GitHub Actions CI (`.github/workflows/test.yml`) runs tests on push and PRs
- GitHub Actions CI (`.github/workflows/publish-listing.yml`) auto-publishes Play Store listing metadata on pushes to `main` that change `fastlane/metadata/android/**`
- No frameworks, no DI, no database — SharedPreferences + file cache only
- Licensed under GPL v3

## Architecture

Three Gradle modules:

- **`:shared`** — Android Library (`today.sweetspot.shared`). Data, model, and util layers used by both phone and watch. Source: `shared/src/main/java/today/sweetspot/`.
- **`:app`** — Phone app (`today.sweetspot`). UI, ViewModel, and Data Layer push. Source: `app/src/main/java/today/sweetspot/`.
- **`:wear`** — Wear OS app (`today.sweetspot.wear`). Watch UI, ViewModel, and Data Layer read. Source: `wear/src/main/java/today/sweetspot/wear/`.

**Data flow (phone):** Duration picker (hours + minutes) → `PriceRepository` (cache or API) → `findCheapestWindow()` sliding window → `UiState` update → Compose UI reacts.

**Data flow (watch):** Data Layer listener → appliance list → user taps chip → `PriceRepository` → `findCheapestWindow()` → `WearUiState` → Wear Compose UI.

**Appliance sync:** Phone pushes appliance JSON to `/appliances` path via `PutDataMapRequest` after every CRUD operation. Zone settings (country code, price zone ID, source order) are pushed to `/settings` path. Watch reads on init and listens for live updates via `DataClient.OnDataChangedListener`.

**Stats sync:** Watch pushes accumulated stats records to `/stats` path after each fetch. Phone receives via `DataClient.OnDataChangedListener`, appends to its local stats file, and includes them in the next report.

### Shared module (`:shared`)

The data layer is organised into four subpackages under `data/`:

**`data/api/`** — API implementations and fetcher infrastructure:
- **`DataSource` / `DataSources`** — Registry of all supported price data sources (ENTSO-E, EnergyZero, Spot-Hinta.fi, Energy-Charts, aWATTar). `DataSources.defaultsForZone(zoneId)` returns available sources in default priority order per zone using a declarative registry — list order defines fallback priority, each entry declares which zones it covers.
- **`PriceFetcher`** — Interface with a single `fetchPrices(from, to, timeZoneId)` method returning `FetchResult` (prices + source name). `FetchResult` pairs a `List<PriceSlot>` with the data source name (e.g. "ENTSO-E", "EnergyZero"). Decouples `PriceRepository` from a specific API provider. Also defines `sharedHttpClient`, a single `OkHttpClient` (10s connect + 10s read timeout) shared by all API implementations.
- **`HttpException`** — Typed exception for non-200 HTTP responses. Carries the HTTP `code` for reliable error categorisation.
- **`EntsoeException`** — Typed exception for ENTSO-E Acknowledgement_MarketDocument errors (HTTP 200 but error body). Carries the `reason` text. Categorised as `ENTSOE_ERROR` in stats.
- **`FallbackPriceFetcher`** — `PriceFetcher` that tries a list of fetchers in order and returns the first successful result. If all fail, throws the last exception. Tries each fetcher in list order; used for all multi-source zones (e.g. NL: ENTSO-E → Energy-Charts → EnergyZero).
- **`PriceFetcherFactory`** — `fun interface` that returns the right `PriceFetcher` for a given `PriceZone`. `defaultPriceFetcherFactory(entsoeToken, sourceOrder, statsCollector, device)` builds the fetcher chain dynamically from the user's source order preference (or zone defaults when `null`). Optionally wraps each fetcher in `InstrumentedPriceFetcher` when `statsCollector` is provided. Always wraps in `FallbackPriceFetcher`.
- **`EnergyZeroApi`** — `PriceFetcher` for the EnergyZero API (NL-only). Returns JSON, parses with kotlinx-serialization. Also exposes `fetchRaw()` and `parse()` directly for tests.
- **`SpotHintaApi`** — `PriceFetcher` for the Spot-Hinta.fi API (15 Nordic/Baltic zones). Returns JSON (top-level array), parses with kotlinx-serialization. Prices are already EUR/kWh, 15-minute resolution. Region parameter maps directly to zone IDs. Also exposes `fetchRaw()` and `parse()` directly for tests.
- **`EnergyChartsApi`** — `PriceFetcher` for the Energy-Charts API (15 European zones). Takes a zone ID and resolves it internally via `ZONE_TO_BZN` companion map. Returns JSON with parallel `unix_seconds` and `price` arrays in EUR/MWh. Converts to EUR/kWh during parsing. Auto-detects resolution (15-min or 60-min) from timestamp gaps. Also exposes `fetchRaw()` and `parse()` directly for tests.
- **`AwattarApi`** — `PriceFetcher` for the aWATTar API (AT and DE-LU). Takes a zone ID and resolves it internally via `ZONE_TO_BASE_URL` companion map. Returns JSON with hourly entries containing `start_timestamp`/`end_timestamp` (milliseconds) and `marketprice` in EUR/MWh. Converts to EUR/kWh during parsing. Also exposes `fetchRaw()` and `parse()` directly for tests.
- **`EntsoeApi`** — `PriceFetcher` for the ENTSO-E Transparency Platform (all European bidding zones). Parses XML with `XmlPullParser`, handles A03 curve type gaps, returns prices at native resolution (PT15M or PT60M), converts EUR/MWh to EUR/kWh. Also exposes `fetchRaw()` and `parse()` directly for tests.
- **`BiddingZone`** — Object with EIC code constants for 43 European bidding zones. EIC codes are a European-wide standard used across ENTSO-E, EPEX SPOT, Nord Pool, etc.

**`data/cache/`** — Caching layer:
- **`PriceCache`** — Interface for caching parsed prices, keyed by zone. `readCached(key)` / `write(key, data)` with global cooldown. Returns `CachedPriceData` (prices + source). Abstracts storage so `PriceRepository` can be tested without Android. Also contains `CachedPrice` (data class with `epochSecond`, `durationMinutes`, `price`) and `CachedPriceData` (wrapper with source name).
- **`FilePriceCache`** — `PriceCache` implementation using per-zone binary files (`cacheDir/prices_<key>.bin`). Format v3: version byte + source UTF + count int + N × (epochSecond long + durationMinutes short + price double) = 18 bytes per entry. SharedPreferences `sweetspot_cache` tracks global cooldown. Returns `null` on any format error for graceful migration (including v1/v2 caches).

**`data/stats/`** — API reliability stats collection (opt-in):
- **`StatsRecord`** — Data class representing a single API request outcome (timestamp, zone, source, device, success, errorCategory, durationMs). Companion methods `writeTo`/`readFrom` handle single-record binary I/O, `encodeToBytes`/`decodeFromBytes` handle list-level conversion for Data Layer transfer and file storage.
- **`StatsCollector`** — Interface for recording, reading, and clearing stats records. Android-free so it can be faked in pure JUnit tests.
- **`FileStatsCollector`** — Append-only binary file implementation (`cacheDir/api_stats_v2.bin`). Thread-safe via synchronized block. Records are written individually, read until EOF. Deletes incompatible v1 file (`api_stats.bin`) on init.
- **`InstrumentedPriceFetcher`** — `PriceFetcher` decorator that records the outcome of every API call. Captures successes, empty results ("EMPTY"), and failures (categorised via `categorise()`). Measures wall-clock duration via `System.nanoTime()`. Wraps individual fetchers inside the fallback chain so intermediate failures are visible.
- **`ErrorCategory`** — `categorise(exception)` function mapping exceptions to stable category strings: `HttpException` → "HTTP_503", `EntsoeException` → "ENTSOE_ERROR", `SocketTimeoutException` → "TIMEOUT", `UnknownHostException` → "DNS", etc.
- **`StatsReporter`** (in `:app`) — Reads local stats, encodes to grouped JSON (v2: includes app language, payment status, and per-request duration), POSTs to `stats.sweetspot.today/report`. Rate-limited to once per 24 hours. On success clears data; on 4xx (except 429) clears corrupted data; on 429/5xx/network error retains for next-day retry.

**`data/repository/`** — Business logic:
- **`PriceRepository`** — Created per-call with current `ZoneId` and `cacheKey`. Returns `PriceResult` (prices + source name). Computes date range (today → day-after-tomorrow), reads typed cache first (maps `CachedPrice` → `PriceSlot` with zone applied), filters to future prices using slot-aware end-time check, re-fetches if coverage is below 12 hours (with 5-minute cooldown). Threads the data source name from `FetchResult`/cache through to `PriceResult`. Takes injectable `PriceFetcher` and `Clock` for testing.
- **`SettingsRepository`** — SharedPreferences `sweetspot_settings`. Stores country code, price zone ID, timezone override, data source order (JSON list of source IDs), appliances (JSON-serialized list), stats preferences (enabled, prompt shown, first launch time), and trial/subscription state (`unlocked` boolean). Auto-detects country on first access via `CountryDetector`. Country change clears custom source order. Trial methods: `isTrialExpired()` checks if 14 days have elapsed since first launch and app is not unlocked, `trialDaysRemaining()` returns 0–14, `isUnlocked()`/`setUnlocked()` cache the subscription state locally for offline access.
- **`CountryDetector`** — Zero-permission country auto-detection for first launch. Checks SIM → network → timezone → locale → NL fallback.
- **`model/PriceZone`** — Data class representing a bidding zone (`id`, `label`, `eicCode`, `timeZoneId`). `Country` groups zones by country. `Countries` is the registry of all 30 supported countries / 43 zones, with `defaultCountry()` (NL), `findByCode()`, and `findPriceZoneById()`.
- **`model/Appliance`** — `@Serializable` data class with `id`, `name`, `durationHours`, `durationMinutes`, and `icon` (string ID referencing the icon registry).
- **`model/ApplianceIcon`** — Icon registry mapping string IDs to drawable resource IDs. Contains 30 curated icons (22 household appliances + 8 generic) using Material Symbols (Outlined, 24px) as XML vector drawables in `shared/src/main/res/drawable/`. `applianceIconFor(id)` resolves an ID to its drawable resource.
- **`util/CheapestWindowFinder`** — Pure function implementing the sliding window algorithm. Works with any slot duration (15min, 30min, 60min). Converts requested duration to "slot units" and multiplies by `slotMinutes / 60.0` for EUR costs. Supports fractional slots. Split into `findBestStartIndex`, `computeWindowCost`, and `buildBreakdown`.
- **`util/FormatUtils`** — `formatDuration()` and `shortTimeFormatter` shared by ViewModel and UI screens.
- **`util/TimeUtils`** — `formatRelative()` helper for "in Xh Ym" display.

### Phone app (`:app`)

- **`SweetSpotViewModel`** — Owns all UI state. Implements `DataClient.OnDataChangedListener` to receive watch stats. Orchestrates duration selection, price fetching via `PriceRepository`, and cheapest-window calculation via `findCheapestWindow()`. Creates `PriceFetcherFactory` dynamically from the current source order preference, optionally with `InstrumentedPriceFetcher` wrapping when stats are enabled. CRUD for appliances persisted via `SettingsRepository`. Country/zone selection with auto-detection on first launch. Pushes appliances, zone settings, source order, stats opt-in, and trial/subscription state to Wearable Data Layer after every change via `syncAppliancesToWear()` / `syncSettingsToWear()`. Stores `priceSource` in `UiState` for display in the results disclaimer. Shows one-time stats opt-in prompt after 3 days. Reports stats via `StatsReporter` after successful fetches. Receives watch stats via `/stats` Data Layer path. Errors use an `AppError` sealed interface (`Validation` for inline errors, `Network` for snackbar errors). Manages billing via `BillingRepository`: connects on init, collects unlock state, shows paywall when trial expired and not subscribed. Debug builds always skip the paywall.
- **`BillingRepository`** (interface in `data/billing/`) — Abstraction over Play Billing with `isUnlocked: StateFlow<Boolean>`, `productPrice: StateFlow<String?>`, `connect()`, `disconnect()`, `launchPurchaseFlow(activity)`, `queryPurchases()`, `onResume()`. Enables injecting a fake in tests.
- **`PlayBillingRepository`** (in `data/billing/`) — Real implementation wrapping `BillingClient` (billing-ktx 8.3.0). Product ID: `yearly_subscription` (SUBS). On connect, queries existing subscriptions to restore state and fetches product details for the price display. Uses `enableAutoServiceReconnection()` for automatic reconnection. Caches subscription state in `SettingsRepository` for offline. Acknowledges purchases to prevent auto-refund. `onResume()` re-queries purchases to detect subscription expiry.

### Wear app (`:wear`)

- **`WearViewModel`** — Reads appliances, zone settings, source order, stats opt-in, and trial/subscription state from Data Layer on init, listens for live updates. Computes `isLocked` from `is_trial_expired && !is_unlocked`. On appliance tap, creates `PriceFetcherFactory` dynamically from source order (with stats instrumentation when enabled), fetches prices via `PriceRepository` (using the phone's zone) and runs `findCheapestWindow()`. Prices are cached locally on the watch. After each fetch, syncs accumulated stats to phone via `/stats` Data Layer path (awaits delivery before clearing local stats).
- **`WearActivity`** — `SwipeDismissableNavHost` with two routes: `"appliances"` (start) and `"result"`. When `state.isLocked`, shows `WearLockedScreen` instead of the appliance list.
- **`ui/ApplianceListScreen`** — `Scaffold` with `PositionIndicator`, `TimeText`, `ScalingLazyColumn` of appliance `Chip`s (icon + name + duration), empty state, loading overlay.
- **`ui/ResultScreen`** — `ScalingLazyColumn` centered on the appliance label, with start/end times in HH:mm and relative display that auto-refreshes every 60 seconds. Scrollable for long labels on round watch faces.
- **`ui/WearLockedScreen`** — Centered text informing the user that the subscription has expired and they need to open the phone app to subscribe.
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
- **Duration picker** — two-column scroll wheel (`DurationPicker`) for hours (0–24) and minutes (0–55 in 5-min steps) with snap-to-item behaviour.
- **Find button** — disabled when duration is 0h 0m.

### Theme

`SweetSpotTheme` wraps Material 3 with dynamic colour. Bar chart colours (blue normal, green optimal) use `CompositionLocal` to stay fixed regardless of dynamic colour.

## External APIs

- **ENTSO-E Transparency Platform** (primary for all zones) — 43 European bidding zones, 15-min resolution. API docs: https://transparencyplatform.zendesk.com/hc/en-us/articles/15692855254548-Sitemap-for-Restful-API-Integration. Token stored in `local.properties` as `ENTSOE_API_TOKEN`, injected via `BuildConfig`.
- **Spot-Hinta.fi** (Nordic/Baltic fallback) — 15 zones (FI, SE1–SE4, DK1–DK2, NO1–NO5, EE, LV, LT), 15-min resolution, prices in EUR/kWh. Endpoint: `https://api.spot-hinta.fi/TodayAndDayForward?region={region}`. No auth required. Used as fallback when ENTSO-E fails for Nordic/Baltic zones.
- **EnergyZero** (NL fallback) — NL-only day-ahead prices: `https://api.energyzero.nl/v1/energyprices`. No auth required. Used as fallback when ENTSO-E fails for NL.
- **Energy-Charts** (European fallback) — 15 zones (AT, BE, CH, CZ, DE-LU, DK1, DK2, FR, HU, IT-North, NL, NO2, PL, SE4, SI), 15-min or 60-min resolution, prices in EUR/MWh. Endpoint: `https://api.energy-charts.info/price?bzn={bzn}&start={ISO}&end={ISO}`. No auth required. CC BY 4.0 licensed. Used as fallback when other sources fail.
- **aWATTar** (AT/DE-LU fallback) — 2 zones (AT, DE-LU), hourly resolution, prices in EUR/MWh. Endpoints: `https://api.awattar.at/v1/marketdata` (AT) and `https://api.awattar.de/v1/marketdata` (DE-LU). Parameters: `start`/`end` in milliseconds epoch. No auth required. Used as tertiary fallback after Energy-Charts for AT and DE-LU.

### Adding a New Data Source

1. **Create `XxxApi.kt`** in `data/api/` implementing `PriceFetcher`. Follow the three-layer pattern: `fetchPrices()` → `fetchRaw()` + `parse()` → `FetchResult("Source Name")`. Expose `fetchRaw()` and `parse()` as public for tests. Add a companion object with a zone mapping (e.g., `ZONES`, `ZONE_TO_BZN`, `ZONE_TO_BASE_URL`) — this is the single source of truth for which zones the API covers.
2. **Register in `DataSources`**: add a `DataSource` constant, add it to the `all` list, and add a `SourceEntry` to the `registry` list. List position in `registry` defines fallback priority — specialised sources (fewer zones) go before broader ones.
3. **Add a `when` branch** in `PriceFetcherFactory.kt` to instantiate the new API class.
4. **Write tests** (3 files, following existing patterns): `XxxApiParseTest` (valid parsing, edge cases), `XxxApiMalformedTest` (invalid JSON/XML handling), `XxxApiDstTest` (5 DST tests with a representative timezone: winter, summer, spring-forward, fall-back, cross-DST).
5. **Update `DataSourceTest`**: update zone count expectations in the `source zone counts match expected values` test.
6. **Update docs**: `CLAUDE.md` (External APIs, test list, test count) and `README.md` (data sources description, test count).

### Adding a New Appliance Icon

Icons use [Material Symbols](https://fonts.google.com/icons?icon.set=Material+Symbols) (Outlined style, 24px) as Android XML vector drawables.

1. **Find the icon** at [fonts.google.com/icons](https://fonts.google.com/icons?icon.set=Material+Symbols). Filter by "Material Symbols" and "Outlined" style. Note the icon's snake_case name (e.g. `dishwasher`, `heat_pump`).
2. **Download the drawable**:
   ```bash
   curl -sL "https://raw.githubusercontent.com/google/material-design-icons/master/symbols/android/<symbol_name>/materialsymbolsoutlined/<symbol_name>_24px.xml" \
     -o shared/src/main/res/drawable/ic_<appliance_id>.xml
   ```
   Where `<symbol_name>` is the Material Symbol name and `<appliance_id>` is the ID you'll use in the registry (should match the label, e.g. `dishwasher`, `heat_pump`).
3. **Register in `ApplianceIcon.kt`**: add an `ApplianceIcon("<id>", R.drawable.ic_<id>, "<Label>")` entry in the appropriate section (household or generic).
4. **Update docs**: update icon counts in `CLAUDE.md` and `README.md`.

## Key Conventions

- Prices are **EUR per kWh** (Double)
- Price data uses `PriceSlot` with a `durationMinutes` field (e.g. 60 for hourly EnergyZero, 15 for quarter-hourly ENTSO-E). The entire pipeline is resolution-aware — no hardcoded 60-minute assumptions.
- All times use configurable `ZoneId` (defaults to the selected price zone's timezone, overridable in settings)
- `ZoneId` is threaded as a parameter through ViewModel → Repository → API — not stored as a global
- **Naming:** `timeZoneId` for `java.time.ZoneId` / timezone concepts, `priceZone` / `priceZoneId` for `PriceZone` / bidding zone concepts — never bare `zoneId`
- Default country (NL) is defined in one place only: `Countries.defaultCountry()`
- Duration is stored as `durationHours: Int` + `durationMinutes: Int` (no string parsing on the main flow)
- UI text is localised via Android string resources (`strings.xml`) in 25 European languages (bg, cs, da, de, el, es, et, fi, fr, hr, hu, it, lt, lv, mk, nb, nl, pl, pt, ro, sk, sl, sr, sv + English). Montenegrin (cnr) translations exist in the source but are excluded from bundles via `localeFilters` because the Play Console rejects the `cnr` language code. Per-app language setting via AppCompat. Defaults to system locale. Strings containing numbers that affect grammar (e.g. "%d minutes") must use `<plurals>` with the correct CLDR plural categories for each language — use `getQuantityString()` / `pluralStringResource()` instead of `getString()` / `stringResource()`.
- All classes and functions have KDoc comments — always add KDoc when creating new functions or classes

## Post-Change Checklist

- After any feature, refactor, or other significant change, check if `README.md` needs updating (features list, test count, usage instructions, etc.)
- After any changes to the website (`site/`), run `make site-validate` to check for broken pages, links, and missing translations
- After adding a new app language, add it to the website too (see "Adding a Website Language" below)

## Website (sweetspot.today)

The `site/` directory contains a Hugo static site deployed to GitHub Pages at `sweetspot.today`. It deploys automatically via `.github/workflows/deploy-site.yml` on pushes to `main` that change `site/**`.

```bash
make site                         # Start local Hugo server and open in browser
hugo --source site --minify       # Build for production into site/public/
```

### Structure

```
site/
  hugo.toml                        # Hugo config: languages, base URL, params
  static/
    CNAME                          # Custom domain: sweetspot.today
    css/style.css                  # All styles (single file)
    js/main.js                     # Nav toggle, language switcher
    images/
      icon.svg                     # App icon (converted from Android vector)
      badges/                      # Official Google Play badges (25 languages)
        en.png, bg.png, cs.png, da.png, de.png, ...
  layouts/
    _default/
      baseof.html                  # Base template: <html>, <head>, nav, footer
      single.html                  # Single page layout (privacy, faq, changelog)
      list.html                    # List layout (unused but required)
    index.html                     # Landing page template
    404.html                       # Custom 404
    partials/
      head.html                    # <head> with SEO meta, OG tags, hreflang
      nav.html                     # Sticky navigation bar
      footer.html                  # Dark footer
      language-switcher.html       # Dropdown using .AllTranslations
  i18n/
    en.toml, bg.toml, cs.toml, ...   # UI strings (25 languages)
  content/
    en/                            # English content (served at root /)
    bg/, cs/, da/, de/, el/        # 24 additional languages, each under /<lang>/
    es/, et/, fi/, fr/, hr/
    hu/, it/, lt/, lv/, mk/
    nb/, nl/, pl/, pt/, ro/
    sk/, sl/, sr/, sv/
```

Each content directory contains: `_index.md` (landing page), `privacy.md`, `changelog.md`, `faq.md`.

### i18n Approach

Two layers of translation:

1. **`i18n/*.toml`** — UI strings shared across templates (nav labels, button text, section headings, feature descriptions). Used via `{{ i18n "key" }}`.
2. **`content/<lang>/*.md`** — Page-specific prose (FAQ answers, privacy policy, changelog entries). Each language gets its own content directory configured in `hugo.toml` via `contentDir`.

English is the default language served at the root (`/`, `/privacy/`, `/faq/`). Other languages are under their prefix (`/nl/`, `/de/privacy/`, etc.).

### Google Play Badges

The landing page uses official Google Play badge images from Google, stored locally in `site/static/images/badges/`. The template selects the correct badge per language via `{{ .Lang }}`:

```html
<img src="/images/badges/{{ .Lang }}.png" alt="{{ i18n "download" }}" height="40">
```

Badge images are downloaded from Google's official URL:
```
https://play.google.com/intl/en_us/badges/static/images/badges/{lang}_badge_web_generic.png
```

Where `{lang}` is the two-letter language code (en, nl, de, fr, sl, etc.).

### Adding a Website Language

When adding a new language to the website:

1. **Add language to `site/hugo.toml`**: add a `[languages.xx]` block with `weight`, `languageName`, and `contentDir = "content/xx"`.
2. **Create `site/i18n/xx.toml`**: translate all UI strings (copy `en.toml` as template, ~40 keys).
3. **Create `site/content/xx/`**: translate all 4 content pages (`_index.md`, `privacy.md`, `changelog.md`, `faq.md`). Changelog versions and dates stay the same, only descriptions are translated.
4. **Download Google Play badge**: `curl -sL "https://play.google.com/intl/en_us/badges/static/images/badges/xx_badge_web_generic.png" -o site/static/images/badges/xx.png`. The badge includes localized "Get it on Google Play" text from Google.
5. **Verify**: run `make site` and check the new language appears in the language switcher and all pages render correctly.

### Design

- **Colours**: primary blue `#4A90D9`, green `#27AE60`, purple `#9B59B6`, yellow `#F1C40F`
- **Light palette**: bg `#F8F9FF`, surface `#FFFFFF`, text `#191C20`, muted `#44474E`
- **Footer**: dark bg `#111318`, text `#E1E2E9`
- **Layout**: max-width 1100px, CSS Grid, responsive at 768px and 1024px
- **Typography**: system font stack, line-height 1.6

### Deployment

GitHub Actions (`.github/workflows/deploy-site.yml`) triggers on pushes to `main` that change `site/**`. Builds with Hugo extended + `--minify`, deploys to GitHub Pages. Custom domain via `CNAME` file + DNS A records pointing to GitHub Pages IPs (185.199.108–111.153).

### Updating the Changelog

When releasing a new app version, add a new entry at the **top** of `site/content/<lang>/changelog.md` for each language:

```html
<div class="changelog-entry">

## <span class="version-badge">vX.Y</span> <span class="version-date">DD. month YYYY</span>

- Change description
- Another change

</div>
```

Date format varies by language (e.g., "March 28, 2026" in English, "28. marec 2026" in Slovenian).

## Commit Messages

[Conventional Commits](https://www.conventionalcommits.org/): `<type>: <description>` describing the **what** and **why**.

Types: `feat`, `fix`, `refactor`, `style`, `docs`, `chore`.

If `git commit` fails with `user.signingKey needs to be set for ssh signing`, stop and inform the user — they need to refresh the GPG key manually. Do not bypass signing.
