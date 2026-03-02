package si.merhar.sweetspot.data

import android.content.Context
import java.io.File

/**
 * File-based [PriceCache] implementation backed by the Android cache directory
 * and SharedPreferences.
 *
 * Stores raw JSON in `cacheDir/prices_cache.json` and tracks the last fetch
 * timestamp in SharedPreferences to enforce a minimum interval between API
 * requests (cooldown).
 *
 * @param context Android context for accessing cache directory and SharedPreferences.
 */
class FilePriceCache(private val context: Context) : PriceCache {

    private val prefs = context.getSharedPreferences("sweetspot_cache", Context.MODE_PRIVATE)
    private val cacheFile = File(context.cacheDir, "prices_cache.json")

    private companion object {
        const val KEY_LAST_FETCH_MS = "last_fetch_ms"
    }

    override fun isCooldownElapsed(cooldownMs: Long): Boolean {
        val lastFetch = prefs.getLong(KEY_LAST_FETCH_MS, 0L)
        return System.currentTimeMillis() - lastFetch >= cooldownMs
    }

    override fun readCachedJson(): String? {
        if (!cacheFile.exists()) return null
        return cacheFile.readText()
    }

    override fun write(json: String) {
        cacheFile.writeText(json)
        prefs.edit()
            .putLong(KEY_LAST_FETCH_MS, System.currentTimeMillis())
            .apply()
    }
}
