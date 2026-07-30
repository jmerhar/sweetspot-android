package today.sweetspot.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import today.sweetspot.data.stats.InstrumentedPriceFetcher
import today.sweetspot.data.stats.StatsCollector
import today.sweetspot.data.stats.StatsRecord
import today.sweetspot.model.Countries

/**
 * Tests for [defaultPriceFetcherFactory] — the chain composition logic that maps a zone and the
 * user's source-order preference onto the right [PriceFetcher]s. Pure JUnit; asserts on the
 * (module-internal) fetcher list of the resulting [FallbackPriceFetcher].
 */
class PriceFetcherFactoryTest {

    private val token = "test-token"
    private val nl = Countries.findPriceZoneById("NL")!!

    /** Unwraps the fallback chain the factory always produces. */
    private fun chain(f: PriceFetcher): List<PriceFetcher> = (f as FallbackPriceFetcher).fetchers

    @Test
    fun `null source order uses zone defaults in priority order`() {
        val chain = chain(defaultPriceFetcherFactory(token).create(nl))
        assertEquals(
            listOf(EntsoeApi::class, EnergyChartsApi::class, EnergyZeroApi::class),
            chain.map { it::class }
        )
    }

    @Test
    fun `custom source order reorders the chain`() {
        val chain = chain(
            defaultPriceFetcherFactory(token, listOf("energyzero", "entsoe", "energycharts")).create(nl)
        )
        assertEquals(
            listOf(EnergyZeroApi::class, EntsoeApi::class, EnergyChartsApi::class),
            chain.map { it::class }
        )
    }

    @Test
    fun `source order not applicable to the zone falls back to defaults`() {
        // spothinta does not cover NL → filtered list is empty → zone defaults are used.
        val chain = chain(defaultPriceFetcherFactory(token, listOf("spothinta")).create(nl))
        assertEquals(
            listOf(EntsoeApi::class, EnergyChartsApi::class, EnergyZeroApi::class),
            chain.map { it::class }
        )
    }

    @Test
    fun `partially applicable source order keeps only the applicable sources`() {
        val chain = chain(defaultPriceFetcherFactory(token, listOf("energyzero", "spothinta")).create(nl))
        assertEquals(listOf(EnergyZeroApi::class), chain.map { it::class })
    }

    @Test
    fun `a disabled source is dropped even with the default order`() {
        // Regression: disabling a source must take effect without also customising the order.
        val chain = chain(defaultPriceFetcherFactory(token, disabledSources = setOf("energyzero")).create(nl))
        assertEquals(listOf(EntsoeApi::class, EnergyChartsApi::class), chain.map { it::class })
    }

    @Test
    fun `a disabled source is dropped from a custom order`() {
        val chain = chain(
            defaultPriceFetcherFactory(token, listOf("energyzero", "entsoe", "energycharts"), disabledSources = setOf("entsoe")).create(nl)
        )
        assertEquals(listOf(EnergyZeroApi::class, EnergyChartsApi::class), chain.map { it::class })
    }

    @Test
    fun `disabling every applicable source falls back to the zone defaults`() {
        // Defensive backstop: never leave a zone with nothing to query.
        val chain = chain(
            defaultPriceFetcherFactory(token, disabledSources = setOf("entsoe", "energycharts", "energyzero")).create(nl)
        )
        assertEquals(
            listOf(EntsoeApi::class, EnergyChartsApi::class, EnergyZeroApi::class),
            chain.map { it::class }
        )
    }

    @Test
    fun `no stats collector leaves fetchers unwrapped`() {
        assertTrue(chain(defaultPriceFetcherFactory(token).create(nl)).none { it is InstrumentedPriceFetcher })
    }

    @Test
    fun `stats collector wraps each fetcher in an instrumented decorator`() {
        val chain = chain(defaultPriceFetcherFactory(token, null, FakeCollector(), device = "watch").create(nl))
        assertTrue(chain.all { it is InstrumentedPriceFetcher })
        assertEquals(
            listOf(EntsoeApi::class, EnergyChartsApi::class, EnergyZeroApi::class),
            chain.map { (it as InstrumentedPriceFetcher).delegate::class }
        )
        assertEquals(
            listOf("entsoe", "energycharts", "energyzero"),
            chain.map { (it as InstrumentedPriceFetcher).sourceId }
        )
    }

    @Test
    fun `nordic zone routes to spot-hinta`() {
        val fi = Countries.findPriceZoneById("FI")!!
        assertTrue(SpotHintaApi::class in chain(defaultPriceFetcherFactory(token).create(fi)).map { it::class })
    }

    @Test
    fun `austria zone routes to awattar`() {
        val at = Countries.findPriceZoneById("AT")!!
        assertTrue(AwattarApi::class in chain(defaultPriceFetcherFactory(token).create(at)).map { it::class })
    }

    private class FakeCollector : StatsCollector {
        override fun record(record: StatsRecord) {}
        override fun readAll(): List<StatsRecord> = emptyList()
        override fun clear() {}
    }
}
