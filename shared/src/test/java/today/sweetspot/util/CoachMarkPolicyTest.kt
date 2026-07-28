package today.sweetspot.util

import today.sweetspot.model.CoachMark
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Verifies which contextual hint [CoachMarkPolicy] surfaces on each screen, in priority order. */
class CoachMarkPolicyTest {

    @Test
    fun `results priority is earlier-cheaper, then chart, then all-in`() {
        val none = emptySet<CoachMark>()
        assertEquals(
            CoachMark.EARLIER_CHEAPER,
            CoachMarkPolicy.resultsDue(none, hasAlternatives = true, allInConfigured = true, hasChart = true)
        )
        assertEquals(
            CoachMark.CHART_PRESS_HOLD,
            CoachMarkPolicy.resultsDue(
                setOf(CoachMark.EARLIER_CHEAPER), hasAlternatives = true, allInConfigured = true, hasChart = true
            )
        )
        assertEquals(
            CoachMark.ALL_IN_TOGGLE,
            CoachMarkPolicy.resultsDue(
                setOf(CoachMark.EARLIER_CHEAPER, CoachMark.CHART_PRESS_HOLD),
                hasAlternatives = true,
                allInConfigured = true,
                hasChart = true
            )
        )
    }

    @Test
    fun `earlier-cheaper hint is skipped when there is only one window`() {
        // No alternatives → the buttons are disabled, so the hint falls through to the next applicable one.
        assertEquals(
            CoachMark.CHART_PRESS_HOLD,
            CoachMarkPolicy.resultsDue(emptySet(), hasAlternatives = false, allInConfigured = false, hasChart = true)
        )
        // Nothing left when there are also no other applicable hints.
        assertNull(
            CoachMarkPolicy.resultsDue(emptySet(), hasAlternatives = false, allInConfigured = false, hasChart = false)
        )
    }

    @Test
    fun `chart hint is skipped when there is no chart`() {
        assertEquals(
            CoachMark.EARLIER_CHEAPER,
            CoachMarkPolicy.resultsDue(emptySet(), hasAlternatives = true, allInConfigured = false, hasChart = false)
        )
        assertNull(
            CoachMarkPolicy.resultsDue(
                setOf(CoachMark.EARLIER_CHEAPER), hasAlternatives = true, allInConfigured = false, hasChart = false
            )
        )
    }

    @Test
    fun `all-in hint appears only when configured`() {
        val seen = setOf(CoachMark.EARLIER_CHEAPER, CoachMark.CHART_PRESS_HOLD)
        assertNull(CoachMarkPolicy.resultsDue(seen, hasAlternatives = true, allInConfigured = false, hasChart = true))
        assertEquals(
            CoachMark.ALL_IN_TOGGLE,
            CoachMarkPolicy.resultsDue(seen, hasAlternatives = true, allInConfigured = true, hasChart = true)
        )
    }

    @Test
    fun `results returns null when every applicable hint is seen`() {
        val allSeen = setOf(CoachMark.EARLIER_CHEAPER, CoachMark.CHART_PRESS_HOLD, CoachMark.ALL_IN_TOGGLE)
        assertNull(CoachMarkPolicy.resultsDue(allSeen, hasAlternatives = true, allInConfigured = true, hasChart = true))
    }

    @Test
    fun `home hint requires an EV chip and being unseen`() {
        assertEquals(CoachMark.EV_CHIP, CoachMarkPolicy.homeDue(emptySet(), hasEvChip = true))
        assertNull(CoachMarkPolicy.homeDue(emptySet(), hasEvChip = false))
        assertNull(CoachMarkPolicy.homeDue(setOf(CoachMark.EV_CHIP), hasEvChip = true))
    }
}
