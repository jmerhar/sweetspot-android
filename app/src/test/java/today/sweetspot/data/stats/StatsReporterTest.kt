package today.sweetspot.data.stats

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StatsReporterTest {

    private val json = Json { ignoreUnknownKeys = true }

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
