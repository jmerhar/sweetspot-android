package si.merhar.sweetspot.data

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import si.merhar.sweetspot.model.Appliance
import si.merhar.sweetspot.model.Countries
import si.merhar.sweetspot.model.Country
import si.merhar.sweetspot.model.PriceZone
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
        prefs.edit().putString(KEY_COUNTRY_CODE, detected.code).apply()
        return detected.code
    }

    /**
     * Persists the selected country code.
     *
     * @param code ISO 3166-1 alpha-2 country code.
     */
    fun setCountryCode(code: String) {
        prefs.edit().putString(KEY_COUNTRY_CODE, code).apply()
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
            prefs.edit().remove(KEY_PRICE_ZONE_ID).apply()
        } else {
            prefs.edit().putString(KEY_PRICE_ZONE_ID, id).apply()
        }
    }

    /**
     * Resolves the current country and zone settings to a concrete [PriceZone].
     *
     * For single-zone countries, returns the only zone. For multi-zone countries,
     * returns the stored zone or the first one if none is stored.
     *
     * @return The resolved [PriceZone].
     */
    fun getResolvedPriceZone(): PriceZone {
        val country = Countries.findByCode(getCountryCode()) ?: Countries.defaultCountry()
        val storedPriceZoneId = getPriceZoneId()
        if (storedPriceZoneId != null) {
            country.zones.find { it.id == storedPriceZoneId }?.let { return it }
        }
        return country.zones.first()
    }

    // --- Timezone ---

    /**
     * Returns the effective timezone.
     *
     * Priority:
     * 1. User's manually set timezone (if any)
     * 2. Timezone derived from the selected price zone
     */
    fun getTimeZoneId(): ZoneId {
        val stored = prefs.getString(KEY_TIMEZONE_ID, null)
        if (stored != null) {
            return try {
                ZoneId.of(stored)
            } catch (_: Exception) {
                ZoneId.of(getResolvedPriceZone().timeZoneId)
            }
        }
        return ZoneId.of(getResolvedPriceZone().timeZoneId)
    }

    /**
     * Persists a custom timezone selection.
     *
     * @param timeZoneId The timezone to store.
     */
    fun setTimeZoneId(timeZoneId: ZoneId) {
        prefs.edit().putString(KEY_TIMEZONE_ID, timeZoneId.id).apply()
    }

    /** Removes the custom timezone, reverting to zone-derived default. */
    fun clearTimeZoneId() {
        prefs.edit().remove(KEY_TIMEZONE_ID).apply()
    }

    /** Returns `true` if no custom timezone has been set (using zone-derived default). */
    fun isUsingDefaultTimezone(): Boolean {
        return prefs.getString(KEY_TIMEZONE_ID, null) == null
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
        prefs.edit().putString(KEY_APPLIANCES, json.encodeToString(appliances)).apply()
    }
}
