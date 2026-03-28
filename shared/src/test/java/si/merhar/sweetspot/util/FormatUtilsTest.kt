package si.merhar.sweetspot.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class FormatUtilsTest {

    @Test
    fun `hours only`() {
        assertEquals("3h", formatDuration(3, 0))
    }

    @Test
    fun `minutes only`() {
        assertEquals("30m", formatDuration(0, 30))
    }

    @Test
    fun `hours and minutes`() {
        assertEquals("2h 30m", formatDuration(2, 30))
    }

    @Test
    fun `zero hours and zero minutes`() {
        assertEquals("0m", formatDuration(0, 0))
    }

    @Test
    fun `one hour`() {
        assertEquals("1h", formatDuration(1, 0))
    }

    @Test
    fun `five minutes`() {
        assertEquals("5m", formatDuration(0, 5))
    }

    @Test
    fun `24 hours`() {
        assertEquals("24h", formatDuration(24, 0))
    }

    @Test
    fun `1h 5m`() {
        assertEquals("1h 5m", formatDuration(1, 5))
    }

    @Test
    fun `formatPrice contains EUR symbol or code`() {
        val result = formatPrice(0.0877, 4)
        assertTrue(
            "Expected EUR symbol (\u20AC) or code (EUR) in '$result'",
            result.contains("\u20AC") || result.contains("EUR")
        )
    }

    @Test
    fun `formatPrice respects decimal count`() {
        val result3 = formatPrice(0.08765, 3)
        val result4 = formatPrice(0.08765, 4)
        // 3 decimals rounds to 0.088, 4 decimals rounds to 0.0877
        // Check the digit sequences are present (regardless of decimal separator)
        assertTrue("3-decimal result '$result3' should contain '088'", result3.contains("088"))
        assertTrue("4-decimal result '$result4' should contain '0877'", result4.contains("0877"))
    }

    @Test
    fun `formatPrice handles zero`() {
        val result = formatPrice(0.0, 4)
        assertTrue(
            "Expected EUR symbol or code in '$result'",
            result.contains("\u20AC") || result.contains("EUR")
        )
        assertTrue("Expected '0000' in '$result'", result.contains("0000"))
    }

    @Test
    fun `formatPrice handles negative prices`() {
        val result = formatPrice(-0.03, 4)
        assertTrue("Expected minus sign in '$result'", result.contains("-") || result.contains("\u2212"))
        assertTrue("Expected '0300' in '$result'", result.contains("0300"))
    }
}
