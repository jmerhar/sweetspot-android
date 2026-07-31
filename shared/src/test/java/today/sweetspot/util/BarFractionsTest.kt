package today.sweetspot.util

import org.junit.Assert.assertEquals
import org.junit.Test

/** Tests for [BarFractions] — the single-colour price chart's pure bar geometry. */
class BarFractionsTest {

    @Test
    fun `zeroFraction places zero within the range and clamps to keep both halves visible`() {
        assertEquals(0.5f, BarFractions.zeroFraction(minPrice = -1.0, maxPrice = 1.0), 1e-6f)
        // All-positive range: no negative half.
        assertEquals(0f, BarFractions.zeroFraction(minPrice = 0.1, maxPrice = 0.3), 1e-6f)
        // A tiny negative extent clamps up to 0.01 rather than 0.
        assertEquals(0.01f, BarFractions.zeroFraction(minPrice = -0.001, maxPrice = 1.0), 1e-6f)
    }

    @Test
    fun `positiveFraction scales by maxPrice and clamps`() {
        assertEquals(0.5f, BarFractions.positiveFraction(price = 5.0, maxPrice = 10.0), 1e-6f)
        assertEquals(1f, BarFractions.positiveFraction(price = 20.0, maxPrice = 10.0), 1e-6f) // clamped
        assertEquals(0f, BarFractions.positiveFraction(price = 5.0, maxPrice = 0.0), 1e-6f)  // guard
    }

    @Test
    fun `negativeFraction scales by the lowest price and guards a non-negative min`() {
        assertEquals(0.5f, BarFractions.negativeFraction(price = -2.0, minPrice = -4.0), 1e-6f)
        assertEquals(1f, BarFractions.negativeFraction(price = -8.0, minPrice = -4.0), 1e-6f) // clamped
        assertEquals(0f, BarFractions.negativeFraction(price = -2.0, minPrice = 0.0), 1e-6f)  // guard
    }
}
