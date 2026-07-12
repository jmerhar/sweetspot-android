package today.sweetspot.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import today.sweetspot.data.api.HttpException
import today.sweetspot.data.api.TariffFetcher
import today.sweetspot.data.cache.RawTariff
import today.sweetspot.data.cache.TariffCache
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class TariffRepositoryTest {

    private val fixedClock = Clock.fixed(Instant.parse("2026-07-11T12:00:00Z"), ZoneId.of("UTC"))
    private val nowMs = fixedClock.millis()

    private fun feed(usable: Boolean, surcharge: Double = 0.015) = """
        {"country":"NL","currency":"EUR","usable":$usable,
         "taxes":[{"id":"vat","type":"percentage","value":0.21}],
         "suppliers":[{"id":"frankenergie","name":"Frank Energie","surchargePerKwh":$surcharge}]}
    """.trimIndent()

    /** In-memory [TariffCache]. */
    private class FakeCache(seed: RawTariff? = null, seedKey: String = "NL") : TariffCache {
        val store = mutableMapOf<String, RawTariff>()
        var writeCount = 0; private set
        init { if (seed != null) store[seedKey.lowercase()] = seed }
        override fun read(countryCode: String) = store[countryCode.lowercase()]
        override fun write(countryCode: String, raw: String, fetchedAtMs: Long) {
            store[countryCode.lowercase()] = RawTariff(raw, fetchedAtMs); writeCount++
        }
        override fun clear() { store.clear() }
    }

    /** Fetcher that returns a fixed body or throws. */
    private class FakeFetcher(private val body: String? = null, private val error: Exception? = null) : TariffFetcher {
        var fetchCount = 0; private set
        override fun fetchRaw(countryCode: String): String {
            fetchCount++
            error?.let { throw it }
            return body!!
        }
    }

    @Test
    fun `cached returns a usable parsed feed without touching the network`() {
        val cache = FakeCache(RawTariff(feed(usable = true), nowMs))
        val fetcher = FakeFetcher(error = RuntimeException("should not be called"))
        val repo = TariffRepository(cache, fetcher, fixedClock)

        val result = repo.cached("NL")

        assertEquals(0, fetcher.fetchCount)
        assertEquals("Frank Energie", result!!.tariff.suppliers[0].name)
        assertEquals(nowMs, result.fetchedAtMs)
    }

    @Test
    fun `cached returns null for an unusable or absent or corrupt feed`() {
        assertNull(TariffRepository(FakeCache(RawTariff(feed(usable = false), nowMs)), FakeFetcher("")).cached("NL"))
        assertNull(TariffRepository(FakeCache(), FakeFetcher("")).cached("NL"))
        assertNull(TariffRepository(FakeCache(RawTariff("garbage", nowMs)), FakeFetcher("")).cached("NL"))
    }

    @Test
    fun `refresh fetches, caches, and returns a usable feed`() {
        val cache = FakeCache()
        val fetcher = FakeFetcher(feed(usable = true, surcharge = 0.02))
        val repo = TariffRepository(cache, fetcher, fixedClock)

        val result = repo.refresh("NL")

        assertEquals(1, fetcher.fetchCount)
        assertEquals(1, cache.writeCount)
        assertEquals(0.02, result!!.tariff.suppliers[0].surchargePerKwh, 1e-9)
        assertEquals(nowMs, result.fetchedAtMs)
    }

    @Test
    fun `refresh keeps last-good cache on network error`() {
        val cache = FakeCache(RawTariff(feed(usable = true, surcharge = 0.015), nowMs - 1000))
        val fetcher = FakeFetcher(error = RuntimeException("network down"))
        val repo = TariffRepository(cache, fetcher, fixedClock)

        val result = repo.refresh("NL")

        assertEquals(0, cache.writeCount)                 // not overwritten
        assertEquals(0.015, result!!.tariff.suppliers[0].surchargePerKwh, 1e-9)  // last-good returned
    }

    @Test
    fun `refresh keeps last-good cache on a 404`() {
        val cache = FakeCache(RawTariff(feed(usable = true), nowMs))
        val fetcher = FakeFetcher(error = HttpException(404, "Not found"))
        val repo = TariffRepository(cache, fetcher, fixedClock)

        assertTrue(repo.refresh("NL") != null)
        assertEquals(0, cache.writeCount)
    }

    @Test
    fun `refresh does not overwrite good data with an unusable feed`() {
        val cache = FakeCache(RawTariff(feed(usable = true), nowMs))
        val fetcher = FakeFetcher(feed(usable = false))
        val repo = TariffRepository(cache, fetcher, fixedClock)

        val result = repo.refresh("NL")

        assertEquals(0, cache.writeCount)                 // last-good kept
        assertTrue(result != null && result.tariff.usable)
    }

    @Test
    fun `refresh returns null when fetch fails and nothing is cached`() {
        val repo = TariffRepository(FakeCache(), FakeFetcher(error = RuntimeException("offline")), fixedClock)
        assertNull(repo.refresh("NL"))
    }
}
