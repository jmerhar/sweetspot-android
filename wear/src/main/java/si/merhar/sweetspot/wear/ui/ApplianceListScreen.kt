package si.merhar.sweetspot.wear.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.scrollAway
import si.merhar.sweetspot.model.Appliance
import si.merhar.sweetspot.model.applianceIconFor
import si.merhar.sweetspot.util.formatDuration
import si.merhar.sweetspot.wear.WearUiState

/**
 * Appliance list screen for the Wear OS app.
 *
 * Displays a scrollable list of appliance chips. Tapping a chip triggers a price fetch
 * and navigates to the result screen.
 *
 * @param state Current UI state.
 * @param onApplianceTapped Callback when an appliance chip is tapped.
 */
@Composable
fun ApplianceListScreen(
    state: WearUiState,
    onApplianceTapped: (Appliance) -> Unit
) {
    val listState = rememberScalingLazyListState()

    Box(modifier = Modifier.fillMaxSize()) {
        TimeText(modifier = Modifier.scrollAway(listState))

        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState
        ) {
            item { ListHeader { Text("SweetSpot") } }

            if (state.appliances.isEmpty()) {
                item {
                    Text(
                        text = "Set up appliances in the phone app.",
                        style = MaterialTheme.typography.body2,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                items(state.appliances, key = { it.id }) { appliance ->
                    Chip(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onApplianceTapped(appliance) },
                        label = {
                            Text(
                                text = appliance.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        secondaryLabel = {
                            Text(
                                text = formatDuration(
                                    appliance.durationHours,
                                    appliance.durationMinutes
                                )
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = applianceIconFor(appliance.icon),
                                contentDescription = appliance.name,
                                modifier = Modifier.size(ChipDefaults.IconSize)
                            )
                        },
                        colors = ChipDefaults.primaryChipColors()
                    )
                }
            }

            if (state.error != null) {
                item {
                    Text(
                        text = state.error,
                        color = MaterialTheme.colors.error,
                        style = MaterialTheme.typography.body2,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
