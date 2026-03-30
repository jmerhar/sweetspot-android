package today.sweetspot.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import today.sweetspot.R

/**
 * Settings section for unlocking the app before the trial expires.
 *
 * Shows the unlock button with the product price (if loaded) and the number of trial days
 * remaining. Hidden when the app is already unlocked.
 *
 * @param trialDaysRemaining Number of trial days remaining (0–14).
 * @param productPrice Localized price string (e.g. "€2.49"), or `null` if not yet loaded.
 * @param onPurchaseClicked Called when the user taps to purchase.
 */
@Composable
internal fun UnlockSection(
    trialDaysRemaining: Int,
    productPrice: String?,
    onPurchaseClicked: () -> Unit
) {
    Text(
        text = stringResource(R.string.settings_unlock),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPurchaseClicked)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (productPrice != null) {
                    stringResource(R.string.paywall_unlock_price, productPrice)
                } else {
                    stringResource(R.string.paywall_unlock)
                },
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = if (trialDaysRemaining > 0) {
                    pluralStringResource(R.plurals.trial_days_remaining, trialDaysRemaining, trialDaysRemaining)
                } else {
                    stringResource(R.string.settings_unlock_description)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
