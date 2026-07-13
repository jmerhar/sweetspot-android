package today.sweetspot.util

import today.sweetspot.model.Appliance
import today.sweetspot.model.ApplianceSort
import today.sweetspot.model.ApplianceUsage
import today.sweetspot.model.EvPosition
import today.sweetspot.model.SortCriterion
import today.sweetspot.model.SortKey

/**
 * How the home screen should lay out appliance and vehicle chips, produced by [mergeForHome].
 */
sealed interface HomeChipLayout {
    /** A single flat run of chips (interleaved, or a First/Last block without a visual break). */
    data class Flat(val items: List<Appliance>) : HomeChipLayout

    /**
     * Two visually separated sections with a divider between them.
     *
     * @property first Chips in the top section.
     * @property second Chips in the bottom section.
     * @property vehiclesFirst Whether the [first] section holds the vehicles (else the appliances).
     */
    data class Sectioned(
        val first: List<Appliance>,
        val second: List<Appliance>,
        val vehiclesFirst: Boolean,
    ) : HomeChipLayout
}

/**
 * A comparator for one [SortKey]. Values are derived from the appliance and its [usage];
 * electric vehicles sort as the longest possible [SortKey.DURATION] (a car charge is the long
 * job) and carry no icon so they cluster under [SortKey.TYPE]. [SortKey.CUSTOM] is inert here —
 * custom order is handled by [sortAppliances] returning the stored order directly.
 */
private fun keyComparator(key: SortKey, usage: Map<String, ApplianceUsage>): Comparator<Appliance> =
    when (key) {
        SortKey.FREQUENCY -> compareBy { usage[it.id]?.count ?: 0 }
        SortKey.RECENCY -> compareBy { usage[it.id]?.lastUsedMs ?: 0L }
        SortKey.NAME -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
        SortKey.DURATION -> compareBy { if (it.isEv) Long.MAX_VALUE else it.durationHours * 60L + it.durationMinutes }
        SortKey.TYPE -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.icon ?: "" }
        SortKey.CUSTOM -> Comparator { _, _ -> 0 }
    }

/**
 * Chains [criteria] into a single comparator (primary first), applying each level's direction.
 * `CUSTOM` entries are skipped. Returns null when no orderable criterion remains.
 */
private fun buildComparator(
    criteria: List<SortCriterion>,
    usage: Map<String, ApplianceUsage>,
): Comparator<Appliance>? {
    var comp: Comparator<Appliance>? = null
    for (c in criteria) {
        if (c.key == SortKey.CUSTOM) continue
        val directed = keyComparator(c.key, usage).let { if (c.descending) it.reversed() else it }
        comp = comp?.then(directed) ?: directed
    }
    return comp
}

/**
 * Orders [appliances] per [sort]. The sort is stable, so the original (stored) order is the
 * implicit final tie-breaker — which is also exactly what [SortKey.CUSTOM] returns unchanged.
 *
 * @param appliances The appliances to order.
 * @param sort The active ordering.
 * @param usage Combined tap statistics feeding the Frequency/Recency keys.
 * @return The ordered list.
 */
fun sortAppliances(
    appliances: List<Appliance>,
    sort: ApplianceSort,
    usage: Map<String, ApplianceUsage>,
): List<Appliance> {
    if (sort.isCustom) return appliances
    val comp = buildComparator(sort.criteria, usage) ?: return appliances
    return appliances.sortedWith(comp)
}

/**
 * Whether [criteria] leave two or more appliances comparing equal — i.e. whether another
 * tie-breaker level would still change the order. Drives progressive disclosure of sort levels.
 *
 * @return false when fewer than two appliances, or when [criteria] carry no orderable key.
 */
fun hasCollisions(
    appliances: List<Appliance>,
    criteria: List<SortCriterion>,
    usage: Map<String, ApplianceUsage>,
): Boolean {
    if (appliances.size < 2) return false
    val comp = buildComparator(criteria, usage) ?: return false
    return appliances.sortedWith(comp).zipWithNext().any { (a, b) -> comp.compare(a, b) == 0 }
}

/**
 * The sort keys that may still be added as a tie-breaker below the given [criteria]: every key
 * except [SortKey.CUSTOM] (terminal) and those already used.
 */
fun nextAssignableKeys(criteria: List<SortCriterion>): List<SortKey> {
    val used = criteria.map { it.key }.toSet()
    return SortKey.entries.filter { it != SortKey.CUSTOM && it !in used }
}

