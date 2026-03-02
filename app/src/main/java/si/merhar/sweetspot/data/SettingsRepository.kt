package si.merhar.sweetspot.data

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import si.merhar.sweetspot.model.Appliance
import java.time.ZoneId

class SettingsRepository(context: Context) {

    private val prefs = context.getSharedPreferences("sweetspot_settings", Context.MODE_PRIVATE)

    private companion object {
        const val KEY_ZONE_ID = "zone_id"
        const val KEY_APPLIANCES = "appliances"
    }

    fun getZoneId(): ZoneId {
        val stored = prefs.getString(KEY_ZONE_ID, null)
        return if (stored != null) ZoneId.of(stored) else ZoneId.systemDefault()
    }

    fun setZoneId(zoneId: ZoneId) {
        prefs.edit().putString(KEY_ZONE_ID, zoneId.id).apply()
    }

    fun clearZoneId() {
        prefs.edit().remove(KEY_ZONE_ID).apply()
    }

    fun isUsingDefaultZone(): Boolean {
        return prefs.getString(KEY_ZONE_ID, null) == null
    }

    fun getAppliances(): List<Appliance> {
        val json = prefs.getString(KEY_APPLIANCES, null) ?: return emptyList()
        return try {
            Json.decodeFromString<List<Appliance>>(json)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun setAppliances(appliances: List<Appliance>) {
        prefs.edit().putString(KEY_APPLIANCES, Json.encodeToString(appliances)).apply()
    }
}
