package si.merhar.sweetspot.model

import kotlinx.serialization.Serializable

/**
 * A user-configured appliance with a preset duration.
 *
 * Persisted as JSON in SharedPreferences via [si.merhar.sweetspot.data.SettingsRepository].
 *
 * @property id Unique identifier (UUID).
 * @property name Display name (e.g. "Washing machine").
 * @property durationHours Hours component of the default run duration.
 * @property durationMinutes Minutes component of the default run duration (0–55, in 5-min steps).
 * @property icon Icon ID referencing the [applianceIcons] registry.
 */
@Serializable
data class Appliance(
    val id: String,
    val name: String,
    val durationHours: Int = 1,
    val durationMinutes: Int = 0,
    val icon: String = "bolt"
)
