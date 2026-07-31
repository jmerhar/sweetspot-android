# Adversarial Review — `:app` (phone) module

Scope: `app/src/main/java/today/sweetspot/**` (SweetSpotViewModel, repositories/SDK wrappers,
MainActivity, ui/ composables). Read-only. Ground truth: `/tmp/sweetspot-review/inventory-app.md`.

Overall: the ViewModel is large but disciplined — SDK boundaries are behind interfaces with fakes,
pure logic is pushed into `:shared` helpers, and the test suite (`SweetSpotViewModelTest`, 150+ cases)
exercises the hard branches (deadline default/soft-default, Earlier/Cheaper offset math, periodic
refresh window preservation, all-in gating + EUR guard, import modes, usage reset-token staleness,
outbox/reply-outbox retry + caps, coach-mark arm/retire, paywall rule, stats status strings). I found
**no Critical/High hard correctness bug**. Findings are one Medium concurrency race, one
Medium logic-duplication-in-excluded-class, and several Low doc/altitude issues.

---

## Remediation status (audit follow-up)

Legend: ✅ fixed · ⏳ not fixed (tracked) · 📋 later phase · ➖ won't fix · ❓ needs decision.

| Finding | Status | Where / note |
|---|---|---|
| M1 `fetchAndFind` cancellation race | ✅ | `dc32ece` |
| M2 EV formula duplicated in excluded dialog | ✅ | `3a4c821` (shared `EvCharging` + tests) |
| L1 `StatsReporter` "unlocked" KDoc | ✅ | `11098c7` (→ "subscribed") |
| L2 "above cheapest" cost wording | ✅ | `6cbd592` (EN → "more than the recommended time"), retranslated in the register pass |
| L3 minor logic in excluded composables | ✅ | `9708763` (UiState nav/cost props), `1edfb88` (BarFractions), `8c3cb34` (dateTimeOverrideMillis), `106030c` (parseDecimalInput) |

---

## MEDIUM

### M1 — `fetchAndFind` publishes results with no cancellation check; a superseded fetch can win (suspected)
`SweetSpotViewModel.fetchAndFind` (app/.../SweetSpotViewModel.kt:1640-1779), launched via
`fetchJob = viewModelScope.launch(ioDispatcher) { fetchAndFind(...) }` (e.g. :1624, :960, :1317, :1347).

- `fetchAndFind` is a **plain (non-suspend) function** with **zero cancellation cooperation**. Its only
  blocking point is `repository.getPrices()` (PriceRepository.kt:71 — a plain fn), which bottoms out in
  OkHttp `client.newCall(request).execute()` (EntsoeApi.kt:81, synchronous/blocking).
- The supersede pattern is `fetchJob?.cancel(); fetchJob = launch{...}`. But `Job.cancel()` on a
  coroutine that is blocked inside `.execute()` does nothing until that call returns — and after it
  returns there is no `ensureActive()`/`isActive` check before the unconditional
  `_uiState.update { ... }` (:1738) and `startResultRefresh()` (:1767).
- Failure scenario: tap appliance A, then quickly tap appliance B. B cancels A's job and starts its
  own. If A's network call was already in flight and resolves *after* B's, A's coroutine still runs its
  success update — clobbering B's result and calling `startResultRefresh()`, which cancels/replaces
  B's refresh loop. Because the success update at :1738 does **not** set `resultLabel`, the screen can
  show B's label (`resultLabel` set at tap time) with A's window/prices/chart — a visible mismatch that
  only self-corrects on the next action.
- Not caught by tests: `rapid onQuickDuration taps ...` (test :~) uses an instantly-returning
  `FakeFetcher` under `runTest`, so cancellation lands at deterministic points and the blocking-in-flight
  case never occurs.
- Fix: make `fetchAndFind` `suspend` (or take the `CoroutineScope`) and call
  `currentCoroutineContext().ensureActive()` immediately before each `_uiState.update`/`startResultRefresh`;
  ideally also wire coroutine cancellation to `Call.cancel()` so the in-flight request aborts.
Confidence: suspected (depends on overlapping real network timing; logic path is confirmed).

### M2 — EV charging-duration formula duplicated into a coverage-excluded `@Composable`
`SocDialog` (app/.../ui/components/EvChargingComponents.kt:120-121, 150) recomputes the charging time:
```
val power = minOf(acMaxPowerKw, homeChargerKw)
val valid = target > current && power > 0.0
val totalMinutes = max(1, ((target - current) / 100.0 * batteryKwh / power * 60).roundToInt())
```
This is the **same formula and validation** as `SweetSpotViewModel.onEvApplianceFind`
(SweetSpotViewModel.kt:914 `effectivePowerKw = minOf(spec.acMaxPowerKw, evHomeChargerKw)`, :924-925
`energyKwh = (targetSoc - currentSoc)/100.0 * batteryKwh; totalMinutes = Math.round(energyKwh/effectivePowerKw*60).toInt().coerceAtLeast(1)`).

Two problems, both flagged by project rules:
1. **Logic in an excluded class** — the dialog is `@Composable` (excluded from coverage per
   app/build.gradle.kts:14). The estimate the user sees before confirming is computed there, untested.
2. **Duplication** — there is no shared `:shared` helper (confirmed: no `evChargeMinutes`/charging
   helper anywhere; grep found only `AllInPricing`). If the model changes (e.g. add a charging-efficiency
   factor, a taper curve, or change rounding), the *displayed* estimate and the *actually-searched*
   duration silently diverge — the dialog says "3h 20m" while the search runs a different length.
