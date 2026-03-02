# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Is

SweetSpot is an Android app that finds the cheapest contiguous time window for running an appliance, based on dynamic electricity prices from the EnergyZero API. It's a port of a PHP web app. Includes a Wear OS companion app for Pixel Watch and other Wear OS 3+ devices.

## Build & Run

```bash
./gradlew assembleDebug          # Build debug APKs (phone + wear)
./gradlew app:installDebug       # Install phone app on connected device/emulator
./gradlew wear:installDebug      # Install wear app on connected watch
./gradlew assembleRelease        # Build signed release APKs
```

A `Makefile` wraps common tasks: `make build`, `make test`, `make install-phone`, `make install-wear`, `make release`, `make clean`.

### Installing the Wear OS app

The watch app must be installed separately via ADB (auto-install only works via Play Store):

1. Enable Developer Options on the watch (Settings > System > About > tap Build Number 7 times)
2. Enable Wi-Fi debugging (Settings > Developer options > Debug over Wi-Fi)
3. Connect: `adb connect <ip>:<port>`
4. Install: `adb -s <watch-serial> install wear/build/outputs/apk/release/wear-release.apk`

Use `adb devices` to list connected devices when both phone and watch are connected.

## Releasing

```bash
./release.sh 1.1 -n notes.md            # Bump version, build, tag, push, create GitHub Release
./release.sh 1.1 -n notes.md --draft     # Same but creates a draft release
```

The `-n` flag points to a Markdown file with release notes. The script appends a "Full Changelog" link automatically. Always write meaningful, user-facing release notes describing what changed and why.

The script auto-increments `versionCode`, sets `versionName`, builds signed phone and wear APKs, commits, tags, pushes, and creates a GitHub Release with both APKs attached.

Release signing is configured via `local.properties` (gitignored):
```
RELEASE_STORE_FILE=release.jks
RELEASE_STORE_PASSWORD=...
RELEASE_KEY_ALIAS=sweetspot
RELEASE_KEY_PASSWORD=...
```

## Testing

```bash
./gradlew test                   # Run all unit tests (116 tests)
./gradlew testDebugUnitTest      # Run debug variant only
```

Tests live in `shared/src/test/`, `app/src/test/`, and `wear/src/test/`:
- `data/PriceRepositoryTest` — cache logic, coverage re-fetch, cooldown, filtering (10 tests, in shared)
- `data/EnergyZeroApiParseTest` — JSON parsing and timezone conversion (5 tests, in shared)
- `data/EnergyZeroApiMalformedTest` — malformed/invalid JSON handling (8 tests, in shared)
- `data/EnergyZeroApiDstTest` — DST transition parsing: winter, summer, spring-forward, fall-back (5 tests, in shared)
- `util/CheapestWindowFinderTest` — sliding window algorithm + breakdown invariants + zero-duration edge case (22 tests, in shared)
- `util/TimeUtilsTest` — relative time formatting (10 tests, in shared)
- `util/FormatUtilsTest` — duration formatting (8 tests, in shared)
- `model/ApplianceIconTest` — icon resolution and unknown-ID fallback (3 tests, in shared)
- `SweetSpotViewModelTest` — ViewModel state, duration, appliance CRUD, timezone, async fetch, rapid-tap cancellation (30 tests, Robolectric, in app)
- `WearViewModelTest` — Wear ViewModel state, appliance tap, async fetch, rapid-tap cancellation, JSON parsing (15 tests, Robolectric, in wear)

## Stack

- Kotlin 2.3, AGP 9, Gradle 9.2 with version catalog (`gradle/libs.versions.toml`)
- minSdk 26 (phone) / 30 (wear), targetSdk 35, compileSdk 36
- Jetpack Compose with Material 3 (dynamic color on SDK 31+)
- Wear Compose with Material for the watch app
- MVVM: `SweetSpotViewModel` (phone) and `WearViewModel` (watch) with `StateFlow`
- OkHttp 5 for HTTP, kotlinx-serialization for JSON
- Wearable Data Layer API for phone-to-watch appliance sync
- Material Icons Extended for appliance icon picker
- JUnit 4 + Robolectric for unit tests (116 tests)
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

