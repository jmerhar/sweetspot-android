package today.sweetspot.util

import android.content.res.Resources
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import today.sweetspot.shared.R

/**
 * A deferred, locale-independent representation of a user-facing string.
 *
 * ViewModels emit [UiText] values instead of resolving strings themselves, and the Compose UI
 * resolves them with [resolve] using the Activity's [Resources] (which always carries the correct
 * per-app locale). This fixes the API 26–32 locale bug where a ViewModel built strings from the
 * `Application` context — stuck on the system locale — while the UI used the per-app locale.
 *
 * The type is pure data (no Android dependencies), so ViewModel logic that produces messages and
 * labels is unit-testable with plain JUnit: assert on the [UiText] structure rather than a
 * resolved, locale-dependent string. Only [resolve] touches Android.
 */
sealed interface UiText {

    /**
     * A literal string that is already localised or locale-independent — e.g. an appliance name,
     * a state-of-charge label, a formatted price, or an exception message.
     *
     * @property value The literal text.
     */
    data class Raw(val value: String) : UiText

    /**
     * An Android string resource with optional format arguments.
     *
     * @property id The `@StringRes` resource id.
     * @property args Format arguments. A [UiText] argument is resolved recursively (substituted as
     *   its resolved string, e.g. for a `%s` placeholder); any other value is passed through
     *   unchanged (e.g. an [Int] for `%d`).
     */
    data class Res(@param:StringRes val id: Int, val args: List<Any> = emptyList()) : UiText

    /**
     * An Android plurals resource.
     *
     * @property id The `@PluralsRes` resource id.
     * @property quantity The quantity selecting the CLDR plural form.
     * @property args Format arguments, resolved as in [Res.args].
     */
    data class Plural(@param:PluralsRes val id: Int, val quantity: Int, val args: List<Any> = emptyList()) : UiText

    /**
     * An ordered concatenation of parts, resolved individually and joined with no separator.
     * Used to combine locale-independent text (names, numbers) with localisable fragments —
     * e.g. an appliance name followed by a formatted duration.
     *
     * @property parts The parts to resolve and join in order.
     */
    data class Composite(val parts: List<UiText>) : UiText

    companion object {
        /**
         * Builds the [UiText] for a duration, mirroring [formatDuration]'s resource selection.
         *
         * @param hours Hours component (0–24).
         * @param minutes Minutes component (0–55).
         * @return A [Res]/[Plural] describing the localised duration (e.g. "2h", "30m", "2h 30m").
         */
        fun duration(hours: Int, minutes: Int): UiText = when {
            hours == 0 -> Res(R.string.duration_minutes_only, listOf(minutes))
            minutes == 0 -> Plural(R.plurals.duration_hours_only, hours, listOf(hours))
            else -> Plural(R.plurals.duration_hours_minutes, hours, listOf(hours, minutes))
        }

        /**
         * Builds a "name · duration" label as a [Composite] of the raw name and a localised duration.
         *
         * @param name Appliance name (locale-independent).
         * @param hours Hours component of the duration.
         * @param minutes Minutes component of the duration.
         */
        fun applianceLabel(name: String, hours: Int, minutes: Int): UiText =
            Composite(listOf(Raw("$name · "), duration(hours, minutes)))
    }
}

/**
 * Resolves this [UiText] to a final string using the given [resources].
 *
 * Call from the Compose UI with the Activity context's resources
 * (`LocalContext.current.resources`) so the per-app locale is applied on all API levels.
 *
 * @param resources Android resources carrying the desired locale.
 * @return The resolved, localised string.
 */
fun UiText.resolve(resources: Resources): String = when (this) {
    is UiText.Raw -> value
    is UiText.Res ->
        if (args.isEmpty()) resources.getString(id)
        else resources.getString(id, *resolveArgs(resources, args))
    is UiText.Plural ->
        if (args.isEmpty()) resources.getQuantityString(id, quantity)
        else resources.getQuantityString(id, quantity, *resolveArgs(resources, args))
    is UiText.Composite -> parts.joinToString(separator = "") { it.resolve(resources) }
}

/**
 * Resolves nested [UiText] format arguments to strings, passing other argument types through
 * unchanged so numeric placeholders (`%d`) still receive their numeric values.
 */
private fun resolveArgs(resources: Resources, args: List<Any>): Array<Any> =
    Array(args.size) { i -> args[i].let { if (it is UiText) it.resolve(resources) else it } }
