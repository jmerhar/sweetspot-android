package today.sweetspot.util

import today.sweetspot.model.CoachMark

/**
 * Pure decision for **which** contextual hint (if any) is due on a given screen.
 *
 * A hint is due only when it is applicable to what's on screen and has not yet been seen. At most one
 * hint is returned per call (the highest-priority applicable one), which is how the "one hint per
 * screen appearance" rule is enforced — the caller shows the returned hint, and once it's seen the next
 * appearance surfaces the next one. Keeping this out of Compose makes the ordering unit-testable.
 */
object CoachMarkPolicy {

    /**
     * The results screen's hint priority, most important first. The screen shows the first entry that
     * is both applicable (see [resultsDue]) and unseen.
     */
    private val RESULTS_PRIORITY = listOf(
        CoachMark.EARLIER_CHEAPER,
        CoachMark.CHART_PRESS_HOLD,
        CoachMark.ALL_IN_TOGGLE
    )

    /**
     * The contextual hint due on the results screen, or `null` if none.
     *
     * @param seen Hints already shown/acted on.
     * @param hasAlternatives Whether more than one window exists, so the Earlier/Cheaper buttons are
     *   actually usable (both are disabled when the cheapest window is also the earliest).
     * @param allInConfigured Whether the total ⇄ spot toggle is present (all-in set up for this zone).
     * @param hasChart Whether the price chart is shown (there are prices to plot).
     */
    fun resultsDue(
        seen: Set<CoachMark>,
        hasAlternatives: Boolean,
        allInConfigured: Boolean,
        hasChart: Boolean
    ): CoachMark? =
        RESULTS_PRIORITY.firstOrNull { mark ->
            mark !in seen && when (mark) {
                CoachMark.EARLIER_CHEAPER -> hasAlternatives
                CoachMark.CHART_PRESS_HOLD -> hasChart
                CoachMark.ALL_IN_TOGGLE -> allInConfigured
                CoachMark.EV_CHIP -> false // home-only, never surfaced on the results screen
            }
        }

    /**
     * The contextual hint due on the home screen, or `null` if none.
     *
     * @param seen Hints already shown/acted on.
     * @param hasEvChip Whether at least one vehicle chip is on the home screen.
     */
    fun homeDue(seen: Set<CoachMark>, hasEvChip: Boolean): CoachMark? =
        CoachMark.EV_CHIP.takeIf { hasEvChip && it !in seen }
}
