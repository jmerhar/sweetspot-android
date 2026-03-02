package si.merhar.sweetspot.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class TimeUtilsTest {

    private val zone = ZoneId.of("Europe/Amsterdam")

    private fun time(hour: Int, minute: Int = 0, second: Int = 0): ZonedDateTime =
        ZonedDateTime.of(2025, 6, 15, hour, minute, second, 0, zone)

    @Test
    fun `target in past returns now`() {
        assertEquals("now", formatRelative(time(10), time(11)))
    }

    @Test
    fun `target equals now returns now`() {
        val t = time(10)
        assertEquals("now", formatRelative(t, t))
    }

    @Test
    fun `minutes only`() {
        assertEquals("in 30m", formatRelative(time(10, 30), time(10, 0)))
    }

    @Test
    fun `hours only`() {
        assertEquals("in 2h", formatRelative(time(12), time(10)))
    }

    @Test
    fun `hours and minutes`() {
        assertEquals("in 2h 15m", formatRelative(time(12, 15), time(10, 0)))
    }

    @Test
    fun `one minute`() {
        assertEquals("in 1m", formatRelative(time(10, 1), time(10, 0)))
    }

    @Test
    fun `just under one hour`() {
        assertEquals("in 59m", formatRelative(time(10, 59), time(10, 0)))
    }

    @Test
    fun `rounds up to nearest minute`() {
        // 3h 59m 50s should round to 4h 0m, displayed as "in 4h"
        assertEquals("in 4h", formatRelative(time(14, 0, 0), time(10, 0, 10)))
    }

    @Test
    fun `rounds down when under 30 seconds`() {
        // 2h 15m 20s should round to 2h 15m
        assertEquals("in 2h 15m", formatRelative(time(12, 15, 20), time(10, 0, 0)))
    }

    @Test
    fun `seconds under 30 returns now`() {
        // 15 seconds in the future rounds to 0 minutes → "now"
        assertEquals("now", formatRelative(time(10, 0, 15), time(10, 0, 0)))
    }
}
