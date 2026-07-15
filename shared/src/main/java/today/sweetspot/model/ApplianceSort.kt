package today.sweetspot.model

import kotlinx.serialization.Serializable

/**
 * A key by which the appliance list can be ordered.
 *
 * [CUSTOM] is a manual per-item order edited by drag-to-reorder; it is terminal — it defines
 * an explicit position, so it is never combined with other keys as a tie-breaker. The
 * remaining keys are derived and may be chained (see [ApplianceSort]).
 */
@Serializable
enum class SortKey { CUSTOM, FREQUENCY, RECENCY, NAME, DURATION, TYPE }

/**
 * One level of an ordering: a [key] and its direction.
 *
 * @property key The attribute to order by.
 * @property descending When true, order high-to-low (or Z-to-A); otherwise low-to-high.
 */
@Serializable
data class SortCriterion(val key: SortKey, val descending: Boolean = false)

/**
 * The user's chosen appliance ordering: a primary criterion plus optional tie-breakers.
 *
 * The first entry is the primary order; each subsequent entry breaks ties left by the ones
 * before it. `[SortCriterion(CUSTOM)]` (the default) means "manual order". A [CUSTOM] primary
 * never has tie-breakers.
 *
 * @property criteria Ordered list of criteria, primary first.
 */
@Serializable
data class ApplianceSort(
    val criteria: List<SortCriterion> = listOf(SortCriterion(SortKey.CUSTOM))
) {
    /** Whether this is the manual (drag-to-reorder) order. */
    val isCustom: Boolean get() = criteria.firstOrNull()?.key == SortKey.CUSTOM
}

/**
 * All-time tap statistics for a single appliance, feeding the [SortKey.FREQUENCY] and
 * [SortKey.RECENCY] orders.
 *
 * @property count Number of times the appliance has been tapped to run a search.
 * @property lastUsedMs Epoch millis of the most recent tap, or 0 if never tapped.
 */
@Serializable
data class ApplianceUsage(val count: Int = 0, val lastUsedMs: Long = 0)

/**
 * How electric vehicles are placed relative to ordinary appliances on the home screen.
 *
 * [INTERLEAVED] sorts vehicles into the same stream by the active sort criteria; [FIRST] and
 * [LAST] group them as a block before or after the appliances. Persisted by its [key].
 */
enum class EvPosition(val key: String) {
    INTERLEAVED("interleaved"),
    FIRST("first"),
    LAST("last");

    companion object {
        /** Resolves a stored [key] to its position, defaulting to [INTERLEAVED] for unknown/null. */
        fun fromKey(key: String?): EvPosition = entries.find { it.key == key } ?: INTERLEAVED
    }
}

/**
 * How home-screen appliance chips are visually grouped by type (their icon).
 *
 * [NONE] keeps a single continuous flow ordered by the active sort (with vehicles placed by
 * [EvPosition]). [ROWS] and [COLUMNS] instead cluster chips of the same type under a titled
 * heading — [ROWS] stacks the groups as full-width bands, [COLUMNS] lays them out side by side.
 * Grouping subsumes [EvPosition]: when active, electric vehicles form their own "Vehicles" group.
 * Persisted by its [key].
 */
enum class ApplianceGrouping(val key: String) {
    NONE("none"),
    ROWS("rows"),
    COLUMNS("columns");

    companion object {
        /** Resolves a stored [key] to its grouping, defaulting to [NONE] for unknown/null. */
        fun fromKey(key: String?): ApplianceGrouping = entries.find { it.key == key } ?: NONE
    }
}
