# SweetSpot for Android

Find the cheapest time to run your appliance, based on dynamic electricity prices.

SweetSpot fetches hourly electricity prices from the [EnergyZero API](https://api.energyzero.nl) for the next 24 hours and finds the cheapest contiguous time window for a user-specified duration using a sliding window algorithm.

This is the native Android port of the [SweetSpot](https://github.com/jmerhar/sweetspot) web app.

## Requirements

- Android 8.0+ (API 26)
- Internet connection

## Usage

Enter an appliance run duration in the search field. Supported formats:

| Input | Duration |
|-------|----------|
| `4h` | 4 hours |
| `90m` | 1.5 hours |
| `2h 30m` | 2.5 hours |
| `2.5` | 2.5 hours |

SweetSpot returns:

- **Cheapest window** — optimal start and end time with countdown
- **Cost breakdown** — per-hour price, fraction used, and cost
- **24h bar chart** — all prices with the optimal window highlighted in green

All costs shown are per 1 kW load. Prices do not include energy tax and supplier fee.

## Features

- Material 3 with dynamic color theming and dark mode
- Configurable timezone (defaults to phone's system timezone)
- Offline-capable with daily price caching

## Building

```
./gradlew assembleDebug
./gradlew installDebug
```