// --- Pure edits driving the sort-control UI (kept here, not in the composable, so they are tested) ---

/**
 * The natural default direction for a freshly-picked [key]: descending for the usage keys, whose
 * labels ("Most used", "Recently used") imply high-to-low, and ascending for the rest (A→Z,
 * shortest-first, etc.). The user can still flip it afterwards.
 */
fun defaultDescending(key: SortKey): Boolean = key == SortKey.FREQUENCY || key == SortKey.RECENCY

/**
 * Sets the primary key with its default direction, discarding any tie-breakers (a new primary
 * starts a fresh chain, and [SortKey.CUSTOM] is terminal).
 */
fun ApplianceSort.withPrimary(key: SortKey): ApplianceSort =
    ApplianceSort(listOf(SortCriterion(key, defaultDescending(key))))

/** Replaces the key at [index], resetting that level to the new key's default direction. */
fun ApplianceSort.withLevelKey(index: Int, key: SortKey): ApplianceSort =
    ApplianceSort(criteria.mapIndexed { i, c -> if (i == index) SortCriterion(key, defaultDescending(key)) else c })

/** Flips the direction of the level at [index]. */
fun ApplianceSort.withToggledDirection(index: Int): ApplianceSort =
    ApplianceSort(criteria.mapIndexed { i, c -> if (i == index) c.copy(descending = !c.descending) else c })

/** Appends [key] as a new tie-breaker level with its default direction. */
fun ApplianceSort.withAddedTiebreaker(key: SortKey): ApplianceSort =
    ApplianceSort(criteria + SortCriterion(key, defaultDescending(key)))

/** Removes the level at [index] (used only for tie-breakers, never the primary). */
fun ApplianceSort.withoutLevel(index: Int): ApplianceSort =
    ApplianceSort(criteria.filterIndexed { i, _ -> i != index })

/**
 * Combines phone-local and watch-reported usage into effective totals: counts add, recency is
 * the more recent of the two. Idempotent with respect to re-delivered watch snapshots.
 */
fun combineUsage(
    phone: Map<String, ApplianceUsage>,
    watch: Map<String, ApplianceUsage>,
): Map<String, ApplianceUsage> =
    (phone.keys + watch.keys).associateWith { id ->
        val p = phone[id]
        val w = watch[id]
        ApplianceUsage(
            count = (p?.count ?: 0) + (w?.count ?: 0),
            lastUsedMs = maxOf(p?.lastUsedMs ?: 0L, w?.lastUsedMs ?: 0L),
        )
    }

/**
 * Folds vehicles into the appliance ordering for the home screen.
 *
 * Interleaved (only meaningful for a derived sort) orders vehicles into the same stream by the
 * active [sort]. For First/Last, appliances are sorted by [sort] while vehicles form a
 * name-ordered block placed before or after them; [separate] renders that block as its own
 * section. A Custom sort never interleaves (there is no manual position for a vehicle), so it
 * falls through to the block path as if Last.
 *
 * @param all All appliances, vehicles included.
 * @param sort The active appliance ordering.
 * @param usage Combined tap statistics.
 * @param position Where vehicles go relative to appliances.
 * @param separate Whether a First/Last vehicle block is drawn as its own section.
 * @return The chip layout to render.
 */
fun mergeForHome(
    all: List<Appliance>,
    sort: ApplianceSort,
    usage: Map<String, ApplianceUsage>,
    position: EvPosition,
    separate: Boolean,
): HomeChipLayout {
    if (position == EvPosition.INTERLEAVED && !sort.isCustom) {
        return HomeChipLayout.Flat(sortAppliances(all, sort, usage))
    }
    val apps = sortAppliances(all.filterNot { it.isEv }, sort, usage)
    val vehicles = all.filter { it.isEv }.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
    // With only one kind present there is nothing to separate — stay flat (avoids an empty section).
    if (vehicles.isEmpty()) return HomeChipLayout.Flat(apps)
    if (apps.isEmpty()) return HomeChipLayout.Flat(vehicles)
    val vehiclesFirst = position == EvPosition.FIRST
    return if (separate) {
        if (vehiclesFirst) HomeChipLayout.Sectioned(vehicles, apps, vehiclesFirst = true)
        else HomeChipLayout.Sectioned(apps, vehicles, vehiclesFirst = false)
    } else {
        HomeChipLayout.Flat(if (vehiclesFirst) vehicles + apps else apps + vehicles)
    }
}
