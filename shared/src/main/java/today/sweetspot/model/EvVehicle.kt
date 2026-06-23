package today.sweetspot.model

import kotlinx.serialization.Serializable

/**
 * A single electric vehicle from the bundled EV database.
 *
 * The database is built at build time by `bin/build-ev-db.py`, which merges open datasets
 * into this normalised schema (see `app/src/main/assets/ev-vehicles.json`). Only the fields
 * needed to compute a charging duration are kept.
 *
 * @property brand Manufacturer (e.g. "Volkswagen").
 * @property model Model name (e.g. "ID.3").
 * @property variant Optional variant/trim (e.g. "1st", "Pro S"), or `null` if unspecified.
 * @property year Optional model/release year, or `null` if unspecified.
 * @property batteryKwh Usable (net) battery capacity in kWh.
 * @property acMaxPowerKw Maximum AC charging power the on-board charger accepts, in kW.
 */
@Serializable
data class EvVehicle(
    val brand: String,
    val model: String,
    val variant: String? = null,
    val year: Int? = null,
    val batteryKwh: Double,
    val acMaxPowerKw: Double
) {
    /** Human-readable label combining brand, model, and (when present) variant and year. */
    val displayName: String
        get() = buildString {
            append(brand).append(' ').append(model)
            variant?.let { append(" ").append(it) }
            year?.let { append(" (").append(it).append(')') }
        }
}

/**
 * The charging-relevant specs of an electric vehicle, attached to an [Appliance] to make it a
 * "vehicle" appliance. When [Appliance.ev] is non-null, tapping the appliance prompts for a
 * state-of-charge range instead of running a fixed-duration search.
 *
 * @property batteryKwh Usable (net) battery capacity in kWh.
 * @property acMaxPowerKw Maximum AC charging power the on-board charger accepts, in kW.
 */
@Serializable
data class EvSpec(
    val batteryKwh: Double,
    val acMaxPowerKw: Double
)
