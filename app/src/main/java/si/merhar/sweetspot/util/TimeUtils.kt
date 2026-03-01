package si.merhar.sweetspot.util

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.Duration

val AMSTERDAM: ZoneId = ZoneId.of("Europe/Amsterdam")

fun formatRelative(target: ZonedDateTime, now: ZonedDateTime = ZonedDateTime.now(AMSTERDAM)): String {
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
