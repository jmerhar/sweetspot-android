package si.merhar.sweetspot.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

/**
 * Tests for [EntsoeApi.parse] with embedded XML strings.
 *
 * Covers hourly and 15-minute resolution, A03 gap filling, multi-TimeSeries,
 * error responses, and DST transitions.
 */
class EntsoeApiParseTest {

    private val zone = ZoneId.of("Europe/Amsterdam")
    private val api = EntsoeApi(token = "test", biddingZone = BiddingZone.NL)

    @Test
    fun `parses PT60M hourly response`() {
        val xml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <Publication_MarketDocument xmlns="urn:iec62325.351:tc57wg16:451-3:publicationdocument:7:3">
          <TimeSeries>
            <mRID>1</mRID>
            <curveType>A01</curveType>
            <currency_Unit.name>EUR</currency_Unit.name>
            <price_Measure_Unit.name>MWH</price_Measure_Unit.name>
            <Period>
              <timeInterval>
                <start>2026-03-02T23:00Z</start>
                <end>2026-03-03T23:00Z</end>
              </timeInterval>
              <resolution>PT60M</resolution>
              <Point><position>1</position><price.amount>50.00</price.amount></Point>
              <Point><position>2</position><price.amount>45.00</price.amount></Point>
              <Point><position>3</position><price.amount>40.00</price.amount></Point>
            </Period>
          </TimeSeries>
        </Publication_MarketDocument>
        """.trimIndent()

        val prices = api.parse(xml, zone)

        assertEquals(3, prices.size)
        // EUR/MWh -> EUR/kWh (÷ 1000)
        assertEquals(0.050, prices[0].price, 0.0001)
        assertEquals(0.045, prices[1].price, 0.0001)
        assertEquals(0.040, prices[2].price, 0.0001)
        // 2026-03-02T23:00Z = 2026-03-03T00:00 CET
        assertEquals(0, prices[0].time.hour)
        assertEquals(1, prices[1].time.hour)
        assertEquals(2, prices[2].time.hour)
        assertEquals(zone, prices[0].time.zone)
    }

    @Test
    fun `parses PT15M response and aggregates to hourly averages`() {
        val xml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <Publication_MarketDocument xmlns="urn:iec62325.351:tc57wg16:451-3:publicationdocument:7:3">
          <TimeSeries>
            <mRID>1</mRID>
            <curveType>A01</curveType>
            <Period>
              <timeInterval>
                <start>2026-03-02T23:00Z</start>
                <end>2026-03-03T01:00Z</end>
              </timeInterval>
              <resolution>PT15M</resolution>
              <Point><position>1</position><price.amount>100.00</price.amount></Point>
              <Point><position>2</position><price.amount>200.00</price.amount></Point>
              <Point><position>3</position><price.amount>300.00</price.amount></Point>
              <Point><position>4</position><price.amount>400.00</price.amount></Point>
              <Point><position>5</position><price.amount>120.00</price.amount></Point>
              <Point><position>6</position><price.amount>120.00</price.amount></Point>
              <Point><position>7</position><price.amount>120.00</price.amount></Point>
              <Point><position>8</position><price.amount>120.00</price.amount></Point>
            </Period>
          </TimeSeries>
        </Publication_MarketDocument>
        """.trimIndent()

        val prices = api.parse(xml, zone)

        assertEquals(2, prices.size)
        // Hour 1: avg(100, 200, 300, 400) = 250 MWh = 0.250 kWh
        assertEquals(0.250, prices[0].price, 0.0001)
        // Hour 2: avg(120, 120, 120, 120) = 120 MWh = 0.120 kWh
        assertEquals(0.120, prices[1].price, 0.0001)
    }

