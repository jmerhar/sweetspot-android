package si.merhar.sweetspot.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import si.merhar.sweetspot.model.HourlyPrice
import java.time.ZoneId
import java.time.ZonedDateTime

class CheapestWindowFinderTest {

    private val zone = ZoneId.of("Europe/Amsterdam")

    /** A time well before any price slot, so no clamping occurs. */
    private val earlyNow = ZonedDateTime.of(2025, 6, 15, 0, 0, 0, 0, zone)

    private fun pricesAt(hour: Int, vararg prices: Double): List<HourlyPrice> {
        val base = ZonedDateTime.of(2025, 6, 15, hour, 0, 0, 0, zone)
        return prices.mapIndexed { i, price ->
            HourlyPrice(time = base.plusHours(i.toLong()), price = price)
        }
    }

    // --- Null / edge cases ---

    @Test
    fun `returns null for empty price list`() {
        assertNull(findCheapestWindow(emptyList(), 1.0, earlyNow))
    }

    @Test
    fun `returns null when not enough data for duration`() {
        val prices = pricesAt(10, 0.10, 0.20) // 2 hours of data
        assertNull(findCheapestWindow(prices, 3.0, earlyNow)) // need 3
    }

    @Test
    fun `returns null when not enough data for fractional duration`() {
        val prices = pricesAt(10, 0.10) // 1 hour of data
        assertNull(findCheapestWindow(prices, 1.5, earlyNow)) // needs 2 slots
    }

    @Test
    fun `zero duration returns zero-cost result with empty breakdown`() {
        val prices = pricesAt(10, 0.10, 0.20, 0.30)
        val result = findCheapestWindow(prices, 0.0, earlyNow)

        assertNotNull(result)
        assertEquals(0.0, result!!.totalCost, 0.0001)
        assertEquals(0.0, result.avgPrice, 0.0001)
        assertTrue(result.breakdown.isEmpty())
    }

    // --- Exact whole-hour durations ---

    @Test
    fun `finds cheapest 1-hour window`() {
        val prices = pricesAt(10, 0.30, 0.10, 0.20)
        val result = findCheapestWindow(prices, 1.0, earlyNow)

        assertNotNull(result)
        assertEquals(11, result!!.startTime.hour)
        assertEquals(0.10, result.totalCost, 0.0001)
        assertEquals(0.10, result.avgPrice, 0.0001)
        assertEquals(1, result.breakdown.size)
    }

    @Test
    fun `finds cheapest 2-hour window`() {
        // Hours: 0.30, 0.10, 0.20, 0.05, 0.15
        // 2h windows: [0.30+0.10]=0.40, [0.10+0.20]=0.30, [0.20+0.05]=0.25, [0.05+0.15]=0.20
        val prices = pricesAt(8, 0.30, 0.10, 0.20, 0.05, 0.15)
        val result = findCheapestWindow(prices, 2.0, earlyNow)!!

        assertEquals(11, result.startTime.hour) // starts at hour index 3
        assertEquals(0.20, result.totalCost, 0.0001)
        assertEquals(0.10, result.avgPrice, 0.0001)
        assertEquals(2, result.breakdown.size)
    }

    @Test
    fun `finds cheapest window when all prices are equal`() {
        val prices = pricesAt(10, 0.15, 0.15, 0.15, 0.15)
        val result = findCheapestWindow(prices, 2.0, earlyNow)!!

        // Should pick the first window when all costs are tied
        assertEquals(10, result.startTime.hour)
        assertEquals(0.30, result.totalCost, 0.0001)
    }

    // --- Fractional durations ---

    @Test
    fun `finds cheapest window with fractional hours`() {
        // 2.5 hours: need 2 full + 0.5 of third slot
        // Prices: 0.40, 0.10, 0.20, 0.06
        // Window at 1: 0.10 + 0.20 + 0.5*0.06 = 0.33
        // Window at 0: 0.40 + 0.10 + 0.5*0.20 = 0.60
        val prices = pricesAt(10, 0.40, 0.10, 0.20, 0.06)
        val result = findCheapestWindow(prices, 2.5, earlyNow)!!

        assertEquals(11, result.startTime.hour)
        assertEquals(0.33, result.totalCost, 0.0001)
        assertEquals(3, result.breakdown.size)
        assertEquals(0.5, result.breakdown[2].fraction, 0.0001)
        assertEquals(0.03, result.breakdown[2].cost, 0.0001)
    }