Fix: extract a pure `fun evChargeMinutes(currentSoc, targetSoc, batteryKwh, powerKw): Int` (and the
effective-power `minOf`) into `:shared`, unit-test it, and call it from both the ViewModel and the dialog.
Confidence: confirmed.

---

## LOW

### L1 — Stale KDoc on `StatsReporter.statusProvider` names the *old* status string ("unlocked")
StatsReporter.kt:27 — `@param statusProvider Returns the current payment status ("trial", "unlocked",
or "expired")`. The app actually sends **"subscribed"** (SweetSpotViewModel.kt:443:
`settingsRepository.isUnlocked() -> "subscribed"`). CLAUDE.md's "Stats Backend" section documents that
this exact "unlocked"→"subscribed" migration silently broke ingestion (stale server whitelist → 400 →
app discards as corrupt). The code is correct; the KDoc is stale and points to the wrong value on a
known-landmine field — worth correcting so the next reader doesn't "restore" the old string.
Confidence: confirmed.

### L2 — `ResultSummary` cost-delta doc/label say "cheapest", but the anchor is the *recommended* window
`ResultSummary` KDoc (ResultSummary.kt:32-33): "Extra cost versus the cheapest window ... null (or
zero) when the cheapest window is displayed", and the string used is `result_cost_above_cheapest`
(:74). But `costDelta` is computed in SweetSpotScreen.kt:119 as
`state.recommendedCost?.let { state.result!!.totalCost - it }`, and `recommendedCost` is the **default
(deadline-meeting) window**, not necessarily the global cheapest (SweetSpotViewModel.kt:1748, and the
UiState KDoc at :216-219 says exactly this). With a "ready by" deadline the delta is vs a costlier
default window, so the "above cheapest" wording is inaccurate. Cosmetic/doc-level, but user-visible copy.
Confidence: confirmed.

### L3 — Small decision logic living in excluded `@Composable`s (altitude)
Per the project rule ("excluded classes must be thin glue, no if/when/calculation"), these are minor
violations — none are wrong today, but each is untestable where it sits:
- `SweetSpotScreen.kt:116-119` computes `canGoEarlier = windowOffset < windowAlternatives.size - 1`,
  `canGoCheaper = windowOffset > 0`, and the `costDelta` subtraction. These are trivial derivations the
  ViewModel could expose (and test) as `UiState` fields.
- `PriceBarChart.kt:134-138, 301, 319, 341` — the **single-colour** chart's fraction math
  (`zeroFraction`, `negFraction = abs(price)/abs(minPrice)`, `posFraction = price/maxPrice`) is inline,
  even though the all-in path's geometry *was* extracted to the tested `AllInBarSegments`. Inconsistent;
  the single-colour fractions could move to a pure helper too.
- `DeveloperSection.kt:217-222` — combines a picked UTC-midnight date with a wall-clock time in the
  user's timezone (`dateAtUtcMidnight.atTime(h, m).atZone(timeZoneId)`). Real date arithmetic in a
  composable, though dev-options-only so impact is minimal.
- `AllInSection.kt:139` — surcharge parse `text.replace(',', '.').toDoubleOrNull()`. Note the partial-
  input behaviour: a non-parseable but non-blank value (e.g. `"0."`, `"-"`) neither updates nor clears
  the stored surcharge, silently keeping the previous value while the field shows the new text. Edge-only.
Confidence: confirmed (as altitude issues; not functional bugs).

---

## Notes / verified-NOT-bugs (to save the next reviewer time)
- **Country-change clearing is correct**: `onCountrySelected` (VM:991) relies on
  `SettingsRepository.setCountryCode` (SettingsRepository.kt:114-122) to persist supplier/surcharge=null;
  the state copy mirrors it. No stale-persisted-supplier bug.
- **Deadline soft-default math is correct**: `deadlineDefaultOffset` (VM:1181) `indexOfFirst { !endTime.isAfter(deadline) }`
  is valid because `findWindowAlternatives` is cheapest→earliest with strictly-decreasing finish times,
  so deadline-meeting windows are a suffix and the first hit is the cheapest that fits. Unreachable
  (`<0`) → `ev_error_deadline_unreachable`; `recalculateResult` coerces `<0`→0 (cheapest overall).
- **Periodic refresh preserves the navigated window** by start-time match with a correct fallback
  (VM:1251-1255); the "keep last result when all slots elapse" path (VM:1242-1246) matches the inventory.
- **Outbox/reply-outbox retry** is atomic under `reportStoreMutex`, re-adds concurrently-queued
  newcomers (`filterNot { it in pending }`), and caps at 5 attempts (VM:1932-1963, 2063-2089). Correct.
- **Dev-unlock paywall suppression** works via `isTrialExpired()` returning false when dev-unlocked
  (SettingsRepository.kt:453-456); `shouldShowPaywall` recomputations are consistent.
- **SDK boundaries** (WatchStatsBridge/WearableStatsBridge, WatchUsageBridge/WearableUsageBridge,
  BillingRepository/PlayBillingRepository, StatsPoster/HttpStatsPoster, ReportSubmitter/HttpReportSubmitter)
  correctly follow the interface+fake pattern; the excluded impls hold no decision logic (response-code
  policy is the pure `reportOutcomeFor`/`FeedbackCodec.submitOutcomeFor`).
- **MainActivity** is genuine navigation glue only (state `when`, dialog composables, import-intent
  forwarding) — nothing to move.
