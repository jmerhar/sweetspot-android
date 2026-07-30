package today.sweetspot.model

import kotlinx.serialization.Serializable

/**
 * A portable snapshot of the parts of a user's setup worth sharing with another household member:
 * the appliance list (including electric vehicles with their [EvSpec]), the chosen appliance
 * [sort], and the EV device settings (home-charger power, default target SoC, and placement).
 *
 * Deliberately excludes anything personal or device-local: tap-history usage, region, data-source
 * order, all-in pricing, and trial/subscription state. Encoded and transported by
 * [today.sweetspot.data.share.SetupShare].
 *
 * @property schemaVersion Payload schema version, so a newer sender can be detected and rejected
 *           gracefully by an older app (see [today.sweetspot.data.share.SetupShare.CURRENT_SCHEMA]).
 * @property appliances The appliances to share, in their stored order.
 * @property sort The appliance ordering to adopt on a full replace.
 * @property evHomeChargerKw Home charger output in kW.
 * @property evDefaultTargetSoc Default target state of charge (0–100).
 * @property evPosition Vehicle placement, stored as [EvPosition.key] (resolved via
 *           [EvPosition.fromKey]) to match how [today.sweetspot.data.repository.SettingsRepository] persists it.
 * @property evSeparate Whether vehicles are shown in their own section.
 * @property grouping Home-screen chip grouping, stored as [ApplianceGrouping.key] (resolved via
 *           [ApplianceGrouping.fromKey]). Defaulted so payloads from older senders decode unchanged.
 */
@Serializable
data class SharedSetup(
    val schemaVersion: Int = 1,
    val appliances: List<Appliance>,
    val sort: ApplianceSort = ApplianceSort(),
    val evHomeChargerKw: Double = 11.0,
    val evDefaultTargetSoc: Int = 80,
    val evPosition: String = EvPosition.INTERLEAVED.key,
    val evSeparate: Boolean = false,
    val grouping: String = ApplianceGrouping.NONE.key,
)
