package today.sweetspot.ui.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

/**
 * A settings menu row: a leading icon, a bold title, and a one-line description, with an optional
 * trailing composable (e.g. a Switch). Tapping the row invokes [onClick].
 *
 * Used for the category rows on the root settings menu (each opens a sub-screen) and for inline
 * toggle rows such as the statistics opt-in.
 *
 * @param iconRes Drawable resource for the leading icon (tinted with the theme's primary colour).
 * @param title Row title.
 * @param description One-line description shown under the title.
 * @param onClick Called when the row is tapped.
 * @param modifier Modifier for the row.
 * @param trailing Optional trailing content (e.g. a [androidx.compose.material3.Switch]); null for a
 *   plain navigation row.
 */
@Composable
internal fun SettingsMenuRow(
    @DrawableRes iconRes: Int,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (trailing != null) {
            Spacer(Modifier.width(16.dp))
            trailing()
        }
    }
}
