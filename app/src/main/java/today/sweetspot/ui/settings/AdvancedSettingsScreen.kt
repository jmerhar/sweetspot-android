package today.sweetspot.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import today.sweetspot.R
import today.sweetspot.data.api.DataSource
import java.time.ZoneId

/**
 * Advanced settings screen containing data source preferences, cache management,
 * and developer options. Opened from the main settings screen via the "Advanced" row.
 *
 * @param sourceOrder Current data source priority order, or null for defaults.
 * @param disabledSources Set of source IDs the user has disabled.
 * @param availableSources All data sources available for the current zone.
 * @param onSourceOrderChanged Called when the user reorders data sources.
 * @param onDisabledSourcesChanged Called when the user toggles a data source.
 * @param onResetSourceOrder Called when the user resets to default source order.
 * @param onClearCache Called when the user taps "Clear cache". Returns a snackbar message.
 * @param devOptionsEnabled Whether developer options are unlocked.
 * @param isCooldownDisabled Whether the API fetch cooldown is currently bypassed.
 * @param onDevCooldownDisabledChanged Called when the cooldown toggle changes.
 * @param onDevResetUnlock Called when the developer taps "Reset unlock state".
 * @param onDevResetStatsTimer Called when the developer taps "Reset stats timer".
 * @param timeOverrideMs Current time override as epoch millis, or `null` if using real time.
 * @param onDevTimeOverrideChanged Called with epoch millis to set, or `null` to clear the override.
 * @param timeZoneId Current timezone for displaying the override datetime.
 * @param onBack Called when the user navigates back.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AdvancedSettingsScreen(
    sourceOrder: List<String>?,
    disabledSources: Set<String>,
    availableSources: List<DataSource>,
    onSourceOrderChanged: (List<String>) -> Unit,
    onDisabledSourcesChanged: (Set<String>) -> Unit,
    onResetSourceOrder: () -> Unit,
    onClearCache: () -> String,
    devOptionsEnabled: Boolean,
    isCooldownDisabled: Boolean,
    onDevCooldownDisabledChanged: (Boolean) -> Unit,
    onDevResetUnlock: () -> Unit,
    onDevResetStatsTimer: () -> Unit,
    timeOverrideMs: Long?,
    onDevTimeOverrideChanged: (Long?) -> Unit,
    timeZoneId: ZoneId,
    onBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_advanced)) },
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            if (availableSources.size >= 2) {
                DataSourcesSection(
                    sourceOrder = sourceOrder,
                    disabledSources = disabledSources,
                    availableSources = availableSources,
                    onSourceOrderChanged = onSourceOrderChanged,
                    onDisabledSourcesChanged = onDisabledSourcesChanged,
                    onResetSourceOrder = onResetSourceOrder
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = {
                        val message = onClearCache()
                        coroutineScope.launch { snackbarHostState.showSnackbar(message) }
                    })
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_clear_cache),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = stringResource(R.string.settings_clear_cache_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (devOptionsEnabled) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                DeveloperSection(
                    isCooldownDisabled = isCooldownDisabled,
                    onCooldownDisabledChanged = onDevCooldownDisabledChanged,
                    onResetUnlock = {
                        onDevResetUnlock()
                        coroutineScope.launch { snackbarHostState.showSnackbar("Unlock state reset") }
                    },
                    onResetStatsTimer = {
                        onDevResetStatsTimer()
                        coroutineScope.launch { snackbarHostState.showSnackbar("Stats timer reset") }
                    },
                    timeOverrideMs = timeOverrideMs,
                    onTimeOverrideChanged = onDevTimeOverrideChanged,
                    timeZoneId = timeZoneId
                )
            }
        }
    }
}
