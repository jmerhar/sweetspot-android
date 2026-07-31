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
 * Parses separator-tolerant decimal input (accepting a comma or a dot) into a [Double], or `null`
 * when it isn't a complete number. Used by numeric text fields such as the all-in surcharge, where a
 * `null` result means "leave the stored value unchanged" so partial input (e.g. `"-"`) isn't lost.
 *
 * @param text Raw field text.
 * @return The parsed value, or `null` if [text] is not a valid number.
 */
fun parseDecimalInput(text: String): Double? = text.replace(',', '.').toDoubleOrNull()

/**
 * Formats a price using the device locale's currency conventions.
 *
 * Handles symbol placement (before/after), decimal separator, thousands separator,
 * and spacing automatically per locale. For example, `formatPrice(0.0877, 4)` produces
 * `"€ 0,0877"` in Dutch but `"0,0877 €"` in German.
 *
 * @param price Price amount.
 * @param decimals Number of decimal places to display.
 * @param currencyCode ISO 4217 currency code (defaults to EUR, the spot-price currency). An
 *   unrecognised code falls back to EUR so a bad feed never crashes formatting.
 * @return Locale-formatted currency string.
 */
fun formatPrice(price: Double, decimals: Int, currencyCode: String = "EUR"): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
        currency = runCatching { Currency.getInstance(currencyCode) }.getOrDefault(Currency.getInstance("EUR"))
        minimumFractionDigits = decimals
        maximumFractionDigits = decimals
    }
    return formatter.format(price)
}

/**
 * Returns the display symbol for a currency code (e.g. "EUR" → "€"), in the device locale.
 * Falls back to the code itself if it isn't a recognised ISO 4217 currency.
 *
 * @param currencyCode ISO 4217 currency code.
 * @return The locale-specific currency symbol, or the code on failure.
 */
fun currencySymbol(currencyCode: String): String =
    runCatching { Currency.getInstance(currencyCode).getSymbol(Locale.getDefault()) }.getOrDefault(currencyCode)

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
    // Branch on the localisation mode once, rather than per line: `resources?.getX(...) ?: english`
    // on every arm would add an unreachable "resources non-null but getX returned null" branch to
    // each line. This keeps the English fallback for pure (non-Robolectric) tests while leaving only
    // real, coverable branches.
    return if (resources != null) {
        when {
            hours == 0 -> resources.getString(R.string.duration_minutes_only, minutes)
            minutes == 0 -> resources.getQuantityString(R.plurals.duration_hours_only, hours, hours)
            else -> resources.getQuantityString(R.plurals.duration_hours_minutes, hours, hours, minutes)
        }
    } else {
        when {
            hours == 0 -> "${minutes}m"
            minutes == 0 -> "${hours}h"
            else -> "${hours}h ${minutes}m"
        }
    }
}
