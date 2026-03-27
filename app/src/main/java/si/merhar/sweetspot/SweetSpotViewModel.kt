package si.merhar.sweetspot

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import si.merhar.sweetspot.data.api.PriceFetcherFactory
import si.merhar.sweetspot.data.api.defaultPriceFetcherFactory
import si.merhar.sweetspot.data.cache.FilePriceCache
import si.merhar.sweetspot.data.cache.PriceCache
import si.merhar.sweetspot.data.repository.CountryDetector
import si.merhar.sweetspot.data.repository.PriceRepository
import si.merhar.sweetspot.data.repository.SettingsRepository
import si.merhar.sweetspot.model.Appliance
import si.merhar.sweetspot.model.Countries
import si.merhar.sweetspot.model.Country
import si.merhar.sweetspot.model.PriceSlot
import si.merhar.sweetspot.model.PriceZone
import si.merhar.sweetspot.model.WindowResult
import si.merhar.sweetspot.util.findCheapestWindow
import si.merhar.sweetspot.util.formatDuration
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
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

/**
 * Distinguishes inline validation errors (shown as [ErrorBox]) from
 * transient network errors (shown as a snackbar).
 */
sealed interface AppError {
    val message: String

    /** Validation or data error shown inline below the form. */
    data class Validation(override val message: String) : AppError

    /** Network or fetch error shown as a snackbar. Unique [id] ensures consecutive identical messages still trigger the snackbar. */
    data class Network(override val message: String, val id: Long = System.nanoTime()) : AppError
}

/**
 * UI state for the main screen.
 *
 * @property durationHours Selected hours component of the duration (0–24).
 * @property durationMinutes Selected minutes component of the duration (0, 5, 10, ..., 55).
 * @property isLoading Whether a price fetch is in progress.
 * @property error Error to display, or `null` if none.
 * @property result The cheapest-window result, or `null` if no search has been performed.
 * @property resultLabel Label shown in the results screen top bar (e.g. "Washing machine · 2h 30m").
 * @property allPrices All price slots for the next 24h, used by the bar chart.
 * @property priceSource Name of the data source (e.g. "ENTSO-E", "EnergyZero"), or `null` if no result.
 * @property showSettings Whether the settings screen is currently visible.
 * @property timeZoneId Active timezone for price date boundaries and display.
 * @property isUsingDefaultTimezone Whether the timezone is the zone-derived default (vs. user-selected).
 * @property appliances User-configured appliances with preset durations.
 * @property countryCode ISO code of the selected country.
 * @property priceZone The resolved price zone for fetching prices, or `null` if a multi-zone country has no selection yet.
 * @property sourceOrder Ordered list of all source IDs for display/priority, or `null` for zone defaults.
 * @property disabledSources Set of source IDs that are disabled, or empty if all enabled.
 * @property countries All supported countries for the country picker.
 */
data class UiState(
    val durationHours: Int = 1,
    val durationMinutes: Int = 0,
    val isLoading: Boolean = false,
    val error: AppError? = null,
    val result: WindowResult? = null,
    val resultLabel: String? = null,
    val allPrices: List<PriceSlot> = emptyList(),
    val priceSource: String? = null,
    val showSettings: Boolean = false,
    val timeZoneId: ZoneId = ZoneId.systemDefault(),
    val isUsingDefaultTimezone: Boolean = true,
    val appliances: List<Appliance> = emptyList(),
    val countryCode: String = Countries.defaultCountry().code,
    val priceZone: PriceZone? = Countries.defaultCountry().zones.first(),
    val sourceOrder: List<String>? = null,
    val disabledSources: Set<String> = emptySet(),
    val countries: List<Country> = Countries.all
)

/**
 * ViewModel for the SweetSpot app.
 *
 * Owns all UI state via [uiState]. Handles duration selection, price fetching,
 * cheapest-window calculation, timezone configuration, country/zone selection,
 * and appliance CRUD.
 *
 * @param application Application context.
 * @param priceFetcherFactory Optional factory override for testing. When `null` (production),
 *   the factory is created dynamically from the current source order.
 * @param priceCache Cache for raw price JSON.
 * @param ioDispatcher Dispatcher for IO-bound work (injectable for testing).
 */
