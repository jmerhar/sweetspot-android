package si.merhar.sweetspot.data

import si.merhar.sweetspot.model.HourlyPrice
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

/**
 * Repository that provides upcoming electricity prices.
 *
 * Uses [PriceCache] to avoid redundant API calls. Always tries the cache first,
 * then checks whether the cached data covers enough upcoming hours. When coverage
 * is below [MIN_COVERAGE_HOURS] (e.g. tomorrow's prices have been published since
 * the last fetch), a re-fetch is attempted — subject to a [COOLDOWN_MS] rate limit
 * to avoid hammering the API.
 *
 * @param cache Cache for parsed price data, keyed by zone.
 * @param zoneId Timezone for date boundary calculations and time display.
 * @param fetcher Provider for fetching prices from the upstream API.
 * @param clock Clock for determining the current time (injectable for testing).
 * @param cacheKey Zone identifier used as the cache key (e.g. `"NL"`, `"DE_LU"`).
 */
class PriceRepository(
    private val cache: PriceCache,
    private val zoneId: ZoneId,
    private val fetcher: PriceFetcher = EnergyZeroApi,
    private val clock: Clock = Clock.system(zoneId),
    private val cacheKey: String = "default"
) {

    private companion object {
        /** Re-fetch if filtered prices cover fewer than this many hours. */
        const val MIN_COVERAGE_HOURS = 12

        /** Minimum interval between API requests (5 minutes). */
        const val COOLDOWN_MS = 5 * 60 * 1000L
    }

    /**
     * Returns all available upcoming hourly prices.
     *
     * Reads from cache first. If coverage is below [MIN_COVERAGE_HOURS] and the
     * [COOLDOWN_MS] rate limit has elapsed, re-fetches from the API in case newer
     * data is available.
     *
     * @return Chronologically sorted list of [HourlyPrice] entries.
     * @throws RuntimeException if the initial API call fails and no cache exists.
     */
    fun getPrices(): List<HourlyPrice> {
        val now = ZonedDateTime.now(clock)

        val cached = cache.readCached(cacheKey)
        var allPrices = if (cached != null) {
            cached.map { entry ->
                HourlyPrice(
                    time = Instant.ofEpochSecond(entry.epochSecond).atZone(zoneId),
                    price = entry.price
                )
            }
        } else {
            fetchAndCache()
        }

        var filtered = filterFuture(allPrices, now)

        // If coverage is low, try re-fetching in case new data (e.g. tomorrow's prices)
        // has been published since the last fetch. Respects a cooldown to avoid hammering.
        // On failure, fall back to the stale cached data rather than crashing.
        if (filtered.size < MIN_COVERAGE_HOURS && cache.isCooldownElapsed(COOLDOWN_MS)) {
            try {
                allPrices = fetchAndCache()
                filtered = filterFuture(allPrices, now)
            } catch (e: Exception) {
                // If we have some stale data, show it rather than crashing.
                // If we have nothing, let the error propagate.
                if (filtered.isEmpty()) throw e
            }
        }

        return filtered
    }

    /**
     * Filters out past prices, keeping only the current hour and beyond.
     *
     * @param prices All available prices (unfiltered).
     * @param now Current time for the filter cutoff.
     * @return Future prices sorted chronologically.
     */
    private fun filterFuture(prices: List<HourlyPrice>, now: ZonedDateTime): List<HourlyPrice> {
        val currentHour = now.truncatedTo(ChronoUnit.HOURS)
        return prices.filter { it.time >= currentHour }
    }

    /**
     * Computes the date range for an API request: today's start to day-after-tomorrow's start.
     *
     * @return Pair of (from, to) instants in UTC.
     */
    private fun dateRange(): Pair<java.time.Instant, java.time.Instant> {
        val today = LocalDate.now(clock).atStartOfDay(zoneId)
        val from = today.toInstant()
        val to = today.plusDays(2).toInstant()
        return from to to
    }

    /**
     * Fetches fresh prices from the API and writes them to cache.
     *
     * @return Parsed list of all hourly prices (unfiltered).
     */
    private fun fetchAndCache(): List<HourlyPrice> {
        val (from, to) = dateRange()
        val prices = fetcher.fetchPrices(from, to, zoneId)
        cache.write(cacheKey, prices.map { CachedPrice(it.time.toInstant().epochSecond, it.price) })
        return prices
    }
}
