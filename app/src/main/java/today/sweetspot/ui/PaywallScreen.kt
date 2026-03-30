package today.sweetspot.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import today.sweetspot.R

/**
 * Full-screen paywall shown when the free trial has expired and the app is not unlocked.
 *
 * Displays the app icon, a heading, a value proposition, a purchase button, and a restore option.
 * This screen is inescapable — there is no back button or dismiss action.
 *
 * @param productPrice Localized price string (e.g. "€2.99"), or `null` if not yet loaded.
 * @param onPurchaseClicked Called when the user taps the purchase button.
 * @param onRestorePurchases Called when the user taps "Restore purchase".
 */
@Composable
fun PaywallScreen(
    productPrice: String?,
    onPurchaseClicked: () -> Unit,
    onRestorePurchases: () -> Unit
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.paywall_title),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.paywall_description),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = onPurchaseClicked) {
                Text(
                    text = if (productPrice != null) {
                        stringResource(R.string.paywall_unlock_price, productPrice)
                    } else {
                        stringResource(R.string.paywall_unlock)
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onRestorePurchases) {
                Text(text = stringResource(R.string.paywall_restore))
            }
        }
    }
}
