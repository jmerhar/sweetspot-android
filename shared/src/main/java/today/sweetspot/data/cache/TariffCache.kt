package today.sweetspot.data.cache

/**
 * A cached tariff feed with the time it was fetched.
 *
 * @property raw Raw feed JSON (parsed by [today.sweetspot.data.repository.TariffRepository]).
 * @property fetchedAtMs Epoch millis when this copy was fetched (drives the UI stale warning).
 */
data class RawTariff(
    val raw: String,
    val fetchedAtMs: Long
)

/**
 * Caches raw tariff feed JSON per country, with a fetch timestamp. Abstracts storage so
 * [today.sweetspot.data.repository.TariffRepository] can be tested without Android.
 */
interface TariffCache {

    /** Returns the cached feed for a country, or null if none is stored. */
    fun read(countryCode: String): RawTariff?

    /** Stores the raw feed for a country, stamping it with [fetchedAtMs]. */
    fun write(countryCode: String, raw: String, fetchedAtMs: Long)

    /** Deletes all cached tariff feeds. */
    fun clear()
}
