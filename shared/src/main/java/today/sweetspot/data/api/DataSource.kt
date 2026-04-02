package today.sweetspot.data.api

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
 * Each source declares which zones it covers. [defaultsForZone] filters and
 * returns sources in registry order (which defines the default priority).
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

    /** aWATTar API — fallback for AT and DE-LU zones. */
    val AWATTAR = DataSource("awattar", "aWATTar")

    /** All known data sources. */
    val all = listOf(ENTSOE, ENERGY_ZERO, SPOT_HINTA, ENERGY_CHARTS, AWATTAR)

    /**
     * A data source paired with the set of zones it covers.
     *
     * @property source The data source.
     * @property zones Zone IDs this source covers, or `null` for all zones.
     */
    private data class SourceEntry(val source: DataSource, val zones: Set<String>?)

    /**
     * Declarative registry of data sources and their zone coverage.
     *
     * List order defines the default fallback priority: ENTSO-E first (covers all
     * zones), then higher-resolution fallbacks (Spot-Hinta for Nordic/Baltic,
     * Energy-Charts for 15 European zones), then hourly sources (EnergyZero for NL,
     * aWATTar for AT/DE-LU). Adding a new source is a single line — no intersection
     * logic required.
     */
    private val registry = listOf(
        SourceEntry(ENTSOE, null),
        SourceEntry(SPOT_HINTA, SpotHintaApi.ZONES),
        SourceEntry(ENERGY_CHARTS, EnergyChartsApi.ZONE_TO_BZN.keys),
        SourceEntry(ENERGY_ZERO, setOf("NL")),
        SourceEntry(AWATTAR, AwattarApi.ZONE_TO_BASE_URL.keys),
    )

    /**
     * Returns available sources for a zone in default priority order.
     *
     * Filters the [registry] to sources that cover the given zone, preserving
     * insertion order as the fallback priority.
     *
     * @param zoneId The [PriceZone.id][today.sweetspot.model.PriceZone.id] to look up.
     * @return Ordered list of available data sources for this zone.
     */
    fun defaultsForZone(zoneId: String): List<DataSource> =
        registry
            .filter { it.zones == null || zoneId in it.zones }
            .map { it.source }
}
