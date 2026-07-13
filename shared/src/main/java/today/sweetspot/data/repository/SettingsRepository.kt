package today.sweetspot.data.repository

import android.content.Context
import androidx.core.content.edit

import kotlinx.serialization.json.Json
import today.sweetspot.model.Appliance
import today.sweetspot.model.ApplianceSort
import today.sweetspot.model.ApplianceUsage
import today.sweetspot.model.Countries
import today.sweetspot.model.EvPosition

import today.sweetspot.model.PriceZone
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * Persistence layer for user settings.
 *
 * Stores country/zone selection, timezone preference, and appliance list in SharedPreferences.
 * Appliances are JSON-serialized via kotlinx-serialization.
 *
 * The repeated get/put boilerplate is funnelled through the small typed helpers at the bottom
 * (`putBool`/`putStr`/`getJson`/…), so each accessor stays a one-liner over one prefs file.
 *
 * @param context Android context for SharedPreferences access.
 */
class SettingsRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("sweetspot_settings", Context.MODE_PRIVATE)

    private companion object {
        const val KEY_TIMEZONE_ID = "zone_id"
        const val KEY_APPLIANCES = "appliances"
        const val KEY_COUNTRY_CODE = "country_code"
        const val KEY_EV_HOME_CHARGER_KW = "ev_home_charger_kw"
        const val KEY_EV_DEFAULT_TARGET_SOC = "ev_default_target_soc"
        const val KEY_EV_LAST_CURRENT_SOC = "ev_last_current_soc"
        const val KEY_PRICE_ZONE_ID = "price_zone_id"
        const val KEY_SOURCE_ORDER = "source_order"
        const val KEY_DISABLED_SOURCES = "disabled_sources"
        const val KEY_STATS_ENABLED = "stats_enabled"
        const val KEY_STATS_PROMPT_SHOWN = "stats_prompt_shown"
        const val KEY_FIRST_LAUNCH_MS = "first_launch_ms"
        const val KEY_UNLOCKED = "unlocked"
        const val KEY_DEV_OPTIONS = "dev_options"
        const val KEY_DEV_UNLOCK = "dev_unlock"
        const val KEY_COOLDOWN_DISABLED = "cooldown_disabled"
        const val KEY_TIME_OVERRIDE = "time_override"
        const val KEY_USE_PRODUCTION_LOGO = "use_production_logo"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_ALL_IN_ENABLED = "all_in_enabled"
        const val KEY_SUPPLIER_ID = "all_in_supplier_id"
        const val KEY_MANUAL_SURCHARGE = "all_in_manual_surcharge"
        const val KEY_APPLIANCE_SORT = "appliance_sort"
        const val KEY_EV_POSITION = "ev_position"
        const val KEY_EV_SEPARATE = "ev_separate"
        const val KEY_APPLIANCE_USAGE = "appliance_usage"
        const val KEY_WATCH_USAGE = "watch_usage"
        const val KEY_USAGE_RESET_TOKEN = "usage_reset_token"

        /** Trial duration in days. */
        const val TRIAL_DAYS = 14

        /** Default home charger output in kW (a common 3-phase wall box). */
        const val DEFAULT_HOME_CHARGER_KW = 11.0f

        /** Default current state of charge (%) prefilled on the EV screen. */
        const val DEFAULT_CURRENT_SOC = 20

        /** Default target state of charge (%) prefilled on the EV screen. */
        const val DEFAULT_TARGET_SOC = 80

        /** Lenient parser that ignores unknown fields for forward compatibility. */
        val json = Json { ignoreUnknownKeys = true }
    }

    // --- Country & Price Zone ---

    /**
     * Returns the stored country code, or auto-detects on first access.
     *
     * On first launch, uses [CountryDetector] to guess the country from SIM/network/locale/timezone.
     * The detected value is persisted so detection only runs once.
     */
    fun getCountryCode(): String {
        val stored = prefs.getString(KEY_COUNTRY_CODE, null)
        if (stored != null) return stored

        val detected = CountryDetector.detect(context)
        putStr(KEY_COUNTRY_CODE, detected.code)
        return detected.code
    }

    /**
     * Persists the selected country code.
     *
     * Also clears any custom source order, since available sources differ per country.
     *
     * @param code ISO 3166-1 alpha-2 country code.
     */
    fun setCountryCode(code: String) {
        putStr(KEY_COUNTRY_CODE, code)
        clearSourceOrder()
        clearDisabledSources()
        // The chosen supplier and any manual surcharge belong to the previous country's tariff feed
        // (different suppliers, currency, and price magnitude); reset both.
        setSupplierId(null)
        setManualSurcharge(null)
    }

    // --- All-in price ---

    /** Whether the user has enabled the approximate all-in consumer price display. */
    fun isAllInEnabled(): Boolean = prefs.getBoolean(KEY_ALL_IN_ENABLED, false)

    /** Persists the all-in enabled preference. */
    fun setAllInEnabled(enabled: Boolean) = putBool(KEY_ALL_IN_ENABLED, enabled)

    /** Returns the chosen supplier id (from the tariff feed), or `null` if none is selected. */
    fun getSupplierId(): String? = prefs.getString(KEY_SUPPLIER_ID, null)

    /** Persists the chosen supplier id, or clears it when `null`. */
    fun setSupplierId(id: String?) = putStr(KEY_SUPPLIER_ID, id)

    /**
     * Returns the manual per-kWh surcharge override (ex-VAT), or `null` when unset.
     *
     * A manual override, when present, takes precedence over the chosen supplier's surcharge.
     */
    fun getManualSurcharge(): Double? =
        prefs.getString(KEY_MANUAL_SURCHARGE, null)?.toDoubleOrNull()

    /** Persists the manual surcharge override, or clears it when `null`. */
    fun setManualSurcharge(value: Double?) = putStr(KEY_MANUAL_SURCHARGE, value?.toString())

    /**
     * Returns the stored price zone ID within the current country, or `null` if using the default (first zone).
     */
    fun getPriceZoneId(): String? = prefs.getString(KEY_PRICE_ZONE_ID, null)

    /**
     * Persists the selected price zone ID.
     *
     * @param id The [PriceZone.id], or `null` to use the country's first zone.
     */
    fun setPriceZoneId(id: String?) = putStr(KEY_PRICE_ZONE_ID, id)

    /**
     * Resolves the current country and zone settings to a concrete [PriceZone].
     *
     * For single-zone countries, returns the only zone automatically.
     * For multi-zone countries, returns the stored zone or `null` if the user
     * hasn't made a selection yet (zone selection is mandatory).
     *
     * @return The resolved [PriceZone], or `null` if a multi-zone country has no selection.
     */
    fun getResolvedPriceZone(): PriceZone? {
        val country = Countries.findByCode(getCountryCode()) ?: Countries.defaultCountry()
        val storedPriceZoneId = getPriceZoneId()
        if (storedPriceZoneId != null) {
            country.zones.find { it.id == storedPriceZoneId }?.let { return it }
        }
        return if (country.zones.size == 1) country.zones.first() else null
    }

    // --- Timezone ---

    /**
     * Returns the effective timezone.
     *
     * Priority:
     * 1. User's manually set timezone (if any)
     * 2. Timezone derived from the selected price zone
     * 3. System default (if no price zone is selected yet)
     */
    fun getTimeZoneId(): ZoneId {
        val stored = prefs.getString(KEY_TIMEZONE_ID, null)
        if (stored != null) {
            return try {
                ZoneId.of(stored)
            } catch (_: Exception) {
                getResolvedPriceZone()?.let { ZoneId.of(it.timeZoneId) } ?: ZoneId.systemDefault()
            }
        }
        return getResolvedPriceZone()?.let { ZoneId.of(it.timeZoneId) } ?: ZoneId.systemDefault()
    }

    /**
     * Persists a custom timezone selection.
     *
     * @param timeZoneId The timezone to store.
     */
    fun setTimeZoneId(timeZoneId: ZoneId) = putStr(KEY_TIMEZONE_ID, timeZoneId.id)

    /** Removes the custom timezone, reverting to zone-derived default. */
    fun clearTimeZoneId() = removeKey(KEY_TIMEZONE_ID)

    /** Returns `true` if no custom timezone has been set (using zone-derived default). */
    fun isUsingDefaultTimezone(): Boolean = prefs.getString(KEY_TIMEZONE_ID, null) == null

    // --- Data Source Order ---

    /**
     * Returns the user's preferred source display order, or `null` if using defaults.
     *
     * The list contains all source IDs in display/priority order (both enabled and disabled).
     */
    fun getSourceOrder(): List<String>? = getJson<List<String>?>(KEY_SOURCE_ORDER, null)

    /**
     * Persists the user's preferred source display order.
     *
     * @param order Ordered list of all source IDs (enabled and disabled).
     */
    fun setSourceOrder(order: List<String>) = putJson(KEY_SOURCE_ORDER, order)

    /** Removes the custom source order, reverting to zone-specific defaults. */
    fun clearSourceOrder() = removeKey(KEY_SOURCE_ORDER)

    /**
     * Returns the set of disabled source IDs.
     *
     * @return Set of disabled source IDs, or empty set if all enabled.
     */
    fun getDisabledSources(): Set<String> = getJson(KEY_DISABLED_SOURCES, emptySet())

    /**
     * Persists the set of disabled source IDs.
     *
     * @param disabled Set of source IDs to disable.
     */
    fun setDisabledSources(disabled: Set<String>) {
        if (disabled.isEmpty()) removeKey(KEY_DISABLED_SOURCES) else putJson(KEY_DISABLED_SOURCES, disabled)
    }

    /** Removes all disabled sources, re-enabling everything. */
    fun clearDisabledSources() = removeKey(KEY_DISABLED_SOURCES)

    // --- Appliances ---

    /**
     * Returns the user's saved appliances.
     *
     * @return List of appliances, or empty list if none saved or on parse error.
     */
    fun getAppliances(): List<Appliance> = getJson(KEY_APPLIANCES, emptyList())

    /**
     * Persists the full appliance list, replacing any previously stored list.
     *
     * @param appliances The appliances to store.
     */
    fun setAppliances(appliances: List<Appliance>) = putJson(KEY_APPLIANCES, appliances)

    // --- Appliance sorting, EV placement & usage ---

    /** Returns the chosen appliance ordering, defaulting to manual (custom) order. */
    fun getApplianceSort(): ApplianceSort = getJson(KEY_APPLIANCE_SORT, ApplianceSort())

    /** Persists the chosen appliance ordering. */
    fun setApplianceSort(sort: ApplianceSort) = putJson(KEY_APPLIANCE_SORT, sort)

    /** Returns where vehicles are placed on the home screen (default [EvPosition.INTERLEAVED]). */
    fun getEvPosition(): EvPosition = EvPosition.fromKey(prefs.getString(KEY_EV_POSITION, null))

    /** Persists the vehicle placement. */
    fun setEvPosition(position: EvPosition) = putStr(KEY_EV_POSITION, position.key)

    /** Whether a First/Last vehicle block is drawn as its own section (default false). */
    fun isEvSeparateSection(): Boolean = prefs.getBoolean(KEY_EV_SEPARATE, false)

    /** Persists the separate-section preference. */
    fun setEvSeparateSection(separate: Boolean) = putBool(KEY_EV_SEPARATE, separate)

    /** Returns phone-local per-appliance tap usage. */
    fun getApplianceUsage(): Map<String, ApplianceUsage> = readUsage(KEY_APPLIANCE_USAGE)

    /** Records one tap for [id] at [nowMs], incrementing its count and last-used time. */
    fun recordApplianceUsage(id: String, nowMs: Long) {
        val current = getApplianceUsage()
        val existing = current[id]
        writeUsage(KEY_APPLIANCE_USAGE, current + (id to ApplianceUsage((existing?.count ?: 0) + 1, nowMs)))
    }

    /** Clears phone-local usage. */
    fun clearApplianceUsage() = removeKey(KEY_APPLIANCE_USAGE)

    /** Returns the last usage snapshot received from the watch (stored separately to avoid double-counting). */
    fun getWatchUsage(): Map<String, ApplianceUsage> = readUsage(KEY_WATCH_USAGE)

    /** Persists the latest watch usage snapshot. */
    fun setWatchUsage(usage: Map<String, ApplianceUsage>) = writeUsage(KEY_WATCH_USAGE, usage)

    /** Clears the stored watch usage snapshot. */
    fun clearWatchUsage() = removeKey(KEY_WATCH_USAGE)

    /** The current usage reset token, bumped on purge and propagated to the watch. */
    fun getUsageResetToken(): Long = prefs.getLong(KEY_USAGE_RESET_TOKEN, 0L)

    /** Advances the reset token so the watch zeroes its own usage on next sync. */
    fun bumpUsageResetToken() = putLong(KEY_USAGE_RESET_TOKEN, getUsageResetToken() + 1)

    // --- EV charging ---

    /** Returns the home charger output in kW. Defaults to 11.0 (a common 3-phase wall box). */
    fun getEvHomeChargerKw(): Double =
        prefs.getFloat(KEY_EV_HOME_CHARGER_KW, DEFAULT_HOME_CHARGER_KW).toDouble()

    /**
     * Persists the home charger output.
     *
     * @param kw Charger output in kW.
     */
    fun setEvHomeChargerKw(kw: Double) = putFloat(KEY_EV_HOME_CHARGER_KW, kw.toFloat())

    /** Returns the default target state of charge (0–100) used to prefill the charge prompt. Defaults to 80. */
    fun getEvDefaultTargetSoc(): Int = prefs.getInt(KEY_EV_DEFAULT_TARGET_SOC, DEFAULT_TARGET_SOC)

    /**
     * Persists the default target state of charge.
     *
     * @param soc Target SoC (0–100).
     */
    fun setEvDefaultTargetSoc(soc: Int) = putInt(KEY_EV_DEFAULT_TARGET_SOC, soc)

    /** Returns the last-used current state of charge (0–100), used to prefill the prompt. Defaults to 20. */
    fun getEvLastCurrentSoc(): Int = prefs.getInt(KEY_EV_LAST_CURRENT_SOC, DEFAULT_CURRENT_SOC)

    /**
     * Persists the last-used current state of charge.
     *
     * @param soc Current SoC (0–100).
     */
    fun setEvLastCurrentSoc(soc: Int) = putInt(KEY_EV_LAST_CURRENT_SOC, soc)

    // --- Stats ---

    /** Returns whether API stats collection is enabled. Defaults to `false`. */
    fun isStatsEnabled(): Boolean = prefs.getBoolean(KEY_STATS_ENABLED, false)

    /**
     * Enables or disables API stats collection.
     *
     * @param enabled `true` to enable stats collection.
     */
    fun setStatsEnabled(enabled: Boolean) = putBool(KEY_STATS_ENABLED, enabled)

    /** Returns whether the one-time stats opt-in prompt has been shown. */
    fun isStatsPromptShown(): Boolean = prefs.getBoolean(KEY_STATS_PROMPT_SHOWN, false)

    /** Marks the stats opt-in prompt as shown so it is never displayed again. */
    fun setStatsPromptShown() = putBool(KEY_STATS_PROMPT_SHOWN, true)

    /**
     * Returns the timestamp of the app's first launch, recording it if not yet set.
     *
     * Used to delay the stats opt-in prompt until the user has been active for a few days.
     *
     * @return Milliseconds since epoch of the first launch.
     */
    fun getFirstLaunchMs(): Long {
        val stored = prefs.getLong(KEY_FIRST_LAUNCH_MS, 0L)
        if (stored != 0L) return stored
        val now = System.currentTimeMillis()
        putLong(KEY_FIRST_LAUNCH_MS, now)
        return now
    }

    // --- Trial & Unlock ---

    /**
     * Returns the number of full trial days remaining (0–14).
     *
     * Based on the elapsed time since [getFirstLaunchMs]. If the app has been unlocked,
     * returns 0 (the caller should check [isUnlocked] separately).
     */
    fun trialDaysRemaining(): Int {
        val now = getTimeOverrideMs() ?: System.currentTimeMillis()
        val elapsed = now - getFirstLaunchMs()
        val elapsedDays = (elapsed / (24 * 60 * 60 * 1000L)).toInt()
        return (TRIAL_DAYS - elapsedDays).coerceIn(0, TRIAL_DAYS)
    }

    /**
     * Returns `true` if the free trial has expired and the app has not been unlocked.
     *
     * The trial lasts [TRIAL_DAYS] days from first launch. Returns `false` when the
     * developer-only subscription bypass ([isDevUnlocked]) is enabled, so a developer
     * can use the app on their own device without an active subscription.
     */
    fun isTrialExpired(): Boolean {
        if (isDevUnlocked()) return false
        return trialDaysRemaining() <= 0 && !isUnlocked()
    }

    /**
     * Returns `true` if the user has purchased the full unlock.
     *
     * This is cached locally in SharedPreferences so the unlock state works offline.
     */
    fun isUnlocked(): Boolean = prefs.getBoolean(KEY_UNLOCKED, false)

    /**
     * Caches the unlock purchase state for offline access.
     *
     * @param unlocked `true` if the user has a valid purchase, `false` if revoked (e.g. refund).
     */
    fun setUnlocked(unlocked: Boolean) = putBool(KEY_UNLOCKED, unlocked)

    // --- Developer Options ---

    /** Returns whether hidden developer options have been unlocked. */
    fun isDevOptionsEnabled(): Boolean = prefs.getBoolean(KEY_DEV_OPTIONS, false)

    /** Persistently enables hidden developer options. */
    fun setDevOptionsEnabled() = putBool(KEY_DEV_OPTIONS, true)

    /**
     * Returns whether the developer-only subscription bypass is enabled.
     *
     * When `true`, [isTrialExpired] always returns `false`, so the paywall never
     * blocks the app. Intended for use on the developer's own device during the
     * testing phase, where Play test subscriptions expire every ~30 minutes.
     */
    fun isDevUnlocked(): Boolean = prefs.getBoolean(KEY_DEV_UNLOCK, false)

    /**
     * Enables or disables the developer-only subscription bypass.
     *
     * @param unlocked `true` to bypass the paywall locally, `false` to restore normal trial/paywall behaviour.
     */
    fun setDevUnlocked(unlocked: Boolean) = putBool(KEY_DEV_UNLOCK, unlocked)

    /** Returns whether the API fetch cooldown is disabled (developer option). */
    fun isCooldownDisabled(): Boolean = prefs.getBoolean(KEY_COOLDOWN_DISABLED, false)

    /**
     * Enables or disables the API fetch cooldown bypass.
     *
     * @param disabled `true` to skip the cooldown between API requests.
     */
    fun setCooldownDisabled(disabled: Boolean) = putBool(KEY_COOLDOWN_DISABLED, disabled)

    /**
     * Returns the stored time override as epoch milliseconds, or `null` if no override is set.
     *
     * When set, the app behaves as if the current time is the override value,
     * affecting price filtering, cheapest window calculation, and trial expiration.
     */
    fun getTimeOverrideMs(): Long? {
        val stored = prefs.getLong(KEY_TIME_OVERRIDE, 0L)
        return if (stored != 0L) stored else null
    }

    /**
     * Sets or clears the time override.
     *
     * @param ms Epoch milliseconds to use as the fake "now", or `null` to clear.
     */
    fun setTimeOverrideMs(ms: Long?) {
        if (ms == null) removeKey(KEY_TIME_OVERRIDE) else putLong(KEY_TIME_OVERRIDE, ms)
    }

    /**
     * Returns a [Clock] that respects the time override developer option.
     *
     * When a time override is set, returns a fixed clock at the override instant.
     * Otherwise returns a normal system clock for the given timezone.
     *
     * @param timeZoneId The timezone for the clock.
     */
    fun devClock(timeZoneId: ZoneId): Clock {
        val overrideMs = getTimeOverrideMs()
        return if (overrideMs != null) {
            Clock.fixed(Instant.ofEpochMilli(overrideMs), timeZoneId)
        } else {
            Clock.system(timeZoneId)
        }
    }

    /** Returns whether the production logo should be used instead of the debug logo. */
    fun isUseProductionLogo(): Boolean = prefs.getBoolean(KEY_USE_PRODUCTION_LOGO, false)

    /**
     * Enables or disables the production logo override.
     *
     * @param use `true` to show the production logo in debug builds.
     */
    fun setUseProductionLogo(use: Boolean) = putBool(KEY_USE_PRODUCTION_LOGO, use)

    // --- Theme ---

    /**
     * Returns the user's preferred theme mode key.
     *
     * @return One of `"system"`, `"light"`, or `"dark"`. Defaults to `"system"`.
     */
    fun getThemeMode(): String = prefs.getString(KEY_THEME_MODE, "system") ?: "system"

    /**
     * Persists the user's preferred theme mode key.
     *
     * @param mode One of `"system"`, `"light"`, or `"dark"`.
     */
    fun setThemeMode(mode: String) = putStr(KEY_THEME_MODE, mode)

    // --- Typed SharedPreferences helpers ---
    // One place for the repetitive edit/serialize boilerplate; every accessor above is a one-liner
    // over these. String writes follow the app's convention: a null value removes the key.

    private fun putBool(key: String, value: Boolean) = prefs.edit { putBoolean(key, value) }

    private fun putInt(key: String, value: Int) = prefs.edit { putInt(key, value) }

    private fun putLong(key: String, value: Long) = prefs.edit { putLong(key, value) }

    private fun putFloat(key: String, value: Float) = prefs.edit { putFloat(key, value) }

    private fun putStr(key: String, value: String?) =
        prefs.edit { if (value == null) remove(key) else putString(key, value) }

    private fun removeKey(key: String) = prefs.edit { remove(key) }

    /** Decodes the JSON stored at [key], returning [default] when absent or malformed. */
    private inline fun <reified T> getJson(key: String, default: T): T {
        val stored = prefs.getString(key, null) ?: return default
        return try {
            json.decodeFromString<T>(stored)
        } catch (_: Exception) {
            default
        }
    }

    private inline fun <reified T> putJson(key: String, value: T) =
        prefs.edit { putString(key, json.encodeToString(value)) }

    /** Reads a per-appliance usage map, defaulting to empty. */
    private fun readUsage(key: String): Map<String, ApplianceUsage> = getJson(key, emptyMap())

    /** Persists a usage map, removing the key when the map is empty. */
    private fun writeUsage(key: String, usage: Map<String, ApplianceUsage>) {
        if (usage.isEmpty()) removeKey(key) else putJson(key, usage)
    }
}
