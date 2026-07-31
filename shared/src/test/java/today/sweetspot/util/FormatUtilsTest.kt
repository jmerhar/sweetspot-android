package today.sweetspot.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test


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

    @Test
    fun `formatPrice uses the given currency`() {
        // A non-EUR code should not render the euro symbol; USD renders '$' or the 'USD' code.
        val usd = formatPrice(0.0877, 4, "USD")
        assertTrue("Expected USD marker in '$usd'", usd.contains("$") || usd.contains("USD"))
    }

    @Test
    fun `formatPrice falls back to EUR for an unknown currency`() {
        val bad = formatPrice(0.0877, 4, "ZZZ")
        assertTrue("Expected EUR fallback in '$bad'", bad.contains("\u20ac") || bad.contains("EUR"))
    }

    // --- currencySymbol ---

    @Test
    fun `currencySymbol resolves known codes`() {
        // "\u20ac" in most locales; some render the "EUR" code \u2014 accept either.
        val sym = currencySymbol("EUR")
        assertTrue("Expected \u20ac or EUR, got '$sym'", sym == "\u20ac" || sym == "EUR")
    }

    @Test
    fun `currencySymbol falls back to the code when unknown`() {
        assertEquals("ZZZ", currencySymbol("ZZZ"))
    }

    // --- formatKw ---

    @Test
    fun `formatKw drops the decimal for whole numbers`() {
        assertEquals("11", formatKw(11.0))
        assertEquals("22", formatKw(22.0))
        assertEquals("0", formatKw(0.0))
    }

    @Test
    fun `formatKw keeps one decimal for fractional values`() {
        // Digit sequence check to stay locale-agnostic on the decimal separator.
        assertTrue("Expected '7' and '4' in '${formatKw(7.4)}'", formatKw(7.4).contains("7") && formatKw(7.4).contains("4"))
        assertEquals(3, formatKw(7.4).length) // "7.4" or "7,4"
    }

    // --- formatHhMm ---

    @Test
    fun `formatHhMm zero-pads hours and minutes`() {
        assertEquals("07:05", formatHhMm(7, 5))
        assertEquals("00:00", formatHhMm(0, 0))
        assertEquals("23:59", formatHhMm(23, 59))
    }

    @Test
    fun `parseDecimalInput accepts comma or dot and rejects partial input`() {
        assertEquals(0.15, parseDecimalInput("0,15")!!, 1e-9)
        assertEquals(0.15, parseDecimalInput("0.15")!!, 1e-9)
        assertEquals(1.0, parseDecimalInput("1")!!, 1e-9)
        assertNull(parseDecimalInput(""))
        assertNull(parseDecimalInput("abc"))
        assertNull(parseDecimalInput("-"))
    }
}
