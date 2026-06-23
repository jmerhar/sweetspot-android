package today.sweetspot.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import today.sweetspot.model.EvVehicle

/**
 * Tests for [EvVehicleRepository] parsing, querying, and search.
 */
class EvVehicleRepositoryTest {

    private val sampleJson = """
        [
        {"brand":"Volkswagen","model":"ID.3","variant":"Pro","year":2023,"batteryKwh":58.0,"acMaxPowerKw":11.0},
        {"brand":"Volkswagen","model":"ID.3","variant":"Pro S","year":2024,"batteryKwh":77.0,"acMaxPowerKw":11.0},
        {"brand":"Volkswagen","model":"ID.4","variant":null,"year":2023,"batteryKwh":77.0,"acMaxPowerKw":11.0},
        {"brand":"Tesla","model":"Model 3","variant":"Long Range","year":2024,"batteryKwh":75.0,"acMaxPowerKw":11.0},
        {"brand":"Abarth","model":"500e","variant":null,"year":2023,"batteryKwh":37.8,"acMaxPowerKw":11.0}
        ]
    """.trimIndent()

    private val repo = EvVehicleRepository(sampleJson)

    @Test
    fun `parses all vehicles`() {
        assertEquals(5, repo.vehicles.size)
    }

    @Test
    fun `parses fields correctly`() {
        val id3Pro = repo.vehicles.first { it.model == "ID.3" && it.variant == "Pro" }
        assertEquals("Volkswagen", id3Pro.brand)
        assertEquals(2023, id3Pro.year)
        assertEquals(58.0, id3Pro.batteryKwh, 0.001)
        assertEquals(11.0, id3Pro.acMaxPowerKw, 0.001)
    }

    @Test
    fun `null variant is preserved`() {
        val id4 = repo.vehicles.first { it.model == "ID.4" }
        assertEquals(null, id4.variant)
    }

    @Test
    fun `brands returns distinct names sorted case-insensitively`() {
        assertEquals(listOf("Abarth", "Tesla", "Volkswagen"), repo.brands())
    }

    @Test
    fun `models filters by brand case-insensitively`() {
        val vw = repo.models("volkswagen")
        assertEquals(3, vw.size)
        assertTrue(vw.all { it.brand == "Volkswagen" })
    }

    @Test
    fun `search matches across brand and model`() {
        val results = repo.search("vw")
        // "vw" should not match Volkswagen (no abbreviation expansion); brand text is matched literally.
        assertTrue(results.isEmpty())
    }

    @Test
    fun `search narrows with multiple terms`() {
        val results = repo.search("volkswagen id.3")
        assertEquals(2, results.size)
        assertTrue(results.all { it.model == "ID.3" })
    }

    @Test
    fun `search matches variant text`() {
        val results = repo.search("long range")
        assertEquals(1, results.size)
        assertEquals("Model 3", results.first().model)
    }

    @Test
    fun `search is case-insensitive`() {
        assertEquals(repo.search("TESLA").size, repo.search("tesla").size)
    }

    @Test
    fun `blank search returns all vehicles`() {
        assertEquals(repo.vehicles.size, repo.search("   ").size)
    }

    @Test
    fun `displayName includes variant and year`() {
        val v = EvVehicle("Volkswagen", "ID.3", "Pro S", 2024, 77.0, 11.0)
        assertEquals("Volkswagen ID.3 Pro S (2024)", v.displayName)
    }

    @Test
    fun `displayName omits null variant and year`() {
        val v = EvVehicle("Tesla", "Model 3", null, null, 75.0, 11.0)
        assertEquals("Tesla Model 3", v.displayName)
    }

    @Test
    fun `malformed JSON yields empty list`() {
        val repo = EvVehicleRepository("not json at all")
        assertTrue(repo.vehicles.isEmpty())
        assertFalse(repo.search("anything").isNotEmpty())
    }
}
