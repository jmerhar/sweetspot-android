package si.merhar.sweetspot.data.api

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId

/**
 * Tests for [SpotHintaApi.parse] with malformed, incomplete, or unexpected JSON input.
 */
class SpotHintaApiMalformedTest {

    private val api = SpotHintaApi("FI")
    private val timeZone = ZoneId.of("Europe/Helsinki")

    @Test(expected = Exception::class)
    fun `completely invalid JSON throws`() {
        api.parse("not json at all", timeZone)
    }

    @Test(expected = Exception::class)
    fun `JSON object instead of array throws`() {
        api.parse("""{"DateTime": "2025-06-15T14:00:00+03:00", "PriceNoTax": 0.10}""", timeZone)
    }

    @Test(expected = Exception::class)
    fun `missing DateTime throws`() {
        api.parse("""[{"PriceNoTax": 0.10}]""", timeZone)
    }

    @Test(expected = Exception::class)
    fun `missing PriceNoTax throws`() {
        api.parse("""[{"DateTime": "2025-06-15T14:00:00+03:00"}]""", timeZone)
    }

    @Test(expected = Exception::class)
    fun `invalid date format throws`() {
        api.parse("""[{"DateTime": "not-a-date", "PriceNoTax": 0.10}]""", timeZone)
    }

    @Test
    fun `price of zero is valid`() {
        val json = """[{"DateTime": "2025-06-15T14:00:00+03:00", "PriceNoTax": 0.0}]"""
        val prices = api.parse(json, timeZone)
        assertEquals(1, prices.size)
        assertEquals(0.0, prices[0].price, 0.0001)
    }

    @Test
    fun `very large price value is preserved`() {
        val json = """[{"DateTime": "2025-06-15T14:00:00+03:00", "PriceNoTax": 999.999}]"""
        val prices = api.parse(json, timeZone)
        assertEquals(999.999, prices[0].price, 0.0001)
    }
}
