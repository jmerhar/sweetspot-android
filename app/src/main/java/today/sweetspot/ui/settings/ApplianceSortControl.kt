package today.sweetspot.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import today.sweetspot.R
import today.sweetspot.model.Appliance
import today.sweetspot.model.ApplianceSort
import today.sweetspot.model.ApplianceUsage
import today.sweetspot.model.SortKey
import today.sweetspot.util.hasCollisions
import today.sweetspot.util.nextAssignableKeys
import today.sweetspot.util.withAddedTiebreaker
import today.sweetspot.util.withLevelKey
import today.sweetspot.util.withPrimary
import today.sweetspot.util.withToggledDirection
import today.sweetspot.util.withoutLevel

/** The user-facing label for a [SortKey]. */
@StringRes
internal fun sortKeyLabelRes(key: SortKey): Int = when (key) {
    SortKey.CUSTOM -> R.string.sort_key_custom
    SortKey.FREQUENCY -> R.string.sort_key_frequency
    SortKey.RECENCY -> R.string.sort_key_recency
    SortKey.NAME -> R.string.sort_key_name
    SortKey.DURATION -> R.string.sort_key_duration
    SortKey.TYPE -> R.string.sort_key_type
}

/**
 * The appliance sort control: a primary criterion plus collision-gated tie-breakers.
 *
 * Each level shows its key (tap to change) and, for derived keys, a direction toggle; tie-breaker
 * levels can be removed. An "add tie-breaker" affordance appears only while the current criteria
 * still leave ties and an unused key remains. Choosing **Custom** as primary collapses to manual
 * order (drag handles appear in the list below). All ordering decisions come from tested pure
 * helpers in `:shared`.
 *
 * @param sort The active ordering.
 * @param appliances The non-EV appliances (used only to detect remaining ties).
 * @param usage Combined tap statistics feeding Frequency/Recency.
 * @param onSortChanged Called with the new ordering after any edit.
 */
@Composable
internal fun ApplianceSortControl(
    sort: ApplianceSort,
    appliances: List<Appliance>,
    usage: Map<String, ApplianceUsage>,
    onSortChanged: (ApplianceSort) -> Unit,
) {
    // Level index being edited via the key picker; -1 = adding a tie-breaker; null = closed.
    var pickerLevel by remember { mutableStateOf<Int?>(null) }

    Text(
        text = stringResource(R.string.settings_sort_title),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )

    sort.criteria.forEachIndexed { index, criterion ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(if (index == 0) R.string.sort_by else R.string.sort_then_by),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(72.dp)
            )
            TextButton(onClick = { pickerLevel = index }) {
                Text(stringResource(sortKeyLabelRes(criterion.key)))
            }
            if (criterion.key != SortKey.CUSTOM) {
                IconButton(onClick = { onSortChanged(sort.withToggledDirection(index)) }) {
                    Icon(
                        imageVector = if (criterion.descending) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                        contentDescription = stringResource(
                            if (criterion.descending) R.string.sort_direction_descending else R.string.sort_direction_ascending
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            if (index > 0) {
                IconButton(onClick = { onSortChanged(sort.withoutLevel(index)) }) {
                    Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.cd_sort_remove_level))
                }
            }
        }
    }

    val canAddTiebreaker = !sort.isCustom &&
        hasCollisions(appliances, sort.criteria, usage) &&
        nextAssignableKeys(sort.criteria).isNotEmpty()
    if (canAddTiebreaker) {
        TextButton(
            onClick = { pickerLevel = -1 },
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text(stringResource(R.string.sort_add_tiebreaker))
        }
    }

    pickerLevel?.let { level ->
        val options = when {
            level < 0 -> nextAssignableKeys(sort.criteria)
            level == 0 -> SortKey.entries.toList()
            else -> (listOf(sort.criteria[level].key) + nextAssignableKeys(sort.criteria)).distinct()
        }
        SortKeyPickerDialog(
            options = options,
            selected = if (level < 0) null else sort.criteria[level].key,
            onPick = { key ->
                val updated = when {
                    level < 0 -> sort.withAddedTiebreaker(key)
                    level == 0 -> sort.withPrimary(key)
                    else -> sort.withLevelKey(level, key)
                }
                onSortChanged(updated)
                pickerLevel = null
            },
            onDismiss = { pickerLevel = null }
        )
    }
}

/** Radio-list dialog for choosing a sort key; tapping an option applies it immediately. */
@Composable
private fun SortKeyPickerDialog(
    options: List<SortKey>,
    selected: SortKey?,
    onPick: (SortKey) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.sort_by)) },
        text = {
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                options.forEach { key ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected = key == selected, role = Role.RadioButton, onClick = { onPick(key) })
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = key == selected, onClick = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(stringResource(sortKeyLabelRes(key)))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}
