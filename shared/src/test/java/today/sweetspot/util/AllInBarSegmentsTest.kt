package today.sweetspot.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import today.sweetspot.util.AllInBarSegments.Role
import today.sweetspot.util.AllInPricing.AllInComponents
import org.junit.Test

class AllInBarSegmentsTest {

    // Fixed block: energy tax 0.10 + surcharge 0.05 → fixedTotal 0.15.
    private val components = AllInComponents(energyTax = 0.10, surcharge = 0.05)

    @Test
    fun `positive spot stacks tax, surcharge, then spot right of the baseline`() {
        // total 0.25 → spot 0.10; xMax 0.25.
        val segs = AllInBarSegments.segmentsFor(total = 0.25, components = components, xMax = 0.25)
        assertEquals(listOf(Role.TAX, Role.SURCHARGE, Role.SPOT), segs.map { it.role })
        // Tax [0, 0.4), surcharge [0.4, 0.6), spot [0.6, 1.0) as fractions of 0.25.
        assertEquals(0f, segs[0].startFraction, 1e-6f)
        assertEquals(0.4f, segs[0].widthFraction, 1e-6f)
        assertEquals(0.4f, segs[1].startFraction, 1e-6f)
        assertEquals(0.2f, segs[1].widthFraction, 1e-6f)
        assertEquals(0.6f, segs[2].startFraction, 1e-6f)   // spot starts at the baseline (fixed/xMax)
        assertEquals(0.4f, segs[2].widthFraction, 1e-6f)
    }

    @Test
    fun `negative spot draws a band over the fixed block's tail`() {
        // total 0.12 (< fixedTotal 0.15) → spot −0.03; xMax = fixedTotal 0.15.
        val segs = AllInBarSegments.segmentsFor(total = 0.12, components = components, xMax = 0.15)
        assertEquals(listOf(Role.TAX, Role.SURCHARGE, Role.SPOT_NEGATIVE), segs.map { it.role })
        // The fixed block still spans the full [0, 1] baseline (tax 0..0.667, surcharge 0.667..1.0).
        assertEquals(1.0f, segs[1].startFraction + segs[1].widthFraction, 1e-6f)
        // Negative-spot band covers [total, fixedTotal] = [0.8, 1.0].
        assertEquals((0.12 / 0.15).toFloat(), segs[2].startFraction, 1e-6f)
        assertEquals((0.03 / 0.15).toFloat(), segs[2].widthFraction, 1e-6f)
    }

    @Test
    fun `spot exactly at the baseline yields only the fixed block`() {
        val segs = AllInBarSegments.segmentsFor(total = 0.15, components = components, xMax = 0.30)
        assertEquals(listOf(Role.TAX, Role.SURCHARGE), segs.map { it.role })
    }

    @Test
    fun `zero-width components are omitted`() {
        val noTax = AllInComponents(energyTax = 0.0, surcharge = 0.05)
        val segs = AllInBarSegments.segmentsFor(total = 0.20, components = noTax, xMax = 0.20)
        assertEquals(listOf(Role.SURCHARGE, Role.SPOT), segs.map { it.role })
        assertEquals(0f, segs[0].startFraction, 1e-6f)   // surcharge starts at the left edge
    }

    @Test
    fun `non-positive range returns no segments`() {
        assertTrue(AllInBarSegments.segmentsFor(0.20, components, 0.0).isEmpty())
        assertTrue(AllInBarSegments.segmentsFor(0.20, components, xMax = 0.10, xMin = 0.10).isEmpty())
    }

    @Test
    fun `a negative total draws the spot band across the zero reference`() {
        // total −0.05 (net "getting paid"): fixedTotal 0.15, spot −0.20. Range spans xMin −0.05 .. xMax 0.30.
        val segs = AllInBarSegments.segmentsFor(total = -0.05, components = components, xMax = 0.30, xMin = -0.05)
        assertEquals(listOf(Role.TAX, Role.SURCHARGE, Role.SPOT_NEGATIVE), segs.map { it.role })

        val range = 0.30 - (-0.05)
        val zeroRef = (0.0 - (-0.05)) / range   // fraction where price 0 falls
        val tax = segs[0]
        val band = segs[2]
        // The fixed block starts at the zero reference (shifted right to make room for the negative region).
        assertEquals(zeroRef.toFloat(), tax.startFraction, 1e-6f)
        // The negative-spot band starts at the far left (total == xMin) and ends at the baseline,
        // i.e. it begins left of the zero reference — the "getting paid" case.
        assertEquals(0f, band.startFraction, 1e-6f)
        assertTrue(band.startFraction < tax.startFraction)
        val baselineFrac = ((0.15 - (-0.05)) / range).toFloat()  // frac(fixedTotal)
        assertEquals(baselineFrac, band.startFraction + band.widthFraction, 1e-6f)  // band ends at the baseline
    }

    @Test
    fun `xMin defaults to zero, reproducing the all-positive layout`() {
        val withDefault = AllInBarSegments.segmentsFor(0.25, components, xMax = 0.25)
        val explicit = AllInBarSegments.segmentsFor(0.25, components, xMax = 0.25, xMin = 0.0)
        assertEquals(explicit, withDefault)
        assertEquals(0f, withDefault[0].startFraction, 1e-6f)  // fixed block at the left edge
    }
}
