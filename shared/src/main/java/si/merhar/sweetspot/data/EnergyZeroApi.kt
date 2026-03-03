package si.merhar.sweetspot.data

import si.merhar.sweetspot.model.HourlyPrice
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

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
 * Singleton client for the EnergyZero electricity price API.
 *
 * Fetches hourly electricity prices for today and tomorrow. The raw JSON
 * can be cached by [PriceCache] to avoid redundant network requests.
 */
object EnergyZeroApi : PriceFetcher {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Fetches and parses electricity prices from the EnergyZero API.
     *
     * @param from Start of the requested period (inclusive).
     * @param to End of the requested period (exclusive).
     * @param timeZoneId Timezone to convert UTC timestamps to local time.
     * @return Chronologically sorted list of [HourlyPrice] entries.
     * @throws RuntimeException if the HTTP request fails.
     */
    override fun fetchPrices(from: Instant, to: Instant, timeZoneId: ZoneId): List<HourlyPrice> {
        return parse(fetchRaw(from, to), timeZoneId)
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
        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("API returned ${response.code}")
            }

            response.body?.string()
                ?: throw RuntimeException("Empty response body")
        }
    }

    /**
     * Parses raw EnergyZero JSON into a sorted list of [HourlyPrice] entries.
     *
     * @param raw Raw JSON string from [fetchRaw].
     * @param timeZoneId Timezone to convert UTC timestamps to local time.
     * @return Chronologically sorted list of hourly prices.
     */
    fun parse(raw: String, timeZoneId: ZoneId): List<HourlyPrice> {
        val parsed = json.decodeFromString<EnergyZeroResponse>(raw)
        return parsed.prices.map { entry ->
            val instant = Instant.parse(entry.readingDate)
            val time = instant.atZone(timeZoneId)
            HourlyPrice(time = time, price = entry.price)
        }.sortedBy { it.time }
    }
}
