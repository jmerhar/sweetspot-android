package si.merhar.sweetspot.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import si.merhar.sweetspot.model.HourlyPrice
import java.time.ZoneId
import java.time.ZonedDateTime

class CheapestWindowFinderTest {

    private val zone = ZoneId.of("Europe/Amsterdam")

    private fun pricesAt(hour: Int, vararg prices: Double): List<HourlyPrice> {
        val base = ZonedDateTime.of(2025, 6, 15, hour, 0, 0, 0, zone)
        return prices.mapIndexed { i, price ->
            HourlyPrice(time = base.plusHours(i.toLong()), price = price)
        }
    }

    // --- Null / edge cases ---

    @Test
    fun `returns null for empty price list`() {
        assertNull(findCheapestWindow(emptyList(), 1.0))
    }

    @Test
    fun `returns null when not enough data for duration`() {
        val prices = pricesAt(10, 0.10, 0.20) // 2 hours of data
        assertNull(findCheapestWindow(prices, 3.0)) // need 3
    }

    @Test
    fun `returns null when not enough data for fractional duration`() {
        val prices = pricesAt(10, 0.10) // 1 hour of data
        assertNull(findCheapestWindow(prices, 1.5)) // needs 2 slots
    }

    // --- Exact whole-hour durations ---

    @Test
    fun `finds cheapest 1-hour window`() {
        val prices = pricesAt(10, 0.30, 0.10, 0.20)
        val result = findCheapestWindow(prices, 1.0)

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
        val result = findCheapestWindow(prices, 2.0)!!

        assertEquals(11, result.startTime.hour) // starts at hour index 3
        assertEquals(0.20, result.totalCost, 0.0001)
        assertEquals(0.10, result.avgPrice, 0.0001)
        assertEquals(2, result.breakdown.size)
    }

    @Test
    fun `finds cheapest window when all prices are equal`() {
        val prices = pricesAt(10, 0.15, 0.15, 0.15, 0.15)
        val result = findCheapestWindow(prices, 2.0)!!

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
        val result = findCheapestWindow(prices, 2.5)!!

        assertEquals(11, result.startTime.hour)
        assertEquals(0.33, result.totalCost, 0.0001)
        assertEquals(3, result.breakdown.size)
        assertEquals(0.5, result.breakdown[2].fraction, 0.0001)
        assertEquals(0.03, result.breakdown[2].cost, 0.0001)
    }

    @Test
    fun `fractional duration of 0_25 hours (15 minutes)`() {
        val prices = pricesAt(10, 0.20, 0.08)
        val result = findCheapestWindow(prices, 0.25)!!

        assertEquals(11, result.startTime.hour)
        assertEquals(0.08 * 0.25, result.totalCost, 0.0001)
        assertEquals(1, result.breakdown.size)
        assertEquals(0.25, result.breakdown[0].fraction, 0.0001)
    }

    // --- Breakdown correctness ---

    @Test
    fun `breakdown has correct times and costs`() {
        val prices = pricesAt(14, 0.10, 0.20, 0.05)
        val result = findCheapestWindow(prices, 2.0)!!

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
        val result = findCheapestWindow(prices, 1.5)!!

        val expectedEndMinutes = result.startTime.plusMinutes(90)
        assertEquals(expectedEndMinutes, result.endTime)
    }

    // --- Negative prices ---

    @Test
    fun `handles negative prices correctly`() {
        val prices = pricesAt(10, 0.10, -0.05, 0.20, -0.10)
        val result = findCheapestWindow(prices, 2.0)!!

        // 2h windows: [0.10+(-0.05)]=0.05, [(-0.05)+0.20]=0.15, [0.20+(-0.10)]=0.10
        assertEquals(10, result.startTime.hour)
        assertEquals(0.05, result.totalCost, 0.0001)
    }

    // --- Single entry ---

    @Test
    fun `single price entry with 1-hour duration`() {
        val prices = pricesAt(10, 0.25)
        val result = findCheapestWindow(prices, 1.0)!!

        assertEquals(10, result.startTime.hour)
        assertEquals(0.25, result.totalCost, 0.0001)
        assertEquals(1, result.breakdown.size)
    }
}
