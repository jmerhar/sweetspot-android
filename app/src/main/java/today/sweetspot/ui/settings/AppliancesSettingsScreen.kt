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

/**
 * Sub-screen listing the user's (non-EV) appliances, with add/edit/delete via [ApplianceDialog].
 *
 * @param appliances All appliances; EV vehicles are filtered out (they live on the EV screen).
 * @param onAddAppliance Called to add a new appliance.
 * @param onUpdateAppliance Called to persist edits to an existing appliance.
 * @param onDeleteAppliance Called with an appliance id to delete it.
 * @param onBack Called to return to the settings menu.
 */
@Composable
internal fun AppliancesSettingsScreen(
    appliances: List<Appliance>,
    onAddAppliance: (name: String, durationHours: Int, durationMinutes: Int, icon: String, powerKw: Double?) -> Unit,
    onUpdateAppliance: (Appliance) -> Unit,
    onDeleteAppliance: (id: String) -> Unit,
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
            appliances = appliances.filterNot { it.isEv },
            onApplianceClick = { editingAppliance = it },
            onAddClick = { showAddDialog = true }
        )
    }
}
