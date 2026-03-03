package si.merhar.sweetspot.data

import android.content.Context
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File

/**
 * File-based [PriceCache] implementation backed by the Android cache directory
 * and SharedPreferences.
 *
 * Stores parsed prices in per-zone binary files (`cacheDir/prices_<key>.bin`)
 * and tracks the last fetch timestamp in SharedPreferences to enforce a minimum
 * interval between API requests (cooldown).
 *
 * Binary format per file (v2):
 * - `version: Byte` (currently 2)
 * - `count: Int` (number of entries)
 * - N × (`epochSecond: Long` | `durationMinutes: Short` | `price: Double`) — 18 bytes per entry
 *
 * V1 caches are gracefully migrated by returning `null` (triggers a re-fetch).
 *
 * @param context Android context for accessing cache directory and SharedPreferences.
 */
class FilePriceCache(private val context: Context) : PriceCache {

    private val prefs = context.getSharedPreferences("sweetspot_cache", Context.MODE_PRIVATE)

    private companion object {
        const val KEY_LAST_FETCH_MS = "last_fetch_ms"
        const val FORMAT_VERSION: Byte = 2
    }

    /** Returns the per-zone cache file for the given key. */
    private fun cacheFile(key: String): File = File(context.cacheDir, "prices_$key.bin")

    override fun isCooldownElapsed(cooldownMs: Long): Boolean {
        val lastFetch = prefs.getLong(KEY_LAST_FETCH_MS, 0L)
        return System.currentTimeMillis() - lastFetch >= cooldownMs
    }

    override fun readCached(key: String): List<CachedPrice>? {
        val file = cacheFile(key)
        if (!file.exists()) return null
        return try {
            DataInputStream(file.inputStream().buffered()).use { input ->
                val version = input.readByte()
                if (version != FORMAT_VERSION) return null
                val count = input.readInt()
                List(count) {
                    CachedPrice(
                        epochSecond = input.readLong(),
                        durationMinutes = input.readShort().toInt(),
                        price = input.readDouble()
                    )
                }
            }
        } catch (_: Exception) {
            // Graceful migration: any I/O or format error returns null
            null
        }
    }

    override fun write(key: String, prices: List<CachedPrice>) {
        DataOutputStream(cacheFile(key).outputStream().buffered()).use { output ->
            output.writeByte(FORMAT_VERSION.toInt())
            output.writeInt(prices.size)
            for (entry in prices) {
                output.writeLong(entry.epochSecond)
                output.writeShort(entry.durationMinutes)
                output.writeDouble(entry.price)
            }
        }
        prefs.edit()
            .putLong(KEY_LAST_FETCH_MS, System.currentTimeMillis())
            .apply()
    }
}
