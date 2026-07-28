package today.sweetspot.model

/**
 * A one-time contextual hint (coach mark) shown the first time the user reaches a hard-to-discover
 * control. Each entry has its own "seen" flag in `SettingsRepository`, keyed by [prefKey], so a hint
 * fires once and then never again (until developer options resets them).
 *
 * The set of hints and the order they surface on the results screen is decided by `CoachMarkPolicy`;
 * this enum only names them and owns the persistence key.
 */
enum class CoachMark {
    /** Results screen: the Earlier / Cheaper buttons that step to a sooner or cheaper window. */
    EARLIER_CHEAPER,

    /** Results screen: press-and-hold the price chart for a per-slot price tooltip. */
    CHART_PRESS_HOLD,

    /** Results screen: the total ⇄ spot toggle (shown only once all-in pricing is configured). */
    ALL_IN_TOGGLE,

    /** Home screen: tapping a vehicle chip plans charging by battery percentage. */
    EV_CHIP;

    /** SharedPreferences key for this hint's "seen" flag. */
    val prefKey: String get() = "coach_" + name.lowercase()
}
