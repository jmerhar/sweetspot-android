---
title: "Privacy Policy"
description: "SweetSpot privacy policy — privacy-first, no accounts, no analytics."
---

## Overview

SweetSpot is designed with privacy in mind. The app does not collect or store any personal data. There are no user accounts, no analytics, and no usage tracking. An optional feature lets you share anonymous API statistics — see details below.

## Data Processing

SweetSpot fetches day-ahead electricity prices from public APIs:

- **ENTSO-E Transparency Platform** — the primary source for all 43 European bidding zones
- **Spot-Hinta.fi** — fallback for Nordic and Baltic zones
- **Energy-Charts** — fallback for 15 European zones
- **EnergyZero** — fallback for the Netherlands
- **aWATTar** — fallback for Austria and Germany

These API requests contain only the bidding zone identifier and date range. No personal information is included.

## Local Storage

Price data is cached locally on your device to reduce API calls and enable faster results. Your appliance configuration (names, durations, icons) and settings (country, zone, language) are also stored locally on your device.

On Wear OS, appliance data and settings are synced between phone and watch using the Wearable Data Layer API. This communication stays on your local devices and does not pass through any external server.

## No Analytics

SweetSpot does not include any analytics SDKs, crash reporting, or usage tracking. The app makes no network requests beyond fetching electricity prices from the public APIs listed above (and optional stats reporting if enabled).

## Optional API Statistics

You can opt in to sharing anonymous API reliability statistics. When enabled, the app periodically sends individual request records for each data source and bidding zone to our server. This data contains:

- Timestamp of the API request
- Bidding zone identifier (e.g. "NL", "DE-LU")
- Data source name (e.g. "ENTSO-E", "EnergyZero")
- Device type (phone or watch)
- Whether the request succeeded or failed
- Error category on failure (e.g. "timeout", "server error")
- App version number
- App language (e.g. "en", "nl")
- Payment status (trial, subscribed, or expired)
- Request duration in milliseconds

This data does **not** contain device identifiers, location, price data, or any other personal information. It is used solely to improve data source reliability and default ordering.

This feature is disabled by default. You can enable or disable it at any time in Settings.

## Open Source

SweetSpot is open source and licensed under GPL v3. You can review the complete source code on [GitHub](https://github.com/jmerhar/sweetspot-android).

## Contact

If you have questions about this privacy policy, you can open an issue on [GitHub](https://github.com/jmerhar/sweetspot-android/issues).

*Last updated: April 2026*
