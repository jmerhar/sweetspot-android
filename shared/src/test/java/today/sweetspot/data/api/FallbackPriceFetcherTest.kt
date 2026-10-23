package today.sweetspot.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import today.sweetspot.model.PriceSlot
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
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

    /** A fetcher that records that it ran and then throws, for asserting an attempt happened. */
    private fun trackingFailFetcher(message: String, ran: MutableList<String>): PriceFetcher =
        object : PriceFetcher {
            override fun fetchPrices(from: Instant, to: Instant, timeZoneId: ZoneId): FetchResult {
                ran.add(message)
                throw RuntimeException(message)
            }
        }

    @Test
    fun `the chain still makes one attempt when the time source reads past the budget`() {
        // A time source whose every reading after the first is already beyond the budget, paired
        // with a primary that fails, so the elapsed check is reached rather than short-circuited
        // by an early return. Testing the budget only after an attempt is what keeps the primary
        // unconditional: were the check consulted first, this chain would end having run nothing
        // and would have neither a result nor an exception to hand back.
        var reads = 0
        val jumpingNanos = { if (reads++ == 0) 0L else 60_000L * 1_000_000 }
        val ran = mutableListOf<String>()
        val fetcher = FallbackPriceFetcher(
            listOf(trackingFailFetcher("Primary", ran), trackingFetcher("NeverReached", ran)),
            budgetMillis = 30_000,
            nanoTime = jumpingNanos
        )

        var thrown: RuntimeException? = null
        try {
            fetcher.fetchPrices(from, to, timeZone)
            fail("Expected the primary's exception, which requires that it actually ran")
        } catch (e: RuntimeException) {
            thrown = e
        }

        // Asserted before the exception itself: if the primary is ever skipped, this says so
        // directly, where the exception would only show up as a missing message.
        assertEquals(listOf("Primary"), ran)
        assertEquals("Primary", thrown?.message)
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

    /** Start of 23 Oct 2026 in Amsterdam, the day the next-day tests fetch on. */
    private val dayFrom = Instant.parse("2026-10-22T22:00:00Z")

    /** End of that day's request: the start of 25 Oct in Amsterdam. */
    private val dayTo = Instant.parse("2026-10-24T22:00:00Z")

    /** 18:00 CEST on 23 Oct, after the 24 Oct prices are due. */
    private val evening = Clock.fixed(Instant.parse("2026-10-23T16:00:00Z"), ZoneOffset.UTC)

    /** 11:00 CEST on 23 Oct, before the 24 Oct prices are due. */
    private val morning = Clock.fixed(Instant.parse("2026-10-23T09:00:00Z"), ZoneOffset.UTC)

    /** [hours] of [minutes]-long slots from the start of 23 Oct in Amsterdam. */
    private fun hours(hours: Int, minutes: Int = 15): List<PriceSlot> =
        (0 until hours * 60 / minutes).map { i ->
            PriceSlot(dayFrom.plusSeconds(i * minutes * 60L).atZone(timeZone), 0.10, minutes)
        }

    /** A fetcher that returns [prices] as [source] and records each call in [ran]. */
    private fun pricesFetcher(source: String, prices: List<PriceSlot>, ran: MutableList<String>): PriceFetcher =
        object : PriceFetcher {
            override fun fetchPrices(from: Instant, to: Instant, timeZoneId: ZoneId): FetchResult {
                ran.add(source)
                return FetchResult(prices, source)
            }
        }

    @Test
    fun `before the next day is due a current-day result ends the chain`() {
        val ran = mutableListOf<String>()
        val fetcher = FallbackPriceFetcher(
            listOf(pricesFetcher("Primary", hours(24), ran), pricesFetcher("Fallback", hours(48), ran)),
            clock = morning
        )

        assertEquals("Primary", fetcher.fetchPrices(dayFrom, dayTo, timeZone).source)
        assertEquals(listOf("Primary"), ran)
    }

    @Test
    fun `once the next day is due a fallback that has it replaces a current-day result`() {
        val ran = mutableListOf<String>()
        val fetcher = FallbackPriceFetcher(
            listOf(pricesFetcher("Primary", hours(24), ran), pricesFetcher("Fallback", hours(48), ran)),
            clock = evening
        )
        val result = fetcher.fetchPrices(dayFrom, dayTo, timeZone)

        assertEquals("Fallback", result.source)
        assertEquals(192, result.prices.size)
    }

    @Test
    fun `the chain continues past a fallback that shares the primary's gap`() {
        // A source that takes its prices from the same upstream as the primary misses the same day.
        val ran = mutableListOf<String>()
        val fetcher = FallbackPriceFetcher(
            listOf(
                pricesFetcher("ENTSO-E", hours(24), ran),
                pricesFetcher("Energy-Charts", hours(24), ran),
                pricesFetcher("EnergyZero", hours(48, minutes = 60), ran)
            ),
            clock = evening
        )

        assertEquals("EnergyZero", fetcher.fetchPrices(dayFrom, dayTo, timeZone).source)
        assertEquals(listOf("ENTSO-E", "Energy-Charts", "EnergyZero"), ran)
    }

    @Test
    fun `a result with the next day ends the chain once it is due`() {
        val ran = mutableListOf<String>()
        val fetcher = FallbackPriceFetcher(
            listOf(pricesFetcher("Primary", hours(48), ran), pricesFetcher("Fallback", hours(48), ran)),
            clock = evening
        )

        assertEquals("Primary", fetcher.fetchPrices(dayFrom, dayTo, timeZone).source)
        assertEquals(listOf("Primary"), ran)
    }

    @Test
    fun `when no source has the next day the furthest-reaching result is returned`() {
        val ran = mutableListOf<String>()
        val fetcher = FallbackPriceFetcher(
            listOf(pricesFetcher("Short", hours(20), ran), pricesFetcher("Longer", hours(24), ran)),
            clock = evening
        )

        assertEquals("Longer", fetcher.fetchPrices(dayFrom, dayTo, timeZone).source)
    }

    @Test
    fun `a later source that reaches less far does not displace an earlier one`() {
        val ran = mutableListOf<String>()
        val fetcher = FallbackPriceFetcher(
            listOf(pricesFetcher("Longer", hours(24), ran), pricesFetcher("Short", hours(20), ran)),
            clock = evening
        )

        assertEquals("Longer", fetcher.fetchPrices(dayFrom, dayTo, timeZone).source)
        assertEquals(listOf("Longer", "Short"), ran)
    }

    @Test
    fun `an equal reach keeps the earlier source`() {
        // Both end at midnight, so the primary's quarter-hours are kept over hourly prices.
        val ran = mutableListOf<String>()
        val fetcher = FallbackPriceFetcher(
            listOf(pricesFetcher("Primary", hours(24), ran), pricesFetcher("Hourly", hours(24, minutes = 60), ran)),
            clock = evening
        )
        val result = fetcher.fetchPrices(dayFrom, dayTo, timeZone)

        assertEquals("Primary", result.source)
        assertEquals(96, result.prices.size)
        assertEquals(listOf("Primary", "Hourly"), ran)
    }

    @Test
    fun `a current-day result is preferred over failures and empty responses around it`() {
        val fetcher = FallbackPriceFetcher(
            listOf(
                failFetcher("First"),
                pricesFetcher("Partial", hours(24), mutableListOf()),
                emptyFetcher("Empty"),
                failFetcher("Last")
            ),
            clock = evening
        )

        assertEquals("Partial", fetcher.fetchPrices(dayFrom, dayTo, timeZone).source)
    }

    @Test
    fun `a period that ends before the next day does not ask for it`() {
        val ran = mutableListOf<String>()
        val fetcher = FallbackPriceFetcher(
            listOf(pricesFetcher("Primary", hours(24), ran), pricesFetcher("Fallback", hours(48), ran)),
            clock = evening
        )
        val endOfDay = Instant.parse("2026-10-23T22:00:00Z")

        assertEquals("Primary", fetcher.fetchPrices(dayFrom, endOfDay, timeZone).source)
        assertEquals(listOf("Primary"), ran)
    }

    @Test
    fun `the budget still stops the chain after a current-day result`() {
        val nanos = FakeNanos()
        val ran = mutableListOf<String>()
        val slowPartial = object : PriceFetcher {
            override fun fetchPrices(from: Instant, to: Instant, timeZoneId: ZoneId): FetchResult {
                nanos.advanceMillis(31_000)
                return FetchResult(hours(24), "SlowPartial")
            }
        }
        val fetcher = FallbackPriceFetcher(
            listOf(slowPartial, pricesFetcher("NeverReached", hours(48), ran)),
            budgetMillis = 30_000,
            nanoTime = nanos::read,
            clock = evening
        )

        assertEquals("SlowPartial", fetcher.fetchPrices(dayFrom, dayTo, timeZone).source)
        assertEquals(emptyList<String>(), ran)
    }
}
