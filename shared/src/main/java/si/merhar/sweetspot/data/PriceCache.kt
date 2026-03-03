package si.merhar.sweetspot.data

/**
 * A single cached price entry, stored in a timezone-agnostic format.
 *
 * The epoch second is UTC so the cache is independent of any particular timezone.
 * The zone is applied when converting back to [si.merhar.sweetspot.model.HourlyPrice].
 *
 * @property epochSecond UTC epoch second for the start of this hourly slot.
 * @property price Price in EUR per kWh.
 */
data class CachedPrice(
    val epochSecond: Long,
    val price: Double
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
     * same upstream API and we want to respect its rate limits.
     *
     * @param cooldownMs Minimum interval between fetches in milliseconds.
     * @return `true` if at least [cooldownMs] have elapsed since the last fetch.
     */
    fun isCooldownElapsed(cooldownMs: Long): Boolean

    /**
     * Reads cached prices for the given zone key, if available.
     *
     * @param key Zone identifier (e.g. `"NL"`, `"DE_LU"`).
     * @return Cached price list, or `null` if no cached data exists or on any error.
     */
    fun readCached(key: String): List<CachedPrice>?

    /**
     * Writes parsed prices to cache and records the fetch timestamp.
     *
     * @param key Zone identifier (e.g. `"NL"`, `"DE_LU"`).
     * @param prices Parsed price entries to cache.
     */
    fun write(key: String, prices: List<CachedPrice>)
}
