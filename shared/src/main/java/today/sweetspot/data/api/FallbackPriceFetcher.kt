package today.sweetspot.data.api

import java.time.Instant
import java.time.ZoneId

/**
 * A [PriceFetcher] that tries multiple fetchers in order and returns the first successful result.
 *
 * Used to provide resilience when the primary data source is unavailable. For example,
 * NL uses ENTSO-E as primary and EnergyZero as fallback.
 *
 * @param fetchers Ordered list of fetchers to try. Must not be empty.
 * @throws IllegalArgumentException if [fetchers] is empty.
 */
class FallbackPriceFetcher(
    internal val fetchers: List<PriceFetcher>
) : PriceFetcher {

    init {
        require(fetchers.isNotEmpty()) { "At least one fetcher required" }
    }

    /**
     * Tries each fetcher in order and returns the first result that actually has prices.
     *
     * A fetcher that responds successfully but with no prices (e.g. HTTP 200 and a well-formed but
     * empty day-ahead document) is not useful, so the chain keeps going — a later source may still
     * have data. If no fetcher yields prices, an empty result is returned when at least one
     * responded (the caller treats an empty list as "not enough data"); the last exception is
     * thrown only when every fetcher failed outright.
     *
     * @param from Start of the requested period (inclusive).
     * @param to End of the requested period (exclusive).
     * @param timeZoneId Timezone to convert timestamps to local time.
     * @return The first non-empty [FetchResult], or an empty one if every fetcher was empty.
     * @throws Exception the exception thrown by the last fetcher if all fetchers threw.
     */
    override fun fetchPrices(from: Instant, to: Instant, timeZoneId: ZoneId): FetchResult {
        var lastException: Exception? = null
        var lastEmpty: FetchResult? = null
        for (fetcher in fetchers) {
            try {
                val result = fetcher.fetchPrices(from, to, timeZoneId)
                if (result.prices.isNotEmpty()) return result
                lastEmpty = result
            } catch (e: Exception) {
                lastException = e
            }
        }
        return lastEmpty ?: throw lastException!!
    }
}
