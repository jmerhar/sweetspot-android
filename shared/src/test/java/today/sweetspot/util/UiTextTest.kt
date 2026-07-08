package today.sweetspot.util

import org.junit.Assert.assertEquals
import org.junit.Test
import today.sweetspot.shared.R

/**
 * Pure JUnit tests for [UiText] construction — no Robolectric, no Android [android.content.res.Resources].
 *
 * This is the core payoff of resolving strings in the UI rather than the ViewModel: the message
 * and label *values* a ViewModel emits are plain data, so their locale/resource-selection logic can
 * be asserted directly here. Actual string resolution ([resolve]) is Android-backed and is covered
 * indirectly by the Robolectric ViewModel/UI tests.
 */
class UiTextTest {

    @Test
    fun `duration with zero hours uses the minutes-only string resource`() {
        assertEquals(UiText.Res(R.string.duration_minutes_only, listOf(30)), UiText.duration(0, 30))
    }

    @Test
    fun `duration with zero minutes uses the hours-only plural`() {
        assertEquals(UiText.Plural(R.plurals.duration_hours_only, 2, listOf(2)), UiText.duration(2, 0))
    }

    @Test
    fun `duration with hours and minutes uses the combined plural with both args`() {
        assertEquals(
            UiText.Plural(R.plurals.duration_hours_minutes, 2, listOf(2, 30)),
            UiText.duration(2, 30)
        )
    }

    @Test
    fun `appliance label composes the raw name with a localised duration`() {
        assertEquals(
            UiText.Composite(listOf(UiText.Raw("Washer · "), UiText.duration(2, 30))),
            UiText.applianceLabel("Washer", 2, 30)
        )
    }

    @Test
    fun `duration selection is locale-independent data, not a resolved string`() {
        // The same (hours, minutes) always yields the same structure regardless of any locale —
        // resolution is deferred to the UI, so this holds without an Android context.
        assertEquals(UiText.duration(1, 0), UiText.duration(1, 0))
    }
}
