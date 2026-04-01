package today.sweetspot.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import today.sweetspot.R
import androidx.compose.ui.res.stringResource

/**
 * Advanced settings section with cache management and optional stats toggle.
 *
 * @param isStatsEnabled Whether API stats collection is currently enabled.
 * @param onStatsEnabledChanged Called when the user toggles the stats switch.
 * @param onClearCache Called when the user taps "Clear cache".
 */
@Composable
internal fun AdvancedSection(
    isStatsEnabled: Boolean,
    onStatsEnabledChanged: (Boolean) -> Unit,
    onClearCache: () -> Unit
) {
    Text(
        text = stringResource(R.string.settings_advanced),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClearCache)
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { onStatsEnabledChanged(!isStatsEnabled) })
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.settings_stats_title),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = stringResource(R.string.settings_stats_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = isStatsEnabled,
            onCheckedChange = onStatsEnabledChanged
        )
    }
}

/**
 * Hidden developer options section with reset buttons and toggles for testing.
 *
 * Shown persistently once unlocked via 7-tap on the version number.
 *
 * @param isCooldownDisabled Whether the API fetch cooldown is currently bypassed.
 * @param onCooldownDisabledChanged Called when the cooldown toggle changes.
 * @param onResetUnlock Called when the developer taps "Reset unlock state". Should return a confirmation message.
 * @param onResetStatsTimer Called when the developer taps "Reset stats timer". Should return a confirmation message.
 */
@Composable
internal fun DeveloperSection(
    isCooldownDisabled: Boolean,
    onCooldownDisabledChanged: (Boolean) -> Unit,
    onResetUnlock: () -> Unit,
    onResetStatsTimer: () -> Unit
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
}

/** A single developer action row with label, description, and tap handler. */
@Composable
private fun DevActionRow(label: String, description: String, onClick: () -> Unit) {
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
