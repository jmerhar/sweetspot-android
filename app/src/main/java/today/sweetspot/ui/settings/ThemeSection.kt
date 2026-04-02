@file:Suppress("AssignedValueIsNeverRead")

package today.sweetspot.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import today.sweetspot.R
import today.sweetspot.ThemeMode

/**
 * Theme settings section showing the current theme mode as a clickable row.
 *
 * Tapping opens a dialog with three radio options: System, Light, Dark.
 *
 * @param themeMode The current [ThemeMode].
 * @param onThemeModeChanged Called with the selected [ThemeMode].
 */
@Composable
internal fun ThemeSection(
    themeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }

    val displayName = when (themeMode) {
        ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
        ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
        ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
    }

    Text(
        text = stringResource(R.string.settings_theme),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { showDialog = true })
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }

    if (showDialog) {
        ThemePickerDialog(
            currentMode = themeMode,
            onModeSelected = { mode ->
                onThemeModeChanged(mode)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

/**
 * Dialog with three radio options for selecting the theme mode.
 *
 * @param currentMode The currently selected [ThemeMode].
 * @param onModeSelected Called when the user taps a mode option.
 * @param onDismiss Called when the dialog is dismissed.
 */
@Composable
private fun ThemePickerDialog(
    currentMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        ThemeMode.SYSTEM to stringResource(R.string.settings_theme_system),
        ThemeMode.LIGHT to stringResource(R.string.settings_theme_light),
        ThemeMode.DARK to stringResource(R.string.settings_theme_dark)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_theme)) },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                options.forEach { (mode, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = mode == currentMode,
                                onClick = { onModeSelected(mode) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = mode == currentMode,
                            onClick = null
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