    @Test
    fun `handles A03 curve type with position gaps`() {
        // Position 3 is missing — should inherit price from position 2
        val xml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <Publication_MarketDocument xmlns="urn:iec62325.351:tc57wg16:451-3:publicationdocument:7:3">
          <TimeSeries>
            <mRID>1</mRID>
            <curveType>A03</curveType>
            <Period>
              <timeInterval>
                <start>2026-03-02T23:00Z</start>
                <end>2026-03-03T03:00Z</end>
              </timeInterval>
              <resolution>PT60M</resolution>
              <Point><position>1</position><price.amount>50.00</price.amount></Point>
              <Point><position>2</position><price.amount>60.00</price.amount></Point>
              <Point><position>4</position><price.amount>80.00</price.amount></Point>
            </Period>
          </TimeSeries>
        </Publication_MarketDocument>
        """.trimIndent()

        val prices = api.parse(xml, zone)

        assertEquals(4, prices.size)
        assertEquals(0.050, prices[0].price, 0.0001)
        assertEquals(0.060, prices[1].price, 0.0001)
        // Position 3 filled from position 2's price
        assertEquals(0.060, prices[2].price, 0.0001)
        assertEquals(0.080, prices[3].price, 0.0001)
    }

    @Test
    fun `handles multiple TimeSeries for multi-day response`() {
        val xml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <Publication_MarketDocument xmlns="urn:iec62325.351:tc57wg16:451-3:publicationdocument:7:3">
          <TimeSeries>
            <mRID>1</mRID>
            <curveType>A01</curveType>
            <Period>
              <timeInterval>
                <start>2026-03-02T23:00Z</start>
                <end>2026-03-03T23:00Z</end>
              </timeInterval>
              <resolution>PT60M</resolution>
              <Point><position>1</position><price.amount>50.00</price.amount></Point>
              <Point><position>2</position><price.amount>55.00</price.amount></Point>
            </Period>
          </TimeSeries>
          <TimeSeries>
            <mRID>2</mRID>
            <curveType>A01</curveType>
            <Period>
              <timeInterval>
                <start>2026-03-03T23:00Z</start>
                <end>2026-03-04T23:00Z</end>
              </timeInterval>
              <resolution>PT60M</resolution>
              <Point><position>1</position><price.amount>70.00</price.amount></Point>
              <Point><position>2</position><price.amount>75.00</price.amount></Point>
            </Period>
          </TimeSeries>
        </Publication_MarketDocument>
        """.trimIndent()

        val prices = api.parse(xml, zone)

        assertEquals(4, prices.size)
        // Day 1 starts 2026-03-02T23:00Z = 2026-03-03T00:00 CET
        assertEquals(0, prices[0].time.hour)
        assertEquals(3, prices[0].time.dayOfMonth)
        // Day 2 starts 2026-03-03T23:00Z = 2026-03-04T00:00 CET
        assertEquals(0, prices[2].time.hour)
        assertEquals(4, prices[2].time.dayOfMonth)
        // All sorted chronologically
        for (i in 1 until prices.size) {
            assertTrue(prices[i].time > prices[i - 1].time)
        }
    }

    @Test(expected = RuntimeException::class)
    fun `error response throws with reason text`() {
        val xml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <Acknowledgement_MarketDocument xmlns="urn:iec62325.351:tc57wg16:451-1:acknowledgementdocument:7:0">
          <mRID>test</mRID>
          <Reason>
            <code>999</code>
            <text>No matching data found</text>
          </Reason>
        </Acknowledgement_MarketDocument>
        """.trimIndent()

        api.parse(xml, zone)
    }

    @Test
    fun `error response includes reason text in exception message`() {
        val xml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <Acknowledgement_MarketDocument xmlns="urn:iec62325.351:tc57wg16:451-1:acknowledgementdocument:7:0">
          <mRID>test</mRID>
          <Reason>
            <code>999</code>
            <text>No matching data found</text>
          </Reason>
        </Acknowledgement_MarketDocument>
        """.trimIndent()

        try {
            api.parse(xml, zone)
        } catch (e: RuntimeException) {
            assertTrue(e.message!!.contains("No matching data found"))
            return
        }
        throw AssertionError("Expected RuntimeException")
    }

    @Test
    fun `spring forward DST produces 23-hour day`() {
        // 2026-03-29: CET -> CEST. Day is 23 hours (02:00 skipped).
        // UTC 2026-03-28T23:00 = local 2026-03-29T00:00 CET
        // UTC 2026-03-29T22:00 = local 2026-03-30T00:00 CEST (23 hours later)
        val points = (1..23).joinToString("\n") { pos ->
            "  <Point><position>$pos</position><price.amount>${50.0 + pos}</price.amount></Point>"
        }
        val xml = buildDayXml("2026-03-28T23:00Z", "2026-03-29T22:00Z", "PT60M", points)

        val prices = api.parse(xml, zone)

        assertEquals(23, prices.size)
        // First hour: CET 00:00
        assertEquals(0, prices[0].time.hour)
        // 02:00 is skipped, so after 01:00 CET comes 03:00 CEST
        assertEquals(1, prices[1].time.hour)
        assertEquals(3, prices[2].time.hour)
        // Last hour: CEST 23:00 (23 positions covering 00:00–23:00 with 02:00 skipped)
        assertEquals(23, prices[22].time.hour)
        // All sorted chronologically
        for (i in 1 until prices.size) {
            assertTrue(
                "Price at $i should be after ${i - 1}",
                prices[i].time.toEpochSecond() > prices[i - 1].time.toEpochSecond()
            )
        }
    }

