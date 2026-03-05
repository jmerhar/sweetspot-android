package si.merhar.sweetspot.wear

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
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
import kotlinx.serialization.json.Json
import si.merhar.sweetspot.data.api.FetchResult
import si.merhar.sweetspot.data.api.PriceFetcher
import si.merhar.sweetspot.data.api.PriceFetcherFactory
import si.merhar.sweetspot.data.cache.CachedPriceData
import si.merhar.sweetspot.data.cache.PriceCache
import si.merhar.sweetspot.model.Appliance
import si.merhar.sweetspot.model.PriceSlot
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Tests for [WearViewModel] state management and async price fetching.
 *
 * Uses injected fakes for [PriceFetcher] and [PriceCache], and a test dispatcher
 * so coroutines complete deterministically via [advanceUntilIdle].
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class WearViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var app: Application

    /** In-memory [PriceCache] that never triggers re-fetch. */
    private class FakeCache : PriceCache {
        override fun isCooldownElapsed(cooldownMs: Long) = true
        override fun readCached(key: String): CachedPriceData? = null
        override fun write(key: String, data: CachedPriceData) {}
    }

    /** [PriceFetcher] that returns configurable prices or throws. */
    private class FakeFetcher(private val prices: List<PriceSlot>? = null) : PriceFetcher {
        override fun fetchPrices(from: Instant, to: Instant, timeZoneId: ZoneId): FetchResult {
            return FetchResult(prices ?: throw RuntimeException("Network error"), "Test")
        }
    }

    /** Generates hourly price slots starting from the current hour. */
    private fun fakePrices(count: Int, basePrice: Double = 0.10): List<PriceSlot> {
        val base = ZonedDateTime.now().withMinute(0).withSecond(0).withNano(0)
        return (0 until count).map { i ->
            PriceSlot(
                time = base.plusHours(i.toLong()),
                price = basePrice + i * 0.01,
                durationMinutes = 60
            )
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        app = ApplicationProvider.getApplicationContext()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** Creates a WearViewModel with injected fakes and the test dispatcher. */
    private fun testViewModel(fetcher: FakeFetcher) =
        WearViewModel(app, PriceFetcherFactory { _ -> fetcher }, FakeCache(), testDispatcher).also {
            // Advance past the init block's loadAppliancesFromDataLayer coroutine
            testDispatcher.scheduler.advanceUntilIdle()
        }

    // --- Initial state ---

    @Test
    fun `initial state has empty appliance list`() {
        assertTrue(testViewModel(FakeFetcher(fakePrices(24))).uiState.value.appliances.isEmpty())
    }

    @Test
    fun `initial state is not loading`() {
        assertFalse(testViewModel(FakeFetcher(fakePrices(24))).uiState.value.isLoading)
    }

    @Test
    fun `initial state has no error`() {
        assertNull(testViewModel(FakeFetcher(fakePrices(24))).uiState.value.error)
    }

    @Test
    fun `initial state has no result`() {
        assertNull(testViewModel(FakeFetcher(fakePrices(24))).uiState.value.result)
    }

    // --- onApplianceTapped ---

    @Test
    fun `onApplianceTapped sets loading and label immediately`() {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        val appliance = Appliance(id = "1", name = "Washer", durationHours = 2, durationMinutes = 30, icon = "laundry")
        viewModel.onApplianceTapped(appliance)

        val state = viewModel.uiState.value
        assertTrue(state.isLoading)
        assertEquals("Washer \u00b7 2h 30m", state.resultLabel)
        assertNull(state.result)
        assertNull(state.error)
    }

    @Test
    fun `onApplianceTapped with prices produces a result`() = runTest {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        val appliance = Appliance(id = "1", name = "Washer", durationHours = 2, durationMinutes = 0, icon = "laundry")
        viewModel.onApplianceTapped(appliance)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertNotNull(state.result)
    }

    @Test
    fun `onApplianceTapped with network error sets error`() = runTest {
        val viewModel = testViewModel(FakeFetcher(prices = null))
        val appliance = Appliance(id = "1", name = "Dryer", durationHours = 1, durationMinutes = 0, icon = "dryer")
        viewModel.onApplianceTapped(appliance)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertNull(state.result)
    }

    @Test
    fun `onApplianceTapped with empty prices sets no data error`() = runTest {
        val viewModel = testViewModel(FakeFetcher(emptyList()))
        val appliance = Appliance(id = "1", name = "Dryer", durationHours = 1, durationMinutes = 0, icon = "dryer")
        viewModel.onApplianceTapped(appliance)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("No price data"))
    }

    @Test
    fun `onApplianceTapped with insufficient prices sets not enough data error`() = runTest {
        val viewModel = testViewModel(FakeFetcher(fakePrices(2)))
        val appliance = Appliance(id = "1", name = "Dryer", durationHours = 5, durationMinutes = 0, icon = "dryer")
        viewModel.onApplianceTapped(appliance)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Not enough data"))
    }

    @Test
    fun `rapid taps cancel previous fetch and keep last result`() = runTest {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        val first = Appliance(id = "1", name = "First", durationHours = 1, durationMinutes = 0, icon = "bolt")
        val second = Appliance(id = "2", name = "Second", durationHours = 2, durationMinutes = 0, icon = "bolt")

        viewModel.onApplianceTapped(first)
        viewModel.onApplianceTapped(second)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Second \u00b7 2h", state.resultLabel)
        assertNotNull(state.result)
    }

    // --- onClearResult ---

    @Test
    fun `onClearResult clears result label and error`() = runTest {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        val appliance = Appliance(id = "1", name = "Washer", durationHours = 1, durationMinutes = 0, icon = "laundry")
        viewModel.onApplianceTapped(appliance)
        advanceUntilIdle()

        viewModel.onClearResult()

        val state = viewModel.uiState.value
        assertNull(state.result)
        assertNull(state.resultLabel)
        assertNull(state.error)
    }

    // --- parseAppliances (JSON parsing behavior) ---

    /**
     * Mirrors [WearViewModel.parseAppliances] to test the JSON parsing contract:
     * valid JSON returns appliances, invalid JSON returns an empty list.
     */
    private fun parseAppliances(json: String): List<Appliance> {
        return try {
            Json.decodeFromString<List<Appliance>>(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @Test
    fun `parseAppliances with valid JSON returns appliance list`() {
        val json = """[{"id":"1","name":"Washer","durationHours":2,"durationMinutes":30,"icon":"laundry"}]"""
        val result = parseAppliances(json)
        assertEquals(1, result.size)
        assertEquals("Washer", result[0].name)
        assertEquals(2, result[0].durationHours)
        assertEquals(30, result[0].durationMinutes)
        assertEquals("laundry", result[0].icon)
    }

    @Test
    fun `parseAppliances with empty array returns empty list`() {
        assertEquals(emptyList<Appliance>(), parseAppliances("[]"))
    }

    @Test
    fun `parseAppliances with malformed JSON returns empty list`() {
        assertEquals(emptyList<Appliance>(), parseAppliances("not json"))
    }

    @Test
    fun `parseAppliances with missing fields uses defaults`() {
        val json = """[{"id":"1","name":"Test"}]"""
        val result = parseAppliances(json)
        assertEquals(1, result.size)
        assertEquals(1, result[0].durationHours)
        assertEquals(0, result[0].durationMinutes)
        assertEquals("bolt", result[0].icon)
    }

    // --- Source order ---

    @Test
    fun `initial state has null source order`() {
        assertNull(testViewModel(FakeFetcher(fakePrices(24))).uiState.value.sourceOrder)
    }
}
