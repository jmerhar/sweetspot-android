package si.merhar.sweetspot.data

import si.merhar.sweetspot.model.HourlyPrice
import java.time.ZoneId

/**
 * Abstraction for fetching and parsing electricity prices.
 *
 * Decouples [PriceRepository] from a specific API provider, making it testable
 * and allowing alternative data sources in the future.
 */
interface PriceFetcher {

    /**
     * Fetches raw JSON price data from the upstream API.
     *
     * @param zoneId Timezone used to determine date boundaries for the request.
     * @return Raw JSON response body.
     * @throws RuntimeException if the request fails.
     */
    fun fetchRawJson(zoneId: ZoneId): String

    /**
     * Parses raw JSON into a sorted list of [HourlyPrice] entries.
     *
     * @param rawJson Raw JSON string from [fetchRawJson] or cache.
     * @param zoneId Timezone to convert timestamps to local time.
     * @return Chronologically sorted list of hourly prices.
     */
    fun parseJson(rawJson: String, zoneId: ZoneId): List<HourlyPrice>
}
