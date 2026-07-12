package today.sweetspot

import android.app.Activity
import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import today.sweetspot.data.api.PriceFetcherFactory
import today.sweetspot.data.api.defaultPriceFetcherFactory
import today.sweetspot.data.billing.BillingRepository
import today.sweetspot.data.billing.PlayBillingRepository
import today.sweetspot.data.cache.FilePriceCache
import today.sweetspot.data.cache.FileTariffCache
import today.sweetspot.data.cache.PriceCache
import today.sweetspot.data.repository.CountryDetector
import today.sweetspot.data.repository.EvVehicleRepository
import today.sweetspot.data.repository.PriceRepository
import today.sweetspot.data.repository.SettingsRepository
import today.sweetspot.data.repository.TariffRepository
import today.sweetspot.data.stats.FileStatsCollector
import today.sweetspot.data.stats.HttpStatsPoster
import today.sweetspot.data.stats.StatsCollector
import today.sweetspot.data.stats.StatsPoster
import today.sweetspot.data.stats.StatsRecord
import today.sweetspot.data.stats.StatsReporter
import today.sweetspot.model.Appliance
import today.sweetspot.model.Countries
import today.sweetspot.model.Country
import today.sweetspot.model.EvSpec
import today.sweetspot.model.EvVehicle
import today.sweetspot.model.PriceSlot
import today.sweetspot.model.PriceZone
import today.sweetspot.model.SupplierTariffs
import today.sweetspot.model.WindowResult
import today.sweetspot.util.AllInPricing
import today.sweetspot.util.UiText
import today.sweetspot.util.findWindowAlternatives
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
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

/**
 * Distinguishes inline validation errors (shown as [ErrorBox]) from
 * transient network errors (shown as a snackbar).
 */
sealed interface AppError {
    val message: UiText

    /** Validation or data error shown inline below the form. */
    data class Validation(override val message: UiText) : AppError

    /** Network or fetch error shown as a snackbar. Unique [id] ensures consecutive identical messages still trigger the snackbar. */
    data class Network(override val message: UiText, val id: Long = System.nanoTime()) : AppError
}

/** Maximum number of vehicle picker results shown at once. */
private const val EV_SEARCH_LIMIT = 50

/** A cached tariff older than this is still used, but the results page flags it as possibly out of date. */
private const val TARIFF_STALENESS_MS = 14L * 24 * 60 * 60 * 1000

/**
 * The currency all spot prices are expressed in. The entire price pipeline (every [PriceFetcher],
 * [today.sweetspot.model.PriceSlot], and [today.sweetspot.util.formatPrice]) assumes EUR. The all-in
 * transform adds the feed's surcharge and taxes to the spot price, so it is only valid — and only
 * offered — when the feed's currency matches this. A feed in any other currency is gated off until the
 * app gains end-to-end multi-currency support (a spot price would need to carry its own currency).
 */
private const val SPOT_CURRENCY = "EUR"

/**
 * UI state for the main screen.
 *
 * @property durationHours Selected hours component of the duration (0–24).
 * @property durationMinutes Selected minutes component of the duration (0, 5, 10, ..., 55).
 * @property isLoading Whether a price fetch is in progress.
 * @property error Error to display, or `null` if none.
 * @property result The currently-displayed window, or `null` if no search has been performed.
 *           This is the cheapest window initially, but the "earlier"/"cheaper" buttons can move
 *           it to an earlier (and costlier) alternative — see [windowAlternatives].
 * @property resultLabel Label shown in the results screen top bar (e.g. "Washing machine · 2h 30m"),
 *           as deferred [UiText] resolved by the UI in the current locale.
 * @property windowAlternatives Progressively-earlier window options, cheapest first (index 0).
 *           [result] is `windowAlternatives[windowOffset]`. Empty when there is no result.
 * @property windowOffset Index into [windowAlternatives] of the currently-displayed window
 *           (0 = cheapest). Advanced by "earlier", reduced by "cheaper".
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
 * @property showStatsPrompt Whether the stats opt-in prompt dialog should be shown.
 * @property isStatsEnabled Whether API stats collection is enabled.
 * @property isTrialExpired Whether the 14-day free trial has expired.
 * @property isUnlocked Whether the app has been unlocked via in-app purchase.
 * @property trialDaysRemaining Number of trial days remaining (0–14).
 * @property showPaywall Whether the paywall screen should block the app.
 * @property productPrice Localized price string for the unlock purchase (e.g. "€2.99"), or `null` if not loaded.
 * @property showThankYou Whether the thank-you dialog should be shown after a successful purchase.
 * @property devOptionsEnabled Whether hidden developer options are visible.
 * @property isDevUnlocked Whether the developer-only subscription bypass is enabled.
 * @property isCooldownDisabled Whether the API fetch cooldown is bypassed (developer option).
 * @property timeOverrideMs Developer time override as epoch millis, or `null` when using real time.
 * @property now The current effective time, reflecting any active time override. Used by the UI for relative time display.
 * @property useProductionLogo Whether to show the production logo instead of the debug logo (debug builds only).
 * @property themeMode The user's preferred theme mode.
 * @property evHomeChargerKw The user's home charger output in kW (set once in Settings).
 * @property evDefaultTargetSoc Default target state of charge (%) used to prefill the charge prompt.
 * @property evLastCurrentSoc Last-used current state of charge (%), used to prefill the charge prompt.
 * @property deadlineEnabled Whether the optional "ready by" deadline is active for searches.
 * @property deadlineHour Hour-of-day component of the "ready by" deadline (0–23).
 * @property deadlineMinute Minute component of the "ready by" deadline (0–59).
 * @property searchDeadline The deadline resolved at search time, or `null` when disabled.
 * @property searchPowerKw Load power (kW) used to scale displayed costs, or `null` for per-1-kW.
 */
