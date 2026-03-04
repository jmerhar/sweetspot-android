package si.merhar.sweetspot.data.api

import si.merhar.sweetspot.model.PriceZone

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

/** Zone IDs that map directly to Spot-Hinta.fi region codes. */
private val SPOT_HINTA_ZONES = setOf(
    "FI", "SE1", "SE2", "SE3", "SE4",
    "DK1", "DK2",
    "NO1", "NO2", "NO3", "NO4", "NO5",
    "EE", "LV", "LT"
)

/**
 * Default factory: all zones use ENTSO-E as the primary source. NL additionally
 * has EnergyZero as a fallback, and the 15 Nordic/Baltic zones have Spot-Hinta.fi
 * as a fallback via [FallbackPriceFetcher].
 *
 * @param entsoeToken ENTSO-E API security token (from BuildConfig).
 * @return A [PriceFetcherFactory] that routes to the correct API per zone.
 */
fun defaultPriceFetcherFactory(entsoeToken: String): PriceFetcherFactory =
    PriceFetcherFactory { zone ->
        val entsoe = EntsoeApi(entsoeToken, zone.eicCode)
        when {
            zone.id == "NL" -> FallbackPriceFetcher(listOf(entsoe, EnergyZeroApi))
            zone.id in SPOT_HINTA_ZONES -> FallbackPriceFetcher(listOf(entsoe, SpotHintaApi(zone.id)))
            else -> entsoe
        }
    }
