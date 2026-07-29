package today.sweetspot.model

import kotlinx.serialization.Serializable

/** Whether a submission is a bug report or general feedback. */
enum class ReportCategory {
    BUG,
    FEEDBACK;

    /** The value the feedback Worker expects in the request's `category` field. */
    val wireValue: String get() = if (this == BUG) "bug" else "feedback"
}

/**
 * A report/feedback submission as sent to the feedback Worker (`POST /report`).
 *
 * [diagnostics] is attached to bug reports only. [email] is optional and opts the reporter into
 * activity notifications — it is stored server-side only and never appears in the public GitHub issue.
 */
@Serializable
data class FeedbackReport(
    val category: String,
    val subject: String,
    val body: String,
    val diagnostics: String? = null,
    val email: String? = null
)

/**
 * A report this device has submitted, remembered locally so "My reports" can show its live status
 * (read from the public GitHub API — the app never stores a token).
 */
@Serializable
data class MyReport(
    val number: Int,
    val subject: String,
    val category: String,
    val submittedAtMs: Long,
    /** Capability token for posting in-app replies to this report (null for older stored reports). */
    val replyToken: String? = null
)

/**
 * A submitted-but-not-yet-delivered report held in the local outbox for retry. Carries the full
 * request (including the diagnostics captured at submit time, so a later retry sends the identical
 * payload) plus bookkeeping. Flushed on app/Help open until it lands.
 */
@Serializable
data class PendingReport(
    val report: FeedbackReport,
    val createdAtMs: Long,
    val attempts: Int = 0
)

/**
 * A reply (comment on an existing report) submitted but not yet delivered, held in the reply outbox
 * for retry — the reply counterpart of [PendingReport]. Carries the target [issue], the report's
 * capability [token], and the [body], plus bookkeeping. Flushed on app/Help open until it lands.
 */
@Serializable
data class PendingReply(
    val issue: Int,
    val token: String,
    val body: String,
    val createdAtMs: Long,
    val attempts: Int = 0
)
