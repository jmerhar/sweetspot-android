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
import today.sweetspot.model.EvPosition
import today.sweetspot.model.EvSpec
import today.sweetspot.model.EvVehicle

/**
 * Sub-screen for EV charging: home charger power, default target charge, home-screen placement,
 * and the list of saved vehicles, with add/edit/delete via [VehicleDialog].
 *
 * @param vehicles The EV-type appliances.
 * @param homeChargerKw Configured home charger power.
 * @param defaultTargetSoc Default target state of charge.
 * @param onHomeChargerChanged Called when the home charger power changes.
 * @param onDefaultTargetChanged Called when the default target charge changes.
 * @param searchVehicles Free-text search over the bundled vehicle database (for the add dialog).
 * @param onAddVehicle Called to save a new vehicle.
 * @param onUpdateAppliance Called to persist edits to an existing vehicle.
 * @param onDeleteAppliance Called with a vehicle id to delete it.
 * @param evPosition Where vehicles are placed on the home screen.
 * @param evSeparate Whether the vehicle block is drawn as its own section.
 * @param sortIsCustom Whether the appliance sort is manual (disables Interleaved).
 * @param onEvPositionChanged Called when the placement changes.
 * @param onEvSeparateChanged Called when the separate-section toggle changes.
 * @param onBack Called to return to the settings menu.
 */
@Composable
internal fun EvSettingsScreen(
    vehicles: List<Appliance>,
    homeChargerKw: Double,
    defaultTargetSoc: Int,
    onHomeChargerChanged: (Double) -> Unit,
    onDefaultTargetChanged: (Int) -> Unit,
    searchVehicles: (String) -> List<EvVehicle>,
    onAddVehicle: (name: String, batteryKwh: Double, acPowerKw: Double) -> Unit,
    onUpdateAppliance: (Appliance) -> Unit,
    onDeleteAppliance: (id: String) -> Unit,
    evPosition: EvPosition,
    evSeparate: Boolean,
    sortIsCustom: Boolean,
    onEvPositionChanged: (EvPosition) -> Unit,
    onEvSeparateChanged: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    var editingVehicle by remember { mutableStateOf<Appliance?>(null) }
    var showAddVehicleDialog by rememberSaveable { mutableStateOf(false) }

    if (showAddVehicleDialog) {
        VehicleDialog(
            vehicle = null,
            searchVehicles = searchVehicles,
            onSave = { name, batteryKwh, acPowerKw ->
                onAddVehicle(name, batteryKwh, acPowerKw)
                showAddVehicleDialog = false
            },
            onDelete = null,
            onDismiss = { showAddVehicleDialog = false }
        )
    }

    editingVehicle?.let { vehicle ->
        VehicleDialog(
            vehicle = vehicle,
            searchVehicles = searchVehicles,
            onSave = { name, batteryKwh, acPowerKw ->
                onUpdateAppliance(vehicle.copy(name = name, ev = EvSpec(batteryKwh, acPowerKw)))
                editingVehicle = null
            },
            onDelete = {
                onDeleteAppliance(vehicle.id)
                editingVehicle = null
            },
            onDismiss = { editingVehicle = null }
        )
    }

    SettingsSubScreen(title = stringResource(R.string.settings_ev_title), onBack = onBack) {
        EvSection(
            vehicles = vehicles,
            homeChargerKw = homeChargerKw,
            defaultTargetSoc = defaultTargetSoc,
            onHomeChargerChanged = onHomeChargerChanged,
            onDefaultTargetChanged = onDefaultTargetChanged,
            onVehicleClick = { editingVehicle = it },
            onAddVehicleClick = { showAddVehicleDialog = true },
            evPosition = evPosition,
            evSeparate = evSeparate,
            sortIsCustom = sortIsCustom,
            onEvPositionChanged = onEvPositionChanged,
            onEvSeparateChanged = onEvSeparateChanged
        )
    }
}
