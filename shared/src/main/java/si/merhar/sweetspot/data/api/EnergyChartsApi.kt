package si.merhar.sweetspot.data.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import si.merhar.sweetspot.model.PriceSlot
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/**
 * JSON response from the Energy-Charts day-ahead price API.
 *
 * Prices are in EUR/MWh. The [unix_seconds] and [price] arrays are parallel —
 * index `i` in both arrays represents the same time slot. Null entries in [price]
 * indicate gaps (no auction result) and are filtered out during parsing.
 *
 * @property unix_seconds Unix timestamps (seconds) for the start of each slot.
 * @property price Day-ahead prices in EUR/MWh, with `null` for gaps.
 */
@Serializable
internal data class EnergyChartsResponse(
    val unix_seconds: List<Long>,
    val price: List<Double?>
)

/**
 * Client for the Energy-Charts day-ahead price API.
 *
 * Covers 15 European bidding zones with 15-minute or 60-minute resolution
 * (zone-dependent). Prices are returned in EUR/MWh and converted to EUR/kWh
 * during parsing. No authentication required. Licensed under CC BY 4.0.
 *
 * Resolution is auto-detected from the gap between the first two timestamps:
 * 900 seconds → 15-minute slots, 3600 seconds → 60-minute slots.
 *
 * @param bzn Energy-Charts zone code (e.g. `"DE-LU"`, `"AT"`, `"IT-North"`).
 */
class EnergyChartsApi(private val bzn: String) : PriceFetcher {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Fetches and parses electricity prices from the Energy-Charts API.
     *
     * @param from Start of the requested period (inclusive).
     * @param to End of the requested period (exclusive).
     * @param timeZoneId Timezone to convert timestamps to local time.
     * @return A [FetchResult] with sorted price slots and source "Energy-Charts".
     * @throws RuntimeException if the HTTP request fails.
     */
    override fun fetchPrices(from: Instant, to: Instant, timeZoneId: ZoneId): FetchResult {
        return FetchResult(parse(fetchRaw(from, to), timeZoneId), "Energy-Charts")
    }

    /**
     * Fetches raw JSON from the Energy-Charts API for the given date range.
     *
     * @param from Start of the requested period (inclusive).
     * @param to End of the requested period (exclusive).
     * @return Raw JSON response body.
     * @throws RuntimeException if the HTTP request fails or the body is empty.
     */
    fun fetchRaw(from: Instant, to: Instant): String {
        val url = "https://api.energy-charts.info/price" +
            "?bzn=$bzn" +
            "&start=${DateTimeFormatter.ISO_INSTANT.format(from)}" +
            "&end=${DateTimeFormatter.ISO_INSTANT.format(to)}"

        val request = Request.Builder().url(url).get().build()
        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("Energy-Charts API returned ${response.code}")
            }

            response.body.string()
        }
    }

    /**
     * Parses raw Energy-Charts JSON into a sorted list of [PriceSlot] entries.
     *
     * Filters out entries where the price is `null` (gaps with no auction result).
     * Converts EUR/MWh to EUR/kWh by dividing by 1000. Auto-detects slot duration
     * from the gap between the first two timestamps (15 or 60 minutes).
     *
     * @param raw Raw JSON string from [fetchRaw].
     * @param timeZoneId Timezone to convert timestamps to local time.
     * @return Chronologically sorted list of price slots.
     * @throws IllegalArgumentException if the parallel arrays have different lengths.
     */
    fun parse(raw: String, timeZoneId: ZoneId): List<PriceSlot> {
        val parsed = json.decodeFromString<EnergyChartsResponse>(raw)

        require(parsed.unix_seconds.size == parsed.price.size) {
            "Mismatched array lengths: ${parsed.unix_seconds.size} timestamps vs ${parsed.price.size} prices"
        }

        if (parsed.unix_seconds.isEmpty()) return emptyList()

        val durationMinutes = if (parsed.unix_seconds.size >= 2) {
            ((parsed.unix_seconds[1] - parsed.unix_seconds[0]) / 60).toInt()
        } else {
            60 // Default to hourly for single-entry responses
        }

        return parsed.unix_seconds.zip(parsed.price)
            .filter { (_, price) -> price != null }
            .map { (epochSecond, price) ->
                val time = Instant.ofEpochSecond(epochSecond).atZone(timeZoneId)
                PriceSlot(time = time, price = price!! / 1000.0, durationMinutes = durationMinutes)
            }
            .sortedBy { it.time }
    }

    companion object {
        /**
         * Maps SweetSpot zone IDs to Energy-Charts `bzn` parameter values.
         *
         * Most zone IDs map directly; exceptions are `DE_LU` → `"DE-LU"`
         * and `IT_NORD` → `"IT-North"`.
         */
        val ZONE_TO_BZN: Map<String, String> = mapOf(
            "AT" to "AT",
            "BE" to "BE",
            "CH" to "CH",
            "CZ" to "CZ",
            "DE_LU" to "DE-LU",
            "DK1" to "DK1",
            "DK2" to "DK2",
            "FR" to "FR",
            "HU" to "HU",
            "IT_NORD" to "IT-North",
            "NL" to "NL",
            "NO2" to "NO2",
            "PL" to "PL",
            "SE4" to "SE4",
            "SI" to "SI"
        )
    }
}
