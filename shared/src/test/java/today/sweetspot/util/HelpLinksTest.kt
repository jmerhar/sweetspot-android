package today.sweetspot.util

import org.junit.Assert.assertEquals
import org.junit.Test

/** Verifies the Help-section link builders, especially localized website URLs. */
class HelpLinksTest {

    @Test
    fun `localizedUrl serves English at the root and asserts the language`() {
        assertEquals("https://sweetspot.today/faq/?lang=en&theme=light", HelpLinks.localizedUrl("faq", ""))
        assertEquals("https://sweetspot.today/faq/?lang=en&theme=light", HelpLinks.localizedUrl("faq", "en"))
        assertEquals("https://sweetspot.today/privacy/?lang=en&theme=light", HelpLinks.localizedUrl("privacy", "en-US"))
    }

    @Test
    fun `localizedUrl prefixes other languages, drops the region, and asserts the language`() {
        assertEquals("https://sweetspot.today/de/faq/?lang=de&theme=light", HelpLinks.localizedUrl("faq", "de"))
        assertEquals("https://sweetspot.today/nl/privacy/?lang=nl&theme=light", HelpLinks.localizedUrl("privacy", "nl"))
        assertEquals("https://sweetspot.today/pt/changelog/?lang=pt&theme=light", HelpLinks.localizedUrl("changelog", "pt-BR"))
    }

    @Test
    fun `localizedUrl uses only the first tag of a comma-joined list`() {
        assertEquals("https://sweetspot.today/de/faq/?lang=de&theme=light", HelpLinks.localizedUrl("faq", "de,en"))
        assertEquals("https://sweetspot.today/faq/?lang=en&theme=light", HelpLinks.localizedUrl("faq", "en-US,de"))
    }

    @Test
    fun `localizedUrl carries the dark theme when requested`() {
        assertEquals("https://sweetspot.today/faq/?lang=en&theme=dark", HelpLinks.localizedUrl("faq", "en", dark = true))
        assertEquals("https://sweetspot.today/de/privacy/?lang=de&theme=dark", HelpLinks.localizedUrl("privacy", "de", dark = true))
    }

    @Test
    fun `play store and issue links`() {
        assertEquals("market://details?id=today.sweetspot", HelpLinks.playStoreUri())
        assertEquals(
            "https://play.google.com/store/apps/details?id=today.sweetspot",
            HelpLinks.playStoreUrl()
        )
        assertEquals("https://github.com/jmerhar/sweetspot-android/issues/7", HelpLinks.issueUrl(7))
    }
}
