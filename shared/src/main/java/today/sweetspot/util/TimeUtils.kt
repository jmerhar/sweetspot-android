package today.sweetspot.util

import android.content.res.Resources
import today.sweetspot.shared.R
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.Duration

/**
 * Formats the time difference between [now] and [target] as a human-readable relative string.
 *
 * Rounds to the nearest minute to avoid misleading truncation (e.g. 3h 59m 50s → "in 4h").
 * When [resources] is provided, uses localised string resources. When `null`, falls back
 * to English formatting so existing tests work without an Android context.
 *
 * @param target The future point in time.
 * @param now The reference "current" time.
 * @param resources Optional Android resources for localised formatting.
 * @return A string like "in 2h 30m", "in 45m", or "now" if the target is in the past.
 */
fun formatRelative(target: ZonedDateTime, now: ZonedDateTime, resources: Resources? = null): String {
    val seconds = Duration.between(now, target).seconds
    val totalMinutes = if (seconds > 0) (seconds + 30) / 60 else 0L
    val h = (totalMinutes / 60).toInt()
    val m = (totalMinutes % 60).toInt()
    // Branch on the localisation mode once, rather than per line: `resources?.getX(...) ?: english`
    // on every arm would add an unreachable "resources non-null but getX returned null" branch to
    // each line, which can never be covered. This keeps the English fallback for pure (non-Robolectric)
    // tests while leaving only real, coverable branches.
    return if (resources != null) {
        when {
            totalMinutes <= 0 -> resources.getString(R.string.relative_now)
            h > 0 && m > 0 -> resources.getQuantityString(R.plurals.relative_in_hours_minutes, h, h, m)
            h > 0 -> resources.getQuantityString(R.plurals.relative_in_hours, h, h)
            else -> resources.getString(R.string.relative_in_minutes, m)
        }
    } else {
        when {
            totalMinutes <= 0 -> "now"
            h > 0 && m > 0 -> "in ${h}h ${m}m"
            h > 0 -> "in ${h}h"
            else -> "in ${m}m"
        }
    }
}

/**
 * Combines a date picked as UTC-midnight epoch millis (as Material's date picker reports it) with a
 * wall-clock [hour]/[minute] interpreted in [timeZoneId], returning the resulting instant as epoch
 * millis. Used by the developer time-override so the chosen local date and time map to one instant.
 *
 * @param pickedDateUtcMidnightMs Selected date at 00:00 UTC, in epoch millis.
 * @param hour Wall-clock hour (0–23) in [timeZoneId].
 * @param minute Wall-clock minute (0–59) in [timeZoneId].
 * @param timeZoneId Timezone the wall-clock time is expressed in.
 * @return The combined instant as epoch millis.
 */
fun dateTimeOverrideMillis(pickedDateUtcMidnightMs: Long, hour: Int, minute: Int, timeZoneId: ZoneId): Long {
    val date = Instant.ofEpochMilli(pickedDateUtcMidnightMs).atZone(ZoneId.of("UTC")).toLocalDate()
    return date.atTime(hour, minute).atZone(timeZoneId).toInstant().toEpochMilli()
}
