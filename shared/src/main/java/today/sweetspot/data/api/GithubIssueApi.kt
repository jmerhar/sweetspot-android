package today.sweetspot.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import today.sweetspot.util.HelpLinks
import java.time.Instant

/** Public status of a GitHub issue, for the in-app "My reports" tracker. */
data class IssueStatus(
    val number: Int,
    val state: String,
    val title: String,
    val comments: Int,
    val htmlUrl: String
)

/**
 * One entry in an issue's public conversation — either the issue body itself (the first item) or a
 * comment. [author] is the GitHub login. [mine] is true when the author is the feedback bot, i.e. this
 * entry is the reporter's own (the app posts on their behalf), so the UI labels it "You" rather than
 * support. [createdAtMs] is 0 when the timestamp is unparseable.
 */
data class ThreadItem(
    val author: String,
    val body: String,
    val createdAtMs: Long,
    val mine: Boolean
)

/** An issue's full public conversation, for the in-app thread view. */
data class IssueThread(
    val number: Int,
    val title: String,
    val state: String,
    val htmlUrl: String,
    val items: List<ThreadItem>
)

/** The subset of GitHub's issue JSON that "My reports" needs. */
@Serializable
internal data class GithubIssueDto(
    val number: Int,
    val state: String,
    val title: String = "",
    val comments: Int = 0,
    @SerialName("html_url") val htmlUrl: String = ""
)

/** A GitHub account (only the login is used). */
@Serializable
internal data class GithubUserDto(val login: String = "")

/** The fuller issue JSON used for the thread head (adds body, author, and creation time). */
@Serializable
internal data class GithubIssueFullDto(
    val number: Int,
    val state: String = "",
    val title: String = "",
    val body: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("html_url") val htmlUrl: String = "",
    val user: GithubUserDto? = null
)

/** One issue comment. */
@Serializable
internal data class GithubCommentDto(
    val body: String = "",
    @SerialName("created_at") val createdAt: String = "",
    val user: GithubUserDto? = null
)

/**
 * Reads **public** GitHub issue status for the in-app "My reports" screen. A public repo needs no
 * auth (60 requests/hour per IP), so the app tracks reports it submitted via the feedback Worker
 * without ever holding a token. Follows the three-layer API pattern (`fetch` → `fetchRaw` + `parse`,
 * `HttpException` on non-2xx) used by the price APIs; `fetchRaw`/`parse` are public for tests.
 */
open class GithubIssueApi(
    private val repo: String = "jmerhar/sweetspot-android",
    private val client: OkHttpClient = sharedHttpClient
) {

    private val json = Json { ignoreUnknownKeys = true }

    /** Fetches and parses the current status of issue [number]. `open` so tests can fake it. */
    open fun fetch(number: Int): IssueStatus = parse(fetchRaw(number))

    /** Raw issue JSON from the public GitHub REST API. */
    fun fetchRaw(number: Int): String =
        getRaw("https://api.github.com/repos/$repo/issues/$number")

    /** GETs [url] from the public GitHub REST API, throwing [HttpException] on a non-2xx response. */
    private fun getRaw(url: String): String {
        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "SweetSpot-app")
            .get()
            .build()
        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw HttpException(response.code, "GitHub API returned ${response.code}")
            }
            response.body.string()
        }
    }

    /** Parses raw issue JSON into an [IssueStatus]. */
    fun parse(raw: String): IssueStatus {
        val dto = json.decodeFromString<GithubIssueDto>(raw)
        return IssueStatus(dto.number, dto.state, dto.title, dto.comments, dto.htmlUrl)
    }

    /** Fetches and assembles the full public conversation of issue [number]. `open` so tests fake it. */
    open fun fetchThread(number: Int): IssueThread = parseThread(fetchRaw(number), fetchCommentsRaw(number))

    /** Raw comments JSON (a top-level array) from the public GitHub REST API. */
    fun fetchCommentsRaw(number: Int): String =
        getRaw("https://api.github.com/repos/$repo/issues/$number/comments?per_page=100")

    /**
     * Builds an [IssueThread] from the issue JSON and its comments JSON. The issue body is the first
     * item, followed by the comments in the order GitHub returns them (chronological).
     */
    fun parseThread(issueRaw: String, commentsRaw: String): IssueThread {
        val issue = json.decodeFromString<GithubIssueFullDto>(issueRaw)
        val head = toItem(issue.user?.login ?: "", issue.body ?: "", issue.createdAt)
        val comments = json.decodeFromString<List<GithubCommentDto>>(commentsRaw)
            .map { toItem(it.user?.login ?: "", it.body, it.createdAt) }
        return IssueThread(issue.number, issue.title, issue.state, issue.htmlUrl, listOf(head) + comments)
    }

    /** Builds a [ThreadItem], marking it [ThreadItem.mine] when authored by the feedback bot. */
    private fun toItem(author: String, body: String, createdAt: String): ThreadItem =
        ThreadItem(author, stripReplyPrefix(stripAppFooter(body)), parseIsoToMs(createdAt), mine = author.equals(HelpLinks.BOT_LOGIN, ignoreCase = true))

    /**
     * Trims the Worker's app footer and the diagnostics block that follows it from an issue body, so
     * the in-app thread shows only the reporter's own text. A body without the marker (every comment)
     * is unchanged. The full body — footer and diagnostics included — remains visible on GitHub.
     */
    private fun stripAppFooter(body: String): String =
        body.substringBefore(HelpLinks.ISSUE_BODY_FOOTER).trimEnd()

    /**
     * Strips the Worker's reporter-reply prefix from a comment body. The Worker posts a reporter's
     * in-app reply as the bot with a leading marker so GitHub readers can tell it's the reporter; the
     * app already labels it "You", so the marker is redundant here (and its removal makes the displayed
     * body match the optimistic copy the app appends on send). A body without the marker is unchanged.
     */
    private fun stripReplyPrefix(body: String): String =
        if (body.startsWith(HelpLinks.REPLY_PREFIX)) body.removePrefix(HelpLinks.REPLY_PREFIX).trimStart('\n', ' ')
        else body

    /** Parses an ISO-8601 UTC timestamp (e.g. `2024-01-02T03:04:05Z`) to epoch millis; 0 on failure. */
    private fun parseIsoToMs(iso: String): Long =
        try {
            if (iso.isBlank()) 0L else Instant.parse(iso).toEpochMilli()
        } catch (_: Exception) {
            0L
        }
}
