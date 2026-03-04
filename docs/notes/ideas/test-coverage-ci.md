# Test Coverage in CI

## Status: Idea

## Goal

Add code coverage reporting to the GitHub Actions CI pipeline so coverage is visible on PRs and regressions are caught early.

## Tool: Kover (recommended)

JetBrains Kover is a Kotlin-native coverage tool. Simpler than JaCoCo for Kotlin projects — single plugin, no XML config, better understanding of Kotlin constructs (inline functions, coroutines). Produces XML and HTML reports.

Alternative: JaCoCo (more mature, wider ecosystem, but more config overhead for Kotlin).

## Implementation Plan

1. Add `id("org.jetbrains.kotlinx.kover")` plugin to `:shared` (and optionally `:app`/`:wear`)
2. Add a `koverXmlReport` step to `.github/workflows/test.yml`
3. Use a GitHub Action (e.g. `mi-kas/kover-report` or `madrapps/jacoco-report`) to post coverage summary as a PR comment
4. Optionally add a coverage badge to README
5. Optionally set a minimum threshold (e.g. fail CI if `:shared` drops below 70%) — measure baseline first

## Scope

- **`:shared`** is the priority — pure logic, data parsing, algorithms. Most testable and most valuable to track.
- **`:app` and `:wear`** use Robolectric and mostly test ViewModel wiring. Coverage is slower and less meaningful. Can be added later.

## CI Changes

```yaml
# In .github/workflows/test.yml, replace or augment the test step:
- run: ./gradlew koverXmlReport
- uses: mi-kas/kover-report@v1
  with:
    path: shared/build/reports/kover/report.xml
    min-coverage-overall: 70  # optional threshold
```

## Considerations

- Kover adds ~5–10s to the test step (instrumentation overhead)
- HTML report can be uploaded as a build artifact for detailed exploration
- Coverage on `:shared` alone gives the best signal-to-noise ratio
