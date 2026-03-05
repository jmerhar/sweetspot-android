package si.merhar.sweetspot.util

import android.content.res.Resources
import si.merhar.sweetspot.shared.R
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Locale-appropriate short time formatter (e.g. "14:00" or "2:00 PM"). */
val shortTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

/**
 * Formats a duration as a human-readable string.
 *
 * When [resources] is provided, uses localized string resources. When `null`, falls back
 * to English formatting so existing tests work without an Android context.
 *
 * @param hours Hours component (0–24).
 * @param minutes Minutes component (0–55).
 * @param resources Optional Android resources for localized formatting.
 * @return Formatted string like "2h", "30m", or "2h 30m".
 *         Returns "0m" when both hours and minutes are zero.
 */
fun formatDuration(hours: Int, minutes: Int, resources: Resources? = null): String {
    return when {
        hours == 0 -> resources?.getString(R.string.duration_minutes_only, minutes) ?: "${minutes}m"
        minutes == 0 -> resources?.getString(R.string.duration_hours_only, hours) ?: "${hours}h"
        else -> resources?.getString(R.string.duration_hours_minutes, hours, minutes) ?: "${hours}h ${minutes}m"
    }
}
