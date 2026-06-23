package today.sweetspot.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import today.sweetspot.R
import today.sweetspot.util.formatDuration
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

/** Formats a kW value, dropping the decimal for whole numbers (11.0 → "11", 7.4 → "7.4"). */
internal fun formatKw(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString()
    else String.format(Locale.getDefault(), "%.1f", value)

/** Formats an hour/minute pair as a 24-hour "HH:mm" label. */
internal fun formatHhMm(hour: Int, minute: Int): String =
    String.format(Locale.getDefault(), "%02d:%02d", hour, minute)

/**
 * A universal "ready by" deadline control: a switch plus a tappable time chip. Tapping the chip
 * opens a [TimePickerDialog]. Used on the main form (and reusable elsewhere) to constrain a search
 * so the chosen window finishes by the given time.
 *
 * @param enabled Whether the deadline is active.
 * @param hour Deadline hour of day (0–23).
 * @param minute Deadline minute (0–59).
 * @param onEnabledChange Called when the switch is toggled.
 * @param onTimeChange Called with the new hour/minute when the user picks a time.
 */
@Composable
fun DeadlineRow(
    enabled: Boolean,
    hour: Int,
    minute: Int,
    onEnabledChange: (Boolean) -> Unit,
    onTimeChange: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.ev_deadline_toggle),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        if (enabled) {
            AssistChip(
                onClick = { showPicker = true },
                label = { Text(formatHhMm(hour, minute)) }
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        Switch(checked = enabled, onCheckedChange = onEnabledChange)
    }

    if (showPicker) {
        TimePickerDialog(
            initialHour = hour,
            initialMinute = minute,
            onConfirm = { h, m -> onTimeChange(h, m); showPicker = false },
            onDismiss = { showPicker = false }
        )
    }
}

/**
 * Prompts for a state-of-charge range when an EV-type appliance is tapped, shows the resulting
 * charging time, and runs the search on confirm.
 *
 * @param vehicleName The vehicle's display name.
 * @param batteryKwh Usable battery capacity in kWh.
 * @param acMaxPowerKw The vehicle's maximum AC charging power in kW.
 * @param homeChargerKw The user's home charger output in kW.
 * @param initialCurrentSoc Prefilled current state of charge (%).
 * @param initialTargetSoc Prefilled target state of charge (%).
 * @param deadlineHint A "ready by HH:mm" hint shown when a deadline is active, or `null`.
 * @param onConfirm Called with the chosen current/target SoC.
 * @param onDismiss Called when the dialog is dismissed.
 */
@Composable
fun SocDialog(
    vehicleName: String,
    batteryKwh: Double,
    acMaxPowerKw: Double,
    homeChargerKw: Double,
    initialCurrentSoc: Int,
    initialTargetSoc: Int,
    deadlineHint: String?,
    onConfirm: (current: Int, target: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val resources = LocalContext.current.resources
    var current by rememberSaveable { mutableIntStateOf(initialCurrentSoc) }
    var target by rememberSaveable { mutableIntStateOf(initialTargetSoc) }

    val power = minOf(acMaxPowerKw, homeChargerKw)
    val valid = target > current && power > 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(vehicleName) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.ev_current_soc, current),
                    style = MaterialTheme.typography.bodyLarge
                )
                Slider(
                    value = current.toFloat(),
                    onValueChange = { current = (it / 5).roundToInt() * 5 },
                    valueRange = 0f..100f,
                    steps = 19
                )
                Text(
                    text = stringResource(R.string.ev_target_soc, target),
                    style = MaterialTheme.typography.bodyLarge
                )
                Slider(
                    value = target.toFloat(),
                    onValueChange = { target = (it / 5).roundToInt() * 5 },
                    valueRange = 0f..100f,
                    steps = 19
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (valid) {
                    val totalMinutes = max(1, ((target - current) / 100.0 * batteryKwh / power * 60).roundToInt())
                    Text(
                        text = stringResource(
                            R.string.ev_charging_estimate,
                            formatDuration(totalMinutes / 60, totalMinutes % 60, resources),
                            formatKw(power)
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = stringResource(R.string.ev_error_invalid_soc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (deadlineHint != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = deadlineHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(current, target) }, enabled = valid) {
                Text(stringResource(R.string.main_find_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}

/** A Material time picker wrapped in a dialog. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timeState = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.ev_deadline_toggle)) },
        text = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                TimePicker(state = timeState)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(timeState.hour, timeState.minute) }) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}
