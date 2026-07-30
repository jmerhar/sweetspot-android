# Adversarial Review — `:wear` (Wear OS) module

Scope: `wear/src/main/java/today/sweetspot/wear/**` and its tests. Read-only.
Cross-referenced against `/tmp/sweetspot-review/inventory-app.md`, `:shared`
helpers (`CheapestWindowFinder`, `WindowResult`, `TimeUtils`), and the phone
`SweetSpotViewModel` for duplication/parity.

Overall: the module is small, cleanly layered, and the `WearSync` interface +
fake pattern is done well. The ViewModel is genuinely testable and most branches
are exercised. The most significant issue is a functional defect in the
advertised per-minute relative-time countdown, plus a latent NPE in `ResultScreen`.

---

## HIGH

### H1. The "updates every 60 seconds" relative-time countdown is defeated by `StateFlow` conflation — the countdown freezes
**Files:** `WearViewModel.kt:259-299` (`startResultRefresh`/`recalculateResult`),
`ui/ResultScreen.kt:26-33` (KDoc), `:72-73`, `:99`, `:133`; `model/WindowResult.kt:14` (data class);
inventory `inventory-app.md:79,206`.
**Confidence: High (mechanism), Medium (user visibility).**

`ResultScreen` computes `now = ZonedDateTime.now(timeZoneId)` at composition time
(`ResultScreen.kt:73`) and renders `formatRelative(result.startTime, now, …)` /
`formatRelative(result.endTime, now, …)`. There is **no ticker in `ResultScreen`**
— no `TimeText`, no `LaunchedEffect`, no minute clock. The only thing that can
recompute `now` is a new `WearUiState` emission from the ViewModel.

The 60s refresh (`startResultRefresh` → `recalculateResult`) re-runs
`findCheapestWindow` and does `_uiState.update { it.copy(result = result) }`
(`:298`). But `WindowResult` is a `data class` and `WearUiState` is a `data class`,
so `MutableStateFlow` conflates by `equals`: if the recomputed window has the same
start/end/cost/breakdown as the current one, **no emission occurs**, so
`ResultScreen` does not recompose and `now` is never refreshed.

