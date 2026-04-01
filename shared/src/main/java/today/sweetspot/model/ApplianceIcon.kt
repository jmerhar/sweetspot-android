package today.sweetspot.model

import androidx.annotation.DrawableRes
import today.sweetspot.shared.R

/**
 * An icon option for appliances, displayed in the icon picker.
 *
 * @property id Unique string identifier (e.g. "washing_machine", "electricity") stored in [Appliance.icon].
 * @property iconRes Drawable resource ID for the Material Symbol vector.
 * @property label Human-readable label shown as content description.
 */
data class ApplianceIcon(val id: String, @DrawableRes val iconRes: Int, val label: String)

/** All available appliance icons, grouped by household appliances and generic icons. */
val applianceIcons: List<ApplianceIcon> = listOf(
    // Laundry & cleaning
    ApplianceIcon("washing_machine", R.drawable.ic_washing_machine, "Washing machine"),
    ApplianceIcon("dryer", R.drawable.ic_dryer, "Dryer"),
    ApplianceIcon("iron", R.drawable.ic_iron, "Iron"),
    ApplianceIcon("dishwasher", R.drawable.ic_dishwasher, "Dishwasher"),
    ApplianceIcon("vacuum", R.drawable.ic_vacuum, "Vacuum"),
    // Kitchen
    ApplianceIcon("oven", R.drawable.ic_oven, "Oven"),
    ApplianceIcon("kettle", R.drawable.ic_kettle, "Kettle"),
    // Heating, cooling & air
    ApplianceIcon("heat_pump", R.drawable.ic_heat_pump, "Heat pump"),
    ApplianceIcon("heater", R.drawable.ic_heater, "Heater"),
    ApplianceIcon("air_conditioner", R.drawable.ic_air_conditioner, "Air conditioner"),
    ApplianceIcon("thermostat", R.drawable.ic_thermostat, "Thermostat"),
    ApplianceIcon("fan", R.drawable.ic_fan, "Fan"),
    ApplianceIcon("dehumidifier", R.drawable.ic_dehumidifier, "Dehumidifier"),
    // Water & wellness
    ApplianceIcon("water_heater", R.drawable.ic_water_heater, "Water heater"),
    ApplianceIcon("hot_tub", R.drawable.ic_hot_tub, "Hot tub"),
    ApplianceIcon("sauna", R.drawable.ic_sauna, "Sauna"),
    ApplianceIcon("pool_pump", R.drawable.ic_pool_pump, "Pool pump"),
    // Outdoor & transport
    ApplianceIcon("lawn_mower", R.drawable.ic_lawn_mower, "Lawn mower"),
    ApplianceIcon("sprinkler", R.drawable.ic_sprinkler, "Sprinkler"),
    ApplianceIcon("ev_charger", R.drawable.ic_ev_charger, "EV charger"),
    ApplianceIcon("solar_battery", R.drawable.ic_solar_battery, "Solar / battery"),
    // Electronics
    ApplianceIcon("tv", R.drawable.ic_tv, "TV"),
    // Generic
    ApplianceIcon("electricity", R.drawable.ic_electricity, "Electricity"),
    ApplianceIcon("power", R.drawable.ic_power, "Power"),
    ApplianceIcon("timer", R.drawable.ic_timer, "Timer"),
    ApplianceIcon("light", R.drawable.ic_light, "Light"),
    ApplianceIcon("device", R.drawable.ic_device, "Device"),
    ApplianceIcon("home", R.drawable.ic_home, "Home"),
    ApplianceIcon("favorite", R.drawable.ic_favorite, "Favorite"),
    ApplianceIcon("other", R.drawable.ic_other, "Other"),
)

private val iconMap: Map<String, Int> = applianceIcons.associate { it.id to it.iconRes }

/**
 * Resolves an icon ID to its drawable resource.
 *
 * @param id Icon identifier from [applianceIcons].
 * @return The corresponding drawable resource ID, or the "electricity" icon if the ID is unknown.
 */
@DrawableRes
fun applianceIconFor(id: String): Int = iconMap[id] ?: R.drawable.ic_electricity
