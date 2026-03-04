package si.merhar.sweetspot

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

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SweetSpotViewModelTest {

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

    /** Creates a ViewModel with default (real) dependencies for non-async tests. */
    private fun defaultViewModel() = SweetSpotViewModel(app)

    /** Creates a ViewModel with injected fakes and the test dispatcher. */
    private fun testViewModel(fetcher: FakeFetcher) =
        SweetSpotViewModel(app, PriceFetcherFactory { _ -> fetcher }, FakeCache(), testDispatcher)

    // --- Initial state ---

    @Test
    fun `initial state has default duration of 1h 0m`() {
        val viewModel = defaultViewModel()
        val state = viewModel.uiState.value
        assertEquals(1, state.durationHours)
        assertEquals(0, state.durationMinutes)
    }

    @Test
    fun `initial state is not loading`() {
        assertEquals(false, defaultViewModel().uiState.value.isLoading)
    }

    @Test
    fun `initial state has no error`() {
        assertNull(defaultViewModel().uiState.value.error)
    }

    @Test
    fun `initial state has no result`() {
        assertNull(defaultViewModel().uiState.value.result)
    }

    @Test
    fun `initial state has settings hidden`() {
        assertEquals(false, defaultViewModel().uiState.value.showSettings)
    }

    // --- Duration changes ---

    @Test
    fun `onDurationChanged updates hours and minutes`() {
        val viewModel = defaultViewModel()
        viewModel.onDurationChanged(3, 30)
        val state = viewModel.uiState.value
        assertEquals(3, state.durationHours)
        assertEquals(30, state.durationMinutes)
    }

    @Test
    fun `onDurationChanged to zero`() {
        val viewModel = defaultViewModel()
        viewModel.onDurationChanged(0, 0)
        val state = viewModel.uiState.value
        assertEquals(0, state.durationHours)
        assertEquals(0, state.durationMinutes)
    }

    // --- Quick duration ---

    @Test
    fun `onQuickDuration sets duration and result label`() {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        viewModel.onQuickDuration(2, 0)
        val state = viewModel.uiState.value
        assertEquals(2, state.durationHours)
        assertEquals(0, state.durationMinutes)
        assertEquals("2h", state.resultLabel)
    }

    @Test
    fun `onQuickDuration with minutes sets correct label`() {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        viewModel.onQuickDuration(1, 30)
        assertEquals("1h 30m", viewModel.uiState.value.resultLabel)
    }

    // --- Appliance duration ---

    @Test
    fun `onApplianceDuration sets duration and label`() {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        val appliance = Appliance(id = "1", name = "Washer", durationHours = 2, durationMinutes = 30, icon = "laundry")
        viewModel.onApplianceDuration(appliance)
        val state = viewModel.uiState.value
        assertEquals(2, state.durationHours)
        assertEquals(30, state.durationMinutes)
        assertEquals("Washer \u00b7 2h 30m", state.resultLabel)
    }

    // --- Validation ---

    @Test
    fun `onFindClicked with zero duration sets error`() {
        val viewModel = defaultViewModel()
        viewModel.onDurationChanged(0, 0)
        viewModel.onFindClicked()
        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertTrue(state.error is AppError.Validation)
        assertTrue(state.error!!.message.contains("duration greater than zero"))
        assertNull(state.result)
    }

    // --- Settings toggle ---

    @Test
    fun `onShowSettings sets showSettings to true`() {
        val viewModel = defaultViewModel()
        viewModel.onShowSettings()
        assertEquals(true, viewModel.uiState.value.showSettings)
    }

    @Test
    fun `onHideSettings sets showSettings to false`() {
        val viewModel = defaultViewModel()
        viewModel.onShowSettings()
        viewModel.onHideSettings()
        assertEquals(false, viewModel.uiState.value.showSettings)
    }

    // --- Clear result ---

    @Test
    fun `onClearResult clears result and related fields`() {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        viewModel.onQuickDuration(1, 0)
        viewModel.onClearResult()
        val state = viewModel.uiState.value
        assertNull(state.result)
        assertNull(state.resultLabel)
        assertNull(state.error)
        assertTrue(state.allPrices.isEmpty())
    }

    // --- Appliance CRUD ---

    @Test
    fun `onAddAppliance adds to list`() {
        val viewModel = defaultViewModel()
        viewModel.onAddAppliance("Dryer", 1, 30, "dryer")
        val appliances = viewModel.uiState.value.appliances
        assertEquals(1, appliances.size)
        assertEquals("Dryer", appliances[0].name)
        assertEquals(1, appliances[0].durationHours)
        assertEquals(30, appliances[0].durationMinutes)
        assertEquals("dryer", appliances[0].icon)
    }

    @Test
    fun `onAddAppliance generates unique IDs`() {
        val viewModel = defaultViewModel()
        viewModel.onAddAppliance("A", 1, 0, "bolt")
        viewModel.onAddAppliance("B", 2, 0, "bolt")
        val appliances = viewModel.uiState.value.appliances
        assertEquals(2, appliances.size)
        assertTrue(appliances[0].id != appliances[1].id)
    }

    @Test
    fun `onUpdateAppliance replaces matching appliance`() {
        val viewModel = defaultViewModel()
        viewModel.onAddAppliance("Old", 1, 0, "bolt")
        val added = viewModel.uiState.value.appliances[0]
        val updated = added.copy(name = "New", durationHours = 3)
        viewModel.onUpdateAppliance(updated)
        val appliances = viewModel.uiState.value.appliances
        assertEquals(1, appliances.size)
        assertEquals("New", appliances[0].name)
        assertEquals(3, appliances[0].durationHours)
    }

    @Test
    fun `onDeleteAppliance removes by ID`() {
        val viewModel = defaultViewModel()
        viewModel.onAddAppliance("A", 1, 0, "bolt")
        viewModel.onAddAppliance("B", 2, 0, "bolt")
        val idToDelete = viewModel.uiState.value.appliances[0].id
        viewModel.onDeleteAppliance(idToDelete)
        val appliances = viewModel.uiState.value.appliances
        assertEquals(1, appliances.size)
        assertEquals("B", appliances[0].name)
    }

    @Test
    fun `onDeleteAppliance with unknown ID does nothing`() {
        val viewModel = defaultViewModel()
        viewModel.onAddAppliance("A", 1, 0, "bolt")
        viewModel.onDeleteAppliance("nonexistent")
        assertEquals(1, viewModel.uiState.value.appliances.size)
    }

    // --- Timezone ---

    @Test
    fun `initial state uses default timezone`() {
        assertTrue(defaultViewModel().uiState.value.isUsingDefaultTimezone)
    }

    @Test
    fun `onTimezoneSelected with null reverts to default`() {
        val viewModel = defaultViewModel()
        viewModel.onTimezoneSelected(java.time.ZoneId.of("Asia/Tokyo"))
        assertEquals(false, viewModel.uiState.value.isUsingDefaultTimezone)
        viewModel.onTimezoneSelected(null)
        assertTrue(viewModel.uiState.value.isUsingDefaultTimezone)
    }

    @Test
    fun `onTimezoneSelected sets custom timezone`() {
        val viewModel = defaultViewModel()
        val tokyo = java.time.ZoneId.of("Asia/Tokyo")
        viewModel.onTimezoneSelected(tokyo)
        assertEquals(tokyo, viewModel.uiState.value.timeZoneId)
        assertEquals(false, viewModel.uiState.value.isUsingDefaultTimezone)
    }

    // --- Async fetch (coroutine) ---

    @Test
    fun `onFindClicked with prices produces a result`() = runTest {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        viewModel.onDurationChanged(2, 0)
        viewModel.onFindClicked()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertNotNull(state.result)
        assertTrue(state.allPrices.isNotEmpty())
    }

    @Test
    fun `onFindClicked with network error sets error message`() = runTest {
        val viewModel = testViewModel(FakeFetcher(prices = null))
        viewModel.onDurationChanged(1, 0)
        viewModel.onFindClicked()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.error is AppError.Network)
        assertTrue(state.error!!.message.contains("Could not fetch prices"))
        assertNull(state.result)
    }

    @Test
    fun `onFindClicked with empty prices sets no data error`() = runTest {
        val viewModel = testViewModel(FakeFetcher(emptyList()))
        viewModel.onDurationChanged(1, 0)
        viewModel.onFindClicked()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.error is AppError.Validation)
        assertTrue(state.error!!.message.contains("No price data"))
    }

    @Test
    fun `onFindClicked with insufficient prices sets not enough data error`() = runTest {
        val viewModel = testViewModel(FakeFetcher(fakePrices(2)))
        viewModel.onDurationChanged(5, 0)
        viewModel.onFindClicked()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.error is AppError.Validation)
        assertTrue(state.error!!.message.contains("Not enough price data"))
    }

    @Test
    fun `onFindClicked sets isLoading before coroutine completes`() = runTest {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        viewModel.onDurationChanged(1, 0)
        viewModel.onFindClicked()

        // Before advancing, isLoading should be true
        assertTrue(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.result)

        advanceUntilIdle()

        // After advancing, isLoading should be false
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `onQuickDuration triggers fetch and produces result`() = runTest {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        viewModel.onQuickDuration(1, 0)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.result)
        assertEquals("1h", state.resultLabel)
    }

    @Test
    fun `onApplianceDuration triggers fetch and produces result`() = runTest {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        val appliance = Appliance(id = "1", name = "Washer", durationHours = 2, durationMinutes = 0, icon = "laundry")
        viewModel.onApplianceDuration(appliance)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.result)
        assertEquals("Washer \u00b7 2h", state.resultLabel)
    }

    @Test
    fun `rapid onQuickDuration taps cancel previous fetch and keep last result`() = runTest {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        viewModel.onQuickDuration(1, 0)
        viewModel.onQuickDuration(3, 0)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("3h", state.resultLabel)
        assertNotNull(state.result)
    }
}
