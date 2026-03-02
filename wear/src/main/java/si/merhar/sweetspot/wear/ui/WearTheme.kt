package si.merhar.sweetspot.wear.ui

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme

/**
 * Wear OS Material theme wrapper for SweetSpot.
 *
 * Uses the default Wear Material theme colors and typography.
 */
@Composable
fun WearTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
