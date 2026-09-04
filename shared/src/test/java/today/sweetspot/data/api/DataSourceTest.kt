package today.sweetspot.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import today.sweetspot.model.Countries

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
    fun `defaultsForZone NL returns ENTSOE, ENERGY_CHARTS, and ENERGY_ZERO`() {
        val sources = DataSources.defaultsForZone("NL")
        assertEquals(3, sources.size)
        assertEquals(DataSources.ENTSOE, sources[0])
        assertEquals(DataSources.ENERGY_CHARTS, sources[1])
        assertEquals(DataSources.ENERGY_ZERO, sources[2])
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
        val sources = DataSources.defaultsForZone("MK")
        assertEquals(1, sources.size)
        assertEquals(DataSources.ENTSOE, sources[0])
    }

    @Test
    fun `defaultsForZone Energy-Charts-only zones return ENTSOE and ENERGY_CHARTS`() {
        val zones = listOf(
            "BG", "ES", "GR", "HR", "PT", "RO", "RS", "SK",
            "IT_CALA", "IT_CNOR", "IT_CSUD", "IT_NORD", "IT_SARD", "IT_SICI", "IT_SUD",
            "ME", "BE", "CH", "CZ", "FR", "HU", "PL", "SI",
        )
        for (zone in zones) {
            val sources = DataSources.defaultsForZone(zone)
            assertEquals("Expected 2 sources for $zone", 2, sources.size)
            assertEquals("Expected ENTSOE first for $zone", DataSources.ENTSOE, sources[0])
            assertEquals(
                "Expected ENERGY_CHARTS second for $zone",
                DataSources.ENERGY_CHARTS,
                sources[1]
            )
        }
    }

    /**
     * Pins the set of zones with no fallback source.
     *
     * A zone served only by ENTSO-E has no path to prices while ENTSO-E is
     * unavailable, so the app is fully broken there for the duration of any
     * outage. Asserting the exact set means adding a country, or extending a
     * fallback's coverage, forces a deliberate decision about that rather than
     * silently growing the list.
     */
    @Test
    fun `only MK and IE_SEM lack a fallback source`() {
        val withoutFallback = allZoneIds
            .filter { DataSources.defaultsForZone(it).size == 1 }
            .toSet()
        assertEquals(setOf("MK", "IE_SEM"), withoutFallback)
    }

    @Test
    fun `defaultsForZone unknown zone returns ENTSOE only`() {
        val sources = DataSources.defaultsForZone("UNKNOWN")
        assertEquals(1, sources.size)
        assertEquals(DataSources.ENTSOE, sources[0])
    }

    @Test
    fun `defaultsForZone AT returns ENTSOE, ENERGY_CHARTS, and AWATTAR`() {
        val sources = DataSources.defaultsForZone("AT")
        assertEquals(3, sources.size)
        assertEquals(DataSources.ENTSOE, sources[0])
        assertEquals(DataSources.ENERGY_CHARTS, sources[1])
        assertEquals(DataSources.AWATTAR, sources[2])
    }

    @Test
    fun `defaultsForZone DE_LU returns ENTSOE, ENERGY_CHARTS, and AWATTAR`() {
        val sources = DataSources.defaultsForZone("DE_LU")
        assertEquals(3, sources.size)
        assertEquals(DataSources.ENTSOE, sources[0])
        assertEquals(DataSources.ENERGY_CHARTS, sources[1])
        assertEquals(DataSources.AWATTAR, sources[2])
    }

    @Test
    fun `all sources have non-blank display names`() {
        for (source in DataSources.all) {
            assertTrue("Source ${source.id} has blank displayName", source.displayName.isNotBlank())
        }
    }

    @Test
    fun `all source zone IDs exist in Countries registry`() {
        val apiZoneSets = mapOf(
            "SpotHintaApi.ZONES" to SpotHintaApi.ZONES,
            "EnergyChartsApi.ZONE_TO_BZN" to EnergyChartsApi.ZONE_TO_BZN.keys,
            "AwattarApi.ZONE_TO_BASE_URL" to AwattarApi.ZONE_TO_BASE_URL.keys,
        )
        for ((label, zones) in apiZoneSets) {
            for (zoneId in zones) {
                assertTrue("$label contains unknown zone: $zoneId", zoneId in allZoneIds)
            }
        }
    }

    @Test
    fun `source zone counts match expected values`() {
        assertEquals(15, SpotHintaApi.ZONES.size)
        assertEquals(30, EnergyChartsApi.ZONE_TO_BZN.size)
        assertEquals(2, AwattarApi.ZONE_TO_BASE_URL.size)
    }
}
