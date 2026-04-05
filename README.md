# SweetSpot for Android

[![Test](https://github.com/jmerhar/sweetspot-android/actions/workflows/test.yml/badge.svg)](https://github.com/jmerhar/sweetspot-android/actions/workflows/test.yml)

Find the cheapest time to run your appliance, based on dynamic electricity prices across 30 European countries. Website: [sweetspot.today](https://sweetspot.today)

SweetSpot fetches day-ahead electricity prices from the [ENTSO-E Transparency Platform](https://transparency.entsoe.eu/) (43 bidding zones, 15-minute resolution) with [Spot-Hinta.fi](https://spot-hinta.fi/) for 15 Nordic/Baltic zones, [Energy-Charts](https://api.energy-charts.info/) for 15 European zones, [EnergyZero](https://api.energyzero.nl) as a fallback for the Netherlands, and [aWATTar](https://www.awattar.com/) for Austria and Germany, then finds the cheapest contiguous time window for a user-specified duration using a sliding window algorithm.

## Requirements

- **Phone app:** Android 8.0+ (API 26)
- **Wear OS app:** Wear OS 3+ (API 30), e.g. Pixel Watch 2
- Internet connection

## Usage

Pick your country and bidding zone in Settings (auto-detected on first launch). Then tap a quick-duration button (1h–6h) or use the scroll wheel picker to select hours and minutes. The picker supports hours from 0–24 and minutes in 5-minute intervals.

You can also create **appliance buttons** (e.g. "Washing machine — 2h 30m") in Settings, each with a configurable icon. Tapping an appliance button fills the duration and searches immediately.

SweetSpot returns:

- **Cheapest window** — optimal start and end time with countdown
- **Cost breakdown** — per-hour price, fraction used, and cost
- **24h bar chart** — all prices with the optimal window highlighted in green; sub-hourly slots are grouped by hour with individual bars stacked within each row

All costs shown are per 1 kW load. Prices do not include energy tax and supplier fee.

### Wear OS companion app

The watch app shows your appliances as tappable chips. Tap one to see the cheapest start and end times at a glance — no phone needed at runtime.

Appliances are synced automatically from the phone via the Wearable Data Layer API. Configure them once on your phone and they appear on the watch.

## Features

- **30 European countries** — 43 bidding zones via ENTSO-E (15-minute resolution), with Spot-Hinta.fi for Nordic/Baltic zones, Energy-Charts for 15 European zones, EnergyZero as a fallback for the Netherlands, and aWATTar for Austria and Germany
- **Country auto-detection** — detects your country on first launch from SIM, network, or timezone (no permissions required)
- **Duration scroll picker** — two-column wheel for hours and minutes with snap behaviour
- **Quick-duration buttons** — 1h–6h chips for common durations
- **Configurable appliances** — save your appliances with name, duration, and icon; persisted across app restarts
- **Data source preferences** — reorder, enable, or disable price data sources per zone in Settings
- **25 languages** — per-app language setting with localised UI in Bulgarian, Croatian, Czech, Danish, Dutch, English, Estonian, Finnish, French, German, Greek, Hungarian, Italian, Latvian, Lithuanian, Macedonian, Norwegian, Polish, Portuguese, Romanian, Serbian, Slovak, Slovenian, Spanish, and Swedish
- **Dedicated results screen** — shows the cheapest window with back navigation to the form
- **Wear OS companion** — tap an appliance on your watch to see cheapest start/end times
- **Automatic appliance sync** — appliances and zone settings sync from phone to watch via Wearable Data Layer
- Material 3 with dynamic colour theming and dark mode
- Configurable timezone (defaults to the selected zone's timezone)
- Offline-capable with smart price caching (both phone and watch)
- Optional anonymous API reliability stats (opt-in via Settings > Advanced)
- **14-day free trial** with a yearly subscription to keep using the app

## Building

```bash
make build                        # Build debug APKs (phone + watch)
make build-release                # Build signed release APKs
make bundle                       # Build signed release AABs for Play Store
make debug                        # Build and install debug app on phone + watch
make debug-phone                  # Build and install debug app on connected phone
make debug-watch                  # Build and install debug app on connected watch
make install                      # Install release APKs on phone + watch
make install-phone                # Install release APK on connected phone
make install-watch                # Install release APK on connected watch
make test                         # Run all unit tests
make clean                        # Remove all build outputs
```

### Installing the Wear OS app

The watch app must be installed separately (auto-install only works via Play Store).

1. Enable Developer Options on the watch (Settings > System > About > tap Build Number 7 times)
2. Enable Wi-Fi debugging (Settings > Developer options > Debug over Wi-Fi)
3. Connect: `adb connect <ip>:<port>`
4. Install: `make install-watch`

## Releasing

```bash
make release VERSION=3.0            # Bump version, build, tag, push, create GitHub Release
make release VERSION=3.0 DRAFT=1    # Same but creates a draft release
```

The release script auto-increments `versionCode`, sets `versionName`, builds signed phone and wear APKs and AABs, commits the version bump, creates a git tag, pushes, and creates a GitHub Release with APKs attached. AABs are built locally for Play Store upload but not published to GitHub.

## Testing

```bash
make test
```

304 unit tests cover the sliding window algorithm (including 15-minute slot support), duration and time formatting, locale-aware price formatting, API parsing (JSON and XML), fallback fetcher chain, icon resolution, API stats instrumentation, trial/subscription logic, and ViewModel state management (via Robolectric).

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE).
