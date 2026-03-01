package si.merhar.sweetspot.model

import java.time.ZonedDateTime

data class HourlyPrice(
    val time: ZonedDateTime,
    val price: Double
)
