package today.sweetspot.data.repository

import android.content.Context
import androidx.core.content.edit

import kotlinx.serialization.json.Json
import today.sweetspot.model.Appliance
import today.sweetspot.model.Countries

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
        prefs.edit { putString(KEY_COUNTRY_CODE, detected.code) }
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
        prefs.edit { putString(KEY_COUNTRY_CODE, code) }
        clearSourceOrder()
        clearDisabledSources()
    }

    /**
     * Returns the stored price zone ID within the current country, or `null` if using the default (first zone).
     */
    fun getPriceZoneId(): String? {
        return prefs.getString(KEY_PRICE_ZONE_ID, null)
    }

    /**
     * Persists the selected price zone ID.
     *
     * @param id The [PriceZone.id], or `null` to use the country's first zone.
     */
    fun setPriceZoneId(id: String?) {
        if (id == null) {
            prefs.edit { remove(KEY_PRICE_ZONE_ID) }
        } else {
            prefs.edit { putString(KEY_PRICE_ZONE_ID, id) }
        }
    }

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
    fun setTimeZoneId(timeZoneId: ZoneId) {
        prefs.edit { putString(KEY_TIMEZONE_ID, timeZoneId.id) }
    }

    /** Removes the custom timezone, reverting to zone-derived default. */
    fun clearTimeZoneId() {
        prefs.edit { remove(KEY_TIMEZONE_ID) }
    }

    /** Returns `true` if no custom timezone has been set (using zone-derived default). */
    fun isUsingDefaultTimezone(): Boolean {
        return prefs.getString(KEY_TIMEZONE_ID, null) == null
    }

    // --- Data Source Order ---

    /**
     * Returns the user's preferred source display order, or `null` if using defaults.
     *
     * The list contains all source IDs in display/priority order (both enabled and disabled).
     */
    fun getSourceOrder(): List<String>? {
        val stored = prefs.getString(KEY_SOURCE_ORDER, null) ?: return null
        return try {
            json.decodeFromString<List<String>>(stored)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Persists the user's preferred source display order.
     *
     * @param order Ordered list of all source IDs (enabled and disabled).
     */
    fun setSourceOrder(order: List<String>) {
        prefs.edit { putString(KEY_SOURCE_ORDER, json.encodeToString(order)) }
    }

    /** Removes the custom source order, reverting to zone-specific defaults. */
    fun clearSourceOrder() {
        prefs.edit { remove(KEY_SOURCE_ORDER) }
    }

    /**
     * Returns the set of disabled source IDs.
     *
     * @return Set of disabled source IDs, or empty set if all enabled.
     */
    fun getDisabledSources(): Set<String> {
        val stored = prefs.getString(KEY_DISABLED_SOURCES, null) ?: return emptySet()
        return try {
            json.decodeFromString<Set<String>>(stored)
        } catch (_: Exception) {
            emptySet()
        }
    }

    /**
     * Persists the set of disabled source IDs.
     *
     * @param disabled Set of source IDs to disable.
     */
    fun setDisabledSources(disabled: Set<String>) {
        if (disabled.isEmpty()) {
            prefs.edit { remove(KEY_DISABLED_SOURCES) }
        } else {
            prefs.edit { putString(KEY_DISABLED_SOURCES, json.encodeToString(disabled)) }
        }
    }

    /** Removes all disabled sources, re-enabling everything. */
    fun clearDisabledSources() {
        prefs.edit { remove(KEY_DISABLED_SOURCES) }
    }

    // --- Appliances ---

    /**
     * Returns the user's saved appliances.
     *
     * @return List of appliances, or empty list if none saved or on parse error.
     */
    fun getAppliances(): List<Appliance> {
        val stored = prefs.getString(KEY_APPLIANCES, null) ?: return emptyList()
        return try {
            json.decodeFromString<List<Appliance>>(stored)
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Persists the full appliance list, replacing any previously stored list.
     *
     * @param appliances The appliances to store.
     */
    fun setAppliances(appliances: List<Appliance>) {
        prefs.edit { putString(KEY_APPLIANCES, json.encodeToString(appliances)) }
    }

    // --- EV charging ---

    /** Returns the home charger output in kW. Defaults to 11.0 (a common 3-phase wall box). */
    fun getEvHomeChargerKw(): Double =
        prefs.getFloat(KEY_EV_HOME_CHARGER_KW, DEFAULT_HOME_CHARGER_KW).toDouble()

    /**
     * Persists the home charger output.
     *
     * @param kw Charger output in kW.
     */
    fun setEvHomeChargerKw(kw: Double) {
        prefs.edit { putFloat(KEY_EV_HOME_CHARGER_KW, kw.toFloat()) }
    }

    /** Returns the default target state of charge (0–100) used to prefill the charge prompt. Defaults to 80. */
    fun getEvDefaultTargetSoc(): Int = prefs.getInt(KEY_EV_DEFAULT_TARGET_SOC, DEFAULT_TARGET_SOC)

    /**
     * Persists the default target state of charge.
     *
     * @param soc Target SoC (0–100).
     */
    fun setEvDefaultTargetSoc(soc: Int) {
        prefs.edit { putInt(KEY_EV_DEFAULT_TARGET_SOC, soc) }
    }

    /** Returns the last-used current state of charge (0–100), used to prefill the prompt. Defaults to 20. */
    fun getEvLastCurrentSoc(): Int = prefs.getInt(KEY_EV_LAST_CURRENT_SOC, DEFAULT_CURRENT_SOC)

    /**
     * Persists the last-used current state of charge.
     *
     * @param soc Current SoC (0–100).
     */
    fun setEvLastCurrentSoc(soc: Int) {
        prefs.edit { putInt(KEY_EV_LAST_CURRENT_SOC, soc) }
    }

    // --- Stats ---

    /** Returns whether API stats collection is enabled. Defaults to `false`. */
    fun isStatsEnabled(): Boolean = prefs.getBoolean(KEY_STATS_ENABLED, false)

    /**
     * Enables or disables API stats collection.
     *
     * @param enabled `true` to enable stats collection.
     */
    fun setStatsEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_STATS_ENABLED, enabled) }
    }

    /** Returns whether the one-time stats opt-in prompt has been shown. */
    fun isStatsPromptShown(): Boolean = prefs.getBoolean(KEY_STATS_PROMPT_SHOWN, false)

    /** Marks the stats opt-in prompt as shown so it is never displayed again. */
    fun setStatsPromptShown() {
        prefs.edit { putBoolean(KEY_STATS_PROMPT_SHOWN, true) }
    }

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
        prefs.edit { putLong(KEY_FIRST_LAUNCH_MS, now) }
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
    fun setUnlocked(unlocked: Boolean) {
        prefs.edit { putBoolean(KEY_UNLOCKED, unlocked) }
    }

    // --- Developer Options ---

    /** Returns whether hidden developer options have been unlocked. */
    fun isDevOptionsEnabled(): Boolean = prefs.getBoolean(KEY_DEV_OPTIONS, false)

    /** Persistently enables hidden developer options. */
    fun setDevOptionsEnabled() {
        prefs.edit { putBoolean(KEY_DEV_OPTIONS, true) }
    }

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
    fun setDevUnlocked(unlocked: Boolean) {
        prefs.edit { putBoolean(KEY_DEV_UNLOCK, unlocked) }
    }

    /** Returns whether the API fetch cooldown is disabled (developer option). */
    fun isCooldownDisabled(): Boolean = prefs.getBoolean(KEY_COOLDOWN_DISABLED, false)

    /**
     * Enables or disables the API fetch cooldown bypass.
     *
     * @param disabled `true` to skip the cooldown between API requests.
     */
    fun setCooldownDisabled(disabled: Boolean) {
        prefs.edit { putBoolean(KEY_COOLDOWN_DISABLED, disabled) }
    }

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
        if (ms == null) {
            prefs.edit { remove(KEY_TIME_OVERRIDE) }
        } else {
            prefs.edit { putLong(KEY_TIME_OVERRIDE, ms) }
        }
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
    fun setUseProductionLogo(use: Boolean) {
        prefs.edit { putBoolean(KEY_USE_PRODUCTION_LOGO, use) }
    }

    // --- Theme ---

    /**
     * Returns the user's preferred theme mode key.
     *
     * @return One of `"system"`, `"light"`, or `"dark"`. Defaults to `"system"`.
     */
    fun getThemeMode(): String {
        return prefs.getString(KEY_THEME_MODE, "system") ?: "system"
    }

    /**
     * Persists the user's preferred theme mode key.
     *
     * @param mode One of `"system"`, `"light"`, or `"dark"`.
     */
    fun setThemeMode(mode: String) {
        prefs.edit { putString(KEY_THEME_MODE, mode) }
    }
}
