package si.merhar.sweetspot.wear.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import si.merhar.sweetspot.util.formatRelative
import si.merhar.sweetspot.wear.WearUiState
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/** Time formatter for HH:mm display. */
private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")

/**
 * Result screen for the Wear OS app.
 *
 * Shows the cheapest start and end times for the selected appliance, along with
 * relative time indicators (e.g. "now", "in 2h 30m"). Scrollable to accommodate
 * longer appliance labels on small watch faces.
 *
 * @param state Current UI state containing the result.
 * @param onDismiss Callback to navigate back (swipe-right is handled by nav host).
 */
@Composable
fun ResultScreen(
    state: WearUiState,
    @Suppress("UNUSED_PARAMETER") onDismiss: () -> Unit
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
                color = Color(0xFFCF6679),
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

    val now = ZonedDateTime.now(state.zoneId)
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
                text = result.startTime.format(timeFormat),
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
                text = result.endTime.format(timeFormat),
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
