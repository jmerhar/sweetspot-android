package si.merhar.sweetspot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import si.merhar.sweetspot.R
import si.merhar.sweetspot.model.PriceSlot
import si.merhar.sweetspot.model.WindowResult
import si.merhar.sweetspot.ui.theme.LocalBarNegativeColor
import si.merhar.sweetspot.ui.theme.LocalBarNormalColor
import si.merhar.sweetspot.ui.theme.LocalBarOptimalColor
import si.merhar.sweetspot.util.formatPrice
import si.merhar.sweetspot.util.shortTimeFormatter
import androidx.compose.ui.tooling.preview.Preview
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs

/**
 * Horizontal bar chart showing electricity prices grouped by hour.
 *
 * Sub-hourly slots (e.g. 15-minute) are stacked vertically within each hourly row,
 * with the time label and average price shown once per hour. For hourly data this
 * renders identically to one bar per row.
 *
 * When all prices are non-negative, bars grow left-to-right from zero.
 * When negative prices exist, the zero axis shifts right proportionally
 * so that negative bars grow leftward and positive bars grow rightward.
 * Negative bars use a distinct colour to highlight that the price is below zero.
 *
 * @param prices Price slots to display (any resolution, sorted chronologically).
 * @param result Optional cheapest-window result whose slots are highlighted.
 * @param modifier Modifier for the outer column.
 */
