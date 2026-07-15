package today.sweetspot.ui.settings

import android.text.format.DateUtils
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import sh.calvin.reorderable.ReorderableColumn
import today.sweetspot.R
import today.sweetspot.model.Appliance
import today.sweetspot.model.ApplianceGrouping
import today.sweetspot.model.ApplianceSort
import today.sweetspot.model.ApplianceUsage
import today.sweetspot.model.SortKey
import today.sweetspot.model.applianceIconFor
import today.sweetspot.model.applianceIcons
import today.sweetspot.ui.components.DurationPicker
import today.sweetspot.util.formatKw
import today.sweetspot.util.formatDuration

/**
 * Appliances settings section: description, sort control, the appliance list (drag-to-reorder in
 * Custom mode, else read-only in the active order), an add button, and a reset-usage action.
 *
 * @param appliances Non-EV appliances already in the active sort order.
 * @param sort The active ordering.
 * @param usage Combined tap statistics (for collision-gated tie-breakers).
 * @param onApplianceClick Opens the edit dialog for an appliance.
 * @param onAddClick Opens the add dialog.
 * @param onSortChanged Persists a changed ordering.
 * @param onReorder Persists a manual (Custom) reorder of the non-EV list.
 * @param onResetUsage Clears tap-usage history.
 * @param grouping How home-screen chips are grouped by type.
 * @param onGroupingChanged Persists a changed grouping.
 */
@Composable
internal fun AppliancesSection(
    appliances: List<Appliance>,
    sort: ApplianceSort,
    usage: Map<String, ApplianceUsage>,
    onApplianceClick: (Appliance) -> Unit,
    onAddClick: () -> Unit,
    onSortChanged: (ApplianceSort) -> Unit,
    onReorder: (List<Appliance>) -> Unit,
    onResetUsage: () -> Unit,
    grouping: ApplianceGrouping,
    onGroupingChanged: (ApplianceGrouping) -> Unit
) {
    val resources = LocalContext.current.resources
    var showResetConfirm by rememberSaveable { mutableStateOf(false) }
    // Usage figures only make sense when a usage-based order is active (never in Custom, which
    // can't be paired with another key and shows drag handles instead).
    val showUsage = sort.criteria.any { it.key == SortKey.FREQUENCY || it.key == SortKey.RECENCY }

    Text(
        text = stringResource(R.string.settings_appliances),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )

    Text(
        text = stringResource(R.string.settings_appliances_description),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp)
    )

    if (sort.isCustom && appliances.isNotEmpty()) {
        // Manual order: drag to reorder. Local state animates the drag; onSettle persists it.
        var items by remember(appliances) { mutableStateOf(appliances) }
        ReorderableColumn(
            list = items,
            onSettle = { from, to ->
                items = items.toMutableList().apply { add(to, removeAt(from)) }
                onReorder(items)
            },
            modifier = Modifier.fillMaxWidth()
        ) { _, appliance, _ ->
            key(appliance.id) {
                ReorderableItem {
                    ApplianceRow(
                        appliance = appliance,
                        usage = usage[appliance.id],
                        showUsage = showUsage,
                        resources = resources,
                        onClick = { onApplianceClick(appliance) },
                        dragHandle = {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = stringResource(R.string.cd_drag_handle),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.draggableHandle()
                            )
                        }
                    )
                }
            }
        }
    } else {
        appliances.forEach { appliance ->
            ApplianceRow(
                appliance = appliance,
                usage = usage[appliance.id],
                showUsage = showUsage,
                resources = resources,
                onClick = { onApplianceClick(appliance) },
                dragHandle = null
            )
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAddClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.settings_add_appliance),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }

    if (appliances.isNotEmpty()) {
        ApplianceSortControl(sort, appliances, usage, onSortChanged)
        ApplianceGroupingControl(grouping, onGroupingChanged)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showResetConfirm = true }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.settings_reset_usage),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = stringResource(R.string.settings_reset_usage_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text(stringResource(R.string.reset_usage_confirm_title)) },
            text = { Text(stringResource(R.string.reset_usage_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    onResetUsage()
                    showResetConfirm = false
                }) { Text(stringResource(R.string.action_reset)) }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

/**
 * The chip-grouping control: title, description, and Off / Rows / Columns choices. Grouping
 * clusters home-screen chips by appliance type and overrides vehicle placement while active.
 */
@Composable
private fun ApplianceGroupingControl(
    grouping: ApplianceGrouping,
    onGroupingChanged: (ApplianceGrouping) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = stringResource(R.string.settings_group_title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(R.string.settings_group_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = grouping == ApplianceGrouping.NONE,
                onClick = { onGroupingChanged(ApplianceGrouping.NONE) },
                label = { Text(stringResource(R.string.group_off)) }
            )
            FilterChip(
                selected = grouping == ApplianceGrouping.ROWS,
                onClick = { onGroupingChanged(ApplianceGrouping.ROWS) },
                label = { Text(stringResource(R.string.group_rows)) }
            )
            FilterChip(
                selected = grouping == ApplianceGrouping.COLUMNS,
                onClick = { onGroupingChanged(ApplianceGrouping.COLUMNS) },
                label = { Text(stringResource(R.string.group_columns)) }
            )
        }
    }
}

