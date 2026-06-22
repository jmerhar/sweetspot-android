package today.sweetspot.util

import today.sweetspot.model.BreakdownSlot
import today.sweetspot.model.PriceSlot
import today.sweetspot.model.WindowResult
import java.time.Duration
import java.time.ZonedDateTime
import kotlin.math.floor

/**
 * Finds the cheapest contiguous time window of [durationHours] length within the given [prices].
 *
 * Works with any slot duration (15min, 30min, 60min, etc.). Uses a sliding window over price
 * slots, converting the requested duration to "slot units" internally. Supports fractional
 * slots — e.g. a 2h 30m duration with 60-min slots uses two full slots plus 50% of a third,
 * while with 15-min slots it uses exactly 10 full slots.
 *
 * If the cheapest window's first slot starts before [now], the start time is clamped to [now]
 * and the end time is computed as [now] + [durationHours], reflecting that the appliance would
 * start immediately rather than at the slot boundary.
 *
 * @param prices Price data sorted chronologically. Each entry represents one slot at the API's
 *               native resolution (all slots must have the same [PriceSlot.durationMinutes]).
 * @param durationHours Desired window length in decimal hours (e.g. 2.5 for 2h 30m).
 * @param now The current time, used to clamp the start when the cheapest window begins in the
 *            current slot.
 * @return The cheapest window with start/end times, total cost, average price, and per-slot
 *         breakdown, or `null` if there isn't enough price data to cover the duration.
 */
fun findCheapestWindow(prices: List<PriceSlot>, durationHours: Double, now: ZonedDateTime): WindowResult? {
    val count = prices.size
    if (count == 0) return null

    val slotMinutes = prices.first().durationMinutes
    val slotHours = slotMinutes / 60.0

    val durationInSlots = durationHours / slotHours
    val fullSlots = floor(durationInSlots).toInt()
    val fractionalSlot = durationInSlots - fullSlots
    val slotsNeeded = fullSlots + if (fractionalSlot > 0) 1 else 0

    if (count < slotsNeeded) return null

    val bestStart = findBestStartIndex(prices, fullSlots, fractionalSlot, count, slotsNeeded, slotHours)

    return buildWindowAt(prices, bestStart, durationHours, durationInSlots, fullSlots, fractionalSlot, slotHours, slotMinutes, now)
}

/**
 * Returns progressively-earlier alternative windows, ordered from cheapest toward "now".
 *
 * The first entry is always the cheapest window (identical to [findCheapestWindow]). Each
 * subsequent entry is the cheapest window whose start is *strictly earlier* than the previous
 * entry's start — i.e. repeatedly applying "find the next cheapest window that starts sooner".
 * The list ends at the earliest window that still fits the duration (its start clamped to [now]).
 *
 * Because price slots are sorted chronologically, start time increases with slot index, so this
 * is the sequence of successive minima found while scanning leftward from the cheapest window.
 * A consequence is that **cost increases monotonically** along the list: each step trades a higher
 * price for an earlier start. This lets the UI step "earlier" (advance the list) and "cheaper"
 * (retreat toward index 0) by simply moving an offset.
 *
 * @param prices Price data sorted chronologically (all slots share the same [PriceSlot.durationMinutes]).
 * @param durationHours Desired window length in decimal hours.
 * @param now The current time, used to clamp the earliest window's start.
 * @return Alternatives ordered cheapest-first then progressively earlier, or an empty list if
 *         there isn't enough price data to cover the duration.
 */
