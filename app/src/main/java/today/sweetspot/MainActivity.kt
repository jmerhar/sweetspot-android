package today.sweetspot

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import today.sweetspot.data.api.DataSources
import today.sweetspot.ui.PaywallScreen
import today.sweetspot.ui.settings.SettingsScreen
import today.sweetspot.ui.SweetSpotScreen
import today.sweetspot.ui.theme.SweetSpotTheme

/**
 * Entry point for the phone app.
 *
 * Hosts the [SweetSpotTheme] and switches between [SweetSpotScreen] and [SettingsScreen]
 * based on [SweetSpotViewModel] state. Also shows a one-time stats opt-in dialog.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SweetSpotTheme {
                val vm: SweetSpotViewModel = viewModel()
                val state by vm.uiState.collectAsState()

                when {
                    state.showPaywall -> {
                        PaywallScreen(
                            productPrice = state.productPrice,
                            onPurchaseClicked = { vm.onPurchaseClicked(this@MainActivity) },
                            onRestorePurchases = vm::onRestorePurchases
                        )
                    }
                    state.showSettings -> {
                        BackHandler { vm.onHideSettings() }
                        SettingsScreen(
                            currentTimeZoneId = state.timeZoneId,
                            isUsingDefaultTimezone = state.isUsingDefaultTimezone,
                            onTimezoneSelected = vm::onTimezoneSelected,
                            appliances = state.appliances,
                            onAddAppliance = vm::onAddAppliance,
                            onUpdateAppliance = vm::onUpdateAppliance,
                            onDeleteAppliance = vm::onDeleteAppliance,
                            countryCode = state.countryCode,
                            priceZone = state.priceZone,
                            countries = state.countries,
                            onCountrySelected = vm::onCountrySelected,
                            onPriceZoneSelected = vm::onPriceZoneSelected,
                            sourceOrder = state.sourceOrder,
                            disabledSources = state.disabledSources,
                            availableSources = DataSources.defaultsForZone(state.priceZone?.id ?: ""),
                            onSourceOrderChanged = vm::onSourceOrderChanged,
                            onDisabledSourcesChanged = vm::onDisabledSourcesChanged,
                            onResetSourceOrder = vm::onResetSourceOrder,
                            onLanguageChanged = vm::onLanguageChanged,
                            onClearCache = vm::onClearCache,
                            isStatsEnabled = state.isStatsEnabled,
                            onStatsEnabledChanged = vm::onStatsEnabledChanged,
                            isUnlocked = state.isUnlocked,
                            trialDaysRemaining = state.trialDaysRemaining,
                            productPrice = state.productPrice,
                            onPurchaseClicked = { vm.onPurchaseClicked(this@MainActivity) },
                            devOptionsEnabled = state.devOptionsEnabled,
                            isCooldownDisabled = state.isCooldownDisabled,
                            onDevOptionsUnlocked = vm::onDevOptionsUnlocked,
                            onDevResetUnlock = vm::onDevResetUnlock,
                            onDevCooldownDisabledChanged = vm::onDevCooldownDisabledChanged,
                            onDevResetStatsTimer = vm::onDevResetStatsTimer,
                            timeOverrideMs = state.timeOverrideMs,
                            onDevTimeOverrideChanged = vm::onDevTimeOverrideChanged,
                            onBack = vm::onHideSettings
                        )
                    }
                    else -> {
                        SweetSpotScreen(viewModel = vm)
                    }
                }

                // Overlay dialogs shown on any screen (except the paywall)
                if (!state.showPaywall) {
                    ThankYouDialog(state, vm)
                    StatsPromptDialog(state, vm)
                }
            }
        }
    }
}

/** Shows a thank-you dialog after a successful in-app purchase. */
@Composable
private fun ThankYouDialog(state: UiState, vm: SweetSpotViewModel) {
    if (!state.showThankYou) return
    AlertDialog(
        onDismissRequest = vm::onThankYouDismissed,
        title = { Text(stringResource(R.string.thank_you_title)) },
        text = { Text(stringResource(R.string.thank_you_message)) },
        confirmButton = {
            TextButton(onClick = vm::onThankYouDismissed) {
                Text(stringResource(android.R.string.ok))
            }
        }
    )
}

/** Shows a one-time opt-in prompt for anonymous API statistics. */
@Composable
private fun StatsPromptDialog(state: UiState, vm: SweetSpotViewModel) {
    if (!state.showStatsPrompt) return
    AlertDialog(
        onDismissRequest = vm::onStatsPromptDismissed,
        title = { Text(stringResource(R.string.stats_prompt_title)) },
        text = { Text(stringResource(R.string.stats_prompt_message)) },
        confirmButton = {
            TextButton(onClick = vm::onStatsPromptEnabled) {
                Text(stringResource(R.string.stats_prompt_enable))
            }
        },
        dismissButton = {
            TextButton(onClick = vm::onStatsPromptDismissed) {
                Text(stringResource(R.string.stats_prompt_dismiss))
            }
        }
    )
}
