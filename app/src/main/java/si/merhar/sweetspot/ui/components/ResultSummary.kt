package si.merhar.sweetspot.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import si.merhar.sweetspot.model.WindowResult
import si.merhar.sweetspot.ui.theme.SubtitleGray
import si.merhar.sweetspot.util.AMSTERDAM
import si.merhar.sweetspot.util.formatRelative
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun ResultSummary(result: WindowResult, modifier: Modifier = Modifier) {
    val now = ZonedDateTime.now(AMSTERDAM)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SummaryItem(
                label = "Start",
                value = result.startTime.format(timeFormatter),
                subtitle = formatRelative(result.startTime, now),
                modifier = Modifier.weight(1f)
            )
            SummaryItem(
                label = "End",
                value = result.endTime.format(timeFormatter),
                subtitle = formatRelative(result.endTime, now),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SummaryItem(
                label = "Total cost",
                value = "\u20AC ${String.format("%.4f", result.totalCost)}",
                modifier = Modifier.weight(1f)
            )
            SummaryItem(
                label = "Avg price",
                value = "\u20AC ${String.format("%.4f", result.avgPrice)}/kWh",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(4.dp)) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = SubtitleGray
        )
        Row {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            if (subtitle != null) {
                Text(
                    text = " ($subtitle)",
                    fontSize = 12.sp,
                    color = SubtitleGray,
                    modifier = Modifier.alignByBaseline()
                )
            }
        }
    }
}
