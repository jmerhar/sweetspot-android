package today.sweetspot.charting

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import today.sweetspot.util.AllInBarSegments

/**
 * Pure colour logic for the price bar chart, kept out of the `@Composable` (which is excluded from
 * coverage) so it can be unit-tested. The chart passes in the theme-fixed bar colours; this object
 * decides how they are faded and which one a segment gets.
 */
object BarColors {

    // How far a non-window bar is faded toward the background (0 = untouched, 1 = fully background).
    // A dark background swallows a faded bar more readily than a light one, so the dark theme needs a
    // little less fade to reach the same separation.
    const val DIM_FRACTION_LIGHT = 0.35f
    const val DIM_FRACTION_DARK = 0.25f

    /**
     * Fades a bar colour toward the theme's background so a non-window bar recedes: lighten (toward
     * white) on a light theme, darken (toward black) on a dark theme. Window bars are left at full
     * strength, which is what makes them stand out.
     *
     * @param color The bar's full-strength colour.
     * @param dark Whether the app is in dark theme.
     */
    fun dim(color: Color, dark: Boolean): Color =
        lerp(color, if (dark) Color.Black else Color.White, if (dark) DIM_FRACTION_DARK else DIM_FRACTION_LIGHT)

    /**
     * Resolves an all-in segment [AllInBarSegments.Role] to its bar colour, faded via [dim] when the
     * bar is not part of the cheapest window.
     *
     * @param role Which price component the segment represents.
     * @param dimmed Whether this bar is outside the cheapest window (and so should recede).
     * @param dark Whether the app is in dark theme.
     */
    fun segmentColor(
        role: AllInBarSegments.Role,
        dimmed: Boolean,
        dark: Boolean,
        taxColor: Color,
        surchargeColor: Color,
        spotColor: Color,
        negativeSpotColor: Color
    ): Color {
        val base = when (role) {
            AllInBarSegments.Role.TAX -> taxColor
            AllInBarSegments.Role.SURCHARGE -> surchargeColor
            AllInBarSegments.Role.SPOT -> spotColor
            AllInBarSegments.Role.SPOT_NEGATIVE -> negativeSpotColor
        }
        return if (dimmed) dim(base, dark) else base
    }
}
