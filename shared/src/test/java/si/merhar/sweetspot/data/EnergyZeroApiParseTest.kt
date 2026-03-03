package si.merhar.sweetspot.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

class EnergyZeroApiParseTest {

    private val zone = ZoneId.of("Europe/Amsterdam")

    @Test
    fun `parses valid JSON into sorted HourlyPrice list`() {
        val json = """
        {
            "Prices": [
                {"readingDate": "2025-06-15T14:00:00Z", "price": 0.25},
                {"readingDate": "2025-06-15T12:00:00Z", "price": 0.10},
                {"readingDate": "2025-06-15T13:00:00Z", "price": 0.20}
            ]
        }
        """.trimIndent()

        val prices = EnergyZeroApi.parse(json, zone)

        assertEquals(3, prices.size)
        // Should be sorted chronologically
        assertEquals(0.10, prices[0].price, 0.0001)
        assertEquals(0.20, prices[1].price, 0.0001)
        assertEquals(0.25, prices[2].price, 0.0001)
        // Verify timezone conversion
        assertEquals(zone, prices[0].time.zone)
    }

    @Test
    fun `parses empty price list`() {
        val json = """{"Prices": []}"""
        val prices = EnergyZeroApi.parse(json, zone)
        assertTrue(prices.isEmpty())
    }

    @Test
    fun `ignores unknown fields in JSON`() {
        val json = """
        {
            "Prices": [
                {"readingDate": "2025-06-15T12:00:00Z", "price": 0.15, "extra": "ignored"}
            ],
            "totalEntries": 1
        }
        """.trimIndent()

        val prices = EnergyZeroApi.parse(json, zone)
        assertEquals(1, prices.size)
        assertEquals(0.15, prices[0].price, 0.0001)
    }

    @Test
    fun `handles negative prices`() {
        val json = """
        {
            "Prices": [
                {"readingDate": "2025-06-15T12:00:00Z", "price": -0.05}
            ]
        }
        """.trimIndent()

        val prices = EnergyZeroApi.parse(json, zone)
        assertEquals(-0.05, prices[0].price, 0.0001)
    }

    @Test
    fun `converts UTC to configured timezone`() {
        val json = """
        {
            "Prices": [
                {"readingDate": "2025-06-15T10:00:00Z", "price": 0.10}
            ]
        }
        """.trimIndent()

        val prices = EnergyZeroApi.parse(json, zone)
        // UTC 10:00 = Amsterdam (CEST, UTC+2) 12:00
        assertEquals(12, prices[0].time.hour)
    }
}
