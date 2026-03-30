package today.sweetspot.data.stats

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import today.sweetspot.data.api.FetchResult
import today.sweetspot.data.api.PriceFetcher
import today.sweetspot.model.PriceSlot
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

class InstrumentedPriceFetcherTest {

    private val timeZone = ZoneId.of("Europe/Amsterdam")
    private val from = Instant.parse("2025-06-15T00:00:00Z")
    private val to = Instant.parse("2025-06-17T00:00:00Z")
    private val fixedClock = Clock.fixed(Instant.parse("2025-06-15T12:00:00Z"), ZoneOffset.UTC)

    private val fakePrices = listOf(
        PriceSlot(ZonedDateTime.of(2025, 6, 15, 14, 0, 0, 0, timeZone), 0.10, 60),
        PriceSlot(ZonedDateTime.of(2025, 6, 15, 15, 0, 0, 0, timeZone), 0.12, 60)
    )

    private fun successFetcher(prices: List<PriceSlot> = fakePrices): PriceFetcher =
        object : PriceFetcher {
            override fun fetchPrices(from: Instant, to: Instant, timeZoneId: ZoneId) =
                FetchResult(prices, "TestSource")
        }

    private fun emptyFetcher(): PriceFetcher = object : PriceFetcher {
        override fun fetchPrices(from: Instant, to: Instant, timeZoneId: ZoneId) =
            FetchResult(emptyList(), "TestSource")
    }

    private fun failFetcher(exception: Exception = RuntimeException("fail")): PriceFetcher =
        object : PriceFetcher {
            override fun fetchPrices(from: Instant, to: Instant, timeZoneId: ZoneId): FetchResult {
                throw exception
            }
        }

    @Test
    fun `success records ok`() {
        val collector = FakeStatsCollector()
        val instrumented = InstrumentedPriceFetcher(
            successFetcher(), "entsoe", "NL", "phone", collector, fixedClock
        )

        val result = instrumented.fetchPrices(from, to, timeZone)
        assertEquals(2, result.prices.size)
        assertEquals("TestSource", result.source)

        assertEquals(1, collector.records.size)
        val record = collector.records[0]
        assertEquals("NL", record.zone)
        assertEquals("entsoe", record.source)
        assertEquals("phone", record.device)
        assertTrue(record.success)
        assertEquals("", record.errorCategory)
        assertEquals(fixedClock.instant().epochSecond, record.epochSecond)
    }

    @Test
    fun `empty result records EMPTY`() {
        val collector = FakeStatsCollector()
        val instrumented = InstrumentedPriceFetcher(
            emptyFetcher(), "energyzero", "NL", "watch", collector, fixedClock
        )

        val result = instrumented.fetchPrices(from, to, timeZone)
        assertTrue(result.prices.isEmpty())

        assertEquals(1, collector.records.size)
        val record = collector.records[0]
        assertEquals(false, record.success)
        assertEquals("EMPTY", record.errorCategory)
        assertEquals("watch", record.device)
    }

    @Test
    fun `failure records error category and rethrows`() {
        val collector = FakeStatsCollector()
        val instrumented = InstrumentedPriceFetcher(
            failFetcher(java.net.SocketTimeoutException("timeout")),
            "spothinta", "FI", "phone", collector, fixedClock
        )

        try {
            instrumented.fetchPrices(from, to, timeZone)
            fail("Expected exception")
        } catch (e: java.net.SocketTimeoutException) {
            assertEquals("timeout", e.message)
        }

        assertEquals(1, collector.records.size)
        val record = collector.records[0]
        assertEquals(false, record.success)
        assertEquals("TIMEOUT", record.errorCategory)
        assertEquals("FI", record.zone)
        assertEquals("spothinta", record.source)
    }

    @Test
    fun `delegates return value unchanged`() {
        val collector = FakeStatsCollector()
        val instrumented = InstrumentedPriceFetcher(
            successFetcher(), "entsoe", "NL", "phone", collector, fixedClock
        )

        val result = instrumented.fetchPrices(from, to, timeZone)
        assertEquals(fakePrices, result.prices)
        assertEquals("TestSource", result.source)
    }

    @Test
    fun `uses clock for timestamp`() {
        val customClock = Clock.fixed(Instant.parse("2030-01-01T00:00:00Z"), ZoneOffset.UTC)
        val collector = FakeStatsCollector()
        val instrumented = InstrumentedPriceFetcher(
            successFetcher(), "entsoe", "NL", "phone", collector, customClock
        )

        instrumented.fetchPrices(from, to, timeZone)
        assertEquals(Instant.parse("2030-01-01T00:00:00Z").epochSecond, collector.records[0].epochSecond)
    }

    @Test
    fun `multiple calls accumulate records`() {
        val collector = FakeStatsCollector()
        val instrumented = InstrumentedPriceFetcher(
            successFetcher(), "entsoe", "NL", "phone", collector, fixedClock
        )

        instrumented.fetchPrices(from, to, timeZone)
        instrumented.fetchPrices(from, to, timeZone)

        assertEquals(2, collector.records.size)
    }

    /** In-memory [StatsCollector] for testing. */
    private class FakeStatsCollector : StatsCollector {
        val records = mutableListOf<StatsRecord>()
        override fun record(record: StatsRecord) { records.add(record) }
        override fun readAll(): List<StatsRecord> = records.toList()
        override fun clear() { records.clear() }
    }
}
