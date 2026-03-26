package si.merhar.sweetspot.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import si.merhar.sweetspot.model.Countries

/**
 * Tests for [DataSource] and [DataSources] registry.
 */
class DataSourceTest {

    private val allZoneIds = Countries.all.flatMap { it.zones }.map { it.id }.toSet()

    @Test
    fun `all source IDs are unique`() {
        val ids = DataSources.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `defaultsForZone NL returns ENTSOE, ENERGY_ZERO, and ENERGY_CHARTS`() {
        val sources = DataSources.defaultsForZone("NL")
        assertEquals(3, sources.size)
        assertEquals(DataSources.ENTSOE, sources[0])
        assertEquals(DataSources.ENERGY_ZERO, sources[1])
        assertEquals(DataSources.ENERGY_CHARTS, sources[2])
    }

    @Test
    fun `defaultsForZone FI returns ENTSOE and SPOT_HINTA`() {
        val sources = DataSources.defaultsForZone("FI")
        assertEquals(2, sources.size)
        assertEquals(DataSources.ENTSOE, sources[0])
        assertEquals(DataSources.SPOT_HINTA, sources[1])
    }

    @Test
    fun `defaultsForZone Nordic-only zones return ENTSOE and SPOT_HINTA`() {
        val nordicOnlyZones = listOf("SE1", "SE2", "SE3", "FI",
            "NO1", "NO3", "NO4", "NO5", "EE", "LV", "LT")
        for (zone in nordicOnlyZones) {
            val sources = DataSources.defaultsForZone(zone)
            assertEquals("Expected 2 sources for $zone", 2, sources.size)
            assertEquals("Expected ENTSOE first for $zone", DataSources.ENTSOE, sources[0])
            assertEquals("Expected SPOT_HINTA second for $zone", DataSources.SPOT_HINTA, sources[1])
        }
    }

    @Test
    fun `defaultsForZone overlapping Nordic and Energy-Charts zones return 3 sources`() {
        val overlapZones = listOf("DK1", "DK2", "NO2", "SE4")
        for (zone in overlapZones) {
            val sources = DataSources.defaultsForZone(zone)
            assertEquals("Expected 3 sources for $zone", 3, sources.size)
            assertEquals("Expected ENTSOE first for $zone", DataSources.ENTSOE, sources[0])
            assertEquals("Expected SPOT_HINTA second for $zone", DataSources.SPOT_HINTA, sources[1])
            assertEquals("Expected ENERGY_CHARTS third for $zone", DataSources.ENERGY_CHARTS, sources[2])
        }
    }

    @Test
    fun `defaultsForZone single-source country returns ENTSOE only`() {
        val sources = DataSources.defaultsForZone("BG")
        assertEquals(1, sources.size)
        assertEquals(DataSources.ENTSOE, sources[0])
    }

    @Test
    fun `defaultsForZone unknown zone returns ENTSOE only`() {
        val sources = DataSources.defaultsForZone("UNKNOWN")
        assertEquals(1, sources.size)
        assertEquals(DataSources.ENTSOE, sources[0])
    }

    @Test
    fun `SPOT_HINTA_ZONES contains exactly 15 zones`() {
        assertEquals(15, DataSources.SPOT_HINTA_ZONES.size)
    }

    @Test
    fun `ENERGY_CHARTS_ZONES contains exactly 15 zones`() {
        assertEquals(15, DataSources.ENERGY_CHARTS_ZONES.size)
    }

    @Test
    fun `defaultsForZone AT returns ENTSOE and ENERGY_CHARTS`() {
        val sources = DataSources.defaultsForZone("AT")
        assertEquals(2, sources.size)
        assertEquals(DataSources.ENTSOE, sources[0])
        assertEquals(DataSources.ENERGY_CHARTS, sources[1])
    }

    @Test
    fun `all sources have non-blank display names`() {
        for (source in DataSources.all) {
            assertTrue("Source ${source.id} has blank displayName", source.displayName.isNotBlank())
        }
    }

    @Test
    fun `SPOT_HINTA_ZONES all exist in Countries registry`() {
        for (zoneId in DataSources.SPOT_HINTA_ZONES) {
            assertTrue("SPOT_HINTA_ZONES contains unknown zone: $zoneId", zoneId in allZoneIds)
        }
    }

    @Test
    fun `ENERGY_CHARTS_ZONES all exist in Countries registry`() {
        for (zoneId in DataSources.ENERGY_CHARTS_ZONES) {
            assertTrue("ENERGY_CHARTS_ZONES contains unknown zone: $zoneId", zoneId in allZoneIds)
        }
    }
}
