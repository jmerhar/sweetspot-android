package si.merhar.sweetspot

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import si.merhar.sweetspot.data.PriceCache
import si.merhar.sweetspot.data.PriceRepository
import si.merhar.sweetspot.data.SettingsRepository
import si.merhar.sweetspot.model.Appliance
import si.merhar.sweetspot.model.BreakdownSlot
import si.merhar.sweetspot.model.HourlyPrice
import si.merhar.sweetspot.model.WindowResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.util.UUID
import kotlin.math.floor

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

class SweetSpotViewModel(application: Application) : AndroidViewModel(application) {

    private val priceCache = PriceCache(application)
    private val settingsRepository = SettingsRepository(application)

    private val _uiState = MutableStateFlow(
        UiState(
            zoneId = settingsRepository.getZoneId(),
            isUsingDefaultZone = settingsRepository.isUsingDefaultZone(),
            appliances = settingsRepository.getAppliances()
        )
    )
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun onDurationChanged(hours: Int, minutes: Int) {
        _uiState.value = _uiState.value.copy(durationHours = hours, durationMinutes = minutes)
    }

    fun formatDuration(hours: Int, minutes: Int): String {
        return when {
            hours == 0 -> "${minutes}m"
            minutes == 0 -> "${hours}h"
            else -> "${hours}h ${minutes}m"
        }
    }

    fun onShowSettings() {
        _uiState.value = _uiState.value.copy(showSettings = true)
    }

    fun onHideSettings() {
        _uiState.value = _uiState.value.copy(
            showSettings = false,
            appliances = settingsRepository.getAppliances()
        )
    }

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

    fun onQuickDuration(hours: Int, minutes: Int) {
        _uiState.value = _uiState.value.copy(
            durationHours = hours,
            durationMinutes = minutes,
            resultLabel = formatDuration(hours, minutes)
        )
        onFindClicked()
    }

    fun onApplianceDuration(appliance: Appliance) {
        val label = "${appliance.name} \u00b7 ${formatDuration(appliance.durationHours, appliance.durationMinutes)}"
        _uiState.value = _uiState.value.copy(
            durationHours = appliance.durationHours,
            durationMinutes = appliance.durationMinutes,
            resultLabel = label
        )
        onFindClicked()
    }

    fun onClearResult() {
        _uiState.value = _uiState.value.copy(
            result = null,
            resultLabel = null,
            allPrices = emptyList(),
            error = null
        )
    }

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
    }

    fun onUpdateAppliance(appliance: Appliance) {
        val updated = _uiState.value.appliances.map {
            if (it.id == appliance.id) appliance else it
        }
        settingsRepository.setAppliances(updated)
        _uiState.value = _uiState.value.copy(appliances = updated)
    }

    fun onDeleteAppliance(id: String) {
        val updated = _uiState.value.appliances.filter { it.id != id }
        settingsRepository.setAppliances(updated)
        _uiState.value = _uiState.value.copy(appliances = updated)
    }

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
            try {
                val repository = PriceRepository(priceCache, _uiState.value.zoneId)
                val prices = repository.getPrices()

                if (prices.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No price data available for the next 24 hours.",
                        allPrices = emptyList()
                    )
                    return@launch
                }

                val result = findCheapestWindow(prices, durationHours)

                if (result == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Not enough price data to cover $durationLabel. Only ${prices.size} hour(s) of data available.",
                        allPrices = prices
                    )
                    return@launch
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

    private fun findCheapestWindow(prices: List<HourlyPrice>, durationHours: Double): WindowResult? {
        val count = prices.size
        if (count == 0) return null

        val fullHours = floor(durationHours).toInt()
        val fractional = durationHours - fullHours

        val slotsNeeded = fullHours + if (fractional > 0) 1 else 0

        if (count < slotsNeeded) return null

        var bestCost: Double? = null
        var bestStart = 0

        for (i in 0..count - slotsNeeded) {
            var cost = 0.0

            for (j in 0 until fullHours) {
                cost += prices[i + j].price
            }

            if (fractional > 0 && (i + fullHours) < count) {
                cost += fractional * prices[i + fullHours].price
            }

            if (bestCost == null || cost < bestCost) {
                bestCost = cost
                bestStart = i
            }
        }

        val breakdown = mutableListOf<BreakdownSlot>()
        for (j in 0 until fullHours) {
            val slot = prices[bestStart + j]
            breakdown.add(
                BreakdownSlot(
                    time = slot.time,
                    price = slot.price,
                    fraction = 1.0,
                    cost = slot.price
                )
            )
        }

        if (fractional > 0) {
            val slot = prices[bestStart + fullHours]
            breakdown.add(
                BreakdownSlot(
                    time = slot.time,
                    price = slot.price,
                    fraction = fractional,
                    cost = fractional * slot.price
                )
            )
        }

        val startTime = prices[bestStart].time
        val endTime = startTime.plusSeconds((durationHours * 3600).toLong())

        return WindowResult(
            startTime = startTime,
            endTime = endTime,
            totalCost = bestCost!!,
            avgPrice = if (durationHours > 0) bestCost / durationHours else 0.0,
            breakdown = breakdown
        )
    }
}
