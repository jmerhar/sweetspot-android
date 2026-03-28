package si.merhar.sweetspot.data.cache

/**
 * A single cached price entry, stored in a timezone-agnostic format.
 *
 * The epoch second is UTC so the cache is independent of any particular timezone.
 * The zone is applied when converting back to [si.merhar.sweetspot.model.PriceSlot].
 *
 * @property epochSecond UTC epoch second for the start of this slot.
 * @property durationMinutes Length of this slot in minutes (e.g. 60 for hourly, 15 for quarter-hourly).
 * @property price Price in EUR per kWh.
 */
data class CachedPrice(
    val epochSecond: Long,
    val durationMinutes: Int,
    val price: Double
)

/**
 * Wrapper for cached price data that includes the data source name.
 *
 * @property source Human-readable name of the data source (e.g. "ENTSO-E", "EnergyZero").
 * @property prices Cached price entries.
 */
data class CachedPriceData(
    val source: String,
    val prices: List<CachedPrice>
)

/**
 * Cache for parsed electricity prices, keyed by zone.
 *
 * Abstracts the storage mechanism so [PriceRepository] can be tested
 * without Android dependencies.
 */
interface PriceCache {

    /**
     * Checks whether enough time has passed since the last API fetch.
     *
     * Cooldown is global (not per-key) since multiple zones may share the
     * same upstream API, and we want to respect its rate limits.
     *
     * @param cooldownMs Minimum interval between fetches in milliseconds.
     * @return `true` if at least [cooldownMs] have elapsed since the last fetch.
     */
    fun isCooldownElapsed(cooldownMs: Long): Boolean

    /**
     * Reads cached prices for the given zone key, if available.
     *
     * @param key Zone identifier (e.g. `"NL"`, `"DE_LU"`).
     * @return Cached price data including source name, or `null` if no cached data exists or on any error.
     */
    fun readCached(key: String): CachedPriceData?

    /**
     * Writes parsed prices to cache and records the fetch timestamp.
     *
     * @param key Zone identifier (e.g. `"NL"`, `"DE_LU"`).
     * @param data Cached price data including source name and price entries.
     */
    fun write(key: String, data: CachedPriceData)

    /**
     * Deletes all cached price data across all zones.
     *
     * Does not reset the fetch timestamp (cooldown remains in effect).
     */
    fun clear()

    /**
     * Deletes cached price data for a specific zone.
     *
     * Does not reset the fetch timestamp (cooldown remains in effect).
     *
     * @param key Zone identifier (e.g. `"NL"`, `"DE_LU"`).
     */
    fun clearForZone(key: String)

    /**
     * Returns the remaining cooldown time in milliseconds before the next API fetch is allowed.
     *
     * @param cooldownMs Minimum interval between fetches in milliseconds.
     * @return Remaining time in milliseconds, or 0 if the cooldown has elapsed.
     */
    fun cooldownRemainingMs(cooldownMs: Long): Long
}
