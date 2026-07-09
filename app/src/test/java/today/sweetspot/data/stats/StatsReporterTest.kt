package today.sweetspot.data.stats

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class StatsReporterTest {

    private val json = Json { ignoreUnknownKeys = true }

    private lateinit var prefs: SharedPreferences

    /** In-memory [StatsCollector] that tracks whether it was cleared. */
    private class FakeCollector(records: List<StatsRecord> = emptyList()) : StatsCollector {
        private val store = records.toMutableList()
        var clearCount = 0
            private set
        override fun record(record: StatsRecord) { store.add(record) }
        override fun readAll(): List<StatsRecord> = store.toList()
        override fun clear() { clearCount++; store.clear() }
    }

    /** [StatsPoster] that records the payload and returns a fixed code (or throws). */
    private class FakePoster(private val code: Int = 200, private val throws: Boolean = false) : StatsPoster {
        var lastJson: String? = null
            private set
        var callCount = 0
            private set
        override fun post(json: String): Int {
            callCount++
            lastJson = json
            if (throws) throw RuntimeException("network down")
            return code
        }
    }

    private fun oneRecord() = listOf(StatsRecord(1711700000L, "NL", "entsoe", "phone", true, "", 450))

    private fun reporter(
        collector: StatsCollector,
        poster: StatsPoster,
        language: String = "en",
        status: String = "trial",
    ) = StatsReporter(collector, prefs, "4.0", { language }, { status }, poster)

    @Before
    fun setUp() {
        prefs = ApplicationProvider.getApplicationContext<Context>()
            .getSharedPreferences("test_stats_${System.nanoTime()}", Context.MODE_PRIVATE)
    }

    // --- reportOutcomeFor (pure policy) ---

    @Test
    fun `reportOutcomeFor maps status codes to actions`() {
        assertEquals(ReportOutcome.CLEAR_AND_STAMP, reportOutcomeFor(200))
        assertEquals(ReportOutcome.CLEAR, reportOutcomeFor(400))
        assertEquals(ReportOutcome.CLEAR, reportOutcomeFor(413))
        assertEquals(ReportOutcome.KEEP, reportOutcomeFor(429)) // rate limited
        assertEquals(ReportOutcome.KEEP, reportOutcomeFor(500))
        assertEquals(ReportOutcome.KEEP, reportOutcomeFor(503))
    }

    // --- reportIfDue ---

    @Test
    fun `reportIfDue posts records and clears on HTTP 200`() {
        val collector = FakeCollector(oneRecord())
        val poster = FakePoster(code = 200)
        reporter(collector, poster, language = "nl", status = "subscribed").reportIfDue()

        assertEquals(1, poster.callCount)
        assertEquals(1, collector.clearCount)
        // Providers were invoked and threaded into the payload.
        val payload = json.parseToJsonElement(poster.lastJson!!).jsonObject
        assertEquals("nl", payload["lang"]?.jsonPrimitive?.content)
        assertEquals("subscribed", payload["status"]?.jsonPrimitive?.content)
    }

    @Test
    fun `reportIfDue clears data on a 4xx client error`() {
        val collector = FakeCollector(oneRecord())
        reporter(collector, FakePoster(code = 400)).reportIfDue()
        assertEquals(1, collector.clearCount)
    }

    @Test
    fun `reportIfDue keeps data on 429 rate limit`() {
        val collector = FakeCollector(oneRecord())
        reporter(collector, FakePoster(code = 429)).reportIfDue()
        assertEquals(0, collector.clearCount)
    }

    @Test
    fun `reportIfDue keeps data on a 5xx server error`() {
        val collector = FakeCollector(oneRecord())
        reporter(collector, FakePoster(code = 503)).reportIfDue()
        assertEquals(0, collector.clearCount)
    }

    @Test
    fun `reportIfDue keeps data on a network error`() {
        val collector = FakeCollector(oneRecord())
        reporter(collector, FakePoster(throws = true)).reportIfDue()
        assertEquals(0, collector.clearCount)
    }

    @Test
    fun `reportIfDue is a no-op with no records`() {
        val poster = FakePoster(code = 200)
        reporter(FakeCollector(emptyList()), poster).reportIfDue()
        assertEquals(0, poster.callCount)
    }

    @Test
    fun `reportIfDue is a no-op before the interval has elapsed`() {
        // A successful report stamps the timestamp; an immediate second call must not post.
        val poster = FakePoster(code = 200)
        val reporter = reporter(FakeCollector(oneRecord()), poster)
        reporter.reportIfDue()
        assertEquals(1, poster.callCount)
        assertFalse(reporter.isReportDue())

        val poster2 = FakePoster(code = 200)
        reporter(FakeCollector(oneRecord()), poster2).reportIfDue()
        assertEquals(0, poster2.callCount)
    }

    @Test
    fun `resetReportTimer makes reporting due again`() {
        val reporter = reporter(FakeCollector(oneRecord()), FakePoster(code = 200))
        reporter.reportIfDue()
        assertFalse(reporter.isReportDue())
        reporter.resetReportTimer()
        assertTrue(reporter.isReportDue())
    }

    // --- buildReportJson defaults ---

    @Test
    fun `buildReportJson uses default language and status when omitted`() {
        val result = json.parseToJsonElement(buildReportJson(oneRecord(), "4.0")).jsonObject
        assertEquals("", result["lang"]?.jsonPrimitive?.content)
        assertEquals("trial", result["status"]?.jsonPrimitive?.content)
    }

    @Test
    fun `buildReportJson formats single success record`() {
        val records = listOf(
            StatsRecord(1711700000L, "NL", "entsoe", "phone", true, "", 450)
        )

        val result = json.parseToJsonElement(buildReportJson(records, "4.0", "en", "trial")).jsonObject

        assertEquals(2, result["v"]?.jsonPrimitive?.int)
        assertEquals("4.0", result["app"]?.jsonPrimitive?.content)
        assertEquals("en", result["lang"]?.jsonPrimitive?.content)
        assertEquals("trial", result["status"]?.jsonPrimitive?.content)

        val groups = result["records"]!!.jsonArray
        assertEquals(1, groups.size)

        val group = groups[0].jsonObject
        assertEquals("NL", group["z"]?.jsonPrimitive?.content)
        assertEquals("entsoe", group["s"]?.jsonPrimitive?.content)
        assertEquals("phone", group["d"]?.jsonPrimitive?.content)

        val entries = group["r"]!!.jsonArray
        assertEquals(1, entries.size)
        val entry = entries[0].jsonObject
        assertEquals(1711700000L, entry["t"]?.jsonPrimitive?.long)
        assertEquals(true, entry["ok"]?.jsonPrimitive?.content?.toBoolean())
        assertEquals(450L, entry["ms"]?.jsonPrimitive?.long)
    }

    @Test
    fun `buildReportJson formats failure with error category and duration`() {
        val records = listOf(
            StatsRecord(1711703600L, "NL", "entsoe", "phone", false, "TIMEOUT", 10000)
        )

        val result = json.parseToJsonElement(buildReportJson(records, "4.0", "nl", "unlocked")).jsonObject
        val entry = result["records"]!!.jsonArray[0].jsonObject["r"]!!.jsonArray[0].jsonObject

        assertEquals(false, entry["ok"]?.jsonPrimitive?.content?.toBoolean())
        assertEquals("TIMEOUT", entry["e"]?.jsonPrimitive?.content)
        assertEquals(10000L, entry["ms"]?.jsonPrimitive?.long)
    }

    @Test
    fun `buildReportJson groups by zone source device`() {
        val records = listOf(
            StatsRecord(1711700000L, "NL", "entsoe", "phone", true, "", 200),
            StatsRecord(1711703600L, "NL", "entsoe", "phone", false, "TIMEOUT", 10000),
            StatsRecord(1711701000L, "NL", "entsoe", "watch", true, "", 300),
            StatsRecord(1711702000L, "DE_LU", "entsoe", "phone", true, "", 250)
        )

        val result = json.parseToJsonElement(buildReportJson(records, "4.0", "de", "expired")).jsonObject
        val groups = result["records"]!!.jsonArray

        assertEquals(3, groups.size)

        // NL/entsoe/phone should have 2 records
        val nlPhoneGroup = groups.first { g ->
            val o = g.jsonObject
            o["z"]?.jsonPrimitive?.content == "NL" &&
                o["s"]?.jsonPrimitive?.content == "entsoe" &&
                o["d"]?.jsonPrimitive?.content == "phone"
        }.jsonObject
        assertEquals(2, nlPhoneGroup["r"]!!.jsonArray.size)
    }

    @Test
    fun `buildReportJson includes version app lang and status`() {
        val records = listOf(
            StatsRecord(1711700000L, "FI", "spothinta", "watch", true, "", 500)
        )

        val result = json.parseToJsonElement(buildReportJson(records, "3.5", "fi", "trial")).jsonObject
        assertEquals(2, result["v"]?.jsonPrimitive?.int)
        assertEquals("3.5", result["app"]?.jsonPrimitive?.content)
        assertEquals("fi", result["lang"]?.jsonPrimitive?.content)
        assertEquals("trial", result["status"]?.jsonPrimitive?.content)
    }

    @Test
    fun `buildReportJson omits error field for success but includes ms`() {
        val records = listOf(
            StatsRecord(1711700000L, "NL", "entsoe", "phone", true, "", 320)
        )

        val result = json.parseToJsonElement(buildReportJson(records, "4.0", "en", "trial")).jsonObject
        val entry = result["records"]!!.jsonArray[0].jsonObject["r"]!!.jsonArray[0].jsonObject

        assertTrue("Success entry should not have 'e' field", !entry.containsKey("e"))
        assertEquals(320L, entry["ms"]?.jsonPrimitive?.long)
    }
}
