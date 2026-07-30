package today.sweetspot.data.repository

import today.sweetspot.util.sweetSpotJson
import today.sweetspot.data.api.TariffApi
import today.sweetspot.data.api.TariffFetcher
import today.sweetspot.data.cache.TariffCache
import today.sweetspot.model.SupplierTariffs
import java.time.Clock

/**
 * A usable tariff feed with the time it was fetched.
 *
 * @property tariff The parsed, usable feed.
 * @property fetchedAtMs Epoch millis when it was fetched (the ViewModel uses this to decide whether to
 *   show the "data may be out of date" warning).
 */
data class CachedTariff(
    val tariff: SupplierTariffs,
    val fetchedAtMs: Long
)

/**
 * Provides a country's all-in tariff feed, backed by [TariffCache].
 *
 * The repository holds **no scheduling** — the caller (ViewModel) decides *when* to [refresh]
 * (piggybacked on the spot-price fetch, on a country change, or to bootstrap when nothing is cached).
 * A cached copy is served regardless of age; staleness only affects the UI warning, computed by the
 * caller from [CachedTariff.fetchedAtMs]. All-in is unavailable only when there is no usable cached
 * feed at all.
 *
 * @param cache Raw-JSON cache keyed by country code.
 * @param fetcher Fetches the raw feed from the network.
 * @param clock Clock for stamping fetch times (injectable for testing).
 */
class TariffRepository(
    private val cache: TariffCache,
    private val fetcher: TariffFetcher = TariffApi(),
    private val clock: Clock = Clock.systemUTC()
) {

    private val json = sweetSpotJson

    /** Parses the cached feed for a country; returns it only when usable, else null. No network. */
    fun cached(countryCode: String): CachedTariff? {
        val raw = cache.read(countryCode) ?: return null
        val tariff = parseUsable(raw.raw) ?: return null
        return CachedTariff(tariff, raw.fetchedAtMs)
    }

    /**
     * Fetches a fresh feed and caches it when usable, returning it. On any failure — network error,
     * non-200, unparseable body, or `usable:false` — the existing cache is left untouched (keep
     * last-good) and the current [cached] value is returned, so a bad refresh never worsens the data.
     */
    fun refresh(countryCode: String): CachedTariff? {
        return try {
            val raw = fetcher.fetchRaw(countryCode)
            val tariff = parseUsable(raw) ?: return cached(countryCode)
            val now = clock.millis()
            cache.write(countryCode, raw, now)
            CachedTariff(tariff, now)
        } catch (_: Exception) {
            cached(countryCode)
        }
    }

    /** Parses raw JSON to [SupplierTariffs], returning it only if `usable`, else null on any problem. */
    private fun parseUsable(raw: String): SupplierTariffs? = try {
        json.decodeFromString<SupplierTariffs>(raw).takeIf { it.usable }
    } catch (_: Exception) {
        null
    }
}
