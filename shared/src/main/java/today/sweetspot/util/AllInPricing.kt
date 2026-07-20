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
     * The VAT-inclusive fixed portion of the all-in price — the part that does not vary with the
     * spot price and is therefore identical for every slot in a series.
     *
     * Each field is a per-kWh amount with the percentage taxes (e.g. VAT) already folded in, so a
     * slot's all-in total decomposes cleanly as `energyTax + surcharge + spot × Π(1 + percentage)`.
     * The chart uses this to draw the constant left-hand block of every bar and derive the
     * time-varying spot tip as `total − fixedTotal`.
     *
     * @property energyTax Per-kWh taxes (summed), VAT-inclusive.
     * @property surcharge Supplier per-kWh surcharge, VAT-inclusive.
     */
    data class AllInComponents(
        val energyTax: Double,
        val surcharge: Double,
    ) {
        /** Combined VAT-inclusive fixed cost per kWh (energy tax + surcharge). */
        val fixedTotal: Double get() = energyTax + surcharge
    }

    /**
     * Decomposes the fixed (spot-independent) part of the all-in price into its VAT-inclusive
     * components, using the same per-kWh/percentage folding as [marginal].
     *
     * The result satisfies `components(taxes, s).fixedTotal + spot × Π(1 + percentage) ==
     * marginal(spot, taxes, s)` for any `spot`, so the fixed block plus the VAT-inclusive spot tip
     * always sums to the all-in total.
     *
     * @param taxes Country tax components (per-kWh additive + percentage multipliers).
     * @param surchargePerKwh Chosen supplier's per-kWh surcharge (ex-VAT).
     * @return The VAT-inclusive fixed components (energy tax + surcharge).
     */
    fun components(taxes: List<TaxComponent>, surchargePerKwh: Double): AllInComponents {
        val additive = taxes.filter { it.type == TaxComponent.TYPE_PER_KWH }.sumOf { it.value }
        val multiplier = taxes
            .filter { it.type == TaxComponent.TYPE_PERCENTAGE }
            .fold(1.0) { acc, tax -> acc * (1.0 + tax.value) }
        return AllInComponents(
            energyTax = additive * multiplier,
            surcharge = surchargePerKwh * multiplier,
        )
    }

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
