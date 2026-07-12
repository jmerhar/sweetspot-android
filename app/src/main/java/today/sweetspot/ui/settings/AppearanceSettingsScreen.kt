package today.sweetspot.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import today.sweetspot.R
import today.sweetspot.ThemeMode

/**
 * Sub-screen for appearance: app language (opens [LanguagePickerScreen]) and theme (light/dark/system,
 * via [ThemeSection]'s own dialog).
 *
 * @param themeMode Current theme mode.
 * @param onThemeModeChanged Called when the theme changes.
 * @param onLanguageChanged Called with the selected BCP-47 language tag.
 * @param onBack Called to return to the settings menu.
 */
@Composable
internal fun AppearanceSettingsScreen(
    themeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onLanguageChanged: (String) -> Unit,
    onBack: () -> Unit
) {
    var showLanguagePicker by rememberSaveable { mutableStateOf(false) }

    if (showLanguagePicker) {
        BackHandler { showLanguagePicker = false }
        LanguagePickerScreen(
            onLanguageChanged = { tag ->
                onLanguageChanged(tag)
                showLanguagePicker = false
            },
            onBack = { showLanguagePicker = false }
        )
        return
    }

    SettingsSubScreen(title = stringResource(R.string.settings_appearance_title), onBack = onBack) {
        LanguageSection(onClick = { showLanguagePicker = true })

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

        ThemeSection(
            themeMode = themeMode,
            onThemeModeChanged = onThemeModeChanged
        )
    }
}
