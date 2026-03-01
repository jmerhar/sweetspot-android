package si.merhar.sweetspot.model

import java.time.ZonedDateTime

data class WindowResult(
    val startTime: ZonedDateTime,
    val endTime: ZonedDateTime,
    val totalCost: Double,
    val avgPrice: Double,
    val breakdown: List<BreakdownSlot>
)
