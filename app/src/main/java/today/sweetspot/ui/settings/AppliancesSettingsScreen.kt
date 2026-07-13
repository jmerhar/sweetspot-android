package today.sweetspot.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import today.sweetspot.R
import today.sweetspot.model.Appliance
import today.sweetspot.model.ApplianceSort
import today.sweetspot.model.ApplianceUsage

/**
 * Sub-screen listing the user's (non-EV) appliances in the active order, with sort controls,
 * drag-to-reorder in Custom mode, add/edit/delete via [ApplianceDialog], and a reset-usage action.
 *
 * @param appliances Non-EV appliances already in the active sort order.
 * @param sort The active ordering.
 * @param usage Combined tap statistics (for collision-gated tie-breakers).
 * @param onAddAppliance Called to add a new appliance.
 * @param onUpdateAppliance Called to persist edits to an existing appliance.
 * @param onDeleteAppliance Called with an appliance id to delete it.
 * @param onSortChanged Called to persist a changed ordering.
 * @param onReorder Called with the reordered non-EV list in Custom mode.
 * @param onResetUsage Called to clear tap-usage history.
 * @param onBack Called to return to the settings menu.
 */
@Composable
internal fun AppliancesSettingsScreen(
    appliances: List<Appliance>,
    sort: ApplianceSort,
    usage: Map<String, ApplianceUsage>,
    onAddAppliance: (name: String, durationHours: Int, durationMinutes: Int, icon: String, powerKw: Double?) -> Unit,
    onUpdateAppliance: (Appliance) -> Unit,
    onDeleteAppliance: (id: String) -> Unit,
    onSortChanged: (ApplianceSort) -> Unit,
    onReorder: (List<Appliance>) -> Unit,
    onResetUsage: () -> Unit,
    onBack: () -> Unit
) {
    var editingAppliance by remember { mutableStateOf<Appliance?>(null) }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    if (showAddDialog) {
        ApplianceDialog(
            appliance = null,
            onSave = { name, durationHours, durationMinutes, icon, powerKw ->
                onAddAppliance(name, durationHours, durationMinutes, icon, powerKw)
                showAddDialog = false
            },
            onDelete = null,
            onDismiss = { showAddDialog = false }
        )
    }

    editingAppliance?.let { appliance ->
        ApplianceDialog(
            appliance = appliance,
            onSave = { name, durationHours, durationMinutes, icon, powerKw ->
                onUpdateAppliance(appliance.copy(name = name, durationHours = durationHours, durationMinutes = durationMinutes, icon = icon, powerKw = powerKw))
                editingAppliance = null
            },
            onDelete = {
                onDeleteAppliance(appliance.id)
                editingAppliance = null
            },
            onDismiss = { editingAppliance = null }
        )
    }

    SettingsSubScreen(title = stringResource(R.string.settings_appliances), onBack = onBack) {
        AppliancesSection(
            appliances = appliances,
            sort = sort,
            usage = usage,
            onApplianceClick = { editingAppliance = it },
            onAddClick = { showAddDialog = true },
            onSortChanged = onSortChanged,
            onReorder = onReorder,
            onResetUsage = onResetUsage
        )
    }
}
