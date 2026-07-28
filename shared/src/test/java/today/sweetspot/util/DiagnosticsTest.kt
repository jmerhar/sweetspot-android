package today.sweetspot.util

import org.junit.Assert.assertTrue
import org.junit.Test

/** Verifies the no-PII diagnostics block used in bug reports. */
class DiagnosticsTest {

    @Test
    fun `build includes app, OS, device, language, zone and source`() {
        val d = Diagnostics.build(
            appVersion = "6.6",
            versionCode = 37,
            androidRelease = "14",
            sdkInt = 34,
            device = "Pixel 8",
            languageTag = "de",
            zoneId = "NL",
            source = "ENTSO-E"
        )
        assertTrue(d.contains("App: 6.6 (37)"))
        assertTrue(d.contains("Android: 14 (API 34)"))
        assertTrue(d.contains("Device: Pixel 8"))
        assertTrue(d.contains("Language: de"))
        assertTrue(d.contains("Zone: NL"))
        assertTrue(d.contains("Source: ENTSO-E"))
    }

    @Test
    fun `build falls back for blank language and null zone or source`() {
        val d = Diagnostics.build("1.0", 1, "13", 33, "Device", "", null, null)
        assertTrue(d.contains("Language: system"))
        assertTrue(d.contains("Zone: -"))
        assertTrue(d.contains("Source: -"))
    }
}
