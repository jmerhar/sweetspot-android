# Consecutive Appliance Scheduling

## Problem

Some appliances need to run back-to-back on cheap energy — e.g. a washing machine (3h)
followed by a dryer (2h). The user wants to program the washing machine's delayed start
so that *both* machines run during the cheapest 5-hour window.

The current app can't express this. If the user enters 5h, the result shows the optimal
start and end of the full window, but not the intermediate handoff time (when washing
ends and drying should begin). If they enter 3h for just the washer, the dryer might end
up running during expensive hours.

What's needed: find the cheapest contiguous window for the *total* duration, and show the
**transition times** between appliances — specifically the end time of the first appliance,
which is what the user programs into the washing machine.

## Solutions

### Solution 1: Appliance Chain (multi-select sequence)

Let the user select multiple appliances in order to form a chain. The app sums their
durations, finds the cheapest window for the total, and shows all transition times.

**Example result for "Washing Machine 3h → Dryer 2h":**

```
Start washing:   14:00 (in 2h 15m)
Start drying:    17:00 (in 5h 15m)
Done:            19:00 (in 7h 15m)
```

#### What changes

**Model** — new `ApplianceChain` data class in `:shared`:

```kotlin
@Serializable
data class ApplianceChain(
    val id: String,
    val name: String,
    val steps: List<ChainStep>,
    val icon: String = "link"
)

@Serializable
data class ChainStep(
    val applianceId: String,     // references Appliance.id
    val name: String,            // denormalized for display
    val durationHours: Int,
    val durationMinutes: Int
)
```

**SettingsRepository** — new `getChains()` / `setChains()` backed by a `chains` JSON key
in SharedPreferences, same pattern as appliances.

**CheapestWindowFinder** — no change. The algorithm already works with any duration. The
chain's total duration is passed as a single `durationHours: Double`, and the algorithm
returns one `WindowResult`.

**SweetSpotViewModel** — new `onChainTapped(chain)` handler:
1. Sum step durations into one `durationHours`.
2. Call `fetchAndFind()` with the total duration (reuses the existing fetch flow).
3. Compute transition times from `result.startTime` by accumulating step durations.
4. Store the chain's steps and transition times in `UiState` for the results screen.

**UiState** — add optional chain result data:

```kotlin
data class UiState(
    // ... existing fields ...
    val chainSteps: List<ChainStepResult>? = null  // null for single-appliance results
)

data class ChainStepResult(
    val name: String,
    val startTime: ZonedDateTime,
    val endTime: ZonedDateTime
)
```

**DurationInput UI** — add chain chips alongside appliance chips, visually
distinguished (e.g. with a link icon or "→" between step names).

**ResultScreen UI** — when `chainSteps` is not null, show a timeline of transition
times instead of (or above) the single start/end pair. Each step gets its own row
with start time, end time, and relative "in Xh Ym" display.

**Settings UI** — new "Chains" section (or "Sequences") in AppliancesSection. A chain
editor dialog where the user picks appliances from a list, reorders them, and names the
chain. Requires at least 2 steps.

**Data Layer sync** — push chains to the watch alongside appliances via a new `/chains`
path, same JSON pattern. WearViewModel reads and displays chain chips on the appliance
list screen.

**Watch UI** — chain chips appear in the appliance list. Tapping works identically to
phone: sum durations, fetch, compute transitions, show in a scrollable result screen.

#### Considerations

- Transition time computation is trivial — just accumulate step durations from the
  window start. No algorithm changes needed.
- The breakdown table and bar chart work unchanged — they show the full window's
  per-slot costs.
- The cost disclaimer ("per 1 kW") becomes less precise since the two appliances may
  have different power ratings. If the power rating feature is added later, each step
  could have its own cost subtotal.
- Chain names could be auto-generated ("Washing Machine → Dryer") or user-defined.

---

### Solution 2: "Followed by" field on appliances

Add an optional "followed by" link to the `Appliance` model. When the user taps
"Washing Machine" and it has `followedBy = Dryer`, the app automatically extends the
search to cover both durations and shows transition times.

#### What changes

**Appliance model** — add one field:

```kotlin
@Serializable
data class Appliance(
    // ... existing fields ...
    val followedById: String? = null  // ID of the next appliance, or null
)
```

**SweetSpotViewModel.onApplianceDuration()** — when the tapped appliance has a
`followedById`, resolve the linked appliance, sum durations, and store both for the
result. Can follow links recursively for chains of 3+ (A → B → C).

```kotlin
fun onApplianceDuration(appliance: Appliance) {
    val chain = buildChain(appliance)  // follow followedById links
    val totalHours = chain.sumOf { it.totalMinutes } / 60.0
    // ... set duration, label, fetch
}
```

**UiState** — same `chainSteps` addition as Solution 1.

**ResultScreen UI** — same transition-time timeline as Solution 1.

**Settings UI** — add a "Followed by" dropdown to the appliance editor dialog, listing
other appliances (or "None"). Show a preview of the chain below the dropdown.

**Data Layer sync** — no new paths needed. Appliances already sync as JSON; the new
field serializes automatically. The watch resolves `followedById` from its local copy
of the appliance list.

#### Considerations