    @Test
    fun `fall back DST produces 25-hour day`() {
        // 2026-10-25: CEST -> CET. Day is 25 hours (02:00 repeated).
        // UTC 2026-10-24T22:00 = local 2026-10-25T00:00 CEST
        // UTC 2026-10-25T23:00 = local 2026-10-26T00:00 CET (25 hours later)
        val points = (1..25).joinToString("\n") { pos ->
            "  <Point><position>$pos</position><price.amount>${50.0 + pos}</price.amount></Point>"
        }
        val xml = buildDayXml("2026-10-24T22:00Z", "2026-10-25T23:00Z", "PT60M", points)

        val prices = api.parse(xml, zone)

        assertEquals(25, prices.size)
        // First: CEST 00:00
        assertEquals(0, prices[0].time.hour)
        // Two entries with local hour 02:00 but different offsets (CEST then CET)
        assertEquals(2, prices[2].time.hour)
        assertEquals("+02:00", prices[2].time.offset.toString())
        assertEquals(2, prices[3].time.hour)
        assertEquals("+01:00", prices[3].time.offset.toString())
        // Last: CET 23:00
        assertEquals(23, prices[24].time.hour)
        // All sorted chronologically by instant
        for (i in 1 until prices.size) {
            assertTrue(
                "Price at $i should be after ${i - 1}",
                prices[i].time.toEpochSecond() > prices[i - 1].time.toEpochSecond()
            )
        }
    }

    @Test
    fun `A03 with PT15M gaps fills and aggregates correctly`() {
        // 4 quarter-hours in one hour. Position 3 is missing (A03 gap).
        // Positions: 1=100, 2=200, (3=200 filled), 4=400
        // Hourly avg: (100+200+200+400)/4 = 225 MWh = 0.225 kWh
        val xml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <Publication_MarketDocument xmlns="urn:iec62325.351:tc57wg16:451-3:publicationdocument:7:3">
          <TimeSeries>
            <mRID>1</mRID>
            <curveType>A03</curveType>
            <Period>
              <timeInterval>
                <start>2026-03-02T23:00Z</start>
                <end>2026-03-03T00:00Z</end>
              </timeInterval>
              <resolution>PT15M</resolution>
              <Point><position>1</position><price.amount>100.00</price.amount></Point>
              <Point><position>2</position><price.amount>200.00</price.amount></Point>
              <Point><position>4</position><price.amount>400.00</price.amount></Point>
            </Period>
          </TimeSeries>
        </Publication_MarketDocument>
        """.trimIndent()

        val prices = api.parse(xml, zone)

        assertEquals(1, prices.size)
        assertEquals(0.225, prices[0].price, 0.0001)
    }

    @Test
    fun `empty Period produces no prices`() {
        val xml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <Publication_MarketDocument xmlns="urn:iec62325.351:tc57wg16:451-3:publicationdocument:7:3">
          <TimeSeries>
            <mRID>1</mRID>
            <curveType>A01</curveType>
            <Period>
              <timeInterval>
                <start>2026-03-02T23:00Z</start>
                <end>2026-03-03T23:00Z</end>
              </timeInterval>
              <resolution>PT60M</resolution>
            </Period>
          </TimeSeries>
        </Publication_MarketDocument>
        """.trimIndent()

        val prices = api.parse(xml, zone)
        assertTrue(prices.isEmpty())
    }

    @Test
    fun `handles negative prices`() {
        val xml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <Publication_MarketDocument xmlns="urn:iec62325.351:tc57wg16:451-3:publicationdocument:7:3">
          <TimeSeries>
            <mRID>1</mRID>
            <curveType>A01</curveType>
            <Period>
              <timeInterval>
                <start>2026-03-02T23:00Z</start>
                <end>2026-03-03T01:00Z</end>
              </timeInterval>
              <resolution>PT60M</resolution>
              <Point><position>1</position><price.amount>-10.50</price.amount></Point>
              <Point><position>2</position><price.amount>-5.25</price.amount></Point>
            </Period>
          </TimeSeries>
        </Publication_MarketDocument>
        """.trimIndent()

        val prices = api.parse(xml, zone)

        assertEquals(2, prices.size)
        assertEquals(-0.01050, prices[0].price, 0.00001)
        assertEquals(-0.00525, prices[1].price, 0.00001)
    }

    /**
     * Builds a minimal ENTSO-E Publication_MarketDocument XML string.
     * Avoids trimIndent issues when points are generated dynamically.
     */
    private fun buildDayXml(start: String, end: String, resolution: String, points: String): String {
        return """<Publication_MarketDocument xmlns="urn:iec62325.351:tc57wg16:451-3:publicationdocument:7:3">
<TimeSeries><mRID>1</mRID><curveType>A01</curveType>
<Period><timeInterval><start>$start</start><end>$end</end></timeInterval>
<resolution>$resolution</resolution>
$points
</Period></TimeSeries></Publication_MarketDocument>"""
    }
}
