# Test Coverage in CI — DONE

## Status: Implemented (Kover, per-module HTML artifacts)

Code coverage is reported in CI via **Kover** (`org.jetbrains.kotlinx.kover` 0.9.8), applied to each module so every module gets its **own** report. Verified working on AGP 9.0.1 / Kotlin 2.3.20.

## How it works

- **Plugin:** the classic per-module Kover plugin, applied in `:shared`, `:app`, and `:wear` via `alias(libs.plugins.kover)` (version in `gradle/libs.versions.toml`). Deliberately *not* the aggregated settings plugin — that merges all modules into a single report, which is the opposite of the per-module visibility we want.
- **Per-module tasks:** each module exposes `koverHtmlReportDebug`, `koverXmlReportDebug`, `koverVerifyDebug`, etc. The `Debug` variant is used because unit tests run on debug (the unqualified "all code" variant would also pull in release classes and skew the numbers). Running an unqualified task name executes it in all three modules.
- **Command:**
  ```bash
  ./gradlew testDebugUnitTest koverHtmlReportDebug koverXmlReportDebug
  ```
  Per module: HTML → `<module>/build/reports/kover/htmlDebug/index.html`, XML → `<module>/build/reports/kover/reportDebug.xml` (both gitignored under `build/`). All three exist simultaneously.
- **CI** (`.github/workflows/test.yml`): runs the command above, renders an at-a-glance per-module coverage table on the run's summary page (via `$GITHUB_STEP_SUMMARY`), and uploads each module's XML to **Codecov** under its own flag (`shared`/`app`/`wear`).
- **Codecov:** browsable per-module report + README badge at [codecov.io/gh/jmerhar/sweetspot-android](https://codecov.io/gh/jmerhar/sweetspot-android). Config in `codecov.yml` (per-module flags, status `informational` = no gate, `comment: false` since there are no PRs). Needs the `CODECOV_TOKEN` repo secret; uploads use `fail_ci_if_error: false` so a Codecov hiccup never fails the build. Chosen over downloadable HTML artifacts (which required download-unzip-hunt) for a one-click browsable report; the HTML is still reproducible locally.
- **Test Analytics:** CI also uploads JUnit results (`codecov-action@v5` with `report_type: test_results`, `if: !cancelled()` — the standalone `test-results-action` is deprecated) so Codecov tracks flaky tests, failure history, and slowest tests. Gradle writes `TEST-*.xml`, so the three modules' `build/test-results/testDebugUnitTest/*.xml` paths are passed explicitly (the action's default search is `*junit.xml`); the test step runs with `--continue` so all modules report even when one fails. Caveat: the PR-comment side of Test Analytics (failed/flaky tests on a PR) is inert since this repo pushes straight to `main` — the dashboard (flaky detection, run history, slowest tests on `main`) still works.
- **HTML reports on GitHub Pages (per commit, kept forever):** Codecov's line-by-line view is fine, but Kover's own HTML report is richer, so every green push to `main` publishes it to the shared, **source-agnostic** site **`jmerhar/coverage`** → `https://jmerhar.github.io/coverage/sweetspot-android/`. This repo is deliberately thin: `bin/collect-coverage.sh` assembles the three modules' `htmlDebug` dirs + a `reports.json` manifest (`bin/coverage-report.py --format reports`), then the coverage repo's own `bin/add-report.sh` (run from a checkout) drops them under `reports/sweetspot-android/<sha>/`, writes `meta.json`, and pushes. **All the site-building logic lives in `jmerhar/coverage`** — its workflow regenerates the project/commit indexes + cross-links and deploys via GitHub Actions Pages. A `coverage/report` commit status links each commit to its report. Guarded by `if: success() && push && main && env.COVERAGE_PAGES_TOKEN != ''`, so publishing is skipped (not failed) until the token exists.

### Adding another project to `jmerhar/coverage`

This lives in the **coverage repo's own README** now (it owns the process). In short: any language/tool works — a project produces its report HTML + a `reports.json`, adds the `COVERAGE_PAGES_TOKEN` secret, and calls the coverage repo's `bin/add-report.sh` from CI. No changes to the coverage repo are needed; it discovers projects from `reports/`. See <https://github.com/jmerhar/coverage>.

## Baseline (at implementation)

Each module's own debug unit tests. `:shared` was subsequently raised from its initial 71.3% by a targeted gap-closing pass (see below):

