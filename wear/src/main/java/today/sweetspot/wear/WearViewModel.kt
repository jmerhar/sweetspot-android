package today.sweetspot.wear

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import today.sweetspot.data.api.PriceFetcherFactory
import today.sweetspot.data.api.defaultPriceFetcherFactory
import today.sweetspot.data.cache.FilePriceCache
import today.sweetspot.data.cache.PriceCache
import today.sweetspot.data.repository.PriceRepository
import today.sweetspot.data.stats.FileStatsCollector
import today.sweetspot.data.stats.StatsCollector
import today.sweetspot.data.stats.StatsRecord
import today.sweetspot.data.usage.FileUsageStore
import today.sweetspot.data.usage.UsageSnapshot
import today.sweetspot.data.usage.UsageStore
import today.sweetspot.model.Appliance
import today.sweetspot.model.Countries
import today.sweetspot.model.PriceSlot
import today.sweetspot.model.PriceZone
import today.sweetspot.model.WindowResult
import today.sweetspot.util.UiText
import today.sweetspot.util.findCheapestWindow
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * UI state for the Wear OS companion app.
 *
 * @property appliances Appliance list synced from the phone via the Wearable Data Layer.
 * @property isLoading Whether a price fetch is in progress.
 * @property error Error message to display ([UiText], resolved by the UI in the current locale), or `null` if none.
 * @property result The cheapest-window result, or `null` if no search has been performed.
 * @property resultLabel Label shown on the result screen (e.g. "Washer · 2h 30m"), as deferred [UiText].
 * @property priceZone The resolved price zone synced from the phone, or `null` if not yet configured.
 * @property sourceOrder Ordered list of all source IDs synced from the phone, or `null` for zone defaults.
 * @property disabledSources Set of disabled source IDs synced from the phone.
 * @property isLocked Whether the watch app is locked (phone trial expired and not unlocked).
 */
data class WearUiState(
    val appliances: List<Appliance> = emptyList(),
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val result: WindowResult? = null,
    val resultLabel: UiText? = null,
    val priceZone: PriceZone? = Countries.defaultCountry().zones.first(),
    val sourceOrder: List<String>? = null,
    val disabledSources: Set<String> = emptySet(),
    val isLocked: Boolean = false
)

/**
 * ViewModel for the Wear OS SweetSpot app.
 *
 * Receives appliances and zone settings from the phone via a [WearSync] (the Wearable Data Layer),
 * fetches electricity prices via [PriceRepository], and runs the cheapest-window algorithm from the
 * shared module. All Data Layer plumbing is isolated in [WearSync] so this class stays testable.
 *
 * @param application Application context.
 * @param priceFetcherFactory Optional factory override for testing. When `null` (production),
 *   the factory is created dynamically from the current source order.
 * @param priceCache Cache for raw price JSON.
 * @param statsCollector Optional stats collector override for testing.
 * @param ioDispatcher Dispatcher for IO-bound work (injectable for testing).
 * @param wearSyncOverride Optional [WearSync] override for testing. When `null` (production), a
 *   real [WearableSync] backed by Google Play Services is used.
 * @param usageStore Local cumulative tap store reported back to the phone (injectable for tests).
 */
