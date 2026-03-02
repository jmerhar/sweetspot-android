package si.merhar.sweetspot.data

/**
 * Cache for raw API price responses.
 *
 * Abstracts the storage mechanism so [PriceRepository] can be tested
 * without Android dependencies.
 */
interface PriceCache {

    /**
     * Checks whether enough time has passed since the last API fetch.
     *
     * @param cooldownMs Minimum interval between fetches in milliseconds.
     * @return `true` if at least [cooldownMs] have elapsed since the last fetch.
     */
    fun isCooldownElapsed(cooldownMs: Long): Boolean

    /**
     * Reads the cached JSON, if available.
     *
     * @return Raw JSON string, or `null` if no cached data exists.
     */
    fun readCachedJson(): String?

    /**
     * Writes raw JSON to cache and records the fetch timestamp.
     *
     * @param json Raw JSON string to cache.
     */
    fun write(json: String)
}
