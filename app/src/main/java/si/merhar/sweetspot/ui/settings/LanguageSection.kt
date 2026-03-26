package si.merhar.sweetspot.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import android.content.Context
import androidx.core.app.LocaleManagerCompat
import androidx.core.os.LocaleListCompat
import si.merhar.sweetspot.R
import java.util.Locale

/**
 * Available language options for the language picker.
 *
 * @property tag BCP 47 language tag (empty string = system default).
 * @property nativeName Language name in the language itself.
 * @property englishName Language name in English (empty for English itself).
 */
private data class LanguageOption(val tag: String, val nativeName: String, val englishName: String)

private val languageOptions = listOf(
    LanguageOption("bg", "\u0411\u044a\u043b\u0433\u0430\u0440\u0441\u043a\u0438", "Bulgarian"),
    LanguageOption("hr", "Hrvatski", "Croatian"),
    LanguageOption("cnr", "Crnogorski", "Montenegrin"),
    LanguageOption("cs", "\u010ce\u0161tina", "Czech"),
    LanguageOption("da", "Dansk", "Danish"),
    LanguageOption("nl", "Nederlands", "Dutch"),
    LanguageOption("en", "English", ""),
    LanguageOption("et", "Eesti", "Estonian"),
    LanguageOption("fi", "Suomi", "Finnish"),
    LanguageOption("fr", "Fran\u00e7ais", "French"),
    LanguageOption("de", "Deutsch", "German"),
    LanguageOption("el", "\u0395\u03bb\u03bb\u03b7\u03bd\u03b9\u03ba\u03ac", "Greek"),
    LanguageOption("hu", "Magyar", "Hungarian"),
    LanguageOption("it", "Italiano", "Italian"),
    LanguageOption("lv", "Latvie\u0161u", "Latvian"),
    LanguageOption("lt", "Lietuvi\u0173", "Lithuanian"),
    LanguageOption("mk", "\u041c\u0430\u043a\u0435\u0434\u043e\u043d\u0441\u043a\u0438", "Macedonian"),
    LanguageOption("nb", "Norsk bokm\u00e5l", "Norwegian"),
    LanguageOption("pl", "Polski", "Polish"),
    LanguageOption("pt", "Portugu\u00eas", "Portuguese"),
    LanguageOption("ro", "Rom\u00e2n\u0103", "Romanian"),
    LanguageOption("sr", "\u0421\u0440\u043f\u0441\u043a\u0438", "Serbian"),
    LanguageOption("sk", "Sloven\u010dina", "Slovak"),
    LanguageOption("sl", "Sloven\u0161\u010dina", "Slovenian"),
    LanguageOption("es", "Espa\u00f1ol", "Spanish"),
    LanguageOption("sv", "Svenska", "Swedish")
)

/**
 * Returns the native name of the system's default language (e.g. "English", "Nederlands").
 *
 * Uses [LocaleManagerCompat.getSystemLocales] to read the actual system locale, which is
 * unaffected by per-app locale changes via [AppCompatDelegate]. Matches against
 * [languageOptions] first, falling back to [Locale.getDisplayLanguage].
 */
private fun systemLanguageName(context: Context): String {
    val systemLocale = LocaleManagerCompat.getSystemLocales(context).get(0)
        ?: Locale.getDefault()
    val systemTag = systemLocale.language
    return languageOptions.find { it.tag == systemTag }?.nativeName
        ?: systemLocale.getDisplayLanguage(systemLocale)
            .replaceFirstChar { it.uppercase() }
}

/**
 * Language settings section showing the current language as a clickable row.
 *
 * Tapping opens the full-screen [LanguagePickerScreen] via the [onClick] callback.
 *
 * @param onClick Called when the row is tapped to open the language picker.
 */
@Composable
internal fun LanguageSection(onClick: () -> Unit) {
    val currentLocales = AppCompatDelegate.getApplicationLocales()
    val currentTag = if (currentLocales.isEmpty) "" else currentLocales.toLanguageTags()
    val systemDefaultLabel = stringResource(R.string.settings_language_system_default)

    val displayName = if (currentTag.isEmpty()) {
        systemDefaultLabel
    } else {
        languageOptions.find { it.tag == currentTag }?.nativeName ?: currentTag
    }

    Text(
        text = stringResource(R.string.settings_language),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * Full-screen language picker with search.
 *
 * Shows "System default" at the top with a divider, then all 26 languages sorted alphabetically
 * by native name. Each row shows the native name as the label and the English name as a subtitle
 * (except for English itself, which has no subtitle). Search matches against both native and
 * English names.
 *
 * @param onLanguageChanged Called with the selected language tag (or empty string for system default).
 *   Also syncs the language to the watch.
 * @param onBack Called when the back button is pressed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LanguagePickerScreen(
    onLanguageChanged: (String) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val currentLocales = AppCompatDelegate.getApplicationLocales()
    val currentTag = if (currentLocales.isEmpty) "" else currentLocales.toLanguageTags()
    val systemDefaultLabel = stringResource(R.string.settings_language_system_default)
    val context = LocalContext.current

    val filteredLanguages = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            languageOptions
        } else {
            val query = searchQuery.lowercase()
            languageOptions.filter {
                it.nativeName.lowercase().contains(query) || it.englishName.lowercase().contains(query)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.picker_language_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(stringResource(R.string.picker_language_search)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // System default option at top
                item {
                    PickerRow(
                        label = systemDefaultLabel,
                        subtitle = systemLanguageName(context),
                        isSelected = currentTag.isEmpty(),
                        onClick = {
                            onLanguageChanged("")
                            AppCompatDelegate.setApplicationLocales(
                                LocaleListCompat.forLanguageTags("")
                            )
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }

                items(filteredLanguages) { option ->
                    PickerRow(
                        label = option.nativeName,
                        subtitle = option.englishName.ifEmpty { null },
                        isSelected = option.tag == currentTag,
                        onClick = {
                            onLanguageChanged(option.tag)
                            AppCompatDelegate.setApplicationLocales(
                                LocaleListCompat.forLanguageTags(option.tag)
                            )
                        }
                    )
                }
            }
        }
    }
}
