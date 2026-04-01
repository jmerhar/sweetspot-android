package today.sweetspot.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Bathtub
import androidx.compose.material.icons.outlined.Blender
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Countertops
import androidx.compose.material.icons.outlined.DevicesOther
import androidx.compose.material.icons.outlined.DryCleaning
import androidx.compose.material.icons.outlined.ElectricCar
import androidx.compose.material.icons.outlined.Fireplace
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.HeatPump
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Iron
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.LocalLaundryService
import androidx.compose.material.icons.outlined.Microwave
import androidx.compose.material.icons.outlined.Pool
import androidx.compose.material.icons.outlined.Power
import androidx.compose.material.icons.outlined.SolarPower
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material.icons.outlined.WaterDamage
import androidx.compose.material.icons.outlined.Flatware
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * An icon option for appliances, displayed in the icon picker.
 *
 * @property id Unique string identifier (e.g. "washing_machine", "electricity") stored in [Appliance.icon].
 * @property icon Material Design icon vector.
 * @property label Human-readable label shown as content description.
 */
data class ApplianceIcon(val id: String, val icon: ImageVector, val label: String)

/** All available appliance icons, grouped by household appliances and generic icons. */
val applianceIcons: List<ApplianceIcon> = listOf(
    // Household appliances
    ApplianceIcon("washing_machine", Icons.Outlined.LocalLaundryService, "Washing machine"),
    ApplianceIcon("dryer", Icons.Outlined.DryCleaning, "Dryer"),
    ApplianceIcon("iron", Icons.Outlined.Iron, "Iron"),
    ApplianceIcon("microwave", Icons.Outlined.Microwave, "Microwave"),
    ApplianceIcon("oven", Icons.Outlined.Kitchen, "Oven"),
    ApplianceIcon("blender", Icons.Outlined.Blender, "Blender"),
    ApplianceIcon("kitchen", Icons.Outlined.Countertops, "Kitchen"),
    ApplianceIcon("vacuum", Icons.Outlined.CleaningServices, "Vacuum"),
    ApplianceIcon("water_heater", Icons.Outlined.Bathtub, "Water heater"),
    ApplianceIcon("dishwasher", Icons.Outlined.Flatware, "Dishwasher"),
    ApplianceIcon("air_conditioner", Icons.Outlined.AcUnit, "Air conditioner"),
    ApplianceIcon("thermostat", Icons.Outlined.Thermostat, "Thermostat"),
    ApplianceIcon("pool_pump", Icons.Outlined.Pool, "Pool pump"),
    ApplianceIcon("lawn_mower", Icons.Outlined.Grass, "Lawn mower"),
    ApplianceIcon("tv", Icons.Outlined.Tv, "TV"),
    ApplianceIcon("electric_car", Icons.Outlined.ElectricCar, "Electric car"),
    ApplianceIcon("solar_battery", Icons.Outlined.SolarPower, "Solar / battery"),
    ApplianceIcon("heat_pump", Icons.Outlined.HeatPump, "Heat pump"),
    ApplianceIcon("sauna", Icons.Outlined.Spa, "Sauna"),
    ApplianceIcon("heater", Icons.Outlined.Fireplace, "Heater"),
    ApplianceIcon("dehumidifier", Icons.Outlined.WaterDamage, "Dehumidifier"),
    ApplianceIcon("fan", Icons.Outlined.Air, "Fan"),
    // Generic
    ApplianceIcon("electricity", Icons.Outlined.Bolt, "Electricity"),
    ApplianceIcon("power", Icons.Outlined.Power, "Power"),
    ApplianceIcon("timer", Icons.Outlined.Timer, "Timer"),
    ApplianceIcon("light", Icons.Outlined.LightMode, "Light"),
    ApplianceIcon("device", Icons.Outlined.DevicesOther, "Device"),
    ApplianceIcon("home", Icons.Outlined.Home, "Home"),
    ApplianceIcon("favorite", Icons.Outlined.Star, "Favorite"),
    ApplianceIcon("other", Icons.Outlined.Category, "Other"),
)

private val iconMap: Map<String, ImageVector> = applianceIcons.associate { it.id to it.icon }

/**
 * Resolves an icon ID to its [ImageVector].
 *
 * @param id Icon identifier from [applianceIcons].
 * @return The corresponding icon, or the "electricity" icon if the ID is unknown.
 */
fun applianceIconFor(id: String): ImageVector = iconMap[id] ?: Icons.Outlined.Bolt
