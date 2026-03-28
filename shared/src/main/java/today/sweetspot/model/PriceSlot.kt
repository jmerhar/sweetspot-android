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
)
