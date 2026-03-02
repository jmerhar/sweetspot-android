package si.merhar.sweetspot

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import si.merhar.sweetspot.data.FilePriceCache
import si.merhar.sweetspot.data.PriceRepository
import si.merhar.sweetspot.data.SettingsRepository
import si.merhar.sweetspot.model.Appliance
import si.merhar.sweetspot.model.HourlyPrice
import si.merhar.sweetspot.model.WindowResult
import si.merhar.sweetspot.util.findCheapestWindow
import si.merhar.sweetspot.util.formatDuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

/**
 * UI state for the main screen.
 *
 * @property durationHours Selected hours component of the duration (0–24).
 * @property durationMinutes Selected minutes component of the duration (0, 5, 10, ..., 55).
 * @property isLoading Whether a price fetch is in progress.
 * @property error Error message to display, or `null` if none.
 * @property result The cheapest-window result, or `null` if no search has been performed.
 * @property resultLabel Label shown in the results screen top bar (e.g. "Washing machine · 2h 30m").
 * @property allPrices All hourly prices for the next 24h, used by the bar chart.
 * @property showSettings Whether the settings screen is currently visible.
 * @property zoneId Active timezone for price date boundaries and display.
 * @property isUsingDefaultZone Whether the timezone is the system default (vs. user-selected).
 * @property appliances User-configured appliances with preset durations.
 */
data class UiState(
    val durationHours: Int = 1,
    val durationMinutes: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val result: WindowResult? = null,
    val resultLabel: String? = null,
    val allPrices: List<HourlyPrice> = emptyList(),
    val showSettings: Boolean = false,
    val zoneId: ZoneId = ZoneId.systemDefault(),
    val isUsingDefaultZone: Boolean = true,
    val appliances: List<Appliance> = emptyList()
)

/**
 * ViewModel for the SweetSpot app.
 *
 * Owns all UI state via [uiState]. Handles duration selection, price fetching,
 * cheapest-window calculation, timezone configuration, and appliance CRUD.
 */
class SweetSpotViewModel(application: Application) : AndroidViewModel(application) {

    private val priceCache = FilePriceCache(application)
    private val settingsRepository = SettingsRepository(application)

    private val _uiState = MutableStateFlow(
        UiState(
            zoneId = settingsRepository.getZoneId(),
            isUsingDefaultZone = settingsRepository.isUsingDefaultZone(),
            appliances = settingsRepository.getAppliances()
        )
    )

    /** Observable UI state. */
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /**
     * Updates the selected duration from the picker.
     *
     * @param hours Hours component (0–24).
     * @param minutes Minutes component (0–55, in 5-minute steps).
     */
    fun onDurationChanged(hours: Int, minutes: Int) {
        _uiState.value = _uiState.value.copy(durationHours = hours, durationMinutes = minutes)
    }

    /** Opens the settings screen. */
    fun onShowSettings() {
        _uiState.value = _uiState.value.copy(showSettings = true)
    }

    /** Closes the settings screen and refreshes the appliance list from storage. */
    fun onHideSettings() {
        val appliances = settingsRepository.getAppliances()
        _uiState.value = _uiState.value.copy(
            showSettings = false,
            appliances = appliances
        )
        syncAppliancesToWear(appliances)
    }

    /**
     * Updates the timezone selection.
     *
     * @param zoneId The chosen timezone, or `null` to revert to system default.
     */
    fun onZoneSelected(zoneId: ZoneId?) {
        if (zoneId == null) {
            settingsRepository.clearZoneId()
            _uiState.value = _uiState.value.copy(
                zoneId = settingsRepository.getZoneId(),
                isUsingDefaultZone = true
            )
        } else {
            settingsRepository.setZoneId(zoneId)
            _uiState.value = _uiState.value.copy(
                zoneId = zoneId,
                isUsingDefaultZone = false
            )
        }
    }

    /**
     * Handles a quick-duration button tap. Sets the picker values and immediately triggers a search.
     *
     * @param hours Hours component of the quick duration.
     * @param minutes Minutes component of the quick duration.
     */
    fun onQuickDuration(hours: Int, minutes: Int) {
        _uiState.value = _uiState.value.copy(
            durationHours = hours,
            durationMinutes = minutes,
            resultLabel = formatDuration(hours, minutes)
        )
        onFindClicked()
    }

