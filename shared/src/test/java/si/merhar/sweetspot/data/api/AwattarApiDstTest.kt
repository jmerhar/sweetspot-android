package si.merhar.sweetspot.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

/**
 * Tests for [AwattarApi.parse] around DST transition boundaries.
 *
 * Europe/Vienna follows the same rules as Europe/Berlin (CET/CEST):
 * - Spring forward: last Sunday of March, 02:00 → 03:00 (CET → CEST)
 * - Fall back: last Sunday of October, 03:00 → 02:00 (CEST → CET)
 */
class AwattarApiDstTest {

    private val api = AwattarApi("AT")
    private val timeZone = ZoneId.of("Europe/Vienna")

    @Test
    fun `winter time CET converts correctly`() {
        // 2025-01-15T10:00:00Z = CET 11:00 (UTC+1)
        val json = """
        {
            "data": [
                {"start_timestamp": 1736935200000, "end_timestamp": 1736938800000, "marketprice": 50.0}
            ]
        }
        """.trimIndent()

        val prices = api.parse(json, timeZone)
        assertEquals(11, prices[0].time.hour)
        assertEquals("+01:00", prices[0].time.offset.toString())
    }

    @Test
    fun `summer time CEST converts correctly`() {
        // 2025-07-15T10:00:00Z = CEST 12:00 (UTC+2)
        val json = """
        {
            "data": [
                {"start_timestamp": 1752573600000, "end_timestamp": 1752577200000, "marketprice": 50.0}
            ]
        }
        """.trimIndent()

        val prices = api.parse(json, timeZone)
        assertEquals(12, prices[0].time.hour)
        assertEquals("+02:00", prices[0].time.offset.toString())
    }

    @Test
    fun `spring forward night produces no duplicate local times`() {
        // 2025-03-30: clocks spring forward at 02:00 → 03:00
        // UTC 00:00 = CET 01:00 (still CET)
        // UTC 01:00 = CEST 03:00 (02:00 is skipped)
        // UTC 02:00 = CEST 04:00
        val json = """
        {
            "data": [
                {"start_timestamp": 1743292800000, "end_timestamp": 1743296400000, "marketprice": 50.0},
                {"start_timestamp": 1743296400000, "end_timestamp": 1743300000000, "marketprice": 60.0},
                {"start_timestamp": 1743300000000, "end_timestamp": 1743303600000, "marketprice": 70.0}
            ]
        }
        """.trimIndent()

        val prices = api.parse(json, timeZone)
        assertEquals(3, prices.size)
        assertEquals(1, prices[0].time.hour)  // CET 01:00
        assertEquals(3, prices[1].time.hour)  // CEST 03:00 (02:00 skipped)
        assertEquals(4, prices[2].time.hour)  // CEST 04:00

        // All local times are unique
        val localHours = prices.map { it.time.hour }
        assertEquals(localHours.size, localHours.distinct().size)
    }

    @Test
    fun `fall back night produces two entries with same local hour but different offsets`() {
        // 2025-10-26: clocks fall back at 03:00 → 02:00
        // UTC 00:00 = CEST 02:00 (+02:00)
        // UTC 01:00 = CET 02:00 (+01:00) — same local hour, different offset
        // UTC 02:00 = CET 03:00
        val json = """
        {
            "data": [
                {"start_timestamp": 1761436800000, "end_timestamp": 1761440400000, "marketprice": 50.0},
                {"start_timestamp": 1761440400000, "end_timestamp": 1761444000000, "marketprice": 60.0},
                {"start_timestamp": 1761444000000, "end_timestamp": 1761447600000, "marketprice": 70.0}
            ]
        }
        """.trimIndent()

        val prices = api.parse(json, timeZone)
        assertEquals(3, prices.size)

        // Both show 02:00 local but with different offsets
        assertEquals(2, prices[0].time.hour)
        assertEquals("+02:00", prices[0].time.offset.toString())  // CEST
        assertEquals(2, prices[1].time.hour)
        assertEquals("+01:00", prices[1].time.offset.toString())  // CET
        assertEquals(3, prices[2].time.hour)

        // Despite same local hour, they are distinct instants and sort correctly
        assertTrue(prices[0].time.toEpochSecond() < prices[1].time.toEpochSecond())
    }

    @Test
    fun `prices spanning DST transition sort chronologically`() {
        // Prices crossing spring forward boundary
        val json = """
        {
            "data": [
                {"start_timestamp": 1743289200000, "end_timestamp": 1743292800000, "marketprice": 40.0},
                {"start_timestamp": 1743292800000, "end_timestamp": 1743296400000, "marketprice": 50.0},
                {"start_timestamp": 1743296400000, "end_timestamp": 1743300000000, "marketprice": 60.0},
                {"start_timestamp": 1743300000000, "end_timestamp": 1743303600000, "marketprice": 70.0}
            ]
        }
        """.trimIndent()

        val prices = api.parse(json, timeZone)
        assertEquals(4, prices.size)

        // Verify chronological order
        for (i in 1 until prices.size) {
            assertTrue(
                "Price at index $i should be after index ${i - 1}",
                prices[i].time.toEpochSecond() > prices[i - 1].time.toEpochSecond()
            )
        }
    }
}
