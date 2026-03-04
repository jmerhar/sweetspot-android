# API Reliability Statistics

## Idea

Collect anonymous, aggregated statistics about API reliability from all app instances.
For each country/zone/API combination, track success and failure counts plus error codes.
This data helps prioritize fallback API development, detect outages, and tune default
source ordering.

## What to collect

Per fetch attempt:

- **Zone** — country code + price zone ID (e.g. `NL`, `DE_LU`, `NO_1`)
- **Data source** — API identifier (e.g. `entsoe`, `energyzero`)
- **Outcome** — success or failure
- **Error type** (on failure) — HTTP status code, timeout, parse error, empty response, etc.
- **Timestamp** — day-level granularity is sufficient (no need for exact times)
- **App version** — to correlate issues with specific releases

Explicitly **not** collected: device ID, IP address, location, price data, usage patterns,
or anything that could identify a user.

## What this enables

- **Default source ordering** — if ENTSO-E fails 20% of the time for zone X but a
  fallback API is rock solid, adjust the defaults.
- **Outage detection** — sudden spike in failures for a zone/API = likely outage.
  Could even power a status page.
- **Prioritizing new APIs** — if a zone has high failure rates and no fallback,
  that's where adding a new API helps the most.
- **Deprecation decisions** — if nobody is using a particular API (or it's always
  failing), consider removing it.

## Open question: where to send the data?

The app has no backend server. Options for collecting stats without running (and paying
for) infrastructure:

### Option A: GitHub Issues / Discussions (free, hacky)

- App posts stats as a GitHub API call (create an issue or discussion comment).
- Requires a GitHub token embedded in the app (bad — token would be public in the APK)
  or a proxy.
- Not really viable without a server in front of it.

### Option B: Google Sheets via Google Forms (free)

- Create a Google Form with fields matching the stats schema.
- App submits stats by POSTing to the form's submission URL (no auth needed).
- Data lands in a Google Sheet for analysis.
- **Pros:** truly free, no server, no auth, Google Sheets is a decent analysis tool.
- **Cons:** fragile (form URL/field IDs can change), Google could rate-limit or block
  automated submissions, not designed for this use case. Hard to batch — each submission
  is a single form entry.

### Option C: Firebase Analytics / Crashlytics (free tier)

- Firebase Analytics free tier is generous (unlimited events, 500 distinct event types).
- Log a custom event per fetch attempt with zone, source, outcome, and error as parameters.
- BigQuery export (free up to 1 TB/month) for deeper analysis.
- **Pros:** purpose-built, robust, free tier is more than enough, good dashboard.
- **Cons:** adds a Google Play Services / Firebase dependency (currently the app has none).
  Increases APK size. Users who distrust Google telemetry may object (even though it's
  anonymous). Firebase on Wear OS is possible but adds complexity.

### Option D: Lightweight cloud function + free database

- A single cloud function (Cloudflare Workers free tier: 100K requests/day, or AWS Lambda
  free tier: 1M requests/month) that accepts a JSON payload and writes to a free database
  (Cloudflare D1, Turso, PlanetScale free tier, or even a Cloudflare KV counter).
- **Pros:** purpose-built, full control, tiny operational surface, free tiers are generous.
- **Cons:** it's a server — needs to be written, deployed, and occasionally maintained.
  Domain/routing setup. But it's truly minimal (one endpoint, append-only writes).

### Option E: Batched local collection + optional manual export

- App collects stats locally (simple counters in SharedPreferences or a small file).
- Periodically (e.g. weekly) shows a non-intrusive prompt: "Share anonymous API stats
  to help improve the app?" with a one-tap share action.
- Share action could open an email compose intent, or copy a compact stats string to
  clipboard, or POST to one of the above endpoints.
- **Pros:** fully transparent, user-initiated, no background telemetry, no server needed
  for the passive collection phase.
- **Cons:** low participation rate. Self-selected sample (only engaged users share).
  Manual process doesn't scale.

### Option F: Aggregate in Play Store vitals / Android vitals

- Not really feasible — Play vitals tracks crashes and ANRs, not custom business metrics.

### Recommendation

**Option D (cloud function)** is the cleanest solution if we accept minimal infrastructure.
Cloudflare Workers + D1 is truly free for this scale, takes an afternoon to set up, and
gives full control over the schema and analysis.

**Option C (Firebase)** is the pragmatic choice if we're willing to add the dependency.
It's zero infrastructure and the analytics dashboard is excellent. But it's a philosophical
shift for an app that currently has zero Google service dependencies beyond the platform.

**Option E (local + manual export)** is a good starting point that requires no
infrastructure at all. Collect the data locally from day one, and decide later how to
aggregate it. Even without centralized collection, users could share their stats in
GitHub issues when reporting API problems.

A phased approach might work best:
1. **Phase 1:** Instrument fetch attempts locally (counters in SharedPreferences).
   Surface them in a "debug info" or "about" screen. Zero infrastructure.
2. **Phase 2:** Add a voluntary export mechanism (copy to clipboard, share intent).
   Users can paste stats into GitHub issues.
3. **Phase 3:** If the app grows enough to justify it, add a lightweight cloud endpoint
   and opt-in automatic reporting.

## Data schema (local collection)

```
Key: "{zone}:{source}" (e.g. "NL:entsoe", "NL:energyzero", "DE_LU:entsoe")

Per key:
  successCount: Int
  failureCount: Int
  lastSuccess: LocalDate?
  lastFailure: LocalDate?
  errorCounts: Map<String, Int>   // e.g. {"HTTP_401": 3, "TIMEOUT": 1, "PARSE_ERROR": 2}
```

Compact enough to store in SharedPreferences as a JSON blob. Reset or roll over monthly
to keep the data fresh and bounded.

## Considerations

- **Privacy** — even anonymous stats can be sensitive. Be transparent about what's
  collected. If automatic reporting is ever added, make it opt-in with a clear explanation.
- **Wear OS** — the watch fetches prices independently, so it should collect stats too.
  But reporting from the watch is harder (no direct internet on some models). Could sync
  stats to the phone via Data Layer and let the phone handle reporting.
- **Error taxonomy** — need a consistent set of error categories across all APIs:
  HTTP status codes (401, 403, 429, 500, 503), timeout, DNS failure, parse error,
  empty response, no data for requested period. Map API-specific errors to these
  categories.
- **Rate limiting** — if automatic reporting is added, batch and deduplicate. One report
  per day per device is plenty. Don't let stats collection become a reliability problem
  itself.
