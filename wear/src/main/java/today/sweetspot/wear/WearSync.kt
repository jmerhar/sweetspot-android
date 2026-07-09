package today.sweetspot.wear

/**
 * The settings the phone pushes to the watch via the Wearable Data Layer, as raw fields.
 *
 * This is the plain-data boundary between the (untestable) Data Layer plumbing in [WearableSync]
 * and the testable mapping logic in [WearViewModel.onSettingsReceived].
 *
 * @property countryCode ISO country code, or `null`.
 * @property priceZoneId Selected price-zone id, or `null`.
 * @property sourceOrder JSON-encoded source order, or `null`/blank for defaults.
 * @property disabledSources JSON-encoded disabled-source set, or `null`/blank for none.
 * @property language BCP-47 language tag, or `null`/empty for system default.
 * @property statsEnabled Whether API stats collection is enabled.
 * @property isTrialExpired Whether the phone's trial has expired.
 * @property isUnlocked Whether the app is unlocked via subscription.
 */
data class WearSettings(
    val countryCode: String?,
    val priceZoneId: String?,
    val sourceOrder: String?,
    val disabledSources: String?,
    val language: String?,
    val statsEnabled: Boolean,
    val isTrialExpired: Boolean,
    val isUnlocked: Boolean,
)

/**
 * Abstraction over the Wearable Data Layer used by [WearViewModel], so the ViewModel's logic is
 * testable without Google Play Services. The production implementation is [WearableSync]; tests
 * inject a fake.
 */
interface WearSync {

    /**
     * Starts observing phone → watch updates and delivers the current values.
     *
     * @param onAppliances Invoked with the raw appliance JSON whenever it changes (and on start).
     * @param onSettings Invoked with the current [WearSettings] whenever they change (and on start).
     */
    fun observe(onAppliances: (String) -> Unit, onSettings: (WearSettings) -> Unit)

    /** Stops observing. */
    fun stop()

    /**
     * Pushes encoded stats bytes to the phone via the `/stats` path.
     *
     * @param bytes Encoded [today.sweetspot.data.stats.StatsRecord] list.
     */
    suspend fun pushStats(bytes: ByteArray)
}
