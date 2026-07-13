package today.sweetspot.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import kotlin.math.roundToInt
import today.sweetspot.R
import today.sweetspot.model.Appliance
import today.sweetspot.model.EvVehicle
import today.sweetspot.model.applianceIconFor
import today.sweetspot.util.formatKw

/** Common home charger power presets (kW). */
private val CHARGER_PRESETS = listOf(3.7, 7.4, 11.0, 22.0)

/**
 * EV charging settings section: home charger power, default target charge, and the list of saved
 * vehicles (with add/edit). Vehicles are stored as EV-type [Appliance]s and also appear as chips
 * on the home screen alongside ordinary appliances.
 *
 * @param vehicles The EV-type appliances.
 * @param homeChargerKw Current home charger output in kW.
 * @param defaultTargetSoc Default target state of charge (%).
 * @param onHomeChargerChanged Called when the charger power changes.
 * @param onDefaultTargetChanged Called when the default target changes.
 * @param onVehicleClick Called when a vehicle row is tapped (to edit).
 * @param onAddVehicleClick Called when "Add vehicle" is tapped.
 */
@Composable
internal fun EvSection(
    vehicles: List<Appliance>,
    homeChargerKw: Double,
    defaultTargetSoc: Int,
    onHomeChargerChanged: (Double) -> Unit,
    onDefaultTargetChanged: (Int) -> Unit,
    onVehicleClick: (Appliance) -> Unit,
    onAddVehicleClick: () -> Unit
) {
    Text(
        text = stringResource(R.string.settings_ev_title),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )

    // Home charger power
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(stringResource(R.string.ev_home_charger), style = MaterialTheme.typography.bodyLarge)
        Text(
            text = stringResource(R.string.ev_home_charger_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CHARGER_PRESETS.forEach { preset ->
                FilterChip(
                    selected = homeChargerKw == preset,
                    onClick = { onHomeChargerChanged(preset) },
                    label = { Text("${formatKw(preset)} kW") }
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = formatKw(homeChargerKw),
            onValueChange = { text ->
                text.replace(',', '.').toDoubleOrNull()?.let { onHomeChargerChanged(it) }
            },
            label = { Text(stringResource(R.string.ev_charger_custom)) },
            suffix = { Text("kW") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
    }

    // Default target charge
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = stringResource(R.string.ev_default_target, defaultTargetSoc),
            style = MaterialTheme.typography.bodyLarge
        )
        Slider(
            value = defaultTargetSoc.toFloat(),
            onValueChange = { onDefaultTargetChanged((it / 5).roundToInt() * 5) },
            valueRange = 0f..100f,
            steps = 19
        )
    }

    // Saved vehicles
    vehicles.forEach { vehicle ->
        val spec = vehicle.ev ?: return@forEach
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onVehicleClick(vehicle) }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(applianceIconFor(vehicle)),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(vehicle.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = stringResource(
                        R.string.ev_vehicle_specs,
                        formatKw(spec.batteryKwh),
                        formatKw(spec.acMaxPowerKw)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAddVehicleClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.ev_add_vehicle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }

    Text(
        text = stringResource(R.string.ev_attribution),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

/**
 * Dialog for adding or editing a vehicle. Searching the bundled database fills the specs; if the
 * vehicle isn't listed, the name/battery/power fields can be filled in manually (a custom vehicle).
 *
 * @param vehicle The vehicle being edited, or `null` when adding.
 * @param searchVehicles Synchronous database search over brand/model/variant.
 * @param onSave Called with the resolved name, battery (kWh), and AC power (kW).
 * @param onDelete Called to delete the vehicle (only when editing), or `null`.
 * @param onDismiss Called when the dialog is dismissed.
 */
@Composable
internal fun VehicleDialog(
    vehicle: Appliance?,
    searchVehicles: (String) -> List<EvVehicle>,
    onSave: (name: String, batteryKwh: Double, acPowerKw: Double) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf(vehicle?.name ?: "") }
    var batteryText by rememberSaveable { mutableStateOf(vehicle?.ev?.batteryKwh?.let { formatKw(it) } ?: "") }
    var powerText by rememberSaveable { mutableStateOf(vehicle?.ev?.acMaxPowerKw?.let { formatKw(it) } ?: "") }
    var query by rememberSaveable { mutableStateOf("") }

    val results = searchVehicles(query)
    val battery = batteryText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val power = powerText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val valid = name.isNotBlank() && battery > 0 && power > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (vehicle == null) stringResource(R.string.dialog_add_vehicle) else stringResource(R.string.dialog_edit_vehicle))
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(stringResource(R.string.ev_search_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (query.isNotBlank()) {
                    results.forEach { v ->
                        Text(
                            text = v.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    name = v.displayName
                                    batteryText = formatKw(v.batteryKwh)
                                    powerText = formatKw(v.acMaxPowerKw)
                                    query = ""
                                }
                                .padding(vertical = 8.dp)
                        )
                    }
                    if (results.isEmpty()) {
                        Text(
                            text = stringResource(R.string.ev_custom_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.dialog_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = batteryText,
                    onValueChange = { batteryText = it },
                    label = { Text(stringResource(R.string.ev_battery_label)) },
                    suffix = { Text("kWh") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = powerText,
                    onValueChange = { powerText = it },
                    label = { Text(stringResource(R.string.ev_power_label)) },
                    suffix = { Text("kW") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name.trim(), battery, power) }, enabled = valid) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}