data class UiState(
    val durationHours: Int = 1,
    val durationMinutes: Int = 0,
    val isLoading: Boolean = false,
    val error: AppError? = null,
    val result: WindowResult? = null,
    val resultLabel: UiText? = null,
    val windowAlternatives: List<WindowResult> = emptyList(),
    val windowOffset: Int = 0,
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
    val countries: List<Country> = Countries.all,
    val showStatsPrompt: Boolean = false,
    val isStatsEnabled: Boolean = false,
    val isTrialExpired: Boolean = false,
    val isUnlocked: Boolean = false,
    val trialDaysRemaining: Int = 14,
    val showPaywall: Boolean = false,
    val productPrice: String? = null,
    val showThankYou: Boolean = false,
    val devOptionsEnabled: Boolean = false,
    val isDevUnlocked: Boolean = false,
    val isCooldownDisabled: Boolean = false,
    val timeOverrideMs: Long? = null,
    val now: ZonedDateTime = ZonedDateTime.now(),
    val useProductionLogo: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    // --- EV charging ---
    val evHomeChargerKw: Double = 11.0,
    val evDefaultTargetSoc: Int = 80,
    val evLastCurrentSoc: Int = 20,
    // --- Universal "ready by" deadline ---
    val deadlineEnabled: Boolean = false,
    val deadlineHour: Int = 7,
    val deadlineMinute: Int = 0,
    /**
     * The "ready by" deadline resolved at search time (the next occurrence of [deadlineHour]:
     * [deadlineMinute]), or `null` when disabled. Threaded into the window finder and the periodic
     * refresh so the chosen window finishes in time.
     */
    val searchDeadline: ZonedDateTime? = null,
    /**
     * Load power (kW) for the current search, used only to scale displayed costs. `null` keeps the
     * per-1-kW behaviour (and disclaimer). Set from an appliance's [Appliance.powerKw] or, for EV
     * charging, the effective charging power.
     */
    val searchPowerKw: Double? = null,
    // --- All-in price ---
    /** Whether the user has enabled the approximate all-in consumer price display. */
    val allInEnabled: Boolean = false,
    /** Chosen supplier id (from [allInTariff]), or `null` if none selected. */
    val supplierId: String? = null,
    /** Manual per-kWh surcharge override (ex-VAT); when set, takes precedence over the chosen supplier. */
    val manualSurcharge: Double? = null,
    /** The loaded tariff feed for the current country (cached or freshly fetched), or `null`. */
    val allInTariff: SupplierTariffs? = null,
    /** Epoch millis when [allInTariff] was fetched; drives the stale warning. */
    val tariffFetchedAtMs: Long? = null,
    /** True when the current result was computed with the all-in transform applied. */
    val allInApplied: Boolean = false,
    /** Name of the supplier whose surcharge was applied (null for a manual override or when not applied). */
    val allInSupplierName: String? = null,
    /** True when the applied tariff is older than the staleness cutoff (shows an "out of date" warning). */
    val allInStale: Boolean = false
) {
    /**
     * Whether all-in pricing is offered for the selected country: a usable tariff feed exists **and**
     * its currency matches the spot-price currency ([SPOT_CURRENCY]). A feed in another currency can't
     * be combined with EUR spot prices, so it is gated off (section hidden) rather than shown wrong.
     */
    val allInSupported: Boolean get() = allInTariff?.let { it.usable && it.currency == SPOT_CURRENCY } == true

    /** Currency of the loaded tariff (for labelling the surcharge field); falls back to the spot currency. */
    val allInCurrency: String get() = allInTariff?.currency ?: SPOT_CURRENCY
}

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
 * @param statsCollector Optional stats collector override for testing.
 * @param ioDispatcher Dispatcher for IO-bound work (injectable for testing).
 * @param billingRepository Optional billing repository override. When `null` (production),
 *   creates a [PlayBillingRepository]. Pass a fake for tests.
 * @param evVehicleRepositoryOverride Optional EV database override for testing. When `null`
 *   (production), the bundled `ev-vehicles.json` asset is loaded lazily on first use.
 * @param statsPoster Optional stats HTTP poster override for testing. When `null` (production),
 *   the real [HttpStatsPoster] is used. A fake lets tests drive stats reporting without a network.
 * @param watchStatsBridgeOverride Optional [WatchStatsBridge] override for testing. When `null`
 *   (production), a real [WearableStatsBridge] backed by Google Play Services is used.
 * @param tariffRepositoryOverride Optional [TariffRepository] override for testing. When `null`
 *   (production), a real one backed by [FileTariffCache] and the remote tariff feed is used.
 */
