package si.merhar.sweetspot.model

import java.time.ZonedDateTime

data class BreakdownSlot(
    val time: ZonedDateTime,
    val price: Double,
    val fraction: Double,
    val cost: Double
)
