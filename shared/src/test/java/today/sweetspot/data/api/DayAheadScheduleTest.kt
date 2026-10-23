package today.sweetspot.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import today.sweetspot.model.PriceSlot
import java.time.Instant
import java.time.ZoneId

class DayAheadScheduleTest {

    private val amsterdam = ZoneId.of("Europe/Amsterdam")

    /** End of a request made on 23 Oct 2026 in Amsterdam: the start of 25 Oct, local time. */
    private val to = Instant.parse("2026-10-24T22:00:00Z")

    /** Start of the 24 Oct 2026 delivery day: midnight CEST. */
    private val nextDay = Instant.parse("2026-10-23T22:00:00Z")

    /** 18:00 CEST on 23 Oct, after the next day is due. */
    private val evening = Instant.parse("2026-10-23T16:00:00Z")

    /** 11:00 CEST on 23 Oct, before the next day is due. */
    private val morning = Instant.parse("2026-10-23T09:00:00Z")

    /** [count] consecutive [minutes]-long slots from [start], presented in [zone]. */
    private fun slots(start: String, count: Int, minutes: Int = 15, zone: ZoneId = amsterdam): List<PriceSlot> {
        val first = Instant.parse(start)
        return (0 until count).map { i ->
            PriceSlot(first.plusSeconds(i * minutes * 60L).atZone(zone), 0.10, minutes)
        }
    }

    /** The 23 Oct 2026 delivery day in quarter-hours, presented in [zone]. */
    private fun currentDay(zone: ZoneId = amsterdam) = slots("2026-10-22T22:00:00Z", 96, zone = zone)

    /** The 24 Oct 2026 delivery day in quarter-hours, presented in [zone]. */
    private fun followingDay(zone: ZoneId = amsterdam) = slots("2026-10-23T22:00:00Z", 96, zone = zone)

    @Test
    fun `the next day is not due before the due time`() {
        assertNull(DayAheadSchedule.dueNextDayStart(Instant.parse("2026-10-23T11:59:59Z"), to))
    }

    @Test
    fun `the next day is due from the due time`() {
        assertEquals(nextDay, DayAheadSchedule.dueNextDayStart(Instant.parse("2026-10-23T12:00:00Z"), to))
    }

    @Test
    fun `the due time follows CET in winter`() {
        val winterTo = Instant.parse("2026-12-02T23:00:00Z")
        assertNull(DayAheadSchedule.dueNextDayStart(Instant.parse("2026-12-01T12:59:59Z"), winterTo))
        assertEquals(
            Instant.parse("2026-12-01T23:00:00Z"),
            DayAheadSchedule.dueNextDayStart(Instant.parse("2026-12-01T13:00:00Z"), winterTo)
        )
    }

    @Test
    fun `after midnight the following day is not due until the afternoon`() {
        // 00:30 CEST on 24 Oct: that day's prices are in, and the 25 Oct prices are not yet due.
        val laterTo = Instant.parse("2026-10-25T23:00:00Z")
        assertNull(DayAheadSchedule.dueNextDayStart(Instant.parse("2026-10-23T22:30:00Z"), laterTo))
    }

    @Test
    fun `a period ending where the next day starts does not require it`() {
        assertNull(DayAheadSchedule.dueNextDayStart(evening, nextDay))
    }

    @Test
    fun `a period ending just after the next day starts requires it`() {
        assertEquals(nextDay, DayAheadSchedule.dueNextDayStart(evening, nextDay.plusSeconds(60)))
    }

    @Test
    fun `the next day starts at market midnight across the autumn clock change`() {
        val laterTo = Instant.parse("2026-10-27T00:00:00Z")
        // Saturday 24 Oct, 15:00 CEST: Sunday starts at 00:00 CEST.
        assertEquals(
            Instant.parse("2026-10-24T22:00:00Z"),
            DayAheadSchedule.dueNextDayStart(Instant.parse("2026-10-24T13:00:00Z"), laterTo)
        )
        // Sunday 25 Oct, 15:00 CET: Monday starts at 00:00 CET, an hour later in UTC.
        assertEquals(
            Instant.parse("2026-10-25T23:00:00Z"),
            DayAheadSchedule.dueNextDayStart(Instant.parse("2026-10-25T14:00:00Z"), laterTo)
        )
    }

