package si.merhar.sweetspot.data

import si.merhar.sweetspot.model.HourlyPrice
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Repository that provides the next 24 hours of electricity prices.
 *
 * Uses [PriceCache] to avoid redundant API calls. If the cache is stale or missing,
 * fetches fresh data from [EnergyZeroApi] and updates the cache.
 *
 * @param cache File cache for raw API JSON.
 * @param zoneId Timezone for date boundary calculations and time display.
 */
class PriceRepository(private val cache: PriceCache, private val zoneId: ZoneId) {

    /**
     * Returns hourly prices for the next 24 hours from now.
     *
     * Reads from cache if fresh; otherwise fetches from the API and caches the result.
     * Filters the full dataset to only include hours within ~24h from now.
     *
     * @return Chronologically sorted list of [HourlyPrice] entries.
     * @throws RuntimeException if the API call fails.
     */
    fun getPrices(): List<HourlyPrice> {
        val today = LocalDate.now(zoneId)
        val allPrices: List<HourlyPrice>

        if (cache.isFresh(today)) {
            val cachedJson = cache.readCachedJson()
            if (cachedJson != null) {
                allPrices = EnergyZeroApi.parseJson(cachedJson, zoneId)
            } else {
                allPrices = fetchAndCache(today)
            }
        } else {
            allPrices = fetchAndCache(today)
        }

        // Filter to next 24h from now
        val now = ZonedDateTime.now(zoneId)
        val end = now.plusHours(24)
        return allPrices.filter { it.time >= now.minusMinutes(30) && it.time < end }
    }

    /**
     * Fetches fresh prices from the API and writes them to cache.
     *
     * @param today Today's date for cache freshness tracking.
     * @return Parsed list of all hourly prices (unfiltered).
     */
    private fun fetchAndCache(today: LocalDate): List<HourlyPrice> {
        val rawJson = EnergyZeroApi.fetchRawJson(zoneId)
        cache.write(rawJson, today)
        return EnergyZeroApi.parseJson(rawJson, zoneId)
    }
}
