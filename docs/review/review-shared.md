# Adversarial Code Review — `:shared` module (SweetSpot Android)

Read-only audit of `shared/src/main/java/today/sweetspot/` against the ground-truth
inventory (`/tmp/sweetspot-review/inventory-app.md`) and the module tests. Findings ranked by
severity; each is marked **confirmed** or **suspected** with a concrete scenario.

The module is, on the whole, strong: the sliding-window finder, all-in pricing/geometry, sorting,
and the API parsers are carefully written and unusually well tested (including DST, A03 gaps,
clamping, monotonicity, and negative-price cases). The findings below are the exceptions.

---

## High

### H1. Empty-but-successful fetch short-circuits the fallback chain (confirmed behaviour; medium user impact)
`FallbackPriceFetcher.fetchPrices` (`data/api/FallbackPriceFetcher.kt:34-44`) returns the first
result that does **not throw**. An upstream that responds HTTP 200 with a valid-but-empty body
(zero price points) is treated as success, so the chain stops and the remaining fallbacks are never
tried — even though a fallback source may have data.

This directly contradicts the instrumentation layer, which classifies an empty result as a
**failure worth recording**: `InstrumentedPriceFetcher.fetchPrices` records
`success=false, errorCategory="EMPTY"` for an empty list (`data/stats/InstrumentedPriceFetcher.kt:50-51`)
but still **returns** it rather than throwing. So the same empty response is "a failure" for stats
yet "a success" for fallback routing.

Failure scenario: a zone where ENTSO-E returns an empty A44 document (not an Acknowledgement error),
e.g. a window where day-ahead data isn't published for that domain but the endpoint still 200s with
no points. The user sees "not enough data" while Spot-Hinta / Energy-Charts / EnergyZero (which the
zone lists as fallbacks) would have returned prices.

Mitigating factor (why not Critical): ENTSO-E's *usual* no-data response is an
`Acknowledgement_MarketDocument` (reason 999), which `EntsoeApi.fetchRaw`/`parse` turn into an
`EntsoeException` (`data/api/EntsoeApi.kt:88-91,109-112`) — that *does* fall through. The empty-list
path is the narrower "200 + well-formed but empty" case.

Test gap: `FallbackPriceFetcherTest` only exercises throwing failures and non-empty successes
(`successFetcher` always returns `count>=3`); the empty-success short-circuit is never asserted, so
this behaviour could change silently in either direction. Recommend deciding the intended policy
(treat empty as a fall-through) and adding a test.

---

## Medium

### M2. `EnergyChartsApi` resolution auto-detect defaults a single-entry response to 60 min (confirmed; low-frequency)
`data/api/EnergyChartsApi.kt:109-113`: `durationMinutes` is derived from the gap between the first
two timestamps; a response with exactly one slot defaults to `60`. For a 15-minute zone that returns
a single trailing slot, the slot is mislabelled hourly, so the finder treats 15 minutes of price as
an hour of cost/coverage. Rare (needs a one-row response) but a silent resolution-mixing error rather
than a failure. `EnergyChartsApiParseTest` does not cover the single-entry branch.

### M3. Unknown tax `type` is silently dropped from the all-in price (confirmed; depends on feed)
`AllInPricing.marginal`/`components` (`util/AllInPricing.kt:82-107`) only sum `TYPE_PER_KWH` and
multiply `TYPE_PERCENTAGE`; any other `type` string contributes nothing. A feed component with a
typo'd/renamed type (`"per_kwh"`, `"vat"`, a future additive-percent hybrid) is discarded, under-
pricing the all-in figure with no error or warning surfaced. Display-only, so it can't change the
recommendation, but the shown "Total price" would be wrong. Since the feed is external
(`sweetspot.today/data/suppliers/<cc>.json`) and can evolve independently of the app, an unrecognised
type is a realistic drift. Consider asserting/warning on unknown types. No test covers a non-matching
type string.

### M4. Duplicated lenient-JSON instances and per-API boilerplate (confirmed; maintainability)
`private val json = Json { ignoreUnknownKeys = true }` is re-created in at least nine places:
`EnergyZeroApi`, `EnergyChartsApi`, `AwattarApi`, `SpotHintaApi`, `SetupShare`, `UsageSnapshot`,
`SettingsRepository`, `TariffRepository`, `EvVehicleRepository`, `FileUsageStore`. A single shared
`Json` (as `PriceFetcher.sharedHttpClient` already does for OkHttp) would remove the repetition and
guarantee consistent decoding config. The four JSON API classes also repeat the identical
`fetchRaw` → HTTP-check → `throw HttpException` → `parse` scaffold; only ENTSO-E meaningfully differs.
Not a bug, but the copy-paste is where inconsistency creeps in (e.g. one class forgetting
`ignoreUnknownKeys`).

---

## Low

