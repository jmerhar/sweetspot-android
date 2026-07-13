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

## EVs: merge policy

EVs live on their own Settings screen (`EvSettingsScreen`); the Appliances screen already
shows `filterNot { it.isEv }`. The mixing only happens on the **home screen** chip flow.
So how EVs fold into the appliance order is a distinct choice, made on the EV screen:

- **Interleaved** — EVs are sorted into the same stream by the *same active sort criteria*.
- **First** — EVs as a block ahead of appliances.
- **Last** — EVs as a block after appliances.
- **Separate** — EVs in their own section (subtle divider/label), appliances in theirs.

Within a block (First / Last / Separate — and to keep it simple, wherever EVs cluster),
**EVs are always ordered among themselves by name**, regardless of the active appliance
sort. Having more than two or three vehicles is a vanishing edge case, so there's no point
threading the full sort machinery through the EV block — name order is predictable and
enough.

### Reconciling "interleaved" with Custom order

Earlier framing ("EVs sit wherever you drag them") was imprecise, and the objection is
correct: EVs are **not** part of the appliance drag list — they're on a separate screen —
so there's no manual position to interleave them at. **Interleaved is therefore undefined
under Custom** and must be disabled (greyed, with a hint) when the appliance sort mode is
Custom; Custom pairs only with First / Last / Separate.

Interleaved *does* work for the computed keys, because EVs also have a value for them:

- **Name / Frequency / Recency** — EVs have a name and get tapped, so they interleave
  cleanly on their own value.
- **Type** — an EV's icon is the charger icon, so "interleaved by Type" naturally clusters
  EVs together anyway — close to *Separate* in practice.
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
- **Merge policy** persists on the EV side, e.g. `ev_merge_policy` →
  `enum EvMergePolicy { INTERLEAVED, FIRST, LAST, SEPARATE }`.
- **Usage tracking** is new local data feeding both the Frequency and Recency keys. Keep it
  *off* the `Appliance` model (which is synced to the watch and should stay pure) — store a
  separate map keyed by appliance id, e.g. `appliance_usage` →
  `{ id: { count: Int, lastUsedMs: Long } }`, updated in the ViewModel whenever a chip tap
  triggers a search (`count++`, `lastUsedMs = now`). Frequency sorts by all-time `count`;
  Recency sorts by `lastUsedMs`. A **"purge frequency data"** settings action clears the
  map (a reset for when historical taps no longer reflect current habits).

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

/** Folds EVs into the appliance ordering per [policy]. */
fun mergeForHome(
    sortedAppliances: List<Appliance>,
    sortedEvs: List<Appliance>,
    policy: EvMergePolicy,
): HomeApplianceLayout   // flat list, or sectioned for SEPARATE
```

`sortAppliances` builds a `Comparator` chain from `criteria` (respecting per-criterion
direction) and finishes with the original stored index, so a stable sort keeps Custom
order as the implicit last resort. `hasCollisions` powers the progressive-disclosure UI.
Both are trivially testable with fixed lists — no Android, no Robolectric.

### ViewModel (`:app`)

- Expose the sorted appliance list (and, for the home screen, the merged EV layout) in
  `UiState`, recomputed when appliances, sort spec, usage, or merge policy change.
- Increment usage on tap in `onApplianceDuration` / `onEvApplianceFind`.
- New handlers: `onSetApplianceSort(ApplianceSort)`, `onMoveAppliance(from, to)` (Custom
  drag), `onSetEvMergePolicy(EvMergePolicy)` — each persists and re-syncs to the watch.

### Settings UI (`:app`)

- **Appliances screen** — a sort control at the top: primary key + direction; reveal a
  secondary row only when `hasCollisions(primary)` is true, tertiary only when
  primary+secondary still collide. Selecting **Custom** as primary hides the extra rows and
  turns the list into a drag-to-reorder list (long-press + drag; either a small
  reorderable-list dependency or a hand-rolled `detectDragGesturesAfterLongPress` + swap —
  the swap logic lives in the ViewModel/pure helper, not the composable).
- **EV screen** — a merge-policy selector (segmented control / radio rows). The
  **Interleaved** option is disabled with a hint when the appliance sort is Custom.

### Home screen (`:app`)

Chips laid out per the merged layout. **Separate** policy renders a subtle section break
(or label) between vehicles and appliances; the other policies stay a single flat flow.

### Watch (`:wear`)

EVs are already filtered out of the wear sync, so the watch only needs the appliance order.
Simplest: the phone pushes the already-sorted appliance list (watch stays dumb — no sort
spec or usage logic on the watch). Watch-side taps wouldn't feed phone usage counts in this
version; note the limitation and revisit only if it matters.

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
