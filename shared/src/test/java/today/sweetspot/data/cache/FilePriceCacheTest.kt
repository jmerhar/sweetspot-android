package today.sweetspot.data.cache

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Tests for [FilePriceCache]: the v3 binary format round-trip, graceful migration/corruption
 * handling (returns `null`), per-zone and global clearing, and the fetch cooldown. Robolectric
 * supplies a real [Context] (cache dir + SharedPreferences).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class FilePriceCacheTest {

    private lateinit var context: Context
    private lateinit var cache: FilePriceCache

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.cacheDir.listFiles()?.forEach { it.delete() }
        context.getSharedPreferences("sweetspot_cache", Context.MODE_PRIVATE).edit().clear().commit()
        cache = FilePriceCache(context)
    }

    /** Builds cache data with one entry per slot duration (exercises the Short durationMinutes field). */
    private fun data(vararg durationMinutes: Int) = CachedPriceData(
        source = "ENTSO-E",
        prices = durationMinutes.mapIndexed { i, m ->
            CachedPrice(epochSecond = 1_700_000_000L + i * 3600L, durationMinutes = m, price = 0.1 * (i + 1))
        }
    )

    private fun file(key: String) = File(context.cacheDir, "prices_$key.bin")

    @Test
    fun `write then read round-trips source and prices`() {
        val d = data(60, 15, 15)
        cache.write("NL", d)
        val read = cache.readCached("NL")
        assertNotNull(read)
        assertEquals("ENTSO-E", read!!.source)
        assertEquals(d.prices, read.prices)
    }

    @Test
    fun `reading an unknown zone returns null`() {
        assertNull(cache.readCached("XX"))
    }

    @Test
    fun `an older format version is migrated by returning null`() {
        // A v2 file: version byte 2 (!= current 3) → treated as absent so a re-fetch occurs.
        file("NL").writeBytes(byteArrayOf(2, 0, 0, 0, 0))
        assertNull(cache.readCached("NL"))
    }

    @Test
    fun `a corrupt file returns null`() {
        // Correct version byte, then garbage where the source UTF length/bytes should be.
        file("NL").writeBytes(byteArrayOf(3, 99, 12, 7))
        assertNull(cache.readCached("NL"))
    }

    @Test
    fun `clearForZone removes only the targeted zone`() {
        cache.write("NL", data(60))
        cache.write("DE_LU", data(60))
        cache.clearForZone("NL")
        assertNull(cache.readCached("NL"))
        assertNotNull(cache.readCached("DE_LU"))
    }

    @Test
    fun `clear removes all cached zones`() {
        cache.write("NL", data(60))
        cache.write("DE_LU", data(60))
        cache.clear()
        assertNull(cache.readCached("NL"))
        assertNull(cache.readCached("DE_LU"))
    }

    @Test
    fun `writing starts the cooldown`() {
        cache.write("NL", data(60))
        assertFalse(cache.isCooldownElapsed(60_000L))
        assertTrue(cache.cooldownRemainingMs(60_000L) > 0L)
    }

    @Test
    fun `resetting the cooldown makes it elapsed again`() {
        cache.write("NL", data(60))
        cache.resetCooldown()
        assertTrue(cache.isCooldownElapsed(60_000L))
        assertEquals(0L, cache.cooldownRemainingMs(60_000L))
    }

    @Test
    fun `cooldown is elapsed when nothing has been fetched yet`() {
        assertTrue(cache.isCooldownElapsed(60_000L))
        assertEquals(0L, cache.cooldownRemainingMs(60_000L))
    }
}
