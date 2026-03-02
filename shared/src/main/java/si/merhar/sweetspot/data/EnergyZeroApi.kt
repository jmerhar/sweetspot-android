package si.merhar.sweetspot.data

import si.merhar.sweetspot.model.HourlyPrice
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.Instant
import java.time.LocalDate
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
 * @property Prices List of hourly price entries.
 */
@Serializable
internal data class EnergyZeroResponse(
    val Prices: List<EnergyZeroPriceEntry>
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
     * Fetches raw JSON from the EnergyZero API for today and tomorrow.
     *
     * @param zoneId Timezone used to determine "today" and date boundaries.
     * @return Raw JSON response body.
     * @throws RuntimeException if the HTTP request fails or the body is empty.
     */
    override fun fetchRawJson(zoneId: ZoneId): String {
        val today = LocalDate.now(zoneId)
        val fromDate = today.atStartOfDay(zoneId).toInstant()
        val tillDate = today.plusDays(2).atStartOfDay(zoneId).toInstant()

        val url = "https://api.energyzero.nl/v1/energyprices" +
            "?fromDate=${DateTimeFormatter.ISO_INSTANT.format(fromDate)}" +
            "&tillDate=${DateTimeFormatter.ISO_INSTANT.format(tillDate)}" +
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
     * @param rawJson Raw JSON string from [fetchRawJson] or cache.
     * @param zoneId Timezone to convert UTC timestamps to local time.
     * @return Chronologically sorted list of hourly prices.
     */
    override fun parseJson(rawJson: String, zoneId: ZoneId): List<HourlyPrice> {
        val parsed = json.decodeFromString<EnergyZeroResponse>(rawJson)
        return parsed.Prices.map { entry ->
            val instant = Instant.parse(entry.readingDate)
            val time = instant.atZone(zoneId)
            HourlyPrice(time = time, price = entry.price)
        }.sortedBy { it.time }
    }
}
