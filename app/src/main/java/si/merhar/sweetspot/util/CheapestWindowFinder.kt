package si.merhar.sweetspot.util

import si.merhar.sweetspot.model.BreakdownSlot
import si.merhar.sweetspot.model.HourlyPrice
import si.merhar.sweetspot.model.WindowResult
import kotlin.math.floor

/**
 * Finds the cheapest contiguous time window of [durationHours] length within the given [prices].
 *
 * Uses a sliding window over hourly price slots. Supports fractional hours — e.g. 2.5 hours
 * uses two full-hour slots plus 50% of a third slot.
 *
 * @param prices Hourly price data, sorted chronologically. Each entry represents one hour.
 * @param durationHours Desired window length in decimal hours (e.g. 2.5 for 2h 30m).
 * @return The cheapest window with start/end times, total cost, average price, and per-slot
 *         breakdown, or `null` if there isn't enough price data to cover the duration.
 */
fun findCheapestWindow(prices: List<HourlyPrice>, durationHours: Double): WindowResult? {
    val count = prices.size
    if (count == 0) return null

    val fullHours = floor(durationHours).toInt()
    val fractional = durationHours - fullHours
    val slotsNeeded = fullHours + if (fractional > 0) 1 else 0

    if (count < slotsNeeded) return null

    val bestStart = findBestStartIndex(prices, fullHours, fractional, count, slotsNeeded)
    val bestCost = computeWindowCost(prices, bestStart, fullHours, fractional)
    val breakdown = buildBreakdown(prices, bestStart, fullHours, fractional)

    val startTime = prices[bestStart].time
    val endTime = startTime.plusSeconds((durationHours * 3600).toLong())

    return WindowResult(
        startTime = startTime,
        endTime = endTime,
        totalCost = bestCost,
        avgPrice = if (durationHours > 0) bestCost / durationHours else 0.0,
        breakdown = breakdown
    )
}

/**
 * Scans all possible window positions and returns the start index with the lowest total cost.
 *
 * @param prices Hourly price data.
 * @param fullHours Number of complete hour slots in the window.
 * @param fractional Fractional part of the last hour (0.0–1.0).
 * @param count Total number of price entries.
 * @param slotsNeeded Total slots required (fullHours + 1 if fractional > 0).
 * @return Index into [prices] where the cheapest window starts.
 */
private fun findBestStartIndex(
    prices: List<HourlyPrice>,
    fullHours: Int,
    fractional: Double,
    count: Int,
    slotsNeeded: Int
): Int {
    var bestCost: Double? = null
    var bestStart = 0

    for (i in 0..count - slotsNeeded) {
        val cost = computeWindowCost(prices, i, fullHours, fractional)
        if (bestCost == null || cost < bestCost) {
            bestCost = cost
            bestStart = i
        }
    }

    return bestStart
}

/**
 * Computes the total cost of a window starting at [startIndex].
 *
 * @param prices Hourly price data.
 * @param startIndex Index of the first slot in the window.
 * @param fullHours Number of complete hour slots.
 * @param fractional Fraction of the last partial hour (0.0 if none).
 * @return Total cost in cents.
 */
private fun computeWindowCost(
    prices: List<HourlyPrice>,
    startIndex: Int,
    fullHours: Int,
    fractional: Double
): Double {
    var cost = 0.0
    for (j in 0 until fullHours) {
        cost += prices[startIndex + j].price
    }
    if (fractional > 0 && (startIndex + fullHours) < prices.size) {
        cost += fractional * prices[startIndex + fullHours].price
    }
    return cost
}

/**
 * Builds the per-slot cost breakdown for the window starting at [startIndex].
 *
 * @param prices Hourly price data.
 * @param startIndex Index of the first slot in the window.
 * @param fullHours Number of complete hour slots.
 * @param fractional Fraction of the last partial hour (0.0 if none).
 * @return List of [BreakdownSlot] entries, one per hour slot used.
 */
private fun buildBreakdown(
    prices: List<HourlyPrice>,
    startIndex: Int,
    fullHours: Int,
    fractional: Double
): List<BreakdownSlot> {
    val breakdown = mutableListOf<BreakdownSlot>()

    for (j in 0 until fullHours) {
        val slot = prices[startIndex + j]
        breakdown.add(
            BreakdownSlot(
                time = slot.time,
                price = slot.price,
                fraction = 1.0,
                cost = slot.price
            )
        )
    }

    if (fractional > 0) {
        val slot = prices[startIndex + fullHours]
        breakdown.add(
            BreakdownSlot(
                time = slot.time,
                price = slot.price,
                fraction = fractional,
                cost = fractional * slot.price
            )
        )
    }

    return breakdown
}
