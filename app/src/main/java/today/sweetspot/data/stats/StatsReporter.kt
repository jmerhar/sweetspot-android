package today.sweetspot.data.stats

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.net.HttpURLConnection
import java.net.URL

/**
 * Reports accumulated API stats to the stats endpoint.
 *
 * Reads stats from the [collector], groups them by zone+source+device, encodes to JSON,
 * and POSTs to [REPORT_URL]. On success (HTTP 200), clears the local stats.
 * On failure, silently ignores — stats will be retried on the next daily check.
 *
 * Rate-limited to at most one report per [MIN_INTERVAL_MS] (24 hours).
 *
 * @param collector The stats collector to read from and clear on success.
 * @param prefs SharedPreferences for tracking the last report timestamp.
 * @param appVersion App version string for the report payload.
 * @param languageProvider Returns the current app language tag (e.g. "en", "nl", "" for system default).
 * @param statusProvider Returns the current payment status ("trial", "subscribed", or "expired").
 * @param poster Sends the JSON payload and returns the HTTP status code. Defaults to the real
 *   [HttpStatsPoster]; tests inject a fake to drive the response-handling branches without a network.
 */
class StatsReporter(
    private val collector: StatsCollector,
    private val prefs: SharedPreferences,
    private val appVersion: String,
    private val languageProvider: () -> String = { "" },
    private val statusProvider: () -> String = { "trial" },
    private val poster: StatsPoster = HttpStatsPoster(appVersion)
) {

    companion object {
        /** Stats reporting endpoint. */
        const val REPORT_URL = "https://stats.sweetspot.today/report"

        /** Minimum interval between reports: 24 hours. */
        const val MIN_INTERVAL_MS = 24 * 60 * 60 * 1000L

        /** JSON payload format version. Bump when the payload structure changes. */
        const val PAYLOAD_VERSION = 2

        private const val KEY_LAST_REPORT_MS = "stats_last_report_ms"
    }

    /**
     * Sends accumulated stats if the minimum interval has elapsed.
     *
     * This method is safe to call frequently — it no-ops if not enough time has passed
     * since the last report, or if there are no records to send.
     *
     * On success (HTTP 200), clears data and records the timestamp.
     * On client error (4xx, except 429), clears data — the payload is invalid and retrying
     * would never succeed. On server error (5xx), rate limit (429), or network failure,
     * keeps data for retry on the next daily check.
     */
    fun reportIfDue() {
        if (!isReportDue()) return
        val records = collector.readAll()
        if (records.isEmpty()) return

        val json = buildReportJson(records, appVersion, languageProvider(), statusProvider())
        val code = try {
            poster.post(json)
        } catch (_: Exception) {
            return // Network error — keep data, retry next day
        }
        when (reportOutcomeFor(code)) {
            ReportOutcome.CLEAR_AND_STAMP -> {
                collector.clear()
                prefs.edit { putLong(KEY_LAST_REPORT_MS, System.currentTimeMillis()) }
            }
            ReportOutcome.CLEAR -> collector.clear()
            ReportOutcome.KEEP -> {} // 429 or 5xx: keep data, retry next day
        }
    }

    /**
     * Checks whether enough time has passed since the last report.
     *
     * @return `true` if at least [MIN_INTERVAL_MS] has elapsed since the last successful report.
     */
    internal fun isReportDue(): Boolean {
        val lastReport = prefs.getLong(KEY_LAST_REPORT_MS, 0L)
        return System.currentTimeMillis() - lastReport >= MIN_INTERVAL_MS
    }

    /**
     * Resets the report timer, allowing immediate stats reporting.
     *
     * Used by developer options for testing.
     */
    fun resetReportTimer() {
        prefs.edit { remove(KEY_LAST_REPORT_MS) }
    }
}