class WearViewModel @JvmOverloads constructor(
    application: Application,
    private val priceFetcherFactory: PriceFetcherFactory? = null,
    private val priceCache: PriceCache = FilePriceCache(application),
    private val statsCollector: StatsCollector = FileStatsCollector(application.cacheDir),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    wearSyncOverride: WearSync? = null,
    private val usageStore: UsageStore = FileUsageStore(application.cacheDir)
) : AndroidViewModel(application) {

    private val wearSync: WearSync =
        wearSyncOverride ?: WearableSync(application, viewModelScope, ioDispatcher)

    private var fetchJob: Job? = null
    private var refreshJob: Job? = null

    /** Whether API stats collection is enabled (synced from phone). */
    @Volatile
    private var statsEnabled: Boolean = false

    /** Prices from the last successful fetch, used for periodic recalculation. */
    private var lastPrices: List<PriceSlot> = emptyList()

    /** Duration from the last appliance tap, used for periodic recalculation. */
    private var lastDurationHours: Double = 0.0

    /** Timezone from the last fetch, used for periodic recalculation. */
    private var lastTimeZoneId: ZoneId? = null

    private val _uiState = MutableStateFlow(WearUiState())
    /** Observable UI state. */
    val uiState: StateFlow<WearUiState> = _uiState.asStateFlow()

    init {
        wearSync.observe(::onAppliancesReceived, ::onSettingsReceived)
    }

    override fun onCleared() {
        super.onCleared()
        stopResultRefresh()
        wearSync.stop()
    }

    /**
     * Applies an appliance list received from the phone.
     *
     * @param json JSON-encoded appliance list from the Data Layer.
     */
    internal fun onAppliancesReceived(json: String) {
        _uiState.update { it.copy(appliances = parseAppliances(json)) }
    }

    /**
     * Applies settings received from the phone: resolves the price zone, source order, disabled
     * sources, stats opt-in, per-app language, and the locked state.
     *
     * @param settings Raw settings pushed from the phone.
     */
    internal fun onSettingsReceived(settings: WearSettings) {
        val zone = resolveZone(settings.countryCode, settings.priceZoneId)
        val sourceOrder = parseSourceOrder(settings.sourceOrder)
        val disabledSources = parseDisabledSources(settings.disabledSources)
        statsEnabled = settings.statsEnabled
        // Honour a phone-side usage purge: a newer reset token zeroes our local store.
        if (settings.usageResetToken > usageStore.token()) usageStore.reset(settings.usageResetToken)
        applyLanguage(settings.language)
        _uiState.update {
            it.copy(
                priceZone = zone,
                sourceOrder = sourceOrder,
                disabledSources = disabledSources,
                isLocked = settings.isTrialExpired && !settings.isUnlocked
            )
        }
    }

    /**
     * Handles an appliance tap. Fetches prices and finds the cheapest window.
     *
     * @param appliance The tapped appliance.
     */
    fun onApplianceTapped(appliance: Appliance) {
        val h = appliance.durationHours
        val m = appliance.durationMinutes
        val durationHours = h + m / 60.0
        val label = UiText.applianceLabel(appliance.name, h, m)

        val priceZone = _uiState.value.priceZone
        if (priceZone == null) {
            _uiState.update {
                it.copy(error = UiText.Res(R.string.wear_error_no_zone))
            }
            return
        }

        // Record the tap only once it's known to trigger a fetch, so a tap with no
        // configured zone (which does nothing) doesn't inflate the usage counters.
        usageStore.record(appliance.id, System.currentTimeMillis())

        stopResultRefresh()
        fetchJob?.cancel()
        _uiState.update {
            it.copy(isLoading = true, error = null, result = null, resultLabel = label)
        }

        val timeZoneId = ZoneId.of(priceZone.timeZoneId)
        fetchJob = viewModelScope.launch(ioDispatcher) {
            try {
                val state = _uiState.value
                val activeCollector = if (statsEnabled) statsCollector else null
                val factory = priceFetcherFactory
                    ?: defaultPriceFetcherFactory(BuildConfig.ENTSOE_API_TOKEN, state.sourceOrder, activeCollector, "watch", state.disabledSources)
                val fetcher = factory.create(priceZone)
                val repository = PriceRepository(priceCache, timeZoneId, fetcher, cacheKey = priceZone.id)
                val prices = repository.getPrices().prices

                if (prices.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = UiText.Res(R.string.wear_error_no_data)
                        )
                    }
                    return@launch
                }

                val now = ZonedDateTime.now(timeZoneId)
                val result = findCheapestWindow(prices, durationHours, now)

                if (result == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = UiText.Res(R.string.wear_error_not_enough_data, listOf(UiText.duration(h, m)))
                        )
                    }
                    return@launch
                }

                lastPrices = prices
                lastDurationHours = durationHours
                lastTimeZoneId = timeZoneId

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        result = result,
                        error = null
                    )
                }
                startResultRefresh()
                syncStatsToPhone()
                syncUsageToPhone()
            } catch (e: Exception) {
                Log.w("WearViewModel", "Could not fetch prices", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = UiText.Res(R.string.wear_error_network)
                    )
                }
                syncStatsToPhone()
                syncUsageToPhone()
            }
        }
    }

    /** Clears the current result to return to the appliance list. */
    fun onClearResult() {
        stopResultRefresh()
        _uiState.update { it.copy(result = null, resultLabel = null, error = null) }
    }

    /**
     * Starts a periodic refresh that recalculates the cheapest window every 60 seconds.
     *
     * Filters out elapsed price slots and re-runs [findCheapestWindow] with the current time,
     * keeping the result screen up-to-date as time passes.
     */
    private fun startResultRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (true) {
                delay(60_000)
                recalculateResult()
            }
        }
    }

    /**
     * Stops the periodic result refresh.
     */
    private fun stopResultRefresh() {
        refreshJob?.cancel()
        refreshJob = null
    }

    /**
     * Recalculates the cheapest window using previously fetched prices and the current time.
     *
     * Filters [lastPrices] to exclude elapsed slots, then re-runs [findCheapestWindow].
     * Updates [WearUiState.result] so the result screen stays current.
     */
    internal fun recalculateResult() {
        val prices = lastPrices
        if (prices.isEmpty() || _uiState.value.result == null) return
        val timeZoneId = lastTimeZoneId ?: return

        val now = ZonedDateTime.now(timeZoneId)
        val futurePrices = prices.filter {
            it.time.plusMinutes(it.durationMinutes.toLong()).isAfter(now)
        }
        lastPrices = futurePrices

        val result = if (futurePrices.isNotEmpty()) {
            findCheapestWindow(futurePrices, lastDurationHours, now)
        } else null

        _uiState.update { it.copy(result = result) }
    }

    /**
     * Applies the language tag received from the phone via the Data Layer.
     *
     * An empty or null tag means "system default".
     *
     * @param languageTag BCP 47 language tag, or empty/null for system default.
     */
    private fun applyLanguage(languageTag: String?) {
        val tag = languageTag ?: ""
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
    }

    /**
     * Resolves country code and zone ID from the Data Layer into a [PriceZone].
     *
     * @param countryCode ISO country code from the phone, or `null`.
     * @param priceZoneId Zone ID from the phone, or `null`.
     * @return The resolved [PriceZone], or `null` for a multi-zone country without a selection or a
     *         zone ID that no longer resolves. Only a completely empty sync falls back to the default.
     */
    private fun resolveZone(countryCode: String?, priceZoneId: String?): PriceZone? {
        // A stored zone id that no longer resolves (e.g. a zone removed in a newer app version)
        // surfaces as "no zone" rather than silently pricing a different country; fall back to the
        // country's own zone only when that country is unambiguously single-zone.
        if (priceZoneId != null) {
            Countries.findPriceZoneById(priceZoneId)?.let { return it }
            return countryCode?.let { Countries.findByCode(it) }?.takeIf { it.zones.size == 1 }?.zones?.first()
        }
        if (countryCode != null) {
            val country = Countries.findByCode(countryCode) ?: return null
            return if (country.zones.size == 1) country.zones.first() else null
        }
        return Countries.defaultCountry().zones.first()
    }

    /**
     * Parses a JSON string into a list of [Appliance].
     *
     * @param json JSON-encoded appliance list.
     * @return Parsed list, or empty list if parsing fails.
     */
    private fun parseAppliances(json: String): List<Appliance> {
        return try {
            Json.decodeFromString<List<Appliance>>(json)
        } catch (e: Exception) {
            Log.w("WearViewModel", "Failed to parse appliances JSON", e)
            emptyList()
        }
    }

    /**
     * Parses a source order string from the Data Layer into a list of source IDs.
     *
     * An empty or blank string means "use defaults" (returns `null`).
     *
     * @param raw JSON-encoded source order string, or `null`/empty for defaults.
     * @return Ordered list of source IDs, or `null` for defaults.
     */
    private fun parseSourceOrder(raw: String?): List<String>? {
        if (raw.isNullOrBlank()) return null
        return try {
            Json.decodeFromString<List<String>>(raw)
        } catch (e: Exception) {
            Log.w("WearViewModel", "Failed to parse source order JSON", e)
            null
        }
    }

    /**
     * Parses a disabled sources string from the Data Layer into a set of source IDs.
     *
     * An empty or blank string means "none disabled" (returns empty set).
     *
     * @param raw JSON-encoded disabled sources string, or `null`/empty for none.
     * @return Set of disabled source IDs.
     */
    private fun parseDisabledSources(raw: String?): Set<String> {
        if (raw.isNullOrBlank()) return emptySet()
        return try {
            Json.decodeFromString<Set<String>>(raw)
        } catch (e: Exception) {
            Log.w("WearViewModel", "Failed to parse disabled sources JSON", e)
            emptySet()
        }
    }

    /**
     * Pushes accumulated stats to the phone via [WearSync].
     *
     * Encodes stats records to the binary format used by [FileStatsCollector] and sends them; the
     * phone merges them into its local stats file. Only runs when stats collection is enabled, and
     * awaits delivery before clearing local stats to avoid loss. Best-effort — failures are ignored.
     */
    private suspend fun syncStatsToPhone() {
        if (!statsEnabled) return
        try {
            val records = statsCollector.readAll()
            if (records.isEmpty()) return
            wearSync.pushStats(StatsRecord.encodeToBytes(records))
            statsCollector.clear()
        } catch (_: Exception) {
            // Best-effort: stats sync should not crash the watch app
        }
    }

    /**
     * Pushes the watch's cumulative tap usage to the phone via [WearSync].
     *
     * The snapshot is cumulative (not a delta) and stamped with the reset token it was recorded
     * under, so re-delivery is harmless and a phone-side purge is honoured. Best-effort — failures
     * are ignored, and the local store is never cleared (the phone keeps only the latest snapshot).
     */
    private suspend fun syncUsageToPhone() {
        try {
            wearSync.pushUsage(UsageSnapshot.encodeToBytes(usageStore.snapshot()), usageStore.token())
        } catch (_: Exception) {
            // Best-effort: usage sync should not crash the watch app
        }
    }
}
