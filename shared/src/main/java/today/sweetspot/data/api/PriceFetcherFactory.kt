package today.sweetspot.data.api

import today.sweetspot.model.PriceZone

/**
 * Factory for creating a [PriceFetcher] appropriate for a given [PriceZone].
 *
 * Decouples ViewModels from knowing which API serves which zone.
 */
fun interface PriceFetcherFactory {

    /**
     * Creates a [PriceFetcher] configured for the given zone.
     *
     * @param zone The price zone to fetch data for.
     * @return A [PriceFetcher] that can retrieve prices for this zone.
     */
    fun create(zone: PriceZone): PriceFetcher
}

/**
 * Default factory: builds the fetcher chain dynamically based on [sourceOrder].
 *
 * When [sourceOrder] is `null`, uses [DataSources.defaultsForZone] for each zone.
 * When provided, filters and reorders the available sources for the zone to match
 * the user's preference. Falls back to defaults if the filtered list is empty
 * (e.g. user's stored sources don't apply to the current zone).
 *
 * Always wraps the result in [FallbackPriceFetcher], which handles single-item
 * lists correctly — keeping one code path for all cases.
 *
 * @param entsoeToken ENTSO-E API security token (from BuildConfig).
 * @param sourceOrder Ordered list of enabled source IDs, or `null` for defaults.
 * @return A [PriceFetcherFactory] that routes to the correct API(s) per zone.
 */
fun defaultPriceFetcherFactory(
    entsoeToken: String,
    sourceOrder: List<String>? = null
): PriceFetcherFactory =
    PriceFetcherFactory { zone ->
        val available = DataSources.defaultsForZone(zone.id)
        val ordered = if (sourceOrder != null) {
            val filtered = sourceOrder.mapNotNull { id -> available.find { it.id == id } }
            filtered.ifEmpty { available }
        } else {
            available
        }

        val fetchers = ordered.map { source ->
            when (source.id) {
                DataSources.ENTSOE.id -> EntsoeApi(entsoeToken, zone.eicCode)
                DataSources.ENERGY_ZERO.id -> EnergyZeroApi()
                DataSources.SPOT_HINTA.id -> SpotHintaApi(zone.id)
                DataSources.ENERGY_CHARTS.id -> EnergyChartsApi(zone.id)
                DataSources.AWATTAR.id -> AwattarApi(zone.id)
                else -> error("Unknown data source: ${source.id}")
            }
        }

        FallbackPriceFetcher(fetchers)
    }
