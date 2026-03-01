package si.merhar.sweetspot

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import si.merhar.sweetspot.data.PriceCache
import si.merhar.sweetspot.data.PriceRepository
import si.merhar.sweetspot.model.BreakdownSlot
import si.merhar.sweetspot.model.HourlyPrice
import si.merhar.sweetspot.model.WindowResult
import si.merhar.sweetspot.util.parseDuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.floor

data class UiState(
    val durationInput: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val result: WindowResult? = null,
    val allPrices: List<HourlyPrice> = emptyList()
)

class SweetSpotViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PriceRepository(PriceCache(application))

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun onDurationChanged(input: String) {
        _uiState.value = _uiState.value.copy(durationInput = input)
    }

    fun onFindClicked() {
        val input = _uiState.value.durationInput
        val durationHours = parseDuration(input)

        if (durationHours == null) {
            _uiState.value = _uiState.value.copy(
                error = "Invalid duration. Try something like \"2h 30m\", \"4h\", \"90m\", or \"2.5\".",
                result = null,
                allPrices = emptyList()
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null, result = null)

        viewModelScope.launch(Dispatchers.IO) {
            try {
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
                        error = "Not enough price data to cover $input. Only ${prices.size} hour(s) of data available.",
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
