# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Is

SweetSpot is an Android app that finds the cheapest contiguous time window for running an appliance, based on dynamic electricity prices. Supports 30 European countries (43 bidding zones) via the ENTSO-E Transparency Platform, with Spot-Hinta.fi as a fallback for 15 Nordic/Baltic zones, Energy-Charts as a fallback for 15 European zones, EnergyZero as a fallback for the Netherlands, and aWATTar as a fallback for Austria and Germany. Appliances can be electric vehicles: tapping a vehicle prompts for current/target state of charge and computes the charging time, and any search can be bounded by an optional "ready by" deadline. A household can copy one member's appliances, sort order, and EV settings to another device by scanning a QR code or opening a share link (offline, via a verified App Link — no account, no server). Includes a Wear OS companion app for Pixel Watch and other Wear OS 3+ devices.

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
make site-screenshots             # Generate per-language website screenshots (WebP) from framed images
make ev-db                        # Rebuild the bundled EV vehicle database from open data sources
make suppliers                    # Rebuild all-in tariff feeds (site/static/data/suppliers/*.json) — needs ENEVER_TOKEN
make screenshots                  # Capture localized screenshots via Screengrab (LOCALE=xx-XX for one)
make frames                       # Frame screenshots with marketing text (LOCALE=xx-XX for one)
make feature-graphic              # Generate localised Play Store feature graphics (LOCALE=xx-XX for one)
make publish                      # Upload metadata, screenshots, and images to Play Store
make deploy                       # Deploy AABs with release notes to Play Store (TRACK=alpha|production APP=phone|wear|both)
make clean                        # Remove all build outputs
```

A `Makefile` wraps common tasks. Helper scripts live in `bin/`:
- **`bin/device/install.sh`** — Finds a connected phone or watch via ADB and installs the latest release APK. Called by `make install-phone` and `make install-watch`.
- **`bin/data/build-ev-db.py`** — Builds the bundled EV vehicle database (`app/src/main/assets/ev-vehicles.json`) by merging two open datasets via per-source adapters into a normalised schema. Cars only; deduped by brand + model + variant + year (so distinct trims are kept), source #2 winning on an exact collision (newer data). Called by `make ev-db`. Both the script and the generated asset are committed. Its pure logic (normalisation, the per-source adapters, and the dedup key) is unit-tested in `bin/data/test_build_ev_db.py` (`make test-ev-db`).
- **`bin/data/build-suppliers.py`** — Builds the per-country **all-in tariff feeds** (`site/static/data/suppliers/<cc>.json`, served at `https://sweetspot.today/data/suppliers/<cc>.json`) for the all-in-pricing feature. Country-agnostic (a `COUNTRIES` registry drives it; NL only for now). For NL it fetches **Frank Energie** GraphQL (no auth — authoritative VAT + energy tax, and Frank's own surcharge) and **enever.nl** (`ENEVER_TOKEN`, from env or `local.properties` — ~25 NL suppliers' surcharge recovered by differencing their VAT-inclusive all-in against enever's exchange price + Frank's tax block; supplier columns are discovered from the feed, and their ids/names come from enever's live "Legenda" **persisted to a committed registry** `site/static/data/enever-suppliers.json` which is the offline fallback — no supplier codes/names are hardcoded in the script). **No baked-in numbers**: every value carries a `source`; if a country's essentials (currency, per-kWh energy tax, VAT multiplier) can't be sourced the build for that country fails — **no file is written** (last-good kept) — and the script exits non-zero. Per-supplier surcharges are best-effort (missing → omitted + `warnings`). Skips rewriting when only the `generated` timestamp would change (no daily-churn commits). Called by `make suppliers` and the `build-suppliers` workflow. Its pure logic (legend parsing, tax derivation, registry merge, surcharge differencing, normalisation) is unit-tested in `bin/data/test_build_suppliers.py` (`make test-suppliers`; also run by the workflow). See `docs/notes/ideas/all-in-pricing-nl-pilot.md` for the schema + formula.
- **`bin/site/install-hugo.sh`** — Downloads and installs the latest Hugo extended binary from GitHub. Used by CI workflows (`deploy-site`, `site-validate`).
- **`bin/deploy/release.sh`** — Bumps version, builds, tags, pushes, and creates a GitHub Release.
- **`bin/quality/inspect.sh`** — Summarises inspection XML files exported from Android Studio. Does **not** run inspections itself. Called by `make inspect`.
- **`bin/quality/coverage-report.py`** — Reads each module's Kover XML (`<module>/build/reports/kover/reportDebug.xml`) and prints a per-module coverage table as Markdown (`--format md`, for the Actions run summary) or the JSON `reports` manifest (`--format reports`, consumed by the `jmerhar/coverage` site's `make-meta.py`; led by a combined **`total`** entry — no `path` — so the coverage site's project index shows the overall number as the headline rather than `:shared`'s); with `--gate` it instead **enforces the per-module line-coverage gate** (thresholds in `GATES`) and exits non-zero on a regression — this is the CI gate (chosen over `koverVerifyDebug`, which mis-applies wildcard excludes). Used by `test.yml`. Its pure logic (Kover XML parsing, percentage and gate maths) is unit-tested in `bin/quality/test_coverage_report.py` (`make test-coverage-report`).
- **`bin/quality/collect-coverage.sh`** — Assembles the three modules' Kover `htmlDebug` reports plus a `reports.json` manifest (via `bin/quality/coverage-report.py --format reports`) into `coverage-upload/` (or `$1`), ready for `jmerhar/coverage`'s `bin/add-report.sh` to publish. Used by `test.yml`.
- **`bin/site/site-validate.sh`** — Validates the Hugo site: builds, checks expected pages/assets exist, verifies internal links resolve, checks page sizes, and ensures i18n key parity across languages. Called by `make site-validate`.
- **`bin/playstore/frame-screenshots.sh`** — Frames raw Screengrab screenshots with marketing text and coloured backgrounds. Produces 6 shots per locale (result, home, prices, and three settings screens: the **Appliances** sub-screen, the **EV charging** sub-screen, and the **language** picker). Shots 3–6 are framed as single upright phones. Outputs to `fastlane/metadata/android/<locale>/images/phoneScreenshots/` and generates `build/screenshots.html` gallery. Requires ImageMagick 7. Called by `make frames`.
- **`bin/site/site-screenshots.sh`** — Generates the per-language website screenshots: converts the framed `fastlane/metadata` PNGs to WebP (563×1000) into `site/static/images/screenshots/<lang>/{1..6}.webp` (gitignored, generated on demand). Run by `make site` / `make site-validate` and by the `deploy-site` CI before the Hugo build, so the landing page shows screenshots in the visitor's language. Requires `cwebp` (or ImageMagick).
- **`bin/playstore/feature-graphic.sh`** — Generates localised Play Store feature graphics (1024x500) with gradient, app icon, and translated tagline. Outputs to `fastlane/metadata/android/<locale>/images/featureGraphic.png` and generates `build/feature-graphics.html` gallery. Requires ImageMagick 7 and Python 3. Called by `make feature-graphic`.
- **`bin/deploy/deploy.sh`** — Deploys phone and/or wear AABs with localised release notes to the Play Store. Supports `APP=phone|wear|both` (default: both) for selective deployment. Wear OS is always skipped on the `alpha` track (closed testing not supported for Wear); use `TRACK=production` to deploy wear. Reads version codes from Gradle, extracts the latest changelog entry from each website translation, writes Fastlane changelog files, and runs the `deploy` Fastlane lane. Called by `make deploy`.
- **`bin/deploy/deploy-feedback.sh`** — Deploys the **feedback Worker** (`server/feedback-worker/`) to Cloudflare via `wrangler deploy`, then health-checks `https://feedback.sweetspot.today/`. Forwards extra args to wrangler (e.g. `--dry-run`). Assumes a one-time `wrangler login` + secrets set (see the worker's README). Called by `make deploy-feedback`. The Worker's crypto-free logic (`readSubscription` token/email parsing — incl. the no-email-report token-preservation guard — plus `notificationFor` webhook bot-skip, `buildIssueBody`, and the small helpers) is unit-tested with **vitest** in `server/feedback-worker/test/` (`make test-feedback`, or `npm test` in that dir). Functions that need the Workers runtime (`timingSafeEqualStr`, `verifySignature`, and the `fetch` handler's GitHub/Brevo calls) aren't covered by this Node harness — a `@cloudflare/vitest-pool-workers` setup would be needed for those.

Shared bash helpers live in **`bin/lib/`** — `log.sh` (consistent `log_info`/`log_success`/`log_warn`/`log_error`/`die`), `locale.sh` (language ↔ Play-locale maps), `gallery.sh`, and `require.sh`, aggregated by `common.sh`; all scripts route status/warnings/errors through `log.sh`. **`bin/README.md`** indexes the scripts by concern. The three `bin/` Python suites (`test_build_suppliers.py`, `test_build_ev_db.py`, `test_coverage_report.py`) run together via **`make test-scripts`**, which `test.yml` runs on every push/PR before the Kotlin tests.

Fastlane is used for automated screenshot capture and Play Store metadata upload. Requires Ruby 3.3 (managed via `.ruby-version` and rbenv). Lanes are defined in `fastlane/Fastfile`:
- **`screenshots`** — Builds debug APKs and runs Screengrab across all locales (or one with `locale:xx`).
- **`publish`** — Uploads metadata, screenshots, and images to the Play Store via `upload_to_play_store`. Dynamically resolves the latest version code on the alpha track. Runs automatically in CI via `.github/workflows/publish-listing.yml` when metadata changes are pushed to `main`. Requires `PLAY_STORE_SERVICE_ACCOUNT_JSON` GitHub secret.
- **`deploy`** — Uploads phone and/or wear AABs with localised release notes to a Play Store track. Takes `track`, `phone_code`, `wear_code`, `skip_phone`, and `skip_wear` parameters. Phone AAB goes to the specified track, Wear AAB goes to the `wear:` prefixed track (e.g. `wear:production`). Called by `bin/deploy/deploy.sh`.

### Installing the Wear OS app

The watch app must be installed separately via ADB (auto-install only works via Play Store):

1. Enable Developer Options on the watch (Settings > System > About > tap Build Number 7 times)
2. Enable Wi-Fi debugging (Settings > Developer options > Debug over Wi-Fi)
3. Connect: `adb connect <ip>:<port>`
4. Install: `make install-watch` (or manually: `adb -s <watch-serial> install wear/build/outputs/apk/release/sweetspot-wear-release.apk`)

Use `adb devices` to list connected devices when both phone and watch are connected.

## Releasing

```bash
make release VERSION=X.Y            # Bump version, build, tag, push, create GitHub Release
make release VERSION=X.Y DRAFT=1    # Same but creates a draft release
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
- **`ideas/`** — Feature ideas (mix of done and pending): website, low price alerts, all-in pricing, widget, API reliability stats
- **`ideas/done/`** — Implemented features: localisation, cache management, data source preferences, car charging, appliance power rating, ViewModel locale bug, test coverage CI
- **`reference/`** — Research and reference: multi-zone API comparison, Play Store publishing guide, country & language coverage audit, Help & support system (`help-support-system.md` — the shipped in-app support + Worker + email architecture)

`docs/entsoe/` contains ENTSO-E API documentation and sample XML responses.

## Testing

```bash
./gradlew test                   # Run all unit tests
./gradlew testDebugUnitTest      # Run debug variant only
```

### Coverage

Code coverage uses **Kover** (`org.jetbrains.kotlinx.kover`), applied per module (`:shared`, `:app`, `:wear`) so each gets its own report — not the aggregated/merging variant. Use the `Debug` variant tasks (unit tests run on debug); an unqualified task name runs in all three modules:

```bash
./gradlew testDebugUnitTest koverHtmlReportDebug koverXmlReportDebug   # all modules, separate reports
./gradlew :shared:koverHtmlReportDebug                                # one module
```

Per module: HTML → `<module>/build/reports/kover/htmlDebug/index.html`, XML → `<module>/build/reports/kover/reportDebug.xml`. CI (`test.yml`) uploads each module's XML to **Codecov** under its own flag (`shared`/`app`/`wear`) — browsable at [codecov.io/gh/jmerhar/sweetspot-android](https://codecov.io/gh/jmerhar/sweetspot-android) — and also renders an at-a-glance per-module table on the run's summary page. Requires the `CODECOV_TOKEN` repo secret; `codecov.yml` sets the flags, marks status informational (no gate), and disables PR comments. Coverage (each module's own tests): `:shared` ~99% line, `:app` ~99% line, `:wear` ~95% — the numbers reflect testable logic because each module's Kover config **excludes presentation/framework code** (see the rule below): `:shared` excludes kotlinx-serialization `@Serializable` types + `BuildConfig`; `:app`/`:wear` also exclude `@Composable` functions, `*ComposableSingletons*`, Activities (`MainActivity`, `today.sweetspot.ui.*`), and thin SDK/IO wrappers (`PlayBillingRepository`, `WearableSync`, `WearableStatsBridge`, `WearableUsageBridge`, `HttpStatsPoster`, `HttpReportSubmitter`). CI also uploads JUnit results to **Codecov Test Analytics** (`codecov-action@v5` with `report_type: test_results`; tests run with `--continue` so all modules report) for flaky-test detection and failure/slowest-test history. **CI gates every module's line coverage** via `python3 bin/quality/coverage-report.py --gate` (thresholds in the script's `GATES`): `:shared` ≥98, `:app` ≥97, `:wear` ≥93 — each a couple of points under its actual number, tight enough to catch a real regression while tolerating defensive/edge lines. The gate reads the **Kover XML reports** (which apply the `excludes` filters correctly) rather than `koverVerifyDebug`, because Kover 0.9.8's verification task does **not** reliably apply wildcard `classes(...)` excludes such as `today.sweetspot.ui.*` — behaviour that also varies by JDK. (For the same reason, `:app`'s excludes list each `ui` sub-package explicitly instead of relying on one `ui.*` to cross package dots.) The README badge shows the **combined Codecov total** across all three flags (not just `:shared`). On green pushes to `main`, CI also publishes the per-module Kover HTML to the shared, source-agnostic **`jmerhar/coverage`** GitHub Pages site, **one report per commit, kept forever**: `bin/quality/collect-coverage.sh` assembles the reports + `reports.json`, then that repo's `bin/add-report.sh` (run from a checkout) drops them under `reports/sweetspot-android/<sha>/` and pushes; the coverage repo's own workflow builds the browsable indexes (project list, commit list, cross-links) and deploys. The project index's per-commit headline shows the combined **`total`** (from the manifest's first entry); the per-commit page lists `total` plus each module. Browse at `jmerhar.github.io/coverage/sweetspot-android/`; each commit also gets a `coverage/report` commit status linking to its report. Needs the `COVERAGE_PAGES_TOKEN` secret (fine-grained PAT, Contents:write on `jmerhar/coverage`); the publish steps are skipped, not failed, when the secret is absent. See `docs/notes/ideas/done/test-coverage-ci.md` and the `jmerhar/coverage` README.

### Presentation vs. logic — keep logic testable

Coverage excludes presentation/framework code that JVM unit tests can't exercise. **This makes it dangerously easy to hide logic from coverage by putting it in an excluded class.** To prevent that:

- **Logic goes in testable classes** — ViewModels (`SweetSpotViewModel`, `WearViewModel`), repositories, `:shared` (`data`/`model`/`util`), or dedicated helpers. All of these are covered and expected to stay high.
- **Excluded classes must contain no logic** — only presentation or thin framework glue. What's excluded (per module `kover { reports { filters { excludes } } }`):
  - `@Composable` functions (`annotatedBy("androidx.compose.runtime.Composable")`) and `*ComposableSingletons*` — all Compose UI.
  - Android entry points — `MainActivity`, `WearActivity`.
  - Thin wrappers over untestable SDKs / IO — `PlayBillingRepository` (Play Billing), `WearableSync`, `WearableStatsBridge`, and `WearableUsageBridge` (Wearable Data Layer), `HttpStatsPoster` and `HttpReportSubmitter` (HTTP). Each SDK/IO boundary is isolated behind an interface (`BillingRepository`, `WearSync`, `WatchStatsBridge`, `WatchUsageBridge`, `StatsPoster`, `ReportSubmitter`) with a **fake used in ViewModel/reporter tests**, so the decision logic stays in the covered class.
  - Generated/data-only: `@Serializable` types, `BuildConfig`.
- **Extraction examples from this codebase** — `formatKw`/`formatHhMm` moved from a Compose file into `:shared` `FormatUtils` (+ tests); the paywall rule pulled out as the pure `shouldShowPaywall(...)`; the stats response-code policy pulled out as the pure `reportOutcomeFor(code)`; the phone's inbound Data Layer glue moved behind `WatchStatsBridge` leaving `onWatchStatsReceived` testable.
- **When you catch yourself writing logic in a Composable/Activity/SDK-wrapper, extract it** — a computation, formatting, branching, or state derivation belongs in a ViewModel or a pure function (ideally in `:shared`), where it is tested. A composable should only lay out state the ViewModel already produced; an SDK wrapper should only translate calls, not decide anything.
- Rule of thumb: if a change adds an `if`/`when`, a calculation, or parsing to an excluded class, it's in the wrong place — move it to a covered class and test it.

Tests live in `shared/src/test/`, `app/src/test/`, and `wear/src/test/` — each `*Test` class documents its own scope in its KDoc/name. Run `./gradlew test` (or a per-module task) to see the current suite; logic belongs in the covered classes above, not the excluded presentation/framework code.

## Inspections

Inspections are run manually in Android Studio and exported as XML — **not** run from the CLI.

**Workflow:**
1. The user runs "Code → Inspect Code" in Android Studio (whole project, default profile)
2. The user exports results to `inspect/xml/` (one XML file per inspection category)
3. Claude reads the XML files, identifies new issues, and fixes them
4. The user re-runs the inspection in Android Studio and re-exports
5. Claude verifies the issues are resolved

**Important:** `make inspect` / `bin/quality/inspect.sh` only **summarises** the existing XML files — it does not run inspections. Do not attempt to run inspections from the CLI. The XML files in `inspect/xml/` are gitignored local artifacts.

## Stack

- GitHub Actions CI (`.github/workflows/test.yml`) runs tests on push and PRs
- GitHub Actions CI (`.github/workflows/publish-listing.yml`) auto-publishes Play Store listing metadata on pushes to `main` that change `fastlane/metadata/android/**`
- GitHub Actions CI (`.github/workflows/build-suppliers.yml`) is a **scheduled cron** (daily) + `workflow_dispatch` that runs the `bin/data/test_build_suppliers.py` unit tests, then `bin/data/build-suppliers.py`, and commits any change under `site/static/data/` (tariff feeds + the enever registry), pushing with the `SITE_COMMIT_TOKEN` PAT (a `GITHUB_TOKEN` push wouldn't trigger `deploy-site`) so the updated all-in tariff feed reaches `sweetspot.today`. Requires the `ENEVER_TOKEN` and `SITE_COMMIT_TOKEN` repo secrets; a failed build (essentials unsourceable) fails the run and commits nothing.
- No frameworks, no DI, no database — SharedPreferences + file cache only (plus one bundled read-only JSON asset, `ev-vehicles.json`, for the EV database)
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

**Usage sync:** Watch pushes its cumulative per-appliance tap snapshot to the `/usage` path after each fetch (idempotent — the whole map, not deltas). Phone stores it separately from its own tap map and combines them (`count` summed, `lastUsed` maxed) to feed the Frequency/Recency sort keys. A purge on the phone bumps a `usage_reset_token` carried on `/settings`; the watch zeroes its store on a newer token and stamps pushes with the token it honoured, and the phone ignores stale-token snapshots.

### Shared module (`:shared`)

The data layer is organised into subpackages under `data/` (`api`, `cache`, `stats`, `usage`, `share`, `repository`):

**`data/api/`** — API implementations and fetcher infrastructure:
- **`DataSource` / `DataSources`** — Registry of all supported price data sources (ENTSO-E, EnergyZero, Spot-Hinta.fi, Energy-Charts, aWATTar). `DataSources.defaultsForZone(zoneId)` returns available sources in default priority order per zone using a declarative registry — list order defines fallback priority, each entry declares which zones it covers.
- **`PriceFetcher`** — Interface with a single `fetchPrices(from, to, timeZoneId)` method returning `FetchResult` (prices + source name). `FetchResult` pairs a `List<PriceSlot>` with the data source name (e.g. "ENTSO-E", "EnergyZero"). Decouples `PriceRepository` from a specific API provider. Also defines `sharedHttpClient`, a single `OkHttpClient` (10s connect + 10s read timeout) shared by all API implementations.
- **`HttpException`** — Typed exception for non-200 HTTP responses. Carries the HTTP `code` for reliable error categorisation.
- **`EntsoeException`** — Typed exception for ENTSO-E Acknowledgement_MarketDocument errors (HTTP 200 but error body). Carries the `reason` text. Categorised as `ENTSOE_ERROR` in stats.
- **`FallbackPriceFetcher`** — `PriceFetcher` that tries a list of fetchers in order and returns the first result that **actually has prices**: a successful-but-empty response (HTTP 200 with no day-ahead data) falls through to the next source. If no fetcher yields prices it returns an empty result (treated as "not enough data") when at least one responded; the last exception is thrown only when every fetcher failed outright. Used for all multi-source zones (e.g. NL: ENTSO-E → Energy-Charts → EnergyZero).
- **`PriceFetcherFactory`** — `fun interface` that returns the right `PriceFetcher` for a given `PriceZone`. `defaultPriceFetcherFactory(entsoeToken, sourceOrder, statsCollector, device, disabledSources)` builds the fetcher chain dynamically from the user's source order preference (or zone defaults when `null`), removing any `disabledSources` in **both** the custom-order and default-order cases (with a backstop so a zone is never left with no source). Optionally wraps each fetcher in `InstrumentedPriceFetcher` when `statsCollector` is provided. Always wraps in `FallbackPriceFetcher`.
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
- **`StatsReporter`** (in `:app`) — Reads local stats, encodes to grouped JSON (v2: includes app language, payment status, and per-request duration), POSTs to `stats.sweetspot.today/report` via an injectable **`StatsPoster`** (production impl `HttpStatsPoster`, excluded from coverage; a fake drives the tests). Rate-limited to once per 24 hours. The response-code policy is the pure `reportOutcomeFor(code)`: on success clears data and stamps the timestamp; on 4xx (except 429) clears corrupted data; on 429/5xx/network error retains for next-day retry.

**`data/share/`** — Household setup sharing (offline, serverless):
- **`SetupShare`** — Pure codec for the sharable [`SharedSetup`] (`model/SharedSetup`): `encode` (JSON → gzip → URL-safe Base64) and `toLink` (`https://sweetspot.today/import#<payload>`, the payload in the fragment so nothing reaches the server), `decode`/`fromLink` returning a `DecodeResult` (`Success` / `TooNew` for a higher `schemaVersion` / `Malformed`), and a pure `mergeAppliances(existing, incoming, replace, newId)` that re-mints every imported appliance id and dedupes by content (name + duration + EV specs + power + icon). Android-free, unit-tested. Mirrors `data/usage/UsageSnapshot`. `SharedSetup` carries the appliances, the `ApplianceSort`, and the EV device settings (home-charger kW, default target SoC, `EvPosition.key`, separate-section) plus the home-screen `ApplianceGrouping` key — never usage. The QR itself is rendered by the coverage-excluded `ui/share/QrCode.kt` (ZXing `core`, pure-Java, no camera).

**`data/repository/`** — Business logic:
- **`PriceRepository`** — Created per-call with current `ZoneId` and `cacheKey`. Returns `PriceResult` (prices + source name + `fromCache` flag). Computes date range (today → day-after-tomorrow), reads typed cache first (maps `CachedPrice` → `PriceSlot` with zone applied), filters to future prices using slot-aware end-time check, re-fetches if coverage is below 12 hours (with 5-minute cooldown). Threads the data source name from `FetchResult`/cache through to `PriceResult`. `fromCache` is true when the result was served entirely from cache (the phone uses it to piggyback a tariff refresh only on real network fetches). Takes injectable `PriceFetcher` and `Clock` for testing.
- **`TariffRepository`** — Provides a country's all-in tariff feed for the display-only all-in price. `cached(cc)` parses the cached feed (returns it only when `usable`, no network); `refresh(cc)` fetches + caches a fresh feed, keeping the last-good copy on any error/404/`usable:false`. Holds no scheduling — the ViewModel decides when to refresh (bootstrap when uncached, on country change, or piggybacked on the price fetch). Staleness (~14 d) is judged by the ViewModel from `CachedTariff.fetchedAtMs` to show a warning; a cached copy of any age is still usable. Backed by `TariffApi` (fetch `https://sweetspot.today/data/suppliers/<cc>.json`) + `FileTariffCache` (raw JSON + timestamp in `sweetspot_cache`); both injectable for tests.
- **`SettingsRepository`** — SharedPreferences `sweetspot_settings`. Stores country code, price zone ID, timezone override, data source order (JSON list of source IDs), appliances (JSON-serialized list — vehicles are appliances with an `EvSpec`), EV charging settings (home charger kW, default target SoC, last current SoC), all-in preferences (enabled, chosen supplier id, manual surcharge override), stats preferences (enabled, prompt shown, first launch time), and trial/subscription state (`unlocked` boolean). Auto-detects country on first access via `CountryDetector`. Country change clears custom source order and the chosen supplier. Trial methods: `isTrialExpired()` checks if 14 days have elapsed since first launch and app is not unlocked, `trialDaysRemaining()` returns 0–14, `isUnlocked()`/`setUnlocked()` cache the subscription state locally for offline access.
- **`EvVehicleRepository`** — Read-only access to the bundled EV database. Android-free (takes the raw `ev-vehicles.json` string, so it is unit-testable). Parses the normalised array eagerly and exposes `vehicles`, `brands()`, `models(brand)`, and a free-text `search(query)`. The ViewModel loads it once in the background and exposes `searchEvVehicles()` for the "add vehicle" picker in Settings.
- **`CountryDetector`** — Zero-permission country auto-detection for first launch. Checks SIM → network → timezone → locale → NL fallback.
- **`model/PriceZone`** — Data class representing a bidding zone (`id`, `label`, `eicCode`, `timeZoneId`). `Country` groups zones by country. `Countries` is the registry of all 30 supported countries / 43 zones, with `defaultCountry()` (NL), `findByCode()`, and `findPriceZoneById()`.
- **`model/Appliance`** — `@Serializable` data class with `id`, `name`, `durationHours`, `durationMinutes`, `icon` (nullable string ID referencing the icon registry — ordinary appliances set one, vehicles leave it null and render a car decided at display time), and an optional `ev: EvSpec?`. When `ev` is non-null the appliance is an electric vehicle: tapping it on the home screen prompts for a state-of-charge range instead of searching a fixed duration.
- **`model/ApplianceSort`** — `@Serializable` ordering spec: `SortKey` (`CUSTOM`/`FREQUENCY`/`RECENCY`/`NAME`/`DURATION`/`TYPE`), `SortCriterion` (key + `descending`), `ApplianceSort` (primary + tie-breakers; `isCustom`). Also `ApplianceUsage` (all-time `count` + `lastUsedMs`), the `EvPosition` enum (`INTERLEAVED`/`FIRST`/`LAST`, persisted by `key`, home-screen placement), and the `ApplianceGrouping` enum (`NONE`/`ROWS`/`COLUMNS`, persisted by `key`) that clusters home-screen chips by appliance type as stacked rows or side-by-side columns.
- **`model/EvVehicle`** — `@Serializable` normalised EV record (`brand`, `model`, `variant`, `year`, `batteryKwh`, `acMaxPowerKw`) from the bundled database, used only to populate the "add vehicle" picker. `EvSpec` (`batteryKwh`, `acMaxPowerKw`) is the subset stored on a vehicle `Appliance`.
- **`model/ApplianceIcon`** — Icon registry mapping string IDs to drawable resource IDs. Contains 30 curated icons (22 household appliances + 8 generic) using Material Symbols (Outlined, 24px) as XML vector drawables in `shared/src/main/res/drawable/`. `applianceIconFor(id)` resolves an ID to its drawable resource; `applianceIconFor(appliance)` additionally renders electric vehicles as a car (`ic_car`, a drawable resolved at display time and not part of the pickable registry), so a vehicle's `icon` is null.
- **`model/SupplierTariffs`** — `@Serializable` model of the all-in tariff feed: `schemaVersion`, `country`, `currency`, `generated`, `usable`, `errors`/`warnings`, a generic `taxes: List<TaxComponent>` (each `type` = `perKwh` additive or `percentage` multiplier), and `suppliers: List<SupplierTariff>` (per-supplier `surchargePerKwh` ex-VAT + optional `fixedMonthlyFee`). Country-agnostic so new markets need no code change.
- **`util/AllInPricing`** — Pure, display-only transform: `marginal(spot, taxes, surcharge) = (spot + Σ perKwh + surcharge) × Π(1 + percentage)`; `applyAllIn(prices, taxes, surcharge)` maps every `PriceSlot.price` through it (affine + monotonic → the cheapest window is unchanged). Feeding all-in slots into `CheapestWindowFinder`/`PriceBarChart`/`ResultSummary` makes the whole results screen reflect all-in, and the chart's existing negative-colour branch becomes the "getting paid" state. `components(taxes, surcharge)` returns the VAT-inclusive fixed part (`AllInComponents`: `energyTax` + `surcharge`, each × the percentage multiplier) so `fixedTotal + spot × Π(1 + percentage) == marginal(...)`; the ViewModel puts it on `UiState.allInComponents` for the stacked chart.
- **`util/AllInBarSegments`** — Pure chart geometry for the all-in "fixed baseline + spot deviation" bars: `segmentsFor(total, components, xMax, xMin = 0.0)` returns ordered `Segment`s (`Role` = `TAX`/`SURCHARGE`/`SPOT`/`SPOT_NEGATIVE`) positioned as fractions of the value range `[xMin, xMax]`. The fixed block is drawn identically on every bar (so its edges form the shared baseline); a positive spot extends right, a negative spot draws a `SPOT_NEGATIVE` band back over the fixed block's tail — extending left of the zero reference (`xMin < 0`) when the all-in total is itself below zero. Keeps the layout maths out of the `@Composable`; the colour mapping lives in the (covered) `charting/BarColors`, so `PriceBarChart` just offsets the boxes.
- **`util/ChartGeometry`** — Pure hit-test for the chart's **press-and-hold tooltip**: `selectedCell(y, totalHeightPx, present)` maps a vertical pointer position to the sub-slot cell it falls on (uniform grid of `rowCount × slotsPerHour` cells), snapping an empty padding cell (partial first/last hour) to the nearest present one. `PriceBarChart` wires it via `detectDragGesturesAfterLongPress` (long-press so a plain drag still scrolls) and shows a `ChartTooltip` with the pressed slot's time range (at the market's native resolution — 15-min or hourly) and price — the VAT-inclusive breakdown (`AllInPricing.breakdown(total, components)`: spot/energy tax/surcharge) when all-in is on, else just the spot price — while its hour row gets a stronger tint. The tooltip renders in a `Popup` (so the results-screen scroll can't clip it) positioned by `ChartGeometry.tooltipTopY(fingerY, gapPx, tooltipHeight, windowHeight)` — a few hour-rows above the finger, flipping below when near the top of the window, clamped to the window — so it tracks the finger and stays on-screen however the chart is scrolled.
- **`util/CoachMarkPolicy`** — Pure decision for which contextual hint is due on a screen: `resultsDue(seen, hasAlternatives, allInConfigured, hasChart)` (priority Earlier/Cheaper → chart → all-in toggle, each skipped when inapplicable — Earlier/Cheaper needs >1 window, chart needs prices, all-in needs configuring — or already seen) and `homeDue(seen, hasEvChip)`. Returns at most one, enforcing "one hint per screen appearance".
- **`util/CoachMarkGeometry`** — Pure placement for the anchored coach-mark bubble: `calloutFor(target, bubbleW, bubbleH, windowW, windowH, gapPx, tailInset)` → `CalloutPlacement` (bubble x/y, above/below flip, tail centre). Prefers above the target, flips below when it won't fit, clamps to the window, and keeps the tail within the bubble. The two-axis, tailed sibling of `ChartGeometry.tooltipTopY`.
- **`util/CheapestWindowFinder`** — Pure functions implementing the sliding window algorithm. Works with any slot duration (15min, 30min, 60min). Converts requested duration to "slot units" and multiplies by `slotMinutes / 60.0` for EUR costs. Supports fractional slots. Split into `findBestStartIndex`, `computeWindowCost`, `buildBreakdown`, and `buildWindowAt`. `findCheapestWindow` returns the single cheapest window; `findWindowAlternatives` returns the "earlier path" — the cheapest window first, then each successively-cheapest window that starts earlier, down to the earliest (start clamped to now). Cost increases monotonically along the list, so the results screen can step "earlier" (costlier, sooner) and "cheaper" (back toward the cheapest) by moving an offset. The finder itself imposes no deadline: the phone's "ready by" is applied as a **soft default** at the ViewModel level (`deadlineDefaultOffset` picks the cheapest window meeting it as the initial offset — see the ViewModel section below), so "Cheaper" can still browse cheaper windows that finish after it.
- **`util/ApplianceSorting`** — Pure appliance ordering: `sortAppliances(list, sort, usage)` (stable comparator chain, EV Duration = +∞, insertion order as final tie-break, custom = stored order), `hasCollisions`/`nextAssignableKeys` (drive collision-gated tie-breaker disclosure), `combineUsage` (phone + watch), `mergeForHome(...) : HomeChipLayout` (`Flat`/`Sectioned`/`Grouped` — folds vehicles in by `EvPosition` + separate-section, EVs name-ordered; when `ApplianceGrouping` is `ROWS`/`COLUMNS` it clusters chips by type into titled `HomeGroup`s rendered as stacked bands or side-by-side columns), and the sort-control edit helpers (`withPrimary`/`withLevelKey`/`withToggledDirection`/`withAddedTiebreaker`/`withoutLevel`). Consumed by `SweetSpotViewModel` and the Settings sort control.
- **`data/usage/`** — `UsageStore` interface + `FileUsageStore` (watch-local cumulative tap store with a reset token, JSON file in cacheDir) and `UsageSnapshot` (byte codec for the `/usage` Data Layer transfer). The phone keeps its own usage in `SettingsRepository` instead.
- **`util/FormatUtils`** — `formatDuration()` and `shortTimeFormatter` shared by ViewModel and UI screens.
- **`util/TimeUtils`** — `formatRelative()` helper for "in Xh Ym" display.
- **`util/EvCharging`** — Pure EV charging maths (`effectivePowerKw`, `chargeMinutes`) shared by `SweetSpotViewModel` and the SoC dialog, so the previewed estimate and the searched duration are computed identically.
- **`util/SweetSpotJson`** — The module's single lenient `sweetSpotJson` (`Json { ignoreUnknownKeys = true }`), used by all kotlinx-serialization parsing (api/repository/usage/support/share) so the decode config lives in one place.

### Phone app (`:app`)

- **`SweetSpotViewModel`** — Owns all UI state. Receives watch stats via an injected **`WatchStatsBridge`** (production impl `WearableStatsBridge`), which decodes `/stats` and calls the testable `onWatchStatsReceived()`. Orchestrates duration selection, price fetching via `PriceRepository`, and cheapest-window calculation via `findCheapestWindow()`. Creates `PriceFetcherFactory` dynamically from the current source order preference, optionally with `InstrumentedPriceFetcher` wrapping when stats are enabled. CRUD for appliances persisted via `SettingsRepository`. Country/zone selection with auto-detection on first launch. Pushes appliances, zone settings, source order, stats opt-in, and trial/subscription state to Wearable Data Layer after every change via `syncAppliancesToWear()` / `syncSettingsToWear()`. Stores `priceSource` in `UiState` for display in the results disclaimer. Shows one-time stats opt-in prompt after 3 days. Reports stats via `StatsReporter` after successful fetches. Receives watch stats via `/stats` Data Layer path. Errors use an `AppError` sealed interface (`Validation` for inline errors, `Network` for snackbar errors). Manages billing via `BillingRepository`: connects on init, collects unlock state, shows paywall when trial expired and not subscribed. The paywall decision is the pure `shouldShowPaywall(isDebug, trialExpired, unlocked)` helper (debug builds always skip it). Also owns EV charging: loads the bundled vehicle DB in the background (`searchEvVehicles()` powers the Settings picker), `onAddVehicle()` saves a vehicle as an EV-type `Appliance`, and `onEvApplianceFind()` computes the charging duration (`ΔSoC/100 × batteryKwh / min(vehicle AC, home charger)`, pure-linear) when a vehicle chip is tapped. A universal "ready by" deadline (`deadlineEnabled`/`deadlineHour`/`deadlineMinute`) is resolved per search into `UiState.searchDeadline` and applied as a **soft default**: `fetchAndFind()` builds the full earlier-path and lands on the cheapest window that finishes by the deadline (`deadlineDefaultOffset`), while "Cheaper" can walk to cheaper windows finishing after it (`resultMissesDeadline` → the `result_after_deadline` note). An impossible deadline (no window can finish in time, distinct from too little data) shows `ev_error_deadline_unreachable`. The periodic refresh (`recalculateResult`) preserves the navigated window by start time, falling back to that default when it elapses. EV-type appliances are filtered out of the wear sync (no state-of-charge UI on the watch). Owns the **all-in price** (phone only): loads the current country's tariff via `TariffRepository` in the background (bootstrap when uncached / on Settings open, always on country change, and piggybacked on network price fetches via `PriceResult.fromCache`); `allInEnabled`/`supplierId`/`manualSurcharge` persist via `SettingsRepository`. The surcharge field is the source of truth — picking a supplier prefills it (like the home-charger presets) and editing it clears the picked supplier. When enabled with a surcharge set, `fetchAndFind()` runs `AllInPricing.applyAllIn(...)` on the fetched prices *before* the window finder, so cost cards/chart/breakdown show all-in figures and the recommendation is unchanged; `allInSupported` (gates the Settings section) is `allInTariff.usable` **and** the feed currency matching the spot currency (`SPOT_CURRENCY` = EUR — the whole price pipeline is EUR-only, so a non-EUR feed is gated off rather than mixed into EUR prices; the apply path enforces the same guard), and `allInStale` (>14 d) drives a results-page warning. When applied, `fetchAndFind()` also sets `UiState.allInComponents` (the VAT-inclusive fixed part from `AllInPricing.components(...)`) so `PriceBarChart` renders the stacked "fixed baseline + spot deviation" bars (energy tax + surcharge block, then spot extending right or, when negative, a band eating back into the fixed block — and left of the zero reference when the all-in *total* itself is below zero; `AllInBarSegments.segmentsFor` does the geometry over an `[xMin, xMax]` range) with a colour legend, and the cheapest window kept at full colour while every other bar is faded toward the background (`charting/BarColors.dim()` — lightened on a light theme, darkened on a dark one) so the window stands out on both themes. The single-colour negative-axis rendering is used only when all-in is off and some price is below zero. The results screen also carries a quick **all-in on/off `Switch`** (shown only when all-in is configured — `allInSupported && manualSurcharge != null`); `onAllInEnabledFromResult(enabled)` persists the same `all_in_enabled` setting and re-runs the current search from the warm cache (no cooldown, no cache clear) so the cards/chart update instantly. Leaving the Total price settings sub-screen is blocked (snackbar `all_in_incomplete`) when all-in is enabled but has neither a supplier nor a surcharge; a country change turns all-in off and clears the supplier/surcharge (they belong to the previous feed), so no enabled-but-inert state persists. The surcharge field's unit shows the feed currency via `currencySymbol(allInTariff.currency)`. `tariffRepositoryOverride` is injectable for tests. Owns **appliance ordering**: `applianceSort`/`evPosition`/`evSeparate` persist via `SettingsRepository`; a private `withApplianceViews()` recomputes the derived `UiState.sortedAppliances` (non-EV, for Settings) and `homeLayout` (`HomeChipLayout` for the home screen) after any change; taps record usage in `onApplianceDuration`/`onEvApplianceFind`; `syncAppliancesToWear()` pushes the sorted non-EV order. Receives watch usage via an injected **`WatchUsageBridge`** into the testable `onWatchUsageReceived()` (ignores stale reset tokens); `onPurgeUsage()` clears usage and bumps the token. `watchUsageBridgeOverride` is injectable for tests. Owns **household sharing**: `onShareSetup()` builds the share deep link from the current appliances/sort/EV settings via `SetupShare`; `onImportLink(uri)` decodes an incoming link's fragment into `UiState.importPreview` (or `importError`); `onImportConfirmed(mode, selectedIds)` applies the import per `ImportMode` (`ADD`/`REPLACE`/`PICK`) — `REPLACE` also adopts the incoming sort, EV settings, and grouping — re-minting appliance ids, refreshing the derived views, and syncing to the watch; `onDismissImport()` clears the preview/error.
- **`BillingRepository`** (interface in `data/billing/`) — Abstraction over Play Billing with `isUnlocked: StateFlow<Boolean>`, `productPrice: StateFlow<String?>`, `connect()`, `disconnect()`, `launchPurchaseFlow(activity)`, `queryPurchases()`, `onResume()`. Enables injecting a fake in tests.
- **`PlayBillingRepository`** (in `data/billing/`) — Real implementation wrapping `BillingClient` (billing-ktx 8.3.0). Product ID: `yearly_subscription` (SUBS). On connect, queries existing subscriptions to restore state and fetches product details for the price display. Uses `enableAutoServiceReconnection()` for automatic reconnection. Caches subscription state in `SettingsRepository` for offline. Acknowledges purchases to prevent auto-refund. `onResume()` re-queries purchases to detect subscription expiry.
- **`WatchStatsBridge`** (interface) / **`WearableStatsBridge`** — Abstraction over the inbound Wearable Data Layer plumbing for watch stats (listener registration + `/stats` byte decoding), so `SweetSpotViewModel` stays testable with a fake and free of `DataClient.OnDataChangedListener`. `WearableStatsBridge` is the thin production impl (no decision logic) and is excluded from coverage.
- **`WatchUsageBridge`** (interface) / **`WearableUsageBridge`** — The same pattern for the `/usage` path: decodes the watch's usage snapshot + reset token and calls the testable `onWatchUsageReceived()`. `WearableUsageBridge` is the thin production impl, excluded from coverage.

### Wear app (`:wear`)

- **`WearViewModel`** — Receives appliances and settings from the phone via a **`WearSync`** (injected; production impl `WearableSync`). `onAppliancesReceived`/`onSettingsReceived` are testable `internal` handlers that resolve the price zone, source order, disabled sources, stats opt-in, per-app language, and `isLocked` (`is_trial_expired && !is_unlocked`). On appliance tap, creates `PriceFetcherFactory` dynamically from source order (with stats instrumentation when enabled), fetches prices via `PriceRepository` (using the phone's zone) and runs `findCheapestWindow()`. Prices are cached locally on the watch. Records the tap in a local `UsageStore` and, after each fetch, pushes accumulated stats via `WearSync.pushStats` and its cumulative usage snapshot via `WearSync.pushUsage`; a newer `usageResetToken` on `/settings` zeroes the store.
- **`WearSync`** (interface) / **`WearableSync`** — Abstraction over the Wearable Data Layer (observe appliances/settings, push stats, push usage), so `WearViewModel` is testable with a fake. `WearableSync` is the thin production impl (listener + initial read + `putDataItem`); it holds no logic and is excluded from coverage.
- **`WearActivity`** — `SwipeDismissableNavHost` with two routes: `"appliances"` (start) and `"result"`. When `state.isLocked`, shows `WearLockedScreen` instead of the appliance list.
- **`ui/ApplianceListScreen`** — `Scaffold` with `PositionIndicator`, `TimeText`, `ScalingLazyColumn` of appliance `Chip`s (icon + name + duration), empty state, loading overlay.
- **`ui/ResultScreen`** — `ScalingLazyColumn` centered on the appliance label, with start/end times in HH:mm and relative display that auto-refreshes every 60 seconds. Scrollable for long labels on round watch faces.
- **`ui/WearLockedScreen`** — Centered text informing the user that the subscription has expired and they need to open the phone app to subscribe.
- **`ui/WearTheme`** — Wear Material theme wrapper.

### Phone navigation

State-based in `MainActivity`, no navigation library:
- `UiState.showOnboarding` (seeded `!settingsRepository.isOnboardingShown()`) shows the first-launch `OnboardingScreen` (`ui/onboarding/`) — a skippable 3-page value-first intro (`HorizontalPager`) — as a full-screen gate just below the import-preview branch and above the paywall. `onOnboardingComplete()` (Skip/Get started) persists `onboarding_shown` and returns to the screen behind it; `onReplayOnboarding()` re-shows it from **Settings › How it works** (a menu row wired via the `onReplayOnboarding` callback, not a `SettingsRoute`). The overlay dialogs are suppressed while it's showing.
- **Contextual coach marks** (follow-up to onboarding): one-time anchored hints that point at a hard-to-discover control the first time it's reached. The `CoachMark` enum (`model/`, four entries: `EARLIER_CHEAPER`, `CHART_PRESS_HOLD`, `ALL_IN_TOGGLE`, `EV_CHIP`) each own a per-hint `coach_*` "seen" flag in `SettingsRepository` (`isCoachMarkSeen`/`setCoachMarkSeen`/`resetCoachMarks`). The pure `CoachMarkPolicy` (`util/`) decides **which** hint is due (`resultsDue` in priority order — Earlier/Cheaper → chart → all-in toggle, the last only when configured; `homeDue` for the EV chip), enforcing "one hint per screen appearance". `SweetSpotViewModel` exposes the due hint as `UiState.activeCoachMark` (set on results success and on returning home / init), clears it on `onCoachMarkDismissed(mark)` ("Got it"), and **retires it when the user uses the feature** — `onEarlierWindow`/`onCheaperWindow`, `onChartInspected` (from the chart long-press), `onAllInEnabledFromResult`, `onEvApplianceFind` each mark theirs seen. The anchored `CoachMarkCallout` (`ui/components/`, coverage-excluded) is a `Popup` bubble + tail placed by the pure `CoachMarkGeometry.calloutFor` (prefers above the target, flips below, clamps to the window, tail tracks the target centre); a control publishes its bounds via `Modifier.coachMarkAnchor(active, onBounds)` and the callout renders only while the target is on screen. Hints are suppressed while a dialog is up (the stats prompt; the EV SoC dialog) and never appear during onboarding/paywall/import (those replace the screen). Developer options → **Reset tips** (`onDevResetCoachMarks`) re-arms them all.
- `UiState.importPreview != null` takes precedence over every other screen: a scanned/tapped setup App Link opens `ImportPreviewScreen` (`ui/share/`) straight away, even on a cold start. `MainActivity` reads the launch `intent.data` in `onCreate` and, because it is `launchMode="singleTask"`, also handles warm starts in `onNewIntent`/`setIntent`, forwarding any `ACTION_VIEW` link to `vm.onImportLink(uri)`. A decode failure sets `importError` and shows an `ImportErrorDialog` overlay (update-the-app for `TOO_NEW`, "couldn't read" for `MALFORMED`) rather than a screen.
- `UiState.showSettings` toggles between `SweetSpotScreen` and `SettingsScreen`
- `SettingsScreen` is itself a small coordinator: a single `SettingsRoute` (rememberSaveable enum, no nav library) switches between a **root menu** of category rows (WhatsApp-style icon + title + one-line description, via `SettingsMenuRow`) and one self-contained category sub-screen — `AppliancesSettingsScreen`, `EvSettingsScreen`, `TotalPriceSettingsScreen`, `RegionSettingsScreen` (country/zone/timezone + the country→zone auto-advance), `AppearanceSettingsScreen` (language/theme), `ShareSetupScreen` (`ui/share/` — the QR + "Share link" sharesheet), `HelpSettingsScreen` (Help & support — see below), and the pre-existing `AdvancedSettingsScreen`. Each sub-screen owns its own pickers/dialogs + `BackHandler` (built on the shared `SettingsSubScreen` scaffold). The statistics opt-in stays an inline toggle row on the menu; the version-footer 7-tap developer-options unlock now lives on Help → About. The **all-in exit guard** (block leaving with all-in enabled but no supplier/surcharge, showing the `all_in_incomplete` snackbar) lives on `TotalPriceSettingsScreen`. Category icons are vector drawables in `shared/src/main/res/drawable/` (`ic_price`/`ic_region`/`ic_appearance`/`ic_advanced`/`ic_stats`/`ic_share`/`ic_help`, reusing `ic_device`/`ic_ev_charger`).
- `HelpSettingsScreen` (`ui/settings/`, coverage-excluded) is a **Help & support** coordinator with its own `rememberSaveable` sub-state (menu / report form / My reports / quick guide) on `SettingsSubScreen`. Its menu groups: **Help** (Quick guide — a short offline in-app how-to; How it works — replays the onboarding intro via `onReplayOnboarding`; Reset tips — re-arms coach marks, relocated here off the developer section; FAQ link), **Support** (Report a problem / Send feedback — open the in-app form; My reports; Contact us — `mailto:`; Rate SweetSpot — Play Store intent), and **About** (What's new / Privacy policy links; the version footer + 7-tap dev unlock). On entry a `LaunchedEffect` calls `loadMyReports()` + `flushOutbox()`. The form POSTs to the feedback Worker via the ViewModel's `onSubmitReport(...)`; success shows "Report #N filed", a retryable failure is saved to the outbox (auto-retried) while keeping the form content (no manual Retry — it would duplicate the queued copy), and a permanent 4xx shows a validation message the user can edit and resend. "My reports" shows each report's live open/closed state plus a comment count when there's activity, and an **unread dot** when the issue has more comments than the user has seen (persisted per report via `SettingsRepository` seen-count; cleared when the thread is opened); tapping a report opens an **in-app conversation thread** (`ThreadScreen` — the issue body + comments read from the public GitHub API via `GithubIssueApi.fetchThread`, entries labelled "You"/"SweetSpot Support" by `ThreadItem.mine`, with retry + open-on-GitHub), driven by the ViewModel's `thread: ThreadState?` + `onOpenThread`/`onCloseThread`. When the device holds the report's `replyToken` (returned by `/report`, stored on `MyReport`), the thread shows a **reply composer**: `onSendReply` POSTs to the Worker `/reply` (`ReportSubmitter.submitReply`), which comments as the bot; on SENT the reply is **appended to the open thread optimistically** (GitHub's public API is edge-cached for unauthenticated reads, so an immediate refetch wouldn't see the just-posted comment — the next open reloads canonically), a transient failure queues the reply in a **reply outbox** (`PendingReply`; `flushReplyOutbox` mirrors the report outbox — same mutex/attempts cap, flushed on init + Help open), a permanent 4xx errors (`ReplyState`). Comment bodies render as **markdown** via `compose-markdown` (JitPack). Website links (FAQ/privacy/changelog) open in a **Chrome Custom Tab** (`androidx.browser`, themed to the app) via `HelpLinks.localizedUrl(path, languageTag, dark)` — English at root else `/<lang>/`, always carrying `?lang=<code>&theme=<light|dark>` so the site matches the app's language and light/dark mode (the site would otherwise redirect to a previously-saved language and render only light). The quick guide's all-in item is shown only where `allInSupported`. The report/feedback pipeline is pure and tested in `:shared` (`model/FeedbackReport`, `data/support/FeedbackCodec`, `util/Diagnostics`, `util/HelpLinks`, `data/api/GithubIssueApi`, and the `SettingsRepository` my-reports/outbox stores); the ViewModel's `onSubmitReport`/`flushOutbox`/`loadMyReports`/`onDismissReportResult` are covered with a fake `ReportSubmitter` + fake `GithubIssueApi`; the `HttpReportSubmitter` HTTP glue is coverage-excluded like the other IO wrappers. Backed by the feedback Worker (see External APIs).
- `UiState.result != null` switches `SweetSpotScreen` from the form view to a dedicated results screen (separate `Scaffold` with back arrow and result label in the top bar)
- The results screen (headed **"Recommended time"** — a generic title, since the shown window may be an Earlier alternative or the cheapest that meets a deadline) shows **Earlier** / **Cheaper** buttons that walk the `findWindowAlternatives` list: "Earlier" moves to a sooner (costlier) window, "Cheaper" steps back toward the cheapest. "Cheaper" is disabled at the cheapest window, "Earlier" at the earliest. The total-cost card shows how much more the shown window costs than the **recommended** window (`UiState.recommendedCost` — the global cheapest, or the cheapest that meets a "ready by" deadline; `windowOffset` / `windowAlternatives` track the walk). With a deadline the default lands on the cheapest window meeting it, but "Cheaper" can walk to cheaper windows that finish **after** the deadline — flagged by `UiState.resultMissesDeadline` → a results-screen note (`result_after_deadline`).
- System back and the top-bar back arrow both call `onClearResult()` to return to the form

### Main Screen

The form view (`DurationInput` card) contains:
- **Appliance chips** (top) — `AssistChip` buttons with configurable icons for user-defined appliances; tapping fills duration and triggers search. EV-type appliances (those with an `EvSpec`) instead open a `SocDialog` to enter current/target state of charge before searching. If no appliances exist, a CTA links to settings.
- **Quick-duration row** (below) — 6 equal-width `SuggestionChip` buttons (1h–6h) using `Row` with `weight(1f)` so they fill the row on any screen width.
- **Duration picker** — two-column scroll wheel (`DurationPicker`) for hours (0–24) and minutes (0–55 in 5-min steps) with snap-to-item behaviour.
- **"Ready by" row** (`DeadlineRow`) — an optional universal deadline (switch + time picker) applied to every search type, including EV charging.
- **Find button** — disabled when duration is 0h 0m.

EV charging is configured in Settings (`EvSection`): the home charger power and default target charge, plus the list of saved vehicles. "Add vehicle" opens `VehicleDialog`, which searches the bundled database (or accepts a custom vehicle typed in manually) and saves it as an EV-type `Appliance`.

### Theme

`SweetSpotTheme` wraps Material 3 with dynamic colour. Bar chart colours (spot blue, negative purple, and the all-in energy-tax/surcharge segments) use `CompositionLocal` to stay fixed regardless of dynamic colour. The cheapest window is highlighted by fading every *other* bar toward the theme background (`PriceBarChart`'s `dim()`), not by a distinct bar colour.

## External APIs

- **ENTSO-E Transparency Platform** (primary for all zones) — 43 European bidding zones, 15-min resolution. API docs: https://transparencyplatform.zendesk.com/hc/en-us/articles/15692855254548-Sitemap-for-Restful-API-Integration. Token stored in `local.properties` as `ENTSOE_API_TOKEN`, injected via `BuildConfig`.
- **Spot-Hinta.fi** (Nordic/Baltic fallback) — 15 zones (FI, SE1–SE4, DK1–DK2, NO1–NO5, EE, LV, LT), 15-min resolution, prices in EUR/kWh. Endpoint: `https://api.spot-hinta.fi/TodayAndDayForward?region={region}`. No auth required. Used as fallback when ENTSO-E fails for Nordic/Baltic zones.
- **EnergyZero** (NL fallback) — NL-only day-ahead prices: `https://api.energyzero.nl/v1/energyprices`. No auth required. Used as fallback when ENTSO-E fails for NL.
- **Energy-Charts** (European fallback) — 15 zones (AT, BE, CH, CZ, DE-LU, DK1, DK2, FR, HU, IT-North, NL, NO2, PL, SE4, SI), 15-min or 60-min resolution, prices in EUR/MWh. Endpoint: `https://api.energy-charts.info/price?bzn={bzn}&start={ISO}&end={ISO}`. No auth required. CC BY 4.0 licensed. Used as fallback when other sources fail.
- **aWATTar** (AT/DE-LU fallback) — 2 zones (AT, DE-LU), hourly resolution, prices in EUR/MWh. Endpoints: `https://api.awattar.at/v1/marketdata` (AT) and `https://api.awattar.de/v1/marketdata` (DE-LU). Parameters: `start`/`end` in milliseconds epoch. No auth required. Used as tertiary fallback after Energy-Charts for AT and DE-LU.
- **All-in tariff feed** (display-only all-in price, not a spot source) — `https://sweetspot.today/data/suppliers/<cc>.json` (currently NL only), published by the `build-suppliers` cron (see `bin/data/build-suppliers.py`). No auth. Fetched by `TariffApi`/`TariffRepository`, cached, and combined with the app's own spot price via `AllInPricing`. Adding a country server-side (a new `<cc>.json`) auto-enables all-in there with no app release. See `docs/notes/ideas/all-in-pricing-nl-pilot.md`.
- **Feedback Worker** (in-app Help & support, not a price source) — `POST https://feedback.sweetspot.today/report` with `{category:"bug"|"feedback", subject, body, diagnostics?, email?}` → `201 {number,url}` (415/400/429/5xx errors). A Cloudflare Worker (`server/feedback-worker/`) turns each submission into a public GitHub issue (labelled `from-app` + `bug`/`enhancement`; returns a per-report `replyToken`) and, via a GitHub webhook, emails opted-in reporters through Brevo (each notification carries a tokenized one-click unsubscribe link — `GET`/`POST /unsubscribe` — that clears the reporter's stored email). `POST /reply {issue, token, body}` lets the app post a comment on its own report (bot-authored, token-authorised). The app holds **no secret** — it only POSTs to the Worker (`HttpReportSubmitter`) and reads *public* issue data for "My reports" and the in-app thread from the unauthenticated GitHub REST API (`GithubIssueApi`, `GET https://api.github.com/repos/jmerhar/sweetspot-android/issues/{n}`, 60/hr/IP). See `docs/notes/reference/help-support-system.md`.

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
- UI text is localised via Android string resources (`strings.xml`) in 25 European languages (bg, cs, da, de, el, es, et, fi, fr, hr, hu, it, lt, lv, mk, nb, nl, pl, pt, ro, sk, sl, sr, sv + English). Per-app language setting via AppCompat. Defaults to system locale. Strings containing numbers that affect grammar (e.g. "%d minutes") must use `<plurals>` with the correct CLDR plural categories for each language — use `getQuantityString()` / `pluralStringResource()` instead of `getString()` / `stringResource()`.
- **Translation terminology must match across the app (`strings.xml`), Play Store metadata, and website (`site/`)**, and prefer semantic over literal translations. Canonical example: "cheapest window" / "time window" means a **period of time**, not an architectural window — never translate it literally (English "window" → `okno` / `fönster` / `παράθυρο` / `vindue` etc. is wrong; use the period/slot term, e.g. Slovenian `termin`). The app's semantic term is the reference; correct the website to match. Genuine UI-dialog "window" uses and the "sliding window algorithm" name are separate, judged by context. Watch for inflected forms (case/gender/number) when substituting.
- **Register (formal vs informal) is fixed per language and must be consistent *within* each language** (cross-language *form* consistency does not matter — pick what reads best per language). **Informal** (familiar T-form) for **German, Dutch, Danish, Norwegian, Swedish** (formal address there is archaic). **Formal** (polite V-form: vous / Sie / usted / Lei / vy / Вие / dumneavoastră / teie …) for **every other language with a T–V distinction**. Apply the chosen register everywhere: app strings, website, Play listing, and screenshot captions. Bare imperative **button labels** ("Save"/"Cancel") follow each language's own UI convention — some treat them as informal and use the polite-plural imperative, others as register-neutral command labels; do whatever is idiomatic for that language (so, e.g., Croatian and Serbian legitimately differ).
- **One natural term per concept — never a calque — matched across app, website, listing, and screenshot captions:**
  - **run period** (the recommended result window): a natural word for a span of time in each language (English **"time"**; e.g. `termin`, `tid`, `tidspunkt`, `fascia`, `créneau`, `período`, `период`) — **never** the architectural-window calque (`okno`/`fönster`/`παράθυρο`/`vindue`). English results header is **"Recommended time"**.
  - **Total price** (the all-in feature): the local equivalent (several languages chose "final price").
  - **market price** (the bare wholesale price): the local term; several markets idiomatically use the "exchange price" form (`biržas cena`, `pörssihinta`, `börsihind`). Keep "spot" only where English does — as a parenthetical alias.
  - **dynamic / day-ahead prices** — never "real-time".
  - **reliability statistics** — drop "API" from the user-facing name (keep "API request" only in the privacy bullet list, as English does).
  - **supplier surcharge** and the **"ready by" deadline** — one consistent term each.
- **Listing length limits** (Play Store, in code points): title ≤ 30, short description ≤ 80, full description ≤ 4000; a Play "what's new" changelog entry ≤ 500. Enforced by `make check-listing` (CI) and `bin/deploy/deploy.sh` (changelog). Romance languages run longest, so keep the English source lean to leave headroom.
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
    .well-known/assetlinks.json    # Digital Asset Links (App Links verification for /import)
    css/style.css                  # All styles (single file)
    js/main.js                     # Language auto-redirect, nav toggle, language switcher
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
5. **Add language to `site/static/js/main.js`**: add the language code to the `SUPPORTED` array (used for browser language auto-detection on first visit).
6. **Verify**: run `make site` and check the new language appears in the language switcher and all pages render correctly.

### Design

- **Colours**: primary blue `#4A90D9`, green `#27AE60`, purple `#9B59B6`, yellow `#F1C40F`
- **Light palette**: bg `#F8F9FF`, surface `#FFFFFF`, text `#191C20`, muted `#44474E`
- **Footer**: dark bg `#111318`, text `#E1E2E9`
- **Dark mode**: all colours are CSS variables in `:root`; a `:root[data-theme="dark"]` block overrides the surface/text palette (brand colours, blue hero gradient, and the already-dark footer stay). The theme is **explicit-parameter driven**: an inline script in `head.html` (before the stylesheet, so no flash) reads `?theme=light|dark`, persists it to `sessionStorage` (per Custom Tab session), and sets `data-theme` — the app opens links with `&theme=` matching its own light/dark mode (see `HelpLinks.localizedUrl`), so a Custom Tab matches the app. No on-site toggle yet; direct visitors default to light (or their saved choice).
- **Layout**: max-width 1100px, CSS Grid, responsive at 768px and 1024px
- **Typography**: system font stack, line-height 1.6

### Deployment

GitHub Actions (`.github/workflows/deploy-site.yml`) triggers on pushes to `main` that change `site/**`, `fastlane/metadata/android/**`, or `bin/site/site-screenshots.sh`. It installs `webp`, runs `bin/site/site-screenshots.sh` to generate the per-language landing-page screenshots from the committed framed images, then builds with Hugo extended + `--minify` and deploys to GitHub Pages. Custom domain via `CNAME` file + DNS A records pointing to GitHub Pages IPs (185.199.108–111.153).

**App Links for household sharing:** `site/static/.well-known/assetlinks.json` (Hugo copies `static/**` to the site root) is the Digital Asset Links statement Google fetches to verify the `today.sweetspot` App Link, so a scanned/tapped `https://sweetspot.today/import#…` link opens the app directly (no browser chooser). It lists the **Play app-signing** SHA-256 (from Play Console → Test and release → App integrity → App signing key certificate) — public by design, safe to commit. Debug builds use the separate `today.sweetspot.debug` package and aren't verified; test the App Link with a release/internal-track build. If the signing certificate ever changes (e.g. a key rotation), update the fingerprint here.

### Updating the Changelog

When releasing a new app version, add a new entry at the **top** of `site/content/<lang>/changelog.md` for each language:

```
{{< changelog version="X.Y" date="DD. month YYYY" >}}
- Change description
- Another change
{{< /changelog >}}
```

Date format varies by language (e.g., "March 28, 2026" in English, "28. marec 2026" in Slovenian). The `version` attribute must match the app's `versionName` exactly — `bin/deploy/deploy.sh` verifies this before uploading.

**Important:** Each changelog entry is extracted by `bin/deploy/deploy.sh` and uploaded as Play Store "What's New" text, which has a **500-character limit**. Keep entries concise — Romance languages (Portuguese, Spanish, French) tend to run longest. The deploy script will refuse to upload if any translation exceeds 500 characters.

## Stats Backend & Monitoring (aurora)

The opt-in API reliability stats (see `StatsReporter`) are received and stored server-side on the `aurora` host:

- **Endpoint** — `server/stats/stats.php`, deployed to `aurora:/var/www/stats.sweetspot.today/` via `make deploy-stats` (`bin/deploy/deploy-stats.sh`, scp + the `clear-rate-limit.sh` helper). Setup runbook: `server/stats/README.md`. (The unrelated `server/feedback-worker/` is the Help-section report/feedback Cloudflare Worker.) Behind Cloudflare; the Apache vhost (`stats.sweetspot.today.conf`) rewrites `POST /report` → `stats.php` and denies every other path (so a dashboard pinging `/` logs harmless 403s). It validates the JSON payload, rate-limits per IP (5 min), converts to line protocol, and writes to **InfluxDB 3 Core** (`db=sweetspot`, measurement `api_fetch`) on `localhost:8181`.
- **InfluxDB (aurora)** — runs as a Docker container `influxdb` (image `influxdb:3-enterprise`) via `docker compose` in `aurora:/opt/monitoring/` (alongside Grafana, an InfluxDB UI, node-red, etc.); DB `sweetspot`, measurement `api_fetch`, token at `/opt/monitoring/secrets/influxdb_token`. `influxdb3` is not on the host PATH — query via the container: `ssh aurora 'docker exec influxdb influxdb3 query --database sweetspot --token "$(cat /opt/monitoring/secrets/influxdb_token)" "SELECT … FROM api_fetch WHERE source != '\''test'\'' …"'`. `api_fetch` fields: `time, zone, source, device, app, outcome` (ok/fail — not `success`), `error`, `status`, `duration_ms`, `lang`, `count`. A `/report` `200 {"ok":true}` is returned only after InfluxDB acks the write (204), so a test-marked POST verifies ingestion end-to-end. Full ops runbook: `server/stats/README.md`.
- **Config** — via `SetEnv` in the webroot `.htaccess` (not in the repo): `INFLUX_TOKEN` (write auth) and `KUMA_PUSH_URL` (optional heartbeat target). `.htaccess` is read per request, so changes are live without an Apache reload.
- **Keep `status` values in sync** — `stats.php` validates the `status` field against a whitelist. The app sends `subscribed` for unlocked users (changed from `unlocked` in the billing migration); a stale whitelist silently rejected every report with `400` from Apr–Jun 2026 (the app discards 4xx as corrupt and never retries). When the app's payment-status strings change, update the whitelist too.
- **Monitoring** — an Uptime Kuma **push** monitor "SweetSpot stats ingestion" (id 122, group Infrastructure) is heartbeated by `ping_kuma()` in `stats.php` after every successful InfluxDB write. 3-day heartbeat interval → alerts (via the shared infra notification channel) if no successful ingestion happens for ~3 days, catching silent pipeline breakage. Inherent limitation: it also alarms if reporting traffic legitimately dries up.
- **Managing Kuma** — no REST API exists; use the socket.io CLI: `docker exec kuma-api python /app/kuma_client.py <list|get|add|clone|edit|...>` (lives at `aurora:/opt/uptime-kuma`). Creating a push monitor via the API does **not** auto-generate a `pushToken` — set it explicitly with `edit`.
- **InfluxDB 3 Core has no row/predicate `DELETE`** ("DML not supported: Delete"; `influxdb3 delete` only drops whole databases/tables/caches). To remove specific rows you must drop and rebuild the table, or exclude them at query time (e.g. `WHERE source != 'test'`).
- **Synthetic/test records must use `source = "test"`** (and conventionally `zone = "ZZ"`, `app = "0.0.0"`) — a stable marker, since rows can't be deleted. The Grafana dashboard ("SweetSpot API Reliability") is committed at `server/stats/grafana-dashboard.json` and already excludes them: every panel query carries `WHERE source != 'test'`. `server/stats/test.sh`'s valid payloads use this marker too. Any manual end-to-end test of `/report` (e.g. verifying ingestion or the Kuma heartbeat) must set `"s": "test"` in its records so it is filtered automatically; never send test traffic under a real source id.

## Commit Messages

[Conventional Commits](https://www.conventionalcommits.org/): `<type>: <description>` describing the **what** and **why**.

Types: `feat`, `fix`, `refactor`, `style`, `docs`, `chore`.

If `git commit` fails with `user.signingKey needs to be set for ssh signing`, stop and inform the user — they need to refresh the GPG key manually. Do not bypass signing.
