package today.sweetspot.data.share

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import today.sweetspot.model.Appliance
import today.sweetspot.model.ApplianceSort
import today.sweetspot.model.EvPosition
import today.sweetspot.model.EvSpec
import today.sweetspot.model.SharedSetup
import today.sweetspot.model.SortCriterion
import today.sweetspot.model.SortKey

/**
 * Tests for [SetupShare] encoding, link building, and decoding.
 */
class SetupShareTest {

    private val setup = SharedSetup(
        appliances = listOf(
            Appliance("a", "Washing machine", 2, 30, icon = "washing_machine", powerKw = 2.0),
            Appliance("b", "Car", ev = EvSpec(batteryKwh = 60.0, acMaxPowerKw = 11.0)),
        ),
        sort = ApplianceSort(listOf(SortCriterion(SortKey.NAME), SortCriterion(SortKey.DURATION, true))),
        evHomeChargerKw = 7.4,
        evDefaultTargetSoc = 90,
        evPosition = EvPosition.LAST.key,
        evSeparate = true,
    )

    @Test
    fun `round-trips a full setup through encode-decode`() {
        val result = SetupShare.decode(SetupShare.encode(setup))
        assertEquals(DecodeResult.Success(setup), result)
    }

    @Test
    fun `toLink builds an import URL with the payload in the fragment`() {
        val link = SetupShare.toLink(setup)
        assertTrue(link.startsWith("${SetupShare.IMPORT_BASE}#"))
        val fragment = link.substringAfter('#')
        assertEquals(DecodeResult.Success(setup), SetupShare.fromLink(fragment))
    }

    @Test
    fun `fromLink returns Malformed for a null or blank fragment`() {
        assertEquals(DecodeResult.Malformed, SetupShare.fromLink(null))
        assertEquals(DecodeResult.Malformed, SetupShare.fromLink(""))
        assertEquals(DecodeResult.Malformed, SetupShare.fromLink("   "))
    }

    @Test
    fun `decode returns Malformed for garbage input`() {
        assertEquals(DecodeResult.Malformed, SetupShare.decode("not-a-real-payload!!!"))
    }

    @Test
    fun `decode reports a newer schema as TooNew`() {
        // Encode a setup that claims a schema one higher than we support.
        val future = SetupShare.encode(setup.copy(schemaVersion = SetupShare.CURRENT_SCHEMA + 1))
        assertEquals(DecodeResult.TooNew(SetupShare.CURRENT_SCHEMA + 1), SetupShare.decode(future))
    }

    @Test
    fun `decode tolerates unknown JSON keys`() {
        // A payload from a future minor version with an extra field still decodes on this schema.
        val json = """{"schemaVersion":1,"appliances":[],"unknownField":42}"""
        val gzipped = java.io.ByteArrayOutputStream().also { out ->
            java.util.zip.GZIPOutputStream(out).use { it.write(json.toByteArray(Charsets.UTF_8)) }
        }.toByteArray()
        val payload = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(gzipped)
        val result = SetupShare.decode(payload)
        assertTrue(result is DecodeResult.Success)
        assertTrue((result as DecodeResult.Success).setup.appliances.isEmpty())
    }
}
