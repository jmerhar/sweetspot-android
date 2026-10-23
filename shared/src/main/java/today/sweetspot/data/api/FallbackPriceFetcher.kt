package today.sweetspot.data.api

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * A [PriceFetcher] that tries multiple fetchers in order and returns the first complete result.
 *
 * Used to provide resilience when the primary data source is unavailable. For example,
 * NL uses ENTSO-E as primary and EnergyZero as fallback.
 *
 * A result is complete when it has prices and, once [DayAheadSchedule] says the next delivery
 * day is due, includes that day. Sources do not publish the next day at the same moment: one can
 * lag the others by hours, and a fallback that takes its prices from the same upstream lags with
 * it. So a result with only the current day is held back while the remaining sources are tried,
 * and is returned only if none of them does better.
 *
 * Because the fetchers run in sequence, their timeouts add up: a zone with three sources
 * can otherwise keep the caller waiting for three full per-request timeouts before
 * reporting failure. [budgetMillis] caps that by stopping the chain once the budget is
 * spent, trading the chance of a later source succeeding against how long a user waits
 * to be told there are no prices.
 *
 * @param fetchers Ordered list of fetchers to try. Must not be empty.
 * @param budgetMillis Total time to spend on the chain before giving up on the remaining
 *   sources. Must be positive: zero or less would leave every fetcher after the first
 *   unreachable, silently turning a multi-source zone into a single-source one.
 * @param nanoTime Monotonic time source in nanoseconds (injectable for testing).
 * @param clock Wall-clock time source, for deciding whether the next delivery day is due
 *   (injectable for testing).
 * @throws IllegalArgumentException if [fetchers] is empty or [budgetMillis] is not positive.
 */
class FallbackPriceFetcher(
    internal val fetchers: List<PriceFetcher>,
    private val budgetMillis: Long = DEFAULT_BUDGET_MILLIS,
    private val nanoTime: () -> Long = System::nanoTime,
    private val clock: Clock = Clock.systemUTC()
) : PriceFetcher {

    init {
        require(fetchers.isNotEmpty()) { "At least one fetcher required" }
        require(budgetMillis > 0) { "Budget must be positive, got $budgetMillis" }
    }

    /**
     * Tries each fetcher in order and returns the first complete result.
     *
     * A fetcher that responds successfully but with no prices (e.g. HTTP 200 and a well-formed but
     * empty day-ahead document) is not useful, so the chain keeps going — a later source may still
     * have data. One that responds with prices but lacks a next delivery day that is already due
     * is kept in reserve while the chain keeps going. If no source is complete, the reserve that
     * reaches furthest is returned, an earlier source winning a tie, so that when no source has
     * the next day yet the primary's prices are kept. If no fetcher yields prices, an empty result
     * is returned when at least one responded (the caller treats an empty list as "not enough
     * data"); the last exception is thrown only when every fetcher failed outright.
     *
     * The budget is checked after each attempt, so it only ever decides whether to start the
     * *next* source and the first fetcher always runs. The outcome is therefore always drawn
     * from a real attempt, which is what lets this return a result or throw rather than finish
     * with nothing to report. Checking after an attempt also matches what the budget can
     * actually control: a fetcher already in progress cannot be interrupted here, so one source
     * may overrun the budget, bounding the total at roughly the budget plus a single source's
     * timeout.
     *
     * @param from Start of the requested period (inclusive).
     * @param to End of the requested period (exclusive).
     * @param timeZoneId Timezone to convert timestamps to local time.
     * @return The first complete [FetchResult]; failing that, the furthest-reaching one with
     *   prices, or an empty one if every fetcher tried was empty.
     * @throws Exception the exception thrown by the last fetcher if every fetcher tried threw.
     */
    override fun fetchPrices(from: Instant, to: Instant, timeZoneId: ZoneId): FetchResult {
        val startNanos = nanoTime()
        val now = clock.instant()
        var lastException: Exception? = null
        var lastEmpty: FetchResult? = null
        var partial: FetchResult? = null
        for (fetcher in fetchers) {
            try {
                val result = fetcher.fetchPrices(from, to, timeZoneId)
                when {
                    result.prices.isEmpty() -> lastEmpty = result
                    !DayAheadSchedule.lacksDueNextDay(result.prices, now, to) -> return result
                    partial == null || coverageEnd(result) > coverageEnd(partial) -> partial = result
                }
            } catch (e: Exception) {
                lastException = e
            }
            if (elapsedMillis(startNanos) >= budgetMillis) break
        }
        // Safe: the loop body runs at least once, so it recorded a result or an exception.
        return partial ?: lastEmpty ?: throw lastException!!
    }

    /** End of the last slot in [result]: how far ahead its prices reach. */
    private fun coverageEnd(result: FetchResult): Instant =
        result.prices.maxOf { it.time.plusMinutes(it.durationMinutes.toLong()).toInstant() }

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
