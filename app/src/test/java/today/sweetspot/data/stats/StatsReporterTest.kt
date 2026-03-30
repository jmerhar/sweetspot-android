package today.sweetspot.data.stats

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StatsReporterTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `buildReportJson formats single success record`() {
        val records = listOf(
            StatsRecord(1711700000L, "NL", "entsoe", "phone", true, "")
        )

        val result = json.parseToJsonElement(buildReportJson(records, "4.0")).jsonObject

        assertEquals(1, result["v"]?.jsonPrimitive?.int)
        assertEquals("4.0", result["app"]?.jsonPrimitive?.content)

        val groups = result["records"]!!.jsonArray
        assertEquals(1, groups.size)

        val group = groups[0].jsonObject
        assertEquals("NL", group["z"]?.jsonPrimitive?.content)
        assertEquals("entsoe", group["s"]?.jsonPrimitive?.content)
        assertEquals("phone", group["d"]?.jsonPrimitive?.content)

        val entries = group["r"]!!.jsonArray
        assertEquals(1, entries.size)
        val entry = entries[0].jsonObject
        assertEquals(1711700000L, entry["t"]?.jsonPrimitive?.content?.toLong())
        assertEquals(true, entry["ok"]?.jsonPrimitive?.content?.toBoolean())
    }

    @Test
    fun `buildReportJson formats failure with error category`() {
        val records = listOf(
            StatsRecord(1711703600L, "NL", "entsoe", "phone", false, "TIMEOUT")
        )

        val result = json.parseToJsonElement(buildReportJson(records, "4.0")).jsonObject
        val entry = result["records"]!!.jsonArray[0].jsonObject["r"]!!.jsonArray[0].jsonObject

        assertEquals(false, entry["ok"]?.jsonPrimitive?.content?.toBoolean())
        assertEquals("TIMEOUT", entry["e"]?.jsonPrimitive?.content)
    }

    @Test
    fun `buildReportJson groups by zone source device`() {
        val records = listOf(
            StatsRecord(1711700000L, "NL", "entsoe", "phone", true, ""),
            StatsRecord(1711703600L, "NL", "entsoe", "phone", false, "TIMEOUT"),
            StatsRecord(1711701000L, "NL", "entsoe", "watch", true, ""),
            StatsRecord(1711702000L, "DE_LU", "entsoe", "phone", true, "")
        )

        val result = json.parseToJsonElement(buildReportJson(records, "4.0")).jsonObject
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
    fun `buildReportJson includes version and app`() {
        val records = listOf(
            StatsRecord(1711700000L, "FI", "spothinta", "watch", true, "")
        )

        val result = json.parseToJsonElement(buildReportJson(records, "3.5")).jsonObject
        assertEquals(1, result["v"]?.jsonPrimitive?.int)
        assertEquals("3.5", result["app"]?.jsonPrimitive?.content)
    }

    @Test
    fun `buildReportJson omits error field for success`() {
        val records = listOf(
            StatsRecord(1711700000L, "NL", "entsoe", "phone", true, "")
        )

        val result = json.parseToJsonElement(buildReportJson(records, "4.0")).jsonObject
        val entry = result["records"]!!.jsonArray[0].jsonObject["r"]!!.jsonArray[0].jsonObject

        assertTrue("Success entry should not have 'e' field", !entry.containsKey("e"))
    }
}
