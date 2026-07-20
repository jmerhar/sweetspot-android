package today.sweetspot.util

import today.sweetspot.util.AllInPricing.AllInComponents

/**
 * Pure geometry for the all-in "fixed baseline + spot deviation" bar chart.
 *
 * Every bar shares a vertical baseline at the constant fixed cost ([AllInComponents.fixedTotal] —
 * energy tax + surcharge, VAT-inclusive). The spot component is drawn as a deviation from that
 * baseline: a positive spot extends the bar to the right of the baseline, a negative spot draws a
 * band back over the right end of the fixed block (so cheaper hours visibly pull the bar in). The
 * fixed block itself is identical on every bar, which is what makes the baseline read as a single
 * vertical line across the chart.
 *
 * All positions are fractions of the chart's value range `[xMin, xMax]`, so a renderer can place
 * each segment by absolute offset/width without any weight arithmetic. When every bar's total is
 * non-negative, `xMin` is 0 and the fixed block sits at the left edge. When some all-in total goes
 * below zero, `xMin` is that lowest total: the zero reference shifts right, and a deeply-negative
 * spot's band extends left of it — the "getting paid" case, drawn with the same components rather
 * than a separate fallback. Segments are returned in draw order: the fixed block first, then the
 * spot layer on top (which matters for a negative spot, whose band overlays the fixed block's tail).
 */
object AllInBarSegments {

    /** Which price component a bar segment represents (drives its colour in the UI). */
    enum class Role {
        /** Energy tax portion of the fixed block. */
        TAX,

        /** Supplier-surcharge portion of the fixed block. */
        SURCHARGE,

        /** Positive spot price, extending right of the baseline. */
        SPOT,

        /** Negative spot price, drawn back over the fixed block's tail. */
        SPOT_NEGATIVE
    }

    /**
     * A single coloured segment of a bar, positioned by fractions of the chart's value range.
     *
     * @property role Which component this segment represents.
     * @property startFraction Left edge as a fraction of `[xMin, xMax]` (0 = the chart's left edge).
     * @property widthFraction Width as a fraction of the `[xMin, xMax]` range.
     */
    data class Segment(val role: Role, val startFraction: Float, val widthFraction: Float)

    /**
     * Computes the segments for one all-in bar in the fixed-baseline + spot-deviation layout.
     *
     * @param total The bar's all-in price (may be negative — a net "getting paid" bar).
     * @param components The VAT-inclusive fixed components (energy tax + surcharge).
     * @param xMax The chart's right extent (≥ every bar's `total` and ≥ `fixedTotal`).
     * @param xMin The chart's left extent (≤ 0 and ≤ every bar's `total`; 0 when no total is negative).
     * @return Ordered segments (fixed block first, then the spot layer). Zero-width components are
     *   omitted so a renderer never has to special-case them. Empty when the range is non-positive.
     */
    fun segmentsFor(
        total: Double,
        components: AllInComponents,
        xMax: Double,
        xMin: Double = 0.0
    ): List<Segment> {
        val range = xMax - xMin
        if (range <= 0.0) return emptyList()
        // Map a value to its fraction of the [xMin, xMax] range. frac(0) is the zero reference.
        fun frac(value: Double): Float = ((value - xMin) / range).toFloat()

        val fixed = components.fixedTotal
        val spot = total - fixed
        // Treat a spot within rounding noise of the baseline as exactly zero (no spot segment),
        // so `total == fixedTotal` doesn't leave a hairline segment from float subtraction.
        val eps = 1e-9
        val segments = mutableListOf<Segment>()

        // Fixed block, always spanning the value range [0, fixedTotal] — constant across bars.
        if (components.energyTax > 0.0) {
            segments += Segment(Role.TAX, frac(0.0), frac(components.energyTax) - frac(0.0))
        }
        if (components.surcharge > 0.0) {
            segments += Segment(Role.SURCHARGE, frac(components.energyTax), frac(fixed) - frac(components.energyTax))
        }

        // Spot deviation from the baseline (fixedTotal).
        if (spot > eps) {
            segments += Segment(Role.SPOT, frac(fixed), frac(total) - frac(fixed))
        } else if (spot < -eps) {
            // Band over [total, fixedTotal] — the market "discount" eating back into the fixed block,
            // crossing the zero reference (frac(0)) when the all-in total itself is below zero.
            segments += Segment(Role.SPOT_NEGATIVE, frac(total), frac(fixed) - frac(total))
        }

        return segments
    }
}
