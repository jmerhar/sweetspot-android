package si.merhar.sweetspot.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

/**
 * Tests for [AwattarApi.parse] with valid JSON input.
 */
class AwattarApiParseTest {

    private val api = AwattarApi("AT")
    private val timeZone = ZoneId.of("Europe/Vienna")

    @Test
    fun `parses valid JSON into sorted PriceSlot list`() {
        val json = """
        {
            "data": [
                {"start_timestamp": 1735689600000, "end_timestamp": 1735693200000, "marketprice": 50.0},
                {"start_timestamp": 1735693200000, "end_timestamp": 1735696800000, "marketprice": 30.0},
                {"start_timestamp": 1735696800000, "end_timestamp": 1735700400000, "marketprice": 70.0}
            ]
        }
        """.trimIndent()

        val prices = api.parse(json, timeZone)

        assertEquals(3, prices.size)
        assertEquals(0.050, prices[0].price, 0.00001)
        assertEquals(0.030, prices[1].price, 0.00001)
        assertEquals(0.070, prices[2].price, 0.00001)
        assertEquals(timeZone, prices[0].time.zone)
    }

    @Test
    fun `parses empty data array`() {
        val json = """{"data": []}"""
        val prices = api.parse(json, timeZone)
        assertTrue(prices.isEmpty())
    }

    @Test
    fun `ignores unknown fields`() {
        val json = """
        {
            "object": "list",
            "data": [
                {"start_timestamp": 1735689600000, "end_timestamp": 1735693200000, "marketprice": 42.5, "unit": "Eur/MWh"}
            ],
            "url": "/at/v1/marketdata"
        }
        """.trimIndent()

        val prices = api.parse(json, timeZone)
        assertEquals(1, prices.size)
        assertEquals(0.0425, prices[0].price, 0.00001)
    }

    @Test
    fun `handles negative prices`() {
        val json = """
        {
            "data": [
                {"start_timestamp": 1735689600000, "end_timestamp": 1735693200000, "marketprice": -15.0}
            ]
        }
        """.trimIndent()

        val prices = api.parse(json, timeZone)
        assertEquals(-0.015, prices[0].price, 0.00001)
    }

    @Test
    fun `converts EUR per MWh to EUR per kWh`() {
        val json = """
        {
            "data": [
                {"start_timestamp": 1735689600000, "end_timestamp": 1735693200000, "marketprice": 1000.0}
            ]
        }
        """.trimIndent()

        val prices = api.parse(json, timeZone)
        assertEquals(1.0, prices[0].price, 0.00001)
    }

    @Test
    fun `converts UTC millisecond timestamps to configured timezone`() {
        // 1735689600000ms = 2025-01-01T00:00:00Z = 2025-01-01T01:00 CET (UTC+1)
        val json = """
        {
            "data": [
                {"start_timestamp": 1735689600000, "end_timestamp": 1735693200000, "marketprice": 50.0}
            ]
        }
        """.trimIndent()

        val prices = api.parse(json, timeZone)
        assertEquals(1, prices[0].time.hour)
        assertEquals("+01:00", prices[0].time.offset.toString())
    }

    @Test
    fun `computes durationMinutes from start and end timestamps`() {
        val json = """
        {
            "data": [
                {"start_timestamp": 1735689600000, "end_timestamp": 1735693200000, "marketprice": 50.0}
            ]
        }
        """.trimIndent()

        val prices = api.parse(json, timeZone)
        assertEquals(60, prices[0].durationMinutes)
    }

    @Test
    fun `handles price of zero`() {
        val json = """
        {
            "data": [
                {"start_timestamp": 1735689600000, "end_timestamp": 1735693200000, "marketprice": 0.0}
            ]
        }
        """.trimIndent()

        val prices = api.parse(json, timeZone)
        assertEquals(1, prices.size)
        assertEquals(0.0, prices[0].price, 0.00001)
    }

    @Test
    fun `sorts unsorted entries chronologically`() {
        val json = """
        {
            "data": [
                {"start_timestamp": 1735696800000, "end_timestamp": 1735700400000, "marketprice": 70.0},
                {"start_timestamp": 1735689600000, "end_timestamp": 1735693200000, "marketprice": 50.0},
                {"start_timestamp": 1735693200000, "end_timestamp": 1735696800000, "marketprice": 30.0}
            ]
        }
        """.trimIndent()

        val prices = api.parse(json, timeZone)
        assertEquals(3, prices.size)
        assertEquals(0.050, prices[0].price, 0.00001)
        assertEquals(0.030, prices[1].price, 0.00001)
        assertEquals(0.070, prices[2].price, 0.00001)
    }
}