    @Test
    fun `fractional duration of 0_25 hours (15 minutes)`() {
        val prices = pricesAt(10, 0.20, 0.08)
        val result = findCheapestWindow(prices, 0.25, earlyNow)!!

        assertEquals(11, result.startTime.hour)
        assertEquals(0.08 * 0.25, result.totalCost, 0.0001)
        assertEquals(1, result.breakdown.size)
        assertEquals(0.25, result.breakdown[0].fraction, 0.0001)
    }

    // --- Breakdown correctness ---

    @Test
    fun `breakdown has correct times and costs`() {
        val prices = pricesAt(14, 0.10, 0.20, 0.05)
        val result = findCheapestWindow(prices, 2.0, earlyNow)!!

        // Cheapest 2h window: [0.10+0.20]=0.30 or [0.20+0.05]=0.25 → starts at 15:00
        assertEquals(15, result.startTime.hour)
        assertEquals(17, result.endTime.hour)
        assertEquals(2, result.breakdown.size)
        assertEquals(15, result.breakdown[0].time.hour)
        assertEquals(0.20, result.breakdown[0].price, 0.0001)
        assertEquals(1.0, result.breakdown[0].fraction, 0.0001)
        assertEquals(16, result.breakdown[1].time.hour)
        assertEquals(0.05, result.breakdown[1].price, 0.0001)
    }

    // --- End time correctness ---

    @Test
    fun `end time is correctly offset from start`() {
        val prices = pricesAt(10, 0.10, 0.20, 0.15)
        val result = findCheapestWindow(prices, 1.5, earlyNow)!!

        val expectedEndMinutes = result.startTime.plusMinutes(90)
        assertEquals(expectedEndMinutes, result.endTime)
    }

    // --- Negative prices ---

    @Test
    fun `handles negative prices correctly`() {
        val prices = pricesAt(10, 0.10, -0.05, 0.20, -0.10)
        val result = findCheapestWindow(prices, 2.0, earlyNow)!!

        // 2h windows: [0.10+(-0.05)]=0.05, [(-0.05)+0.20]=0.15, [0.20+(-0.10)]=0.10
        assertEquals(10, result.startTime.hour)
        assertEquals(0.05, result.totalCost, 0.0001)
    }

    // --- Single entry ---

    @Test
    fun `single price entry with 1-hour duration`() {
        val prices = pricesAt(10, 0.25)
        val result = findCheapestWindow(prices, 1.0, earlyNow)!!

        assertEquals(10, result.startTime.hour)
        assertEquals(0.25, result.totalCost, 0.0001)
        assertEquals(1, result.breakdown.size)
    }

    // --- Now-clamping ---

    @Test
    fun `clamps start time to now when cheapest slot is current hour`() {
        // Cheapest slot is 10:00, but "now" is 10:07 — start should be clamped to 10:07
        // Appliance runs 10:07–11:07: 53 min at €0.05 + 7 min at €0.20
        val prices = pricesAt(10, 0.05, 0.20, 0.30)
        val now = ZonedDateTime.of(2025, 6, 15, 10, 7, 0, 0, zone)

        val result = findCheapestWindow(prices, 1.0, now)!!

        assertEquals(now, result.startTime)
        assertEquals(now.plusHours(1), result.endTime)

        // Breakdown: partial first slot (53/60) + partial second slot (7/60)
        assertEquals(2, result.breakdown.size)
        assertEquals(53.0 / 60.0, result.breakdown[0].fraction, 0.0001)
        assertEquals(10, result.breakdown[0].time.hour)
        assertEquals(7.0 / 60.0, result.breakdown[1].fraction, 0.0001)
        assertEquals(11, result.breakdown[1].time.hour)

        val expectedCost = (53.0 / 60.0) * 0.05 + (7.0 / 60.0) * 0.20
        assertEquals(expectedCost, result.totalCost, 0.0001)
        assertEquals(expectedCost / 1.0, result.avgPrice, 0.0001)
    }

    @Test
    fun `does not clamp when cheapest slot starts after now`() {
        // Cheapest slot is 11:00, "now" is 10:07 — no clamping
        val prices = pricesAt(10, 0.30, 0.05, 0.20)
        val now = ZonedDateTime.of(2025, 6, 15, 10, 7, 0, 0, zone)

        val result = findCheapestWindow(prices, 1.0, now)!!

        assertEquals(11, result.startTime.hour)
        assertEquals(0, result.startTime.minute)
        assertEquals(12, result.endTime.hour)

        // Breakdown unchanged: single full slot
        assertEquals(1, result.breakdown.size)
        assertEquals(1.0, result.breakdown[0].fraction, 0.0001)
        assertEquals(0.05, result.totalCost, 0.0001)
    }

