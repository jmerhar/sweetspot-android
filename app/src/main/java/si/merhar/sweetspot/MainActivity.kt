package si.merhar.sweetspot

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import si.merhar.sweetspot.data.api.DataSources
import si.merhar.sweetspot.ui.SettingsScreen
import si.merhar.sweetspot.ui.SweetSpotScreen
import si.merhar.sweetspot.ui.theme.SweetSpotTheme

/**
 * Entry point for the phone app.
 *
 * Hosts the [SweetSpotTheme] and switches between [SweetSpotScreen] and [SettingsScreen]
 * based on [SweetSpotViewModel] state.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SweetSpotTheme {
                val vm: SweetSpotViewModel = viewModel()
                val state by vm.uiState.collectAsState()

                if (state.showSettings) {
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
                        onBack = vm::onHideSettings
                    )
                } else {
                    SweetSpotScreen(viewModel = vm)
                }
            }
        }
    }
}
