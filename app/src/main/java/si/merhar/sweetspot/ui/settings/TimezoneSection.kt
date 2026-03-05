package si.merhar.sweetspot.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import si.merhar.sweetspot.R
import java.time.ZoneId

/** Timezone settings section showing the current timezone selection. */
@Composable
internal fun TimezoneSection(
    currentTimeZoneId: ZoneId,
    isUsingDefaultTimezone: Boolean,
    onClick: () -> Unit
) {
    Text(
        text = stringResource(R.string.settings_timezone),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isUsingDefaultTimezone) stringResource(R.string.settings_auto_timezone) else currentTimeZoneId.id.replace('_', ' '),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = if (isUsingDefaultTimezone) currentTimeZoneId.id.replace('_', ' ') else stringResource(R.string.settings_custom_timezone),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Full-screen picker listing all available timezones with a search field and system-default option. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TimezonePickerScreen(
    currentTimeZoneId: ZoneId,
    defaultTimeZoneId: ZoneId,
    isUsingDefaultTimezone: Boolean,
    onTimezoneSelected: (ZoneId?) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val allZoneIds = remember {
        ZoneId.getAvailableZoneIds()
            .filter { it.contains('/') }
            .sorted()
    }

    val filteredZones = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            allZoneIds
        } else {
            val query = searchQuery.lowercase()
            allZoneIds.filter {
                val lower = it.lowercase()
                lower.contains(query) || lower.replace('_', ' ').contains(query)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.picker_timezone_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(stringResource(R.string.picker_timezone_search)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // Auto (from country) option
                item {
                    PickerRow(
                        label = stringResource(R.string.settings_auto_timezone),
                        subtitle = defaultTimeZoneId.id.replace('_', ' '),
                        isSelected = isUsingDefaultTimezone,
                        onClick = { onTimezoneSelected(null) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }

                items(filteredZones) { zoneIdStr ->
                    val isSelected = !isUsingDefaultTimezone && currentTimeZoneId.id == zoneIdStr
                    PickerRow(
                        label = zoneIdStr.replace('_', ' '),
                        subtitle = null,
                        isSelected = isSelected,
                        onClick = { onTimezoneSelected(ZoneId.of(zoneIdStr)) }
                    )
                }
            }
        }
    }
}
