package today.sweetspot.ui.share

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import today.sweetspot.ImportMode
import today.sweetspot.R
import today.sweetspot.model.Appliance
import today.sweetspot.model.SharedSetup
import today.sweetspot.model.applianceIconFor

/**
 * Full-screen preview shown when the app is opened by a scanned/tapped setup link. Summarises the
 * incoming setup, lets the user choose how to merge it (add / replace / pick), and applies it.
 *
 * @param setup The decoded incoming setup.
 * @param onImport Called with the chosen [ImportMode] and, for [ImportMode.PICK], the ids of the
 *        appliances the user ticked (the sender's ids as shown here).
 * @param onCancel Dismisses the preview without importing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportPreviewScreen(
    setup: SharedSetup,
    onImport: (ImportMode, Set<String>) -> Unit,
    onCancel: () -> Unit
) {
    BackHandler { onCancel() }

    var mode by rememberSaveable { mutableStateOf(ImportMode.ADD) }
    // Which incoming appliances are ticked for a "pick" import (all selected initially).
    val picked: SnapshotStateList<String> =
        remember(setup) { setup.appliances.map { it.id }.toMutableStateList() }

    val importCount = if (mode == ImportMode.PICK) picked.size else setup.appliances.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.import_title)) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ImportSummary(setup)

            HorizontalDivider()

            ModeOption(
                selected = mode == ImportMode.ADD,
                title = stringResource(R.string.import_mode_add),
                description = stringResource(R.string.import_mode_add_desc),
                onSelect = { mode = ImportMode.ADD }
            )
            ModeOption(
                selected = mode == ImportMode.REPLACE,
                title = stringResource(R.string.import_mode_replace),
                description = stringResource(R.string.import_mode_replace_desc),
                onSelect = { mode = ImportMode.REPLACE }
            )
            ModeOption(
                selected = mode == ImportMode.PICK,
                title = stringResource(R.string.import_mode_pick),
                description = stringResource(R.string.import_mode_pick_desc),
                onSelect = { mode = ImportMode.PICK }
            )

            if (mode == ImportMode.PICK) {
                setup.appliances.forEach { appliance ->
                    ApplianceCheckRow(
                        appliance = appliance,
                        checked = appliance.id in picked,
                        onToggle = { checked ->
                            if (checked) picked.add(appliance.id) else picked.remove(appliance.id)
                        }
                    )
                }
            }

            Button(
                onClick = {
                    val ids = if (mode == ImportMode.PICK) picked.toSet() else emptySet()
                    onImport(mode, ids)
                },
                enabled = importCount > 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(pluralStringResource(R.plurals.import_button, importCount, importCount))
            }
            OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    }
}

/** A one/two-line summary of the incoming setup: appliance count and, if any, vehicle count. */
@Composable
private fun ImportSummary(setup: SharedSetup) {
    val vehicles = setup.appliances.count { it.isEv }
    Column {
        Text(
            text = pluralStringResource(
                R.plurals.import_appliance_count,
                setup.appliances.size,
                setup.appliances.size
            ),
            style = MaterialTheme.typography.bodyLarge
        )
        if (vehicles > 0) {
            Text(
                text = pluralStringResource(R.plurals.import_vehicle_count, vehicles, vehicles),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** A selectable merge-mode row: a radio button, a title, and a one-line description. */
@Composable
private fun ModeOption(
    selected: Boolean,
    title: String,
    description: String,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onSelect),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** A checkbox row for a single incoming appliance in the "pick" list: icon, name, checkbox. */
@Composable
private fun ApplianceCheckRow(
    appliance: Appliance,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!checked) }
            .padding(start = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(applianceIconFor(appliance)),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(appliance.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Checkbox(checked = checked, onCheckedChange = onToggle)
    }
}
