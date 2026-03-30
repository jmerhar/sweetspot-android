package today.sweetspot.data.repository

import android.content.Context
import androidx.core.content.edit

import kotlinx.serialization.json.Json
import today.sweetspot.model.Appliance
import today.sweetspot.model.Countries

import today.sweetspot.model.PriceZone
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
        const val KEY_PRICE_ZONE_ID = "price_zone_id"
        const val KEY_SOURCE_ORDER = "source_order"
        const val KEY_DISABLED_SOURCES = "disabled_sources"
        const val KEY_STATS_ENABLED = "stats_enabled"
        const val KEY_STATS_PROMPT_SHOWN = "stats_prompt_shown"
        const val KEY_FIRST_LAUNCH_MS = "first_launch_ms"
        const val KEY_UNLOCKED = "unlocked"

        /** Trial duration in days. */
        const val TRIAL_DAYS = 14

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
        val elapsed = System.currentTimeMillis() - getFirstLaunchMs()
        val elapsedDays = (elapsed / (24 * 60 * 60 * 1000L)).toInt()
        return (TRIAL_DAYS - elapsedDays).coerceIn(0, TRIAL_DAYS)
    }

    /**
     * Returns `true` if the free trial has expired and the app has not been unlocked.
     *
     * The trial lasts [TRIAL_DAYS] days from first launch.
     */
    fun isTrialExpired(): Boolean {
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
}
