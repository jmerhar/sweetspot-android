package si.merhar.sweetspot.model

import java.time.ZonedDateTime

/**
 * One slot in a cheapest-window cost breakdown.
 *
 * Each slot corresponds to one price slot at the API's native resolution. The last slot
 * in a window may be fractional (e.g. 50% of a slot for a duration that doesn't align
 * to slot boundaries).
 *
 * @property time Start time of this slot.
 * @property price Full-slot price in EUR/kWh.
 * @property fraction Fraction of this slot used (1.0 for full slots, < 1.0 for partial).
 * @property cost Actual cost for this slot: `price * fraction * (durationMinutes / 60.0)`.
 * @property durationMinutes Length of this slot in minutes (e.g. 60 for hourly, 15 for quarter-hourly).
 */
data class BreakdownSlot(
    val time: ZonedDateTime,
    val price: Double,
    val fraction: Double,
    val cost: Double,
    val durationMinutes: Int
)
