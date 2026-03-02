# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Is

SweetSpot is an Android app that finds the cheapest contiguous time window for running an appliance, based on dynamic electricity prices from the EnergyZero API. It's a port of a PHP web app.

## Build & Run

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew installDebug           # Build and install on connected device/emulator
./gradlew assembleRelease        # Build signed release APK
```

## Releasing

```bash
./release.sh 1.1                 # Bump version, build, tag, push, create GitHub Release
./release.sh 1.1 --draft         # Same but creates a draft release
```

The script auto-increments `versionCode`, sets `versionName`, builds a signed release APK, commits, tags, pushes, and creates a GitHub Release with the APK attached.

Release signing is configured via `local.properties` (gitignored):
```
RELEASE_STORE_FILE=release.jks
RELEASE_STORE_PASSWORD=...
RELEASE_KEY_ALIAS=sweetspot
RELEASE_KEY_PASSWORD=...
```

## Testing

```bash
./gradlew test                   # Run all unit tests
./gradlew testDebugUnitTest      # Run debug variant only
```

Tests live in `app/src/test/java/si/merhar/sweetspot/` and cover:
- `util/CheapestWindowFinderTest` — sliding window algorithm (12 tests)
- `util/TimeUtilsTest` — relative time formatting (7 tests)
- `util/FormatUtilsTest` — duration formatting (8 tests)
- `data/EnergyZeroApiParseTest` — JSON parsing and timezone conversion (5 tests)
- `SweetSpotViewModelTest` — ViewModel state, duration, appliance CRUD, timezone (22 tests, Robolectric)

## Stack

- Kotlin 2.1, minSdk 26, targetSdk/compileSdk 35
- Jetpack Compose with Material 3 (dynamic color on SDK 31+)
- MVVM: single `SweetSpotViewModel` with `StateFlow<UiState>`
- OkHttp for HTTP, kotlinx-serialization for JSON
- Material Icons Extended for appliance icon picker
- JUnit 4 + Robolectric for unit tests
- No frameworks, no DI, no database — SharedPreferences + file cache only

## Architecture

All source lives under `app/src/main/java/si/merhar/sweetspot/`.

**Data flow:** Duration picker (hours + minutes) → `PriceRepository` (cache or API) → `findCheapestWindow()` sliding window → `UiState` update → Compose UI reacts.

### Key layers

- **`data/EnergyZeroApi`** — Singleton. Fetches hourly prices from `api.energyzero.nl` for today+tomorrow. Takes `ZoneId` to compute date boundaries.
- **`data/PriceCache`** — Stores raw JSON in `cacheDir/prices_cache.json`. Freshness date in SharedPreferences `sweetspot_cache`.
- **`data/PriceRepository`** — Created per-call with current `ZoneId`. Checks cache freshness, filters to next 24h.
- **`data/SettingsRepository`** — SharedPreferences `sweetspot_settings`. Stores timezone and appliances (JSON-serialized list).
- **`model/Appliance`** — `@Serializable` data class with `id`, `name`, `durationHours`, `durationMinutes`, and `icon` (string ID referencing the icon registry).
- **`model/ApplianceIcon`** — Icon registry mapping string IDs to Material `ImageVector`s. Contains 26 curated icons (18 household appliances + 8 generic). `applianceIconFor(id)` resolves an ID to its icon.
- **`util/CheapestWindowFinder`** — Pure function implementing the sliding window algorithm. Supports fractional hours (e.g. 2h30m = 2.5h with a partial last slot). Split into `findBestStartIndex`, `computeWindowCost`, and `buildBreakdown`.
- **`util/FormatUtils`** — `formatDuration()` helper shared by ViewModel and UI screens.
- **`util/TimeUtils`** — `formatRelative()` helper for "in Xh Ym" display.
- **`SweetSpotViewModel`** — Owns all UI state. Orchestrates duration selection, price fetching via `PriceRepository`, and cheapest-window calculation via `findCheapestWindow()`. CRUD for appliances persisted via `SettingsRepository`.

### Navigation

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
