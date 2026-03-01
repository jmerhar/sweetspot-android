package si.merhar.sweetspot.util

/**
 * Parse free-form duration input into decimal hours.
 * Accepts: "2h 30m", "4h", "90m", "2.5", "0.5", etc.
 * Returns null on failure. Valid range: 1 minute to 24 hours.
 */
fun parseDuration(input: String): Double? {
    val trimmed = input.trim().lowercase()
    if (trimmed.isEmpty()) return null

    var hours = 0.0
    var matched = false

    // Match hours component: "2h", "2.5h"
    val hRegex = Regex("""(\d+(?:\.\d+)?)\s*h""")
    hRegex.find(trimmed)?.let {
        hours += it.groupValues[1].toDouble()
        matched = true
    }

    // Match minutes component: "30m", "90m"
    val mRegex = Regex("""(\d+(?:\.\d+)?)\s*m""")
    mRegex.find(trimmed)?.let {
        hours += it.groupValues[1].toDouble() / 60.0
        matched = true
    }

    // If no h/m suffix found, treat as decimal hours
    if (!matched) {
        val numRegex = Regex("""^(\d+(?:\.\d+)?)$""")
        val numMatch = numRegex.find(trimmed) ?: return null
        hours = numMatch.groupValues[1].toDouble()
    }

    // Validate range: 1 minute to 24 hours
    if (hours < 1.0 / 60.0 || hours > 24.0) return null

    return hours
}
