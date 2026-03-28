package today.sweetspot.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test
import today.sweetspot.data.api.FetchResult
import today.sweetspot.data.api.PriceFetcher
import today.sweetspot.data.cache.CachedPrice
import today.sweetspot.data.cache.CachedPriceData
import today.sweetspot.data.cache.PriceCache
import today.sweetspot.model.PriceSlot
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class PriceRepositoryTest {

    private val timeZone = ZoneId.of("Europe/Amsterdam")

    /** Fixed "now" at 2025-06-15 16:30 CEST. */
    private val now = ZonedDateTime.of(2025, 6, 15, 16, 30, 0, 0, timeZone)
    private val fixedClock = Clock.fixed(now.toInstant(), timeZone)

    // --- Helpers ---

    /** Generates hourly price slots starting at [startHour] on the same day as [now]. */
    private fun prices(startHour: Int, count: Int, basePrice: Double = 0.10): List<PriceSlot> =
        (0 until count).map { i ->
            PriceSlot(
                time = ZonedDateTime.of(2025, 6, 15, startHour, 0, 0, 0, timeZone).plusHours(i.toLong()),
                price = basePrice + i * 0.01,
                durationMinutes = 60
            )
        }

    /** Converts a list of [PriceSlot] to [CachedPriceData] for pre-populating [FakeCache]. */
    private fun List<PriceSlot>.toCachedData(source: String = "Test") = CachedPriceData(
        source = source,
        prices = map { CachedPrice(it.time.toInstant().epochSecond, it.durationMinutes, it.price) }
    )

    // --- Fakes ---

    /** In-memory [PriceCache] with configurable cooldown behaviour and per-key storage. */
    private class FakeCache(
        initialData: CachedPriceData? = null,
        private var cooldownElapsed: Boolean = true,
        key: String = "default"
    ) : PriceCache {
        private val store = mutableMapOf<String, CachedPriceData>()
        var writeCount = 0
            private set

        init {
            if (initialData != null) store[key] = initialData
        }

        override fun isCooldownElapsed(cooldownMs: Long) = cooldownElapsed
        override fun readCached(key: String) = store[key]
        override fun write(key: String, data: CachedPriceData) {
            store[key] = data
            writeCount++
        }
        override fun clear() { store.clear() }
        override fun clearForZone(key: String) { store.remove(key) }
        override fun cooldownRemainingMs(cooldownMs: Long) = if (cooldownElapsed) 0L else cooldownMs
    }

    /**
     * Fake [PriceFetcher] that returns results from a queue.
     *
     * Each call to [fetchPrices] consumes the next result from the queue.
     */
    private class FakeFetcher(vararg results: List<PriceSlot>) : PriceFetcher {
        private val fetchResults = ArrayDeque(results.toList())
        var fetchCount = 0
            private set

        override fun fetchPrices(from: Instant, to: Instant, timeZoneId: ZoneId): FetchResult {
            fetchCount++
            return FetchResult(fetchResults.removeFirstOrNull() ?: emptyList(), "Test")
        }
    }

    // --- Tests ---

    @Test
    fun `no cache fetches from API`() {
        val fetcher = FakeFetcher(prices(startHour = 14, count = 20))
        val cache = FakeCache(initialData = null)
        val repo = PriceRepository(cache, timeZone, fetcher, fixedClock)

        val result = repo.getPrices()

        assertEquals(1, fetcher.fetchCount)
        assertEquals(1, cache.writeCount)
        // 14:00+20h = 10:00 next day, filtered from 16:00 = 18 hours
        assertEquals(18, result.prices.size)
    }

    @Test
    fun `cache hit with good coverage skips API call`() {
        val cached = prices(startHour = 14, count = 20).toCachedData()
        val fetcher = FakeFetcher()
        val cache = FakeCache(initialData = cached)
        val repo = PriceRepository(cache, timeZone, fetcher, fixedClock)

        val result = repo.getPrices()

        // 18 future hours (1080 min) >= 12h (720 min), no re-fetch needed
        assertEquals(0, fetcher.fetchCount)
        assertEquals(0, cache.writeCount)
        assertEquals(18, result.prices.size)
    }

    @Test
    fun `cache hit with low coverage triggers re-fetch`() {
        // Cache: only today's prices (16:00–23:00 = 8 hours < 12)
        val cached = prices(startHour = 16, count = 8).toCachedData()
        // Re-fetch: today + tomorrow
        val withTomorrow = prices(startHour = 14, count = 30)
        val fetcher = FakeFetcher(withTomorrow)
        val cache = FakeCache(initialData = cached, cooldownElapsed = true)
        val repo = PriceRepository(cache, timeZone, fetcher, fixedClock)

        val result = repo.getPrices()

        assertEquals(1, fetcher.fetchCount)
        assertEquals(1, cache.writeCount)
        // After re-fetch: 14:00+30h = 20:00 next day, filtered from 16:00 = 28 hours
        assertEquals(28, result.prices.size)
    }

    @Test
    fun `low coverage but cooldown not elapsed skips re-fetch`() {
        val cached = prices(startHour = 16, count = 8).toCachedData()
        val fetcher = FakeFetcher()
        val cache = FakeCache(initialData = cached, cooldownElapsed = false)
        val repo = PriceRepository(cache, timeZone, fetcher, fixedClock)

        val result = repo.getPrices()

        // Coverage is low (8 hours = 480 min) but cooldown hasn't elapsed
        assertEquals(0, fetcher.fetchCount)
        assertEquals(0, cache.writeCount)
        assertEquals(8, result.prices.size)
    }

    @Test
    fun `filters out past prices`() {
        // Prices from 10:00 to 22:00 — only 16:00+ should remain
        // (now is 16:30, slot ending at 17:00 is included since its end is after now)
        val cached = prices(startHour = 10, count = 13).toCachedData()
        val fetcher = FakeFetcher()
        val cache = FakeCache(initialData = cached, cooldownElapsed = false)
        val repo = PriceRepository(cache, timeZone, fetcher, fixedClock)

        val result = repo.getPrices()

        assertEquals(7, result.prices.size)
        assertEquals(16, result.prices.first().time.hour)
        assertEquals(22, result.prices.last().time.hour)
    }

    @Test
    fun `includes current hour even when now is mid-hour`() {
        // now is 16:30, so the 16:00 slot should be included (ends at 17:00 which is after now)
        val cached = prices(startHour = 15, count = 20).toCachedData()
        val fetcher = FakeFetcher()
        val cache = FakeCache(initialData = cached)
        val repo = PriceRepository(cache, timeZone, fetcher, fixedClock)

        val result = repo.getPrices()

        assertEquals(16, result.prices.first().time.hour)
    }

    @Test
    fun `all cached prices in the past triggers re-fetch`() {
        // Cache has yesterday's prices — all in the past
        val yesterday = prices(startHour = 0, count = 24).map {
            it.copy(time = it.time.minusDays(1))
        }.toCachedData()
        // Re-fetch returns today's prices
        val todayPrices = prices(startHour = 16, count = 20)
        val fetcher = FakeFetcher(todayPrices)
        val cache = FakeCache(initialData = yesterday)
        val repo = PriceRepository(cache, timeZone, fetcher, fixedClock)

        val result = repo.getPrices()

        // Cache read: 0 future prices (< 12h) → re-fetch
        assertEquals(1, fetcher.fetchCount)
        assertEquals(20, result.prices.size)
    }

    @Test
    fun `empty cache and empty API returns empty list`() {
        val fetcher = FakeFetcher(emptyList(), emptyList())
        val cache = FakeCache(initialData = null)
        val repo = PriceRepository(cache, timeZone, fetcher, fixedClock)

        val result = repo.getPrices()

        // Fetches once (empty), coverage 0 < 12h, re-fetches (still empty)
        assertEquals(2, fetcher.fetchCount)
        assertEquals(0, result.prices.size)
    }

    @Test
    fun `re-fetch failure falls back to stale cache data`() {
        // Cache has low-coverage data (8 hours, below MIN_COVERAGE_HOURS)
        val staleData = prices(startHour = 16, count = 8)
        val cached = staleData.toCachedData()
        // Fetcher throws on any call (network error)
        val fetcher = object : PriceFetcher {
            var fetchCount = 0
            override fun fetchPrices(from: Instant, to: Instant, timeZoneId: ZoneId): FetchResult {
                fetchCount++
                throw RuntimeException("Network error")
            }
        }
        val cache = FakeCache(initialData = cached, cooldownElapsed = true)
        val repo = PriceRepository(cache, timeZone, fetcher, fixedClock)

        val result = repo.getPrices()

        // Re-fetch was attempted but failed
        assertEquals(1, fetcher.fetchCount)
        // Falls back to stale filtered data instead of throwing
        assertEquals(8, result.prices.size)
    }

    @Test(expected = RuntimeException::class)
    fun `re-fetch failure with no stale data propagates error`() {
        // Cache has all-past data — filtered list will be empty
        val pastData = prices(startHour = 0, count = 10).map {
            it.copy(time = it.time.minusDays(1))
        }.toCachedData()
        val fetcher = object : PriceFetcher {
            override fun fetchPrices(from: Instant, to: Instant, timeZoneId: ZoneId): FetchResult {
                throw RuntimeException("Network error")
            }
        }
        val cache = FakeCache(initialData = pastData, cooldownElapsed = true)
        val repo = PriceRepository(cache, timeZone, fetcher, fixedClock)

        repo.getPrices() // should throw
    }
}
