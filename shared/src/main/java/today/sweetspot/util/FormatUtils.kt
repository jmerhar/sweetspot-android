package today.sweetspot.util

import android.content.res.Resources
import today.sweetspot.shared.R
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale

/** 24-hour short time formatter (e.g. "14:00"). All 30 supported countries use 24h format. */
val shortTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/**
 * Formats a kW value, dropping the decimal for whole numbers (11.0 → "11", 7.4 → "7.4").
 *
 * Uses the device locale for the decimal separator.
 *
 * @param value Power in kW.
 * @return Formatted kW string without a unit suffix.
 */
fun formatKw(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString()
    else String.format(Locale.getDefault(), "%.1f", value)

/**
 * Formats an hour/minute pair as a 24-hour "HH:mm" label (e.g. 7, 5 → "07:05").
 *
 * @param hour Hour of day (0–23).
 * @param minute Minute (0–59).
 * @return Zero-padded "HH:mm" string.
 */
fun formatHhMm(hour: Int, minute: Int): String =
    String.format(Locale.getDefault(), "%02d:%02d", hour, minute)

/**
 * Formats a EUR price using the device locale's currency conventions.
 *
 * Handles symbol placement (before/after), decimal separator, thousands separator,
 * and spacing automatically per locale. For example, `formatPrice(0.0877, 4)` produces
 * `"€ 0,0877"` in Dutch but `"0,0877 €"` in German.
 *
 * @param price Price in EUR.
 * @param decimals Number of decimal places to display.
 * @return Locale-formatted currency string.
 */
fun formatPrice(price: Double, decimals: Int): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
        currency = Currency.getInstance("EUR")
        minimumFractionDigits = decimals
        maximumFractionDigits = decimals
    }
    return formatter.format(price)
}

/**
 * Formats a duration as a human-readable string.
 *
 * When [resources] is provided, uses localised string resources. When `null`, falls back
 * to English formatting so existing tests work without an Android context.
 *
 * @param hours Hours component (0–24).
 * @param minutes Minutes component (0–55).
 * @param resources Optional Android resources for localised formatting.
 * @return Formatted string like "2h", "30m", or "2h 30m".
 *         Returns "0m" when both hours and minutes are zero.
 */
fun formatDuration(hours: Int, minutes: Int, resources: Resources? = null): String {
    return when {
        hours == 0 -> resources?.getString(R.string.duration_minutes_only, minutes) ?: "${minutes}m"
        minutes == 0 -> resources?.getQuantityString(R.plurals.duration_hours_only, hours, hours) ?: "${hours}h"
        else -> resources?.getQuantityString(R.plurals.duration_hours_minutes, hours, hours, minutes) ?: "${hours}h ${minutes}m"
    }
}
