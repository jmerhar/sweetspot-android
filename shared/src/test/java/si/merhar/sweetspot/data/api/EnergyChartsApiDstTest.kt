package si.merhar.sweetspot.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

/**
 * Tests for [EnergyChartsApi.parse] around DST transition boundaries.
 *
 * Europe/Berlin transitions:
 * - Spring forward: last Sunday of March, 02:00 → 03:00 (CET → CEST)
 * - Fall back: last Sunday of October, 03:00 → 02:00 (CEST → CET)
 */
class EnergyChartsApiDstTest {

    private val api = EnergyChartsApi("DE-LU")
    private val timeZone = ZoneId.of("Europe/Berlin")

    @Test
    fun `winter time CET converts correctly`() {
        // 2025-01-15T10:00:00Z = CET 11:00 (UTC+1)
        val json = """{"unix_seconds": [1736935200], "price": [50.0]}"""

        val prices = api.parse(json, timeZone)
        assertEquals(11, prices[0].time.hour)
        assertEquals("+01:00", prices[0].time.offset.toString())
    }

    @Test
    fun `summer time CEST converts correctly`() {
        // 2025-07-15T10:00:00Z = CEST 12:00 (UTC+2)
        val json = """{"unix_seconds": [1752573600], "price": [50.0]}"""

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
            "unix_seconds": [1743292800, 1743296400, 1743300000],
            "price": [50.0, 60.0, 70.0]
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
            "unix_seconds": [1761436800, 1761440400, 1761444000],
            "price": [50.0, 60.0, 70.0]
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
            "unix_seconds": [1743289200, 1743292800, 1743296400, 1743300000],
            "price": [40.0, 50.0, 60.0, 70.0]
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
