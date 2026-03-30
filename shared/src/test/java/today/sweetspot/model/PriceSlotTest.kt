package today.sweetspot.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Tests for [PriceSlot.overlapsWindow].
 */
class PriceSlotTest {

    private val zone = ZoneId.of("Europe/Amsterdam")
    private val base = ZonedDateTime.of(2026, 3, 30, 12, 0, 0, 0, zone)

    /** Creates a 15-minute slot starting at [base] plus [offsetMinutes]. */
    private fun slot(offsetMinutes: Long, durationMinutes: Int = 15): PriceSlot =
        PriceSlot(base.plusMinutes(offsetMinutes), 0.10, durationMinutes)

    /** Epoch second at [base] plus [offsetMinutes]. */
    private fun epoch(offsetMinutes: Long): Long =
        base.plusMinutes(offsetMinutes).toEpochSecond()

    @Test
    fun `slot fully inside window overlaps`() {
        // Window 12:00–13:00, slot 12:15–12:30
        val s = slot(15)
        assertTrue(s.overlapsWindow(epoch(0), epoch(60)))
    }

    @Test
    fun `slot before window does not overlap`() {
        // Window 12:30–13:00, slot 12:00–12:15
        val s = slot(0)
        assertFalse(s.overlapsWindow(epoch(30), epoch(60)))
    }

    @Test
    fun `slot after window does not overlap`() {
        // Window 12:00–12:30, slot 12:30–12:45
        val s = slot(30)
        assertFalse(s.overlapsWindow(epoch(0), epoch(30)))
    }

    @Test
    fun `slot ending exactly at window start does not overlap`() {
        // Window starts at 12:15, slot is 12:00–12:15
        val s = slot(0)
        assertFalse(s.overlapsWindow(epoch(15), epoch(60)))
    }

    @Test
    fun `slot starting exactly at window end does not overlap`() {
        // Window ends at 12:30, slot is 12:30–12:45
        val s = slot(30)
        assertFalse(s.overlapsWindow(epoch(0), epoch(30)))
    }

    @Test
    fun `slot partially overlapping window start`() {
        // Window 12:10–13:00, slot 12:00–12:15 (overlaps by 5 min)
        val s = slot(0)
        assertTrue(s.overlapsWindow(epoch(10), epoch(60)))
    }

    @Test
    fun `slot partially overlapping window end`() {
        // Window 12:00–12:40, slot 12:30–12:45 (overlaps by 10 min)
        val s = slot(30)
        assertTrue(s.overlapsWindow(epoch(0), epoch(40)))
    }

    @Test
    fun `hourly slot overlaps window`() {
        // Window 12:30–13:30, hourly slot 12:00–13:00
        val s = slot(0, durationMinutes = 60)
        assertTrue(s.overlapsWindow(epoch(30), epoch(90)))
    }
}
