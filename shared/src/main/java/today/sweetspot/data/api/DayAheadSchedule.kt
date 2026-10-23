package today.sweetspot.data.api

import today.sweetspot.model.PriceSlot
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

/**
 * When a source is expected to have the next delivery day's day-ahead prices.
 *
 * Every supported market auctions the next day's prices once a day, and the delivery day is a
 * calendar day in Central European Time whatever the bidding zone's own timezone: in Helsinki it
 * runs from 01:00 to 01:00 local time, in Lisbon from 23:00 to 23:00. Both the day boundary and
 * the publication deadline are therefore evaluated in [MARKET_TIME_ZONE], never in the zone's.
 */
object DayAheadSchedule {

    /** Timezone the delivery day and its publication time are defined in. */
    val MARKET_TIME_ZONE: ZoneId = ZoneId.of("Europe/Brussels")

    /**
     * Market time from which a source is expected to have the next delivery day.
     *
     * The auction results are out shortly before 13:00. The extra hour lets every source carry
     * them before one that has only the current day is passed over for another, because a
     * fallback's prices, once chosen, stay cached until about midday the next day: the primary is
     * not asked again in the meantime, even for the rest of the current day.
     */
    val NEXT_DAY_DUE: LocalTime = LocalTime.of(14, 0)

    /**
     * Returns the start of the next delivery day if prices fetched at [now] for a period ending at
     * [to] should already include it.
     *
     * @param now The moment the prices are fetched.
     * @param to End of the requested period (exclusive).
     * @return The next delivery day's start, or `null` when it is not required: before
     *   [NEXT_DAY_DUE], or when the period ends at or before the start of that day.
     */
    fun dueNextDayStart(now: Instant, to: Instant): Instant? {
        val marketNow = now.atZone(MARKET_TIME_ZONE)
        if (marketNow.toLocalTime() < NEXT_DAY_DUE) return null
        val nextDay = marketNow.toLocalDate().plusDays(1).atStartOfDay(MARKET_TIME_ZONE).toInstant()
        return nextDay.takeIf { it < to }
    }

    /**
     * Returns true when [prices] fetched at [now] for a period ending at [to] lack a delivery day
     * that should already be published: [dueNextDayStart] is set and no slot starts on or after it.
     *
     * An empty list lacks it too, so callers that treat an empty response separately check for
     * that first.
     *
     * @param prices The prices a source returned.
     * @param now The moment the prices were fetched.
     * @param to End of the requested period (exclusive).
     * @return True if the next delivery day is due and missing from [prices].
     */
    fun lacksDueNextDay(prices: List<PriceSlot>, now: Instant, to: Instant): Boolean {
        val nextDay = dueNextDayStart(now, to) ?: return false
        return prices.none { !it.time.toInstant().isBefore(nextDay) }
    }
}
