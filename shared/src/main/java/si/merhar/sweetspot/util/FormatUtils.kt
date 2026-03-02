package si.merhar.sweetspot.util

/**
 * Formats a duration as a human-readable string.
 *
 * @param hours Hours component (0–24).
 * @param minutes Minutes component (0–55).
 * @return Formatted string like "2h", "30m", or "2h 30m".
 *         Returns "0m" when both hours and minutes are zero.
 */
fun formatDuration(hours: Int, minutes: Int): String {
    return when {
        hours == 0 -> "${minutes}m"
        minutes == 0 -> "${hours}h"
        else -> "${hours}h ${minutes}m"
    }
}
