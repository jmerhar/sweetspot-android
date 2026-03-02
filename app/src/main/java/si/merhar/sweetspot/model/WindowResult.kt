package si.merhar.sweetspot.model

import java.time.ZonedDateTime

/**
 * Result of the cheapest-window search.
 *
 * @property startTime When to start running the appliance.
 * @property endTime When the appliance will finish.
 * @property totalCost Total electricity cost in EUR for the window (per 1 kW load).
 * @property avgPrice Average price in EUR/kWh across the window.
 * @property breakdown Per-slot cost breakdown (one entry per hourly slot used).
 */
data class WindowResult(
    val startTime: ZonedDateTime,
    val endTime: ZonedDateTime,
    val totalCost: Double,
    val avgPrice: Double,
    val breakdown: List<BreakdownSlot>
)
