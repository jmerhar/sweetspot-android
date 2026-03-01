package si.merhar.sweetspot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import si.merhar.sweetspot.ui.SweetSpotScreen
import si.merhar.sweetspot.ui.theme.SweetSpotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SweetSpotTheme {
                SweetSpotScreen(viewModel = viewModel())
            }
        }
    }
}
