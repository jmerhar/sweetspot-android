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

Each module's own debug unit tests:

| Module | Line | Instruction | Branch | Class |
|---|---|---|---|---|
| **`:shared`** | **71.3%** | 64.2% | 48.2% | 83.6% |
| `:app` | 15.7% | 12.1% | 6.8% | 21.6% |
| `:wear` | 25.9% | 20.9% | 12.3% | 26.3% |

`:shared` (pure logic/parsing/algorithms) carries the real coverage; `:app`/`:wear` are mostly Compose UI that unit tests don't touch.

**Note on the `:shared` number.** A per-module report counts only coverage from that module's *own* tests. An earlier aggregated experiment showed `:shared` at ~85% line because it also counted `:shared` classes exercised by `:app`/`:wear` ViewModel tests (which call into shared repositories/finders). 71.3% is the honest "does `:shared`'s own suite cover `:shared`" figure — the right one to gate on.

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
                  bound { minValue = 70 } // set just under the :shared baseline
              }
          }
      }
  }
  ```
  Then `./gradlew :shared:koverVerifyDebug` in CI. Scope the gate to `:shared` so the low-signal UI coverage in `:app`/`:wear` doesn't dictate the threshold. (Alternatively, flip `codecov.yml`'s `informational: true` to a target once a baseline is trusted.)
- **Filters:** consider excluding generated/UI classes (`*.BuildConfig`, `*ComposableSingletons*`, `R`) if the `:app`/`:wear` numbers are ever gated.
