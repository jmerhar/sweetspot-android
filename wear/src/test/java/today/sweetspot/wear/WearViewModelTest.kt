package today.sweetspot.wear

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import today.sweetspot.data.api.FetchResult
import today.sweetspot.data.api.PriceFetcher
import today.sweetspot.data.cache.CachedPriceData
import today.sweetspot.data.cache.PriceCache
import today.sweetspot.data.stats.StatsCollector
import today.sweetspot.data.stats.StatsRecord
import today.sweetspot.model.Appliance
import today.sweetspot.model.Countries
import today.sweetspot.model.PriceSlot
import today.sweetspot.util.UiText
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Tests for [WearViewModel] state management, settings/appliance handling, and async price
 * fetching. Uses injected fakes for [PriceFetcher], [PriceCache], [StatsCollector], and [WearSync]
 * (so no Play Services), and a test dispatcher so coroutines complete deterministically.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class WearViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var app: Application

    private class FakeCache : PriceCache {
        override fun isCooldownElapsed(cooldownMs: Long) = true
        override fun readCached(key: String): CachedPriceData? = null
        override fun write(key: String, data: CachedPriceData) {}
        override fun clear() {}
        override fun clearForZone(key: String) {}
        override fun cooldownRemainingMs(cooldownMs: Long) = 0L
        override fun resetCooldown() {}
    }

    private class FakeFetcher(private val prices: List<PriceSlot>? = null) : PriceFetcher {
        override fun fetchPrices(from: Instant, to: Instant, timeZoneId: ZoneId): FetchResult =
            FetchResult(prices ?: throw RuntimeException("Network error"), "Test")
    }

    private class FakeStatsCollector : StatsCollector {
        val records = mutableListOf<StatsRecord>()
        override fun record(record: StatsRecord) { records.add(record) }
        override fun readAll(): List<StatsRecord> = records.toList()
        override fun clear() { records.clear() }
    }

    /** Captures pushed stats; observe() callbacks are unused (tests call the handlers directly). */
    private class FakeWearSync : WearSync {
        val pushed = mutableListOf<ByteArray>()
        override fun observe(onAppliances: (String) -> Unit, onSettings: (WearSettings) -> Unit) {}
        override fun stop() {}
        override suspend fun pushStats(bytes: ByteArray) { pushed.add(bytes) }
    }

    /** Hourly price slots starting from the current hour. */
    private fun fakePrices(count: Int, basePrice: Double = 0.10): List<PriceSlot> {
        val base = ZonedDateTime.now().withMinute(0).withSecond(0).withNano(0)
        return (0 until count).map { i ->
            PriceSlot(time = base.plusHours(i.toLong()), price = basePrice + i * 0.01, durationMinutes = 60)
        }
    }

    private fun settings(
        country: String? = null, zoneId: String? = null, order: String? = null,
        disabled: String? = null, language: String? = null,
        stats: Boolean = false, expired: Boolean = false, unlocked: Boolean = false,
    ) = WearSettings(country, zoneId, order, disabled, language, stats, expired, unlocked)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        app = ApplicationProvider.getApplicationContext()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun testViewModel(
        fetcher: FakeFetcher,
        collector: StatsCollector = FakeStatsCollector(),
        sync: WearSync = FakeWearSync(),
    ) = WearViewModel(app, { _ -> fetcher }, FakeCache(), collector, testDispatcher, sync).also {
        testDispatcher.scheduler.advanceUntilIdle()
    }

    // --- Initial state ---

    @Test
    fun `initial state has empty appliance list`() {
        assertTrue(testViewModel(FakeFetcher(fakePrices(24))).uiState.value.appliances.isEmpty())
    }

    @Test
    fun `initial state is not loading, no error, no result, source order null, unlocked`() {
        val s = testViewModel(FakeFetcher(fakePrices(24))).uiState.value
        assertFalse(s.isLoading)
        assertNull(s.error)
        assertNull(s.result)
        assertNull(s.sourceOrder)
        assertFalse(s.isLocked)
    }

    // --- onApplianceTapped ---

    @Test
    fun `onApplianceTapped sets loading and label immediately`() {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        viewModel.onApplianceTapped(Appliance("1", "Washer", 2, 30, "laundry"))
        val state = viewModel.uiState.value
        assertTrue(state.isLoading)
        assertEquals(UiText.applianceLabel("Washer", 2, 30), state.resultLabel)
        assertNull(state.result)
        assertNull(state.error)
    }

    @Test
    fun `onApplianceTapped with prices produces a result`() = runTest {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        viewModel.onApplianceTapped(Appliance("1", "Washer", 2, 0, "laundry"))
        runCurrent()
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertNotNull(state.result)
        viewModel.onClearResult()
    }

    @Test
    fun `onApplianceTapped with network error sets error`() = runTest {
        val viewModel = testViewModel(FakeFetcher(prices = null))
        viewModel.onApplianceTapped(Appliance("1", "Dryer", 1, 0, "dryer"))
        runCurrent()
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertNull(state.result)
    }

    @Test
    fun `onApplianceTapped with empty prices sets no data error`() = runTest {
        val viewModel = testViewModel(FakeFetcher(emptyList()))
        viewModel.onApplianceTapped(Appliance("1", "Dryer", 1, 0, "dryer"))
        runCurrent()
        assertEquals(UiText.Res(R.string.wear_error_no_data), viewModel.uiState.value.error)
    }

    @Test
    fun `onApplianceTapped with insufficient prices sets not enough data error`() = runTest {
        val viewModel = testViewModel(FakeFetcher(fakePrices(2)))
        viewModel.onApplianceTapped(Appliance("1", "Dryer", 5, 0, "dryer"))
        runCurrent()
        val notEnough = viewModel.uiState.value.error
        assertTrue(notEnough is UiText.Res && notEnough.id == R.string.wear_error_not_enough_data)
    }

    @Test
    fun `onApplianceTapped with no resolved zone sets no-zone error`() {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        // A multi-zone country with no selection resolves to a null zone.
        val multi = Countries.all.first { it.zones.size > 1 }
        viewModel.onSettingsReceived(settings(country = multi.code))
        viewModel.onApplianceTapped(Appliance("1", "Washer", 1, 0, "laundry"))
        assertEquals(UiText.Res(R.string.wear_error_no_zone), viewModel.uiState.value.error)
    }

    @Test
    fun `rapid taps cancel previous fetch and keep last result`() = runTest {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        viewModel.onApplianceTapped(Appliance("1", "First", 1, 0, "electricity"))
        viewModel.onApplianceTapped(Appliance("2", "Second", 2, 0, "electricity"))
        runCurrent()
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(UiText.applianceLabel("Second", 2, 0), state.resultLabel)
        assertNotNull(state.result)
        viewModel.onClearResult()
    }

    // --- onClearResult ---

    @Test
    fun `onClearResult clears result label and error`() = runTest {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        viewModel.onApplianceTapped(Appliance("1", "Washer", 1, 0, "laundry"))
        runCurrent()
        viewModel.onClearResult()
        val state = viewModel.uiState.value
        assertNull(state.result)
        assertNull(state.resultLabel)
        assertNull(state.error)
    }

    // --- recalculateResult ---

    @Test
    fun `recalculateResult keeps a still-valid result`() = runTest {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        viewModel.onApplianceTapped(Appliance("1", "Washer", 2, 0, "laundry"))
        runCurrent()
        assertNotNull(viewModel.uiState.value.result)
        viewModel.recalculateResult() // prices still cover the future window
        assertNotNull(viewModel.uiState.value.result)
        viewModel.onClearResult()
    }

    @Test
    fun `recalculateResult is a no-op with no prior result`() {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        viewModel.recalculateResult()
        assertNull(viewModel.uiState.value.result)
    }

    // --- onAppliancesReceived (real JSON parsing) ---

    @Test
    fun `onAppliancesReceived with valid JSON populates appliances`() {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        viewModel.onAppliancesReceived("""[{"id":"1","name":"Washer","durationHours":2,"durationMinutes":30,"icon":"laundry"}]""")
        val a = viewModel.uiState.value.appliances
        assertEquals(1, a.size)
        assertEquals("Washer", a[0].name)
        assertEquals(2, a[0].durationHours)
        assertEquals(30, a[0].durationMinutes)
        assertEquals("laundry", a[0].icon)
    }

    @Test
    fun `onAppliancesReceived with empty array clears the list`() {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        viewModel.onAppliancesReceived("[]")
        assertTrue(viewModel.uiState.value.appliances.isEmpty())
    }

    @Test
    fun `onAppliancesReceived with malformed JSON yields empty list`() {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        viewModel.onAppliancesReceived("not json")
        assertTrue(viewModel.uiState.value.appliances.isEmpty())
    }

    @Test
    fun `onAppliancesReceived fills missing appliance fields with defaults`() {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        viewModel.onAppliancesReceived("""[{"id":"1","name":"Test"}]""")
        val a = viewModel.uiState.value.appliances
        assertEquals(1, a.size)
        assertEquals(1, a[0].durationHours)
        assertEquals(0, a[0].durationMinutes)
        assertEquals("electricity", a[0].icon)
    }

    // --- onSettingsReceived ---

    @Test
    fun `single-zone country resolves to its only zone`() {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        viewModel.onSettingsReceived(settings(country = "NL"))
        assertEquals(Countries.findPriceZoneById("NL"), viewModel.uiState.value.priceZone)
    }

    @Test
    fun `multi-zone country without a selection resolves to null`() {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        val multi = Countries.all.first { it.zones.size > 1 }
        viewModel.onSettingsReceived(settings(country = multi.code))
        assertNull(viewModel.uiState.value.priceZone)
    }

    @Test
    fun `an explicit price zone id resolves to that zone`() {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        val multi = Countries.all.first { it.zones.size > 1 }
        viewModel.onSettingsReceived(settings(zoneId = multi.zones[1].id))
        assertEquals(multi.zones[1], viewModel.uiState.value.priceZone)
    }

    @Test
    fun `an unknown country resolves to null`() {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        viewModel.onSettingsReceived(settings(country = "ZZ"))
        assertNull(viewModel.uiState.value.priceZone)
    }

    @Test
    fun `source order and disabled sources round-trip, blank means defaults`() {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        viewModel.onSettingsReceived(settings(country = "NL", order = """["energyzero","entsoe"]""", disabled = """["entsoe"]"""))
        assertEquals(listOf("energyzero", "entsoe"), viewModel.uiState.value.sourceOrder)
        assertEquals(setOf("entsoe"), viewModel.uiState.value.disabledSources)

        viewModel.onSettingsReceived(settings(country = "NL", order = "", disabled = ""))
        assertNull(viewModel.uiState.value.sourceOrder)
        assertTrue(viewModel.uiState.value.disabledSources.isEmpty())
    }

    @Test
    fun `malformed source order and disabled sources fall back to defaults`() {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        viewModel.onSettingsReceived(settings(country = "NL", order = "not json", disabled = "not json"))
        assertNull(viewModel.uiState.value.sourceOrder)
        assertTrue(viewModel.uiState.value.disabledSources.isEmpty())
    }

    @Test
    fun `isLocked is true only when the trial is expired and not unlocked`() {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        viewModel.onSettingsReceived(settings(expired = true, unlocked = false))
        assertTrue(viewModel.uiState.value.isLocked)
        viewModel.onSettingsReceived(settings(expired = true, unlocked = true))
        assertFalse(viewModel.uiState.value.isLocked)
        viewModel.onSettingsReceived(settings(expired = false, unlocked = false))
        assertFalse(viewModel.uiState.value.isLocked)
    }

    @Test
    fun `a language tag is applied without error`() {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        viewModel.onSettingsReceived(settings(country = "NL", language = "de"))
        // No crash; zone still resolved alongside the language.
        assertEquals(Countries.findPriceZoneById("NL"), viewModel.uiState.value.priceZone)
    }

    // --- Stats sync ---

    @Test
    fun `stats are pushed to the phone and cleared when enabled`() = runTest {
        val collector = FakeStatsCollector().apply {
            record(StatsRecord(1000L, "NL", "entsoe", "watch", true, "", 5))
        }
        val sync = FakeWearSync()
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)), collector, sync)
        viewModel.onSettingsReceived(settings(country = "NL", stats = true))
        viewModel.onApplianceTapped(Appliance("1", "Washer", 2, 0, "laundry"))
        runCurrent()
        assertEquals(1, sync.pushed.size)
        assertTrue(collector.readAll().isEmpty())
        viewModel.onClearResult()
    }

    @Test
    fun `stats are not pushed when disabled`() = runTest {
        val collector = FakeStatsCollector().apply {
            record(StatsRecord(1000L, "NL", "entsoe", "watch", true, "", 5))
        }
        val sync = FakeWearSync()
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)), collector, sync)
        viewModel.onApplianceTapped(Appliance("1", "Washer", 2, 0, "laundry")) // stats default off
        runCurrent()
        assertTrue(sync.pushed.isEmpty())
        assertEquals(1, collector.readAll().size)
        viewModel.onClearResult()
    }
}
