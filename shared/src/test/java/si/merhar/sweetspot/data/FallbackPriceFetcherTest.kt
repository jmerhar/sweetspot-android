package si.merhar.sweetspot.data

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import si.merhar.sweetspot.model.PriceSlot
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

    @Test(expected = IllegalArgumentException::class)
    fun `empty list throws IllegalArgumentException`() {
        FallbackPriceFetcher(emptyList())
    }
}
