package today.sweetspot.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

/**
 * Tests for [SpotHintaApi.parse] around DST transition boundaries.
 *
 * Europe/Helsinki transitions:
 * - Spring forward: last Sunday of March, 03:00 → 04:00 (EET → EEST)
 * - Fall back: last Sunday of October, 04:00 → 03:00 (EEST → EET)
 */
class SpotHintaApiDstTest {

    private val api = SpotHintaApi("FI")
    private val timeZone = ZoneId.of("Europe/Helsinki")

    @Test
    fun `winter time EET converts correctly`() {
        // January: EET = UTC+2, so UTC 10:00 = local 12:00
        val json = """
        [
            {"DateTime": "2025-01-15T10:00:00Z", "PriceNoTax": 0.10}
        ]
        """.trimIndent()

        val prices = api.parse(json, timeZone)
        assertEquals(12, prices[0].time.hour)
        assertEquals("+02:00", prices[0].time.offset.toString())
    }

    @Test
    fun `summer time EEST converts correctly`() {
        // July: EEST = UTC+3, so UTC 10:00 = local 13:00
        val json = """
        [
            {"DateTime": "2025-07-15T10:00:00Z", "PriceNoTax": 0.10}
        ]
        """.trimIndent()

        val prices = api.parse(json, timeZone)
        assertEquals(13, prices[0].time.hour)
        assertEquals("+03:00", prices[0].time.offset.toString())
    }

    @Test
    fun `spring forward night produces no duplicate local times`() {
        // 2025-03-30: clocks spring forward at 03:00 → 04:00
        // UTC 00:00 = EET 02:00 (still EET)
        // UTC 01:00 = EEST 04:00 (03:00 is skipped)
        // UTC 02:00 = EEST 05:00
        val json = """
        [
            {"DateTime": "2025-03-30T00:00:00Z", "PriceNoTax": 0.10},
            {"DateTime": "2025-03-30T01:00:00Z", "PriceNoTax": 0.20},
            {"DateTime": "2025-03-30T02:00:00Z", "PriceNoTax": 0.30}
        ]
        """.trimIndent()

        val prices = api.parse(json, timeZone)
        assertEquals(3, prices.size)
        assertEquals(2, prices[0].time.hour)   // EET 02:00
        assertEquals(4, prices[1].time.hour)   // EEST 04:00 (03:00 skipped)
        assertEquals(5, prices[2].time.hour)   // EEST 05:00

        // All local times are unique
        val localHours = prices.map { it.time.hour }
        assertEquals(localHours.size, localHours.distinct().size)
    }

    @Test
    fun `fall back night produces two entries with same local hour but different offsets`() {
        // 2025-10-26: clocks fall back at 04:00 → 03:00
        // UTC 00:00 = EEST 03:00 (+03:00)
        // UTC 01:00 = EET 03:00 (+02:00) — same local hour, different offset
        // UTC 02:00 = EET 04:00
        val json = """
        [
            {"DateTime": "2025-10-26T00:00:00Z", "PriceNoTax": 0.10},
            {"DateTime": "2025-10-26T01:00:00Z", "PriceNoTax": 0.20},
            {"DateTime": "2025-10-26T02:00:00Z", "PriceNoTax": 0.30}
        ]
        """.trimIndent()

        val prices = api.parse(json, timeZone)
        assertEquals(3, prices.size)

        // Both show 03:00 local but with different offsets
        assertEquals(3, prices[0].time.hour)
        assertEquals("+03:00", prices[0].time.offset.toString())  // EEST
        assertEquals(3, prices[1].time.hour)
        assertEquals("+02:00", prices[1].time.offset.toString())  // EET
        assertEquals(4, prices[2].time.hour)

        // Despite same local hour, they are distinct instants and sort correctly
        assertTrue(prices[0].time.toEpochSecond() < prices[1].time.toEpochSecond())
    }

    @Test
    fun `prices spanning DST transition sort chronologically`() {
        // Prices crossing spring forward: 01:00 EET → 02:00 → 04:00 EEST → 05:00 EEST
        val json = """
        [
            {"DateTime": "2025-03-29T23:00:00Z", "PriceNoTax": 0.05},
            {"DateTime": "2025-03-30T00:00:00Z", "PriceNoTax": 0.04},
            {"DateTime": "2025-03-30T01:00:00Z", "PriceNoTax": 0.03},
            {"DateTime": "2025-03-30T02:00:00Z", "PriceNoTax": 0.02}
        ]
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