class SweetSpotViewModel @JvmOverloads constructor(
    application: Application,
    private val priceFetcherFactory: PriceFetcherFactory? = null,
    private val priceCache: PriceCache = FilePriceCache(application),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)

    private var fetchJob: Job? = null
    private var refreshJob: Job? = null

    private val _uiState = MutableStateFlow(
        UiState(
            timeZoneId = settingsRepository.getTimeZoneId(),
            isUsingDefaultTimezone = settingsRepository.isUsingDefaultTimezone(),
            appliances = settingsRepository.getAppliances(),
            countryCode = settingsRepository.getCountryCode(),
            priceZone = settingsRepository.getResolvedPriceZone(),
            sourceOrder = settingsRepository.getSourceOrder(),
            disabledSources = settingsRepository.getDisabledSources(),
            countries = countriesWithDetectedFirst(application)
        )
    )

    /** Observable UI state. */
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    override fun onCleared() {
        super.onCleared()
        stopResultRefresh()
    }

    /**
     * Updates the selected duration from the picker.
     *
     * @param hours Hours component (0–24).
     * @param minutes Minutes component (0–55, in 5-minute steps).
     */
    fun onDurationChanged(hours: Int, minutes: Int) {
        _uiState.update { it.copy(durationHours = hours, durationMinutes = minutes) }
    }

    /** Opens the settings screen. */
    fun onShowSettings() {
        _uiState.update { it.copy(showSettings = true) }
    }

    /** Closes the settings screen and refreshes the appliance list from storage. */
    fun onHideSettings() {
        val appliances = settingsRepository.getAppliances()
        _uiState.update { it.copy(showSettings = false, appliances = appliances) }
        syncAppliancesToWear(appliances)
    }

    /**
     * Updates the timezone selection.
     *
     * @param timeZoneId The chosen timezone, or `null` to revert to zone-derived default.
     */
    fun onTimezoneSelected(timeZoneId: ZoneId?) {
        if (timeZoneId == null) {
            settingsRepository.clearTimeZoneId()
            _uiState.update {
                it.copy(timeZoneId = settingsRepository.getTimeZoneId(), isUsingDefaultTimezone = true)
            }
        } else {
            settingsRepository.setTimeZoneId(timeZoneId)
            _uiState.update { it.copy(timeZoneId = timeZoneId, isUsingDefaultTimezone = false) }
        }
    }

    /**
     * Handles country selection from the settings picker.
     *
     * Saves the country code, resolves the first zone for that country,
     * updates timezone to the zone's timezone (unless manually overridden),
     * and syncs settings to the watch.
     *
     * @param code ISO 3166-1 alpha-2 country code.
     */
    fun onCountrySelected(code: String) {
        settingsRepository.setCountryCode(code)
        settingsRepository.setPriceZoneId(null)
        val zone = settingsRepository.getResolvedPriceZone()
        val timeZoneId = settingsRepository.getTimeZoneId()
        _uiState.update {
            it.copy(
                countryCode = code,
                priceZone = zone,
                timeZoneId = timeZoneId,
                sourceOrder = null,
                disabledSources = emptySet()
            )
        }
        syncSettingsToWear()
    }

    /**
     * Handles price zone selection within the current country.
     *
     * Saves the zone ID, updates timezone to the zone's timezone (unless manually overridden),
     * and syncs settings to the watch.
     *
     * @param priceZoneId The [PriceZone.id] selected.
     */
    fun onPriceZoneSelected(priceZoneId: String) {
        settingsRepository.setPriceZoneId(priceZoneId)
        val zone = settingsRepository.getResolvedPriceZone()
        val timeZoneId = settingsRepository.getTimeZoneId()
        _uiState.update {
            it.copy(
                priceZone = zone,
                timeZoneId = timeZoneId
            )
        }
        syncSettingsToWear()
    }

    /**
     * Updates the data source display/priority order.
     *
     * Saves the ordered list of all source IDs and syncs to the watch.
     *
     * @param order Ordered list of all source IDs (enabled and disabled).
     */
    fun onSourceOrderChanged(order: List<String>) {
        settingsRepository.setSourceOrder(order)
        _uiState.update { it.copy(sourceOrder = order) }
        syncSettingsToWear()
    }

    /**
     * Updates which data sources are disabled.
     *
     * @param disabled Set of source IDs to disable.
     */
    fun onDisabledSourcesChanged(disabled: Set<String>) {
        settingsRepository.setDisabledSources(disabled)
        _uiState.update { it.copy(disabledSources = disabled) }
        syncSettingsToWear()
    }

    /** Resets the data source order and disabled set to zone defaults and syncs to the watch. */
    fun onResetSourceOrder() {
        settingsRepository.clearSourceOrder()
        settingsRepository.clearDisabledSources()
        _uiState.update { it.copy(sourceOrder = null, disabledSources = emptySet()) }
        syncSettingsToWear()
    }

    /**
     * Handles a quick-duration button tap. Sets the picker values and immediately triggers a search.
     *
     * @param hours Hours component of the quick duration.
     * @param minutes Minutes component of the quick duration.
     */
    fun onQuickDuration(hours: Int, minutes: Int) {
        val res = getApplication<Application>().resources
        _uiState.update {
            it.copy(
                durationHours = hours,
                durationMinutes = minutes,
                resultLabel = formatDuration(hours, minutes, res)
            )
        }
        onFindClicked()
    }

    /**
     * Handles an appliance chip tap. Sets the picker to the appliance's duration
     * and immediately triggers a search.
     *
     * @param appliance The tapped appliance.
     */
    fun onApplianceDuration(appliance: Appliance) {
        val res = getApplication<Application>().resources
        val label = "${appliance.name} \u00b7 ${formatDuration(appliance.durationHours, appliance.durationMinutes, res)}"
        _uiState.update {
            it.copy(
                durationHours = appliance.durationHours,
                durationMinutes = appliance.durationMinutes,
                resultLabel = label
            )
        }
        onFindClicked()
    }

    /** Clears the current result and returns to the form screen. */
    fun onClearResult() {
        stopResultRefresh()
        _uiState.update {
            it.copy(result = null, resultLabel = null, allPrices = emptyList(), priceSource = null, error = null)
        }
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
     * Recalculates the cheapest window using already-fetched prices and the current time.
     *
     * Filters [UiState.allPrices] to exclude elapsed slots, then re-runs [findCheapestWindow].
     * Updates both [UiState.result] and [UiState.allPrices] so the chart and summary stay current.
     */
    private fun recalculateResult() {
        val state = _uiState.value
        val prices = state.allPrices
        if (prices.isEmpty() || state.result == null) return

        val timeZoneId = state.timeZoneId
        val now = ZonedDateTime.now(timeZoneId)
        val futurePrices = prices.filter {
            it.time.plusMinutes(it.durationMinutes.toLong()).isAfter(now)
        }

        val durationHours = state.durationHours + state.durationMinutes / 60.0
        val result = if (futurePrices.isNotEmpty()) {
            findCheapestWindow(futurePrices, durationHours, now)
        } else null

        _uiState.update { it.copy(result = result, allPrices = futurePrices) }
    }

    /**
     * Clears all cached price data if the API cooldown has elapsed.
     *
     * @return A user-facing message: confirmation if cleared, or cooldown warning.
     */
    fun onClearCache(): String {
        val remaining = priceCache.cooldownRemainingMs(PriceRepository.COOLDOWN_MS)
        val app = getApplication<Application>()
        return if (remaining > 0) {
            val minutes = (remaining / 60_000).toInt() + 1
            app.resources.getQuantityString(R.plurals.error_cooldown, minutes, minutes)
        } else {
            priceCache.clear()
            app.getString(R.string.snackbar_cache_cleared)
        }
    }

    /**
     * Re-fetches prices and recalculates the cheapest window from the results screen.
     *
     * If the API cooldown is still active, shows a "try again in X minutes" snackbar.
     * Otherwise clears the zone cache, preserves the existing result, and re-runs
     * the fetch-and-find flow.
     */
    fun onRefreshResults() {
        val remaining = priceCache.cooldownRemainingMs(PriceRepository.COOLDOWN_MS)
        val app = getApplication<Application>()
        if (remaining > 0) {
            val minutes = (remaining / 60_000).toInt() + 1
            _uiState.update { it.copy(error = AppError.Network(app.resources.getQuantityString(R.plurals.error_cooldown, minutes, minutes))) }
            return
        }

        val state = _uiState.value
        val priceZone = state.priceZone ?: return
        priceCache.clearForZone(priceZone.id)

        val h = state.durationHours
        val m = state.durationMinutes
        val durationHours = h + m / 60.0
        val durationLabel = formatDuration(h, m, app.resources)

        _uiState.update { it.copy(isLoading = true, error = null) }

        val timeZoneId = state.timeZoneId
        stopResultRefresh()
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch(ioDispatcher) {
            fetchAndFind(durationHours, durationLabel, timeZoneId, priceZone)
        }
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
        _uiState.update { it.copy(appliances = updated) }
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
        _uiState.update { it.copy(appliances = updated) }
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
        _uiState.update { it.copy(appliances = updated) }
        syncAppliancesToWear(updated)
    }

    /**
     * Pushes the current appliance list to the Wearable Data Layer so
     * the Wear OS companion app receives it.
     *
     * Silently ignores failures (e.g. Play Services unavailable) since
     * watch sync is best-effort and should never crash the phone app.
     *
     * @param appliances The appliance list to sync.
     */
    private fun syncAppliancesToWear(appliances: List<Appliance>) {
        try {
            val json = Json.encodeToString(appliances)
            val request = PutDataMapRequest.create("/appliances").apply {
                dataMap.putString("json", json)
                dataMap.putLong("ts", System.currentTimeMillis())
            }.asPutDataRequest().setUrgent()
            Wearable.getDataClient(getApplication()).putDataItem(request)
        } catch (_: Exception) {
            // Best-effort: watch sync should not crash the phone app
        }
    }

    /**
     * Called before the user changes the per-app language in Settings.
     *
     * Accepts the new [languageTag] explicitly because [AppCompatDelegate.getApplicationLocales]
     * still returns the old value at this point. Syncs the tag to the watch via the Data Layer.
     * The actual locale switch is handled by [AppCompatDelegate.setApplicationLocales] in the UI.
     */
    fun onLanguageChanged(languageTag: String) {
        syncSettingsToWear(languageTag = languageTag)
    }

    /**
     * Pushes country and zone settings to the Wearable Data Layer so the
     * watch can use the same price zone as the phone.
     *
     * @param languageTag Explicit language tag override. When `null`, reads from
     *   [AppCompatDelegate.getApplicationLocales] (correct for non-language syncs).
     *
     * Silently ignores failures since watch sync is best-effort.
     */
    private fun syncSettingsToWear(languageTag: String? = null) {
        try {
            val state = _uiState.value
            val priceZone = state.priceZone ?: return
            val resolvedTag = languageTag ?: run {
                val locales = AppCompatDelegate.getApplicationLocales()
                if (locales.isEmpty) "" else locales.toLanguageTags()
            }
            val request = PutDataMapRequest.create("/settings").apply {
                dataMap.putString("country_code", state.countryCode)
                dataMap.putString("price_zone_id", priceZone.id)
                dataMap.putString("source_order", state.sourceOrder?.let { Json.encodeToString(it) } ?: "")
                dataMap.putString("disabled_sources", state.disabledSources.takeIf { it.isNotEmpty() }?.let { Json.encodeToString(it) } ?: "")
                dataMap.putString("language", resolvedTag)
                dataMap.putLong("ts", System.currentTimeMillis())
            }.asPutDataRequest().setUrgent()
            Wearable.getDataClient(getApplication()).putDataItem(request)
        } catch (_: Exception) {
            // Best-effort: watch sync should not crash the phone app
        }
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
            _uiState.update {
                it.copy(
                    error = AppError.Validation(getApplication<Application>().getString(R.string.error_zero_duration)),
                    result = null,
                    allPrices = emptyList()
                )
            }
            return
        }

        val priceZone = _uiState.value.priceZone
        if (priceZone == null) {
            _uiState.update {
                it.copy(
                    error = AppError.Validation(getApplication<Application>().getString(R.string.error_no_zone)),
                    result = null,
                    allPrices = emptyList()
                )
            }
            return
        }

        val durationHours = h + m / 60.0
        val durationLabel = formatDuration(h, m, getApplication<Application>().resources)

        _uiState.update {
            it.copy(
                isLoading = true,
                error = null,
                result = null,
                resultLabel = it.resultLabel ?: durationLabel
            )
        }

        val timeZoneId = _uiState.value.timeZoneId
        stopResultRefresh()
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch(ioDispatcher) {
            fetchAndFind(durationHours, durationLabel, timeZoneId, priceZone)
        }
    }

    /**
     * Fetches prices from the repository and runs the cheapest-window algorithm.
     *
     * Called on [Dispatchers.IO]. Updates [_uiState] with the result or an error.
     * On success, starts the periodic refresh via [startResultRefresh].
     *
     * @param durationHours Duration in decimal hours.
     * @param durationLabel Human-readable duration label for error messages.
     * @param timeZoneId Timezone snapshot captured before the IO dispatch.
     * @param priceZone The price zone to fetch data for.
     */
    private fun fetchAndFind(durationHours: Double, durationLabel: String, timeZoneId: ZoneId, priceZone: PriceZone) {
        try {
            val state = _uiState.value
            val enabledOrder = state.sourceOrder?.filter { it !in state.disabledSources }
            val factory = priceFetcherFactory
                ?: defaultPriceFetcherFactory(BuildConfig.ENTSOE_API_TOKEN, enabledOrder)
            val fetcher = factory.create(priceZone)
            val repository = PriceRepository(priceCache, timeZoneId, fetcher, cacheKey = priceZone.id)
            val priceResult = repository.getPrices()
            val prices = priceResult.prices

            if (prices.isEmpty()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = AppError.Validation(getApplication<Application>().getString(R.string.error_no_data)),
                        allPrices = emptyList(),
                        priceSource = null
                    )
                }
                return
            }

            val now = ZonedDateTime.now(timeZoneId)
            val result = findCheapestWindow(prices, durationHours, now)

            if (result == null) {
                val coverageHours = prices.sumOf { it.durationMinutes.toLong() } / 60
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = AppError.Validation(getApplication<Application>().resources.getQuantityString(R.plurals.error_not_enough_data, coverageHours.toInt(), durationLabel, coverageHours)),
                        allPrices = prices
                    )
                }
                return
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    result = result,
                    allPrices = prices,
                    priceSource = priceResult.source,
                    error = null
                )
            }
            startResultRefresh()
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = AppError.Network(getApplication<Application>().getString(R.string.error_network, e.message ?: "")),
                    allPrices = emptyList(),
                    priceSource = null
                )
            }
        }
    }
}

/**
 * Returns the country list with the auto-detected country moved to the top.
 *
 * @param application Application context for [CountryDetector].
 * @return [Countries.all] with the detected country first, rest in alphabetical order.
 */
private fun countriesWithDetectedFirst(application: Application): List<Country> {
    val detectedCode = CountryDetector.detect(application).code
    val (detected, rest) = Countries.all.partition { it.code == detectedCode }
    return detected + rest
}