**Appliance sync:** Phone pushes appliance JSON to `/appliances` path via `PutDataMapRequest` after every CRUD operation. Watch reads on init and listens for live updates via `DataClient.OnDataChangedListener`.

### Shared module (`:shared`)

- **`data/PriceFetcher`** — Interface for fetching and parsing prices. Decouples `PriceRepository` from a specific API provider.
- **`data/EnergyZeroApi`** — `PriceFetcher` singleton. Fetches hourly prices from `api.energyzero.nl` for today+tomorrow. Takes `ZoneId` to compute date boundaries.
- **`data/PriceCache`** — Interface for caching raw API JSON. Abstracts storage so `PriceRepository` can be tested without Android.
- **`data/FilePriceCache`** — `PriceCache` implementation backed by `cacheDir/prices_cache.json` and SharedPreferences `sweetspot_cache`. Tracks last fetch timestamp for cooldown.
- **`data/PriceRepository`** — Created per-call with current `ZoneId`. Reads cache first, filters to future prices, re-fetches if coverage is below 12 hours (with 5-minute cooldown). Takes injectable `PriceFetcher` and `Clock` for testing.
- **`data/SettingsRepository`** — SharedPreferences `sweetspot_settings`. Stores timezone and appliances (JSON-serialized list).
- **`model/Appliance`** — `@Serializable` data class with `id`, `name`, `durationHours`, `durationMinutes`, and `icon` (string ID referencing the icon registry).
- **`model/ApplianceIcon`** — Icon registry mapping string IDs to Material `ImageVector`s. Contains 26 curated icons (18 household appliances + 8 generic). `applianceIconFor(id)` resolves an ID to its icon.
- **`util/CheapestWindowFinder`** — Pure function implementing the sliding window algorithm. Supports fractional hours (e.g. 2h30m = 2.5h with a partial last slot). Split into `findBestStartIndex`, `computeWindowCost`, and `buildBreakdown`.
- **`util/FormatUtils`** — `formatDuration()` and `shortTimeFormatter` shared by ViewModel and UI screens.
- **`util/TimeUtils`** — `formatRelative()` helper for "in Xh Ym" display.

### Phone app (`:app`)

- **`SweetSpotViewModel`** — Owns all UI state. Orchestrates duration selection, price fetching via `PriceRepository`, and cheapest-window calculation via `findCheapestWindow()`. CRUD for appliances persisted via `SettingsRepository`. Pushes appliances to Wearable Data Layer after every CRUD operation via `syncAppliancesToWear()`. Errors use an `AppError` sealed interface (`Validation` for inline errors, `Network` for snackbar errors).

### Wear app (`:wear`)

- **`WearViewModel`** — Reads appliances from Data Layer on init, listens for live updates. On appliance tap, fetches prices via `PriceRepository` and runs `findCheapestWindow()`. Prices are cached locally on the watch.
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

## Key Conventions

- Prices are **EUR per kWh** (Double)
- All times use configurable `ZoneId` (defaults to phone's system timezone, overridable in settings)
- `ZoneId` is threaded as a parameter through ViewModel → Repository → API — not stored as a global
- Duration is stored as `durationHours: Int` + `durationMinutes: Int` (no string parsing on the main flow)
- UI text is hardcoded in Composables (no string resources / i18n)
- All classes and functions have KDoc comments — always add KDoc when creating new functions or classes

## Commit Messages

[Conventional Commits](https://www.conventionalcommits.org/): `<type>: <description>` describing the **what** and **why**.

Types: `feat`, `fix`, `refactor`, `style`, `docs`, `chore`.
