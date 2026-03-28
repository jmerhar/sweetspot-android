package today.sweetspot.data.api

import okhttp3.OkHttpClient
import today.sweetspot.model.PriceSlot
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * Shared [OkHttpClient] used by all API implementations.
 *
 * A single client shares one connection pool and thread pool across all APIs,
 * which is more efficient than creating separate clients per API.
 */
internal val sharedHttpClient: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(10, TimeUnit.SECONDS)
    .build()

/**
 * Result of a price fetch, pairing the price data with the name of the data source.
 *
 * @property prices Chronologically sorted list of [PriceSlot] entries at the API's native resolution.
 * @property source Human-readable name of the data source (e.g. "ENTSO-E", "EnergyZero").
 */
data class FetchResult(
    val prices: List<PriceSlot>,
    val source: String
)

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
     * @return A [FetchResult] containing sorted price slots and the data source name.
     * @throws RuntimeException if the request fails.
     */
    fun fetchPrices(from: Instant, to: Instant, timeZoneId: ZoneId): FetchResult
}
