# SweetSpot for Android

Find the cheapest time to run your appliance, based on dynamic electricity prices.

SweetSpot fetches hourly electricity prices from the [EnergyZero API](https://api.energyzero.nl) for the next 24 hours and finds the cheapest contiguous time window for a user-specified duration using a sliding window algorithm.

This is the native Android port of the [SweetSpot PHP web app](https://github.com/jmerhar/sweetspot-php).

## Requirements

- Android 8.0+ (API 26)
- Internet connection

## Usage

Tap a quick-duration button (1h–6h) or use the scroll wheel picker to select hours and minutes. The picker supports hours from 0–24 and minutes in 5-minute intervals.

You can also create **appliance buttons** (e.g. "Washing machine — 2h 30m") in Settings, each with a configurable icon. Tapping an appliance button fills the duration and searches immediately.

SweetSpot returns:

- **Cheapest window** — optimal start and end time with countdown
- **Cost breakdown** — per-hour price, fraction used, and cost
- **24h bar chart** — all prices with the optimal window highlighted in green

All costs shown are per 1 kW load. Prices do not include energy tax and supplier fee.

## Features

- **Duration scroll picker** — two-column wheel for hours and minutes with snap behavior
- **Quick-duration buttons** — 1h–6h chips for common durations
- **Configurable appliances** — save your appliances with name, duration, and icon; persisted across app restarts
- **Dedicated results screen** — shows the cheapest window with back navigation to the form
- Material 3 with dynamic color theming and dark mode
- Configurable timezone (defaults to phone's system timezone)
- Offline-capable with daily price caching

## Building

```bash
./gradlew assembleDebug           # Build debug APK
./gradlew installDebug            # Install on connected device/emulator
./gradlew assembleRelease         # Build signed release APK
```

## Releasing

```bash
./release.sh 1.1                  # Bump version, build, tag, push, create GitHub Release
./release.sh 1.1 --draft          # Same but creates a draft release
```

The release script auto-increments `versionCode`, sets `versionName`, builds a signed APK, commits the version bump, creates a git tag, pushes, and creates a GitHub Release with the APK attached. Install the APK on your phone to upgrade.

## Testing

```bash
./gradlew test                    # Run all unit tests
./gradlew testDebugUnitTest       # Run debug variant only
```

Unit tests cover the sliding window algorithm, duration and time formatting, API JSON parsing, and ViewModel state management (via Robolectric).
