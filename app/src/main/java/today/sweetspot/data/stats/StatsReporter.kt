package today.sweetspot.data.stats

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.serialization.json.JsonArray
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
 */
class StatsReporter(
    private val collector: StatsCollector,
    private val prefs: SharedPreferences,
    private val appVersion: String
) {

    companion object {
        /** Stats reporting endpoint. */
        const val REPORT_URL = "https://stats.sweetspot.today/report"

        /** Minimum interval between reports: 24 hours. */
        const val MIN_INTERVAL_MS = 24 * 60 * 60 * 1000L

        private const val KEY_LAST_REPORT_MS = "stats_last_report_ms"
    }

    /**
     * Sends accumulated stats if the minimum interval has elapsed.
     *
     * This method is safe to call frequently — it no-ops if not enough time has passed
     * since the last report, or if there are no records to send.
     */
    fun reportIfDue() {
        if (!isReportDue()) return
        val records = collector.readAll()
        if (records.isEmpty()) return

        try {
            val json = buildReportJson(records, appVersion)
            val connection = (URL(REPORT_URL).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("User-Agent", "SweetSpot/$appVersion")
                connectTimeout = 10_000
                readTimeout = 10_000
                doOutput = true
            }
            try {
                connection.outputStream.use { it.write(json.toByteArray(Charsets.UTF_8)) }
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    collector.clear()
                    prefs.edit { putLong(KEY_LAST_REPORT_MS, System.currentTimeMillis()) }
                }
            } finally {
                connection.disconnect()
            }
        } catch (_: Exception) {
            // Silently ignore — retry next day
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
}

/**
 * Builds the JSON report payload from a list of stats records.
 *
 * Groups records by zone+source+device to minimise repetition. Each group contains
 * individual records with timestamps for time-of-day analysis.
 *
 * @param records The stats records to encode.
 * @param appVersion App version string.
 * @return JSON string ready for POST.
 */
internal fun buildReportJson(records: List<StatsRecord>, appVersion: String): String {
    val grouped = records.groupBy { Triple(it.zone, it.source, it.device) }
    val recordsArray = JsonArray(
        grouped.map { (key, entries) ->
            val (zone, source, device) = key
            buildJsonObject {
                put("z", zone)
                put("s", source)
                put("d", device)
                put("r", JsonArray(entries.map { entry ->
                    if (entry.success) {
                        JsonObject(mapOf(
                            "t" to JsonPrimitive(entry.epochSecond),
                            "ok" to JsonPrimitive(true)
                        ))
                    } else {
                        JsonObject(mapOf(
                            "t" to JsonPrimitive(entry.epochSecond),
                            "ok" to JsonPrimitive(false),
                            "e" to JsonPrimitive(entry.errorCategory)
                        ))
                    }
                }))
            }
        }
    )

    return JsonObject(mapOf(
        "v" to JsonPrimitive(1),
        "app" to JsonPrimitive(appVersion),
        "records" to recordsArray
    )).toString()
}
