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
import si.merhar.sweetspot.data.PriceCache
import si.merhar.sweetspot.data.PriceFetcher
import si.merhar.sweetspot.model.Appliance
import si.merhar.sweetspot.model.HourlyPrice
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
        override fun readCachedJson(): String? = null
        override fun write(json: String) {}
    }

    /** [PriceFetcher] that returns configurable prices or throws. */
    private class FakeFetcher(private val prices: List<HourlyPrice>? = null) : PriceFetcher {
        override fun fetchRawJson(zoneId: ZoneId): String = """{"Prices":[]}"""
        override fun parseJson(rawJson: String, zoneId: ZoneId): List<HourlyPrice> {
            return prices ?: throw RuntimeException("Network error")
        }
    }

    /** Generates hourly prices starting from the current hour. */
    private fun fakePrices(count: Int, basePrice: Double = 0.10): List<HourlyPrice> {
        val base = ZonedDateTime.now().withMinute(0).withSecond(0).withNano(0)
        return (0 until count).map { i ->
            HourlyPrice(
                time = base.plusHours(i.toLong()),
                price = basePrice + i * 0.01
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
        WearViewModel(app, fetcher, FakeCache(), testDispatcher).also {
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
}