| Module | Line | Instruction | Branch | Class |
|---|---|---|---|---|
| **`:shared`** | **99.6%** | 99.1% | 88.8% | 98.0% |
| `:app` (initial) | 15.7% | 12.1% | 6.8% | 21.6% |
| **`:app` (after pass)** | **99.0%** | 98.7% | 83.0% | 100% |
| `:wear` (initial) | 25.9% | 20.9% | 12.3% | 26.3% |
| **`:wear` (after pass)** | **~95%** | ~89% | ~73% | — |

`:shared` (pure logic/parsing/algorithms) carries the real coverage; `:app`/`:wear` are mostly Compose UI that unit tests don't touch.

**Note on the `:shared` number.** A per-module report counts only coverage from that module's *own* tests. An earlier aggregated experiment showed `:shared` higher because it also counted `:shared` classes exercised by `:app`/`:wear` ViewModel tests (which call into shared repositories/finders). The per-module figure is the honest "does `:shared`'s own suite cover `:shared`" number — the right one to gate on.

### `:shared` gap-closing pass

Before considering a gate, the real gaps were addressed (71.3% → 97.2% line):

- **Added Robolectric to `:shared`** (previously pure JUnit) to test the `Context`-backed data classes.
- New tests: `FilePriceCacheTest` (v3 format/migration/corruption/cooldown → `data/cache` 16%→100%), `SettingsRepositoryTest` (trial/source-order/appliance/zone/timezone logic), `CountryDetectorTest` (detection fallback chain → `data/repository` 31%→93%), `PriceFetcherFactoryTest` (chain composition, was untested anywhere), `StatsRecordTest` (`data/stats`→100%), and `ApiHttpTest` (the five clients' HTTP paths → `data/api` 74%→99%).
- **Kover filters** exclude generated boilerplate (`@Serializable` types, `BuildConfig`) so the number reflects real logic — see `shared/build.gradle.kts`.
- To support real assertions: `FallbackPriceFetcher.fetchers` and `InstrumentedPriceFetcher.delegate`/`sourceId` were widened `private` → `internal`, and each API client gained an injectable `OkHttpClient` param (default `sharedHttpClient`) so a canned-response interceptor can drive `fetchRaw`/`fetchPrices` without a network.

A second pass then took `:shared` to **99.6% line / 88.8% branch**: added a `:shared` `UiTextResolveTest` (so `resolve()` is covered in its own module too), `ResourceFormattingTest` (the localised branches of `formatDuration`/`formatRelative`), more `SettingsRepositoryTest` cases (developer options, invalid-timezone fallback, auto-detect), a `CountryDetector` locale-region case, and ENTSO-E parser edge cases (missing price/resolution, A03 with no position 1, out-of-context tags).

The only remaining uncovered **lines** are three defensive `error(...)` guards that are unreachable in practice (`PriceFetcherFactory` unknown-source `else`, `EnergyChartsApi`/`AwattarApi` "no mapping for zone").

### Branch-coverage pass (`:shared` 88.8% → 92.4%)

Branch coverage lagged line coverage mainly because of the **dual-mode formatter idiom** `resources?.getX(...) ?: "englishFallback"` in `TimeUtils`/`FormatUtils`: every production caller passes a real `Resources`, so the fallback exists only for pure (non-Robolectric) tests — and each localised line carried an *unreachable* branch ("`resources` non-null **and** the getter returned null"), which Kover marks missed forever (this alone pinned `TimeUtils` at 54% branch despite full line + both-mode coverage). Fix: branch on `resources` **once** (`if (resources != null) { …localised when… } else { …english when… }`) instead of per line — `TimeUtils`/`FormatUtils` went to 100% branch with no test changes. A follow-up edge-test pass then covered the genuinely-reachable branches (unknown-country zone fallback, invalid stored timezone, telephony-less `CountryDetector`, empty SIM ISO, `clear()`'s file-name filter, legacy stats-file delete, deadline-capped/sub-slot/clamped-breakdown windows).

The remaining unhit **branches** are all defensive/unreachable: null-safe `?.` sides (`listFiles()` etc.), the null-`TelephonyManager`/null-ISO paths, `try`-with-resources `use {}` close paths, `for`-loop zero-iteration exits the guards make unreachable, the fractional-slot-beyond-array guards, `?:`-with-a-non-null-default, compound short-circuits, and Kotlin `when`-on-`String` hashCode artifacts. Kover has no per-branch exclusion, so these can't be filtered individually; they aren't worth contriving tests for.

### `:app` / `:wear` — presentation vs. logic pass

The UI modules were dominated by Compose UI (unit-untestable). The approach (documented as a rule in `CLAUDE.md` → "Presentation vs. logic"): **extract any real logic out of presentation/framework classes into testable units, then exclude only the presentation/glue.** Excludes per module: `@Composable` functions, `*ComposableSingletons*`, Activities, `BuildConfig`, and thin SDK wrappers.

`:wear` (25.9% → ~95%): the UI (`ResultScreen`, `ApplianceListScreen`, `WearLockedScreen`, `WearTheme`, `WearActivity`) had no extractable logic — it all delegated to tested `:shared` utilities. `WearViewModel`'s Wearable Data Layer plumbing (previously inline and duplicated across `onDataChanged`/`loadFromDataLayer`) was isolated behind a **`WearSync`** interface (real impl `WearableSync`, excluded); the mapping/zone/parse logic moved into testable `internal` handlers (`onAppliancesReceived`/`onSettingsReceived`), driven in tests by a fake `WearSync`.

`:app` (15.7% → ~99%): the extractions were —
- `formatKw` / `formatHhMm` moved from the `EvChargingComponents` Compose file into `:shared` `FormatUtils` (pure formatting, now unit-tested there).
- The paywall decision (5 duplicated `!BuildConfig.DEBUG && …` expressions, unreachable in debug unit tests) became the pure `shouldShowPaywall(isDebug, trialExpired, unlocked)`, tested for both build types.
- `StatsReporter`'s HTTP send was isolated behind a **`StatsPoster`** interface (real impl `HttpStatsPoster`, excluded); the response-code policy became the pure `reportOutcomeFor(code)`, so `reportIfDue`'s branches (200/4xx/429/5xx/network) are driven by a fake poster.
- The phone's inbound Data Layer glue (the `DataClient.OnDataChangedListener` + `onDataChanged` decode) moved behind a **`WatchStatsBridge`** interface (real impl `WearableStatsBridge`, excluded), leaving the tested `onWatchStatsReceived`.
- `SweetSpotViewModel` (already the logic home) gained a `statsPoster`/`watchStatsBridge` injection point and fill-in tests for the previously-uncovered branches (stats reporting with each payment status, EV no-zone/zero-charger, find no-zone, unreachable deadline, dev time-override, purchase forwarding, `recalculateResult` when all slots elapsed). Excludes: `@Composable`, `*ComposableSingletons*`, `today.sweetspot.ui.*`, `MainActivity`, `PlayBillingRepository`, `WearableStatsBridge`, `HttpStatsPoster`, `BuildConfig`. The ~6 residual uncovered lines are DI wiring and defensive `?:` guards (the release-only `PlayBillingRepository` construction, the default fetcher factory, a null-zone early return).

## Decisions

- **Per-module reports** (not aggregated) — separate, persistent HTML per module, one Codecov flag each.
- **Surfacing:** Codecov (browsable web report + badge) plus a run-page summary table. No PR comment (this repo doesn't use PRs).
- **Gate on `:shared`** — after the gap-closing passes took it to ~99.6%, a Kover verify rule (`shared/build.gradle.kts`) requires **≥98% line coverage**; CI runs `./gradlew :shared:koverVerifyDebug` as the final step (after the uploads, so coverage/test data is still captured when the gate fails). Chosen over a Codecov status target because this repo pushes straight to `main` — a Codecov status has nothing to block, whereas the Kover rule fails the build itself, locally and in CI. `:app`/`:wear` are ungated (mostly untested Compose UI). The 98% bound sits ~1.6 points under the real number — tight enough to catch a real regression while leaving a small buffer for defensive/edge lines.

## Follow-ups (deliberately deferred)

- **Tighten / extend the gate:** raise the `:shared` line bound (currently 98) as coverage stays high, and/or add a branch bound. Codecov's flag status could also be flipped from `informational` to a target if PR-based workflows are ever adopted.
- **Filters:** consider excluding generated/UI classes (`*.BuildConfig`, `*ComposableSingletons*`, `R`) if the `:app`/`:wear` numbers are ever gated. The three defensive `error()`-guard lines could be excluded too if the `:shared` line gate is ever pushed to ~100%.
