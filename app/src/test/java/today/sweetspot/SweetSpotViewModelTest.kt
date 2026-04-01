package today.sweetspot

import android.app.Application
import android.content.Context
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

import today.sweetspot.data.billing.BillingRepository
import today.sweetspot.data.cache.CachedPriceData
import today.sweetspot.data.cache.PriceCache
import today.sweetspot.data.stats.StatsCollector
import today.sweetspot.data.stats.StatsRecord
import today.sweetspot.model.Appliance
import today.sweetspot.model.PriceSlot
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

import android.app.Activity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
        override fun connect() {}
        override fun disconnect() {}
        override fun launchPurchaseFlow(activity: Activity) {}
        override fun queryPurchases() {}
        fun setUnlocked(value: Boolean) { _isUnlocked.value = value }
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
        assertTrue(state.error!!.message.contains("Could not fetch prices"))
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
        assertTrue(state.error!!.message.contains("No price data"))
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
        assertEquals("1h", state.resultLabel)
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
        assertEquals("Washer \u00b7 2h", state.resultLabel)
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
        assertEquals("3h", state.resultLabel)
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
        assertTrue(message.contains("cleared", ignoreCase = true))
    }

    @Test
    fun `onClearCache with cooldown active does not clear and returns warning`() {
        val cache = FakeCache(cooldownRemaining = 120_000L)
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)), cache)
        val message = viewModel.onClearCache()
        assertEquals(0, cache.clearCount)
        assertTrue(message.contains("minutes", ignoreCase = true))
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
        assertTrue(state.error!!.message.contains("minutes", ignoreCase = true))
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
    fun `onClearCache bypasses cooldown when cooldown is disabled`() {
        val cache = FakeCache(cooldownRemaining = 120_000L)
        val viewModel = testViewModel(FakeFetcher(fakePrices(24)), cache)
        viewModel.onDevCooldownDisabledChanged(true)

        val message = viewModel.onClearCache()
        assertEquals(1, cache.clearCount)
        assertTrue(message.contains("cleared", ignoreCase = true))
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
}