    /**
     * Handles an appliance chip tap. Sets the picker to the appliance's duration
     * and immediately triggers a search.
     *
     * @param appliance The tapped appliance.
     */
    fun onApplianceDuration(appliance: Appliance) {
        val label = "${appliance.name} \u00b7 ${formatDuration(appliance.durationHours, appliance.durationMinutes)}"
        _uiState.value = _uiState.value.copy(
            durationHours = appliance.durationHours,
            durationMinutes = appliance.durationMinutes,
            resultLabel = label
        )
        onFindClicked()
    }

    /** Clears the current result and returns to the form screen. */
    fun onClearResult() {
        _uiState.value = _uiState.value.copy(
            result = null,
            resultLabel = null,
            allPrices = emptyList(),
            error = null
        )
    }

    /**
     * Adds a new appliance and persists it.
     *
     * @param name Display name.
     * @param durationHours Hours component of the default duration.
     * @param durationMinutes Minutes component of the default duration.
     * @param icon Icon ID from the appliance icon registry.
     */
    fun onAddAppliance(name: String, durationHours: Int, durationMinutes: Int, icon: String) {
        val appliance = Appliance(
            id = UUID.randomUUID().toString(),
            name = name,
            durationHours = durationHours,
            durationMinutes = durationMinutes,
            icon = icon
        )
        val updated = _uiState.value.appliances + appliance
        settingsRepository.setAppliances(updated)
        _uiState.value = _uiState.value.copy(appliances = updated)
        syncAppliancesToWear(updated)
    }

    /**
     * Replaces an existing appliance (matched by ID) and persists the change.
     *
     * @param appliance The updated appliance.
     */
    fun onUpdateAppliance(appliance: Appliance) {
        val updated = _uiState.value.appliances.map {
            if (it.id == appliance.id) appliance else it
        }
        settingsRepository.setAppliances(updated)
        _uiState.value = _uiState.value.copy(appliances = updated)
        syncAppliancesToWear(updated)
    }

    /**
     * Deletes an appliance by ID and persists the change.
     *
     * @param id The appliance ID to remove.
     */
    fun onDeleteAppliance(id: String) {
        val updated = _uiState.value.appliances.filter { it.id != id }
        settingsRepository.setAppliances(updated)
        _uiState.value = _uiState.value.copy(appliances = updated)
        syncAppliancesToWear(updated)
    }

    /**
     * Pushes the current appliance list to the Wearable Data Layer so
     * the Wear OS companion app receives it.
     *
     * @param appliances The appliance list to sync.
     */
    private fun syncAppliancesToWear(appliances: List<Appliance>) {
        val json = Json.encodeToString(appliances)
        val request = PutDataMapRequest.create("/appliances").apply {
            dataMap.putString("json", json)
            dataMap.putLong("ts", System.currentTimeMillis())
        }.asPutDataRequest().setUrgent()
        Wearable.getDataClient(getApplication<Application>()).putDataItem(request)
    }

    /**
     * Validates the current duration, fetches prices, and finds the cheapest window.
     *
     * Sets [UiState.isLoading] while working. On success, populates [UiState.result]
     * and [UiState.allPrices]. On failure, sets [UiState.error].
     */
    fun onFindClicked() {
        val h = _uiState.value.durationHours
        val m = _uiState.value.durationMinutes

        if (h == 0 && m == 0) {
            _uiState.value = _uiState.value.copy(
                error = "Please select a duration greater than zero.",
                result = null,
                allPrices = emptyList()
            )
            return
        }

        val durationHours = h + m / 60.0
        val durationLabel = formatDuration(h, m)

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null,
            result = null,
            resultLabel = _uiState.value.resultLabel ?: durationLabel
        )

        viewModelScope.launch(Dispatchers.IO) {
            fetchAndFind(durationHours, durationLabel)
        }
    }

    /**
     * Fetches prices from the repository and runs the cheapest-window algorithm.
     *
     * Called on [Dispatchers.IO]. Updates [_uiState] with the result or an error.
     *
     * @param durationHours Duration in decimal hours.
     * @param durationLabel Human-readable duration label for error messages.
     */
    private fun fetchAndFind(durationHours: Double, durationLabel: String) {
        try {
            val repository = PriceRepository(priceCache, _uiState.value.zoneId)
            val prices = repository.getPrices()

            if (prices.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "No price data available for the next 24 hours.",
                    allPrices = emptyList()
                )
                return
            }

            val now = ZonedDateTime.now(_uiState.value.zoneId)
            val result = findCheapestWindow(prices, durationHours, now)

            if (result == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Not enough price data to cover $durationLabel. Only ${prices.size} hour(s) of data available.",
                    allPrices = prices
                )
                return
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                result = result,
                allPrices = prices,
                error = null
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Could not fetch prices: ${e.message}",
                allPrices = emptyList()
            )
        }
    }
}
