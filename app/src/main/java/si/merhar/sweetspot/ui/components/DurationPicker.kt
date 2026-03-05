package si.merhar.sweetspot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import si.merhar.sweetspot.R
import kotlinx.coroutines.flow.distinctUntilChanged

private val ITEM_HEIGHT = 40.dp
private const val VISIBLE_ITEMS = 5
private val hourValues = (0..24).toList()
private val minuteValues = (0..55 step 5).toList()

/**
 * Two-column scroll wheel for selecting a duration in hours (0–24) and minutes (0–55, 5-min steps).
 * Snaps to the nearest value on fling and emits changes via [onChanged] when scrolling settles.
 */
@Composable
fun DurationPicker(
    hours: Int,
    minutes: Int,
    onChanged: (hours: Int, minutes: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacerCount = VISIBLE_ITEMS / 2

    val currentHours = rememberUpdatedState(hours)
    val currentMinutes = rememberUpdatedState(minutes)
    val currentOnChanged = rememberUpdatedState(onChanged)

    val hoursListState = rememberLazyListState(
        initialFirstVisibleItemIndex = hourValues.indexOf(hours).coerceAtLeast(0)
    )
    val minutesListState = rememberLazyListState(
        initialFirstVisibleItemIndex = minuteValues.indexOf(minutes).coerceAtLeast(0)
    )

    // Scroll to correct position when values change externally
    LaunchedEffect(hours) {
        val targetIndex = hourValues.indexOf(hours).coerceAtLeast(0)
        if (hoursListState.firstVisibleItemIndex != targetIndex) {
            hoursListState.animateScrollToItem(targetIndex)
        }
    }

    LaunchedEffect(minutes) {
        val targetIndex = minuteValues.indexOf(minutes).coerceAtLeast(0)
        if (minutesListState.firstVisibleItemIndex != targetIndex) {
            minutesListState.animateScrollToItem(targetIndex)
        }
    }

    // Emit hour changes when scroll settles
    LaunchedEffect(hoursListState) {
        snapshotFlow { hoursListState.firstVisibleItemIndex to hoursListState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { (index, scrolling) ->
                if (!scrolling && index in hourValues.indices) {
                    val newHours = hourValues[index]
                    val mins = currentMinutes.value
                    if (newHours == 24 && mins != 0) {
                        currentOnChanged.value(24, 0)
                    } else if (newHours != currentHours.value) {
                        currentOnChanged.value(newHours, mins)
                    }
                }
            }
    }

    // Emit minute changes when scroll settles
    LaunchedEffect(minutesListState) {
        snapshotFlow { minutesListState.firstVisibleItemIndex to minutesListState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { (index, scrolling) ->
                if (!scrolling && index in minuteValues.indices) {
                    val newMinutes = minuteValues[index]
                    val hrs = currentHours.value
                    if (newMinutes != currentMinutes.value) {
                        if (hrs == 24 && newMinutes != 0) {
                            currentOnChanged.value(24, 0)
                        } else {
                            currentOnChanged.value(hrs, newMinutes)
                        }
                    }
                }
            }
    }

    val minutesText = minutes.toString().padStart(2, '0')
    val durationPickerCd = stringResource(R.string.cd_duration_picker)
    val hoursCd = stringResource(R.string.cd_hours, hours)
    val minutesCd = stringResource(R.string.cd_minutes, minutesText)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = durationPickerCd
                stateDescription = "${hours}h ${minutesText}m"
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.picker_duration),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Highlight indicator behind center selection
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(ITEM_HEIGHT)
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                        RoundedCornerShape(10.dp)
                    )
            )

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Hours column
                LazyColumn(
                    state = hoursListState,
                    flingBehavior = rememberSnapFlingBehavior(hoursListState),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(60.dp)
                        .height(ITEM_HEIGHT * VISIBLE_ITEMS)
                        .semantics { contentDescription = hoursCd }
                ) {
                    // Top spacer items so first real item can be centered
                    items(spacerCount) {
                        Box(modifier = Modifier.height(ITEM_HEIGHT))
                    }
                    items(hourValues.size) { index ->
                        val centerIndex = hoursListState.firstVisibleItemIndex
                        val isCenter = index == centerIndex && !hoursListState.isScrollInProgress
                        PickerItem(
                            text = hourValues[index].toString(),
                            isCenter = isCenter
                        )
                    }
                    // Bottom spacer
                    items(spacerCount) {
                        Box(modifier = Modifier.height(ITEM_HEIGHT))
                    }
                }

                Text(
                    text = stringResource(R.string.picker_hour_suffix),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )

                // Minutes column
                LazyColumn(
                    state = minutesListState,
                    flingBehavior = rememberSnapFlingBehavior(minutesListState),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(60.dp)
                        .height(ITEM_HEIGHT * VISIBLE_ITEMS)
                        .semantics { contentDescription = minutesCd }
                ) {
                    // Top spacer
                    items(spacerCount) {
                        Box(modifier = Modifier.height(ITEM_HEIGHT))
                    }
                    items(minuteValues.size) { index ->
                        val centerIndex = minutesListState.firstVisibleItemIndex
                        val isCenter = index == centerIndex && !minutesListState.isScrollInProgress
                        PickerItem(
                            text = minuteValues[index].toString().padStart(2, '0'),
                            isCenter = isCenter
                        )
                    }
                    // Bottom spacer
                    items(spacerCount) {
                        Box(modifier = Modifier.height(ITEM_HEIGHT))
                    }
                }

                Text(
                    text = stringResource(R.string.picker_minute_suffix),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun PickerItem(text: String, isCenter: Boolean) {
    Box(
        modifier = Modifier
            .height(ITEM_HEIGHT)
            .width(60.dp)
            .alpha(if (isCenter) 1f else 0.3f),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = if (isCenter) 22.sp else 16.sp,
            fontWeight = if (isCenter) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}
