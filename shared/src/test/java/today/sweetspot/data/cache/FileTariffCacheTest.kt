package today.sweetspot.data.cache

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests for [FileTariffCache]: raw-JSON round-trip with the fetch timestamp, per-country keying,
 * missing-entry handling, and clearing. Robolectric supplies a real [Context].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class FileTariffCacheTest {

    private lateinit var context: Context
    private lateinit var cache: FileTariffCache

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.cacheDir.listFiles()?.forEach { it.delete() }
        context.getSharedPreferences("sweetspot_cache", Context.MODE_PRIVATE).edit().clear().commit()
        cache = FileTariffCache(context)
    }

    @Test
    fun `write then read round-trips the raw json and timestamp`() {
        cache.write("NL", """{"country":"NL"}""", 123_456L)
        val read = cache.read("NL")
        assertEquals("""{"country":"NL"}""", read!!.raw)
        assertEquals(123_456L, read.fetchedAtMs)
    }

    @Test
    fun `read is case-insensitive on the country code`() {
        cache.write("NL", "x", 1L)
        assertEquals("x", cache.read("nl")!!.raw)
    }

    @Test
    fun `read returns null when nothing is stored`() {
        assertNull(cache.read("NL"))
    }

    @Test
    fun `read returns null when the file exists but the timestamp is missing`() {
        // File present without a timestamp is treated as absent (triggers a re-fetch).
        java.io.File(context.cacheDir, "tariff_nl.json").writeText("x")
        assertNull(cache.read("NL"))
    }

    @Test
    fun `clear removes files and timestamps`() {
        cache.write("NL", "x", 1L)
        cache.write("BE", "y", 2L)
        cache.clear()
        assertNull(cache.read("NL"))
        assertNull(cache.read("BE"))
    }
}