fun findWindowAlternatives(prices: List<PriceSlot>, durationHours: Double, now: ZonedDateTime): List<WindowResult> {
    val count = prices.size
    if (count == 0) return emptyList()

    val slotMinutes = prices.first().durationMinutes
    val slotHours = slotMinutes / 60.0

    val durationInSlots = durationHours / slotHours
    val fullSlots = floor(durationInSlots).toInt()
    val fractionalSlot = durationInSlots - fullSlots
    val slotsNeeded = fullSlots + if (fractionalSlot > 0) 1 else 0

    if (count < slotsNeeded) return emptyList()

    val lastStart = count - slotsNeeded
    val costs = DoubleArray(lastStart + 1) { i ->
        computeWindowCost(prices, i, fullSlots, fractionalSlot, slotHours)
    }

    // prefixMinIdx[k] = index of the cheapest window in [0, k] (earliest index on ties). This lets
    // us answer "cheapest window starting before index c" in O(1) as prefixMinIdx[c - 1].
    val prefixMinIdx = IntArray(lastStart + 1)
    for (k in 0..lastStart) {
        prefixMinIdx[k] = if (k == 0 || costs[k] < costs[prefixMinIdx[k - 1]]) k else prefixMinIdx[k - 1]
    }

    // Start at the cheapest window overall, then repeatedly jump to the cheapest window that starts
    // strictly earlier, until none remains. Each jump strictly decreases the index (earlier start)
    // and never decreases cost, so the list runs cheapest -> earliest with monotonic cost.
    val startIndices = mutableListOf(prefixMinIdx[lastStart])
    var cur = startIndices.first()
    while (cur > 0) {
        cur = prefixMinIdx[cur - 1]
        startIndices.add(cur)
    }

    return startIndices.map { start ->
        buildWindowAt(prices, start, durationHours, durationInSlots, fullSlots, fractionalSlot, slotHours, slotMinutes, now)
    }
}

/**
 * Builds the [WindowResult] for a window starting at [startIndex], applying start-time clamping
 * to [now] when the slot begins in the past.
 *
 * @param prices Price data.
 * @param startIndex Index of the first slot in the window.
 * @param durationHours Window length in decimal hours.
 * @param durationInSlots Window length in slot units.
 * @param fullSlots Number of complete slots in the (unclamped) window.
 * @param fractionalSlot Fractional part of the last slot (0.0–1.0 in slot units).
 * @param slotHours Duration of one slot in hours.
 * @param slotMinutes Duration of one slot in minutes.
 * @param now The current time, used to clamp the start when the window begins in the current slot.
 * @return The window with start/end times, total cost, average price, and per-slot breakdown.
 */
