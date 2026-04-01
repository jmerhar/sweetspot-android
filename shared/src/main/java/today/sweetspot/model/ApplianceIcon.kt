package today.sweetspot.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import today.sweetspot.shared.R

/**
 * An icon option for appliances, displayed in the icon picker.
 *
 * @property id Unique string identifier (e.g. "washing_machine", "electricity") stored in [Appliance.icon].
 * @property iconRes Drawable resource ID for the Material Symbol vector.
 * @property labelRes String resource ID for the human-readable label.
 */
data class ApplianceIcon(val id: String, @DrawableRes val iconRes: Int, @StringRes val labelRes: Int)

/** All available appliance icons, grouped by household appliances and generic icons. */
val applianceIcons: List<ApplianceIcon> = listOf(
    // Laundry & cleaning
    ApplianceIcon("washing_machine", R.drawable.ic_washing_machine, R.string.icon_washing_machine),
    ApplianceIcon("dryer", R.drawable.ic_dryer, R.string.icon_dryer),
    ApplianceIcon("iron", R.drawable.ic_iron, R.string.icon_iron),
    ApplianceIcon("dishwasher", R.drawable.ic_dishwasher, R.string.icon_dishwasher),
    ApplianceIcon("vacuum", R.drawable.ic_vacuum, R.string.icon_vacuum),
    // Kitchen
    ApplianceIcon("oven", R.drawable.ic_oven, R.string.icon_oven),
    ApplianceIcon("kettle", R.drawable.ic_kettle, R.string.icon_kettle),
    // Heating, cooling & air
    ApplianceIcon("heat_pump", R.drawable.ic_heat_pump, R.string.icon_heat_pump),
    ApplianceIcon("heater", R.drawable.ic_heater, R.string.icon_heater),
    ApplianceIcon("air_conditioner", R.drawable.ic_air_conditioner, R.string.icon_air_conditioner),
    ApplianceIcon("thermostat", R.drawable.ic_thermostat, R.string.icon_thermostat),
    ApplianceIcon("fan", R.drawable.ic_fan, R.string.icon_fan),
    ApplianceIcon("dehumidifier", R.drawable.ic_dehumidifier, R.string.icon_dehumidifier),
    // Water & wellness
    ApplianceIcon("water_heater", R.drawable.ic_water_heater, R.string.icon_water_heater),
    ApplianceIcon("hot_tub", R.drawable.ic_hot_tub, R.string.icon_hot_tub),
    ApplianceIcon("sauna", R.drawable.ic_sauna, R.string.icon_sauna),
    ApplianceIcon("pool_pump", R.drawable.ic_pool_pump, R.string.icon_pool_pump),
    // Outdoor & transport
    ApplianceIcon("lawn_mower", R.drawable.ic_lawn_mower, R.string.icon_lawn_mower),
    ApplianceIcon("sprinkler", R.drawable.ic_sprinkler, R.string.icon_sprinkler),
    ApplianceIcon("ev_charger", R.drawable.ic_ev_charger, R.string.icon_ev_charger),
    ApplianceIcon("solar_battery", R.drawable.ic_solar_battery, R.string.icon_solar_battery),
    // Electronics
    ApplianceIcon("tv", R.drawable.ic_tv, R.string.icon_tv),
    // Generic
    ApplianceIcon("electricity", R.drawable.ic_electricity, R.string.icon_electricity),
    ApplianceIcon("power", R.drawable.ic_power, R.string.icon_power),
    ApplianceIcon("timer", R.drawable.ic_timer, R.string.icon_timer),
    ApplianceIcon("light", R.drawable.ic_light, R.string.icon_light),
    ApplianceIcon("device", R.drawable.ic_device, R.string.icon_device),
    ApplianceIcon("home", R.drawable.ic_home, R.string.icon_home),
    ApplianceIcon("favorite", R.drawable.ic_favorite, R.string.icon_favorite),
    ApplianceIcon("other", R.drawable.ic_other, R.string.icon_other),
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
