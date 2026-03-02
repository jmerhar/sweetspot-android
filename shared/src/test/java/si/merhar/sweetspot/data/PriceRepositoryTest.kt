package si.merhar.sweetspot.data

import org.junit.Assert.assertEquals
import org.junit.Test
import si.merhar.sweetspot.model.HourlyPrice
import java.time.Clock
import java.time.ZoneId
import java.time.ZonedDateTime

class PriceRepositoryTest {

    private val zone = ZoneId.of("Europe/Amsterdam")

    /** Fixed "now" at 2025-06-15 16:30 CEST. */
    private val now = ZonedDateTime.of(2025, 6, 15, 16, 30, 0, 0, zone)
    private val fixedClock = Clock.fixed(now.toInstant(), zone)

    /** A dummy JSON string used as cache content. */
    private val cachedJson = """{"cached":true}"""

    // --- Helpers ---

    /** Generates hourly prices starting at [startHour] on the same day as [now]. */
    private fun prices(startHour: Int, count: Int, basePrice: Double = 0.10): List<HourlyPrice> =
        (0 until count).map { i ->
            HourlyPrice(
                time = ZonedDateTime.of(2025, 6, 15, startHour, 0, 0, 0, zone).plusHours(i.toLong()),
                price = basePrice + i * 0.01
            )
        }

    // --- Fakes ---

    /** In-memory [PriceCache] with configurable cooldown behavior. */
    private class FakeCache(
        private var json: String? = null,
        private var cooldownElapsed: Boolean = true
    ) : PriceCache {
        var writeCount = 0
            private set

        override fun isCooldownElapsed(cooldownMs: Long) = cooldownElapsed
        override fun readCachedJson() = json
        override fun write(json: String) {
            this.json = json
            writeCount++
        }
    }

    /**
     * Fake [PriceFetcher] that returns results from a queue.
     *
     * Each call to [parseJson] (whether parsing cache or fresh data) consumes
     * the next result from the queue. This lets tests control what the cache
     * parse returns vs. what a re-fetch returns.
     */
    private class FakeFetcher(vararg results: List<HourlyPrice>) : PriceFetcher {
        private val parseResults = ArrayDeque(results.toList())
        var fetchCount = 0
            private set

        override fun fetchRawJson(zoneId: ZoneId): String {
            fetchCount++
            return """{"fetch":$fetchCount}"""
        }

        override fun parseJson(rawJson: String, zoneId: ZoneId): List<HourlyPrice> {
            return parseResults.removeFirstOrNull() ?: emptyList()
        }
    }

    // --- Tests ---

    @Test
    fun `no cache fetches from API`() {
        val fetcher = FakeFetcher(prices(startHour = 14, count = 20))
        val cache = FakeCache(json = null)
        val repo = PriceRepository(cache, zone, fetcher, fixedClock)

        val result = repo.getPrices()

        assertEquals(1, fetcher.fetchCount)
        assertEquals(1, cache.writeCount)
        // 14:00+20h = 10:00 next day, filtered from 16:00 = 18 hours
        assertEquals(18, result.size)
    }

    @Test
    fun `cache hit with good coverage skips API call`() {
        val fetcher = FakeFetcher(prices(startHour = 14, count = 20))
        val cache = FakeCache(json = cachedJson)
        val repo = PriceRepository(cache, zone, fetcher, fixedClock)

        val result = repo.getPrices()

        // 18 future hours >= 12, no re-fetch needed
        assertEquals(0, fetcher.fetchCount)
        assertEquals(0, cache.writeCount)
        assertEquals(18, result.size)
    }

    @Test
    fun `cache hit with low coverage triggers re-fetch`() {
        // First parse (cache): only today's prices (16:00–23:00 = 8 hours < 12)
        val todayOnly = prices(startHour = 16, count = 8)
        // Second parse (re-fetch): today + tomorrow
        val withTomorrow = prices(startHour = 14, count = 30)
        val fetcher = FakeFetcher(todayOnly, withTomorrow)
        val cache = FakeCache(json = cachedJson, cooldownElapsed = true)
        val repo = PriceRepository(cache, zone, fetcher, fixedClock)

        val result = repo.getPrices()

        assertEquals(1, fetcher.fetchCount)
        assertEquals(1, cache.writeCount)
        // After re-fetch: 14:00+30h = 20:00 next day, filtered from 16:00 = 28 hours
        assertEquals(28, result.size)
    }

    @Test
    fun `low coverage but cooldown not elapsed skips re-fetch`() {
        val todayOnly = prices(startHour = 16, count = 8)
        val fetcher = FakeFetcher(todayOnly)
        val cache = FakeCache(json = cachedJson, cooldownElapsed = false)
        val repo = PriceRepository(cache, zone, fetcher, fixedClock)

        val result = repo.getPrices()

        // Coverage is low (8 hours) but cooldown hasn't elapsed
        assertEquals(0, fetcher.fetchCount)
        assertEquals(0, cache.writeCount)
        assertEquals(8, result.size)
    }

    @Test
    fun `filters out past prices`() {
        // Prices from 10:00 to 22:00 — only 16:00+ should remain
        val allPrices = prices(startHour = 10, count = 13)
        val fetcher = FakeFetcher(allPrices)
        val cache = FakeCache(json = cachedJson, cooldownElapsed = false)
        val repo = PriceRepository(cache, zone, fetcher, fixedClock)

        val result = repo.getPrices()

        assertEquals(7, result.size)
        assertEquals(16, result.first().time.hour)
        assertEquals(22, result.last().time.hour)
    }

    @Test
    fun `includes current hour even when now is mid-hour`() {
        // now is 16:30, so the 16:00 slot should be included
        val allPrices = prices(startHour = 15, count = 20)
        val fetcher = FakeFetcher(allPrices)
        val cache = FakeCache(json = cachedJson)
        val repo = PriceRepository(cache, zone, fetcher, fixedClock)

        val result = repo.getPrices()

        assertEquals(16, result.first().time.hour)
    }

    @Test
    fun `all cached prices in the past triggers re-fetch`() {
        // Cache has yesterday's prices — all in the past
        val yesterday = prices(startHour = 0, count = 24).map {
            it.copy(time = it.time.minusDays(1))
        }
        // Re-fetch returns today's prices
        val todayPrices = prices(startHour = 16, count = 20)
        val fetcher = FakeFetcher(yesterday, todayPrices)
        val cache = FakeCache(json = cachedJson)
        val repo = PriceRepository(cache, zone, fetcher, fixedClock)

        val result = repo.getPrices()

        // Cache parse: 0 future prices (< 12) → re-fetch
        assertEquals(1, fetcher.fetchCount)
        assertEquals(20, result.size)
    }

    @Test
    fun `empty cache and empty API returns empty list`() {
        val fetcher = FakeFetcher(emptyList(), emptyList())
        val cache = FakeCache(json = null)
        val repo = PriceRepository(cache, zone, fetcher, fixedClock)

        val result = repo.getPrices()

        // Fetches once (empty), coverage 0 < 12, re-fetches (still empty)
        assertEquals(2, fetcher.fetchCount)
        assertEquals(0, result.size)
    }
}
