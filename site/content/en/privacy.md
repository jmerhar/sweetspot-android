---
title: "Privacy Policy"
description: "SweetSpot privacy policy — no data collected, no accounts, no analytics."
---

## Overview

SweetSpot is designed with privacy in mind. The app does not collect, store, or transmit any personal data. There are no user accounts, no analytics, and no tracking of any kind.

## Data Processing

SweetSpot fetches day-ahead electricity prices from public APIs:

- **ENTSO-E Transparency Platform** — the primary source for all 43 European bidding zones
- **Spot-Hinta.fi** — fallback for Nordic and Baltic zones
- **EnergyZero** — fallback for the Netherlands
- **Energy-Charts** — fallback for 15 European zones
- **aWATTar** — fallback for Austria and Germany

These API requests contain only the bidding zone identifier and date range. No personal information is included.

## Local Storage

Price data is cached locally on your device to reduce API calls and enable faster results. Your appliance configuration (names, durations, icons) and settings (country, zone, language) are also stored locally on your device.

On Wear OS, appliance data and settings are synced between phone and watch using the Wearable Data Layer API. This communication stays on your local devices and does not pass through any external server.

## No Analytics

SweetSpot does not include any analytics SDKs, crash reporting, or usage tracking. The app makes no network requests beyond fetching electricity prices from the public APIs listed above.

## Open Source

SweetSpot is open source and licensed under GPL v3. You can review the complete source code on [GitHub](https://github.com/jmerhar/sweetspot-android).

## Contact

If you have questions about this privacy policy, you can open an issue on [GitHub](https://github.com/jmerhar/sweetspot-android/issues).

*Last updated: March 2026*
