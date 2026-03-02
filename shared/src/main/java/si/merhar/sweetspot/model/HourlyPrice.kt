package si.merhar.sweetspot.model

import java.time.ZonedDateTime

/**
 * One hour of electricity price data.
 *
 * @property time Start time of the hourly slot.
 * @property price Price in EUR per kWh for this hour.
 */
data class HourlyPrice(
    val time: ZonedDateTime,
    val price: Double
)
