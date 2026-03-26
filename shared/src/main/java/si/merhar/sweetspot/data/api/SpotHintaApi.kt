package si.merhar.sweetspot.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Request
import si.merhar.sweetspot.model.PriceSlot
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

/**
 * A single price entry from the Spot-Hinta.fi API response.
 *
 * @property dateTime ISO-8601 timestamp string with offset (e.g. `2025-01-15T12:00:00+02:00` or `Z`).
 * @property priceNoTax Electricity price in EUR/kWh (no tax).
 */
@Serializable
internal data class SpotHintaPriceEntry(
    @SerialName("DateTime") val dateTime: String,
    @SerialName("PriceNoTax") val priceNoTax: Double
)

/**
 * Client for the Spot-Hinta.fi day-ahead price API.
 *
 * Covers 15 Nordic and Baltic bidding zones (FI, SE1–SE4, DK1–DK2, NO1–NO5, EE, LV, LT)
 * with 15-minute resolution. Prices are already in EUR/kWh — no unit conversion needed.
 * No authentication required.
 *
 * The API always returns today's and tomorrow's prices (when available) — there are
 * no `from`/`to` date parameters.
 *
 * @param region Spot-Hinta.fi region code (matches SweetSpot zone IDs exactly).
 */
class SpotHintaApi(private val region: String) : PriceFetcher {

    companion object {
        /** Zone IDs covered by the Spot-Hinta.fi API (region codes match zone IDs directly). */
        val ZONES: Set<String> = setOf(
            "FI", "SE1", "SE2", "SE3", "SE4",
            "DK1", "DK2",
            "NO1", "NO2", "NO3", "NO4", "NO5",
            "EE", "LV", "LT"
        )
    }

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Fetches and parses electricity prices from the Spot-Hinta.fi API.
     *
     * @param from Start of the requested period (unused — API always returns today+tomorrow).
     * @param to End of the requested period (unused — API always returns today+tomorrow).
     * @param timeZoneId Timezone to convert timestamps to local time.
     * @return A [FetchResult] with sorted 15-minute price slots and source "Spot-Hinta.fi".
     * @throws RuntimeException if the HTTP request fails.
     */
    override fun fetchPrices(from: Instant, to: Instant, timeZoneId: ZoneId): FetchResult {
        return FetchResult(parse(fetchRaw(), timeZoneId), "Spot-Hinta.fi")
    }

    /**
     * Fetches raw JSON from the Spot-Hinta.fi API.
     *
     * @return Raw JSON response body (a top-level array of price entries).
     * @throws RuntimeException if the HTTP request fails or the body is empty.
     */
    fun fetchRaw(): String {
        val url = "https://api.spot-hinta.fi/TodayAndDayForward?region=$region"

        val request = Request.Builder().url(url).get().build()
        return sharedHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("Spot-Hinta.fi API returned ${response.code}")
            }

            response.body.string()
        }
    }

    /**
     * Parses raw Spot-Hinta.fi JSON into a sorted list of [PriceSlot] entries.
     *
     * Timestamps include timezone offsets (e.g. `+02:00`, `+03:00`, or `Z`) and are
     * converted to the configured timezone via [OffsetDateTime.parse].
     *
     * @param raw Raw JSON string from [fetchRaw] (top-level array).
     * @param timeZoneId Timezone to convert timestamps to local time.
     * @return Chronologically sorted list of 15-minute price slots.
     */
    fun parse(raw: String, timeZoneId: ZoneId): List<PriceSlot> {
        val parsed = json.decodeFromString<List<SpotHintaPriceEntry>>(raw)
        return parsed.map { entry ->
            val odt = OffsetDateTime.parse(entry.dateTime)
            val time = odt.atZoneSameInstant(timeZoneId)
            PriceSlot(time = time, price = entry.priceNoTax, durationMinutes = 15)
        }.sortedBy { it.time }
    }
}
