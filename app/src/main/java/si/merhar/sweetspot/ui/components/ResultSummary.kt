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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import si.merhar.sweetspot.R
import si.merhar.sweetspot.model.WindowResult
import si.merhar.sweetspot.util.formatRelative
import si.merhar.sweetspot.util.shortTimeFormatter
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Summary cards showing the cheapest window's start time, end time, and estimated cost.
 * Times include a relative label (e.g. "now" or "in 2h 30m") that stays current because
 * the ViewModel periodically recalculates the result.
 */
@Composable
fun ResultSummary(result: WindowResult, timeZoneId: ZoneId, modifier: Modifier = Modifier) {
    val resources = LocalContext.current.resources
    val now = ZonedDateTime.now(timeZoneId)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCard(
                label = stringResource(R.string.result_start),
                value = result.startTime.format(shortTimeFormatter),
                subtitle = formatRelative(result.startTime, now, resources),
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                label = stringResource(R.string.result_end),
                value = result.endTime.format(shortTimeFormatter),
                subtitle = formatRelative(result.endTime, now, resources),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCard(
                label = stringResource(R.string.result_total_cost),
                value = "\u20AC ${String.format("%.4f", result.totalCost)}",
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                label = stringResource(R.string.result_avg_price),
                value = "\u20AC ${String.format("%.4f", result.avgPrice)}${stringResource(R.string.result_per_kwh)}",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null
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
