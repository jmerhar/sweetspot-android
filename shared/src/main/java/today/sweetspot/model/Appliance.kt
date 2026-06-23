package today.sweetspot.model

import kotlinx.serialization.Serializable

/**
 * A user-configured appliance with a preset duration.
 *
 * Persisted as JSON in SharedPreferences via [today.sweetspot.data.SettingsRepository].
 *
 * @property id Unique identifier (UUID).
 * @property name Display name (e.g. "Washing machine").
 * @property durationHours Hours component of the default run duration.
 * @property durationMinutes Minutes component of the default run duration (0–55, in 5-min steps).
 * @property icon Icon ID referencing the [applianceIcons] registry.
 * @property ev When non-null, this appliance is an electric vehicle: tapping it prompts for a
 *           state-of-charge range and the charging duration is computed from these specs rather
 *           than [durationHours]/[durationMinutes]. `null` for ordinary appliances.
 */
@Serializable
data class Appliance(
    val id: String,
    val name: String,
    val durationHours: Int = 1,
    val durationMinutes: Int = 0,
    val icon: String = "electricity",
    val ev: EvSpec? = null
) {
    /** Whether this appliance represents an electric vehicle (has charging specs). */
    val isEv: Boolean get() = ev != null
}
