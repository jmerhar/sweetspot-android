package si.merhar.sweetspot.data

import si.merhar.sweetspot.model.HourlyPrice
import java.time.Instant
import java.time.ZoneId

/**
 * Abstraction for fetching electricity prices from an upstream API.
 *
 * Decouples [PriceRepository] from a specific API provider, making it testable
 * and allowing alternative data sources in the future.
 */
interface PriceFetcher {

    /**
     * Fetches and parses electricity prices for the given date range.
     *
     * @param from Start of the requested period (inclusive).
     * @param to End of the requested period (exclusive).
     * @param zoneId Timezone to convert timestamps to local time.
     * @return Chronologically sorted list of [HourlyPrice] entries.
     * @throws RuntimeException if the request fails.
     */
    fun fetchPrices(from: Instant, to: Instant, zoneId: ZoneId): List<HourlyPrice>
}
