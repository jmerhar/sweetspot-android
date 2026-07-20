package today.sweetspot.charting

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import today.sweetspot.util.AllInBarSegments.Role
import org.junit.Test

/**
 * Verifies the pure colour logic extracted from the chart composable: theme-aware fading and the
 * role → colour mapping (with fading applied off-window).
 */
class BarColorsTest {

    private val tax = Color(0xFFE67E22)
    private val surcharge = Color(0xFF16A085)
    private val spot = Color(0xFF4A90D9)
    private val negative = Color(0xFF9B59B6)

    @Test
    fun `dim on dark theme moves the colour toward black`() {
        val d = BarColors.dim(spot, dark = true)
        assertNotEquals(spot, d)
        assertTrue(d.red <= spot.red && d.green <= spot.green && d.blue <= spot.blue)
    }

    @Test
    fun `dim on light theme moves the colour toward white`() {
        val d = BarColors.dim(spot, dark = false)
        assertNotEquals(spot, d)
        assertTrue(d.red >= spot.red && d.green >= spot.green && d.blue >= spot.blue)
    }

    @Test
    fun `dim differs between light and dark themes`() {
        assertNotEquals(BarColors.dim(spot, dark = true), BarColors.dim(spot, dark = false))
    }

    @Test
    fun `segmentColor maps each role to its colour at full strength when in the window`() {
        fun full(role: Role) = BarColors.segmentColor(role, dimmed = false, dark = false, tax, surcharge, spot, negative)
        assertEquals(tax, full(Role.TAX))
        assertEquals(surcharge, full(Role.SURCHARGE))
        assertEquals(spot, full(Role.SPOT))
        assertEquals(negative, full(Role.SPOT_NEGATIVE))
    }

    @Test
    fun `segmentColor fades the role colour when the bar is outside the window`() {
        assertEquals(
            BarColors.dim(tax, dark = true),
            BarColors.segmentColor(Role.TAX, dimmed = true, dark = true, tax, surcharge, spot, negative)
        )
        assertEquals(
            BarColors.dim(negative, dark = false),
            BarColors.segmentColor(Role.SPOT_NEGATIVE, dimmed = true, dark = false, tax, surcharge, spot, negative)
        )
    }
}
