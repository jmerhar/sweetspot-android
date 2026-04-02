@file:Suppress("AssignedValueIsNeverRead")

package today.sweetspot.ui.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Hidden developer options section with reset buttons and toggles for testing.
 *
 * Shown persistently once unlocked via 7-tap on the version number.
 *
 * @param isCooldownDisabled Whether the API fetch cooldown is currently bypassed.
 * @param onCooldownDisabledChanged Called when the cooldown toggle changes.
 * @param onResetUnlock Called when the developer taps "Reset unlock state". Should return a confirmation message.
 * @param onResetStatsTimer Called when the developer taps "Reset stats timer". Should return a confirmation message.
 * @param timeOverrideMs Current time override as epoch millis, or `null` if using real time.
 * @param onTimeOverrideChanged Called with epoch millis to set, or `null` to clear the override.
 * @param timeZoneId Current timezone for displaying the override datetime.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun DeveloperSection(
    isCooldownDisabled: Boolean,
    onCooldownDisabledChanged: (Boolean) -> Unit,
    onResetUnlock: () -> Unit,
    onResetStatsTimer: () -> Unit,
    timeOverrideMs: Long?,
    onTimeOverrideChanged: (Long?) -> Unit,
    timeZoneId: ZoneId
) {
    Text(
        text = "Developer",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )

    DevActionRow(
        label = "Reset unlock state",
        description = "Clears local unlock flag, re-enables paywall",
        onClick = onResetUnlock
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { onCooldownDisabledChanged(!isCooldownDisabled) })
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Disable API cooldown",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Skip the 5-minute rate limit between API requests",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = isCooldownDisabled,
            onCheckedChange = onCooldownDisabledChanged
        )
    }

    DevActionRow(
        label = "Reset stats timer",
        description = "Allows immediate stats reporting",
        onClick = onResetStatsTimer
    )

    // --- Time override ---

    var showDatePicker by remember { mutableStateOf(false) }
    var pickedDateMs by remember { mutableLongStateOf(0L) }
    var showTimePicker by remember { mutableStateOf(false) }

    val overrideLabel = if (timeOverrideMs != null) {
        val zdt = Instant.ofEpochMilli(timeOverrideMs).atZone(timeZoneId)
        zdt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    } else {
        "Off"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { showDatePicker = true },
                onLongClick = { onTimeOverrideChanged(null) }
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Time override",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = overrideLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = if (timeOverrideMs != null) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showDatePicker) {
        val initialMs = timeOverrideMs ?: System.currentTimeMillis()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMs)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedMs = datePickerState.selectedDateMillis
                    if (selectedMs != null) {
                        pickedDateMs = selectedMs
                        showDatePicker = false
                        showTimePicker = true
                    }
                }) { Text("Next") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                if (timeOverrideMs != null) {
                    TextButton(onClick = {
                        onTimeOverrideChanged(null)
                        showDatePicker = false
                    }) { Text("Clear") }
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val existing = if (timeOverrideMs != null) {
            Instant.ofEpochMilli(timeOverrideMs).atZone(timeZoneId)
        } else {
            ZonedDateTime.now(timeZoneId)
        }
        val timePickerState = rememberTimePickerState(
            initialHour = existing.hour,
            initialMinute = existing.minute,
            is24Hour = true
        )
        DatePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    // Combine the picked date (UTC midnight) with the picked time in the user's timezone
                    val dateAtUtcMidnight = Instant.ofEpochMilli(pickedDateMs).atZone(ZoneId.of("UTC")).toLocalDate()
                    val combined = dateAtUtcMidnight
                        .atTime(timePickerState.hour, timePickerState.minute)
                        .atZone(timeZoneId)
                    onTimeOverrideChanged(combined.toInstant().toEpochMilli())
                    showTimePicker = false
                }) { Text("Set") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

/** A single developer action row with label, description, and tap handler. */
@Composable
internal fun DevActionRow(label: String, description: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