    @Test
    fun `the next day starts at market midnight across the spring clock change`() {
        val laterTo = Instant.parse("2026-03-31T00:00:00Z")
        // Saturday 28 Mar, 15:00 CET: Sunday starts at 00:00 CET.
        assertEquals(
            Instant.parse("2026-03-28T23:00:00Z"),
            DayAheadSchedule.dueNextDayStart(Instant.parse("2026-03-28T14:00:00Z"), laterTo)
        )
        // Sunday 29 Mar, 15:00 CEST: Monday starts at 00:00 CEST.
        assertEquals(
            Instant.parse("2026-03-29T22:00:00Z"),
            DayAheadSchedule.dueNextDayStart(Instant.parse("2026-03-29T13:00:00Z"), laterTo)
        )
    }

    @Test
    fun `current-day prices lack the next day once it is due`() {
        assertTrue(DayAheadSchedule.lacksDueNextDay(currentDay(), evening, to))
    }

    @Test
    fun `current-day prices are enough before the next day is due`() {
        assertFalse(DayAheadSchedule.lacksDueNextDay(currentDay(), morning, to))
    }

    @Test
    fun `prices that include the next day do not lack it`() {
        assertFalse(DayAheadSchedule.lacksDueNextDay(currentDay() + followingDay(), evening, to))
    }

    @Test
    fun `hourly prices for the next day count`() {
        assertFalse(DayAheadSchedule.lacksDueNextDay(slots("2026-10-22T22:00:00Z", 48, minutes = 60), evening, to))
    }

    @Test
    fun `in Helsinki the current delivery day running past local midnight is not the next day`() {
        val helsinki = ZoneId.of("Europe/Helsinki")
        val helsinkiTo = Instant.parse("2026-10-24T21:00:00Z")
        // The 23 Oct delivery day ends at 01:00 on 24 Oct in Helsinki, so a test against local
        // midnight would mistake its last hour for the next day.
        assertTrue(DayAheadSchedule.lacksDueNextDay(currentDay(helsinki), evening, helsinkiTo))
        assertFalse(
            DayAheadSchedule.lacksDueNextDay(currentDay(helsinki) + followingDay(helsinki), evening, helsinkiTo)
        )
    }

    @Test
    fun `in Lisbon the next delivery day starts before local midnight`() {
        val lisbon = ZoneId.of("Europe/Lisbon")
        val lisbonTo = Instant.parse("2026-10-24T23:00:00Z")
        // The first hour of the 24 Oct delivery day is 23:00 to 24:00 on 23 Oct in Lisbon.
        val firstHour = slots("2026-10-23T22:00:00Z", 4, zone = lisbon)
        assertTrue(DayAheadSchedule.lacksDueNextDay(currentDay(lisbon), evening, lisbonTo))
        assertFalse(DayAheadSchedule.lacksDueNextDay(currentDay(lisbon) + firstHour, evening, lisbonTo))
    }

    @Test
    fun `a slot starting exactly at the next day counts and one ending there does not`() {
        assertFalse(DayAheadSchedule.lacksDueNextDay(slots("2026-10-23T22:00:00Z", 1), evening, to))
        assertTrue(DayAheadSchedule.lacksDueNextDay(slots("2026-10-23T21:45:00Z", 1), evening, to))
    }

    @Test
    fun `empty prices lack the next day only once it is due`() {
        assertTrue(DayAheadSchedule.lacksDueNextDay(emptyList(), evening, to))
        assertFalse(DayAheadSchedule.lacksDueNextDay(emptyList(), morning, to))
    }
}
