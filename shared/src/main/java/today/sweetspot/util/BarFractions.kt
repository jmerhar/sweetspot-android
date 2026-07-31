package today.sweetspot.util

import kotlin.math.abs

/**
 * Pure bar geometry for the single-colour (spot-only) price chart, the sibling of [AllInBarSegments]
 * for the stacked all-in chart. Each function returns a fraction in `0..1` that the chart Composable
 * applies as a fill or weight, keeping the ratio maths testable and out of the coverage-excluded
 * `PriceBarChart` Composable.
 */
object BarFractions {

    /**
     * Where zero falls within the `[minPrice, maxPrice]` range (0 = left edge, 1 = right edge), used
     * to split a bar into its negative (left) and positive (right) halves. Returns 0 when there are
     * no negative prices; otherwise clamped to `0.01..0.99` so both halves stay visible.
     */
    fun zeroFraction(minPrice: Double, maxPrice: Double): Float =
        if (minPrice < 0 && maxPrice > minPrice) {
            ((0.0 - minPrice) / (maxPrice - minPrice)).toFloat().coerceIn(0.01f, 0.99f)
        } else 0f

    /** Fraction of the positive axis a non-negative [price] fills, relative to [maxPrice] (0 if none). */
    fun positiveFraction(price: Double, maxPrice: Double): Float =
        if (maxPrice > 0) (price / maxPrice).toFloat().coerceIn(0f, 1f) else 0f

    /** Fraction of the negative axis a below-zero [price] fills, relative to the lowest [minPrice]. */
    fun negativeFraction(price: Double, minPrice: Double): Float =
        if (minPrice < 0) (abs(price) / abs(minPrice)).toFloat().coerceIn(0f, 1f) else 0f
}
