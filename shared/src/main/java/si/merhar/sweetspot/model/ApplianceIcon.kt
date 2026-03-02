package si.merhar.sweetspot.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Bathtub
import androidx.compose.material.icons.outlined.Blender
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.CoffeeMaker
import androidx.compose.material.icons.outlined.Countertops
import androidx.compose.material.icons.outlined.DevicesOther
import androidx.compose.material.icons.outlined.DryCleaning
import androidx.compose.material.icons.outlined.ElectricCar
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Iron
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.LocalLaundryService
import androidx.compose.material.icons.outlined.Microwave
import androidx.compose.material.icons.outlined.Pool
import androidx.compose.material.icons.outlined.Power
import androidx.compose.material.icons.outlined.SolarPower
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * An icon option for appliances, displayed in the icon picker.
 *
 * @property id Unique string identifier (e.g. "laundry", "bolt") stored in [Appliance.icon].
 * @property icon Material Design icon vector.
 * @property label Human-readable label shown as content description.
 */
data class ApplianceIcon(val id: String, val icon: ImageVector, val label: String)

/** All available appliance icons, grouped by household appliances and generic icons. */
val applianceIcons: List<ApplianceIcon> = listOf(
    // Household appliances
    ApplianceIcon("laundry", Icons.Outlined.LocalLaundryService, "Washing machine"),
    ApplianceIcon("dryer", Icons.Outlined.DryCleaning, "Dryer"),
    ApplianceIcon("iron", Icons.Outlined.Iron, "Iron"),
    ApplianceIcon("microwave", Icons.Outlined.Microwave, "Microwave"),
    ApplianceIcon("kitchen", Icons.Outlined.Kitchen, "Oven"),
    ApplianceIcon("coffee", Icons.Outlined.CoffeeMaker, "Coffee maker"),
    ApplianceIcon("blender", Icons.Outlined.Blender, "Blender"),
    ApplianceIcon("countertops", Icons.Outlined.Countertops, "Kitchen"),
    ApplianceIcon("vacuum", Icons.Outlined.CleaningServices, "Vacuum"),
    ApplianceIcon("bathtub", Icons.Outlined.Bathtub, "Water heater"),
    ApplianceIcon("water", Icons.Outlined.WaterDrop, "Dishwasher"),
    ApplianceIcon("ac", Icons.Outlined.AcUnit, "Air conditioner"),
    ApplianceIcon("thermostat", Icons.Outlined.Thermostat, "Thermostat"),
    ApplianceIcon("pool", Icons.Outlined.Pool, "Pool pump"),
    ApplianceIcon("grass", Icons.Outlined.Grass, "Lawn mower"),
    ApplianceIcon("tv", Icons.Outlined.Tv, "TV"),
    ApplianceIcon("ev", Icons.Outlined.ElectricCar, "Electric car"),
    ApplianceIcon("solar", Icons.Outlined.SolarPower, "Solar / battery"),
    // Generic
    ApplianceIcon("bolt", Icons.Outlined.Bolt, "Electricity"),
    ApplianceIcon("power", Icons.Outlined.Power, "Power"),
    ApplianceIcon("timer", Icons.Outlined.Timer, "Timer"),
    ApplianceIcon("light", Icons.Outlined.LightMode, "Light"),
    ApplianceIcon("devices", Icons.Outlined.DevicesOther, "Device"),
    ApplianceIcon("home", Icons.Outlined.Home, "Home"),
    ApplianceIcon("star", Icons.Outlined.Star, "Favorite"),
    ApplianceIcon("category", Icons.Outlined.Category, "Other"),
)

private val iconMap: Map<String, ImageVector> = applianceIcons.associate { it.id to it.icon }

/**
 * Resolves an icon ID to its [ImageVector].
 *
 * @param id Icon identifier from [applianceIcons].
 * @return The corresponding icon, or the "bolt" icon if the ID is unknown.
 */
fun applianceIconFor(id: String): ImageVector = iconMap[id] ?: Icons.Outlined.Bolt
