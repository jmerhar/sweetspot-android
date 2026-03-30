# Error Reporting

## Idea

Send non-API error reports (crashes, unexpected exceptions) to the home server for
collection and browsing. Complements the existing API reliability stats by capturing
app-level bugs that users might not bother to report via GitHub issues.

## Key challenges

### Obfuscated stack traces

Release builds use R8 (`isMinifyEnabled = true`), so stack traces from production look
like `at a.b.c(Unknown Source:12)`. Useless without the `mapping.txt` from that exact
build. Any solution needs:

- Archiving `mapping.txt` per release (the release script could automate this)
- A deobfuscation step — either server-side via `retrace` (bundled with Android SDK)
  or at browse-time by matching the report's app version to the correct mapping file

This is the core problem that Crashlytics, Sentry, and Bugsnag exist to solve.

### Crash-time reliability

`Thread.setDefaultUncaughtExceptionHandler` can intercept crashes, but the process is
dying — no time for a network call. Reports must be written to a local file and sent on
the **next** app launch. Risks:

- User uninstalls before next launch
- A crash-on-startup bug prevents reports from ever being sent
- Reports sit on disk indefinitely if the app isn't reopened

### Duplicate noise

One bug affecting many users = thousands of identical reports. The server needs
deduplication or clustering by stack trace fingerprint, otherwise the UI is a wall of
noise.

### Privacy

Stack traces contain class/method names (code-level, not personal), but exception
*messages* might contain user data: zone IDs, appliance names, file paths. Would need
message sanitisation or acceptance that some context leaks. Given the app's privacy
stance, this should be opt-in.

### What to capture

- **Uncaught exceptions (crashes)** — most valuable, but rare in a well-tested app
- **Caught exceptions** — currently silently swallowed in many places (Data Layer,
  cache, stats sync). Would need deliberate instrumentation at each catch site to
  decide which are worth reporting vs. expected failures

### Effort vs. value

For a single-developer app with 273 tests and no analytics SDK, the effort is
significant relative to the payoff. The API stats feature already captures the most
common failure mode (API errors). True crashes are rare and often reproducible from
user reports.

## Options to explore

### Option A: Android Vitals (Play Console)

- Free, automatic, no code changes needed
- Handles deobfuscation if you upload `mapping.txt` per release
- Clusters crashes by root cause
- **Pros:** zero infrastructure, already available once published on Play Store
- **Cons:** only covers Play Store installs, ~24h reporting delay, no custom error
  reporting (crashes only), limited filtering/search

### Option B: Firebase Crashlytics

- Free, real-time, handles deobfuscation and clustering automatically
- Gradle plugin uploads mapping files at build time
- Rich dashboard with affected user counts, device/OS breakdown, breadcrumbs
- **Pros:** best-in-class crash reporting, zero infrastructure, handles all the hard
  problems (obfuscation, dedup, clustering)
- **Cons:** adds Firebase/Google Play Services dependency (conflicts with the app's
  no-analytics philosophy, though Crashlytics is genuinely just crash reporting — no
  usage analytics). Increases APK size. Some users may object to any Google telemetry.

### Option C: Self-hosted Sentry

- Open source, full-featured error tracking with deobfuscation support
- Android SDK sends reports; server handles clustering, dedup, alerting
- **Pros:** full control, no third-party data sharing, rich UI, supports custom
  error reporting beyond just crashes
- **Cons:** heavy Docker deployment (PostgreSQL, Redis, Kafka, Snuba, etc.) —
  significant resource overhead on the home server for a single app. Ongoing
  maintenance burden. Overkill unless multiple projects use it.

## Decision

Pending. All three options are worth evaluating once the app is on the Play Store.
Android Vitals is the lowest-effort starting point (just upload mapping files).
Crashlytics is the pragmatic choice if the Firebase dependency is acceptable.
Self-hosted Sentry is the privacy-purist option but comes with infrastructure cost.
