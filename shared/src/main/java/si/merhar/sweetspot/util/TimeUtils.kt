package si.merhar.sweetspot.util

import android.content.res.Resources
import si.merhar.sweetspot.shared.R
import java.time.ZonedDateTime
import java.time.Duration

/**
 * Formats the time difference between [now] and [target] as a human-readable relative string.
 *
 * Rounds to the nearest minute to avoid misleading truncation (e.g. 3h 59m 50s → "in 4h").
 * When [resources] is provided, uses localized string resources. When `null`, falls back
 * to English formatting so existing tests work without an Android context.
 *
 * @param target The future point in time.
 * @param now The reference "current" time.
 * @param resources Optional Android resources for localized formatting.
 * @return A string like "in 2h 30m", "in 45m", or "now" if the target is in the past.
 */
fun formatRelative(target: ZonedDateTime, now: ZonedDateTime, resources: Resources? = null): String {
    val seconds = Duration.between(now, target).seconds
    if (seconds <= 0) return resources?.getString(R.string.relative_now) ?: "now"
    val totalMinutes = (seconds + 30) / 60
    if (totalMinutes <= 0) return resources?.getString(R.string.relative_now) ?: "now"
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    return when {
        h > 0 && m > 0 -> resources?.getQuantityString(R.plurals.relative_in_hours_minutes, h.toInt(), h.toInt(), m.toInt()) ?: "in ${h}h ${m}m"
        h > 0 -> resources?.getQuantityString(R.plurals.relative_in_hours, h.toInt(), h.toInt()) ?: "in ${h}h"
        else -> resources?.getString(R.string.relative_in_minutes, m.toInt()) ?: "in ${m}m"
    }
}