In the common case — the cheapest window is in the *future* (e.g. "cheapest is
tonight, in 6h") — `buildWindowAt` does **not** clamp the start
(`CheapestWindowFinder.kt:147-149`), so `startTime`/`endTime` are fixed absolute
timestamps. As earlier slots elapse, `recalculateResult` filters `lastPrices`
(`:289-292`) but `findCheapestWindow` keeps returning the *same* window
(identical timestamps and cost). Result: `WindowResult` is value-equal every
minute → state conflated → no recomposition → the "in 6h 0m" text stays frozen
for hours, only jumping when the chosen window actually changes slot (roughly once
per elapsed slot for hourly data). This directly contradicts the `ResultScreen`
KDoc ("relative time indicators … that update every 60 seconds") and the inventory.

The countdown only updates smoothly in the narrow case where the cheapest window
is the *current* slot (start clamped to `now`, so it changes each minute) — the
opposite of the typical result.

**Fix direction:** drive the relative-time display off a per-minute time source in
the composable (e.g. a `LaunchedEffect` tick that updates a `now` state), or make
`recalculateResult` emit a monotonically-changing field (e.g. a refresh counter /
`now` timestamp on `WearUiState`) so the countdown recomposes even when the window
is unchanged. No test asserts the countdown actually advances, which is why this
slipped through (see C1).

---

## MEDIUM

### M1. `state.priceZone!!` in `ResultScreen` can NPE-crash when the phone nulls the zone while a result is on screen
**File:** `ui/ResultScreen.kt:72`. **Confidence: Medium.**

```kotlin
val timeZoneId = remember(state.priceZone) { ZoneId.of(state.priceZone!!.timeZoneId) }
```

This `!!` is reached only after `result != null` (guards at `:41-70`). At the
moment of the tap that produced the result, `priceZone` was non-null (enforced in
`onApplianceTapped`, `WearViewModel.kt:170-176`). But `priceZone` is *mutable
state* fed by the phone: `onSettingsReceived` sets `priceZone = resolveZone(…)`
which returns **`null`** for a multi-zone country with no explicit selection
(`WearViewModel.kt:320-329`, `resolveZone`). If, while the user is on the result
screen, the phone switches to a multi-zone country (or pushes a `/settings` update
that resolves to a null zone), the watch state becomes `result != null` **and**
`priceZone == null`, and the next recomposition of `ResultScreen` throws NPE →
app crash. The result screen still holds a valid `WindowResult` with its own
`startTime` carrying a zone, so the crash is avoidable — the timezone for
formatting could come from `result.startTime.zone` instead of re-deriving it from
`state.priceZone`. This is decision logic (`!!` risk) living in an excluded
composable; it should be guarded.

### M2. Disabled sources are silently ignored whenever the source order is the default (`null`)
**File:** `WearViewModel.kt:188`. **Confidence: Medium (parity), Low (whether it's a real defect).**

```kotlin
val enabledOrder = state.sourceOrder?.filter { it !in state.disabledSources }
```

When `sourceOrder == null` (the user never customised order — the default), the
whole expression is `null`, so `disabledSources` is **never applied** and
`defaultPriceFetcherFactory` uses the full zone default chain. A user who disabled
a source on the phone but kept the default ordering would still have that source
queried on the watch. Note: this is **byte-identical to the phone**
(`SweetSpotViewModel.kt:1643`), so it is a shared behaviour, likely intended
(disabling implies a custom order) rather than a wear-specific regression. Flag for
confirmation: if disabling is meant to be independent of ordering, both platforms
are wrong; the fix belongs in `:shared` (fold disabled-source filtering into
`defaultPriceFetcherFactory`/`defaultsForZone`) so it can't be forgotten per-caller.

### M3. Usage is recorded for taps that never fetch (no resolved zone)
**File:** `WearViewModel.kt:164` vs the early return at `:170-176`. **Confidence: Medium.**

`usageStore.record(appliance.id, …)` runs *first*, before the `priceZone == null`
guard returns with `wear_error_no_zone`. So tapping an appliance while the watch
has no resolved zone (multi-zone country, no selection) still increments the
all-time tap count that feeds the phone's Frequency/Recency sort keys — a tap that
produced nothing inflates usage. It also isn't pushed until the next real fetch (no
`syncUsageToPhone` on the no-zone path), so it silently lingers. Minor data-quality
issue; recording should arguably happen only once the fetch is actually attempted.

---

## LOW

### L1. `resolveZone` masks a stale/unknown `priceZoneId` and silently falls back to NL
**File:** `WearViewModel.kt:320-329`. **Confidence: Medium.**
If `priceZoneId` is non-null but unknown (`findPriceZoneById` returns null) and
`countryCode` is also null, the function falls through to
`Countries.defaultCountry().zones.first()` (NL). A watch carrying a stale zone id
after a zone-registry change would silently compute prices for the Netherlands
rather than surfacing "no zone". Low likelihood, but a silent wrong-country result
is worse than an error.

### L2. `ResultScreen` has no `TimeText`/`Scaffold`, unlike every other screen
**File:** `ui/ResultScreen.kt:77-82` vs `ApplianceListScreen.kt:54-57`,
`WearLockedScreen.kt:25-27`. **Confidence: High (factual), Low (impact).**
The result screen drops the ambient watch clock and position indicator that the
list and locked screens keep. Minor UX inconsistency; also removes the one thing
that would have forced a per-minute recomposition (compare H1).

### L3. `WearUiState.priceZone` default is a live call to `Countries.defaultCountry().zones.first()`
**File:** `WearViewModel.kt:59`. **Confidence: Low.**
Harmless today, but the default value of a `data class` property is evaluated per
instantiation; keeping app-model lookups in a UI-state default couples the state
type to the country registry. Cosmetic.

---

## Coverage gaps (gate ~93% line for `:wear`)

### C1. No test asserts the relative-time refresh actually re-emits state
The suite tests `recalculateResult` keeps a valid result and is a no-op with no
prior result (`WearViewModelTest.kt:240-256`), but nothing asserts that a refresh
after time passes produces an **observable** change for the UI. A test that taps,
advances the clock, and asserts a *new* `WearUiState` emission (or a changed
relative field) would have caught H1. **Recommended.**

### C2. `recalculateResult`'s "all future slots elapsed → result = null" branch is untested
**File:** `WearViewModel.kt:294-298` (`else null`). No test drives `futurePrices`
empty after filtering, so the branch that clears the result on the watch is
uncovered. (The `lastTimeZoneId ?: return` guard at `:286` is also only reached via
the empty-`lastPrices` early return, never the null-timezone case distinctly.)

### C3. Stats/usage push on the *error* path is executed but unasserted
**File:** `WearViewModel.kt:241-242` (catch block). The network-error test
(`:173-182`) runs the catch (so lines are covered) but doesn't assert that usage is
still pushed on failure — behaviour worth pinning since it's deliberate.

### C4. `usageResetToken` "older/equal token does NOT reset" is only covered incidentally
The explicit test covers the newer-token reset (`:415-423`); the negative case
(older/equal token must not wipe the store) is only exercised as a side effect of
other `onSettingsReceived(resetToken = 0)` calls. A direct assertion would harden
the monotonic-token contract described in `WearSettings` KDoc (`WearSync.kt:17-18`).

---

## Logic-in-excluded-classes audit (per `wear/build.gradle.kts` excludes)

- **`WearableSync.kt`** (excluded): thin. `deliver` (`:68-85`) is a `when` over the
  path plus `DataMap.getX` reads — pure Data Layer marshalling into the plain
  `WearSettings`, no decisions. Correct placement. ✅
- **`WearActivity.kt`** (excluded): navigation + `if (state.isLocked)` swap
  (`:36-54`) — presentation glue only. The `onDispose { onClearResult() }`
  (`:50-52`) is lifecycle wiring, not logic. ✅
- **`ApplianceListScreen` / `WearLockedScreen` / `WearTheme`** (excluded via
  `@Composable`): pure layout; formatting delegates to `:shared`
  (`formatDuration`, `applianceIconFor`, `UiText.resolve`). ✅
- **`ResultScreen`** (excluded): mostly presentation, but harbours two real
  concerns — the `state.priceZone!!` deref (M1) and the timezone derivation
  (`ZoneId.of(state.priceZone!!.timeZoneId)`, `:72`). The timezone for display
  could be taken from the already-computed `result.startTime.zone`, removing both
  the `!!` and the dependency on `priceZone` here. Borderline: it's derivation
  logic sitting in an excluded file.

No hidden branching/parsing/calculation was found in the excluded classes beyond
the `ResultScreen` deref above — the extraction discipline is otherwise sound.

## Duplication

- `resolveZone` (`WearViewModel.kt:320-329`) encodes "single-zone country → its
  only zone; multi-zone → null; nothing → default" — a small mapping not shared
  with the phone (the phone resolves zones through explicit `SettingsRepository`
  selection). Not a true duplication of an existing `:shared` helper, but if the
  phone ever needs the same "resolve from country+zoneId" fallback it should live
  in `Countries`. Low priority.
- Source-order/disabled filtering (`:188`) duplicates the phone's exact expression
  (`SweetSpotViewModel.kt:1643`) — see M2; candidate to push into
  `defaultPriceFetcherFactory`.

## Comment / KDoc correctness

- **`ResultScreen.kt:29-32`** — "relative time indicators … that update every 60
  seconds" is inaccurate given H1; the countdown does not reliably tick per minute.
- KDoc elsewhere is accurate and change-narrative-free (no forbidden
  "now/previously/used to"). `WearSettings`/`WearSync` KDoc correctly documents the
  reset-token contract. `WearViewModel` class KDoc matches behaviour.
- `WindowResult.kt:12` / `:19` still say "per hourly slot" though the finder is
  resolution-agnostic (15/30/60-min) — a `:shared` nit, out of `:wear` scope but
  noted.