private fun buildWindowAt(
    prices: List<PriceSlot>,
    startIndex: Int,
    durationHours: Double,
    durationInSlots: Double,
    fullSlots: Int,
    fractionalSlot: Double,
    slotHours: Double,
    slotMinutes: Int,
    now: ZonedDateTime
): WindowResult {
    val slotStart = prices[startIndex].time
    val clamped = slotStart.isBefore(now)
    val startTime = if (clamped) now else slotStart
    val endTime = startTime.plusSeconds((durationHours * 3600).toLong())

    val breakdown = if (clamped) {
        val firstSlotRemaining = 1.0 - Duration.between(slotStart, now).seconds / (slotMinutes * 60.0)
        buildClampedBreakdown(prices, startIndex, durationInSlots, firstSlotRemaining, slotHours, slotMinutes)
    } else {
        buildBreakdown(prices, startIndex, fullSlots, fractionalSlot, slotHours, slotMinutes)
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
 * @param prices Price data.
 * @param fullSlots Number of complete slots in the window.
 * @param fractionalSlot Fractional part of the last slot (0.0–1.0 in slot units).
 * @param count Total number of price entries.
 * @param slotsNeeded Total slots required (fullSlots + 1 if fractionalSlot > 0).
 * @param slotHours Duration of one slot in hours.
 * @return Index into [prices] where the cheapest window starts.
 */
private fun findBestStartIndex(
    prices: List<PriceSlot>,
    fullSlots: Int,
    fractionalSlot: Double,
    count: Int,
    slotsNeeded: Int,
    slotHours: Double
): Int {
    var bestCost: Double? = null
    var bestStart = 0

    for (i in 0..count - slotsNeeded) {
        val cost = computeWindowCost(prices, i, fullSlots, fractionalSlot, slotHours)
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
 * @param prices Price data.
 * @param startIndex Index of the first slot in the window.
 * @param fullSlots Number of complete slots.
 * @param fractionalSlot Fraction of the last partial slot (0.0 if none), in slot units.
 * @param slotHours Duration of one slot in hours.
 * @return Total cost in EUR (per 1 kW load).
 */
private fun computeWindowCost(
    prices: List<PriceSlot>,
    startIndex: Int,
    fullSlots: Int,
    fractionalSlot: Double,
    slotHours: Double
): Double {
    var cost = 0.0
    for (j in 0 until fullSlots) {
        cost += prices[startIndex + j].price * slotHours
    }
    if (fractionalSlot > 0 && (startIndex + fullSlots) < prices.size) {
        cost += fractionalSlot * prices[startIndex + fullSlots].price * slotHours
    }
    return cost
}

/**
 * Builds the per-slot cost breakdown for a window starting at [startIndex].
 *
 * @param prices Price data.
 * @param startIndex Index of the first slot in the window.
 * @param fullSlots Number of complete slots.
 * @param fractionalSlot Fraction of the last partial slot (0.0 if none), in slot units.
 * @param slotHours Duration of one slot in hours.
 * @param slotMinutes Duration of one slot in minutes.
 * @return List of [BreakdownSlot] entries, one per slot used.
 */
private fun buildBreakdown(
    prices: List<PriceSlot>,
    startIndex: Int,
    fullSlots: Int,
    fractionalSlot: Double,
    slotHours: Double,
    slotMinutes: Int
): List<BreakdownSlot> {
    val breakdown = mutableListOf<BreakdownSlot>()

    for (j in 0 until fullSlots) {
        val slot = prices[startIndex + j]
        breakdown.add(
            BreakdownSlot(
                time = slot.time,
                price = slot.price,
                fraction = 1.0,
                cost = slot.price * slotHours,
                durationMinutes = slotMinutes
            )
        )
    }

    if (fractionalSlot > 0) {
        val slot = prices[startIndex + fullSlots]
        breakdown.add(
            BreakdownSlot(
                time = slot.time,
                price = slot.price,
                fraction = fractionalSlot,
                cost = fractionalSlot * slot.price * slotHours,
                durationMinutes = slotMinutes
            )
        )
    }

    return breakdown
}

/**
 * Builds the per-slot cost breakdown when the window start is clamped to "now".
 *
 * Because the appliance starts mid-slot, the first slot is only partially used and the
 * window extends into an additional slot at the end compared to the unclamped case.
 *
 * @param prices Price data.
 * @param startIndex Index of the first slot in the window.
 * @param durationInSlots Total duration in slot units.
 * @param firstSlotRemaining Fraction of the first slot remaining after "now" (0.0–1.0).
 * @param slotHours Duration of one slot in hours.
 * @param slotMinutes Duration of one slot in minutes.
 * @return List of [BreakdownSlot] entries with adjusted fractions.
 */
private fun buildClampedBreakdown(
    prices: List<PriceSlot>,
    startIndex: Int,
    durationInSlots: Double,
    firstSlotRemaining: Double,
    slotHours: Double,
    slotMinutes: Int
): List<BreakdownSlot> {
    val breakdown = mutableListOf<BreakdownSlot>()
    var remaining = durationInSlots
    var idx = startIndex

    // First (partial) slot
    val firstFraction = minOf(firstSlotRemaining, remaining)
    breakdown.add(
        BreakdownSlot(
            time = prices[idx].time,
            price = prices[idx].price,
            fraction = firstFraction,
            cost = firstFraction * prices[idx].price * slotHours,
            durationMinutes = slotMinutes
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
                cost = prices[idx].price * slotHours,
                durationMinutes = slotMinutes
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
                cost = remaining * prices[idx].price * slotHours,
                durationMinutes = slotMinutes
            )
        )
    }

    return breakdown
}
