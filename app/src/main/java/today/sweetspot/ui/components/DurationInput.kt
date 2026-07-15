package today.sweetspot.ui.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import today.sweetspot.R
import today.sweetspot.model.Appliance
import today.sweetspot.model.applianceIconFor
import today.sweetspot.model.applianceIcons
import today.sweetspot.shared.R as SharedR
import today.sweetspot.util.HomeChipLayout
import today.sweetspot.util.HomeGroup
import today.sweetspot.util.formatDuration

/** Maximum number of type columns shown side by side before wrapping to a new row of columns. */
private const val MAX_GROUP_COLUMNS = 3

private data class QuickDuration(val hours: Int, val minutes: Int)

private val quickDurations = listOf(
    QuickDuration(1, 0),
    QuickDuration(2, 0),
    QuickDuration(3, 0),
    QuickDuration(4, 0),
    QuickDuration(5, 0),
    QuickDuration(6, 0)
)

/**
 * Card containing the full duration-selection UI: appliance chips (or a CTA to add them),
 * quick-duration buttons, a scroll-wheel [DurationPicker], and a "Find cheapest time" button.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DurationInput(
    hours: Int,
    minutes: Int,
    onDurationChanged: (Int, Int) -> Unit,
    onFind: () -> Unit,
    onQuickDuration: (Int, Int) -> Unit,
    homeLayout: HomeChipLayout,
    onApplianceTap: (Appliance) -> Unit,
    onAddAppliancesTap: () -> Unit,
    isLoading: Boolean,
    deadlineEnabled: Boolean,
    deadlineHour: Int,
    deadlineMinute: Int,
    onDeadlineEnabledChange: (Boolean) -> Unit,
    onDeadlineTimeChange: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val resources = LocalContext.current.resources

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.main_card_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Appliance buttons or CTA
            when (val layout = homeLayout) {
                is HomeChipLayout.Flat -> if (layout.items.isNotEmpty()) {
                    ApplianceChipFlow(layout.items, onApplianceTap)
                    Spacer(modifier = Modifier.height(4.dp))
                } else {
                    AddAppliancesButton(onAddAppliancesTap)
                }
                is HomeChipLayout.Sectioned -> {
                    val firstLabel = if (layout.vehiclesFirst) R.string.home_section_vehicles else R.string.home_section_appliances
                    val secondLabel = if (layout.vehiclesFirst) R.string.home_section_appliances else R.string.home_section_vehicles
                    HomeSectionLabel(stringResource(firstLabel))
                    ApplianceChipFlow(layout.first, onApplianceTap)
                    Spacer(modifier = Modifier.height(8.dp))
                    HomeSectionLabel(stringResource(secondLabel))
                    ApplianceChipFlow(layout.second, onApplianceTap)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                is HomeChipLayout.Grouped -> if (layout.groups.isEmpty() && layout.vehicles.isEmpty()) {
                    AddAppliancesButton(onAddAppliancesTap)
                } else {
                    GroupedAppliances(layout, onApplianceTap)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // Quick duration buttons — wrap to multiple rows for longer translations
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                quickDurations.forEach { qd ->
                    SuggestionChip(
                        onClick = { onQuickDuration(qd.hours, qd.minutes) },
                        label = {
                            Text(text = formatDuration(qd.hours, qd.minutes, resources))
                        }
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

            DeadlineRow(
                enabled = deadlineEnabled,
                hour = deadlineHour,
                minute = deadlineMinute,
                onEnabledChange = onDeadlineEnabledChange,
                onTimeChange = onDeadlineTimeChange
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
                    Text(stringResource(R.string.main_searching))
                } else {
                    Text(stringResource(R.string.main_find_button))
                }
            }
        }
    }
}

/** A wrapping row of appliance chips; each fills its duration and searches on tap. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ApplianceChipFlow(appliances: List<Appliance>, onApplianceTap: (Appliance) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        appliances.forEach { appliance ->
            ApplianceChip(appliance, onApplianceTap)
        }
    }
}

/** A single appliance chip: type icon + name, running its duration on tap. */
@Composable
private fun ApplianceChip(
    appliance: Appliance,
    onApplianceTap: (Appliance) -> Unit,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = { onApplianceTap(appliance) },
        modifier = modifier,
        label = {
            Text(
                text = appliance.name,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(applianceIconFor(appliance)),
                contentDescription = null,
                modifier = Modifier.size(AssistChipDefaults.IconSize)
            )
        }
    )
}

/**
 * Chips grouped by type. In row mode each group is a full-width band (header + wrapping chips);
 * in column mode groups sit side by side, capped at [MAX_GROUP_COLUMNS] and wrapping to further
 * rows of columns, with the chips in each column stacked vertically.
 */
@Composable
private fun GroupedAppliances(layout: HomeChipLayout.Grouped, onApplianceTap: (Appliance) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        if (layout.vehicles.isNotEmpty() && layout.vehiclesFirst) {
            VehicleBlock(layout.vehicles, onApplianceTap)
        }
        if (layout.groups.isNotEmpty()) {
            if (layout.columns) GroupColumns(layout.groups, onApplianceTap)
            else GroupRows(layout.groups, onApplianceTap)
        }
        if (layout.vehicles.isNotEmpty() && !layout.vehiclesFirst) {
            VehicleBlock(layout.vehicles, onApplianceTap)
        }
    }
}

/** Type groups laid out side by side, capped at [MAX_GROUP_COLUMNS] and wrapping to further rows. */
@Composable
private fun GroupColumns(groups: List<HomeGroup>, onApplianceTap: (Appliance) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        groups.chunked(MAX_GROUP_COLUMNS).forEach { rowGroups ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowGroups.forEach { group ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        HomeGroupHeader(group)
                        group.items.forEach { appliance ->
                            ApplianceChip(appliance, onApplianceTap, Modifier.fillMaxWidth())
                        }
                    }
                }
                // Keep the last (partial) row's columns the same width as full rows.
                repeat(MAX_GROUP_COLUMNS - rowGroups.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/** Type groups stacked as full-width bands (header + wrapping chips). */
@Composable
private fun GroupRows(groups: List<HomeGroup>, onApplianceTap: (Appliance) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        groups.forEach { group ->
            Column {
                HomeGroupHeader(group)
                ApplianceChipFlow(group.items, onApplianceTap)
            }
        }
    }
}

/** The separate vehicles block drawn above or below the type grid: a "Vehicles" label + chips. */
@Composable
private fun VehicleBlock(vehicles: List<Appliance>, onApplianceTap: (Appliance) -> Unit) {
    Column {
        HomeSectionLabel(stringResource(R.string.home_section_vehicles))
        ApplianceChipFlow(vehicles, onApplianceTap)
    }
}

/** Caption for a type group: the group's icon plus its type name (or "Vehicles"). */
@Composable
private fun HomeGroupHeader(group: HomeGroup) {
    val title = when {
        group.isVehicles -> stringResource(R.string.home_section_vehicles)
        else -> applianceIcons.firstOrNull { it.id == group.iconId }
            ?.let { stringResource(it.labelRes) } ?: stringResource(SharedR.string.icon_other)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 2.dp)
    ) {
        Icon(
            painter = painterResource(applianceIconFor(group.items.first())),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Small caption above a home-screen chip section (vehicles / appliances). */
@Composable
private fun HomeSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

/** The "add appliances" call-to-action shown when there are no appliance chips yet. */
@Composable
private fun AddAppliancesButton(onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Outlined.Add,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(stringResource(R.string.main_add_appliances))
    }
}
