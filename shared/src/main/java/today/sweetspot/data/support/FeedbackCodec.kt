package today.sweetspot.data.support

import kotlinx.serialization.encodeToString
import today.sweetspot.util.sweetSpotJson
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import today.sweetspot.model.FeedbackReport

/** Outcome of parsing the feedback Worker's `/report` success body. */
sealed interface SubmitResult {
    /**
     * The issue was created; carries its number and web URL, and the per-report [replyToken] the app
     * stores to post in-app replies (null if the Worker didn't return one, e.g. an older deployment).
     */
    data class Success(val number: Int, val url: String, val replyToken: String? = null) : SubmitResult

    /** The response wasn't the expected `{number,url}` shape. */
    data object Malformed : SubmitResult
}

/** What to do with a submission attempt, decided from the HTTP status code. */
enum class SubmitOutcome {
    /** 2xx — delivered. */
    SENT,

    /** Transient (429 / 5xx / network) — keep the report and retry later. */
    RETRYABLE,

    /** A 4xx that retrying can't fix (validation) — surface an error, don't queue. */
    PERMANENT
}

/**
 * Pure encode/parse + retry policy for the feedback Worker. Android-free and unit-tested; mirrors
 * [today.sweetspot.data.share.SetupShare]'s lenient-[sweetSpotJson] + sealed-result style and
 * `StatsReporter.reportOutcomeFor`'s status-code policy.
 */
object FeedbackCodec {

    private val json = sweetSpotJson

    /** Serialises the request body sent to `POST /report`. */
    fun encodeRequest(report: FeedbackReport): String = json.encodeToString(report)

    /** Serialises the `{issue, token, body}` request body sent to `POST /reply`. */
    fun encodeReply(issue: Int, token: String, body: String): String =
        buildJsonObject {
            put("issue", issue)
            put("token", token)
            put("body", body)
        }.toString()

    /**
     * Parses the `{number, url, replyToken?}` success body; [SubmitResult.Malformed] on anything
     * unexpected (missing number/url).
     */
    fun parseSubmitResponse(body: String): SubmitResult =
        try {
            val obj = json.parseToJsonElement(body).jsonObject
            val number = obj["number"]?.jsonPrimitive?.intOrNull
            val url = obj["url"]?.jsonPrimitive?.contentOrNull
            val replyToken = obj["replyToken"]?.jsonPrimitive?.contentOrNull
            if (number != null && url != null) SubmitResult.Success(number, url, replyToken) else SubmitResult.Malformed
        } catch (_: Exception) {
            SubmitResult.Malformed
        }

    /**
     * Retry policy from the HTTP status code: 2xx → [SubmitOutcome.SENT]; 429 or 5xx →
     * [SubmitOutcome.RETRYABLE]; any other 4xx → [SubmitOutcome.PERMANENT]. A network exception (no
     * status code) is treated as retryable by the caller.
     */
    fun submitOutcomeFor(code: Int): SubmitOutcome = when {
        code in 200..299 -> SubmitOutcome.SENT
        code == 429 || code in 500..599 -> SubmitOutcome.RETRYABLE
        else -> SubmitOutcome.PERMANENT
    }
}
