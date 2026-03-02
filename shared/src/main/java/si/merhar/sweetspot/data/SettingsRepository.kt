package si.merhar.sweetspot.data

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import si.merhar.sweetspot.model.Appliance
import java.time.ZoneId

/**
 * Persistence layer for user settings.
 *
 * Stores timezone preference and appliance list in SharedPreferences.
 * Appliances are JSON-serialized via kotlinx-serialization.
 *
 * @param context Android context for SharedPreferences access.
 */
class SettingsRepository(context: Context) {

    private val prefs = context.getSharedPreferences("sweetspot_settings", Context.MODE_PRIVATE)

    private companion object {
        const val KEY_ZONE_ID = "zone_id"
        const val KEY_APPLIANCES = "appliances"

        /** Lenient parser that ignores unknown fields for forward compatibility. */
        val json = Json { ignoreUnknownKeys = true }
    }

    /**
     * Returns the user's configured timezone, or the system default if none is set
     * or if the stored value is invalid.
     */
    fun getZoneId(): ZoneId {
        val stored = prefs.getString(KEY_ZONE_ID, null) ?: return ZoneId.systemDefault()
        return try {
            ZoneId.of(stored)
        } catch (_: Exception) {
            ZoneId.systemDefault()
        }
    }

    /**
     * Persists a custom timezone selection.
     *
     * @param zoneId The timezone to store.
     */
    fun setZoneId(zoneId: ZoneId) {
        prefs.edit().putString(KEY_ZONE_ID, zoneId.id).apply()
    }

    /** Removes the custom timezone, reverting to system default. */
    fun clearZoneId() {
        prefs.edit().remove(KEY_ZONE_ID).apply()
    }

    /** Returns `true` if no custom timezone has been set. */
    fun isUsingDefaultZone(): Boolean {
        return prefs.getString(KEY_ZONE_ID, null) == null
    }

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