@Composable
fun PriceBarChart(
    prices: List<PriceSlot>,
    result: WindowResult?,
    modifier: Modifier = Modifier
) {
    val optimalTimes = remember(result) {
        result?.breakdown?.map { it.time.toEpochSecond() }?.toSet() ?: emptySet()
    }
    val barNormalColor = LocalBarNormalColor.current
    val barOptimalColor = LocalBarOptimalColor.current
    val barNegativeColor = LocalBarNegativeColor.current
    val highlightColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    val cheapestWindowCd = stringResource(R.string.cd_cheapest_window)

    val minPrice = remember(prices) { prices.minOfOrNull { it.price } ?: 0.0 }
    val maxPrice = remember(prices) { prices.maxOfOrNull { it.price } ?: 1.0 }
    val hasNegative = minPrice < 0
    // Where zero falls within the min–max range (0 = left edge, 1 = right edge)
    val zeroFraction = remember(minPrice, maxPrice) {
        if (minPrice < 0 && maxPrice > minPrice) {
            ((0.0 - minPrice) / (maxPrice - minPrice)).toFloat().coerceIn(0.01f, 0.99f)
        } else 0f
    }

    // Group slots by hour (using epoch second of truncated hour for correct DST handling)
    val slotsPerHour = remember(prices) {
        if (prices.isEmpty()) 1 else 60 / prices.first().durationMinutes
    }
    val hourlyGroups = remember(prices) {
        prices.groupBy { it.time.truncatedTo(ChronoUnit.HOURS).toEpochSecond() }
            .toSortedMap()
            .values
            .toList()
    }

    Column(modifier = modifier.fillMaxWidth()) {
        hourlyGroups.forEach { slots ->
            val hourTime = slots.first().time.truncatedTo(ChronoUnit.HOURS)
            val avgPrice = slots.map { it.price }.average()
            val anyOptimal = slots.any { it.time.toEpochSecond() in optimalTimes }
            val rowBackground = if (anyOptimal) highlightColor else Color.Transparent
            val timeText = hourTime.format(shortTimeFormatter)
            val priceText = formatPrice(avgPrice, 3)
            val rowDescription = if (anyOptimal) "$timeText, $priceText, $cheapestWindowCd" else "$timeText, $priceText"

            // Build a fixed-size list of slots for this hour, with nulls for missing sub-slots.
            // This ensures incomplete hours (first/last) always render the same height.
            val slotByMinute = slots.associateBy { it.time.minute }
            val durationMinutes = slots.first().durationMinutes
            val paddedSlots = (0 until slotsPerHour).map { i ->
                slotByMinute[i * durationMinutes]
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(rowBackground)
                    .semantics(mergeDescendants = true) {
                        contentDescription = rowDescription
                    }
                    .padding(horizontal = 4.dp, vertical = 1.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(40.dp)
                )

                // Stack sub-hourly bars vertically within the hourly row height.
                // Each bar gets its own rounded track background and spacing.
                // Missing sub-slots (incomplete first/last hour) render as empty spacers.
                val barShape = RoundedCornerShape(6.dp)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(20.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    paddedSlots.forEach { slot ->
                        if (slot == null) {
                            Spacer(modifier = Modifier.fillMaxWidth().weight(1f))
                        } else {
                            val isOptimal = slot.time.toEpochSecond() in optimalTimes

                            if (hasNegative) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .clip(barShape)
                                        .background(trackColor)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(zeroFraction)
                                            .fillMaxHeight(),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        if (slot.price < 0) {
                                            val negFraction = (abs(slot.price) / abs(minPrice)).toFloat().coerceIn(0f, 1f)
                                            val barColor = if (isOptimal) barOptimalColor else barNegativeColor
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(negFraction)
                                                    .fillMaxHeight()
                                                    .clip(barShape)
                                                    .background(barColor)
                                            )
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f - zeroFraction)
                                            .fillMaxHeight(),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (slot.price >= 0) {
                                            val posFraction = if (maxPrice > 0) {
                                                (slot.price / maxPrice).toFloat().coerceIn(0f, 1f)
                                            } else 0f
                                            val barColor = if (isOptimal) barOptimalColor else barNormalColor
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(posFraction)
                                                    .fillMaxHeight()
                                                    .clip(barShape)
                                                    .background(barColor)
                                            )
                                        }
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .clip(barShape)
                                        .background(trackColor)
                                ) {
                                    val barFraction = if (maxPrice > 0) {
                                        (slot.price / maxPrice).toFloat().coerceIn(0f, 1f)
                                    } else 0f
                                    val barColor = if (isOptimal) barOptimalColor else barNormalColor
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(barFraction)
                                            .fillMaxHeight()
                                            .clip(barShape)
                                            .background(barColor)
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    text = priceText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .width(72.dp)
                        .padding(start = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Mixed positive and negative prices")
@Composable
private fun PriceBarChartNegativePreview() {
    val base = ZonedDateTime.now().withHour(10).withMinute(0).withSecond(0).withNano(0)
    val prices = listOf(
        PriceSlot(base, 0.12, 60),
        PriceSlot(base.plusHours(1), 0.08, 60),
        PriceSlot(base.plusHours(2), 0.02, 60),
        PriceSlot(base.plusHours(3), -0.01, 60),
        PriceSlot(base.plusHours(4), -0.03, 60),
        PriceSlot(base.plusHours(5), -0.02, 60),
        PriceSlot(base.plusHours(6), 0.01, 60),
        PriceSlot(base.plusHours(7), 0.06, 60),
        PriceSlot(base.plusHours(8), 0.10, 60),
    )
    si.merhar.sweetspot.ui.theme.SweetSpotTheme {
        PriceBarChart(prices = prices, result = null)
    }
}

@Preview(showBackground = true, name = "All positive prices")
@Composable
private fun PriceBarChartPositivePreview() {
    val base = ZonedDateTime.now().withHour(10).withMinute(0).withSecond(0).withNano(0)
    val prices = listOf(
        PriceSlot(base, 0.12, 60),
        PriceSlot(base.plusHours(1), 0.08, 60),
        PriceSlot(base.plusHours(2), 0.04, 60),
        PriceSlot(base.plusHours(3), 0.02, 60),
        PriceSlot(base.plusHours(4), 0.05, 60),
        PriceSlot(base.plusHours(5), 0.09, 60),
        PriceSlot(base.plusHours(6), 0.11, 60),
    )
    si.merhar.sweetspot.ui.theme.SweetSpotTheme {
        PriceBarChart(prices = prices, result = null)
    }
}

@Preview(showBackground = true, name = "15-minute slots")
@Composable
private fun PriceBarChart15MinPreview() {
    val base = ZonedDateTime.now().withHour(10).withMinute(0).withSecond(0).withNano(0)
    val prices = listOf(
        PriceSlot(base, 0.10, 15),
        PriceSlot(base.plusMinutes(15), 0.12, 15),
        PriceSlot(base.plusMinutes(30), 0.08, 15),
        PriceSlot(base.plusMinutes(45), 0.11, 15),
        PriceSlot(base.plusHours(1), 0.06, 15),
        PriceSlot(base.plusHours(1).plusMinutes(15), 0.04, 15),
        PriceSlot(base.plusHours(1).plusMinutes(30), 0.05, 15),
        PriceSlot(base.plusHours(1).plusMinutes(45), 0.03, 15),
        PriceSlot(base.plusHours(2), 0.09, 15),
        PriceSlot(base.plusHours(2).plusMinutes(15), 0.14, 15),
        PriceSlot(base.plusHours(2).plusMinutes(30), 0.11, 15),
        PriceSlot(base.plusHours(2).plusMinutes(45), 0.13, 15),
    )
    si.merhar.sweetspot.ui.theme.SweetSpotTheme {
        PriceBarChart(prices = prices, result = null)
    }
}
