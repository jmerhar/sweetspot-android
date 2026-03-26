package si.merhar.sweetspot.data.api

/**
 * Represents a price data source (API provider).
 *
 * @property id Unique identifier used for persistence and source ordering.
 * @property displayName Human-readable name shown in the UI.
 */
data class DataSource(val id: String, val displayName: String)

/**
 * Registry of all supported price data sources.
 *
 * Defines the available sources and provides default source ordering per zone.
 * Zone IDs that map to Spot-Hinta.fi are listed in [SPOT_HINTA_ZONES].
 * Zone IDs that map to Energy-Charts are listed in [ENERGY_CHARTS_ZONES].
 */
object DataSources {

    /** ENTSO-E Transparency Platform — primary source for all 43 European bidding zones. */
    val ENTSOE = DataSource("entsoe", "ENTSO-E")

    /** EnergyZero API — NL-only fallback with day-ahead prices. */
    val ENERGY_ZERO = DataSource("energyzero", "EnergyZero")

    /** Spot-Hinta.fi API — fallback for 15 Nordic/Baltic zones. */
    val SPOT_HINTA = DataSource("spothinta", "Spot-Hinta.fi")

    /** Energy-Charts API — fallback for 15 European zones (CC BY 4.0). */
    val ENERGY_CHARTS = DataSource("energycharts", "Energy-Charts")

    /** All known data sources. */
    val all = listOf(ENTSOE, ENERGY_ZERO, SPOT_HINTA, ENERGY_CHARTS)

    /** Zone IDs that map directly to Spot-Hinta.fi region codes. */
    val SPOT_HINTA_ZONES = setOf(
        "FI", "SE1", "SE2", "SE3", "SE4",
        "DK1", "DK2",
        "NO1", "NO2", "NO3", "NO4", "NO5",
        "EE", "LV", "LT"
    )

    /** Zone IDs covered by the Energy-Charts API. */
    val ENERGY_CHARTS_ZONES = EnergyChartsApi.ZONE_TO_BZN.keys

    /**
     * Returns available sources for a zone in default priority order.
     *
     * - NL → ENTSO-E, EnergyZero, Energy-Charts
     * - Spot-Hinta ∩ Energy-Charts zones → ENTSO-E, Spot-Hinta.fi, Energy-Charts
     * - Spot-Hinta only zones → ENTSO-E, Spot-Hinta.fi
     * - Energy-Charts only zones → ENTSO-E, Energy-Charts
     * - All others → ENTSO-E only
     *
     * @param zoneId The [PriceZone.id][si.merhar.sweetspot.model.PriceZone.id] to look up.
     * @return Ordered list of available data sources for this zone.
     */
    fun defaultsForZone(zoneId: String): List<DataSource> = when {
        zoneId == "NL" -> listOf(ENTSOE, ENERGY_ZERO, ENERGY_CHARTS)
        zoneId in SPOT_HINTA_ZONES && zoneId in ENERGY_CHARTS_ZONES ->
            listOf(ENTSOE, SPOT_HINTA, ENERGY_CHARTS)
        zoneId in SPOT_HINTA_ZONES -> listOf(ENTSOE, SPOT_HINTA)
        zoneId in ENERGY_CHARTS_ZONES -> listOf(ENTSOE, ENERGY_CHARTS)
        else -> listOf(ENTSOE)
    }
}
