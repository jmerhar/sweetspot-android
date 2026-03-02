package si.merhar.sweetspot.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import si.merhar.sweetspot.model.Appliance
import si.merhar.sweetspot.model.applianceIconFor

private data class QuickDuration(val hours: Int, val minutes: Int, val label: String)

private val quickDurations = listOf(
    QuickDuration(1, 0, "1h"),
    QuickDuration(2, 0, "2h"),
    QuickDuration(3, 0, "3h"),
    QuickDuration(4, 0, "4h"),
    QuickDuration(5, 0, "5h"),
    QuickDuration(6, 0, "6h")
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DurationInput(
    hours: Int,
    minutes: Int,
    onDurationChanged: (Int, Int) -> Unit,
    onFind: () -> Unit,
    onQuickDuration: (Int, Int) -> Unit,
    appliances: List<Appliance>,
    onApplianceTap: (Appliance) -> Unit,
    onAddAppliancesTap: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Find the cheapest time to run your appliance",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Appliance buttons or CTA
            if (appliances.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    appliances.forEach { appliance ->
                        AssistChip(
                            onClick = { onApplianceTap(appliance) },
                            label = {
                                Text(
                                    text = appliance.name,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = applianceIconFor(appliance.icon),
                                    contentDescription = null,
                                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            } else {
                TextButton(onClick = onAddAppliancesTap) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add your appliances")
                }
            }

            // Quick duration buttons — equal-width row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                quickDurations.forEach { qd ->
                    SuggestionChip(
                        onClick = { onQuickDuration(qd.hours, qd.minutes) },
                        label = { Text(qd.label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            DurationPicker(
                hours = hours,
                minutes = minutes,
                onChanged = onDurationChanged
            )

            Spacer(modifier = Modifier.height(8.dp))

            FilledTonalButton(
                onClick = onFind,
                enabled = !isLoading && (hours > 0 || minutes > 0),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Searching\u2026")
                } else {
                    Text("Find Cheapest Window")
                }
            }
        }
    }
}