### L5. A03 gap-fill stops at the last present position, not the period's end (suspected; low)
`EntsoeApi.fillA03Gaps` (`data/api/EntsoeApi.kt:232-242`) fills missing positions only up to
`maxPos = max(present positions)`. ENTSO-E A03 semantics carry the *last* published price forward to
the **end of the period**; if the final change is at position 90 of a 96-slot day, positions 91–96
are never emitted. Result: a few trailing slots missing. Low impact because the repository fetches
two days and only needs 12h coverage, and day-ahead A44 docs usually use A01 (all points present).
Confidence low — depends on whether ENTSO-E A03 responses ever omit trailing positions in practice.
`EntsoeApiParseTest` tests interior A03 gaps but not a trailing-position gap.

### L6. Cheapest-window selection ignores start clamping (confirmed; by-design, worth documenting)
`findBestStartIndex`/`computeWindowCost` (`util/CheapestWindowFinder.kt:180-226`) rank windows by
their **unclamped** cost (full first slot), while `buildWindowAt` later trims the first slot for a
window that starts before `now` (`:146-156`). So the window chosen as "cheapest" is selected on a
cost that differs from the cost ultimately displayed for that window. In practice this only affects
the earliest (past-started) window and never breaks the cheapest pick or the alternatives'
monotonic-cost invariant (the earlier-in-list windows always have a higher-priced first slot, so
trimming can't push a clamped window below its cheaper successor). Behaviour is fine; the KDoc claims
"cost increases monotonically along the list" without noting it's the *unclamped* ordering cost —
worth a one-line clarification so a future edit doesn't assume displayed costs are monotonic.

### L7. `formatKw` whole-number check misreads very large values (suspected; negligible)
`util/FormatUtils.kt:21-23`: `value == value.toLong().toDouble()`. For power ratings (≤ ~50 kW) this
is exact and fine; only pathological inputs (> 2^53, NaN) misbehave. Not reachable from real appliance
data — noted only for completeness.

### L8. `getPrices` re-fetch guard relies on the just-written cooldown timestamp (confirmed; correct but subtle)
`PriceRepository.getPrices` (`data/repository/PriceRepository.kt:85-110`): on a cold start with no
cache, the initial `fetchAndCache()` writes the cache and stamps `last_fetch_ms`; the subsequent
low-coverage re-fetch is then skipped because `isCooldownElapsed` is false. This is the intended
"no double fetch" outcome, but it's an implicit coupling (the cooldown write suppresses the second
branch) rather than an explicit guard — fragile if `write` ever stops stamping the timestamp. A test
pinning "cold start fetches exactly once even when coverage < 12h" would lock it down.

---

## Comment / KDoc correctness

### C9. Broken KDoc reference to `today.sweetspot.data.SettingsRepository` (confirmed)
`model/Appliance.kt:9` and `data/repository/SettingsRepository.kt:26` both reference
`today.sweetspot.data.SettingsRepository` (and `[today.sweetspot.data.SettingsRepository]`). The class
actually lives at `today.sweetspot.data.repository.SettingsRepository`, so the `@link`/reference does
not resolve. Two occurrences.

### C10. Doc drift vs. inventory: `ApplianceGrouping.ROWS/COLUMNS` under-described in project docs
Not a code defect, but confirming the inventory's drift note (`inventory-app.md:118-120,236`): the
`mergeForHome`/`groupForHome` grouping layer (`util/ApplianceSorting.kt:196-299`) implements
`ROWS`/`COLUMNS` side-by-side type grouping that `CLAUDE.md` doesn't describe (it documents only
`HomeChipLayout` Flat/Sectioned). The code and its tests (`ApplianceSortingTest`) are consistent and
thorough; only the top-level prose is stale.

---

## Notably solid (checked, no action needed)

- **`CheapestWindowFinder`** — fractional slots, 15/60-min resolution, now-clamping (incl. tail
  running past the last slot), and `findWindowAlternatives`' prefix-min monotonic construction are all
  correct and comprehensively tested (`CheapestWindowFinderTest`, 40+ cases).
- **`AllInPricing` / `AllInBarSegments`** — affine/monotonic transform preserves the cheapest window;
  negative-spot and below-zero-total (`xMin<0`, "getting paid") geometry is correct and tested,
  including the zero-reference shift.
- **ENTSO-E / Energy-Charts / aWATTar / Spot-Hinta / EnergyZero** — three-layer pattern followed;
  EUR/MWh→EUR/kWh conversions correct; DST handled via absolute instants; ENTSO-E dedup keeps the
  last (corrected) TimeSeries. ENTSO-E has DST + A03 + error-document tests folded into
  `EntsoeApiParseTest` (no separate `*DstTest`/`*MalformedTest` files, but the coverage is present).
- **`SetupShare`** — encode/decode round-trip, schema-version gating (`TooNew` read before full
  decode), and `mergeAppliances` re-mint-then-dedupe are sound.
- **`FilePriceCache` / `FileStatsCollector` / `FileUsageStore`** — version byte + graceful
  null-on-error migration; stats/usage stores are `synchronized`; usage reset-token flow is correct.
- **`SettingsRepository`** — trial/day math, null-removes-key convention, and malformed-JSON→default
  decoding are consistent.
