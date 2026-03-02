package si.merhar.sweetspot.util

import java.time.ZonedDateTime
import java.time.Duration

/**
 * Formats the time difference between [now] and [target] as a human-readable relative string.
 *
 * @param target The future point in time.
 * @param now The reference "current" time.
 * @return A string like "in 2h 30m", "in 45m", or "now" if the target is in the past.
 */
fun formatRelative(target: ZonedDateTime, now: ZonedDateTime): String {
    val seconds = Duration.between(now, target).seconds
    if (seconds <= 0) return "now"
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    return when {
        h > 0 && m > 0 -> "in ${h}h ${m}m"
        h > 0 -> "in ${h}h"
        else -> "in ${m}m"
    }
}
