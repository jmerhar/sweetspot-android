package today.sweetspot.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

/** Public status of a GitHub issue, for the in-app "My reports" tracker. */
data class IssueStatus(
    val number: Int,
    val state: String,
    val title: String,
    val comments: Int,
    val htmlUrl: String
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
    fun fetchRaw(number: Int): String {
        val request = Request.Builder()
            .url("https://api.github.com/repos/$repo/issues/$number")
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
}
