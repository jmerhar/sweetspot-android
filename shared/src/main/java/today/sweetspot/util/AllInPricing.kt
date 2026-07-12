package today.sweetspot.util

import today.sweetspot.model.PriceSlot
import today.sweetspot.model.TaxComponent

/**
 * Pure functions turning a bare spot price into an approximate all-in consumer price.
 *
 * The transform is **display-only**: it is affine and monotonically increasing in the spot price, so
 * applying it uniformly to a price series never changes which window is cheapest — only the displayed
 * cost. Because [PriceSlot.price] feeds every downstream cost/chart calculation, mapping the price list
 * through [applyAllIn] is enough to make the whole results screen reflect all-in figures.
 *
 * Formula: `allIn = (spot + Σ perKwh-tax.value + surcharge) × Π(1 + percentage-tax.value)`. Per-kWh
 * taxes (e.g. energy tax) and the supplier surcharge are additive and ex-VAT; percentage taxes
 * (e.g. VAT) are multiplicative and applied last.
 */
object AllInPricing {

    /**
     * Computes the all-in price for a single spot price.
     *
     * @param spot Bare spot price (e.g. EUR/kWh).
     * @param taxes Country tax components (per-kWh additive + percentage multipliers).
     * @param surchargePerKwh Chosen supplier's per-kWh surcharge (ex-VAT).
     * @return The all-in consumer price for [spot].
     */
    fun marginal(spot: Double, taxes: List<TaxComponent>, surchargePerKwh: Double): Double {
        val additive = taxes.filter { it.type == TaxComponent.TYPE_PER_KWH }.sumOf { it.value }
        val multiplier = taxes
            .filter { it.type == TaxComponent.TYPE_PERCENTAGE }
            .fold(1.0) { acc, tax -> acc * (1.0 + tax.value) }
        return (spot + additive + surchargePerKwh) * multiplier
    }

    /**
     * Returns a copy of [prices] with each slot's price mapped to its all-in value (time and duration
     * preserved), so all downstream cost/chart code reflects all-in automatically.
     */
    fun applyAllIn(
        prices: List<PriceSlot>,
        taxes: List<TaxComponent>,
        surchargePerKwh: Double
    ): List<PriceSlot> = prices.map { it.copy(price = marginal(it.price, taxes, surchargePerKwh)) }
}
