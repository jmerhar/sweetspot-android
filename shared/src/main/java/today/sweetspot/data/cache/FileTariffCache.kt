package today.sweetspot.data.cache

import android.content.Context
import androidx.core.content.edit
import java.io.File

/**
 * File-based [TariffCache] backed by the Android cache directory and SharedPreferences.
 *
 * Stores each country's raw feed JSON in `cacheDir/tariff_<cc>.json` and its fetch timestamp in the
 * shared `sweetspot_cache` SharedPreferences (key `tariff_fetch_ms_<cc>`). Returns null on any read
 * error so a corrupt cache simply triggers a re-fetch.
 *
 * @param context Android context for the cache directory and SharedPreferences.
 */
class FileTariffCache(private val context: Context) : TariffCache {

    private val prefs = context.getSharedPreferences("sweetspot_cache", Context.MODE_PRIVATE)

    private companion object {
        const val FILE_PREFIX = "tariff_"
        const val FILE_SUFFIX = ".json"
        const val TS_PREFIX = "tariff_fetch_ms_"
    }

    private fun cacheFile(countryCode: String): File =
        File(context.cacheDir, "$FILE_PREFIX${countryCode.lowercase()}$FILE_SUFFIX")

    private fun tsKey(countryCode: String): String = "$TS_PREFIX${countryCode.lowercase()}"

    @Synchronized
    override fun read(countryCode: String): RawTariff? {
        val file = cacheFile(countryCode)
        if (!file.exists()) return null
        val ts = prefs.getLong(tsKey(countryCode), 0L)
        if (ts == 0L) return null
        return try {
            RawTariff(file.readText(), ts)
        } catch (_: Exception) {
            null
        }
    }

    @Synchronized
    override fun write(countryCode: String, raw: String, fetchedAtMs: Long) {
        cacheFile(countryCode).writeText(raw)
        prefs.edit { putLong(tsKey(countryCode), fetchedAtMs) }
    }

    @Synchronized
    override fun clear() {
        context.cacheDir.listFiles()
            ?.filter { it.name.startsWith(FILE_PREFIX) && it.name.endsWith(FILE_SUFFIX) }
            ?.forEach { it.delete() }
        prefs.edit {
            prefs.all.keys.filter { it.startsWith(TS_PREFIX) }.forEach { remove(it) }
        }
    }
}
