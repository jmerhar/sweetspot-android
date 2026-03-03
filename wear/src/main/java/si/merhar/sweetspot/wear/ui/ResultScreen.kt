package si.merhar.sweetspot.wear.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import si.merhar.sweetspot.util.formatRelative
import si.merhar.sweetspot.util.shortTimeFormatter
import si.merhar.sweetspot.wear.WearUiState
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Result screen for the Wear OS app.
 *
 * Shows the cheapest start and end times for the selected appliance, along with
 * relative time indicators (e.g. "now", "in 2h 30m") that update every 60 seconds.
 * Scrollable to accommodate longer appliance labels on small watch faces.
 *
 * @param state Current UI state containing the result.
 */
@Composable
fun ResultScreen(
    state: WearUiState
) {
    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (state.error != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = state.error,
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.body2,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    val result = state.result
    if (result == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No result",
                style = MaterialTheme.typography.body2,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    val timeZoneId = remember(state.priceZone) { ZoneId.of(state.priceZone!!.timeZoneId) }
    var now by remember { mutableStateOf(ZonedDateTime.now(timeZoneId)) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            now = ZonedDateTime.now(timeZoneId)
        }
    }
    // Center on the label item (index 3: Start caption, time, relative, then label)
    val listState = rememberScalingLazyListState(initialCenterItemIndex = 3)

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        autoCentering = AutoCenteringParams(itemIndex = 3)
    ) {
        // Start time
        item {
            Text(
                text = "Start",
                style = MaterialTheme.typography.caption2,
                color = MaterialTheme.colors.onSurfaceVariant
            )
        }
        item {
            Text(
                text = result.startTime.format(shortTimeFormatter),
                style = MaterialTheme.typography.display3
            )
        }
        item {
            Text(
                text = formatRelative(result.startTime, now),
                style = MaterialTheme.typography.caption2,
                color = MaterialTheme.colors.primary
            )
        }

        // Appliance label (centered between start/end for max horizontal space)
        if (state.resultLabel != null) {
            item {
                Text(
                    text = state.resultLabel,
                    style = MaterialTheme.typography.caption1,
                    color = MaterialTheme.colors.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        // End time
        item {
            Text(
                text = "End",
                style = MaterialTheme.typography.caption2,
                color = MaterialTheme.colors.onSurfaceVariant
            )
        }
        item {
            Text(
                text = result.endTime.format(shortTimeFormatter),
                style = MaterialTheme.typography.display3
            )
        }
        item {
            Text(
                text = formatRelative(result.endTime, now),
                style = MaterialTheme.typography.caption2,
                color = MaterialTheme.colors.primary
            )
        }
    }
}
