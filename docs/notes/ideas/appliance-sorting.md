# Appliance Sorting & Reordering

## Problem

Appliances (and EVs, which are appliances with an `EvSpec`) currently appear in the
order they were added — the stored list order, which both the home-screen chips and the
Settings › Appliances list read directly. With a dozen programmes plus a vehicle, that
insertion order stops matching how people actually reach for things: the most-used
programme can sit in the middle, and the EV is wherever it happened to be created.

There's no data problem to solve here — display order already *equals* list order, so
this is entirely about giving the user control over that order (and a way to derive it
automatically). No schema migration is needed for the ordering itself.

## The shape of the feature

A combination of three mechanisms:

- **Sort modes** — the user picks how the appliance list is ordered.
- **Multi-level sort** — a primary order, plus further orders used only to break ties,
  revealed progressively (to arbitrary depth) as collisions demand them.
- **Custom drag-to-reorder** — one of the modes; when chosen, manual drag handles appear
  and no further sort levels apply.

Plus a separate, orthogonal decision for how EVs merge into the appliance ordering on the
home screen, chosen on the EV settings screen.

## Sort modes

Six keys, each independently **ascending or descending**:

| Key           | Orders by                                   | Notes |
|---------------|---------------------------------------------|-------|
| **Custom**    | Manual position (drag-to-reorder)           | Terminal — see below. |
| **Frequency** | All-time tap count                          | New local counter; purgeable (see below). |
| **Recency**   | Last-tapped timestamp                       | New local timestamp. |
| **Name**      | Appliance name, locale-aware collation      | Rarely collides. |
| **Duration**  | Total minutes (`durationHours*60 + durationMinutes`) | EVs treated as longer than any appliance — see below. |
| **Type**      | Icon id (the `icon` string)                 | Collides heavily (many programmes share the washer icon). |

Frequency and Recency are split into two separate keys rather than one blended "usage"
signal, so they can be combined deliberately (e.g. primary Frequency, secondary Recency).

**Custom is terminal.** It defines an explicit per-item position, so it can't
meaningfully be a *tie-breaker* for another key, nor can another key break ties within it
(there are no ties — every position is distinct). Therefore Custom is never offered as a
secondary/tertiary level, and selecting it as primary hides all further levels and shows
drag handles instead. Every other mode is a *derived view* computed from the stored list;
it never mutates the stored (custom) order, so switching to a sort mode and back to Custom
leaves the manual arrangement intact.

### Multi-level sort with collision-gated disclosure

When the user picks a primary key, the app offers a **next** order *only if the criteria
so far leave collisions* — i.e. two or more appliances still compare equal under them. The
same rule gates every subsequent level, to **arbitrary depth**: each new level appears only
while ties remain. Once the applied criteria fully disambiguate every item, no further level
is offered. This gives a natural, self-terminating cutoff:

- **Type** (icon) collides a lot → a second level is offered. Example: primary **Type
  ascending**, secondary **Frequency descending** → programmes grouped by appliance icon,
  most-used first within each group. (This is the motivating example.)
- **Name** with all-distinct names → no collisions → no second level offered at all.
- If Type + Frequency still tied two items (same icon, same count), a third level (e.g.
  Recency, then Name) would be offered, and so on until nothing ties.

The chain is thus bounded twice over: by collisions (stop as soon as items are unique) and
by the pool of remaining keys. **Nonsensical combinations are excluded** — a key can't
appear twice in the chain, and **Custom** can't appear below the primary (nor can it be a
primary that then offers further levels). In practice the depth is small because most keys
disambiguate quickly, but there's no fixed cap.

## EVs: merge position & sectioning

EVs live on their own Settings screen (`EvSettingsScreen`); the Appliances screen already
shows `filterNot { it.isEv }`. The mixing only happens on the **home screen** chip flow.
So how EVs fold into the appliance order is a distinct choice, made on the EV screen, split
into two orthogonal controls:

**Position** — where the EVs go:

- **Interleaved** — EVs are sorted into the same stream by the *same active sort criteria*.
- **First** — EVs as a block ahead of appliances.
- **Last** — EVs as a block after appliances.

