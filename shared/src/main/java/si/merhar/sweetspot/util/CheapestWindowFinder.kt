package si.merhar.sweetspot.util

import si.merhar.sweetspot.model.BreakdownSlot
import si.merhar.sweetspot.model.HourlyPrice
import si.merhar.sweetspot.model.WindowResult
import java.time.Duration
import java.time.ZonedDateTime
import kotlin.math.floor

/**
 * Finds the cheapest contiguous time window of [durationHours] length within the given [prices].
 *
 * Uses a sliding window over hourly price slots. Supports fractional hours — e.g. 2.5 hours
 * uses two full-hour slots plus 50% of a third slot.
 *
 * If the cheapest window's first slot starts before [now], the start time is clamped to [now]
 * and the end time is computed as [now] + [durationHours], reflecting that the appliance would
 * start immediately rather than at the hour boundary.
 *
 * @param prices Hourly price data, sorted chronologically. Each entry represents one hour.
 * @param durationHours Desired window length in decimal hours (e.g. 2.5 for 2h 30m).
 * @param now The current time, used to clamp the start when the cheapest window begins in the
 *            current hour.
 * @return The cheapest window with start/end times, total cost, average price, and per-slot
 *         breakdown, or `null` if there isn't enough price data to cover the duration.
 */
fun findCheapestWindow(prices: List<HourlyPrice>, durationHours: Double, now: ZonedDateTime): WindowResult? {
    val count = prices.size
    if (count == 0) return null

    val fullHours = floor(durationHours).toInt()
    val fractional = durationHours - fullHours
    val slotsNeeded = fullHours + if (fractional > 0) 1 else 0

    if (count < slotsNeeded) return null

    val bestStart = findBestStartIndex(prices, fullHours, fractional, count, slotsNeeded)

    val slotStart = prices[bestStart].time
    val clamped = slotStart.isBefore(now)
    val startTime = if (clamped) now else slotStart
    val endTime = startTime.plusSeconds((durationHours * 3600).toLong())

    val breakdown = if (clamped) {
        val firstSlotRemaining = 1.0 - Duration.between(slotStart, now).seconds / 3600.0
        buildClampedBreakdown(prices, bestStart, durationHours, firstSlotRemaining)
    } else {
        buildBreakdown(prices, bestStart, fullHours, fractional)
    }

    val totalCost = breakdown.sumOf { it.cost }

    return WindowResult(
        startTime = startTime,
        endTime = endTime,
        totalCost = totalCost,
        avgPrice = if (durationHours > 0) totalCost / durationHours else 0.0,
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
 * @return Total cost in EUR (per 1 kW load).
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
 * Builds the per-slot cost breakdown for a window starting at [startIndex].
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

/**
 * Builds the per-slot cost breakdown when the window start is clamped to "now".
 *
 * Because the appliance starts mid-hour, the first slot is only partially used and the
 * window extends into an additional slot at the end compared to the unclamped case.
 *
 * @param prices Hourly price data.
 * @param startIndex Index of the first slot in the window.
 * @param durationHours Total duration in decimal hours.
 * @param firstSlotRemaining Fraction of the first hour remaining after "now" (0.0–1.0).
 * @return List of [BreakdownSlot] entries with adjusted fractions.
 */
private fun buildClampedBreakdown(
    prices: List<HourlyPrice>,
    startIndex: Int,
    durationHours: Double,
    firstSlotRemaining: Double
): List<BreakdownSlot> {
    val breakdown = mutableListOf<BreakdownSlot>()
    var remaining = durationHours
    var idx = startIndex

    // First (partial) slot
    val firstFraction = minOf(firstSlotRemaining, remaining)
    breakdown.add(
        BreakdownSlot(
            time = prices[idx].time,
            price = prices[idx].price,
            fraction = firstFraction,
            cost = firstFraction * prices[idx].price
        )
    )
    remaining -= firstFraction
    idx++

    // Full middle slots
    while (remaining >= 1.0 && idx < prices.size) {
        breakdown.add(
            BreakdownSlot(
                time = prices[idx].time,
                price = prices[idx].price,
                fraction = 1.0,
                cost = prices[idx].price
            )
        )
        remaining -= 1.0
        idx++
    }

    // Last partial slot (if any)
    if (remaining > 1e-9 && idx < prices.size) {
        breakdown.add(
            BreakdownSlot(
                time = prices[idx].time,
                price = prices[idx].price,
                fraction = remaining,
                cost = remaining * prices[idx].price
            )
        )
    }

    return breakdown
}
