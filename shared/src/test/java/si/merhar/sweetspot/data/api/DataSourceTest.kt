package si.merhar.sweetspot.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for [DataSource] and [DataSources] registry.
 */
class DataSourceTest {

    @Test
    fun `all source IDs are unique`() {
        val ids = DataSources.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `defaultsForZone NL returns ENTSOE and ENERGY_ZERO`() {
        val sources = DataSources.defaultsForZone("NL")
        assertEquals(2, sources.size)
        assertEquals(DataSources.ENTSOE, sources[0])
        assertEquals(DataSources.ENERGY_ZERO, sources[1])
    }

    @Test
    fun `defaultsForZone FI returns ENTSOE and SPOT_HINTA`() {
        val sources = DataSources.defaultsForZone("FI")
        assertEquals(2, sources.size)
        assertEquals(DataSources.ENTSOE, sources[0])
        assertEquals(DataSources.SPOT_HINTA, sources[1])
    }

    @Test
    fun `defaultsForZone Nordic zones return ENTSOE and SPOT_HINTA`() {
        val nordicZones = listOf("SE1", "SE2", "SE3", "SE4", "DK1", "DK2",
            "NO1", "NO2", "NO3", "NO4", "NO5", "EE", "LV", "LT")
        for (zone in nordicZones) {
            val sources = DataSources.defaultsForZone(zone)
            assertEquals("Expected 2 sources for $zone", 2, sources.size)
            assertEquals("Expected ENTSOE first for $zone", DataSources.ENTSOE, sources[0])
            assertEquals("Expected SPOT_HINTA second for $zone", DataSources.SPOT_HINTA, sources[1])
        }
    }

    @Test
    fun `defaultsForZone single-source country returns ENTSOE only`() {
        val sources = DataSources.defaultsForZone("FR")
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
    fun `all sources have non-blank display names`() {
        for (source in DataSources.all) {
            assertTrue("Source ${source.id} has blank displayName", source.displayName.isNotBlank())
        }
    }
}