**Separate section** — an additional on/off option: render the EV block in its own
visually-distinct section (subtle divider/label) rather than flowing straight into the
appliance chips. It is only meaningful for a *block*, so it is **disabled when Interleaved
is selected** (there's no block to set apart). The position (First / Last) already answers
"before or after"; the Separate toggle only adds the sectioning treatment on top. So
"EVs in their own section before appliances" is *First + Separate*, and after is
*Last + Separate*.

Within the EV block (First or Last, with or without the Separate section), **EVs are always
ordered among themselves by name**, regardless of the active appliance sort. Having more
than two or three vehicles is a vanishing edge case, so there's no point threading the full
sort machinery through the EV block — name order is predictable and enough.

### Reconciling "interleaved" with Custom order

Earlier framing ("EVs sit wherever you drag them") was imprecise, and the objection is
correct: EVs are **not** part of the appliance drag list — they're on a separate screen —
so there's no manual position to interleave them at. **Interleaved is therefore undefined
under Custom** and must be disabled (greyed, with a hint) when the appliance sort mode is
Custom; Custom pairs only with First / Last / Separate.

Interleaved *does* work for the computed keys, because EVs also have a value for them:

- **Name / Frequency / Recency** — EVs have a name and get tapped, so they interleave
  cleanly on their own value.
- **Type** — a vehicle has no stored icon id (it always renders as a car, decided at
  display time), so under a Type-by-icon sort EVs share the same (empty) key and cluster
  together anyway — close to *Separate* in practice.
- **Duration** — an EV has no fixed duration (it's computed per-search from SoC), so **EVs
  are always treated as longer than any appliance** (duration key = +∞). That just makes
  sense — a car charge is the long job — so under Duration ascending EVs sort to the end,
  and descending to the front, deterministically.

If interleaving under Custom is ever genuinely wanted, the real fix is a *single combined
drag surface* (appliances + EVs in one reorderable list) rather than two split settings
screens — a larger change, out of scope for this iteration.

## Implementation sketch

### Data / persistence (`:shared`)

A small, serializable sort spec, persisted in `SettingsRepository` (SharedPreferences,
JSON) under a new `appliance_sort` key:

```kotlin
@Serializable
enum class SortKey { CUSTOM, FREQUENCY, RECENCY, NAME, DURATION, TYPE }

@Serializable
data class SortCriterion(val key: SortKey, val descending: Boolean = false)

// Primary first; empty or [CUSTOM] means "custom order".
@Serializable
data class ApplianceSort(val criteria: List<SortCriterion> = listOf(SortCriterion(SortKey.CUSTOM)))
```

- **Custom order** stays the stored list order itself (`getAppliances()`/`setAppliances()`
  reorder it) — the source of truth for Custom mode and the stable final tie-break for
  every derived sort. No separate order list needed.
- **EV merge** persists on the EV side as two values: a position
  `enum EvPosition { INTERLEAVED, FIRST, LAST }` (`ev_position`) and a boolean
  `ev_separate_section` (ignored/false under `INTERLEAVED`).
- **Usage tracking** is new local data feeding both the Frequency and Recency keys. Keep it
  *off* the `Appliance` model (which is synced to the watch and should stay pure) — store a
  separate map keyed by appliance id, e.g. `appliance_usage` →
  `{ id: { count: Int, lastUsedMs: Long } }`, updated in the ViewModel whenever a chip tap
  triggers a search (`count++`, `lastUsedMs = now`). Frequency sorts by all-time `count`;
  Recency sorts by `lastUsedMs`. The phone also stores the **last watch usage snapshot** and
  a **`usageResetToken`** for cross-device sync (see Watch). A **"purge frequency data"**
  settings action clears the phone map and the watch snapshot and bumps the reset token (a
  reset for when historical taps no longer reflect current habits).

### Sorting logic (pure, `:shared`, unit-tested)

Keep all decision logic out of Compose (per the repo's presentation-vs-logic rule):

```kotlin
/** Orders appliances per [sort]; stable, with stored order as the final tie-break. */
fun sortAppliances(
    appliances: List<Appliance>,
    sort: ApplianceSort,
    usage: Map<String, ApplianceUsage>,
): List<Appliance>

/** True if any two items compare equal under [criteria] — gates the next sort level. */
fun hasCollisions(
    appliances: List<Appliance>,
    criteria: List<SortCriterion>,
    usage: Map<String, ApplianceUsage>,
): Boolean

/** Folds EVs into the appliance ordering per [position]; sections them off if [separate]. */
fun mergeForHome(
    sortedAppliances: List<Appliance>,
    sortedEvs: List<Appliance>,        // always name-ordered
    position: EvPosition,
    separate: Boolean,                 // ignored when position == INTERLEAVED
): HomeApplianceLayout   // flat list, or two labelled sections when separate
```

`sortAppliances` builds a `Comparator` chain from `criteria` (respecting per-criterion
direction) and finishes with the original stored index, so a stable sort keeps Custom
order as the implicit last resort. `hasCollisions` powers the progressive-disclosure UI.
Both are trivially testable with fixed lists — no Android, no Robolectric.

### ViewModel (`:app`)

- Expose the sorted appliance list (and, for the home screen, the merged EV layout) in
  `UiState`, recomputed when appliances, sort spec, usage, or EV merge settings change.
- Increment phone usage on tap in `onApplianceDuration` / `onEvApplianceFind`; receive watch
  usage via the `/usage` bridge and fold it in with `combineUsage(phone, watch)` before
  sorting.
- New handlers: `onSetApplianceSort(ApplianceSort)`, `onMoveAppliance(from, to)` (Custom
  drag), `onSetEvPosition(EvPosition)`, `onSetEvSeparateSection(Boolean)` — each persists
  and re-syncs to the watch.

### Settings UI (`:app`)

- **Appliances screen** — a sort control at the top: primary key + direction; reveal a
  secondary row only when `hasCollisions(primary)` is true, tertiary only when
  primary+secondary still collide. Selecting **Custom** as primary hides the extra rows and
  turns the list into a drag-to-reorder list (long-press + drag; either a small
  reorderable-list dependency or a hand-rolled `detectDragGesturesAfterLongPress` + swap —
  the swap logic lives in the ViewModel/pure helper, not the composable).
- **EV screen** — a position selector (segmented control / radio rows:
  Interleaved / First / Last) plus a "Separate section" toggle. Two disable rules:
  **Interleaved** is disabled (with a hint) when the appliance sort is Custom; the
  **Separate section** toggle is disabled when the position is Interleaved.

### Home screen (`:app`)

Chips laid out per the merged layout. When the **Separate section** option is on (First or
Last position), render a subtle section break (or label) between vehicles and appliances;
otherwise the chips stay a single flat flow.

### Watch (`:wear`)

EVs are already filtered out of the wear sync, so for *ordering* the watch only needs the
sorted appliance list: the phone pushes the already-sorted list (watch stays dumb — no sort
spec on the watch). But watch taps must still feed the phone's Frequency/Recency counters
(see below), so the watch does track and report its own usage.

### Cross-device usage sync

A tap on the watch has to count toward the same Frequency/Recency as a phone tap, so the
watch reports usage back. This reuses the existing watch→phone Data Layer pattern (the
`/stats` path): add a `/usage` path handled the same way, behind a small bridge interface
like `WatchStatsBridge`, so the phone-side merge stays a unit-tested pure function and the
Data Layer plumbing stays excluded from coverage.

The robust shape is **cumulative snapshots, not deltas**:

- The watch keeps its *own* all-time usage map (`{ id: { count, lastUsedMs } }`),
  incremented on each watch tap, and after a tap pushes the **entire map** to `/usage` via
  `PutDataMapRequest`. Because the payload is the watch's cumulative total (not an
  increment), the Data Layer's replace-and-at-least-once delivery is naturally idempotent:
  the phone stores "latest known watch totals" and a re-delivered item changes nothing.
- The phone keeps its phone-side map and the last-received watch snapshot **separately**.
  Effective usage for sorting is a pure `combineUsage(phone, watch)`:
  `count = phone.count + watch.count`, `lastUsedMs = max(phone.lastUsedMs, watch.lastUsedMs)`,
  per id. Nothing merges into a shared counter, so nothing can double-count.
- **Purge** must reset both sides, or the next watch snapshot re-inflates the totals. The
  phone bumps a `usageResetToken` (an epoch stamp) carried in the existing `/settings`
  DataMap; the watch zeroes its local map when it sees a new token and stamps every `/usage`
  push with the token it last honoured. The phone ignores any snapshot carrying a stale
  token, closing the reset race, and on purge clears its own map and the stored watch
  snapshot too.

Deltas (watch pushes increments, clears on put-success, phone adds) are simpler but fragile
— a re-delivered or replayed delta double-counts, and a delta lost to a failed put is gone.
The cumulative snapshot avoids both, for the price of one small per-appliance map crossing
the wire (trivial payload). Clock skew between devices can make `max(lastUsedMs)` off by a
few seconds — irrelevant for ordering.

## Considerations

- **Default is Custom**, seeded from today's stored order — so existing users see no change
  until they opt into a sort mode, and "custom" starts as their current arrangement.
- **Derived sorts never destroy custom order** — switching away and back is lossless. Worth
  making obvious in the UI (e.g. Custom always available, drag re-enabled instantly).
- **Duration key + EVs** — EVs sort as +∞ (always the longest job). Deterministic, no null
  handling; cover it in tests so the sentinel doesn't regress.
- **Type = icon**, not appliance category — two unrelated programmes that happen to share
  an icon group together. Acceptable, but name it "Type (icon)" in the UI to set
  expectations; a future explicit category field could refine it.
- **Frequency / Recency are adaptive** — chips can move between sessions. That's expected
  for these modes (unlike making it the *only* behaviour); the purge action gives the user
  an escape hatch when old counts stop reflecting current habits.
- **Localisation** — the sort-mode labels, direction labels, merge-policy labels, and the
  purge-data action are new strings across all 25 languages.

## Resolved decisions

- **Sort depth** — arbitrary levels, gated by collisions and by the pool of remaining keys;
  no fixed cap on primary/secondary/tertiary.
- **Usage** — two distinct keys: **Frequency** (all-time count, with a purge action) and
  **Recency** (last-tapped time).
- **Duration + EVs** — EVs are always treated as longer than any appliance (+∞).
- **EV block order** — always by name; more than two or three vehicles is a vanishing edge
  case not worth further machinery.
- **EV merge = position + separate flag** — position is Interleaved / First / Last; a
  distinct "Separate section" toggle adds the sectioning treatment and is disabled under
  Interleaved.
- **Watch usage counts** — watch taps feed the phone's Frequency/Recency via a `/usage`
  Data Layer path using idempotent cumulative snapshots, combined on the phone as
  phone + watch (count) and max (recency).
