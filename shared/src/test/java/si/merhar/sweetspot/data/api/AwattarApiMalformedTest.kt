package si.merhar.sweetspot.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

/**
 * Tests for [AwattarApi.parse] with malformed, incomplete, or unexpected JSON input.
 */
class AwattarApiMalformedTest {

    private val api = AwattarApi("AT")
    private val timeZone = ZoneId.of("Europe/Vienna")

    @Test(expected = Exception::class)
    fun `completely invalid JSON throws`() {
        api.parse("not json at all", timeZone)
    }

    @Test(expected = Exception::class)
    fun `array instead of object throws`() {
        api.parse("[1, 2, 3]", timeZone)
    }

    @Test(expected = Exception::class)
    fun `missing data field throws`() {
        api.parse("""{"object": "list"}""", timeZone)
    }

    @Test(expected = Exception::class)
    fun `empty string throws`() {
        api.parse("", timeZone)
    }

    @Test
    fun `very large price value is preserved`() {
        val json = """
        {
            "data": [
                {"start_timestamp": 1735689600000, "end_timestamp": 1735693200000, "marketprice": 999999.0}
            ]
        }
        """.trimIndent()

        val prices = api.parse(json, timeZone)
        assertEquals(999.999, prices[0].price, 0.001)
    }

    @Test
    fun `valid JSON with empty data array returns empty list`() {
        val json = """{"data": []}"""
        val prices = api.parse(json, timeZone)
        assertTrue(prices.isEmpty())
    }
}
