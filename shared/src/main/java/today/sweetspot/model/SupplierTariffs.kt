package today.sweetspot.model

import kotlinx.serialization.Serializable

/**
 * A country's electricity tariff feed, downloaded from
 * `https://sweetspot.today/data/suppliers/<cc>.json` and used to turn a bare spot price into an
 * approximate all-in consumer price. The feed is built by `bin/build-suppliers.py`.
 *
 * The all-in price for a spot price is
 * `(spot + Σ perKwh-tax.value + supplierSurcharge) × Π(1 + percentage-tax.value)` —
 * see [today.sweetspot.util.AllInPricing].
 *
 * @property schemaVersion Feed schema version.
 * @property country ISO 3166-1 alpha-2 country code (e.g. "NL").
 * @property currency ISO 4217 currency code (e.g. "EUR").
 * @property generated ISO-8601 timestamp of when the feed was generated.
 * @property usable Whether the essential tax fields were sourced; all-in must not be shown when false.
 * @property errors Diagnostics recorded when the feed could not be fully built.
 * @property warnings Non-fatal diagnostics (e.g. a supplier that couldn't be priced).
 * @property taxes Country tax components applied to every price (per-kWh additive + percentage multipliers).
 * @property suppliers Per-supplier surcharges the user can choose from.
 */
@Serializable
data class SupplierTariffs(
    val schemaVersion: Int = 0,
    val country: String = "",
    val currency: String = "",
    val generated: String = "",
    val usable: Boolean = false,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val taxes: List<TaxComponent> = emptyList(),
    val suppliers: List<SupplierTariff> = emptyList()
)

/**
 * One tax/levy component of the all-in price.
 *
 * @property id Stable identifier (e.g. "energyTax", "vat").
 * @property name Human-readable label.
 * @property type [TYPE_PER_KWH] (additive, ex-VAT) or [TYPE_PERCENTAGE] (multiplicative, applied last).
 * @property value A per-kWh amount in the feed's currency (perKwh) or a rate like `0.21` (percentage).
 * @property source Provenance of the value (e.g. "frank").
 */
@Serializable
data class TaxComponent(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val value: Double = 0.0,
    val source: String? = null
) {
    companion object {
        /** Additive per-kWh component, stored ex-VAT (e.g. energy tax). */
        const val TYPE_PER_KWH = "perKwh"

        /** Multiplicative component applied last as `× (1 + value)` (e.g. VAT). */
        const val TYPE_PERCENTAGE = "percentage"
    }
}

/**
 * A supplier's per-kWh surcharge (ex-VAT) over the spot price.
 *
 * @property id Stable app-facing supplier id (e.g. "frankenergie").
 * @property name Human-readable supplier name.
 * @property surchargePerKwh Per-kWh markup over spot, ex-VAT, in the feed's currency.
 * @property fixedMonthlyFee Optional fixed monthly fee — not part of the marginal run cost; may be null.
 * @property source Provenance of the value (e.g. "enever", "frank").
 */
@Serializable
data class SupplierTariff(
    val id: String = "",
    val name: String = "",
    val surchargePerKwh: Double = 0.0,
    val fixedMonthlyFee: Double? = null,
    val source: String? = null
)
