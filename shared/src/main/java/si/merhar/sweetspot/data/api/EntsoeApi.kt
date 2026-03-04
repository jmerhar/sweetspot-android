package si.merhar.sweetspot.data.api

import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import si.merhar.sweetspot.model.PriceSlot
import java.io.StringReader
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.concurrent.TimeUnit

/**
 * Client for the ENTSO-E Transparency Platform day-ahead price API.
 *
 * Fetches day-ahead electricity prices (documentType A44) for a specific bidding zone.
 * The response is XML containing one or more `TimeSeries > Period > Point` blocks.
 * Prices are in EUR/MWh and converted to EUR/kWh (÷ 1000).
 *
 * Returns prices at the API's native resolution (PT15M or PT60M) — no aggregation
 * is performed, so the rest of the pipeline sees the original slot duration.
 *
 * @param token ENTSO-E API security token.
 * @param biddingZone EIC code for the bidding zone (see [BiddingZone]).
 */
class EntsoeApi(
    private val token: String,
    private val biddingZone: String
) : PriceFetcher {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    /**
     * Parses ENTSO-E timestamps that may omit seconds (e.g. `2026-03-02T23:00Z`).
     * Standard [Instant.parse] requires seconds, so we use a lenient formatter.
     */
    private val timestampParser: DateTimeFormatter = DateTimeFormatterBuilder()
        .append(DateTimeFormatter.ISO_LOCAL_DATE)
        .appendLiteral('T')
        .appendPattern("HH:mm[:ss]")
        .appendOffsetId()
        .toFormatter()

    /**
     * Fetches and parses day-ahead electricity prices from the ENTSO-E API.
     *
     * @param from Start of the requested period (inclusive).
     * @param to End of the requested period (exclusive).
     * @param timeZoneId Timezone to convert UTC timestamps to local time.
     * @return A [FetchResult] with sorted price slots in EUR/kWh at native resolution and source "ENTSO-E".
     * @throws RuntimeException if the HTTP request fails or the response is an error document.
     */
    override fun fetchPrices(from: Instant, to: Instant, timeZoneId: ZoneId): FetchResult {
        return FetchResult(parse(fetchRaw(from, to), timeZoneId), "ENTSO-E")
    }

    /**
     * Fetches raw XML from the ENTSO-E API for the given date range.
     *
     * @param from Start of the requested period (inclusive).
     * @param to End of the requested period (exclusive).
     * @return Raw XML response body.
     * @throws RuntimeException if the HTTP request fails, the body is empty,
     *         or the response is an Acknowledgement error document.
     */
    fun fetchRaw(from: Instant, to: Instant): String {
        val fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmm").withZone(ZoneOffset.UTC)
        val url = "https://web-api.tp.entsoe.eu/api" +
            "?securityToken=$token" +
            "&documentType=A44" +
            "&in_Domain=$biddingZone" +
            "&out_Domain=$biddingZone" +
            "&periodStart=${fmt.format(from)}" +
            "&periodEnd=${fmt.format(to)}"

        val request = Request.Builder().url(url).get().build()
        val body = client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("ENTSO-E API returned ${response.code}")
            }
            response.body?.string()
                ?: throw RuntimeException("Empty response body")
        }

        if (body.contains("Acknowledgement_MarketDocument")) {
            val reason = extractErrorReason(body)
            throw RuntimeException("ENTSO-E API error: $reason")
        }

        return body
    }

    /**
     * Parses ENTSO-E XML into a sorted list of [PriceSlot] entries at native resolution.
     *
     * Handles both PT60M (hourly) and PT15M (quarter-hourly) resolutions.
     * Supports A03 curve type where positions may be skipped (price carries forward
     * from the last point).
     *
     * @param raw Raw XML string from [fetchRaw].
     * @param timeZoneId Timezone to convert UTC timestamps to local time.
     * @return Chronologically sorted list of price slots at native resolution.
     * @throws RuntimeException if the XML is an error document.
     */
    fun parse(raw: String, timeZoneId: ZoneId): List<PriceSlot> {
        if (raw.contains("Acknowledgement_MarketDocument")) {
            val reason = extractErrorReason(raw)
            throw RuntimeException("ENTSO-E API error: $reason")
        }

        val rawPrices = mutableListOf<Triple<Instant, Double, Int>>()
        val parser = XmlPullParserFactory.newInstance().newPullParser()
        parser.setInput(StringReader(raw))

        var periodStart: Instant? = null
        var resolution: Duration? = null
        var currentPosition: Int? = null
        var currentPrice: Double? = null
        var inTimeSeries = false
        var inPeriod = false
        var inPoint = false
        var inTimeInterval = false
        var currentTag = ""

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    currentTag = parser.name
                    when (currentTag) {
                        "TimeSeries" -> inTimeSeries = true
                        "Period" -> if (inTimeSeries) inPeriod = true
                        "timeInterval" -> if (inPeriod) inTimeInterval = true
                        "Point" -> if (inPeriod) {
                            inPoint = true
                            currentPosition = null
                            currentPrice = null
                        }
                    }
                }

                XmlPullParser.TEXT -> {
                    val text = parser.text.trim()
                    if (text.isEmpty()) {
                        eventType = parser.next()
                        continue
                    }
                    when {
                        inTimeInterval && currentTag == "start" ->
                            periodStart = OffsetDateTime.parse(text, timestampParser).toInstant()

                        inPeriod && !inTimeInterval && currentTag == "resolution" ->
                            resolution = Duration.parse(text)

                        inPoint && currentTag == "position" ->
                            currentPosition = text.toInt()

                        inPoint && currentTag == "price.amount" ->
                            currentPrice = text.toDouble()
                    }
                }

                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "TimeSeries" -> inTimeSeries = false
                        "timeInterval" -> inTimeInterval = false
                        "Point" -> {
                            if (inPoint && currentPosition != null && currentPrice != null) {
                                val start = periodStart!!
                                val res = resolution!!
                                val resMinutes = res.toMinutes().toInt()
                                val timestamp = start.plus(
                                    res.multipliedBy((currentPosition - 1).toLong())
                                )
                                rawPrices.add(Triple(timestamp, currentPrice, resMinutes))
                            }
                            inPoint = false
                        }

                        "Period" -> {
                            if (inPeriod && periodStart != null && resolution != null) {
                                fillA03Gaps(rawPrices, periodStart, resolution)
                            }
                            inPeriod = false
                            periodStart = null
                            resolution = null
                        }
                    }
                    currentTag = ""
                }
            }
            eventType = parser.next()
        }

        return buildPriceSlots(rawPrices, timeZoneId)
    }

    /**
     * Fills gaps left by A03 curve type by carrying forward the last known price.
     *
     * For A03 responses, not every position is present — missing positions inherit
     * the price from the preceding point. This method detects gaps in the already-parsed
     * points for the current period and fills them.
     *
     * @param prices Mutable list of (timestamp, price, resolutionMinutes) triples to fill in-place.
     * @param periodStart Start instant of the current period.
     * @param resolution Duration of each position slot.
     */
    private fun fillA03Gaps(
        prices: MutableList<Triple<Instant, Double, Int>>,
        periodStart: Instant,
        resolution: Duration
    ) {
        if (prices.isEmpty()) return

        val resMinutes = resolution.toMinutes().toInt()

        // Find the points that belong to this period
        val periodPrices = prices.filter { (ts, _, _) -> !ts.isBefore(periodStart) }
        if (periodPrices.size < 2) return

        // Build a map of position → price for existing points
        val positionMap = mutableMapOf<Int, Double>()
        for ((ts, price, _) in periodPrices) {
            val pos = ((Duration.between(periodStart, ts).toMinutes()) / resolution.toMinutes()).toInt() + 1
            positionMap[pos] = price
        }

        val maxPos = positionMap.keys.max()
        var lastPrice = positionMap[1] ?: return

        for (pos in 1..maxPos) {
            if (positionMap.containsKey(pos)) {
                lastPrice = positionMap[pos]!!
            } else {
                val ts = periodStart.plus(resolution.multipliedBy((pos - 1).toLong()))
                prices.add(Triple(ts, lastPrice, resMinutes))
            }
        }
    }

    /**
     * Converts raw price triples to [PriceSlot] entries, applying EUR/MWh → EUR/kWh conversion.
     *
     * @param prices List of (timestamp, price_in_EUR_MWh, resolutionMinutes) triples.
     * @param timeZoneId Timezone for the resulting [PriceSlot] entries.
     * @return Sorted list of price slots in EUR/kWh at native resolution.
     */
    private fun buildPriceSlots(
        prices: List<Triple<Instant, Double, Int>>,
        timeZoneId: ZoneId
    ): List<PriceSlot> {
        return prices
            .map { (ts, priceMwh, resMinutes) ->
                PriceSlot(
                    time = ts.atZone(timeZoneId),
                    price = priceMwh / 1000.0,
                    durationMinutes = resMinutes
                )
            }
            .sortedBy { it.time }
    }

    /**
     * Extracts the error reason text from an ENTSO-E Acknowledgement_MarketDocument.
     *
     * @param xml Raw XML error response.
     * @return The reason text, or "Unknown error" if not found.
     */
    private fun extractErrorReason(xml: String): String {
        val parser = XmlPullParserFactory.newInstance().newPullParser()
        parser.setInput(StringReader(xml))
        var inReason = false
        var currentTag = ""

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    currentTag = parser.name
                    if (currentTag == "Reason") inReason = true
                }

                XmlPullParser.TEXT -> {
                    if (inReason && currentTag == "text") {
                        val text = parser.text.trim()
                        if (text.isNotEmpty()) return text
                    }
                }

                XmlPullParser.END_TAG -> {
                    if (parser.name == "Reason") inReason = false
                    currentTag = ""
                }
            }
            eventType = parser.next()
        }
        return "Unknown error"
    }
}
