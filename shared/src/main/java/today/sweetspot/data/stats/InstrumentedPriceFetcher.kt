package today.sweetspot.data.stats

import today.sweetspot.data.api.FetchResult
import today.sweetspot.data.api.PriceFetcher
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * Decorator that wraps a [PriceFetcher] and records the outcome to a [StatsCollector].
 *
 * Records every attempt in the fallback chain — including intermediate failures that
 * [FallbackPriceFetcher][today.sweetspot.data.api.FallbackPriceFetcher] would normally swallow.
 * This provides full visibility into per-source reliability.
 *
 * On success with an empty price list, records an "EMPTY" error category since the API
 * technically responded but provided no usable data.
 *
 * @param delegate The underlying fetcher to wrap.
 * @param sourceId Data source identifier (e.g. "entsoe", "energyzero").
 * @param priceZoneId Bidding zone identifier (e.g. "NL", "DE_LU").
 * @param device Device type: "phone" or "watch".
 * @param collector Stats collector to record outcomes to.
 * @param clock Clock for timestamps (injectable for testing).
 */
class InstrumentedPriceFetcher(
    private val delegate: PriceFetcher,
    private val sourceId: String,
    private val priceZoneId: String,
    private val device: String,
    private val collector: StatsCollector,
    private val clock: Clock = Clock.systemUTC()
) : PriceFetcher {

    /**
     * Delegates to the wrapped fetcher, recording the outcome before returning or rethrowing.
     *
     * @param from Start of the requested period (inclusive).
     * @param to End of the requested period (exclusive).
     * @param timeZoneId Timezone to convert timestamps to local time.
     * @return The [FetchResult] from the delegate.
     * @throws Exception if the delegate throws, after recording the failure.
     */
    override fun fetchPrices(from: Instant, to: Instant, timeZoneId: ZoneId): FetchResult {
        val timestamp = Instant.now(clock).epochSecond
        val startNanos = System.nanoTime()
        return try {
            val result = delegate.fetchPrices(from, to, timeZoneId)
            val durationMs = (System.nanoTime() - startNanos) / 1_000_000
            if (result.prices.isEmpty()) {
                collector.record(StatsRecord(timestamp, priceZoneId, sourceId, device, false, "EMPTY", durationMs))
            } else {
                collector.record(StatsRecord(timestamp, priceZoneId, sourceId, device, true, "", durationMs))
            }
            result
        } catch (e: Exception) {
            val durationMs = (System.nanoTime() - startNanos) / 1_000_000
            collector.record(StatsRecord(timestamp, priceZoneId, sourceId, device, false, categorise(e), durationMs))
            throw e
        }
    }
}
