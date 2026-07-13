package today.sweetspot.ui.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import sh.calvin.reorderable.ReorderableColumn
import today.sweetspot.R
import today.sweetspot.data.api.DataSource

/**
 * Data sources settings section for configuring API priority order.
 *
 * Shows each available source with a toggle switch and a drag handle for reordering.
 * Sources maintain their enabled state when reordered; disabling a source does not move it.
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

    // Drag to reorder: local state animates the drag; onSettle persists the new order.
    var items by remember(displayOrder) { mutableStateOf(displayOrder) }
    ReorderableColumn(
        list = items,
        onSettle = { from, to ->
            items = items.toMutableList().apply { add(to, removeAt(from)) }
            onSourceOrderChanged(items)
        },
        modifier = Modifier.fillMaxWidth()
    ) { _, sourceId, _ ->
        key(sourceId) {
            ReorderableItem {
                val source = availableSources.find { it.id == sourceId }
                if (source != null) {
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
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(R.string.cd_drag_handle),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.draggableHandle()
                        )
                    }
                }
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
