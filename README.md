# SweetSpot for Android

[![Test](https://github.com/jmerhar/sweetspot-android/actions/workflows/test.yml/badge.svg)](https://github.com/jmerhar/sweetspot-android/actions/workflows/test.yml)
[![codecov](https://codecov.io/gh/jmerhar/sweetspot-android/graph/badge.svg)](https://app.codecov.io/gh/jmerhar/sweetspot-android)

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
- **24h bar chart** — all prices with the cheapest window kept at full colour while the other bars fade toward the background; sub-hourly slots are grouped by hour with individual bars stacked within each row. Press and hold a bar (and slide) for a tooltip with that slot's time range and price — down to the market's resolution (15-minute where available, otherwise hourly), with the full component breakdown when all-in is on, otherwise the spot price

Costs are shown per 1 kW load by default; set an appliance's power rating (or charge an EV) and the cost reflects the actual load. Prices do not include energy tax and supplier fee.

### Wear OS companion app

The watch app shows your appliances as tappable chips. Tap one to see the cheapest start and end times at a glance — no phone needed at runtime.

Appliances are synced automatically from the phone via the Wearable Data Layer API. Configure them once on your phone and they appear on the watch.

## Features

- **30 European countries** — 43 bidding zones via ENTSO-E (15-minute resolution), with Spot-Hinta.fi for Nordic/Baltic zones, Energy-Charts for 15 European zones, EnergyZero as a fallback for the Netherlands, and aWATTar for Austria and Germany
- **Country auto-detection** — detects your country on first launch from SIM, network, or timezone (no permissions required)
- **Duration scroll picker** — two-column wheel for hours and minutes with snap behaviour
- **Quick-duration buttons** — 1h–6h chips for common durations
- **Configurable appliances** — save your appliances with name, duration, icon, and an optional power rating (kW) so the cost reflects the real load; persisted across app restarts
- **Sorting & reordering** — order appliances by a custom drag-to-reorder arrangement, or automatically by most used, recently used, name, duration, or type (with collision-gated tie-breakers); usage counts combine phone and watch taps. Choose where vehicles sit among the appliance buttons (mixed in, first, last, or their own section)
- **Group by type** — optionally cluster the home-screen buttons by appliance type (their icon), each under its own heading — as stacked rows or side-by-side columns — so programmes for the same machine (e.g. dishwasher Eco/Quick/Auto) stay together. Vehicles still honour their separate-section setting, shown as a block above or below the grid
- **EV charging** — add your car as a special appliance (picked from a bundled database of EVs/PHEVs, or entered manually); tapping it asks for current and target state of charge and computes the charging time from the battery size and the lower of the car's AC limit and your home charger
- **"Ready by" deadline** — optionally set a time any search should be done by; the recommended window is the cheapest one that finishes in time, and you can still tap "Cheaper" to browse cheaper windows that finish later (they're clearly flagged as past your deadline)
- **All-in price (Netherlands)** — optionally show the approximate full consumer price (spot + energy tax + your supplier's surcharge + VAT) instead of the bare market price; pick your supplier (or enter a custom surcharge) in Settings. Display-only — it never changes which window is cheapest, but it gives a realistic run cost and an honest "you're being paid" signal only when the all-in price is truly below zero. When it's on, the upcoming-prices chart splits each bar into its components (energy tax · surcharge · spot, all VAT-inclusive, with a legend) so you can see how little of the bill varies with time, and the results screen has a quick switch to flip between the total and bare-spot views
- **Share your setup** — copy your appliances, their order, and your EV settings to another household device by scanning a QR code or opening a share link. Works offline via a verified App Link — no account, no server; the receiver reviews an import preview and chooses to add, replace, or pick what to import
- **Data source preferences** — reorder, enable, or disable price data sources per zone in Settings
- **25 languages** — per-app language setting with localised UI in Bulgarian, Croatian, Czech, Danish, Dutch, English, Estonian, Finnish, French, German, Greek, Hungarian, Italian, Latvian, Lithuanian, Macedonian, Norwegian, Polish, Portuguese, Romanian, Serbian, Slovak, Slovenian, Spanish, and Swedish
- **First-launch intro & contextual tips** — a short, skippable set of screens explaining what the app does (re-openable any time from Settings › Help & support › How it works), plus one-time hints that point at less obvious controls the first time you reach them (the Earlier/Cheaper buttons, press-and-hold chart, all-in toggle, and EV chip)
- **Help & support** — an in-app support hub in Settings: a quick guide, links to the FAQ, privacy policy, changelog, and Play Store rating, and a form to report a problem or send feedback without leaving the app or needing a GitHub account. Reports become public GitHub issues you can track under "My reports" — with an in-app conversation thread where you can reply and see the maintainer's responses. You can optionally be notified by email (with one-click unsubscribe in every message). Failed submissions and replies are saved to an outbox and retried automatically — nothing you write is lost
- **Dedicated results screen** — shows the recommended window (headed "Recommended time") with back navigation to the form
- **Earlier / Cheaper navigation** — step to a sooner window when the cheapest time is inconvenient, and see exactly how much more it costs than the cheapest; step back toward the cheapest at any time
- **Wear OS companion** — tap an appliance on your watch to see cheapest start/end times
- **Automatic appliance sync** — appliances and zone settings sync from phone to watch via Wearable Data Layer
- Material 3 with dynamic colour theming and dark mode
- Configurable timezone (defaults to the selected zone's timezone)
- Offline-capable with smart price caching (both phone and watch)
- Optional anonymous API reliability stats (opt-in from Settings)
- **14-day free trial** with a yearly subscription to keep using the app

## Development workflow

A typical loop for working on the app:

1. **Set up once.** Install a JDK (17+) and the Android SDK — Android Studio is the easiest way to get both. Add your free [ENTSO-E API token](https://transparency.entsoe.eu/) to `local.properties` so prices can be fetched:
   ```
   ENTSOE_API_TOKEN=your-token
   ```
   Signed release builds also need the release-signing keys in `local.properties` (see [Releasing](#releasing)), and the Fastlane tasks (screenshots, publish, deploy) need Ruby 3.3 via rbenv (pinned in `.ruby-version`).

2. **Code.** The project is three Gradle modules — `:shared` (data/model/util), `:app` (phone), `:wear` (watch). Keep logic in `:shared`, in the ViewModels, or in pure helpers where it is unit-tested; keep Composables, Activities, and SDK wrappers thin (they are excluded from coverage). See [CLAUDE.md](CLAUDE.md) for the full architecture and conventions.

3. **Test as you go.**
   ```bash
   make test                              # all modules (Robolectric + JUnit)
   ./gradlew :shared:testDebugUnitTest    # a single module — faster inner loop
   ```
   CI gates per-module line coverage, so add or update tests alongside your change.

4. **Run it on a device or emulator.**
   ```bash
   make debug-phone     # build + install the debug app on a connected phone/emulator
   make debug-watch     # same for a Wear OS device (see "Installing the Wear OS app")
   ```

5. **Before committing.** Run `make test`; if you touched the website, run `make site-validate`; update `README.md` / `CLAUDE.md` if behaviour changed. Use [Conventional Commits](https://www.conventionalcommits.org/) (`feat:`, `fix:`, `refactor:`, `docs:`, `chore:`). Pushing to `main` runs the test + coverage workflow.

6. **Release** (see [Releasing](#releasing)): write user-facing notes in `docs/notes/release.md` and add a changelog entry for every language under `site/content/<lang>/changelog.md`, then:
   ```bash
   make release VERSION=x.y             # bump version, build, tag, push, GitHub Release
   make deploy TRACK=alpha APP=phone    # upload the AAB + localized release notes to Play Store
   ```

7. **Refresh store assets** when the UI changes:
   ```bash
   make screenshots && make frames      # capture localized screenshots and frame them
   make publish                         # upload metadata, screenshots, and images
   ```
   Changes under `fastlane/metadata/android/**` also auto-publish via CI on push to `main`, and changes under `site/**` redeploy [sweetspot.today](https://sweetspot.today).

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
make screenshots                  # Capture localized screenshots via Screengrab
make frames                       # Frame screenshots with marketing text
make feature-graphic              # Generate localised Play Store feature graphics
make publish                       # Upload metadata and images to Play Store
make deploy                        # Deploy AABs to Play Store (APP=phone|wear|both)
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
make release VERSION=X.Y            # Bump version, build, tag, push, create GitHub Release
make release VERSION=X.Y DRAFT=1    # Same but creates a draft release
```

The release script auto-increments `versionCode`, sets `versionName`, builds signed phone and wear APKs and AABs, commits the version bump, creates a git tag, pushes, and creates a GitHub Release with APKs attached. AABs are built locally for Play Store upload but not published to GitHub.

## Testing

```bash
make test
```

The unit-test suite covers the sliding window algorithm (including 15-minute slot support, earlier-window alternatives, and the optional "ready by" deadline), duration and time formatting, locale-aware price formatting, API parsing (JSON and XML), fallback fetcher chain, the all-in price transform and tariff feed (fetch/cache/staleness), icon resolution, appliance sorting/reordering and cross-device usage, household setup sharing (encode/decode/merge), the EV vehicle database (parsing/search), API stats instrumentation, trial/subscription logic, the in-app Help & support flow (report encoding, response parsing, submit retry policy, diagnostics, public GitHub issue/thread parsing, and the report/outbox/reply-outbox stores), and ViewModel state management including EV charging, all-in pricing, and setup import (via Robolectric).

Code coverage is reported per module via [Kover](https://github.com/Kotlin/kotlinx-kover) and uploaded to [Codecov](https://codecov.io/gh/jmerhar/sweetspot-android) (one flag per module) — CI runs `./gradlew testDebugUnitTest koverHtmlReportDebug koverXmlReportDebug`. All three modules are high once Compose UI and thin SDK wrappers are excluded (`:shared` ~99.6%, `:app` ~99%, `:wear` ~95% line), and CI gates each module's line coverage (`:shared` ≥98, `:app` ≥97, `:wear` ≥93) via the shared coverage tooling, configured in `coverage.toml`. The badge above shows the combined Codecov total across all three. The full per-module Kover HTML report is also published for every commit to the shared coverage site: **[jmerhar.github.io/coverage/sweetspot-android](https://jmerhar.github.io/coverage/sweetspot-android/)**.

## Data attribution

The bundled EV database is built from two open datasets, merged into a normalised schema by
`bin/data/build-ev-db.py` (run `make ev-db` to refresh):

- [Open EV Data](https://github.com/KilowattApp/open-ev-data) (MIT) — broad coverage of EVs/PHEVs back to 2010.
- [open-ev-data-dataset](https://github.com/open-ev-data/open-ev-data-dataset) (CDLA-Permissive-2.0) — recent models, preferred on overlap.

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE).
