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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import si.merhar.sweetspot.data.FilePriceCache
import si.merhar.sweetspot.data.PriceRepository
import si.merhar.sweetspot.model.Appliance
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
 * @property zoneId Active timezone for price date boundaries and display.
 */
data class WearUiState(
    val appliances: List<Appliance> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val result: WindowResult? = null,
    val resultLabel: String? = null,
    val zoneId: ZoneId = ZoneId.systemDefault()
)

/**
 * ViewModel for the Wear OS SweetSpot app.
 *
 * Reads appliances from the Wearable Data Layer (pushed by the phone app),
 * fetches electricity prices via [PriceRepository], and runs the cheapest-window
 * algorithm from the shared module.
 */
class WearViewModel(application: Application) : AndroidViewModel(application),
    DataClient.OnDataChangedListener {

    private val priceCache = FilePriceCache(application)

    private val _uiState = MutableStateFlow(WearUiState())
    /** Observable UI state. */
    val uiState: StateFlow<WearUiState> = _uiState.asStateFlow()

    init {
        Wearable.getDataClient(application).addListener(this)
        loadAppliancesFromDataLayer()
    }

    override fun onCleared() {
        super.onCleared()
        Wearable.getDataClient(getApplication<Application>()).removeListener(this)
    }

    /**
     * Called when the phone pushes an appliance list update via the Data Layer.
     */
    override fun onDataChanged(events: DataEventBuffer) {
        for (event in events) {
            if (event.type == DataEvent.TYPE_CHANGED &&
                event.dataItem.uri.path == "/appliances"
            ) {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val json = dataMap.getString("json") ?: continue
                val appliances = parseAppliances(json)
                _uiState.value = _uiState.value.copy(appliances = appliances)
            }
        }
    }

    /**
     * Loads the current appliance list from the Data Layer on startup.
     */
    private fun loadAppliancesFromDataLayer() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dataItems = Wearable.getDataClient(getApplication<Application>())
                    .getDataItems().await()

                for (item in dataItems) {
                    if (item.uri.path == "/appliances") {
                        val dataMap = DataMapItem.fromDataItem(item).dataMap
                        val json = dataMap.getString("json") ?: continue
                        val appliances = parseAppliances(json)
                        _uiState.value = _uiState.value.copy(appliances = appliances)
                    }
                }
                dataItems.release()
            } catch (e: Exception) {
                Log.w("WearViewModel", "Could not read appliances from Data Layer", e)
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

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null,
            result = null,
            resultLabel = label
        )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val zoneId = _uiState.value.zoneId
                val repository = PriceRepository(priceCache, zoneId)
                val prices = repository.getPrices()

                if (prices.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No price data available."
                    )
                    return@launch
                }

                val now = ZonedDateTime.now(zoneId)
                val result = findCheapestWindow(prices, durationHours, now)

                if (result == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Not enough data for ${formatDuration(h, m)}."
                    )
                    return@launch
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    result = result,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Could not fetch prices."
                )
            }
        }
    }

    /** Clears the current result to return to the appliance list. */
    fun onClearResult() {
        _uiState.value = _uiState.value.copy(
            result = null,
            resultLabel = null,
            error = null
        )
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
