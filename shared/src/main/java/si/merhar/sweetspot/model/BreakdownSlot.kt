package si.merhar.sweetspot.model

import java.time.ZonedDateTime

/**
 * One slot in a cheapest-window cost breakdown.
 *
 * Each slot corresponds to one hourly price period. The last slot in a window may be
 * fractional (e.g. 50% of an hour for a 2h 30m duration).
 *
 * @property time Start time of this hourly slot.
 * @property price Full-hour price in EUR/kWh.
 * @property fraction Fraction of this hour used (1.0 for full hours, < 1.0 for partial).
 * @property cost Actual cost for this slot: `price * fraction`.
 */
data class BreakdownSlot(
    val time: ZonedDateTime,
    val price: Double,
    val fraction: Double,
    val cost: Double
)
