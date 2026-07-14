package today.sweetspot.ui.share

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import today.sweetspot.R
import today.sweetspot.ui.settings.SettingsSubScreen

/**
 * Settings sub-screen for sharing the user's setup with another household device.
 *
 * Shows a QR code of the share deep link (scan with the other phone's system camera) plus a
 * "Share link" button that opens the Android sharesheet with the same link. The payload rides in
 * the link's fragment, so nothing is uploaded — sharing is entirely offline.
 *
 * @param shareLink Produces the encoded share deep link; called once and remembered.
 * @param onBack Returns to the settings menu.
 */
@Composable
fun ShareSetupScreen(
    shareLink: () -> String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val link = remember { shareLink() }
    val qr = remember(link) { runCatching { qrBitmap(link, 720) }.getOrNull() }
    val chooserTitle = stringResource(R.string.share_setup_chooser_title)

    SettingsSubScreen(title = stringResource(R.string.settings_share_title), onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = stringResource(R.string.share_setup_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (qr != null) {
                // White surface + padding give the code a light quiet zone so it scans in dark theme.
                Surface(color = Color.White, shape = RoundedCornerShape(8.dp)) {
                    Image(
                        bitmap = qr,
                        contentDescription = stringResource(R.string.cd_qr_code),
                        modifier = Modifier
                            .padding(16.dp)
                            .size(240.dp)
                    )
                }
            } else {
                // A payload too large to encode is rare (many appliances); fall back to the link only.
                Text(
                    text = stringResource(R.string.share_setup_qr_unavailable),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = {
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, link)
                    }
                    context.startActivity(Intent.createChooser(send, chooserTitle))
                }
            ) {
                Text(stringResource(R.string.share_setup_button))
            }
        }
    }
}
