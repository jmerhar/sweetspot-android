package si.merhar.sweetspot.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import androidx.compose.ui.unit.dp
import si.merhar.sweetspot.model.WindowResult
import si.merhar.sweetspot.util.formatRelative
import si.merhar.sweetspot.util.shortTimeFormatter
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Summary cards showing the cheapest window's start time, end time, and estimated cost.
 * Times include a relative label (e.g. "now" or "in 2h 30m") that updates every 60 seconds.
 */
@Composable
fun ResultSummary(result: WindowResult, timeZoneId: ZoneId, modifier: Modifier = Modifier) {
    var now by remember { mutableStateOf(ZonedDateTime.now(timeZoneId)) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            now = ZonedDateTime.now(timeZoneId)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCard(
                label = "Start",
                value = result.startTime.format(shortTimeFormatter),
                subtitle = formatRelative(result.startTime, now),
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                label = "End",
                value = result.endTime.format(shortTimeFormatter),
                subtitle = formatRelative(result.endTime, now),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCard(
                label = "Total cost",
                value = "\u20AC ${String.format("%.4f", result.totalCost)}",
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                label = "Avg price",
                value = "\u20AC ${String.format("%.4f", result.avgPrice)}/kWh",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryCard(
    label: String,
    value: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier,
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