/**
 * Builds the JSON report payload from a list of stats records.
 *
 * Groups records by zone+source+device to minimise repetition. Each group contains
 * individual records with timestamps for time-of-day analysis.
 *
 * @param records The stats records to encode.
 * @param appVersion App version string.
 * @param language Current app language tag (e.g. "en", "nl", "" for system default).
 * @param status Current payment status ("trial", "subscribed", or "expired").
 * @return JSON string ready for POST.
 */
internal fun buildReportJson(
    records: List<StatsRecord>,
    appVersion: String,
    language: String = "",
    status: String = "trial"
): String {
    val grouped = records.groupBy { Triple(it.zone, it.source, it.device) }
    val recordsArray = JsonArray(
        grouped.map { (key, entries) ->
            val (zone, source, device) = key
            buildJsonObject {
                put("z", zone)
                put("s", source)
                put("d", device)
                put("r", JsonArray(entries.map { entry ->
                    val base = mutableMapOf<String, JsonElement>(
                        "t" to JsonPrimitive(entry.epochSecond),
                        "ok" to JsonPrimitive(entry.success),
                        "ms" to JsonPrimitive(entry.durationMs)
                    )
                    if (!entry.success) {
                        base["e"] = JsonPrimitive(entry.errorCategory)
                    }
                    JsonObject(base)
                }))
            }
        }
    )

    return JsonObject(mapOf(
        "v" to JsonPrimitive(StatsReporter.PAYLOAD_VERSION),
        "app" to JsonPrimitive(appVersion),
        "lang" to JsonPrimitive(language),
        "status" to JsonPrimitive(status),
        "records" to recordsArray
    )).toString()
}

/**
 * Sends a JSON stats payload to the reporting endpoint and returns the HTTP status code.
 *
 * Isolated behind an interface so [StatsReporter]'s response-handling logic can be exercised in
 * tests with a fake, without a real network call. The production implementation is [HttpStatsPoster].
 */
fun interface StatsPoster {
    /**
     * POSTs [json] to the stats endpoint.
     *
     * @param json The report payload.
     * @return The HTTP response code.
     * @throws Exception on any network/IO failure (the caller treats this as "keep data, retry").
     */
    fun post(json: String): Int
}

/**
 * Production [StatsPoster] using [HttpURLConnection]. Thin IO glue with no decision logic (the
 * response-code handling lives in [reportOutcomeFor]), so it is excluded from coverage — it can
 * only be exercised against a real network.
 *
 * @param appVersion App version string, sent in the User-Agent header.
 */
class HttpStatsPoster(private val appVersion: String) : StatsPoster {
    override fun post(json: String): Int {
        val connection = (URL(StatsReporter.REPORT_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("User-Agent", "SweetSpot/$appVersion")
            connectTimeout = 10_000
            readTimeout = 10_000
            doOutput = true
        }
        return try {
            connection.outputStream.use { it.write(json.toByteArray(Charsets.UTF_8)) }
            connection.responseCode
        } finally {
            connection.disconnect()
        }
    }
}

/** The action to take on the local stats buffer after a report attempt. */
internal enum class ReportOutcome {
    /** HTTP 200: report accepted — clear the buffer and record the timestamp. */
    CLEAR_AND_STAMP,

    /** 4xx (except 429): payload is permanently invalid — clear it so it isn't retried forever. */
    CLEAR,

    /** 429 (rate limit) or 5xx: transient — keep the buffer for the next daily retry. */
    KEEP
}

/**
 * Maps an HTTP response code to the action to take on the local stats buffer.
 *
 * Pure and Android-free so the reporting policy is unit-testable without a network or prefs.
 *
 * @param code HTTP status code returned by [StatsPoster.post].
 * @return The [ReportOutcome] to apply.
 */
internal fun reportOutcomeFor(code: Int): ReportOutcome = when {
    code == HttpURLConnection.HTTP_OK -> ReportOutcome.CLEAR_AND_STAMP
    code in 400..499 && code != 429 -> ReportOutcome.CLEAR
    else -> ReportOutcome.KEEP
}
