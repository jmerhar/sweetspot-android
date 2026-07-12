package today.sweetspot.data.api

import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import today.sweetspot.model.TaxComponent

class TariffApiParseTest {

    private val api = TariffApi()

    private val validJson = """
        {
          "schemaVersion": 1,
          "country": "NL",
          "currency": "EUR",
          "generated": "2026-07-11T04:17:00Z",
          "usable": true,
          "errors": [],
          "warnings": ["enever: something"],
          "taxes": [
            {"id":"energyTax","name":"Energy tax","type":"perKwh","value":0.09161,"source":"frank"},
            {"id":"vat","name":"VAT","type":"percentage","value":0.21,"source":"frank"}
          ],
          "suppliers": [
            {"id":"frankenergie","name":"Frank Energie","surchargePerKwh":0.015,"fixedMonthlyFee":null,"source":"frank"},
            {"id":"tibber","name":"Tibber","surchargePerKwh":0.02125,"source":"enever"}
          ]
        }
    """.trimIndent()

    @Test
    fun `parses the full tariff schema`() {
        val t = api.parse(validJson)
        assertEquals("NL", t.country)
        assertEquals("EUR", t.currency)
        assertTrue(t.usable)
        assertEquals(2, t.taxes.size)
        assertEquals(TaxComponent.TYPE_PER_KWH, t.taxes[0].type)
        assertEquals(0.09161, t.taxes[0].value, 1e-9)
        assertEquals(2, t.suppliers.size)
        assertEquals("Frank Energie", t.suppliers[0].name)
        assertEquals(0.015, t.suppliers[0].surchargePerKwh, 1e-9)
        assertEquals(null, t.suppliers[0].fixedMonthlyFee)
    }

    @Test
    fun `ignores unknown fields`() {
        val withExtra = validJson.replace("\"usable\": true,", "\"usable\": true, \"newField\": 42,")
        assertTrue(api.parse(withExtra).usable)
    }

    @Test
    fun `missing fields fall back to defaults`() {
        val t = api.parse("""{"country":"NL"}""")
        assertEquals("NL", t.country)
        assertFalse(t.usable)          // default false
        assertTrue(t.suppliers.isEmpty())
        assertTrue(t.taxes.isEmpty())
    }

    @Test
    fun `malformed json throws`() {
        assertThrows(SerializationException::class.java) { api.parse("not json") }
    }
}
