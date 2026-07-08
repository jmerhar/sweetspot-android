package today.sweetspot.util

import android.content.Context
import android.content.res.Resources
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Robolectric coverage of the *localised* (non-null [Resources]) branches of [formatDuration] and
 * [formatRelative]. The pure [FormatUtilsTest]/[TimeUtilsTest] exercise the English fallback
 * (`resources == null`); these cover the resource-backed side of the same conditionals.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ResourceFormattingTest {

    private lateinit var res: Resources
    private val base: ZonedDateTime = ZonedDateTime.of(2026, 1, 1, 12, 0, 0, 0, ZoneId.of("UTC"))

    @Before
    fun setUp() {
        res = ApplicationProvider.getApplicationContext<Context>().resources
    }

    // --- formatDuration (all three branches) ---

    @Test
    fun `formatDuration minutes only`() {
        assertEquals("30m", formatDuration(0, 30, res))
    }

    @Test
    fun `formatDuration hours only`() {
        assertEquals("2h", formatDuration(2, 0, res))
    }

    @Test
    fun `formatDuration hours and minutes`() {
        assertEquals("2h 30m", formatDuration(2, 30, res))
    }

    // --- formatRelative (all branches) ---

    @Test
    fun `formatRelative past target reads now`() {
        assertEquals("now", formatRelative(base.minusMinutes(5), base, res))
    }

    @Test
    fun `formatRelative sub-minute rounds down to now`() {
        assertEquals("now", formatRelative(base.plusSeconds(1), base, res))
    }

    @Test
    fun `formatRelative minutes only`() {
        assertEquals("in 45m", formatRelative(base.plusMinutes(45), base, res))
    }

    @Test
    fun `formatRelative hours only`() {
        assertEquals("in 2h", formatRelative(base.plusHours(2), base, res))
    }

    @Test
    fun `formatRelative hours and minutes`() {
        assertEquals("in 2h 30m", formatRelative(base.plusHours(2).plusMinutes(30), base, res))
    }
}
