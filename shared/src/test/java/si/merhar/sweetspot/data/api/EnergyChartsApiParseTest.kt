package si.merhar.sweetspot.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

/**
 * Tests for [EnergyChartsApi.parse] with valid JSON input.
 */
class EnergyChartsApiParseTest {

    private val api = EnergyChartsApi("DE_LU")
    private val timeZone = ZoneId.of("Europe/Berlin")

    @Test
    fun `parses valid JSON into sorted PriceSlot list`() {
        val json = """
        {
            "unix_seconds": [1735689600, 1735693200, 1735696800],
            "price": [50.0, 30.0, 70.0]
        }
        """.trimIndent()

        val prices = api.parse(json, timeZone)

        assertEquals(3, prices.size)
        // Sorted chronologically — prices in original order since timestamps are ascending
        assertEquals(0.050, prices[0].price, 0.00001)
        assertEquals(0.030, prices[1].price, 0.00001)
        assertEquals(0.070, prices[2].price, 0.00001)
        assertEquals(timeZone, prices[0].time.zone)
    }

    @Test
    fun `parses empty arrays`() {
        val json = """{"unix_seconds": [], "price": []}"""
        val prices = api.parse(json, timeZone)
        assertTrue(prices.isEmpty())
    }

    @Test
    fun `ignores unknown fields`() {
        val json = """
        {
            "license_info": "CC BY 4.0",
            "unix_seconds": [1735689600],
            "price": [42.5],
            "unit": "EUR / MWh",
            "deprecated": false
        }
        """.trimIndent()

        val prices = api.parse(json, timeZone)
        assertEquals(1, prices.size)
        assertEquals(0.0425, prices[0].price, 0.00001)
    }

    @Test
    fun `handles negative prices`() {
        val json = """{"unix_seconds": [1735689600], "price": [-15.0]}"""
        val prices = api.parse(json, timeZone)
        assertEquals(-0.015, prices[0].price, 0.00001)
    }

    @Test
    fun `filters out null price gaps`() {
        val json = """
        {
            "unix_seconds": [1735689600, 1735690500, 1735691400],
            "price": [50.0, null, 70.0]
        }
        """.trimIndent()

        val prices = api.parse(json, timeZone)
        assertEquals(2, prices.size)
        assertEquals(0.050, prices[0].price, 0.00001)
        assertEquals(0.070, prices[1].price, 0.00001)
    }

    @Test
    fun `detects 15-minute resolution from timestamp gaps`() {
        // 900-second gaps = 15 minutes
        val json = """
        {
            "unix_seconds": [1735689600, 1735690500, 1735691400],
            "price": [50.0, 60.0, 70.0]
        }
        """.trimIndent()

        val prices = api.parse(json, timeZone)
        assertTrue(prices.all { it.durationMinutes == 15 })
    }

    @Test
    fun `detects 60-minute resolution from timestamp gaps`() {
        // 3600-second gaps = 60 minutes (e.g. CH zone)
        val chApi = EnergyChartsApi("CH")
        val json = """
        {
            "unix_seconds": [1735689600, 1735693200, 1735696800],
            "price": [137.46, 136.85, 137.08]
        }
        """.trimIndent()

        val prices = chApi.parse(json, ZoneId.of("Europe/Zurich"))
        assertTrue(prices.all { it.durationMinutes == 60 })
    }

    @Test
    fun `converts EUR per MWh to EUR per kWh`() {
        val json = """{"unix_seconds": [1735689600], "price": [1000.0]}"""
        val prices = api.parse(json, timeZone)
        assertEquals(1.0, prices[0].price, 0.00001)
    }

    @Test
    fun `converts UTC timestamps to configured timezone`() {
        // 1735689600 = 2025-01-01T00:00:00Z = 2025-01-01T01:00 CET (UTC+1)
        val json = """{"unix_seconds": [1735689600], "price": [50.0]}"""
        val prices = api.parse(json, timeZone)
        assertEquals(1, prices[0].time.hour)
        assertEquals("+01:00", prices[0].time.offset.toString())
    }
}