- Simpler than Solution 1 — no new data structure, just one optional field.
- Circular reference detection needed: A → B → A must be rejected in the editor.
- Deleting an appliance that's referenced by `followedById` must clear the link.
- Less flexible than explicit chains — the link is always active. If the user sometimes
  wants just washing without drying, they'd need a separate "Washing (solo)" appliance
  or a way to ignore the link.
- On the watch, the chip for "Washing Machine" would implicitly include the dryer.
  This could be confusing if the label doesn't indicate it. Auto-appending "→ Dryer"
  to the chip label helps.

---

### Solution 3: Split marker on the duration picker

Keep the single-duration flow but let the user annotate a split point. The result shows
the split time alongside start and end.

**Example:** user enters 5h total with a split at 3h:

```
Start:          14:00 (in 2h 15m)
── Split ──     17:00 (in 5h 15m)  ← program washing machine end time
End:            19:00 (in 7h 15m)
```

#### What changes

**UiState** — add optional split data:

```kotlin
data class UiState(
    // ... existing fields ...
    val splitAtHours: Int = 0,       // 0 = no split
    val splitAtMinutes: Int = 0
)
```

**DurationInput UI** — add a "Split" toggle or secondary duration input below the main
picker. When enabled, shows a second, smaller duration picker for the first segment.
Validates that split duration < total duration.

**SweetSpotViewModel** — no change to the fetch/find flow. After getting the
`WindowResult`, compute the split time:

```kotlin
val splitTime = result.startTime.plusHours(splitHours).plusMinutes(splitMinutes)
```

**ResultScreen UI** — when `splitAtHours > 0`, show three time cards instead of two:
Start, Split, End. The split card is visually distinct (different colour or divider).

**CheapestWindowFinder** — no change. The algorithm is unaware of splits.

**Settings / Appliance model** — no changes. Splits are ephemeral (per-search), not
persisted.

**Watch** — would need a way to input splits on the small screen, which is awkward.
Could skip watch support entirely or auto-derive splits from the chain features.

#### Considerations

- Simplest implementation — purely a UI annotation on existing results.
- No model changes, no persistence changes, no algorithm changes.
- But: the split is ephemeral. The user re-enters it every time. For a daily
  wash+dry routine, this is tedious compared to Solutions 1 or 2.
- The split concept is generic (not tied to appliances), which is both a strength
  (flexible) and a weakness (less intuitive — "what is a split?").
- Multiple splits would handle 3+ appliance chains but the UI gets cluttered quickly.

---

### Solution 4: "Split this window" from results

After finding the cheapest window, the results screen offers a "Split" action. The user
enters a sub-duration or picks an appliance, and the app overlays the transition time.

#### What changes

**ResultScreen UI** — add a "Split this window" button below the start/end time cards.
Tapping opens a bottom sheet or inline picker:
- Option A: enter a duration (hours + minutes) for the first segment.
- Option B: pick from existing appliances (auto-fills the duration).
The split time appears as a new time card between start and end.

**UiState** — add the computed split time:

```kotlin
data class UiState(
    // ... existing fields ...
    val splitTime: ZonedDateTime? = null
)
```

**SweetSpotViewModel** — new `onSplitWindow(hours, minutes)` that computes:

```kotlin
fun onSplitWindow(hours: Int, minutes: Int) {
    val result = _uiState.value.result ?: return
    val split = result.startTime.plusHours(hours.toLong()).plusMinutes(minutes.toLong())
    if (split >= result.endTime) { /* show error */ return }
    _uiState.update { it.copy(splitTime = split) }
}
```

**Everything else** — no changes. No model changes, no persistence, no algorithm changes.

**Watch** — the small screen makes a bottom sheet awkward. Could show a simplified
version: after results load, offer "Split at [appliance]?" if the user has appliances
whose duration is shorter than the result window.

#### Considerations

- Very low implementation cost — it's just a post-hoc UI annotation.
- Non-intrusive: the feature only appears after a search, so users who don't need it
  never see it.
- But: it's an extra step on every search. For a daily routine, Solution 1 or 2 is
  faster (one tap → full result with transitions).
- Discovery might be poor — users may not realize they can split results.
- The split is ephemeral and lost on back-navigation or refresh.

## Recommendation

**Solution 1 (Appliance Chain)** is the most complete and natural UX. Users already
think in terms of "wash then dry" — modelling this explicitly as a named chain makes
it a one-tap operation for daily routines. The implementation is the most involved but
the bulk of the work is UI (chain editor in settings, transition times in results). The
core algorithm and fetch pipeline need no changes — just sum the durations and compute
transition offsets from the result's start time.

**Solution 2** is a reasonable alternative if Solution 1 feels too heavy. It reuses the
existing appliance model with one extra field. The trade-off is less flexibility (the
link is always active) and the risk of confusing implicit behaviour.

**Solution 3 or 4** could ship as a quick interim feature while Solution 1 is being
designed. They require minimal code changes but the UX is weaker for repeated use.

A phased approach could work well:
1. Ship Solution 4 first (small, self-contained, useful immediately).
2. Build Solution 1 as the full feature (chain editor, watch support, persistence).
3. Remove or keep Solution 4 as a fallback for ad-hoc splits.
