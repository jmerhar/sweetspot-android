package today.sweetspot.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

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
}
