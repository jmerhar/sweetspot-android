package today.sweetspot.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

/**
 * Tests for [EnergyChartsApi.parse] with malformed, incomplete, or unexpected JSON input.
 */
class EnergyChartsApiMalformedTest {

    private val api = EnergyChartsApi("DE_LU")
    private val timeZone = ZoneId.of("Europe/Berlin")

    @Test(expected = Exception::class)
    fun `completely invalid JSON throws`() {
        api.parse("not json at all", timeZone)
    }

    @Test(expected = Exception::class)
    fun `array instead of object throws`() {
        api.parse("[1, 2, 3]", timeZone)
    }

    @Test(expected = Exception::class)
    fun `missing unix_seconds throws`() {
        api.parse("""{"price": [50.0]}""", timeZone)
    }

    @Test(expected = Exception::class)
    fun `missing price array throws`() {
        api.parse("""{"unix_seconds": [1735689600]}""", timeZone)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `mismatched array lengths throws`() {
        api.parse("""{"unix_seconds": [1735689600, 1735690500], "price": [50.0]}""", timeZone)
    }

    @Test
    fun `price of zero is valid`() {
        val json = """{"unix_seconds": [1735689600], "price": [0.0]}"""
        val prices = api.parse(json, timeZone)
        assertEquals(1, prices.size)
        assertEquals(0.0, prices[0].price, 0.00001)
    }

    @Test
    fun `very large price value is preserved`() {
        val json = """{"unix_seconds": [1735689600], "price": [999999.0]}"""
        val prices = api.parse(json, timeZone)
        assertEquals(999.999, prices[0].price, 0.001)
    }

    @Test
    fun `all null prices returns empty list`() {
        val json = """{"unix_seconds": [1735689600, 1735690500], "price": [null, null]}"""
        val prices = api.parse(json, timeZone)
        assertTrue(prices.isEmpty())
    }
}
