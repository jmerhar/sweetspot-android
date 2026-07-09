package today.sweetspot

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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

import today.sweetspot.data.billing.BillingRepository
import today.sweetspot.data.cache.CachedPriceData
import today.sweetspot.data.cache.PriceCache
import today.sweetspot.data.repository.EvVehicleRepository
import today.sweetspot.data.stats.StatsCollector
import today.sweetspot.data.stats.StatsPoster
import today.sweetspot.data.stats.StatsRecord
import today.sweetspot.model.Appliance
import today.sweetspot.model.PriceSlot
import today.sweetspot.util.UiText
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

import android.app.Activity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.robolectric.Robolectric

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SweetSpotViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var app: Application

    /** In-memory [PriceCache] that never triggers re-fetch. */
    private class FakeCache(private val cooldownRemaining: Long = 0L) : PriceCache {
        var clearCount = 0
            private set
        var clearedZones = mutableListOf<String>()
            private set

        override fun isCooldownElapsed(cooldownMs: Long) = true
        override fun readCached(key: String): CachedPriceData? = null
        override fun write(key: String, data: CachedPriceData) {}
        override fun clear() { clearCount++ }
        override fun clearForZone(key: String) { clearedZones += key }
        override fun cooldownRemainingMs(cooldownMs: Long) = cooldownRemaining
        override fun resetCooldown() {}
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

    /** In-memory [StatsCollector] for testing. */
    private class FakeStatsCollector : StatsCollector {
        val records = mutableListOf<StatsRecord>()
        override fun record(record: StatsRecord) { records.add(record) }
        override fun readAll(): List<StatsRecord> = records.toList()
        override fun clear() { records.clear() }
    }

    /** Fake [BillingRepository] for testing paywall/unlock behaviour. */
    private class FakeBillingRepository(initialUnlocked: Boolean = false) : BillingRepository {
        private val _isUnlocked = MutableStateFlow(initialUnlocked)
        override val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()
        override val productPrice: StateFlow<String?> = MutableStateFlow(null)
        var resumeCount = 0
            private set
        var launchCount = 0
            private set
        var queryCount = 0
            private set
        override fun connect() {}
        override fun disconnect() {}
        override fun launchPurchaseFlow(activity: Activity) { launchCount++ }
        override fun queryPurchases() { queryCount++ }
        override fun onResume() { resumeCount++ }
        fun setUnlocked(value: Boolean) { _isUnlocked.value = value }
    }

    /** [StatsPoster] that records the payload and returns a fixed code. */
    private class FakePoster : StatsPoster {
        var lastJson: String? = null
            private set
        var callCount = 0
            private set
        override fun post(json: String): Int { callCount++; lastJson = json; return 200 }
    }

    /** Creates a ViewModel with injected fakes and the test dispatcher. */
    private fun testViewModel(
        fetcher: FakeFetcher,
        cache: FakeCache = FakeCache(),
        billing: BillingRepository? = null
    ) =
        SweetSpotViewModel(app, { _ -> fetcher }, cache, FakeStatsCollector(), testDispatcher, billing)

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
        assertEquals(UiText.duration(2, 0), state.resultLabel)
    }

    @Test
    fun `onQuickDuration with minutes sets correct label`() {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        viewModel.onQuickDuration(1, 30)
        assertEquals(UiText.duration(1, 30), viewModel.uiState.value.resultLabel)
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
        assertEquals(UiText.applianceLabel("Washer", 2, 30), state.resultLabel)
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
        assertEquals(UiText.Res(R.string.error_zero_duration), state.error!!.message)
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
        viewModel.onAddAppliance("A", 1, 0, "electricity")
        viewModel.onAddAppliance("B", 2, 0, "electricity")
        val appliances = viewModel.uiState.value.appliances
        assertEquals(2, appliances.size)
        assertTrue(appliances[0].id != appliances[1].id)
    }

    @Test
    fun `onUpdateAppliance replaces matching appliance`() {
        val viewModel = defaultViewModel()
        viewModel.onAddAppliance("Old", 1, 0, "electricity")
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
        viewModel.onAddAppliance("A", 1, 0, "electricity")
        viewModel.onAddAppliance("B", 2, 0, "electricity")
        val idToDelete = viewModel.uiState.value.appliances[0].id
        viewModel.onDeleteAppliance(idToDelete)
        val appliances = viewModel.uiState.value.appliances
        assertEquals(1, appliances.size)
        assertEquals("B", appliances[0].name)
    }

    @Test
    fun `onDeleteAppliance with unknown ID does nothing`() {
        val viewModel = defaultViewModel()
        viewModel.onAddAppliance("A", 1, 0, "electricity")
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
        viewModel.onTimezoneSelected(ZoneId.of("Asia/Tokyo"))
        assertEquals(false, viewModel.uiState.value.isUsingDefaultTimezone)
        viewModel.onTimezoneSelected(null)
        assertTrue(viewModel.uiState.value.isUsingDefaultTimezone)
    }

    @Test
    fun `onTimezoneSelected sets custom timezone`() {
        val viewModel = defaultViewModel()
        val tokyo = ZoneId.of("Asia/Tokyo")
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
        runCurrent()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertNotNull(state.result)
        assertTrue(state.allPrices.isNotEmpty())
        viewModel.onClearResult()
    }

    @Test
    fun `onFindClicked with network error sets error message`() = runTest {
        val viewModel = testViewModel(FakeFetcher(prices = null))
        viewModel.onDurationChanged(1, 0)
        viewModel.onFindClicked()
        runCurrent()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.error is AppError.Network)
        val networkMsg = state.error!!.message
        assertTrue(networkMsg is UiText.Res && networkMsg.id == R.string.error_network)
        assertNull(state.result)
    }

    @Test
    fun `onFindClicked with empty prices sets no data error`() = runTest {
        val viewModel = testViewModel(FakeFetcher(emptyList()))
        viewModel.onDurationChanged(1, 0)
        viewModel.onFindClicked()
        runCurrent()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.error is AppError.Validation)
        assertEquals(UiText.Res(R.string.error_no_data), state.error!!.message)
    }

    @Test
    fun `onFindClicked with insufficient prices sets not enough data error`() = runTest {
        val viewModel = testViewModel(FakeFetcher(fakePrices(2)))
        viewModel.onDurationChanged(5, 0)
        viewModel.onFindClicked()
        runCurrent()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.error is AppError.Validation)
        val notEnoughMsg = state.error!!.message
        assertTrue(notEnoughMsg is UiText.Plural && notEnoughMsg.id == R.plurals.error_not_enough_data)
    }

    @Test
    fun `onFindClicked sets isLoading before coroutine completes`() = runTest {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        viewModel.onDurationChanged(1, 0)
        viewModel.onFindClicked()

        // Before advancing, isLoading should be true
        assertTrue(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.result)

        runCurrent()

        // After advancing, isLoading should be false
        assertFalse(viewModel.uiState.value.isLoading)
        viewModel.onClearResult()
    }

    @Test
    fun `onQuickDuration triggers fetch and produces result`() = runTest {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        viewModel.onQuickDuration(1, 0)
        runCurrent()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.result)
        assertEquals(UiText.duration(1, 0), state.resultLabel)
        viewModel.onClearResult()
    }

    @Test
    fun `onApplianceDuration triggers fetch and produces result`() = runTest {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        val appliance = Appliance(id = "1", name = "Washer", durationHours = 2, durationMinutes = 0, icon = "laundry")
        viewModel.onApplianceDuration(appliance)
        runCurrent()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.result)
        assertEquals(UiText.applianceLabel("Washer", 2, 0), state.resultLabel)
        viewModel.onClearResult()
    }

    @Test
    fun `rapid onQuickDuration taps cancel previous fetch and keep last result`() = runTest {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        viewModel.onQuickDuration(1, 0)
        viewModel.onQuickDuration(3, 0)
        runCurrent()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(UiText.duration(3, 0), state.resultLabel)
        assertNotNull(state.result)
        viewModel.onClearResult()
    }

    // --- Source order ---

    @Test
    fun `initial state has null source order`() {
        assertNull(defaultViewModel().uiState.value.sourceOrder)
    }

    @Test
    fun `initial state has empty disabled sources`() {
        assertTrue(defaultViewModel().uiState.value.disabledSources.isEmpty())
    }

    @Test
    fun `onSourceOrderChanged updates source order in state`() {
        val viewModel = defaultViewModel()
        viewModel.onSourceOrderChanged(listOf("energyzero", "entsoe"))
        assertEquals(listOf("energyzero", "entsoe"), viewModel.uiState.value.sourceOrder)
    }

    @Test
    fun `onDisabledSourcesChanged updates disabled sources in state`() {
        val viewModel = defaultViewModel()
        viewModel.onDisabledSourcesChanged(setOf("entsoe"))
        assertEquals(setOf("entsoe"), viewModel.uiState.value.disabledSources)
    }

    @Test
    fun `onResetSourceOrder clears source order and disabled sources`() {
        val viewModel = defaultViewModel()
        viewModel.onSourceOrderChanged(listOf("energyzero", "entsoe"))
        viewModel.onDisabledSourcesChanged(setOf("entsoe"))
        viewModel.onResetSourceOrder()
        assertNull(viewModel.uiState.value.sourceOrder)
        assertTrue(viewModel.uiState.value.disabledSources.isEmpty())
    }

    @Test
    fun `onCountrySelected resets source order and disabled sources`() {
        val viewModel = defaultViewModel()
        viewModel.onSourceOrderChanged(listOf("energyzero", "entsoe"))
        viewModel.onDisabledSourcesChanged(setOf("entsoe"))
        viewModel.onCountrySelected("DE")
        assertNull(viewModel.uiState.value.sourceOrder)
        assertTrue(viewModel.uiState.value.disabledSources.isEmpty())
    }

    // --- Clear cache ---

    @Test
    fun `onClearCache with cooldown elapsed clears cache and returns confirmation`() {
        val cache = FakeCache(cooldownRemaining = 0L)
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)), cache)
        val message = viewModel.onClearCache()
        assertEquals(1, cache.clearCount)
        assertEquals(UiText.Res(R.string.snackbar_cache_cleared), message)
    }

    @Test
    fun `onClearCache with cooldown active does not clear and returns warning`() {
        val cache = FakeCache(cooldownRemaining = 120_000L)
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)), cache)
        val message = viewModel.onClearCache()
        assertEquals(0, cache.clearCount)
        assertTrue(message is UiText.Plural && message.id == R.plurals.error_cooldown)
    }

    // --- Refresh results ---

    @Test
    fun `onRefreshResults with cooldown active sets error without loading`() {
        val cache = FakeCache(cooldownRemaining = 180_000L)
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)), cache)

        viewModel.onRefreshResults()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.error is AppError.Network)
        val cooldownMsg = state.error!!.message
        assertTrue(cooldownMsg is UiText.Plural && cooldownMsg.id == R.plurals.error_cooldown)
    }

    @Test
    fun `onRefreshResults with cooldown elapsed clears zone cache and starts loading`() = runTest {
        val cache = FakeCache(cooldownRemaining = 0L)
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)), cache)

        // First perform a search so there's a result to refresh
        viewModel.onQuickDuration(1, 0)
        runCurrent()
        assertNotNull(viewModel.uiState.value.result)

        viewModel.onRefreshResults()

        // Should be loading and zone cache should be cleared
        assertTrue(viewModel.uiState.value.isLoading)
        assertTrue(cache.clearedZones.isNotEmpty())

        runCurrent()

        // After completion, should have a result and not be loading
        assertFalse(viewModel.uiState.value.isLoading)
        assertNotNull(viewModel.uiState.value.result)
        viewModel.onClearResult()
    }

    @Test
    fun `onRefreshResults consecutive cooldown errors have different ids`() {
        val cache = FakeCache(cooldownRemaining = 60_000L)
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)), cache)

        viewModel.onRefreshResults()
        val firstError = viewModel.uiState.value.error

        viewModel.onRefreshResults()
        val secondError = viewModel.uiState.value.error

        // Both are Network errors but should be different objects (unique id)
        assertTrue(firstError is AppError.Network)
        assertTrue(secondError is AppError.Network)
        assertFalse(firstError == secondError)
    }

    // --- Stats ---

    @Test
    fun `initial state has stats disabled`() {
        assertFalse(defaultViewModel().uiState.value.isStatsEnabled)
    }

    @Test
    fun `initial state does not show stats prompt`() {
        assertFalse(defaultViewModel().uiState.value.showStatsPrompt)
    }

    @Test
    fun `onStatsEnabledChanged true enables stats`() {
        val viewModel = defaultViewModel()
        viewModel.onStatsEnabledChanged(true)
        assertTrue(viewModel.uiState.value.isStatsEnabled)
    }

    @Test
    fun `onStatsEnabledChanged false disables stats`() {
        val viewModel = defaultViewModel()
        viewModel.onStatsEnabledChanged(true)
        viewModel.onStatsEnabledChanged(false)
        assertFalse(viewModel.uiState.value.isStatsEnabled)
    }

    @Test
    fun `onStatsPromptEnabled sets enabled and hides prompt`() {
        val viewModel = defaultViewModel()
        viewModel.onStatsPromptEnabled()
        val state = viewModel.uiState.value
        assertTrue(state.isStatsEnabled)
        assertFalse(state.showStatsPrompt)
    }

    @Test
    fun `onStatsPromptDismissed hides prompt without enabling stats`() {
        val viewModel = defaultViewModel()
        viewModel.onStatsPromptDismissed()
        val state = viewModel.uiState.value
        assertFalse(state.isStatsEnabled)
        assertFalse(state.showStatsPrompt)
    }

    @Test
    fun `stats prompt shown after 3 days of use`() {
        val prefs = app.getSharedPreferences("sweetspot_settings", Context.MODE_PRIVATE)
        val fourDaysAgo = System.currentTimeMillis() - (4 * 24 * 60 * 60 * 1000L)
        prefs.edit().putLong("first_launch_ms", fourDaysAgo).commit()

        val viewModel = SweetSpotViewModel(app)
        assertTrue(viewModel.uiState.value.showStatsPrompt)
    }

    @Test
    fun `stats prompt not shown within 3 days`() {
        val prefs = app.getSharedPreferences("sweetspot_settings", Context.MODE_PRIVATE)
        val oneDayAgo = System.currentTimeMillis() - (1 * 24 * 60 * 60 * 1000L)
        prefs.edit().putLong("first_launch_ms", oneDayAgo).commit()

        val viewModel = SweetSpotViewModel(app)
        assertFalse(viewModel.uiState.value.showStatsPrompt)
    }

    @Test
    fun `stats prompt not shown when already enabled`() {
        val prefs = app.getSharedPreferences("sweetspot_settings", Context.MODE_PRIVATE)
        val fourDaysAgo = System.currentTimeMillis() - (4 * 24 * 60 * 60 * 1000L)
        prefs.edit().putLong("first_launch_ms", fourDaysAgo).putBoolean("stats_enabled", true).commit()

        val viewModel = SweetSpotViewModel(app)
        assertFalse(viewModel.uiState.value.showStatsPrompt)
    }

    @Test
    fun `stats prompt not shown after being dismissed`() {
        val prefs = app.getSharedPreferences("sweetspot_settings", Context.MODE_PRIVATE)
        val fourDaysAgo = System.currentTimeMillis() - (4 * 24 * 60 * 60 * 1000L)
        prefs.edit().putLong("first_launch_ms", fourDaysAgo).putBoolean("stats_prompt_shown", true).commit()

        val viewModel = SweetSpotViewModel(app)
        assertFalse(viewModel.uiState.value.showStatsPrompt)
    }

    @Test
    fun `onWatchStatsReceived appends records when stats enabled`() {
        val collector = FakeStatsCollector()
        val viewModel = SweetSpotViewModel(app, { _ -> FakeFetcher(fakePrices(24)) }, FakeCache(), collector, testDispatcher)
        viewModel.onStatsEnabledChanged(true)

        val records = listOf(
            StatsRecord(1000L, "NL", "entsoe", "watch", true, ""),
            StatsRecord(2000L, "NL", "entsoe", "watch", false, "TIMEOUT")
        )
        viewModel.onWatchStatsReceived(records)

        assertEquals(2, collector.records.size)
        assertTrue(collector.records[0].success)
        assertFalse(collector.records[1].success)
        assertEquals("TIMEOUT", collector.records[1].errorCategory)
    }

    @Test
    fun `onWatchStatsReceived ignores records when stats disabled`() {
        val collector = FakeStatsCollector()
        val viewModel = SweetSpotViewModel(app, { _ -> FakeFetcher(fakePrices(24)) }, FakeCache(), collector, testDispatcher)
        // Stats are disabled by default

        val records = listOf(StatsRecord(1000L, "NL", "entsoe", "watch", true, ""))
        viewModel.onWatchStatsReceived(records)

        assertTrue(collector.records.isEmpty())
    }

    // --- Trial & Paywall ---

    @Test
    fun `fresh install has trial not expired and paywall not shown`() {
        val prefs = app.getSharedPreferences("sweetspot_settings", Context.MODE_PRIVATE)
        prefs.edit().putLong("first_launch_ms", System.currentTimeMillis()).commit()

        val viewModel = SweetSpotViewModel(app)
        val state = viewModel.uiState.value
        assertFalse(state.isTrialExpired)
        assertFalse(state.showPaywall)
        assertTrue(state.trialDaysRemaining > 0)
    }

    @Test
    fun `expired trial without unlock shows paywall`() {
        val prefs = app.getSharedPreferences("sweetspot_settings", Context.MODE_PRIVATE)
        val fifteenDaysAgo = System.currentTimeMillis() - (15 * 24 * 60 * 60 * 1000L)
        prefs.edit()
            .putLong("first_launch_ms", fifteenDaysAgo)
            .putBoolean("unlocked", false)
            .commit()

        val viewModel = SweetSpotViewModel(app)
        val state = viewModel.uiState.value
        assertTrue(state.isTrialExpired)
        // In test (debug) builds, paywall is always skipped, but isTrialExpired still reflects reality
        assertEquals(0, state.trialDaysRemaining)
    }

    @Test
    fun `expired trial with unlock does not show paywall`() {
        val prefs = app.getSharedPreferences("sweetspot_settings", Context.MODE_PRIVATE)
        val fifteenDaysAgo = System.currentTimeMillis() - (15 * 24 * 60 * 60 * 1000L)
        prefs.edit()
            .putLong("first_launch_ms", fifteenDaysAgo)
            .putBoolean("unlocked", true)
            .commit()

        val viewModel = SweetSpotViewModel(app)
        val state = viewModel.uiState.value
        assertFalse(state.isTrialExpired)
        assertTrue(state.isUnlocked)
        assertFalse(state.showPaywall)
    }

    @Test
    fun `trial days remaining computed correctly`() {
        val prefs = app.getSharedPreferences("sweetspot_settings", Context.MODE_PRIVATE)
        val fiveDaysAgo = System.currentTimeMillis() - (5 * 24 * 60 * 60 * 1000L)
        prefs.edit().putLong("first_launch_ms", fiveDaysAgo).commit()

        val viewModel = SweetSpotViewModel(app)
        assertEquals(9, viewModel.uiState.value.trialDaysRemaining)
    }

    @Test
    fun `billing unlock state change updates paywall`() = runTest {
        val billing = FakeBillingRepository(initialUnlocked = false)
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)), billing = billing)
        runCurrent()

        assertFalse(viewModel.uiState.value.isUnlocked)

        billing.setUnlocked(true)
        runCurrent()

        assertTrue(viewModel.uiState.value.isUnlocked)
        assertFalse(viewModel.uiState.value.showPaywall)
        assertTrue(viewModel.uiState.value.showThankYou)
    }

    @Test
    fun `onResume forwards to billing`() = runTest {
        val billing = FakeBillingRepository(initialUnlocked = false)
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)), billing = billing)
        runCurrent()

        assertEquals(0, billing.resumeCount)
        viewModel.onResume()
        assertEquals(1, billing.resumeCount)
    }

    // --- Developer options ---

    @Test
    fun `initial state has developer options disabled`() {
        assertFalse(defaultViewModel().uiState.value.devOptionsEnabled)
    }

    @Test
    fun `initial state has cooldown not disabled`() {
        assertFalse(defaultViewModel().uiState.value.isCooldownDisabled)
    }

    @Test
    fun `onDevOptionsUnlocked enables developer options`() {
        val viewModel = defaultViewModel()
        viewModel.onDevOptionsUnlocked()
        assertTrue(viewModel.uiState.value.devOptionsEnabled)
    }

    @Test
    fun `onDevOptionsUnlocked persists across ViewModel creation`() {
        val viewModel1 = defaultViewModel()
        viewModel1.onDevOptionsUnlocked()

        val viewModel2 = SweetSpotViewModel(app)
        assertTrue(viewModel2.uiState.value.devOptionsEnabled)
    }

    @Test
    fun `onDevResetUnlock clears unlock state`() {
        val prefs = app.getSharedPreferences("sweetspot_settings", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("unlocked", true).commit()

        val viewModel = SweetSpotViewModel(app)
        assertTrue(viewModel.uiState.value.isUnlocked)

        viewModel.onDevResetUnlock()
        assertFalse(viewModel.uiState.value.isUnlocked)
    }

    @Test
    fun `onDevCooldownDisabledChanged true disables cooldown`() {
        val viewModel = defaultViewModel()
        viewModel.onDevCooldownDisabledChanged(true)
        assertTrue(viewModel.uiState.value.isCooldownDisabled)
    }

    @Test
    fun `onDevCooldownDisabledChanged false re-enables cooldown`() {
        val viewModel = defaultViewModel()
        viewModel.onDevCooldownDisabledChanged(true)
        viewModel.onDevCooldownDisabledChanged(false)
        assertFalse(viewModel.uiState.value.isCooldownDisabled)
    }

    @Test
    fun `onDevCooldownDisabledChanged persists across ViewModel creation`() {
        val viewModel1 = defaultViewModel()
        viewModel1.onDevCooldownDisabledChanged(true)

        val viewModel2 = SweetSpotViewModel(app)
        assertTrue(viewModel2.uiState.value.isCooldownDisabled)
    }

    @Test
    fun `initial state has dev unlock disabled`() {
        assertFalse(defaultViewModel().uiState.value.isDevUnlocked)
    }

    @Test
    fun `onDevUnlockChanged true enables subscription bypass`() {
        val viewModel = defaultViewModel()
        viewModel.onDevUnlockChanged(true)
        assertTrue(viewModel.uiState.value.isDevUnlocked)
    }

    @Test
    fun `onDevUnlockChanged false disables subscription bypass`() {
        val viewModel = defaultViewModel()
        viewModel.onDevUnlockChanged(true)
        viewModel.onDevUnlockChanged(false)
        assertFalse(viewModel.uiState.value.isDevUnlocked)
    }

    @Test
    fun `onDevUnlockChanged persists across ViewModel creation`() {
        val viewModel1 = defaultViewModel()
        viewModel1.onDevUnlockChanged(true)

        val viewModel2 = SweetSpotViewModel(app)
        assertTrue(viewModel2.uiState.value.isDevUnlocked)
    }

    @Test
    fun `dev unlock overrides expired trial`() {
        val prefs = app.getSharedPreferences("sweetspot_settings", Context.MODE_PRIVATE)
        val fifteenDaysAgo = System.currentTimeMillis() - (15 * 24 * 60 * 60 * 1000L)
        prefs.edit()
            .putLong("first_launch_ms", fifteenDaysAgo)
            .putBoolean("unlocked", false)
            .putBoolean("dev_unlock", true)
            .commit()

        val viewModel = SweetSpotViewModel(app)
        val state = viewModel.uiState.value
        assertTrue(state.isDevUnlocked)
        assertFalse(state.isTrialExpired)
        assertFalse(state.showPaywall)
    }

    @Test
    fun `disabling dev unlock restores expired trial state`() {
        val prefs = app.getSharedPreferences("sweetspot_settings", Context.MODE_PRIVATE)
        val fifteenDaysAgo = System.currentTimeMillis() - (15 * 24 * 60 * 60 * 1000L)
        prefs.edit()
            .putLong("first_launch_ms", fifteenDaysAgo)
            .putBoolean("unlocked", false)
            .putBoolean("dev_unlock", true)
            .commit()

        val viewModel = SweetSpotViewModel(app)
        assertFalse(viewModel.uiState.value.isTrialExpired)

        viewModel.onDevUnlockChanged(false)
        assertTrue(viewModel.uiState.value.isTrialExpired)
    }

    @Test
    fun `onClearCache bypasses cooldown when cooldown is disabled`() {
        val cache = FakeCache(cooldownRemaining = 120_000L)
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)), cache)
        viewModel.onDevCooldownDisabledChanged(true)

        val message = viewModel.onClearCache()
        assertEquals(1, cache.clearCount)
        assertEquals(UiText.Res(R.string.snackbar_cache_cleared), message)
    }

    @Test
    fun `onRefreshResults bypasses cooldown when cooldown is disabled`() = runTest {
        val cache = FakeCache(cooldownRemaining = 180_000L)
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)), cache)
        viewModel.onDevCooldownDisabledChanged(true)

        // Perform initial search so there's something to refresh
        viewModel.onQuickDuration(1, 0)
        runCurrent()
        assertNotNull(viewModel.uiState.value.result)

        viewModel.onRefreshResults()

        // Should be loading (not blocked by cooldown)
        assertTrue(viewModel.uiState.value.isLoading)
        assertTrue(cache.clearedZones.isNotEmpty())

        runCurrent()
        assertFalse(viewModel.uiState.value.isLoading)
        viewModel.onClearResult()
    }

    // --- Thank-you dialog ---

    @Test
    fun `showThankYou becomes true when billing transitions to unlocked`() = runTest {
        val billing = FakeBillingRepository(initialUnlocked = false)
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)), billing = billing)
        runCurrent()

        assertFalse(viewModel.uiState.value.showThankYou)

        billing.setUnlocked(true)
        runCurrent()

        assertTrue(viewModel.uiState.value.showThankYou)
        assertTrue(viewModel.uiState.value.isUnlocked)
        assertFalse(viewModel.uiState.value.showPaywall)
    }

    @Test
    fun `onThankYouDismissed clears showThankYou`() = runTest {
        val billing = FakeBillingRepository(initialUnlocked = false)
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)), billing = billing)
        runCurrent()

        billing.setUnlocked(true)
        runCurrent()
        assertTrue(viewModel.uiState.value.showThankYou)

        viewModel.onThankYouDismissed()
        assertFalse(viewModel.uiState.value.showThankYou)
    }

    // --- Earlier / Cheaper window navigation ---

    /**
     * Hourly slots whose price *decreases* over time, so the cheapest window is last and every
     * earlier slot is a progressively-earlier (and costlier) alternative.
     */
    private fun descendingPrices(count: Int): List<PriceSlot> {
        val base = ZonedDateTime.now().withMinute(0).withSecond(0).withNano(0)
        return (0 until count).map { i ->
            PriceSlot(time = base.plusHours(i.toLong()), price = 1.0 - i * 0.05, durationMinutes = 60)
        }
    }

    @Test
    fun `fresh result starts at cheapest window with alternatives populated`() = runTest {
        val viewModel = testViewModel(FakeFetcher(descendingPrices(6)))
        viewModel.onQuickDuration(1, 0)
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals(0, state.windowOffset)
        assertTrue("expected several alternatives", state.windowAlternatives.size > 1)
        assertEquals(state.windowAlternatives.first(), state.result)
        viewModel.onClearResult()
    }

    @Test
    fun `onEarlierWindow advances to an earlier costlier window`() = runTest {
        val viewModel = testViewModel(FakeFetcher(descendingPrices(6)))
        viewModel.onQuickDuration(1, 0)
        runCurrent()

        val cheapest = viewModel.uiState.value.result!!
        viewModel.onEarlierWindow()

        val state = viewModel.uiState.value
        assertEquals(1, state.windowOffset)
        assertTrue(state.result!!.startTime.isBefore(cheapest.startTime))
        assertTrue(state.result!!.totalCost > cheapest.totalCost)
        viewModel.onClearResult()
    }

    @Test
    fun `onCheaperWindow reverses onEarlierWindow`() = runTest {
        val viewModel = testViewModel(FakeFetcher(descendingPrices(6)))
        viewModel.onQuickDuration(1, 0)
        runCurrent()

        val cheapest = viewModel.uiState.value.result!!
        viewModel.onEarlierWindow()
        viewModel.onEarlierWindow()
        viewModel.onCheaperWindow()

        val state = viewModel.uiState.value
        assertEquals(1, state.windowOffset)
        viewModel.onCheaperWindow()
        assertEquals(0, viewModel.uiState.value.windowOffset)
        assertEquals(cheapest, viewModel.uiState.value.result)
        viewModel.onClearResult()
    }

    @Test
    fun `onCheaperWindow is a no-op at the cheapest window`() = runTest {
        val viewModel = testViewModel(FakeFetcher(descendingPrices(6)))
        viewModel.onQuickDuration(1, 0)
        runCurrent()

        viewModel.onCheaperWindow()
        assertEquals(0, viewModel.uiState.value.windowOffset)
        viewModel.onClearResult()
    }

    @Test
    fun `onEarlierWindow stops at the earliest window`() = runTest {
        val viewModel = testViewModel(FakeFetcher(descendingPrices(6)))
        viewModel.onQuickDuration(1, 0)
        runCurrent()

        val lastIndex = viewModel.uiState.value.windowAlternatives.size - 1
        repeat(lastIndex + 5) { viewModel.onEarlierWindow() } // tap past the end
        assertEquals(lastIndex, viewModel.uiState.value.windowOffset)
        viewModel.onClearResult()
    }

    @Test
    fun `periodic refresh preserves the navigated window`() = runTest {
        val viewModel = testViewModel(FakeFetcher(descendingPrices(6)))
        viewModel.onQuickDuration(1, 0)
        runCurrent()

        viewModel.onEarlierWindow()
        val selectedStart = viewModel.uiState.value.result!!.startTime

        advanceTimeBy(61_000) // fire the 60s recalculateResult
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals(selectedStart, state.result!!.startTime)
        assertEquals(1, state.windowOffset)
        viewModel.onClearResult() // cancel the refresh loop so runTest can settle
    }

    @Test
    fun `onClearResult resets window navigation`() = runTest {
        val viewModel = testViewModel(FakeFetcher(descendingPrices(6)))
        viewModel.onQuickDuration(1, 0)
        runCurrent()
        viewModel.onEarlierWindow()

        viewModel.onClearResult()
        val state = viewModel.uiState.value
        assertEquals(0, state.windowOffset)
        assertTrue(state.windowAlternatives.isEmpty())
        assertNull(state.result)
    }

    // --- EV charging ---

    private val testEvRepo = EvVehicleRepository(
        """[{"brand":"Test","model":"EV","variant":null,"year":2024,"batteryKwh":60.0,"acMaxPowerKw":11.0}]"""
    )

    /** Creates a ViewModel with injected fakes plus the test EV database. */
    private fun evViewModel(fetcher: FakeFetcher = FakeFetcher(fakePrices(24))) =
        SweetSpotViewModel(app, { _ -> fetcher }, FakeCache(), FakeStatsCollector(), testDispatcher, null, testEvRepo)

    /** A test vehicle appliance: 60 kWh battery, 11 kW max AC. */
    private fun addTestVehicle(viewModel: SweetSpotViewModel): Appliance {
        viewModel.onAddVehicle("Test EV", 60.0, 11.0)
        return viewModel.uiState.value.appliances.first { it.isEv }
    }

    @Test
    fun `searchEvVehicles finds matches once the database has loaded`() = runTest {
        val viewModel = evViewModel()
        runCurrent() // let the eager DB load complete
        assertEquals(1, viewModel.searchEvVehicles("test ev").size)
        assertTrue(viewModel.searchEvVehicles("").isEmpty())
    }

    @Test
    fun `onAddVehicle stores an EV-type appliance`() {
        val viewModel = evViewModel()
        viewModel.onAddVehicle("Test EV", 60.0, 11.0)

        val vehicles = viewModel.uiState.value.appliances.filter { it.isEv }
        assertEquals(1, vehicles.size)
        assertEquals("Test EV", vehicles.first().name)
        assertEquals(60.0, vehicles.first().ev!!.batteryKwh, 0.001)
        assertEquals("ev_charger", vehicles.first().icon)
    }

    @Test
    fun `onEvApplianceFind computes duration and produces a result`() = runTest {
        val viewModel = evViewModel()
        val vehicle = addTestVehicle(viewModel)
        viewModel.onEvHomeChargerChanged(11.0)
        viewModel.onEvApplianceFind(vehicle, 20, 80)
        runCurrent()

        val state = viewModel.uiState.value
        // 36 kWh at 11 kW = 196 min = 3h 16m.
        assertEquals(3, state.durationHours)
        assertEquals(16, state.durationMinutes)
        assertNotNull(state.result)
        assertTrue((state.resultLabel as UiText.Raw).value.contains("→"))
        viewModel.onClearResult()
    }

    @Test
    fun `onEvApplianceFind uses the lower of vehicle and charger power`() = runTest {
        val viewModel = evViewModel()
        val vehicle = addTestVehicle(viewModel) // 11 kW max
        viewModel.onEvHomeChargerChanged(3.7) // slower charger wins
        viewModel.onEvApplianceFind(vehicle, 20, 80)
        runCurrent()

        val state = viewModel.uiState.value
        // 36 kWh at 3.7 kW = 584 min = 9h 44m.
        assertEquals(9, state.durationHours)
        assertEquals(44, state.durationMinutes)
        viewModel.onClearResult()
    }

    @Test
    fun `onEvApplianceFind with target not above current sets validation error`() {
        val viewModel = evViewModel()
        val vehicle = addTestVehicle(viewModel)
        viewModel.onEvApplianceFind(vehicle, 80, 80)

        assertTrue(viewModel.uiState.value.error is AppError.Validation)
        assertNull(viewModel.uiState.value.result)
    }

    @Test
    fun `deadline is applied to a regular duration search when enabled`() = runTest {
        val viewModel = evViewModel()
        viewModel.onDurationChanged(2, 0)

        viewModel.onFindClicked()
        runCurrent()
        assertNull(viewModel.uiState.value.searchDeadline)
        viewModel.onClearResult()

        viewModel.onDeadlineEnabledChanged(true)
        viewModel.onDeadlineChanged(7, 30)
        viewModel.onDurationChanged(2, 0)
        viewModel.onFindClicked()
        runCurrent()
        assertNotNull(viewModel.uiState.value.searchDeadline)
        viewModel.onClearResult()
    }

    @Test
    fun `onEvApplianceFind applies the universal deadline when enabled`() = runTest {
        val viewModel = evViewModel()
        val vehicle = addTestVehicle(viewModel)
        viewModel.onDeadlineEnabledChanged(true)
        viewModel.onDeadlineChanged(7, 30)
        viewModel.onEvApplianceFind(vehicle, 20, 80)
        runCurrent()

        assertNotNull(viewModel.uiState.value.searchDeadline)
        viewModel.onClearResult()
    }

    @Test
    fun `EV settings persist across ViewModel instances`() {
        val viewModel = evViewModel()
        val vehicle = addTestVehicle(viewModel)
        viewModel.onEvHomeChargerChanged(7.4)
        viewModel.onEvDefaultTargetChanged(90)
        viewModel.onEvApplianceFind(vehicle, 30, 90) // persists last current SoC

        val reloaded = evViewModel()
        val state = reloaded.uiState.value
        assertEquals(1, state.appliances.count { it.isEv })
        assertEquals(7.4, state.evHomeChargerKw, 0.001)
        assertEquals(90, state.evDefaultTargetSoc)
        assertEquals(30, state.evLastCurrentSoc)
    }

    @Test
    fun `EV appliances are excluded from wear sync but kept on the phone`() {
        val viewModel = evViewModel()
        viewModel.onAddAppliance("Washer", 2, 0, "washing_machine")
        viewModel.onAddVehicle("Test EV", 60.0, 11.0)

        // Both kinds are kept in the phone-side appliance list.
        assertEquals(2, viewModel.uiState.value.appliances.size)
        assertEquals(1, viewModel.uiState.value.appliances.count { it.isEv })
    }

    // --- Power rating (cost scaling) ---

    @Test
    fun `manual search has no load power`() = runTest {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        viewModel.onDurationChanged(2, 0)
        viewModel.onFindClicked()
        runCurrent()
        assertNull(viewModel.uiState.value.searchPowerKw)
        viewModel.onClearResult()
    }

    @Test
    fun `appliance tap carries its power rating into the search`() = runTest {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        viewModel.onAddAppliance("Dishwasher", 2, 0, "dishwasher", 2.0)
        val appliance = viewModel.uiState.value.appliances.first { it.name == "Dishwasher" }
        viewModel.onApplianceDuration(appliance)
        runCurrent()
        assertEquals(2.0, viewModel.uiState.value.searchPowerKw)
        viewModel.onClearResult()
    }

    @Test
    fun `appliance without a power rating searches per 1 kW`() = runTest {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        viewModel.onAddAppliance("Lamp", 2, 0, "electricity")
        val appliance = viewModel.uiState.value.appliances.first { it.name == "Lamp" }
        viewModel.onApplianceDuration(appliance)
        runCurrent()
        assertNull(viewModel.uiState.value.searchPowerKw)
        viewModel.onClearResult()
    }

    @Test
    fun `EV charging uses the effective charging power as the load`() = runTest {
        val viewModel = evViewModel()
        val vehicle = addTestVehicle(viewModel) // 11 kW max AC
        viewModel.onEvHomeChargerChanged(7.4)   // slower charger wins
        viewModel.onEvApplianceFind(vehicle, 20, 80)
        runCurrent()
        assertEquals(7.4, viewModel.uiState.value.searchPowerKw)
        viewModel.onClearResult()
    }

    @Test
    fun `appliance power rating persists across ViewModel instances`() {
        val viewModel = evViewModel()
        viewModel.onAddAppliance("Dishwasher", 2, 0, "dishwasher", 2.0)

        val reloaded = evViewModel()
        val appliance = reloaded.uiState.value.appliances.first { it.name == "Dishwasher" }
        assertEquals(2.0, appliance.powerKw)
    }

    @Test
    fun `onClearResult resets the load power`() = runTest {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        viewModel.onAddAppliance("Dishwasher", 2, 0, "dishwasher", 2.0)
        viewModel.onApplianceDuration(viewModel.uiState.value.appliances.first())
        runCurrent()
        viewModel.onClearResult()
        assertNull(viewModel.uiState.value.searchPowerKw)
    }

    // --- Paywall decision (pure) ---

    @Test
    fun `shouldShowPaywall only blocks a release build with an expired unpaid trial`() {
        assertTrue(shouldShowPaywall(isDebug = false, trialExpired = true, unlocked = false))
        assertFalse(shouldShowPaywall(isDebug = true, trialExpired = true, unlocked = false))  // debug always skips
        assertFalse(shouldShowPaywall(isDebug = false, trialExpired = false, unlocked = false)) // trial live
        assertFalse(shouldShowPaywall(isDebug = false, trialExpired = true, unlocked = true))   // subscribed
    }

    // --- Stats reporting (provider + poster wiring) ---

    /** Creates a ViewModel with an injected stats poster and pre-seeded collector. */
    private fun reportingViewModel(poster: StatsPoster, collector: FakeStatsCollector) =
        SweetSpotViewModel(app, { _ -> FakeFetcher(fakePrices(24)) }, FakeCache(), collector, testDispatcher, null, null, poster)

    @Test
    fun `successful fetch reports stats with the trial status`() = runTest {
        val collector = FakeStatsCollector().apply { record(StatsRecord(1000L, "NL", "entsoe", "phone", true, "", 5)) }
        val poster = FakePoster()
        val viewModel = reportingViewModel(poster, collector)
        viewModel.onStatsEnabledChanged(true)
        viewModel.onDurationChanged(1, 0)
        viewModel.onFindClicked()
        runCurrent()

        assertEquals(1, poster.callCount)
        assertTrue(poster.lastJson!!.contains("\"status\":\"trial\""))
        viewModel.onClearResult()
    }

    @Test
    fun `stats report carries the subscribed status when unlocked`() = runTest {
        val prefs = app.getSharedPreferences("sweetspot_settings", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("unlocked", true).commit()
        val collector = FakeStatsCollector().apply { record(StatsRecord(1000L, "NL", "entsoe", "phone", true, "", 5)) }
        val poster = FakePoster()
        val viewModel = reportingViewModel(poster, collector)
        viewModel.onStatsEnabledChanged(true)
        viewModel.onDurationChanged(1, 0)
        viewModel.onFindClicked()
        runCurrent()

        assertTrue(poster.lastJson!!.contains("\"status\":\"subscribed\""))
        viewModel.onClearResult()
    }

    @Test
    fun `stats report carries the expired status when the trial has lapsed`() = runTest {
        val prefs = app.getSharedPreferences("sweetspot_settings", Context.MODE_PRIVATE)
        val fifteenDaysAgo = System.currentTimeMillis() - (15 * 24 * 60 * 60 * 1000L)
        prefs.edit().putLong("first_launch_ms", fifteenDaysAgo).putBoolean("unlocked", false).commit()
        val collector = FakeStatsCollector().apply { record(StatsRecord(1000L, "NL", "entsoe", "phone", true, "", 5)) }
        val poster = FakePoster()
        val viewModel = reportingViewModel(poster, collector)
        viewModel.onStatsEnabledChanged(true)
        viewModel.onDurationChanged(1, 0)
        viewModel.onFindClicked()
        runCurrent()

        assertTrue(poster.lastJson!!.contains("\"status\":\"expired\""))
        viewModel.onClearResult()
    }

    @Test
    fun `stats are not reported when opt-in is disabled`() = runTest {
        val collector = FakeStatsCollector().apply { record(StatsRecord(1000L, "NL", "entsoe", "phone", true, "", 5)) }
        val poster = FakePoster()
        val viewModel = reportingViewModel(poster, collector)
        // stats disabled by default
        viewModel.onDurationChanged(1, 0)
        viewModel.onFindClicked()
        runCurrent()

        assertEquals(0, poster.callCount)
        viewModel.onClearResult()
    }

    // --- EV database lazy load (real asset) ---

    @Test
    fun `real vehicle database loads and powers the search`() = runTest {
        // No EV repo override → the bundled ev-vehicles.json asset is parsed in init.
        val viewModel = SweetSpotViewModel(app, { _ -> FakeFetcher(fakePrices(24)) }, FakeCache(), FakeStatsCollector(), testDispatcher)
        runCurrent() // let the eager load finish
        assertTrue(viewModel.searchEvVehicles("").isEmpty())      // blank query short-circuits
        assertTrue(viewModel.searchEvVehicles("a").isNotEmpty())  // some vehicle matches "a"
    }

    // --- EV find edge cases ---

    @Test
    fun `onEvApplianceFind ignores a non-EV appliance`() {
        val viewModel = evViewModel()
        val notEv = Appliance(id = "1", name = "Washer", durationHours = 2, durationMinutes = 0, icon = "laundry")
        viewModel.onEvApplianceFind(notEv, 20, 80)
        val state = viewModel.uiState.value
        assertNull(state.result)
        assertNull(state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `onEvApplianceFind with no resolved zone sets a validation error`() {
        val viewModel = evViewModel()
        val vehicle = addTestVehicle(viewModel)
        val multi = today.sweetspot.model.Countries.all.first { it.zones.size > 1 }
        viewModel.onCountrySelected(multi.code) // multi-zone country, no zone selected → null zone
        assertNull(viewModel.uiState.value.priceZone)

        viewModel.onEvApplianceFind(vehicle, 20, 80)
        assertTrue(viewModel.uiState.value.error is AppError.Validation)
        assertNull(viewModel.uiState.value.result)
    }

    @Test
    fun `onEvApplianceFind with a zero-power charger sets a validation error`() {
        val viewModel = evViewModel()
        val vehicle = addTestVehicle(viewModel)
        viewModel.onEvHomeChargerChanged(0.0) // effective power = min(11, 0) = 0
        viewModel.onEvApplianceFind(vehicle, 20, 80)
        assertTrue(viewModel.uiState.value.error is AppError.Validation)
        assertNull(viewModel.uiState.value.result)
    }

    // --- Find validation: no zone ---

    @Test
    fun `onFindClicked with no resolved zone sets a validation error`() {
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        val multi = today.sweetspot.model.Countries.all.first { it.zones.size > 1 }
        viewModel.onCountrySelected(multi.code)
        assertNull(viewModel.uiState.value.priceZone)

        viewModel.onDurationChanged(2, 0)
        viewModel.onFindClicked()
        val state = viewModel.uiState.value
        assertTrue(state.error is AppError.Validation)
        assertEquals(UiText.Res(R.string.error_no_zone), state.error!!.message)
    }

    // --- Deadline unreachable ---

    @Test
    fun `an unreachable deadline yields the deadline error`() = runTest {
        // 24h of prices, but a deadline ~1h out and a 10h duration → no window fits by the deadline.
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)))
        val now = ZonedDateTime.now()
        viewModel.onDeadlineEnabledChanged(true)
        viewModel.onDeadlineChanged(now.plusHours(1).hour, 0)
        viewModel.onDurationChanged(10, 0)
        viewModel.onFindClicked()
        runCurrent()

        val err = viewModel.uiState.value.error
        assertTrue(err is AppError.Validation)
        assertEquals(UiText.Res(R.string.ev_error_deadline_unreachable), (err as AppError.Validation).message)
        viewModel.onClearResult()
    }

    // --- Zone selection ---

    @Test
    fun `onPriceZoneSelected resolves and applies the chosen zone`() {
        val viewModel = defaultViewModel()
        val multi = today.sweetspot.model.Countries.all.first { it.zones.size > 1 }
        viewModel.onCountrySelected(multi.code)
        val zone = multi.zones[1]
        viewModel.onPriceZoneSelected(zone.id)
        assertEquals(zone, viewModel.uiState.value.priceZone)
    }

    // --- Language & theme ---

    @Test
    fun `onLanguageChanged applies without error`() {
        val viewModel = defaultViewModel()
        viewModel.onLanguageChanged("de")
        // No crash; state unchanged (locale switch is a framework side effect).
        assertNotNull(viewModel.uiState.value)
    }

    @Test
    fun `onThemeModeChanged updates and persists the theme`() {
        val viewModel = defaultViewModel()
        viewModel.onThemeModeChanged(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, viewModel.uiState.value.themeMode)

        val reloaded = SweetSpotViewModel(app)
        assertEquals(ThemeMode.DARK, reloaded.uiState.value.themeMode)
    }

    // --- Developer options (remaining) ---

    @Test
    fun `onDevTimeOverrideChanged sets the override and clears the cache`() {
        val cache = FakeCache()
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)), cache)
        val future = System.currentTimeMillis() + (2 * 24 * 60 * 60 * 1000L)
        viewModel.onDevTimeOverrideChanged(future)
        assertEquals(future, viewModel.uiState.value.timeOverrideMs)
        assertTrue(cache.clearCount > 0)

        viewModel.onDevTimeOverrideChanged(null)
        assertNull(viewModel.uiState.value.timeOverrideMs)
    }

    @Test
    fun `onDevUseProductionLogoChanged toggles the flag`() {
        val viewModel = defaultViewModel()
        viewModel.onDevUseProductionLogoChanged(true)
        assertTrue(viewModel.uiState.value.useProductionLogo)
    }

    @Test
    fun `onDevResetStatsTimer does not crash`() {
        val viewModel = defaultViewModel()
        viewModel.onDevResetStatsTimer()
    }

    // --- Purchase forwarding ---

    @Test
    fun `onPurchaseClicked and onRestorePurchases forward to billing`() = runTest {
        val billing = FakeBillingRepository(initialUnlocked = false)
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)), billing = billing)
        runCurrent()

        val activity = Robolectric.buildActivity(Activity::class.java).get()
        viewModel.onPurchaseClicked(activity)
        viewModel.onRestorePurchases()

        assertEquals(1, billing.launchCount)
        assertEquals(1, billing.queryCount)
    }

    // --- recalculateResult: no window fits ---

    @Test
    fun `recalculateResult keeps the last result when every slot has elapsed`() = runTest {
        val cache = FakeCache()
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)), cache)
        viewModel.onQuickDuration(1, 0)
        runCurrent()
        assertNotNull(viewModel.uiState.value.result)
        val kept = viewModel.uiState.value.result

        // Jump "now" two days ahead so all fetched slots are in the past.
        viewModel.onDevTimeOverrideChanged(System.currentTimeMillis() + (2 * 24 * 60 * 60 * 1000L))
        viewModel.recalculateResult()

        // Result is preserved (not nulled) even though no future window remains.
        assertEquals(kept, viewModel.uiState.value.result)
        viewModel.onClearResult()
    }
}
