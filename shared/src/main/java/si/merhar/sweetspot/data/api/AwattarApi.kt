package si.merhar.sweetspot.data.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import si.merhar.sweetspot.model.PriceSlot
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * JSON response from the aWATTar day-ahead price API.
 *
 * @property data List of price entries, each with start/end timestamps and a market price.
 */
@Serializable
internal data class AwattarResponse(val data: List<AwattarEntry>)

/**
 * A single price entry from the aWATTar API.
 *
 * @property start_timestamp Start of the slot as milliseconds since epoch.
 * @property end_timestamp End of the slot as milliseconds since epoch.
 * @property marketprice Day-ahead price in EUR/MWh.
 */
@Serializable
internal data class AwattarEntry(
    val start_timestamp: Long,
    val end_timestamp: Long,
    val marketprice: Double
)

/**
 * Client for the aWATTar day-ahead price API.
 *
 * Covers AT and DE-LU with hourly resolution. Prices are returned in EUR/MWh
 * and converted to EUR/kWh during parsing. No authentication required.
 *
 * Two regional endpoints: `api.awattar.at` for Austria, `api.awattar.de` for Germany/Luxembourg.
 *
 * @param zoneId SweetSpot zone ID (`"AT"` or `"DE_LU"`).
 */
class AwattarApi(zoneId: String) : PriceFetcher {

    private val baseUrl = ZONE_TO_BASE_URL[zoneId]
        ?: error("No aWATTar mapping for zone: $zoneId")

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Fetches and parses electricity prices from the aWATTar API.
     *
     * @param from Start of the requested period (inclusive).
     * @param to End of the requested period (exclusive).
     * @param timeZoneId Timezone to convert timestamps to local time.
     * @return A [FetchResult] with sorted price slots and source "aWATTar".
     * @throws RuntimeException if the HTTP request fails.
     */
    override fun fetchPrices(from: Instant, to: Instant, timeZoneId: ZoneId): FetchResult {
        return FetchResult(parse(fetchRaw(from, to), timeZoneId), "aWATTar")
    }

    /**
     * Fetches raw JSON from the aWATTar API for the given time range.
     *
     * @param from Start of the requested period (inclusive).
     * @param to End of the requested period (exclusive).
     * @return Raw JSON response body.
     * @throws RuntimeException if the HTTP request fails or the body is empty.
     */
    fun fetchRaw(from: Instant, to: Instant): String {
        val url = "$baseUrl/v1/marketdata" +
            "?start=${from.toEpochMilli()}" +
            "&end=${to.toEpochMilli()}"

        val request = Request.Builder().url(url).get().build()
        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("aWATTar API returned ${response.code}")
            }

            response.body.string()
        }
    }

    /**
     * Parses raw aWATTar JSON into a sorted list of [PriceSlot] entries.
     *
     * Converts EUR/MWh to EUR/kWh by dividing by 1000. Computes slot duration
     * from the difference between start and end timestamps.
     *
     * @param raw Raw JSON string from [fetchRaw].
     * @param timeZoneId Timezone to convert timestamps to local time.
     * @return Chronologically sorted list of price slots.
     */
    fun parse(raw: String, timeZoneId: ZoneId): List<PriceSlot> {
        val parsed = json.decodeFromString<AwattarResponse>(raw)

        return parsed.data.map { entry ->
            val time = Instant.ofEpochMilli(entry.start_timestamp).atZone(timeZoneId)
            val durationMinutes = ((entry.end_timestamp - entry.start_timestamp) / 60_000).toInt()
            PriceSlot(time = time, price = entry.marketprice / 1000.0, durationMinutes = durationMinutes)
        }.sortedBy { it.time }
    }

    companion object {
        /**
         * Maps SweetSpot zone IDs to aWATTar base URLs.
         *
         * `"AT"` uses the Austrian endpoint, `"DE_LU"` uses the German endpoint.
         */
        val ZONE_TO_BASE_URL: Map<String, String> = mapOf(
            "AT" to "https://api.awattar.at",
            "DE_LU" to "https://api.awattar.de"
        )
    }
}
