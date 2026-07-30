package today.sweetspot.data.api

import today.sweetspot.util.sweetSpotJson
import okhttp3.OkHttpClient
import okhttp3.Request
import today.sweetspot.model.SupplierTariffs

/**
 * Abstraction for fetching a country's all-in tariff feed, so [today.sweetspot.data.repository.TariffRepository]
 * can be tested with a fake. Returns the raw JSON; parsing is the repository's concern.
 */
fun interface TariffFetcher {

    /**
     * Fetches the raw tariff feed JSON for a country.
     *
     * @param countryCode ISO 3166-1 alpha-2 code (e.g. "NL"); the feed lives at `<lowercase>.json`.
     * @return The raw feed JSON.
     * @throws HttpException on a non-200 response (404 = no feed for this country).
     */
    fun fetchRaw(countryCode: String): String
}

/**
 * Client for the SweetSpot all-in tariff feed published by `bin/build-suppliers.py` at
 * `https://sweetspot.today/data/suppliers/<cc>.json`. The raw JSON is cached by
 * [today.sweetspot.data.cache.TariffCache].
 */
class TariffApi(private val client: OkHttpClient = sharedHttpClient) : TariffFetcher {

    private val json = sweetSpotJson

    companion object {
        /** Base URL for the per-country tariff feeds; the lowercase country code + ".json" is appended. */
        const val BASE_URL = "https://sweetspot.today/data/suppliers/"
    }

    override fun fetchRaw(countryCode: String): String {
        val url = BASE_URL + countryCode.lowercase() + ".json"
        val request = Request.Builder().url(url).get().build()
        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw HttpException(response.code, "Tariff feed returned ${response.code}")
            }
            response.body.string()
        }
    }

    /** Parses raw tariff JSON into [SupplierTariffs]. Exposed for tests. */
    fun parse(raw: String): SupplierTariffs = json.decodeFromString(raw)
}
