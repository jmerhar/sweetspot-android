package today.sweetspot.data.repository

import android.content.Context
import android.telephony.TelephonyManager
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.util.Locale
import java.util.TimeZone

/**
 * Tests for [CountryDetector.detect]'s zero-permission fallback chain:
 * SIM → network → timezone → locale → NL. Robolectric shadows the [TelephonyManager] for the
 * SIM/network cases; [TimeZone]/[Locale] defaults drive the rest (restored after each test).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CountryDetectorTest {

    private lateinit var context: Context
    private lateinit var telephony: TelephonyManager
    private val originalTz = TimeZone.getDefault()
    private val originalLocale = Locale.getDefault()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(originalTz)
        Locale.setDefault(originalLocale)
    }

    @Test
    fun `SIM country takes priority`() {
        shadowOf(telephony).setSimCountryIso("de")
        // Timezone/locale point elsewhere; SIM must win.
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Stockholm"))
        assertEquals("DE", CountryDetector.detect(context).code)
    }

    @Test
    fun `network country is used when SIM is absent`() {
        shadowOf(telephony).setNetworkCountryIso("fr")
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Stockholm"))
        assertEquals("FR", CountryDetector.detect(context).code)
    }

    @Test
    fun `timezone maps to country when SIM and network are absent`() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Stockholm"))
        assertEquals("SE", CountryDetector.detect(context).code)
    }

    @Test
    fun `locale is used when the timezone is unmapped`() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York")) // not in the map
        Locale.setDefault(Locale.forLanguageTag("sv-SE"))
        assertEquals("SE", CountryDetector.detect(context).code)
    }

    @Test
    fun `falls back to the Netherlands when nothing resolves to a supported country`() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))
        Locale.setDefault(Locale.forLanguageTag("en-US")) // unsupported → fall through
        assertEquals("NL", CountryDetector.detect(context).code)
    }

    @Test
    fun `an unsupported SIM country falls through to the next signal`() {
        shadowOf(telephony).setSimCountryIso("us") // unsupported → ignored
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"))
        assertEquals("DE", CountryDetector.detect(context).code)
    }

    @Test
    fun `a locale without a country region falls through to the fallback`() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York")) // unmapped
        Locale.setDefault(Locale.forLanguageTag("sv")) // language only, no region → empty country
        assertEquals("NL", CountryDetector.detect(context).code)
    }

    @Test
    fun `a device without telephony skips SIM and network and uses the timezone`() {
        // A context with no TelephonyManager (tm == null) must skip the SIM/network signals
        // and fall through to the timezone mapping.
        val noTelephony = object : android.content.ContextWrapper(context) {
            override fun getSystemService(name: String): Any? =
                if (name == Context.TELEPHONY_SERVICE) null else super.getSystemService(name)
        }
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"))
        assertEquals("DE", CountryDetector.detect(noTelephony).code)
    }

    @Test
    fun `an empty SIM country falls through to network`() {
        // Empty (not null) SIM ISO → ifEmpty maps it to null → the network signal is consulted.
        shadowOf(telephony).setSimCountryIso("")
        shadowOf(telephony).setNetworkCountryIso("fr")
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Stockholm"))
        assertEquals("FR", CountryDetector.detect(context).code)
    }
}
