package si.merhar.sweetspot.data

import android.content.Context
import java.io.File
import java.time.LocalDate

/**
 * File-based cache for EnergyZero API responses.
 *
 * Stores raw JSON in `cacheDir/prices_cache.json` and tracks freshness by date
 * in SharedPreferences. The cache is considered "fresh" if it was written today
 * (in the configured timezone).
 *
 * @param context Android context for accessing cache directory and SharedPreferences.
 */
class PriceCache(private val context: Context) {

    private val prefs = context.getSharedPreferences("sweetspot_cache", Context.MODE_PRIVATE)
    private val cacheFile = File(context.cacheDir, "prices_cache.json")

    private companion object {
        const val KEY_CACHE_DATE = "cache_date"
    }

    /**
     * Checks whether the cache is fresh for the given date.
     *
     * @param todayAmsterdam Today's date in the configured timezone.
     * @return `true` if cached data exists and was fetched today.
     */
    fun isFresh(todayAmsterdam: LocalDate): Boolean {
        val cached = prefs.getString(KEY_CACHE_DATE, null) ?: return false
        return cached == todayAmsterdam.toString()
    }

    /**
     * Reads the cached JSON, if the cache file exists.
     *
     * @return Raw JSON string, or `null` if no cache file is present.
     */
    fun readCachedJson(): String? {
        if (!cacheFile.exists()) return null
        return cacheFile.readText()
    }

    /**
     * Writes raw JSON to the cache file and records the fetch date.
     *
     * @param json Raw JSON string to cache.
     * @param dateAmsterdam The date this data was fetched for.
     */
    fun write(json: String, dateAmsterdam: LocalDate) {
        cacheFile.writeText(json)
        prefs.edit().putString(KEY_CACHE_DATE, dateAmsterdam.toString()).apply()
    }
}
