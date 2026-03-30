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
                        if (state.showStatsPrompt) {
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
                            onBack = vm::onHideSettings
                        )
                    }
                    else -> {
                        if (state.showStatsPrompt) {
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

                        SweetSpotScreen(viewModel = vm)
                    }
                }
            }
        }
    }
}
