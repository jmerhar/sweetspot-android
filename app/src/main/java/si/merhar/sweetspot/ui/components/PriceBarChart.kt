package si.merhar.sweetspot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import si.merhar.sweetspot.model.HourlyPrice
import si.merhar.sweetspot.model.WindowResult
import si.merhar.sweetspot.ui.theme.LocalBarNegativeColor
import si.merhar.sweetspot.ui.theme.LocalBarNormalColor
import si.merhar.sweetspot.ui.theme.LocalBarOptimalColor
import androidx.compose.ui.tooling.preview.Preview
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.abs

private val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

/**
 * Horizontal bar chart showing hourly electricity prices.
 *
 * When all prices are non-negative, bars grow left-to-right from zero.
 * When negative prices exist, the zero axis shifts right proportionally
 * so that negative bars grow leftward and positive bars grow rightward.
 * Negative bars use a distinct color to highlight that the price is below zero.
 *
 * @param prices Hourly prices to display.
 * @param result Optional cheapest-window result whose slots are highlighted.
 * @param modifier Modifier for the outer column.
 */
@Composable
fun PriceBarChart(
    prices: List<HourlyPrice>,
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

    val minPrice = remember(prices) { prices.minOfOrNull { it.price } ?: 0.0 }
    val maxPrice = remember(prices) { prices.maxOfOrNull { it.price } ?: 1.0 }
    val hasNegative = minPrice < 0
    // Where zero falls within the min–max range (0 = left edge, 1 = right edge)
    val zeroFraction = remember(minPrice, maxPrice) {
        if (minPrice < 0 && maxPrice > minPrice) {
            ((0.0 - minPrice) / (maxPrice - minPrice)).toFloat().coerceIn(0.01f, 0.99f)
        } else 0f
    }

    Column(modifier = modifier.fillMaxWidth()) {
        prices.forEach { price ->
            val isOptimal = price.time.toEpochSecond() in optimalTimes
            val rowBackground = if (isOptimal) highlightColor else Color.Transparent
            val timeText = price.time.format(timeFormatter)
            val priceText = "\u20AC ${String.format("%.3f", price.price)}"
            val rowDescription = if (isOptimal) "$timeText, $priceText, cheapest window" else "$timeText, $priceText"

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

                if (hasNegative) {
                    // Diverging layout: negative portion | positive portion
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(20.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(trackColor)
                    ) {
                        // Left half (negative area)
                        Box(
                            modifier = Modifier
                                .weight(zeroFraction)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            if (price.price < 0) {
                                val negFraction = (abs(price.price) / abs(minPrice)).toFloat().coerceIn(0f, 1f)
                                val barColor = if (isOptimal) barOptimalColor else barNegativeColor
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(negFraction)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(barColor)
                                )
                            }
                        }
                        // Right half (positive area)
                        Box(
                            modifier = Modifier
                                .weight(1f - zeroFraction)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (price.price >= 0) {
                                val posFraction = if (maxPrice > 0) {
                                    (price.price / maxPrice).toFloat().coerceIn(0f, 1f)
                                } else 0f
                                val barColor = if (isOptimal) barOptimalColor else barNormalColor
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(posFraction)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(barColor)
                                )
                            }
                        }
                    }
                } else {
                    // Simple layout: all bars grow left-to-right from zero
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(20.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(trackColor)
                    ) {
                        val barFraction = if (maxPrice > 0) {
                            (price.price / maxPrice).toFloat().coerceIn(0f, 1f)
                        } else 0f
                        val barColor = if (isOptimal) barOptimalColor else barNormalColor
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(barFraction)
                                .height(20.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(barColor)
                        )
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
        HourlyPrice(base, 0.12),
        HourlyPrice(base.plusHours(1), 0.08),
        HourlyPrice(base.plusHours(2), 0.02),
        HourlyPrice(base.plusHours(3), -0.01),
        HourlyPrice(base.plusHours(4), -0.03),
        HourlyPrice(base.plusHours(5), -0.02),
        HourlyPrice(base.plusHours(6), 0.01),
        HourlyPrice(base.plusHours(7), 0.06),
        HourlyPrice(base.plusHours(8), 0.10),
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
        HourlyPrice(base, 0.12),
        HourlyPrice(base.plusHours(1), 0.08),
        HourlyPrice(base.plusHours(2), 0.04),
        HourlyPrice(base.plusHours(3), 0.02),
        HourlyPrice(base.plusHours(4), 0.05),
        HourlyPrice(base.plusHours(5), 0.09),
        HourlyPrice(base.plusHours(6), 0.11),
    )
    si.merhar.sweetspot.ui.theme.SweetSpotTheme {
        PriceBarChart(prices = prices, result = null)
    }
}
