package si.merhar.sweetspot.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatUtilsTest {

    @Test
    fun `hours only`() {
        assertEquals("3h", formatDuration(3, 0))
    }

    @Test
    fun `minutes only`() {
        assertEquals("30m", formatDuration(0, 30))
    }

    @Test
    fun `hours and minutes`() {
        assertEquals("2h 30m", formatDuration(2, 30))
    }

    @Test
    fun `zero hours and zero minutes`() {
        assertEquals("0m", formatDuration(0, 0))
    }

    @Test
    fun `one hour`() {
        assertEquals("1h", formatDuration(1, 0))
    }

    @Test
    fun `five minutes`() {
        assertEquals("5m", formatDuration(0, 5))
    }

    @Test
    fun `24 hours`() {
        assertEquals("24h", formatDuration(24, 0))
    }

    @Test
    fun `1h 5m`() {
        assertEquals("1h 5m", formatDuration(1, 5))
    }
}
