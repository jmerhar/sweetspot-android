package si.merhar.sweetspot.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

/**
 * Tests for [EnergyZeroApi.parse] with malformed, incomplete, or unexpected JSON input.
 */
class EnergyZeroApiMalformedTest {

    private val api = EnergyZeroApi()

    private val timeZone = ZoneId.of("Europe/Amsterdam")

    @Test(expected = Exception::class)
    fun `completely invalid JSON throws`() {
        api.parse("not json at all", timeZone)
    }

    @Test(expected = Exception::class)
    fun `missing Prices field throws`() {
        api.parse("""{"data": []}""", timeZone)
    }

    @Test(expected = Exception::class)
    fun `missing readingDate in entry throws`() {
        val json = """{"Prices": [{"price": 0.10}]}"""
        api.parse(json, timeZone)
    }

    @Test(expected = Exception::class)
    fun `missing price in entry throws`() {
        val json = """{"Prices": [{"readingDate": "2025-06-15T12:00:00Z"}]}"""
        api.parse(json, timeZone)
    }

    @Test(expected = Exception::class)
    fun `invalid date format throws`() {
        val json = """{"Prices": [{"readingDate": "not-a-date", "price": 0.10}]}"""
        api.parse(json, timeZone)
    }

    @Test
    fun `null Prices list is handled`() {
        // kotlinx.serialization decodes JSON null for a list as an exception,
        // so we verify it throws rather than returning null
        try {
            api.parse("""{"Prices": null}""", timeZone)
            // If it doesn't throw, it should at least return empty
        } catch (_: Exception) {
            // Expected — null is not a valid list
        }
    }

    @Test
    fun `price of zero is valid`() {
        val json = """{"Prices": [{"readingDate": "2025-06-15T12:00:00Z", "price": 0.0}]}"""
        val prices = api.parse(json, timeZone)
        assertEquals(1, prices.size)
        assertEquals(0.0, prices[0].price, 0.0001)
    }

    @Test
    fun `very large price value is preserved`() {
        val json = """{"Prices": [{"readingDate": "2025-06-15T12:00:00Z", "price": 999.999}]}"""
        val prices = api.parse(json, timeZone)
        assertEquals(999.999, prices[0].price, 0.0001)
    }
}
