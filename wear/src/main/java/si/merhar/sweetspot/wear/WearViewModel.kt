package si.merhar.sweetspot.wear

import android.app.Application
import android.util.Log
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import si.merhar.sweetspot.data.api.PriceFetcherFactory
import si.merhar.sweetspot.data.api.defaultPriceFetcherFactory
import si.merhar.sweetspot.data.cache.FilePriceCache
import si.merhar.sweetspot.data.cache.PriceCache
import si.merhar.sweetspot.data.repository.PriceRepository
import si.merhar.sweetspot.model.Appliance
import si.merhar.sweetspot.model.Countries
import si.merhar.sweetspot.model.PriceZone
import si.merhar.sweetspot.model.WindowResult
import si.merhar.sweetspot.util.findCheapestWindow
import si.merhar.sweetspot.util.formatDuration
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
 */
data class WearUiState(
    val appliances: List<Appliance> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val result: WindowResult? = null,
    val resultLabel: String? = null,
    val priceZone: PriceZone? = Countries.defaultCountry().zones.first()
)

/**
 * ViewModel for the Wear OS SweetSpot app.
 *
 * Reads appliances and zone settings from the Wearable Data Layer (pushed by the phone app),
 * fetches electricity prices via [PriceRepository], and runs the cheapest-window
 * algorithm from the shared module.
 *
 * @param application Application context.
 * @param priceFetcherFactory Factory for creating price fetchers per zone.
 * @param priceCache Cache for raw price JSON.
 * @param ioDispatcher Dispatcher for IO-bound work (injectable for testing).
 */
class WearViewModel @JvmOverloads constructor(
    application: Application,
    private val priceFetcherFactory: PriceFetcherFactory = defaultPriceFetcherFactory(BuildConfig.ENTSOE_API_TOKEN),
    private val priceCache: PriceCache = FilePriceCache(application),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AndroidViewModel(application),
    DataClient.OnDataChangedListener {

    private var fetchJob: Job? = null

    private val _uiState = MutableStateFlow(WearUiState())
    /** Observable UI state. */
    val uiState: StateFlow<WearUiState> = _uiState.asStateFlow()

    init {
        Wearable.getDataClient(application).addListener(this)
        loadFromDataLayer()
    }

    override fun onCleared() {
        super.onCleared()
        Wearable.getDataClient(getApplication<Application>()).removeListener(this)
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
                        _uiState.update { it.copy(priceZone = zone) }
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
                dataItems = Wearable.getDataClient(getApplication<Application>())
                    .getDataItems().await()

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
                            _uiState.update { it.copy(priceZone = zone) }
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
        val label = "${appliance.name} \u00b7 ${formatDuration(h, m)}"

        val priceZone = _uiState.value.priceZone
        if (priceZone == null) {
            _uiState.update {
                it.copy(error = "Please configure your zone on the phone.")
            }
            return
        }

        fetchJob?.cancel()
        _uiState.update {
            it.copy(isLoading = true, error = null, result = null, resultLabel = label)
        }

        val timeZoneId = ZoneId.of(priceZone.timeZoneId)
        fetchJob = viewModelScope.launch(ioDispatcher) {
            try {
                val fetcher = priceFetcherFactory.create(priceZone)
                val repository = PriceRepository(priceCache, timeZoneId, fetcher, cacheKey = priceZone.id)
                val prices = repository.getPrices().prices

                if (prices.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "No price data available."
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
                            error = "Not enough data for ${formatDuration(h, m)}."
                        )
                    }
                    return@launch
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        result = result,
                        error = null
                    )
                }
            } catch (e: Exception) {
                Log.w("WearViewModel", "Could not fetch prices", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Could not fetch prices."
                    )
                }
            }
        }
    }

    /** Clears the current result to return to the appliance list. */
    fun onClearResult() {
        _uiState.update { it.copy(result = null, resultLabel = null, error = null) }
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
}
