# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Is

SweetSpot is an Android app that finds the cheapest contiguous time window for running an appliance, based on dynamic electricity prices from the EnergyZero API. It's a port of a PHP web app.

## Build & Run

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew installDebug           # Build and install on connected device/emulator
```

No test framework is configured yet.

## Stack

- Kotlin 2.1, minSdk 26, targetSdk/compileSdk 35
- Jetpack Compose with Material 3 (dynamic color on SDK 31+)
- MVVM: single `SweetSpotViewModel` with `StateFlow<UiState>`
- OkHttp for HTTP, kotlinx-serialization for JSON
- No frameworks, no DI, no database — SharedPreferences + file cache only

## Architecture

All source lives under `app/src/main/java/si/merhar/sweetspot/`.

**Data flow:** User input → `DurationParser` → `PriceRepository` (cache or API) → `findCheapestWindow()` sliding window → `UiState` update → Compose UI reacts.

### Key layers

- **`data/EnergyZeroApi`** — Singleton. Fetches hourly prices from `api.energyzero.nl` for today+tomorrow. Takes `ZoneId` to compute date boundaries.
- **`data/PriceCache`** — Stores raw JSON in `cacheDir/prices_cache.json` (cleared by "Clear Cache"). Freshness date in SharedPreferences `sweetspot_cache`.
- **`data/PriceRepository`** — Created per-call with current `ZoneId`. Checks cache freshness, filters to next 24h.
- **`data/SettingsRepository`** — SharedPreferences `sweetspot_settings`. Currently stores timezone only, designed to be extended.
- **`SweetSpotViewModel`** — Owns all state. `findCheapestWindow()` uses a sliding window supporting fractional hours (e.g. 2h30m = 2.5h with a partial last slot).

### Navigation

State-based: `UiState.showSettings` boolean toggles between `SweetSpotScreen` and `SettingsScreen` in `MainActivity`. No navigation library.

### Theme

`SweetSpotTheme` wraps Material 3 with dynamic color. Bar chart colors (blue normal, green optimal) use `CompositionLocal` to stay fixed regardless of dynamic color.

## Key Conventions

- Prices are **cents per kWh** (Double); displayed as EUR (÷100)
- All times use configurable `ZoneId` (defaults to phone's system timezone, overridable in settings)
- `ZoneId` is threaded as a parameter through ViewModel → Repository → API — not stored as a global
- UI text is hardcoded in Composables (no string resources / i18n)

## Commit Messages

[Conventional Commits](https://www.conventionalcommits.org/): `<type>: <description>` describing the **what** and **why**.

Types: `feat`, `fix`, `refactor`, `style`, `docs`, `chore`.
