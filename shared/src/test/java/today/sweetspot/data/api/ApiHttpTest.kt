package today.sweetspot.data.api

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

/**
 * Tests the HTTP paths (`fetchPrices`/`fetchRaw`) of each API client — the request/response
 * plumbing the `parse()`-only suites don't exercise: success flows through to a [FetchResult],
 * a non-200 maps to [HttpException] carrying the status code, and (ENTSO-E) an Acknowledgement
 * body maps to [EntsoeException]. Uses an [OkHttpClient] whose interceptor returns canned
 * responses, so no network is involved.
 */
class ApiHttpTest {

    private val from: Instant = Instant.parse("2026-01-01T00:00:00Z")
    private val to: Instant = Instant.parse("2026-01-02T00:00:00Z")
    private val tz: ZoneId = ZoneId.of("Europe/Amsterdam")

    /** An OkHttpClient whose interceptor short-circuits every call with a canned response. */
    private fun cannedClient(code: Int, body: String, onUrl: (String) -> Unit = {}): OkHttpClient =
        OkHttpClient.Builder().addInterceptor(
            Interceptor { chain ->
                onUrl(chain.request().url.toString())
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(code)
                    .message(if (code == 200) "OK" else "Error")
                    .body(body.toResponseBody("application/json".toMediaType()))
                    .build()
            }
        ).build()

    // --- EnergyZero ---

    @Test
    fun `energyzero fetchPrices returns a result on success`() {
        val result = EnergyZeroApi(cannedClient(200, """{"Prices":[]}""")).fetchPrices(from, to, tz)
        assertEquals("EnergyZero", result.source)
        assertTrue(result.prices.isEmpty())
    }

    @Test
    fun `energyzero maps a non-200 to HttpException with the status code`() {
        val e = assertThrows(HttpException::class.java) {
            EnergyZeroApi(cannedClient(503, "")).fetchPrices(from, to, tz)
        }
        assertEquals(503, e.code)
    }

    @Test
    fun `energyzero builds a request URL with the date range`() {
        var url = ""
        EnergyZeroApi(cannedClient(200, """{"Prices":[]}""") { url = it }).fetchRaw(from, to)
        assertTrue(url, url.contains("api.energyzero.nl"))
        assertTrue(url, url.contains("fromDate=") && url.contains("tillDate="))
    }

    // --- Spot-Hinta.fi ---

    @Test
    fun `spothinta fetchPrices returns a result on success`() {
        val result = SpotHintaApi("FI", cannedClient(200, "[]")).fetchPrices(from, to, tz)
        assertEquals("Spot-Hinta.fi", result.source)
        assertTrue(result.prices.isEmpty())
    }

    @Test
    fun `spothinta maps a non-200 to HttpException with the status code`() {
        val e = assertThrows(HttpException::class.java) {
            SpotHintaApi("FI", cannedClient(500, "")).fetchPrices(from, to, tz)
        }
        assertEquals(500, e.code)
    }

    // --- Energy-Charts ---

    @Test
    fun `energycharts fetchPrices returns a result on success`() {
        val result = EnergyChartsApi("NL", cannedClient(200, """{"unix_seconds":[],"price":[]}""")).fetchPrices(from, to, tz)
        assertEquals("Energy-Charts", result.source)
        assertTrue(result.prices.isEmpty())
    }

    @Test
    fun `energycharts maps a non-200 to HttpException with the status code`() {
        val e = assertThrows(HttpException::class.java) {
            EnergyChartsApi("NL", cannedClient(429, "")).fetchPrices(from, to, tz)
        }
        assertEquals(429, e.code)
    }

    // --- aWATTar ---

    @Test
    fun `awattar fetchPrices returns a result on success`() {
        val result = AwattarApi("AT", cannedClient(200, """{"data":[]}""")).fetchPrices(from, to, tz)
        assertEquals("aWATTar", result.source)
        assertTrue(result.prices.isEmpty())
    }

    @Test
    fun `awattar maps a non-200 to HttpException with the status code`() {
        val e = assertThrows(HttpException::class.java) {
            AwattarApi("AT", cannedClient(502, "")).fetchPrices(from, to, tz)
        }
        assertEquals(502, e.code)
    }

    // --- ENTSO-E ---

    @Test
    fun `entsoe fetchPrices parses a publication document on success`() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Publication_MarketDocument xmlns="urn:iec62325.351:tc57wg16:451-3:publicationdocument:7:3">
              <TimeSeries>
                <mRID>1</mRID>
                <curveType>A01</curveType>
                <Period>
                  <timeInterval><start>2026-03-02T23:00Z</start><end>2026-03-03T23:00Z</end></timeInterval>
                  <resolution>PT60M</resolution>
                  <Point><position>1</position><price.amount>50.00</price.amount></Point>
                  <Point><position>2</position><price.amount>45.00</price.amount></Point>
                </Period>
              </TimeSeries>
            </Publication_MarketDocument>
        """.trimIndent()
        val result = EntsoeApi("token", "10YNL----------L", cannedClient(200, xml)).fetchPrices(from, to, tz)
        assertEquals("ENTSO-E", result.source)
        assertEquals(2, result.prices.size)
    }

    @Test
    fun `entsoe maps a non-200 to HttpException with the status code`() {
        val e = assertThrows(HttpException::class.java) {
            EntsoeApi("token", "zone", cannedClient(503, "")).fetchPrices(from, to, tz)
        }
        assertEquals(503, e.code)
    }

    @Test
    fun `entsoe maps an acknowledgement document to EntsoeException with the reason`() {
        val ack = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Acknowledgement_MarketDocument>
              <Reason><text>No matching data found</text></Reason>
            </Acknowledgement_MarketDocument>
        """.trimIndent()
        val e = assertThrows(EntsoeException::class.java) {
            EntsoeApi("token", "zone", cannedClient(200, ack)).fetchPrices(from, to, tz)
        }
        assertEquals("No matching data found", e.reason)
    }

    @Test
    fun `entsoe acknowledgement without reason text yields Unknown error`() {
        // An acknowledgement whose Reason has no (non-empty) text → the extractor's fallback.
        val ack = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Acknowledgement_MarketDocument>
              <Reason><code>999</code></Reason>
            </Acknowledgement_MarketDocument>
        """.trimIndent()
        val e = assertThrows(EntsoeException::class.java) {
            EntsoeApi("token", "zone", cannedClient(200, ack)).fetchPrices(from, to, tz)
        }
        assertEquals("Unknown error", e.reason)
    }
}
