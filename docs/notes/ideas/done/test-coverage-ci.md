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

## Baseline (at implementation)

Each module's own debug unit tests. `:shared` was subsequently raised from its initial 71.3% by a targeted gap-closing pass (see below):

| Module | Line | Instruction | Branch | Class |
|---|---|---|---|---|
| **`:shared`** | **99.6%** | 99.1% | 88.8% | 98.0% |
| `:app` | 15.7% | 12.1% | 6.8% | 21.6% |
| `:wear` | 25.9% | 20.9% | 12.3% | 26.3% |

`:shared` (pure logic/parsing/algorithms) carries the real coverage; `:app`/`:wear` are mostly Compose UI that unit tests don't touch.

**Note on the `:shared` number.** A per-module report counts only coverage from that module's *own* tests. An earlier aggregated experiment showed `:shared` higher because it also counted `:shared` classes exercised by `:app`/`:wear` ViewModel tests (which call into shared repositories/finders). The per-module figure is the honest "does `:shared`'s own suite cover `:shared`" number — the right one to gate on.

### `:shared` gap-closing pass

Before considering a gate, the real gaps were addressed (71.3% → 97.2% line):

- **Added Robolectric to `:shared`** (previously pure JUnit) to test the `Context`-backed data classes.
- New tests: `FilePriceCacheTest` (v3 format/migration/corruption/cooldown → `data/cache` 16%→100%), `SettingsRepositoryTest` (trial/source-order/appliance/zone/timezone logic), `CountryDetectorTest` (detection fallback chain → `data/repository` 31%→93%), `PriceFetcherFactoryTest` (chain composition, was untested anywhere), `StatsRecordTest` (`data/stats`→100%), and `ApiHttpTest` (the five clients' HTTP paths → `data/api` 74%→99%).
- **Kover filters** exclude generated boilerplate (`@Serializable` types, `BuildConfig`) so the number reflects real logic — see `shared/build.gradle.kts`.
- To support real assertions: `FallbackPriceFetcher.fetchers` and `InstrumentedPriceFetcher.delegate`/`sourceId` were widened `private` → `internal`, and each API client gained an injectable `OkHttpClient` param (default `sharedHttpClient`) so a canned-response interceptor can drive `fetchRaw`/`fetchPrices` without a network.

A second pass then took `:shared` to **99.6% line / 88.8% branch**: added a `:shared` `UiTextResolveTest` (so `resolve()` is covered in its own module too), `ResourceFormattingTest` (the localised branches of `formatDuration`/`formatRelative`), more `SettingsRepositoryTest` cases (developer options, invalid-timezone fallback, auto-detect), a `CountryDetector` locale-region case, and ENTSO-E parser edge cases (missing price/resolution, A03 with no position 1, out-of-context tags).

The only remaining uncovered **lines** are three defensive `error(...)` guards that are unreachable in practice (`PriceFetcherFactory` unknown-source `else`, `EnergyChartsApi`/`AwattarApi` "no mapping for zone"). The remaining unhit **branches** are all defensive/unreachable too: null-safe `?.` sides (`listFiles()` etc.), the null-`TelephonyManager` path, compound short-circuits the upstream guards make unreachable, and Kotlin `when`-on-`String` hashCode artifacts. These aren't worth contriving tests for; if a gate ever needs a round number, exclude the `error()`-guard lines via a Kover filter.

## Decisions

- **Per-module reports** (not aggregated) — separate, persistent HTML per module, one Codecov flag each.
- **Surfacing:** Codecov (browsable web report + badge) plus a run-page summary table. No PR comment (this repo doesn't use PRs).
- **No gate yet** — baseline established first; Codecov status is `informational`.

## Follow-ups (deliberately deferred)

- **Gating:** add a rule to the target module(s) and run the verify task. In `shared/build.gradle.kts`:
  ```kotlin
  kover {
      reports {
          verify {
              rule {
                  bound { minValue = 90 } // set just under the :shared baseline (~91%)
              }
          }
      }
  }
  ```
  Then `./gradlew :shared:koverVerifyDebug` in CI. Scope the gate to `:shared` so the low-signal UI coverage in `:app`/`:wear` doesn't dictate the threshold. (Alternatively, flip `codecov.yml`'s `informational: true` to a target once a baseline is trusted.)
- **Filters:** consider excluding generated/UI classes (`*.BuildConfig`, `*ComposableSingletons*`, `R`) if the `:app`/`:wear` numbers are ever gated.
