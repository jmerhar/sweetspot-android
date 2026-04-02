package today.sweetspot

import androidx.appcompat.app.AppCompatDelegate

/**
 * User-selectable theme modes for the app.
 *
 * Each entry carries the [key] persisted in SharedPreferences and the corresponding
 * [AppCompatDelegate] [nightMode] constant.
 *
 * @property key The string stored in SharedPreferences.
 * @property nightMode The [AppCompatDelegate] night mode constant.
 */
enum class ThemeMode(val key: String, val nightMode: Int) {

    /** Follow the system dark mode setting. */
    SYSTEM("system", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),

    /** Force light theme. */
    LIGHT("light", AppCompatDelegate.MODE_NIGHT_NO),

    /** Force dark theme. */
    DARK("dark", AppCompatDelegate.MODE_NIGHT_YES);

    companion object {
        /** Returns the [ThemeMode] matching the given [key], defaulting to [SYSTEM]. */
        fun fromKey(key: String): ThemeMode = entries.find { it.key == key } ?: SYSTEM
    }
}
