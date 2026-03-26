package si.merhar.sweetspot.data.api

import si.merhar.sweetspot.model.PriceSlot
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Request
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * A single price entry from the EnergyZero API response.
 *
 * @property readingDate ISO-8601 timestamp string for the start of this hourly slot.
 * @property price Electricity price in EUR/kWh.
 */
@Serializable
internal data class EnergyZeroPriceEntry(
    val readingDate: String,
    val price: Double
)

/**
 * Top-level response from the EnergyZero electricity prices endpoint.
 *
 * @property prices List of hourly price entries.
 */
@Serializable
internal data class EnergyZeroResponse(
    @SerialName("Prices") val prices: List<EnergyZeroPriceEntry>
)

/**
 * Client for the EnergyZero electricity price API (NL-only).
 *
 * Fetches hourly electricity prices for today and tomorrow. The raw JSON
 * can be cached by [PriceCache] to avoid redundant network requests.
 */
class EnergyZeroApi : PriceFetcher {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Fetches and parses electricity prices from the EnergyZero API.
     *
     * @param from Start of the requested period (inclusive).
     * @param to End of the requested period (exclusive).
     * @param timeZoneId Timezone to convert UTC timestamps to local time.
     * @return A [FetchResult] with sorted hourly price slots and source "EnergyZero".
     * @throws RuntimeException if the HTTP request fails.
     */
    override fun fetchPrices(from: Instant, to: Instant, timeZoneId: ZoneId): FetchResult {
        return FetchResult(parse(fetchRaw(from, to), timeZoneId), "EnergyZero")
    }

    /**
     * Fetches raw JSON from the EnergyZero API for the given date range.
     *
     * @param from Start of the requested period (inclusive).
     * @param to End of the requested period (exclusive).
     * @return Raw JSON response body.
     * @throws RuntimeException if the HTTP request fails or the body is empty.
     */
    fun fetchRaw(from: Instant, to: Instant): String {
        val url = "https://api.energyzero.nl/v1/energyprices" +
            "?fromDate=${DateTimeFormatter.ISO_INSTANT.format(from)}" +
            "&tillDate=${DateTimeFormatter.ISO_INSTANT.format(to)}" +
            "&interval=4&usageType=1"

        val request = Request.Builder().url(url).get().build()
        return sharedHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("API returned ${response.code}")
            }

            response.body.string()
        }
    }

    /**
     * Parses raw EnergyZero JSON into a sorted list of [PriceSlot] entries.
     *
     * @param raw Raw JSON string from [fetchRaw].
     * @param timeZoneId Timezone to convert UTC timestamps to local time.
     * @return Chronologically sorted list of hourly price slots (60-minute duration).
     */
    fun parse(raw: String, timeZoneId: ZoneId): List<PriceSlot> {
        val parsed = json.decodeFromString<EnergyZeroResponse>(raw)
        return parsed.prices.map { entry ->
            val instant = Instant.parse(entry.readingDate)
            val time = instant.atZone(timeZoneId)
            PriceSlot(time = time, price = entry.price, durationMinutes = 60)
        }.sortedBy { it.time }
    }
}
