package si.merhar.sweetspot.data

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

/**
 * Default factory: NL uses EnergyZero (no auth required), all other zones use ENTSO-E.
 *
 * @param entsoeToken ENTSO-E API security token (from BuildConfig).
 * @return A [PriceFetcherFactory] that routes to the correct API per zone.
 */
fun defaultPriceFetcherFactory(entsoeToken: String): PriceFetcherFactory =
    PriceFetcherFactory { zone ->
        if (zone.id == "NL") EnergyZeroApi
        else EntsoeApi(entsoeToken, zone.eicCode)
    }
