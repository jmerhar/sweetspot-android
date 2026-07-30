package today.sweetspot.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/** Verifies parsing of the public GitHub issue response used by "My reports". */
class GithubIssueApiTest {

    private val api = GithubIssueApi()

    @Test
    fun `parse reads the fields My reports needs and ignores the rest`() {
        val raw = """
            {
              "number": 3,
              "state": "open",
              "title": "[test] webhook",
              "comments": 2,
              "html_url": "https://github.com/jmerhar/sweetspot-android/issues/3",
              "id": 999, "locked": false, "author_association": "OWNER"
            }
        """.trimIndent()
        val status = api.parse(raw)
        assertEquals(3, status.number)
        assertEquals("open", status.state)
        assertEquals("[test] webhook", status.title)
        assertEquals(2, status.comments)
        assertEquals("https://github.com/jmerhar/sweetspot-android/issues/3", status.htmlUrl)
    }

    @Test
    fun `parse tolerates missing optional fields`() {
        val status = api.parse("""{"number":5,"state":"closed"}""")
        assertEquals(5, status.number)
        assertEquals("closed", status.state)
        assertEquals("", status.title)
        assertEquals(0, status.comments)
        assertEquals("", status.htmlUrl)
    }

    @Test
    fun `parse throws on malformed json`() {
        assertThrows(Exception::class.java) { api.parse("not json") }
        assertThrows(Exception::class.java) { api.parse("""{"state":"open"}""") } // missing required number
    }

    @Test
    fun `parseThread puts the issue body first, then comments in order`() {
        val issue = """
            {
              "number": 7, "state": "open", "title": "Crash on launch",
              "body": "Steps to reproduce…",
              "created_at": "2026-07-01T10:00:00Z",
              "html_url": "https://github.com/jmerhar/sweetspot-android/issues/7",
              "user": { "login": "sweetspot-support" }
            }
        """.trimIndent()
        val comments = """
            [
              { "body": "Thanks, looking into it.", "created_at": "2026-07-02T09:30:00Z", "user": { "login": "jmerhar" } },
              { "body": "Fixed in 6.7.", "created_at": "2026-07-03T08:00:00Z", "user": { "login": "jmerhar" } }
            ]
        """.trimIndent()
        val thread = api.parseThread(issue, comments)

        assertEquals(7, thread.number)
        assertEquals("Crash on launch", thread.title)
        assertEquals("open", thread.state)
        assertEquals(3, thread.items.size)
        assertEquals("sweetspot-support", thread.items[0].author)
        assertEquals("Steps to reproduce…", thread.items[0].body)
        assertEquals(Instant.parse("2026-07-01T10:00:00Z").toEpochMilli(), thread.items[0].createdAtMs)
        assertTrue(thread.items[0].mine)  // authored by the bot → the reporter's own
        assertEquals("jmerhar", thread.items[1].author)
        assertFalse(thread.items[1].mine) // maintainer reply → not mine
        assertEquals("Fixed in 6.7.", thread.items[2].body)
    }

    @Test
    fun `parseThread strips the Worker reply prefix from a reporter's own comment`() {
        // The Worker posts a reporter's in-app reply as the bot with a leading marker; the app labels
        // it "You", so the marker is stripped for display (and to match the optimistic copy on send).
        val issue = """{"number":9,"state":"open","title":"x","user":{"login":"sweetspot-support"}}"""
        val comments = """
            [
              { "body": "💬 **Reporter (via app):**\n\nStill broken on 6.7.", "created_at": "2026-07-02T09:30:00Z", "user": { "login": "sweetspot-support" } },
              { "body": "Thanks, on it.", "created_at": "2026-07-03T08:00:00Z", "user": { "login": "jmerhar" } }
            ]
        """.trimIndent()
        val thread = api.parseThread(issue, comments)

        // Bot-authored reply: prefix stripped, marked as the user's own.
        assertEquals("Still broken on 6.7.", thread.items[1].body)
        assertTrue(thread.items[1].mine)
        // Maintainer reply without the prefix is left untouched.
        assertEquals("Thanks, on it.", thread.items[2].body)
        assertFalse(thread.items[2].mine)
    }

    @Test
    fun `parseThread strips the app footer and diagnostics block from the issue body`() {
        // The Worker appends "Submitted from the SweetSpot app." + a collapsible diagnostics code block
        // to every issue body; the in-app thread shows only the reporter's own text (the code block
        // also renders with a non-theme-aware background). The full body stays on GitHub.
        val body = "It crashed on launch.\\n\\n<sub>Submitted from the SweetSpot app.</sub>\\n\\n" +
            "<details><summary>Diagnostics</summary>\\n\\n```\\nApp: 6.6 (37)\\nZone: NL\\n```\\n</details>"
        val issue = """{"number":12,"state":"open","title":"x","body":"$body","user":{"login":"sweetspot-support"}}"""
        val thread = api.parseThread(issue, "[]")
        assertEquals("It crashed on launch.", thread.items[0].body)
    }

    @Test
    fun `parseThread tolerates no body and unparseable timestamps`() {
        // Issue with created_at absent (blank branch); comment with a bad timestamp (exception branch).
        val issue = """{"number":8,"state":"open","title":"x"}"""
        val comments = """[{"body":"hi","created_at":"not-a-date","user":{"login":"jmerhar"}}]"""
        val thread = api.parseThread(issue, comments)
        assertEquals(2, thread.items.size)
        assertEquals("", thread.items[0].author)
        assertEquals("", thread.items[0].body)
        assertEquals(0L, thread.items[0].createdAtMs) // blank → 0
        assertEquals(0L, thread.items[1].createdAtMs) // unparseable → 0
    }

    @Test
    fun `parseThread throws on malformed json`() {
        assertThrows(Exception::class.java) { api.parseThread("not json", "[]") }
        assertThrows(Exception::class.java) { api.parseThread("""{"number":1,"state":"open"}""", "not json") }
    }
}