class SweetSpotViewModel @JvmOverloads constructor(
    application: Application,
    private val priceFetcherFactory: PriceFetcherFactory? = null,
    private val priceCache: PriceCache = FilePriceCache(application),
    private val statsCollector: StatsCollector = FileStatsCollector(application.cacheDir),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val billingRepository: BillingRepository? = null,
    private val evVehicleRepositoryOverride: EvVehicleRepository? = null,
    statsPoster: StatsPoster? = null,
    watchStatsBridgeOverride: WatchStatsBridge? = null,
    tariffRepositoryOverride: TariffRepository? = null
) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)

    /** Provides the all-in tariff feed for the current country (real impl, or a fake in tests). */
    private val tariffRepository: TariffRepository =
        tariffRepositoryOverride ?: TariffRepository(FileTariffCache(application))
    private val statsReporter = StatsReporter(
        statsCollector,
        application.getSharedPreferences("sweetspot_stats", android.content.Context.MODE_PRIVATE),
        BuildConfig.VERSION_NAME,
        languageProvider = {
            val locales = AppCompatDelegate.getApplicationLocales()
            if (locales.isEmpty) "" else locales.toLanguageTags()
        },
        statusProvider = {
            when {
                settingsRepository.isUnlocked() -> "subscribed"
                settingsRepository.isTrialExpired() -> "expired"
                else -> "trial"
            }
        },
        poster = statsPoster ?: HttpStatsPoster(BuildConfig.VERSION_NAME)
    )

    /** Receives watch API-reliability stats via the Data Layer (real impl, or a fake in tests). */
    private val watchStatsBridge: WatchStatsBridge =
        watchStatsBridgeOverride ?: WearableStatsBridge(application)

    private var fetchJob: Job? = null
    private var refreshJob: Job? = null

    /** Whether [evVehicleRepository] has finished loading (set after the eager load in [init]). */
    @Volatile
    private var evDbReady: Boolean = false

    /**
     * The bundled EV vehicle database, backing the "add vehicle" picker in Settings. In production
     * it is parsed from the `ev-vehicles.json` asset; tests may inject a small fixture via the
     * constructor. Loaded eagerly off the main thread in [init] so [searchEvVehicles] is synchronous.
     */
    private val evVehicleRepository: EvVehicleRepository by lazy {
        evVehicleRepositoryOverride ?: EvVehicleRepository(
            getApplication<Application>().assets.open("ev-vehicles.json")
                .bufferedReader().use { it.readText() }
        )
    }

    private val _uiState = MutableStateFlow(
        UiState(
            timeZoneId = settingsRepository.getTimeZoneId(),
            isUsingDefaultTimezone = settingsRepository.isUsingDefaultTimezone(),
            appliances = settingsRepository.getAppliances(),
            countryCode = settingsRepository.getCountryCode(),
            priceZone = settingsRepository.getResolvedPriceZone(),
            sourceOrder = settingsRepository.getSourceOrder(),
            disabledSources = settingsRepository.getDisabledSources(),
            countries = countriesWithDetectedFirst(application),
            isStatsEnabled = settingsRepository.isStatsEnabled(),
            isTrialExpired = settingsRepository.isTrialExpired(),
            isUnlocked = settingsRepository.isUnlocked(),
            trialDaysRemaining = settingsRepository.trialDaysRemaining(),
            showPaywall = shouldShowPaywall(BuildConfig.DEBUG, settingsRepository.isTrialExpired(), settingsRepository.isUnlocked()),
            devOptionsEnabled = settingsRepository.isDevOptionsEnabled(),
            isDevUnlocked = settingsRepository.isDevUnlocked(),
            isCooldownDisabled = settingsRepository.isCooldownDisabled(),
            timeOverrideMs = settingsRepository.getTimeOverrideMs(),
            now = currentNow(settingsRepository.getTimeZoneId()),
            useProductionLogo = settingsRepository.isUseProductionLogo(),
            themeMode = ThemeMode.fromKey(settingsRepository.getThemeMode()),
            evHomeChargerKw = settingsRepository.getEvHomeChargerKw(),
            evDefaultTargetSoc = settingsRepository.getEvDefaultTargetSoc(),
            evLastCurrentSoc = settingsRepository.getEvLastCurrentSoc(),
            allInEnabled = settingsRepository.isAllInEnabled(),
            supplierId = settingsRepository.getSupplierId(),
            manualSurcharge = settingsRepository.getManualSurcharge()
        )
    )

    /** Observable UI state. */
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /** Active billing repository: injected fake, production PlayBillingRepository, or null for debug builds. */
    private val activeBilling: BillingRepository? = when {
        billingRepository != null -> billingRepository
        BuildConfig.DEBUG -> null
        else -> PlayBillingRepository(application, settingsRepository, viewModelScope)
    }

    init {
        // Record first launch time for stats prompt delay
        settingsRepository.getFirstLaunchMs()
        checkStatsPrompt()
        // Parse the bundled EV database off the main thread so the vehicle picker search is instant.
        viewModelScope.launch(ioDispatcher) {
            evVehicleRepository.vehicles
            evDbReady = true
        }
        // Load the all-in tariff for the current country. Cache-only unless all-in is already enabled,
        // in which case bootstrap-fetch when nothing is cached so results can apply it immediately.
        loadTariffAsync(_uiState.value.countryCode, allowNetwork = _uiState.value.allInEnabled)
        // Connect billing and observe unlock state
        activeBilling?.let { billing ->
            billing.connect()
            viewModelScope.launch {
                billing.isUnlocked.collect { unlocked ->
                    _uiState.update {
                        it.copy(
                            isUnlocked = unlocked,
                            showPaywall = shouldShowPaywall(BuildConfig.DEBUG, settingsRepository.isTrialExpired(), unlocked),
                            showThankYou = it.showThankYou || (unlocked && !it.isUnlocked)
                        )
                    }
                    syncSettingsToWear()
                }
            }
            viewModelScope.launch {
                billing.productPrice.collect { price ->
                    _uiState.update { it.copy(productPrice = price) }
                }
            }
        }
        // Listen for watch stats via the Data Layer
        watchStatsBridge.observe(::onWatchStatsReceived)
    }

    override fun onCleared() {
        super.onCleared()
        stopResultRefresh()
        activeBilling?.disconnect()
        watchStatsBridge.stop()
    }

    /**
     * Returns the effective "now" for the given timezone, respecting any active time override.
     *
     * @param timeZoneId Timezone to apply.
     */
    private fun currentNow(timeZoneId: ZoneId): ZonedDateTime {
        val overrideMs = settingsRepository.getTimeOverrideMs()
        return if (overrideMs != null) {
            Instant.ofEpochMilli(overrideMs).atZone(timeZoneId)
        } else {
            ZonedDateTime.now(timeZoneId)
        }
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
        // Bootstrap the tariff (only fetches if nothing is cached) so the all-in section can decide
        // whether the country is supported and populate the supplier picker.
        loadTariffAsync(_uiState.value.countryCode, allowNetwork = true)
    }

    /** Closes the settings screen and refreshes the appliance list from storage. */
    fun onHideSettings() {
        val appliances = settingsRepository.getAppliances()
        _uiState.update { it.copy(showSettings = false, appliances = appliances) }
        syncAppliancesToWear(appliances)
    }

    // --- All-in price ---

    /**
     * Enables/disables the all-in price display. When turning it on without a loaded tariff, kicks a
     * background fetch so the results screen can apply it.
     */
    fun onAllInEnabledChanged(enabled: Boolean) {
        settingsRepository.setAllInEnabled(enabled)
        _uiState.update { it.copy(allInEnabled = enabled) }
        if (enabled && _uiState.value.allInTariff == null) {
            loadTariffAsync(_uiState.value.countryCode, allowNetwork = true)
        }
    }

    /**
     * Selects the user's supplier and **prefills the surcharge field** with that supplier's per-kWh
     * surcharge (like the home-charger presets) — the surcharge field is the effective value, so the
     * user can then tweak it. Records the supplier id for display until the field is edited.
     *
     * @param id The chosen [today.sweetspot.model.SupplierTariff.id].
     */
    fun onSupplierSelected(id: String) {
        val surcharge = _uiState.value.allInTariff?.suppliers?.firstOrNull { it.id == id }?.surchargePerKwh
        settingsRepository.setSupplierId(id)
        settingsRepository.setManualSurcharge(surcharge)
        _uiState.update { it.copy(supplierId = id, manualSurcharge = surcharge) }
    }

    /**
     * Sets or clears the per-kWh surcharge (ex-VAT) from the custom field. Editing it means the value
     * is no longer "the supplier's", so the chosen supplier is cleared (the field is the source of truth).
     *
     * @param value Surcharge in the feed's currency per kWh, or `null` to clear it.
     */
    fun onManualSurchargeChanged(value: Double?) {
        settingsRepository.setManualSurcharge(value)
        settingsRepository.setSupplierId(null)
        _uiState.update { it.copy(manualSurcharge = value, supplierId = null) }
    }

    /**
     * Loads the all-in tariff for a country into state, off the main thread.
     *
     * @param countryCode Country whose feed to load.
     * @param allowNetwork When false, only a cached copy is used (no fetch). When true, a cached copy
     *   is served if present and only fetched when absent — unless [force] is set.
     * @param force When true (with [allowNetwork]), always re-fetch (used on an explicit country change).
     */
    private fun loadTariffAsync(countryCode: String, allowNetwork: Boolean, force: Boolean = false) {
        viewModelScope.launch(ioDispatcher) {
            val resolved = when {
                allowNetwork && force -> tariffRepository.refresh(countryCode)
                allowNetwork -> tariffRepository.cached(countryCode) ?: tariffRepository.refresh(countryCode)
                else -> tariffRepository.cached(countryCode)
            }
            // Guard against a late load landing after the user switched countries: only apply if the
            // selected country still matches the one we loaded for.
            _uiState.update {
                if (it.countryCode == countryCode) {
                    it.copy(allInTariff = resolved?.tariff, tariffFetchedAtMs = resolved?.fetchedAtMs)
                } else {
                    it
                }
            }
        }
    }

    // --- EV charging ---

    /**
     * Searches the bundled vehicle database for the "add vehicle" picker in Settings.
     *
     * Runs synchronously over the in-memory list (loaded eagerly in [init]); returns an empty
     * list if the database has not finished loading yet or the query is blank.
     *
     * @param query Free-text search over brand/model/variant.
     * @return Up to [EV_SEARCH_LIMIT] matching vehicles.
     */
    fun searchEvVehicles(query: String): List<EvVehicle> {
        if (!evDbReady || query.isBlank()) return emptyList()
        return evVehicleRepository.search(query).take(EV_SEARCH_LIMIT)
    }

    /**
     * Adds a vehicle as an EV-type [Appliance] and persists it.
     *
     * Used by both database picks and custom entries — the caller supplies the resolved specs.
     *
     * @param name Display name (e.g. "VW ID.3" or a custom label).
     * @param batteryKwh Usable battery capacity in kWh.
     * @param acMaxPowerKw Maximum AC charging power in kW.
     */
    fun onAddVehicle(name: String, batteryKwh: Double, acMaxPowerKw: Double) {
        val vehicle = Appliance(
            id = UUID.randomUUID().toString(),
            name = name,
            durationHours = 0,
            durationMinutes = 0,
            icon = "ev_charger",
            ev = EvSpec(batteryKwh, acMaxPowerKw)
        )
        val updated = _uiState.value.appliances + vehicle
        settingsRepository.setAppliances(updated)
        _uiState.update { it.copy(appliances = updated) }
        syncAppliancesToWear(updated)
    }

    /**
     * Updates and persists the home charger output.
     *
     * @param kw Charger output in kW.
     */
    fun onEvHomeChargerChanged(kw: Double) {
        settingsRepository.setEvHomeChargerKw(kw)
        _uiState.update { it.copy(evHomeChargerKw = kw) }
    }

    /**
     * Updates and persists the default target state of charge used to prefill the charge prompt.
     *
     * @param soc Target SoC (0–100).
     */
    fun onEvDefaultTargetChanged(soc: Int) {
        settingsRepository.setEvDefaultTargetSoc(soc)
        _uiState.update { it.copy(evDefaultTargetSoc = soc) }
    }

    /** Toggles the optional universal "ready by" deadline. */
    fun onDeadlineEnabledChanged(enabled: Boolean) {
        _uiState.update { it.copy(deadlineEnabled = enabled) }
    }

    /**
     * Sets the "ready by" deadline time of day.
     *
     * @param hour Hour of day (0–23).
     * @param minute Minute (0–59).
     */
    fun onDeadlineChanged(hour: Int, minute: Int) {
        _uiState.update { it.copy(deadlineHour = hour, deadlineMinute = minute) }
    }

    /**
     * Resolves the active "ready by" deadline to a concrete instant: the next occurrence of the
     * configured time of day at or after [now], or `null` when the deadline is disabled.
     */
    private fun resolveDeadline(now: ZonedDateTime): ZonedDateTime? {
        val state = _uiState.value
        if (!state.deadlineEnabled) return null
        var dl = now.withHour(state.deadlineHour).withMinute(state.deadlineMinute).withSecond(0).withNano(0)
        if (!dl.isAfter(now)) dl = dl.plusDays(1)
        return dl
    }

    /**
     * Computes the charging duration for a vehicle appliance from a state-of-charge range and
     * runs the cheapest-window search (honouring the universal "ready by" deadline if set).
     *
     * Effective charging power is the lesser of the vehicle's max AC power and the home charger
     * output. Duration uses a pure-linear model, appropriate for slow AC charging.
     *
     * @param appliance The tapped vehicle appliance (must have a non-null [Appliance.ev]).
     * @param currentSoc Current state of charge (0–100).
     * @param targetSoc Target state of charge (0–100).
     */
    fun onEvApplianceFind(appliance: Appliance, currentSoc: Int, targetSoc: Int) {
        val spec = appliance.ev ?: return

        if (targetSoc <= currentSoc) {
            _uiState.update { it.copy(error = AppError.Validation(UiText.Res(R.string.ev_error_invalid_soc))) }
            return
        }
        val priceZone = _uiState.value.priceZone
        if (priceZone == null) {
            _uiState.update { it.copy(error = AppError.Validation(UiText.Res(R.string.error_no_zone))) }
            return
        }
        val effectivePowerKw = minOf(spec.acMaxPowerKw, _uiState.value.evHomeChargerKw)
        if (effectivePowerKw <= 0.0) {
            _uiState.update { it.copy(error = AppError.Validation(UiText.Res(R.string.ev_error_invalid_charger))) }
            return
        }

        // Pure-linear AC charging model: energy needed / effective power.
        val energyKwh = (targetSoc - currentSoc) / 100.0 * spec.batteryKwh
        val totalMinutes = Math.round(energyKwh / effectivePowerKw * 60).toInt().coerceAtLeast(1)
        val roundedDurationHours = totalMinutes / 60.0

        val timeZoneId = _uiState.value.timeZoneId
        val deadline = resolveDeadline(currentNow(timeZoneId))

        // Remember the current SoC to prefill the prompt next time.
        settingsRepository.setEvLastCurrentSoc(currentSoc)

        val label = UiText.Raw("${appliance.name} · ${currentSoc}→${targetSoc}%")

        _uiState.update {
            it.copy(
                evLastCurrentSoc = currentSoc,
                durationHours = totalMinutes / 60,
                durationMinutes = totalMinutes % 60,
                isLoading = true,
                error = null,
                result = null,
                resultLabel = label,
                searchDeadline = deadline,
                searchPowerKw = effectivePowerKw
            )
        }

        stopResultRefresh()
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch(ioDispatcher) {
            fetchAndFind(roundedDurationHours, label, timeZoneId, priceZone)
        }
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
                disabledSources = emptySet(),
                // Reset the chosen supplier, manual surcharge, and tariff — they belong to the
                // previous country's feed (different suppliers/currency/magnitude).
                supplierId = null,
                manualSurcharge = null,
                allInTariff = null,
                tariffFetchedAtMs = null
            )
        }
        syncSettingsToWear()
        // A country change is an explicit action selecting a different feed — always re-fetch.
        loadTariffAsync(code, allowNetwork = true, force = true)
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
        _uiState.update {
            it.copy(
                durationHours = hours,
                durationMinutes = minutes,
                resultLabel = UiText.duration(hours, minutes)
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
        val label = UiText.applianceLabel(appliance.name, appliance.durationHours, appliance.durationMinutes)
        _uiState.update {
            it.copy(
                durationHours = appliance.durationHours,
                durationMinutes = appliance.durationMinutes,
                resultLabel = label
            )
        }
        onFindClicked(appliance.powerKw)
    }

    /** Clears the current result and returns to the form screen. */
    fun onClearResult() {
        stopResultRefresh()
        _uiState.update {
            it.copy(
                result = null,
                resultLabel = null,
                windowAlternatives = emptyList(),
                windowOffset = 0,
                allPrices = emptyList(),
                priceSource = null,
                error = null,
                searchDeadline = null,
                searchPowerKw = null
            )
        }
    }

    /**
     * Moves the displayed window one step earlier, to the next-cheapest window that starts sooner.
     *
     * Advances [UiState.windowOffset] within [UiState.windowAlternatives]. No-op when already at
     * the earliest available window (the last alternative). Each step is costlier but starts sooner.
     */
    fun onEarlierWindow() {
        _uiState.update { state ->
            val next = state.windowOffset + 1
            if (next >= state.windowAlternatives.size) return@update state
            state.copy(windowOffset = next, result = state.windowAlternatives[next])
        }
    }

    /**
     * Moves the displayed window one step back toward the cheapest window.
     *
     * Reduces [UiState.windowOffset], reversing [onEarlierWindow]. No-op when already showing the
     * cheapest window (offset 0).
     */
    fun onCheaperWindow() {
        _uiState.update { state ->
            val prev = state.windowOffset - 1
            if (prev < 0) return@update state
            state.copy(windowOffset = prev, result = state.windowAlternatives[prev])
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
     * Recalculates the window alternatives using already-fetched prices and the current time.
     *
     * Filters [UiState.allPrices] to exclude elapsed slots, then re-runs [findWindowAlternatives].
     * Preserves the user's current selection by matching the previously-displayed window's start
     * time in the rebuilt list; if that window has elapsed, falls back to the cheapest (offset 0).
     * Updates [UiState.result], [UiState.windowAlternatives], [UiState.windowOffset], and
     * [UiState.allPrices] so the chart and summary stay current.
     */
    internal fun recalculateResult() {
        val state = _uiState.value
        val prices = state.allPrices
        if (prices.isEmpty() || state.result == null) return

        val timeZoneId = state.timeZoneId
        val now = currentNow(timeZoneId)
        val futurePrices = prices.filter {
            it.time.plusMinutes(it.durationMinutes.toLong()).isAfter(now)
        }

        val durationHours = state.durationHours + state.durationMinutes / 60.0
        val alternatives = if (futurePrices.isNotEmpty()) {
            findWindowAlternatives(futurePrices, durationHours, now, state.searchDeadline)
        } else emptyList()

        // No window fits any more — every slot has elapsed, or a "ready by" deadline has now
        // passed. Keep the last result on screen (the search already happened) and stop refreshing
        // rather than nulling the result and flipping the UI back to the form.
        if (alternatives.isEmpty()) {
            stopResultRefresh()
            _uiState.update { it.copy(now = now) }
            return
        }

        // Keep showing the window the user navigated to, matched by start time. If it has
        // elapsed out of the list, fall back to the cheapest window.
        val selectedStart = state.result.startTime.toEpochSecond()
        val offset = alternatives.indexOfFirst { it.startTime.toEpochSecond() == selectedStart }
            .let { if (it >= 0) it else 0 }

        _uiState.update {
            it.copy(
                result = alternatives.getOrNull(offset),
                windowAlternatives = alternatives,
                windowOffset = offset,
                allPrices = futurePrices,
                now = now
            )
        }
    }

    /**
     * Clears all cached price data if the API cooldown has elapsed.
     *
     * @return A user-facing message ([UiText], resolved by the UI): confirmation if cleared, or cooldown warning.
     */
    fun onClearCache(): UiText {
        val cooldownDisabled = settingsRepository.isCooldownDisabled()
        val remaining = if (cooldownDisabled) 0L else priceCache.cooldownRemainingMs(PriceRepository.COOLDOWN_MS)
        return if (remaining > 0) {
            val minutes = (remaining / 60_000).toInt() + 1
            UiText.Plural(R.plurals.error_cooldown, minutes, listOf(minutes))
        } else {
            priceCache.clear()
            UiText.Res(R.string.snackbar_cache_cleared)
        }
    }

    /**
     * Re-fetches prices and recalculates the cheapest window from the results screen.
     *
     * If the API cooldown is still active, shows a "try again in X minutes" snackbar.
     * Otherwise, clears the zone cache, preserves the existing result, and re-runs
     * the fetch-and-find flow.
     */
    fun onRefreshResults() {
        val cooldownDisabled = settingsRepository.isCooldownDisabled()
        val remaining = if (cooldownDisabled) 0L else priceCache.cooldownRemainingMs(PriceRepository.COOLDOWN_MS)
        if (remaining > 0) {
            val minutes = (remaining / 60_000).toInt() + 1
            _uiState.update { it.copy(error = AppError.Network(UiText.Plural(R.plurals.error_cooldown, minutes, listOf(minutes)))) }
            return
        }

        val state = _uiState.value
        val priceZone = state.priceZone ?: return
        priceCache.clearForZone(priceZone.id)

        val h = state.durationHours
        val m = state.durationMinutes
        val durationHours = h + m / 60.0
        val durationText = UiText.duration(h, m)

        _uiState.update { it.copy(isLoading = true, error = null) }

        val timeZoneId = state.timeZoneId
        stopResultRefresh()
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch(ioDispatcher) {
            fetchAndFind(durationHours, durationText, timeZoneId, priceZone)
        }
    }

    /**
     * Adds a new appliance and persists it.
     *
     * @param name Display name.
     * @param durationHours Hours component of the default duration.
     * @param durationMinutes Minutes component of the default duration.
     * @param icon Icon ID from the appliance icon registry.
     * @param powerKw Optional load rating in kW, or `null` for the default per-1-kW cost display.
     */
    fun onAddAppliance(name: String, durationHours: Int, durationMinutes: Int, icon: String, powerKw: Double? = null) {
        val appliance = Appliance(
            id = UUID.randomUUID().toString(),
            name = name,
            durationHours = durationHours,
            durationMinutes = durationMinutes,
            icon = icon,
            powerKw = powerKw
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
     * EV-type appliances are excluded — the watch has no state-of-charge UI in this version.
     *
     * @param appliances The appliance list to sync (EV appliances are filtered out).
     */
    private fun syncAppliancesToWear(appliances: List<Appliance>) {
        try {
            val json = Json.encodeToString(appliances.filterNot { it.isEv })
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
     * Called when the user picks a new per-app language in Settings.
     *
     * Syncs the tag to the watch via the Data Layer and then triggers the locale switch.
     * Setting the locale here (before the Compose navigation state changes) avoids a
     * flash of the old language when the picker closes and the Activity recreates.
     */
    fun onLanguageChanged(languageTag: String) {
        syncSettingsToWear(languageTag = languageTag)
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(languageTag)
        )
    }

    /**
     * Applies the selected theme mode and persists it.
     *
     * @param mode The selected [ThemeMode].
     */
    fun onThemeModeChanged(mode: ThemeMode) {
        settingsRepository.setThemeMode(mode.key)
        _uiState.update { it.copy(themeMode = mode) }
        AppCompatDelegate.setDefaultNightMode(mode.nightMode)
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
                dataMap.putBoolean("stats_enabled", settingsRepository.isStatsEnabled())
                dataMap.putBoolean("is_trial_expired", settingsRepository.isTrialExpired())
                dataMap.putBoolean("is_unlocked", settingsRepository.isUnlocked())
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
     *
     * @param powerKw Optional load rating (kW) used to scale displayed costs. `null` (the default,
     *   used by the Find button and quick-duration chips) keeps the per-1-kW behaviour.
     */
    fun onFindClicked(powerKw: Double? = null) {
        val h = _uiState.value.durationHours
        val m = _uiState.value.durationMinutes

        if (h == 0 && m == 0) {
            _uiState.update {
                it.copy(
                    error = AppError.Validation(UiText.Res(R.string.error_zero_duration)),
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
                    error = AppError.Validation(UiText.Res(R.string.error_no_zone)),
                    result = null,
                    allPrices = emptyList()
                )
            }
            return
        }

        val durationHours = h + m / 60.0
        val durationText = UiText.duration(h, m)

        _uiState.update {
            it.copy(
                isLoading = true,
                error = null,
                result = null,
                resultLabel = it.resultLabel ?: durationText,
                searchDeadline = resolveDeadline(currentNow(it.timeZoneId)),
                searchPowerKw = powerKw
            )
        }

        val timeZoneId = _uiState.value.timeZoneId
        stopResultRefresh()
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch(ioDispatcher) {
            fetchAndFind(durationHours, durationText, timeZoneId, priceZone)
        }
    }

    /**
     * Fetches prices from the repository and runs the cheapest-window algorithm.
     *
     * Called on [Dispatchers.IO]. Updates [_uiState] with the result or an error.
     * On success, starts the periodic refresh via [startResultRefresh].
     *
     * @param durationHours Duration in decimal hours.
     * @param durationText Human-readable duration label ([UiText]) used in the "not enough data" error.
     * @param timeZoneId Timezone snapshot captured before the IO dispatch.
     * @param priceZone The price zone to fetch data for.
     */
    private fun fetchAndFind(durationHours: Double, durationText: UiText, timeZoneId: ZoneId, priceZone: PriceZone) {
        try {
            val state = _uiState.value
            val enabledOrder = state.sourceOrder?.filter { it !in state.disabledSources }
            val activeCollector = if (settingsRepository.isStatsEnabled()) statsCollector else null
            val factory = priceFetcherFactory
                ?: defaultPriceFetcherFactory(BuildConfig.ENTSOE_API_TOKEN, enabledOrder, activeCollector, "phone")
            val fetcher = factory.create(priceZone)
            if (settingsRepository.isCooldownDisabled()) priceCache.resetCooldown()
            val repository = PriceRepository(priceCache, timeZoneId, fetcher, clock = settingsRepository.devClock(timeZoneId), cacheKey = priceZone.id)
            val priceResult = repository.getPrices()

            // Piggyback: when all-in is in use and prices were fetched from the network, refresh the
            // tariff too. Gated on `allInEnabled` so we never fetch tariff data for users who don't use
            // all-in (and so unit tests without it make no tariff network calls).
            if (state.allInEnabled && !priceResult.fromCache) {
                loadTariffAsync(state.countryCode, allowNetwork = true, force = true)
            }

            // Apply the display-only all-in transform when enabled and a surcharge is set. The surcharge
            // field is the source of truth (supplier picks prefill it); ranking is unchanged (monotonic).
            val tariff = state.allInTariff
            val surcharge = state.manualSurcharge
            // Non-null only when all-in should be applied (enabled + usable, same-currency tariff + a
            // surcharge value). The currency guard mirrors `allInSupported` — never add a non-EUR feed's
            // surcharge to EUR spot prices.
            val allInTariff = if (state.allInEnabled && tariff != null && tariff.usable && tariff.currency == SPOT_CURRENCY && surcharge != null) tariff else null
            val allInApplied = allInTariff != null
            val prices = if (allInTariff != null && surcharge != null) {
                AllInPricing.applyAllIn(priceResult.prices, allInTariff.taxes, surcharge)
            } else {
                priceResult.prices
            }
            // Name the supplier only while its picked value is unchanged (editing the field clears supplierId).
            val allInSupplierName = if (allInTariff != null && state.supplierId != null) {
                allInTariff.suppliers.firstOrNull { it.id == state.supplierId }?.name
            } else {
                null
            }

            if (prices.isEmpty()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = AppError.Validation(UiText.Res(R.string.error_no_data)),
                        allPrices = emptyList(),
                        priceSource = null
                    )
                }
                return
            }

            val now = currentNow(timeZoneId)
            val deadline = _uiState.value.searchDeadline
            val alternatives = findWindowAlternatives(prices, durationHours, now, deadline)

            if (alternatives.isEmpty()) {
                val message = if (deadline != null) {
                    UiText.Res(R.string.ev_error_deadline_unreachable)
                } else {
                    val coverageHours = prices.sumOf { it.durationMinutes.toLong() } / 60
                    UiText.Plural(R.plurals.error_not_enough_data, coverageHours.toInt(), listOf(durationText, coverageHours))
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = AppError.Validation(message),
                        allPrices = prices
                    )
                }
                return
            }

            val allInStale = allInApplied && state.tariffFetchedAtMs?.let {
                now.toInstant().toEpochMilli() - it > TARIFF_STALENESS_MS
            } == true
            _uiState.update {
                it.copy(
                    isLoading = false,
                    result = alternatives.first(),
                    windowAlternatives = alternatives,
                    windowOffset = 0,
                    allPrices = prices,
                    priceSource = priceResult.source,
                    error = null,
                    now = now,
                    allInApplied = allInApplied,
                    allInSupplierName = allInSupplierName,
                    allInStale = allInStale
                )
            }
            startResultRefresh()
            tryReportStats()
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = AppError.Network(UiText.Res(R.string.error_network, listOf(e.message ?: ""))),
                    allPrices = emptyList(),
                    priceSource = null
                )
            }
        }
    }

    /**
     * Checks whether the stats opt-in prompt should be shown.
     *
     * Shows the prompt once, after the user has been using the app for at least 3 days,
     * unless stats are already enabled (e.g. the user enabled them manually in settings).
     */
    private fun checkStatsPrompt() {
        if (settingsRepository.isStatsPromptShown()) return
        if (settingsRepository.isStatsEnabled()) return
        val firstLaunch = settingsRepository.getFirstLaunchMs()
        val daysSinceFirst = (System.currentTimeMillis() - firstLaunch) / (24 * 60 * 60 * 1000L)
        if (daysSinceFirst >= 3) {
            _uiState.update { it.copy(showStatsPrompt = true) }
        }
    }

    /**
     * Handles the user enabling stats from the opt-in prompt.
     *
     * Sets stats as enabled and marks the prompt as shown.
     */
    fun onStatsPromptEnabled() {
        settingsRepository.setStatsEnabled(true)
        settingsRepository.setStatsPromptShown()
        _uiState.update { it.copy(showStatsPrompt = false, isStatsEnabled = true) }
        syncSettingsToWear()
    }

    /**
     * Handles the user dismissing the stats opt-in prompt.
     *
     * Marks the prompt as shown so it is never displayed again.
     */
    fun onStatsPromptDismissed() {
        settingsRepository.setStatsPromptShown()
        _uiState.update { it.copy(showStatsPrompt = false) }
    }

    /**
     * Toggles API stats collection from the settings screen.
     *
     * @param enabled Whether stats collection should be enabled.
     */
    fun onStatsEnabledChanged(enabled: Boolean) {
        settingsRepository.setStatsEnabled(enabled)
        _uiState.update { it.copy(isStatsEnabled = enabled) }
        syncSettingsToWear()
    }

    /**
     * Attempts to report stats to the server if opt-in is enabled and the interval has elapsed.
     *
     * Called after a successful price fetch. Runs on the IO dispatcher.
     */
    private fun tryReportStats() {
        if (!settingsRepository.isStatsEnabled()) return
        try {
            statsReporter.reportIfDue()
        } catch (_: Exception) {
            // Best-effort: stats reporting should never affect the main flow
        }
    }

    /**
     * Receives stats records from the watch via the Wearable Data Layer.
     *
     * Appends each watch record to the phone's local stats file so they
     * are included in the next report.
     *
     * @param records Stats records from the watch.
     */
    fun onWatchStatsReceived(records: List<StatsRecord>) {
        if (!settingsRepository.isStatsEnabled()) return
        for (record in records) {
            statsCollector.record(record)
        }
    }

    // --- Purchase ---

    /**
     * Re-queries subscription state to detect expiry when the app returns to foreground.
     *
     * Called from [MainActivity.onResume].
     */
    fun onResume() {
        activeBilling?.onResume()
    }

    /**
     * Launches the subscription purchase flow.
     *
     * @param activity The activity to host the purchase UI.
     */
    fun onPurchaseClicked(activity: Activity) {
        activeBilling?.launchPurchaseFlow(activity)
    }

    /**
     * Re-queries existing purchases to restore subscription state.
     *
     * Useful for users who previously subscribed on another device or after reinstalling.
     */
    fun onRestorePurchases() {
        activeBilling?.queryPurchases()
    }

    /** Dismisses the thank-you dialog shown after a successful purchase. */
    fun onThankYouDismissed() {
        _uiState.update { it.copy(showThankYou = false) }
    }

    // --- Developer options ---
    // (Watch stats arrive via [watchStatsBridge]; the decoded records are handled by
    //  [onWatchStatsReceived]. The Data Layer plumbing lives in [WearableStatsBridge].)

    /**
     * Persistently enables hidden developer options (triggered by 7-tap on version number).
     */
    fun onDevOptionsUnlocked() {
        settingsRepository.setDevOptionsEnabled()
        _uiState.update { it.copy(devOptionsEnabled = true) }
    }

    /**
     * Resets the unlock/payment state, re-enabling the trial paywall.
     *
     * Clears the local unlock flag and updates the UI state. Does not affect
     * the actual Google Play purchase — only the local cached state.
     */
    fun onDevResetUnlock() {
        settingsRepository.setUnlocked(false)
        _uiState.update {
            it.copy(
                isUnlocked = false,
                showPaywall = shouldShowPaywall(BuildConfig.DEBUG, settingsRepository.isTrialExpired(), settingsRepository.isUnlocked())
            )
        }
        syncSettingsToWear()
    }

    /**
     * Enables or disables the developer-only subscription bypass.
     *
     * When enabled, the paywall is suppressed on this device regardless of trial
     * or subscription state. Intended for the developer's own phone during the
     * testing phase, where Play test subscriptions expire every ~30 minutes.
     * Also propagates to the watch via the Data Layer so the wear app unlocks too.
     *
     * @param enabled `true` to bypass the paywall locally, `false` to restore normal behaviour.
     */
    fun onDevUnlockChanged(enabled: Boolean) {
        settingsRepository.setDevUnlocked(enabled)
        _uiState.update {
            it.copy(
                isDevUnlocked = enabled,
                isTrialExpired = settingsRepository.isTrialExpired(),
                showPaywall = shouldShowPaywall(BuildConfig.DEBUG, settingsRepository.isTrialExpired(), settingsRepository.isUnlocked())
            )
        }
        syncSettingsToWear()
    }

    /**
     * Toggles the API fetch cooldown bypass.
     *
     * When disabled, all API cooldown checks are skipped, allowing immediate fetches.
     *
     * @param disabled `true` to disable the cooldown.
     */
    fun onDevCooldownDisabledChanged(disabled: Boolean) {
        settingsRepository.setCooldownDisabled(disabled)
        _uiState.update { it.copy(isCooldownDisabled = disabled) }
    }

    /**
     * Sets or clears the developer time override.
     *
     * When set, the app behaves as if the current time is the override value.
     * Clears cached prices so the next fetch uses the overridden date range.
     *
     * @param ms Epoch milliseconds for the fake "now", or `null` to clear.
     */
    fun onDevTimeOverrideChanged(ms: Long?) {
        settingsRepository.setTimeOverrideMs(ms)
        priceCache.clear()
        val timeZoneId = _uiState.value.timeZoneId
        _uiState.update {
            it.copy(
                timeOverrideMs = ms,
                now = currentNow(timeZoneId),
                isTrialExpired = settingsRepository.isTrialExpired(),
                trialDaysRemaining = settingsRepository.trialDaysRemaining(),
                showPaywall = shouldShowPaywall(BuildConfig.DEBUG, settingsRepository.isTrialExpired(), settingsRepository.isUnlocked())
            )
        }
    }

    /**
     * Toggles the production logo override for debug builds.
     *
     * @param use `true` to show the production logo.
     */
    fun onDevUseProductionLogoChanged(use: Boolean) {
        settingsRepository.setUseProductionLogo(use)
        _uiState.update { it.copy(useProductionLogo = use) }
    }

    /**
     * Resets the stats report timer, allowing immediate stats reporting.
     */
    fun onDevResetStatsTimer() {
        statsReporter.resetReportTimer()
    }
}

/**
 * Decides whether the subscription paywall should block the app.
 *
 * Pure so the rule is unit-testable for both build types (release unit tests always run with
 * `BuildConfig.DEBUG == true`, so the debug-skip branch is otherwise unreachable in tests).
 *
 * @param isDebug Whether this is a debug build (paywall is always skipped in debug).
 * @param trialExpired Whether the free trial has expired.
 * @param unlocked Whether the app has been unlocked via subscription.
 * @return `true` only for a release build with an expired trial and no active unlock.
 */
internal fun shouldShowPaywall(isDebug: Boolean, trialExpired: Boolean, unlocked: Boolean): Boolean =
    !isDebug && trialExpired && !unlocked

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
