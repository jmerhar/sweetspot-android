package si.merhar.sweetspot.wear

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import si.merhar.sweetspot.wear.ui.ApplianceListScreen
import si.merhar.sweetspot.wear.ui.ResultScreen
import si.merhar.sweetspot.wear.ui.WearTheme

/**
 * Entry point for the Wear OS companion app.
 *
 * Hosts a [SwipeDismissableNavHost] with two routes: the appliance list and the result screen.
 */
class WearActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearTheme {
                val wearViewModel: WearViewModel = viewModel()
                val state by wearViewModel.uiState.collectAsStateWithLifecycle()
                val navController = rememberSwipeDismissableNavController()

                SwipeDismissableNavHost(
                    navController = navController,
                    startDestination = "appliances"
                ) {
                    composable("appliances") {
                        ApplianceListScreen(
                            state = state,
                            onApplianceTapped = { appliance ->
                                wearViewModel.onApplianceTapped(appliance)
                                navController.navigate("result")
                            }
                        )
                    }
                    composable("result") {
                        DisposableEffect(Unit) {
                            onDispose { wearViewModel.onClearResult() }
                        }
                        ResultScreen(state = state)
                    }
                }
            }
        }
    }
}
