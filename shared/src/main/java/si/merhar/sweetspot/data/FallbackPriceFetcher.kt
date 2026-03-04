package si.merhar.sweetspot.data

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
    private val fetchers: List<PriceFetcher>
) : PriceFetcher {

    init {
        require(fetchers.isNotEmpty()) { "At least one fetcher required" }
    }

    /**
     * Tries each fetcher in order and returns the first successful [FetchResult].
     *
     * If all fetchers fail, throws the exception from the last fetcher.
     *
     * @param from Start of the requested period (inclusive).
     * @param to End of the requested period (exclusive).
     * @param timeZoneId Timezone to convert timestamps to local time.
     * @return The [FetchResult] from the first fetcher that succeeds.
     * @throws Exception the exception thrown by the last fetcher if all fail.
     */
    override fun fetchPrices(from: Instant, to: Instant, timeZoneId: ZoneId): FetchResult {
        var lastException: Exception? = null
        for (fetcher in fetchers) {
            try {
                return fetcher.fetchPrices(from, to, timeZoneId)
            } catch (e: Exception) {
                lastException = e
            }
        }
        throw lastException!!
    }
}
