package si.merhar.sweetspot.data

import si.merhar.sweetspot.model.HourlyPrice
import si.merhar.sweetspot.util.AMSTERDAM
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

@Serializable
data class EnergyZeroPriceEntry(
    val readingDate: String,
    val price: Double
)

@Serializable
data class EnergyZeroResponse(
    val Prices: List<EnergyZeroPriceEntry>
)

object EnergyZeroApi {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    fun fetchRawJson(): String {
        val today = LocalDate.now(AMSTERDAM)
        val fromDate = today.atStartOfDay(AMSTERDAM).toInstant()
        val tillDate = today.plusDays(2).atStartOfDay(AMSTERDAM).toInstant()

        val url = "https://api.energyzero.nl/v1/energyprices" +
            "?fromDate=${DateTimeFormatter.ISO_INSTANT.format(fromDate)}" +
            "&tillDate=${DateTimeFormatter.ISO_INSTANT.format(tillDate)}" +
            "&interval=4&usageType=1"

        val request = Request.Builder().url(url).get().build()
        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            throw RuntimeException("API returned ${response.code}")
        }

        return response.body?.string()
            ?: throw RuntimeException("Empty response body")
    }

    fun parseJson(rawJson: String): List<HourlyPrice> {
        val parsed = json.decodeFromString<EnergyZeroResponse>(rawJson)
        return parsed.Prices.map { entry ->
            val instant = Instant.parse(entry.readingDate)
            val time = instant.atZone(AMSTERDAM)
            HourlyPrice(time = time, price = entry.price)
        }.sortedBy { it.time }
    }
}
