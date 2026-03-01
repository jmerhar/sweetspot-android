package si.merhar.sweetspot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import si.merhar.sweetspot.model.HourlyPrice
import si.merhar.sweetspot.model.WindowResult
import si.merhar.sweetspot.ui.theme.LocalBarNormalColor
import si.merhar.sweetspot.ui.theme.LocalBarOptimalColor
import java.time.format.DateTimeFormatter
import kotlin.math.max

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun PriceBarChart(
    prices: List<HourlyPrice>,
    result: WindowResult?,
    modifier: Modifier = Modifier
) {
    val optimalTimes = result?.breakdown?.map { it.time.toEpochSecond() }?.toSet() ?: emptySet()
    val barNormalColor = LocalBarNormalColor.current
    val barOptimalColor = LocalBarOptimalColor.current
    val highlightColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)

    val maxPrice = prices.maxOfOrNull { max(0.0, it.price) } ?: 1.0
    val scaleFactor = if (maxPrice > 0) maxPrice else 1.0

    Column(modifier = modifier.fillMaxWidth()) {
        prices.forEach { price ->
            val isOptimal = price.time.toEpochSecond() in optimalTimes
            val barColor = if (isOptimal) barOptimalColor else barNormalColor
            val barFraction = (max(0.0, price.price) / scaleFactor).toFloat().coerceIn(0f, 1f)
            val rowBackground = if (isOptimal) highlightColor else Color.Transparent

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(rowBackground)
                    .padding(horizontal = 4.dp, vertical = 1.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = price.time.format(timeFormatter),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(40.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(barFraction)
                            .height(20.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(barColor)
                    )
                }
                Text(
                    text = "\u20AC ${String.format("%.3f", price.price)}",
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
