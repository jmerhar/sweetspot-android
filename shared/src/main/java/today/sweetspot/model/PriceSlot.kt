package today.sweetspot.model

import java.time.ZonedDateTime

/**
 * One slot of electricity price data at the API's native resolution.
 *
 * @property time Start time of the slot.
 * @property price Price in EUR per kWh for this slot.
 * @property durationMinutes Length of this slot in minutes (e.g. 60 for hourly, 15 for quarter-hourly).
 */
data class PriceSlot(
    val time: ZonedDateTime,
    val price: Double,
    val durationMinutes: Int
) {
    /**
     * Checks whether this slot's time range overlaps with the given window.
     *
     * Uses standard interval intersection: two intervals `[a, b)` and `[c, d)` overlap
     * iff `a < d && b > c`.
     *
     * @param windowStartEpoch Window start as epoch seconds.
     * @param windowEndEpoch Window end as epoch seconds.
     * @return `true` if any part of this slot falls within the window.
     */
    fun overlapsWindow(windowStartEpoch: Long, windowEndEpoch: Long): Boolean {
        val slotStart = time.toEpochSecond()
        val slotEnd = slotStart + durationMinutes * 60
        return slotStart < windowEndEpoch && slotEnd > windowStartEpoch
    }
}
