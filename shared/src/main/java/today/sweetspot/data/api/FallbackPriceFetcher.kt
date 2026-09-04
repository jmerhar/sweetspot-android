package today.sweetspot.data.api

import java.time.Instant
import java.time.ZoneId

/**
 * A [PriceFetcher] that tries multiple fetchers in order and returns the first successful result.
 *
 * Used to provide resilience when the primary data source is unavailable. For example,
 * NL uses ENTSO-E as primary and EnergyZero as fallback.
 *
 * Because the fetchers run in sequence, their timeouts add up: a zone with three sources
 * can otherwise keep the caller waiting for three full per-request timeouts before
 * reporting failure. [budgetMillis] caps that by stopping the chain once the budget is
 * spent, trading the chance of a later source succeeding against how long a user waits
 * to be told there are no prices.
 *
 * @param fetchers Ordered list of fetchers to try. Must not be empty.
 * @param budgetMillis Total time to spend on the chain before giving up on the remaining
 *   sources. Must be positive.
 * @param nanoTime Monotonic time source in nanoseconds (injectable for testing).
 * @throws IllegalArgumentException if [fetchers] is empty or [budgetMillis] is not positive.
 */
class FallbackPriceFetcher(
    internal val fetchers: List<PriceFetcher>,
    private val budgetMillis: Long = DEFAULT_BUDGET_MILLIS,
    private val nanoTime: () -> Long = System::nanoTime
) : PriceFetcher {

    init {
        require(fetchers.isNotEmpty()) { "At least one fetcher required" }
        require(budgetMillis > 0) { "Budget must be positive, got $budgetMillis" }
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
     * The first fetcher always runs, so the outcome is always drawn from a real attempt; the
     * budget only decides whether to start each *subsequent* source. It is checked between
     * fetchers rather than enforced on them, since a fetcher in progress cannot be interrupted
     * here — one source may therefore overrun the budget, bounding the total at roughly the
     * budget plus a single source's timeout.
     *
     * @param from Start of the requested period (inclusive).
     * @param to End of the requested period (exclusive).
     * @param timeZoneId Timezone to convert timestamps to local time.
     * @return The first non-empty [FetchResult], or an empty one if every fetcher tried was empty.
     * @throws Exception the exception thrown by the last fetcher if every fetcher tried threw.
     */
    override fun fetchPrices(from: Instant, to: Instant, timeZoneId: ZoneId): FetchResult {
        val startNanos = nanoTime()
        var lastException: Exception? = null
        var lastEmpty: FetchResult? = null
        for ((index, fetcher) in fetchers.withIndex()) {
            if (index > 0 && elapsedMillis(startNanos) >= budgetMillis) break
            try {
                val result = fetcher.fetchPrices(from, to, timeZoneId)
                if (result.prices.isNotEmpty()) return result
                lastEmpty = result
            } catch (e: Exception) {
                lastException = e
            }
        }
        // Safe: the first fetcher always runs, so it has either recorded an empty result or thrown.
        return lastEmpty ?: throw lastException!!
    }

    /** Milliseconds elapsed since [startNanos] on the injected monotonic time source. */
    private fun elapsedMillis(startNanos: Long): Long = (nanoTime() - startNanos) / 1_000_000

    companion object {
        /**
         * Default time budget for a whole fallback chain.
         *
         * Sized against the two ways a source fails. A refusal comes back fast — observed
         * ENTSO-E 503s land in well under four seconds — so in that case the budget never
         * binds and every source still gets its turn, which is the behaviour the chain
         * exists for. A hanging source instead burns its full connect-plus-read timeout,
         * and it is only there that this cuts in, keeping the wait to roughly this budget
         * plus one source's timeout instead of one timeout per source in the zone.
         */
        const val DEFAULT_BUDGET_MILLIS: Long = 30_000
    }
}
