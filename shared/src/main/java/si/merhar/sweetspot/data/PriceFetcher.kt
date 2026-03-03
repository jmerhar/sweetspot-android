package si.merhar.sweetspot.data

import si.merhar.sweetspot.model.PriceSlot
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
     * @param timeZoneId Timezone to convert timestamps to local time.
     * @return Chronologically sorted list of [PriceSlot] entries at the API's native resolution.
     * @throws RuntimeException if the request fails.
     */
    fun fetchPrices(from: Instant, to: Instant, timeZoneId: ZoneId): List<PriceSlot>
}
