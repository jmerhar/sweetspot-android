package si.merhar.sweetspot.ui.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import si.merhar.sweetspot.R
import si.merhar.sweetspot.data.api.DataSource

/**
 * Data sources settings section for configuring API priority order.
 *
 * Shows each available source with a toggle switch and up/down reorder buttons.
 * Sources maintain their position when toggled — disabling a source does not move it.
 * The last enabled source's switch is disabled to prevent disabling all sources.
 * A "Reset to defaults" button appears when the configuration differs from zone defaults.
 */
@Composable
internal fun DataSourcesSection(
    sourceOrder: List<String>?,
    disabledSources: Set<String>,
    availableSources: List<DataSource>,
    onSourceOrderChanged: (List<String>) -> Unit,
    onDisabledSourcesChanged: (Set<String>) -> Unit,
    onResetSourceOrder: () -> Unit
) {
    val defaults = availableSources.map { it.id }
    val displayOrder = sourceOrder?.filter { id -> availableSources.any { it.id == id } } ?: defaults
    val enabledIds = displayOrder.filter { it !in disabledSources }

    Text(
        text = stringResource(R.string.settings_data_sources),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )

    Text(
        text = stringResource(R.string.settings_data_sources_description),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp)
    )

    displayOrder.forEachIndexed { index, sourceId ->
        val source = availableSources.find { it.id == sourceId } ?: return@forEachIndexed
        val isEnabled = sourceId !in disabledSources
        val isLastEnabled = isEnabled && enabledIds.size == 1

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = isEnabled,
                onCheckedChange = { checked ->
                    if (checked) {
                        onDisabledSourcesChanged(disabledSources - sourceId)
                    } else if (!isLastEnabled) {
                        onDisabledSourcesChanged(disabledSources + sourceId)
                    }
                },
                enabled = !isLastEnabled
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = source.displayName,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                color = if (isEnabled) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
            IconButton(
                onClick = {
                    if (index > 0) {
                        val reordered = displayOrder.toMutableList()
                        reordered.removeAt(index)
                        reordered.add(index - 1, sourceId)
                        onSourceOrderChanged(reordered)
                    }
                },
                enabled = index > 0
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = stringResource(R.string.cd_move_up)
                )
            }
            IconButton(
                onClick = {
                    if (index < displayOrder.size - 1) {
                        val reordered = displayOrder.toMutableList()
                        reordered.removeAt(index)
                        reordered.add(index + 1, sourceId)
                        onSourceOrderChanged(reordered)
                    }
                },
                enabled = index < displayOrder.size - 1
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.cd_move_down)
                )
            }
        }
    }

    if (sourceOrder != null || disabledSources.isNotEmpty()) {
        val isCustomized = (sourceOrder != null && sourceOrder != defaults) || disabledSources.isNotEmpty()
        if (isCustomized) {
            TextButton(
                onClick = onResetSourceOrder,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(stringResource(R.string.settings_reset_defaults))
            }
        }
    }
}
