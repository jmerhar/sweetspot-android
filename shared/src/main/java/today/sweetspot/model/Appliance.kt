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
 * @property icon Icon ID referencing the [applianceIcons] registry, or null. Ordinary
 *           appliances always set one. Electric vehicles leave it null — their icon is a car,
 *           decided at display time by [applianceIconFor] rather than stored.
 * @property ev When non-null, this appliance is an electric vehicle: tapping it prompts for a
 *           state-of-charge range and the charging duration is computed from these specs rather
 *           than [durationHours]/[durationMinutes]. `null` for ordinary appliances.
 * @property powerKw Optional load rating in kilowatts. When set, displayed costs reflect this
 *           load instead of the default per-1-kW figure. `null` leaves the per-1-kW behaviour.
 *           Not used by EV appliances, whose charging power is derived per search.
 */
@Serializable
data class Appliance(
    val id: String,
    val name: String,
    val durationHours: Int = 1,
    val durationMinutes: Int = 0,
    val icon: String? = null,
    val ev: EvSpec? = null,
    val powerKw: Double? = null
) {
    /** Whether this appliance represents an electric vehicle (has charging specs). */
    val isEv: Boolean get() = ev != null
}
