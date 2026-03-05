package si.merhar.sweetspot.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import si.merhar.sweetspot.R

/**
 * Available language options for the language picker.
 *
 * @property tag BCP 47 language tag (empty string = system default).
 * @property displayName Human-readable language name shown in the picker.
 */
private data class LanguageOption(val tag: String, val displayName: String)

private val languageOptions = listOf(
    LanguageOption("", ""),    // System default — label resolved from string resource
    LanguageOption("en", "English"),
    LanguageOption("nl", "Nederlands"),
    LanguageOption("de", "Deutsch"),
    LanguageOption("fr", "Fran\u00e7ais")
)

/**
 * Language settings section with a selectable list of supported languages.
 *
 * Uses [AppCompatDelegate.setApplicationLocales] for per-app locale switching.
 * The activity automatically recreates when the locale changes.
 *
 * @param onLanguageChanged Callback to sync the new language tag to the watch.
 */
@Composable
internal fun LanguageSection(onLanguageChanged: (String) -> Unit) {
    val systemDefaultLabel = stringResource(R.string.settings_language_system_default)
    val currentLocales = AppCompatDelegate.getApplicationLocales()
    val currentTag = if (currentLocales.isEmpty) "" else currentLocales.toLanguageTags()

    Text(
        text = stringResource(R.string.settings_language),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )

    languageOptions.forEach { option ->
        val displayName = if (option.tag.isEmpty()) systemDefaultLabel else option.displayName
        val isSelected = option.tag == currentTag

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onLanguageChanged(option.tag)
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(option.tag)
                    )
                }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.cd_selected),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
