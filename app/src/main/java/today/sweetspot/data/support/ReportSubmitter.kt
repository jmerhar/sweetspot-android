package today.sweetspot.data.support

import java.net.HttpURLConnection
import java.net.URL

/** HTTP result of a report submission: the status code and the response body. */
data class SubmitHttpResult(val code: Int, val body: String)

/**
 * Sends a report/feedback JSON payload to the feedback Worker and returns the status code + body.
 * Isolated behind an interface so the ViewModel's submit/retry logic is testable with a fake — the
 * production impl is [HttpReportSubmitter].
 */
fun interface ReportSubmitter {
    /**
     * POSTs [json] to the feedback endpoint.
     *
     * @return the HTTP status code and response body.
     * @throws Exception on any network/IO failure (the caller treats this as retryable).
     */
    fun submit(json: String): SubmitHttpResult
}

/**
 * Production [ReportSubmitter] using [HttpURLConnection]. Thin IO glue with no decision logic (the
 * outcome policy lives in `FeedbackCodec.submitOutcomeFor` and the parsing in
 * `FeedbackCodec.parseSubmitResponse`), so it is excluded from coverage — it can only be exercised
 * against a real network.
 *
 * @param appVersion App version string, sent in the User-Agent header.
 */
class HttpReportSubmitter(private val appVersion: String) : ReportSubmitter {
    override fun submit(json: String): SubmitHttpResult {
        val connection = (URL(REPORT_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("User-Agent", "SweetSpot/$appVersion")
            connectTimeout = 10_000
            readTimeout = 10_000
            doOutput = true
        }
        return try {
            connection.outputStream.use { it.write(json.toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            SubmitHttpResult(code, body)
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        const val REPORT_URL = "https://feedback.sweetspot.today/report"
    }
}
