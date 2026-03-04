package si.merhar.sweetspot.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

class SpotHintaApiParseTest {

    private val api = SpotHintaApi("FI")
    private val timeZone = ZoneId.of("Europe/Helsinki")

    @Test
    fun `parses valid JSON into sorted PriceSlot list`() {
        val json = """
        [
            {"DateTime": "2025-06-15T16:00:00+03:00", "PriceNoTax": 0.25},
            {"DateTime": "2025-06-15T14:00:00+03:00", "PriceNoTax": 0.10},
            {"DateTime": "2025-06-15T15:00:00+03:00", "PriceNoTax": 0.20}
        ]
        """.trimIndent()

        val prices = api.parse(json, timeZone)

        assertEquals(3, prices.size)
        // Should be sorted chronologically
        assertEquals(0.10, prices[0].price, 0.0001)
        assertEquals(0.20, prices[1].price, 0.0001)
        assertEquals(0.25, prices[2].price, 0.0001)
        // Verify timezone
        assertEquals(timeZone, prices[0].time.zone)
    }

    @Test
    fun `parses empty array`() {
        val prices = api.parse("[]", timeZone)
        assertTrue(prices.isEmpty())
    }

    @Test
    fun `ignores unknown fields`() {
        val json = """
        [
            {
                "DateTime": "2025-06-15T14:00:00+03:00",
                "PriceNoTax": 0.15,
                "Rank": 5,
                "PriceWithTax": 0.186
            }
        ]
        """.trimIndent()

        val prices = api.parse(json, timeZone)
        assertEquals(1, prices.size)
        assertEquals(0.15, prices[0].price, 0.0001)
    }

    @Test
    fun `handles negative prices`() {
        val json = """
        [
            {"DateTime": "2025-06-15T14:00:00+03:00", "PriceNoTax": -0.05}
        ]
        """.trimIndent()

        val prices = api.parse(json, timeZone)
        assertEquals(-0.05, prices[0].price, 0.0001)
    }

    @Test
    fun `all slots have 15-minute duration`() {
        val json = """
        [
            {"DateTime": "2025-06-15T14:00:00+03:00", "PriceNoTax": 0.10},
            {"DateTime": "2025-06-15T14:15:00+03:00", "PriceNoTax": 0.11},
            {"DateTime": "2025-06-15T14:30:00+03:00", "PriceNoTax": 0.12}
        ]
        """.trimIndent()

        val prices = api.parse(json, timeZone)
        assertTrue(prices.all { it.durationMinutes == 15 })
    }

    @Test
    fun `converts UTC timestamps to configured timezone`() {
        val json = """
        [
            {"DateTime": "2025-06-15T10:00:00Z", "PriceNoTax": 0.10}
        ]
        """.trimIndent()

        val prices = api.parse(json, timeZone)
        // UTC 10:00 = Helsinki (EEST, UTC+3) 13:00
        assertEquals(13, prices[0].time.hour)
    }

    @Test
    fun `handles timestamps with timezone offset`() {
        val json = """
        [
            {"DateTime": "2025-01-15T14:00:00+02:00", "PriceNoTax": 0.10}
        ]
        """.trimIndent()

        val prices = api.parse(json, timeZone)
        // +02:00 14:00 = UTC 12:00 = Helsinki (EET, UTC+2) 14:00
        assertEquals(14, prices[0].time.hour)
        assertEquals("+02:00", prices[0].time.offset.toString())
    }
}
