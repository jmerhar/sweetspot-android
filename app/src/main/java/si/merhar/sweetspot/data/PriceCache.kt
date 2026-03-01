package si.merhar.sweetspot.data

import android.content.Context
import java.io.File
import java.time.LocalDate

class PriceCache(private val context: Context) {

    private val prefs = context.getSharedPreferences("sweetspot_cache", Context.MODE_PRIVATE)
    private val cacheFile = File(context.filesDir, "prices_cache.json")

    private companion object {
        const val KEY_CACHE_DATE = "cache_date"
    }

    fun isFresh(todayAmsterdam: LocalDate): Boolean {
        val cached = prefs.getString(KEY_CACHE_DATE, null) ?: return false
        return cached == todayAmsterdam.toString()
    }

    fun readCachedJson(): String? {
        if (!cacheFile.exists()) return null
        return cacheFile.readText()
    }

    fun write(json: String, dateAmsterdam: LocalDate) {
        cacheFile.writeText(json)
        prefs.edit().putString(KEY_CACHE_DATE, dateAmsterdam.toString()).apply()
    }
}