/**
 * A single appliance row: icon, name, duration, right-aligned usage (tap count + last used), and
 * an optional trailing drag handle.
 */
@Composable
private fun ApplianceRow(
    appliance: Appliance,
    usage: ApplianceUsage?,
    showUsage: Boolean,
    resources: android.content.res.Resources,
    onClick: () -> Unit,
    dragHandle: (@Composable () -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {}
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(applianceIconFor(appliance)),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = appliance.name,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = formatDuration(appliance.durationHours, appliance.durationMinutes, resources),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (showUsage) UsageStats(usage)
        dragHandle?.invoke()
    }
}

/** Right-aligned tap statistics for an appliance: frequency (count) over recency (last used). */
@Composable
private fun UsageStats(usage: ApplianceUsage?) {
    val lastUsedMs = usage?.lastUsedMs ?: 0L
    val recency = if (lastUsedMs <= 0L) {
        stringResource(R.string.usage_never)
    } else {
        DateUtils.getRelativeTimeSpanString(lastUsedMs, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString()
    }
    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier.padding(start = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.usage_count_format, usage?.count ?: 0),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = recency,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Dialog for creating or editing an appliance with name, duration, icon, and optional power. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ApplianceDialog(
    appliance: Appliance?,
    onSave: (name: String, durationHours: Int, durationMinutes: Int, icon: String, powerKw: Double?) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf(appliance?.name ?: "") }
    var pickerHours by rememberSaveable { mutableIntStateOf(appliance?.durationHours ?: 1) }
    var pickerMinutes by rememberSaveable { mutableIntStateOf(appliance?.durationMinutes ?: 0) }
    var selectedIcon by rememberSaveable { mutableStateOf(appliance?.icon ?: "electricity") }
    var powerText by rememberSaveable { mutableStateOf(appliance?.powerKw?.let { formatKw(it) } ?: "") }

    // Power is optional: blank ⇒ null. When entered it must parse to a positive number.
    val powerKw = powerText.trim().replace(',', '.').toDoubleOrNull()
    val powerValid = powerText.isBlank() || (powerKw != null && powerKw > 0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (appliance == null) stringResource(R.string.dialog_add_appliance) else stringResource(R.string.dialog_edit_appliance))
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text(
                            text = stringResource(R.string.action_delete),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.dialog_name)) },
                    placeholder = { Text(stringResource(R.string.dialog_name_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = powerText,
                    onValueChange = { powerText = it },
                    label = { Text(stringResource(R.string.dialog_power)) },
                    suffix = { Text("kW") },
                    singleLine = true,
                    isError = !powerValid,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                DurationPicker(
                    hours = pickerHours,
                    minutes = pickerMinutes,
                    onChanged = { h, m -> pickerHours = h; pickerMinutes = m }
                )
                Spacer(modifier = Modifier.height(12.dp))
                val selectedLabel = applianceIcons.firstOrNull { it.id == selectedIcon }?.let { stringResource(it.labelRes) } ?: ""
                Text(
                    text = if (selectedLabel.isNotEmpty()) "${stringResource(R.string.dialog_icon)} - $selectedLabel"
                           else stringResource(R.string.dialog_icon),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    applianceIcons.forEach { entry ->
                        val isSelected = entry.id == selectedIcon
                        val shape = RoundedCornerShape(8.dp)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(shape)
                                .then(
                                    if (isSelected) Modifier.border(
                                        2.dp,
                                        MaterialTheme.colorScheme.primary,
                                        shape
                                    ) else Modifier
                                )
                                .clickable { selectedIcon = entry.id },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(entry.iconRes),
                                contentDescription = stringResource(entry.labelRes),
                                modifier = Modifier.size(22.dp),
                                tint = if (isSelected) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name.trim(), pickerHours, pickerMinutes, selectedIcon, powerKw?.takeIf { powerText.isNotBlank() }) },
                enabled = name.isNotBlank() && (pickerHours > 0 || pickerMinutes > 0) && powerValid
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