    @Test
    fun `clamps start time with fractional duration`() {
        // 2.5h window, cheapest starts at 10:00, now is 10:15
        // Appliance runs 10:15–12:45: 45 min at €0.05 + 60 min at €0.05 + 45 min at €0.05
        val prices = pricesAt(10, 0.05, 0.05, 0.05, 0.50)
        val now = ZonedDateTime.of(2025, 6, 15, 10, 15, 0, 0, zone)

        val result = findCheapestWindow(prices, 2.5, now)!!

        assertEquals(now, result.startTime)
        assertEquals(12, result.endTime.hour)
        assertEquals(45, result.endTime.minute)

        // Breakdown: 0.75 + 1.0 + 0.75 = 2.5h across 3 slots
        assertEquals(3, result.breakdown.size)
        assertEquals(0.75, result.breakdown[0].fraction, 0.0001)
        assertEquals(1.0, result.breakdown[1].fraction, 0.0001)
        assertEquals(0.75, result.breakdown[2].fraction, 0.0001)

        val expectedCost = 0.75 * 0.05 + 1.0 * 0.05 + 0.75 * 0.05
        assertEquals(expectedCost, result.totalCost, 0.0001)
        assertEquals(expectedCost / 2.5, result.avgPrice, 0.0001)
    }

    // --- Breakdown invariants ---

    @Test
    fun `breakdown fractions sum to duration for whole hours`() {
        val prices = pricesAt(10, 0.30, 0.10, 0.20, 0.05, 0.15)
        val duration = 3.0
        val result = findCheapestWindow(prices, duration, earlyNow)!!

        val fractionSum = result.breakdown.sumOf { it.fraction }
        assertEquals(duration, fractionSum, 0.0001)
    }

    @Test
    fun `breakdown fractions sum to duration for fractional hours`() {
        val prices = pricesAt(10, 0.40, 0.10, 0.20, 0.06)
        val duration = 2.5
        val result = findCheapestWindow(prices, duration, earlyNow)!!

        val fractionSum = result.breakdown.sumOf { it.fraction }
        assertEquals(duration, fractionSum, 0.0001)
    }

    @Test
    fun `breakdown costs sum to totalCost for whole hours`() {
        val prices = pricesAt(10, 0.30, 0.10, 0.20, 0.05, 0.15)
        val result = findCheapestWindow(prices, 3.0, earlyNow)!!

        val costSum = result.breakdown.sumOf { it.cost }
        assertEquals(result.totalCost, costSum, 0.0001)
    }

    @Test
    fun `breakdown costs sum to totalCost for fractional hours`() {
        val prices = pricesAt(10, 0.40, 0.10, 0.20, 0.06)
        val result = findCheapestWindow(prices, 2.5, earlyNow)!!

        val costSum = result.breakdown.sumOf { it.cost }
        assertEquals(result.totalCost, costSum, 0.0001)
    }

    @Test
    fun `breakdown invariants hold when start is clamped to now`() {
        val prices = pricesAt(10, 0.05, 0.20, 0.30)
        val now = ZonedDateTime.of(2025, 6, 15, 10, 15, 0, 0, zone)
        val duration = 2.0
        val result = findCheapestWindow(prices, duration, now)!!

        val fractionSum = result.breakdown.sumOf { it.fraction }
        assertEquals(duration, fractionSum, 0.0001)

        val costSum = result.breakdown.sumOf { it.cost }
        assertEquals(result.totalCost, costSum, 0.0001)
    }

    @Test
    fun `breakdown invariants hold for clamped fractional duration`() {
        val prices = pricesAt(10, 0.05, 0.05, 0.05, 0.50)
        val now = ZonedDateTime.of(2025, 6, 15, 10, 15, 0, 0, zone)
        val duration = 2.5
        val result = findCheapestWindow(prices, duration, now)!!

        val fractionSum = result.breakdown.sumOf { it.fraction }
        assertEquals(duration, fractionSum, 0.0001)

        val costSum = result.breakdown.sumOf { it.cost }
        assertEquals(result.totalCost, costSum, 0.0001)
    }
}
