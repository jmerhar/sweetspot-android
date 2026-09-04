package today.sweetspot.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import today.sweetspot.util.sweetSpotJson
import okhttp3.OkHttpClient
import okhttp3.Request
import today.sweetspot.model.PriceSlot
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * JSON response from the Energy-Charts day-ahead price API.
 *
 * Prices are in EUR/MWh. The [unixSeconds] and [price] arrays are parallel —
 * index `i` in both arrays represents the same time slot. Null entries in [price]
 * indicate gaps (no auction result) and are filtered out during parsing.
 *
 * @property unixSeconds Unix timestamps (seconds) for the start of each slot.
 * @property price Day-ahead prices in EUR/MWh, with `null` for gaps.
 */
@Serializable
internal data class EnergyChartsResponse(
    @SerialName("unix_seconds") val unixSeconds: List<Long>,
    val price: List<Double?>
)

/**
 * Client for the Energy-Charts day-ahead price API.
 *
 * Covers 30 European bidding zones with 15-minute or 60-minute resolution
 * (zone-dependent). Prices are returned in EUR/MWh and converted to EUR/kWh
 * during parsing. No authentication required. Licensed under CC BY 4.0.
 *
 * Resolution is auto-detected from the gap between the first two timestamps:
 * 900 seconds → 15-minute slots, 3600 seconds → 60-minute slots.
 *
 * @param zoneId SweetSpot zone ID (e.g. `"DE_LU"`, `"AT"`, `"IT_NORD"`).
 */
class EnergyChartsApi(
    zoneId: String,
    private val client: OkHttpClient = sharedHttpClient
) : PriceFetcher {

    private val bzn = ZONE_TO_BZN[zoneId]
        ?: error("No Energy-Charts mapping for zone: $zoneId")

    private val json = sweetSpotJson

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
                throw HttpException(response.code, "Energy-Charts API returned ${response.code}")
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

        require(parsed.unixSeconds.size == parsed.price.size) {
            "Mismatched array lengths: ${parsed.unixSeconds.size} timestamps vs ${parsed.price.size} prices"
        }

        if (parsed.unixSeconds.isEmpty()) return emptyList()

        val durationMinutes = if (parsed.unixSeconds.size >= 2) {
            ((parsed.unixSeconds[1] - parsed.unixSeconds[0]) / 60).toInt()
        } else {
            60 // Default to hourly for single-entry responses
        }

        return parsed.unixSeconds.zip(parsed.price)
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
         * Most zone IDs map directly. The exceptions are `DE_LU` → `"DE-LU"`
         * and the Italian zones, which Energy-Charts spells out in full
         * (`IT_NORD` → `"IT-North"`, `IT_CSUD` → `"IT-Centre-South"`, …).
         *
         * Zones absent here fall back to ENTSO-E alone, so a zone is added only
         * once Energy-Charts has been confirmed to serve real prices for it —
         * an unrecognised `bzn` is answered with HTTP 400, which would present
         * as a dead fallback rather than a missing one.
         */
        val ZONE_TO_BZN: Map<String, String> = mapOf(
            "AT" to "AT",
            "BE" to "BE",
            "BG" to "BG",
            "CH" to "CH",
            "CZ" to "CZ",
            "DE_LU" to "DE-LU",
            "DK1" to "DK1",
            "DK2" to "DK2",
            "ES" to "ES",
            "FR" to "FR",
            "GR" to "GR",
            "HR" to "HR",
            "HU" to "HU",
            "IT_CALA" to "IT-Calabria",
            "IT_CNOR" to "IT-Centre-North",
            "IT_CSUD" to "IT-Centre-South",
            "IT_NORD" to "IT-North",
            "IT_SARD" to "IT-Sardinia",
            "IT_SICI" to "IT-Sicily",
            "IT_SUD" to "IT-South",
            "ME" to "ME",
            "NL" to "NL",
            "NO2" to "NO2",
            "PL" to "PL",
            "PT" to "PT",
            "RO" to "RO",
            "RS" to "RS",
            "SE4" to "SE4",
            "SI" to "SI",
            "SK" to "SK"
        )
    }
}
