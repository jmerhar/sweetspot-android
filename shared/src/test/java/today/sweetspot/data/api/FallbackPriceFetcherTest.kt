package today.sweetspot.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import today.sweetspot.model.PriceSlot
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class FallbackPriceFetcherTest {

    private val timeZone = ZoneId.of("Europe/Amsterdam")
    private val from = Instant.parse("2025-06-15T00:00:00Z")
    private val to = Instant.parse("2025-06-17T00:00:00Z")

    /** Creates a [PriceFetcher] that always succeeds with the given source name. */
    private fun successFetcher(source: String, count: Int = 3): PriceFetcher {
        val base = ZonedDateTime.of(2025, 6, 15, 14, 0, 0, 0, timeZone)
        val prices = (0 until count).map { i ->
            PriceSlot(time = base.plusHours(i.toLong()), price = 0.10 + i * 0.01, durationMinutes = 60)
        }
        return object : PriceFetcher {
            override fun fetchPrices(from: Instant, to: Instant, timeZoneId: ZoneId) =
                FetchResult(prices, source)
        }
    }

    /** Creates a [PriceFetcher] that always throws with the given message. */
    private fun failFetcher(message: String): PriceFetcher = object : PriceFetcher {
        override fun fetchPrices(from: Instant, to: Instant, timeZoneId: ZoneId): FetchResult {
            throw RuntimeException(message)
        }
    }

    /** Creates a [PriceFetcher] that succeeds with an empty price list (e.g. HTTP 200, no data). */
    private fun emptyFetcher(source: String): PriceFetcher = object : PriceFetcher {
        override fun fetchPrices(from: Instant, to: Instant, timeZoneId: ZoneId) =
            FetchResult(emptyList(), source)
    }

    @Test
    fun `single fetcher succeeds`() {
        val fetcher = FallbackPriceFetcher(listOf(successFetcher("Primary")))
        val result = fetcher.fetchPrices(from, to, timeZone)

        assertEquals("Primary", result.source)
        assertEquals(3, result.prices.size)
    }

    @Test
    fun `first fails second succeeds`() {
        val fetcher = FallbackPriceFetcher(listOf(failFetcher("Primary failed"), successFetcher("Fallback")))
        val result = fetcher.fetchPrices(from, to, timeZone)

        assertEquals("Fallback", result.source)
        assertEquals(3, result.prices.size)
    }

    @Test
    fun `all fail throws last exception`() {
        val fetcher = FallbackPriceFetcher(listOf(failFetcher("First"), failFetcher("Last")))
        try {
            fetcher.fetchPrices(from, to, timeZone)
            fail("Expected exception")
        } catch (e: RuntimeException) {
            assertEquals("Last", e.message)
        }
    }

    @Test
    fun `three fetchers first two fail returns third`() {
        val fetcher = FallbackPriceFetcher(
            listOf(failFetcher("First"), failFetcher("Second"), successFetcher("Third", count = 5))
        )
        val result = fetcher.fetchPrices(from, to, timeZone)

        assertEquals("Third", result.source)
        assertEquals(5, result.prices.size)
    }

    @Test
    fun `empty result falls through to a fetcher with prices`() {
        val fetcher = FallbackPriceFetcher(listOf(emptyFetcher("Empty"), successFetcher("Fallback")))
        val result = fetcher.fetchPrices(from, to, timeZone)

        assertEquals("Fallback", result.source)
        assertEquals(3, result.prices.size)
    }

    @Test
    fun `all empty returns an empty result without throwing`() {
        val fetcher = FallbackPriceFetcher(listOf(emptyFetcher("A"), emptyFetcher("B")))
        val result = fetcher.fetchPrices(from, to, timeZone)

        assertEquals(0, result.prices.size)
    }

    @Test
    fun `an empty response is preferred over a thrown exception`() {
        val fetcher = FallbackPriceFetcher(listOf(emptyFetcher("Empty"), failFetcher("Boom")))
        val result = fetcher.fetchPrices(from, to, timeZone)

        assertEquals(0, result.prices.size)
        assertEquals("Empty", result.source)
    }

    /**
     * A controllable monotonic time source, in nanoseconds.
     *
     * The budget is measured off [FallbackPriceFetcher]'s injected time source, so driving
     * it by hand keeps these tests deterministic and instant instead of sleeping.
     */
    private class FakeNanos(private var nanos: Long = 0) {
        fun read(): Long = nanos
        fun advanceMillis(millis: Long) {
            nanos += millis * 1_000_000
        }
    }

    /** A fetcher that advances [clock] by [costMillis] and then throws, standing in for a stall. */
    private fun slowFailFetcher(message: String, clock: FakeNanos, costMillis: Long): PriceFetcher =
        object : PriceFetcher {
            override fun fetchPrices(from: Instant, to: Instant, timeZoneId: ZoneId): FetchResult {
                clock.advanceMillis(costMillis)
                throw RuntimeException(message)
            }
        }

    /** A fetcher that records that it ran, so a test can assert it was never reached. */
    private fun trackingFetcher(source: String, ran: MutableList<String>): PriceFetcher =
        object : PriceFetcher {
            override fun fetchPrices(from: Instant, to: Instant, timeZoneId: ZoneId): FetchResult {
                ran.add(source)
                return FetchResult(
                    listOf(
                        PriceSlot(
                            time = ZonedDateTime.of(2025, 6, 15, 14, 0, 0, 0, timeZone),
                            price = 0.10,
                            durationMinutes = 60
                        )
                    ),
                    source
                )
            }
        }

    @Test
    fun `budget stops the chain before a later source is started`() {
        val clock = FakeNanos()
        val ran = mutableListOf<String>()
        val fetcher = FallbackPriceFetcher(
            listOf(
                slowFailFetcher("Stalled", clock, costMillis = 31_000),
                trackingFetcher("NeverReached", ran)
            ),
            budgetMillis = 30_000,
            nanoTime = clock::read
        )

        try {
            fetcher.fetchPrices(from, to, timeZone)
            fail("Expected the first fetcher's exception to propagate")
        } catch (e: RuntimeException) {
            assertEquals("Stalled", e.message)
        }
        assertEquals(emptyList<String>(), ran)
    }

    @Test
    fun `sources that fail fast all get their turn within the budget`() {
        val clock = FakeNanos()
        val ran = mutableListOf<String>()
        val fetcher = FallbackPriceFetcher(
            listOf(
                slowFailFetcher("Fast1", clock, costMillis = 500),
                slowFailFetcher("Fast2", clock, costMillis = 500),
                trackingFetcher("Third", ran)
            ),
            budgetMillis = 30_000,
            nanoTime = clock::read
        )
        val result = fetcher.fetchPrices(from, to, timeZone)

        assertEquals("Third", result.source)
        assertEquals(listOf("Third"), ran)
    }

    @Test
    fun `the first source runs even when the time source reads past the budget`() {
        // A time source whose every reading after the first is already beyond the budget.
        // The primary is exempt from the elapsed check, so it still runs; were it not, the
        // chain would end having made no attempt at all and have no outcome to report.
        var reads = 0
        val jumpingNanos = { if (reads++ == 0) 0L else 60_000L * 1_000_000 }
        val ran = mutableListOf<String>()
        val fetcher = FallbackPriceFetcher(
            listOf(trackingFetcher("Primary", ran), trackingFetcher("Secondary", ran)),
            budgetMillis = 30_000,
            nanoTime = jumpingNanos
        )
        val result = fetcher.fetchPrices(from, to, timeZone)

        assertEquals("Primary", result.source)
        assertEquals(listOf("Primary"), ran)
    }

    @Test
    fun `budget cut short still returns an empty response over throwing`() {
        val clock = FakeNanos()
        val slowEmpty = object : PriceFetcher {
            override fun fetchPrices(from: Instant, to: Instant, timeZoneId: ZoneId): FetchResult {
                clock.advanceMillis(31_000)
                return FetchResult(emptyList(), "SlowEmpty")
            }
        }
        val ran = mutableListOf<String>()
        val fetcher = FallbackPriceFetcher(
            listOf(slowEmpty, trackingFetcher("NeverReached", ran)),
            budgetMillis = 30_000,
            nanoTime = clock::read
        )
        val result = fetcher.fetchPrices(from, to, timeZone)

        assertEquals("SlowEmpty", result.source)
        assertEquals(0, result.prices.size)
        assertEquals(emptyList<String>(), ran)
    }

    @Test
    fun `a source finishing exactly on the budget stops the chain`() {
        val clock = FakeNanos()
        val ran = mutableListOf<String>()
        val fetcher = FallbackPriceFetcher(
            listOf(
                slowFailFetcher("ExactlyOnBudget", clock, costMillis = 30_000),
                trackingFetcher("NeverReached", ran)
            ),
            budgetMillis = 30_000,
            nanoTime = clock::read
        )

        try {
            fetcher.fetchPrices(from, to, timeZone)
            fail("Expected the first fetcher's exception to propagate")
        } catch (e: RuntimeException) {
            assertEquals("ExactlyOnBudget", e.message)
        }
        assertEquals(emptyList<String>(), ran)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `zero budget throws IllegalArgumentException`() {
        FallbackPriceFetcher(listOf(successFetcher("Primary")), budgetMillis = 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `negative budget throws IllegalArgumentException`() {
        FallbackPriceFetcher(listOf(successFetcher("Primary")), budgetMillis = -1)
    }

    @Test
    fun `default budget is generous enough not to bind on healthy fetches`() {
        // A healthy chain must never be truncated by the default, so the default has to
        // exceed the time a normal multi-source attempt takes.
        assertTrue(FallbackPriceFetcher.DEFAULT_BUDGET_MILLIS >= 20_000)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `empty list throws IllegalArgumentException`() {
        FallbackPriceFetcher(emptyList())
    }
}
