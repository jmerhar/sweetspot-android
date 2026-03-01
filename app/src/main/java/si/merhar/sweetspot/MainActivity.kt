package si.merhar.sweetspot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import si.merhar.sweetspot.ui.SettingsScreen
import si.merhar.sweetspot.ui.SweetSpotScreen
import si.merhar.sweetspot.ui.theme.SweetSpotTheme

class MainActivity : ComponentActivity() {
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
                        currentZoneId = state.zoneId,
                        isUsingDefaultZone = state.isUsingDefaultZone,
                        onZoneSelected = vm::onZoneSelected,
                        onBack = vm::onHideSettings
                    )
                } else {
                    SweetSpotScreen(viewModel = vm)
                }
            }
        }
    }
}
