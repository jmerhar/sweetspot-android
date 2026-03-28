package today.sweetspot.wear

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import today.sweetspot.data.api.PriceFetcherFactory
import today.sweetspot.data.api.defaultPriceFetcherFactory
import today.sweetspot.data.cache.FilePriceCache
import today.sweetspot.data.cache.PriceCache
import today.sweetspot.data.repository.PriceRepository
import today.sweetspot.model.Appliance
import today.sweetspot.model.Countries
import today.sweetspot.model.PriceSlot
import today.sweetspot.model.PriceZone
import today.sweetspot.model.WindowResult
import today.sweetspot.util.findCheapestWindow
import today.sweetspot.util.formatDuration
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * UI state for the Wear OS companion app.
 *
 * @property appliances Appliance list synced from the phone via the Wearable Data Layer.
 * @property isLoading Whether a price fetch is in progress.
 * @property error Error message to display, or `null` if none.
 * @property result The cheapest-window result, or `null` if no search has been performed.
 * @property resultLabel Label shown on the result screen (e.g. "Washer · 2h 30m").
 * @property priceZone The resolved price zone synced from the phone, or `null` if not yet configured.
 * @property sourceOrder Ordered list of all source IDs synced from the phone, or `null` for zone defaults.
 * @property disabledSources Set of disabled source IDs synced from the phone.
 */
data class WearUiState(
    val appliances: List<Appliance> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val result: WindowResult? = null,
    val resultLabel: String? = null,
    val priceZone: PriceZone? = Countries.defaultCountry().zones.first(),
    val sourceOrder: List<String>? = null,
    val disabledSources: Set<String> = emptySet()
)

/**
 * ViewModel for the Wear OS SweetSpot app.
 *
 * Reads appliances and zone settings from the Wearable Data Layer (pushed by the phone app),
 * fetches electricity prices via [PriceRepository], and runs the cheapest-window
 * algorithm from the shared module.
 *
 * @param application Application context.
 * @param priceFetcherFactory Optional factory override for testing. When `null` (production),
 *   the factory is created dynamically from the current source order.
 * @param priceCache Cache for raw price JSON.
 * @param ioDispatcher Dispatcher for IO-bound work (injectable for testing).
 */
class WearViewModel @JvmOverloads constructor(
    application: Application,
    private val priceFetcherFactory: PriceFetcherFactory? = null,
    private val priceCache: PriceCache = FilePriceCache(application),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AndroidViewModel(application),
    DataClient.OnDataChangedListener {

    private var fetchJob: Job? = null
    private var refreshJob: Job? = null

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
        Wearable.getDataClient(application).addListener(this)
        loadFromDataLayer()
    }

    override fun onCleared() {
        super.onCleared()
        stopResultRefresh()
        Wearable.getDataClient(getApplication()).removeListener(this)
    }

    /**
     * Called when the phone pushes data updates via the Data Layer.
     * Handles both `/appliances` and `/settings` paths.
     */
    override fun onDataChanged(events: DataEventBuffer) {
        try {
            for (event in events) {
                if (event.type != DataEvent.TYPE_CHANGED) continue
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap

                when (event.dataItem.uri.path) {
                    "/appliances" -> {
                        val json = dataMap.getString("json") ?: continue
                        val appliances = parseAppliances(json)
                        _uiState.update { it.copy(appliances = appliances) }
                    }
                    "/settings" -> {
                        val zone = resolveZone(
                            dataMap.getString("country_code"),
                            dataMap.getString("price_zone_id")
                        )
                        val sourceOrder = parseSourceOrder(dataMap.getString("source_order"))
                        val disabledSources = parseDisabledSources(dataMap.getString("disabled_sources"))
                        applyLanguage(dataMap.getString("language"))
                        _uiState.update { it.copy(priceZone = zone, sourceOrder = sourceOrder, disabledSources = disabledSources) }
                    }
                }
            }
        } finally {
            events.release()
        }
    }

    /**
     * Loads the current appliance list and zone settings from the Data Layer on startup.
     */
    private fun loadFromDataLayer() {
        viewModelScope.launch(ioDispatcher) {
            var dataItems: com.google.android.gms.wearable.DataItemBuffer? = null
            try {
                dataItems = Wearable.getDataClient(getApplication())
                    .dataItems.await()

                for (item in dataItems) {
                    val dataMap = DataMapItem.fromDataItem(item).dataMap

                    when (item.uri.path) {
                        "/appliances" -> {
                            val json = dataMap.getString("json") ?: continue
                            val appliances = parseAppliances(json)
                            _uiState.update { it.copy(appliances = appliances) }
                        }
                        "/settings" -> {
                            val zone = resolveZone(
                                dataMap.getString("country_code"),
                                dataMap.getString("price_zone_id")
                            )
                            val sourceOrder = parseSourceOrder(dataMap.getString("source_order"))
                            val disabledSources = parseDisabledSources(dataMap.getString("disabled_sources"))
                            applyLanguage(dataMap.getString("language"))
                            _uiState.update { it.copy(priceZone = zone, sourceOrder = sourceOrder, disabledSources = disabledSources) }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("WearViewModel", "Could not read from Data Layer", e)
            } finally {
                dataItems?.release()
            }
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
        val res = getApplication<Application>().resources
        val label = "${appliance.name} \u00b7 ${formatDuration(h, m, res)}"

        val priceZone = _uiState.value.priceZone
        if (priceZone == null) {
            _uiState.update {
                it.copy(error = getApplication<Application>().getString(R.string.wear_error_no_zone))
            }
            return
        }

        stopResultRefresh()
        fetchJob?.cancel()
        _uiState.update {
            it.copy(isLoading = true, error = null, result = null, resultLabel = label)
        }

        val timeZoneId = ZoneId.of(priceZone.timeZoneId)
        fetchJob = viewModelScope.launch(ioDispatcher) {
            try {
                val state = _uiState.value
                val enabledOrder = state.sourceOrder?.filter { it !in state.disabledSources }
                val factory = priceFetcherFactory
                    ?: defaultPriceFetcherFactory(BuildConfig.ENTSOE_API_TOKEN, enabledOrder)
                val fetcher = factory.create(priceZone)
                val repository = PriceRepository(priceCache, timeZoneId, fetcher, cacheKey = priceZone.id)
                val prices = repository.getPrices().prices

                if (prices.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = getApplication<Application>().getString(R.string.wear_error_no_data)
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
                            error = getApplication<Application>().getString(R.string.wear_error_not_enough_data, formatDuration(h, m, res))
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
            } catch (e: Exception) {
                Log.w("WearViewModel", "Could not fetch prices", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = getApplication<Application>().getString(R.string.wear_error_network)
                    )
                }
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
    private fun recalculateResult() {
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
     * @return The resolved [PriceZone], or `null` for multi-zone countries without a selection.
     */
    private fun resolveZone(countryCode: String?, priceZoneId: String?): PriceZone? {
        if (priceZoneId != null) {
            Countries.findPriceZoneById(priceZoneId)?.let { return it }
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
}
