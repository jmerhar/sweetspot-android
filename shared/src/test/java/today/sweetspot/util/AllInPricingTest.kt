package today.sweetspot.util

import org.junit.Assert.assertEquals
import org.junit.Test
import today.sweetspot.model.PriceSlot
import today.sweetspot.model.TaxComponent
import java.time.ZonedDateTime

class AllInPricingTest {

    private val nlTaxes = listOf(
        TaxComponent("energyTax", "Energy tax", TaxComponent.TYPE_PER_KWH, 0.09161, "frank"),
        TaxComponent("vat", "VAT", TaxComponent.TYPE_PERCENTAGE, 0.21, "frank")
    )

    @Test
    fun `marginal applies NL energy tax plus surcharge then VAT`() {
        // (0.15066 + 0.09161 + 0.015) * 1.21 = 0.311297 — matches Frank's published all-in (~0.3113).
        assertEquals(0.311297, AllInPricing.marginal(0.15066, nlTaxes, 0.015), 1e-6)
    }

    @Test
    fun `marginal with no taxes and no surcharge is the identity`() {
        assertEquals(0.1234, AllInPricing.marginal(0.1234, emptyList(), 0.0), 1e-9)
    }

    @Test
    fun `percentage taxes multiply as a product`() {
        val taxes = listOf(
            TaxComponent("a", "A", TaxComponent.TYPE_PERCENTAGE, 0.10, null),
            TaxComponent("b", "B", TaxComponent.TYPE_PERCENTAGE, 0.05, null)
        )
        // 0.20 * 1.10 * 1.05 = 0.231
        assertEquals(0.231, AllInPricing.marginal(0.20, taxes, 0.0), 1e-9)
    }

    @Test
    fun `all-in turns negative only below the honest cutoff`() {
        // Cutoff for "you're being paid" = -(energyTax + surcharge) = -(0.09161 + 0.015) = -0.10661.
        // Percentage VAT is multiplicative and never flips the sign, so it doesn't enter the cutoff.
        assertEquals(true, AllInPricing.marginal(-0.12, nlTaxes, 0.015) < 0)  // below cutoff → negative
        assertEquals(true, AllInPricing.marginal(-0.05, nlTaxes, 0.015) > 0)  // above cutoff → still positive
        // A bare-negative spot (−0.05) is NOT "getting paid" once tax + surcharge are added — the point of all-in.
    }

    @Test
    fun `applyAllIn maps every slot and preserves time and duration`() {
        val base = ZonedDateTime.parse("2026-07-11T00:00+02:00")
        val prices = listOf(
            PriceSlot(base, 0.10, 15),
            PriceSlot(base.plusMinutes(15), 0.20, 15)
        )
        val out = AllInPricing.applyAllIn(prices, nlTaxes, 0.015)
        assertEquals(prices.size, out.size)
        assertEquals(base, out[0].time)
        assertEquals(15, out[0].durationMinutes)
        assertEquals(AllInPricing.marginal(0.10, nlTaxes, 0.015), out[0].price, 1e-9)
        assertEquals(AllInPricing.marginal(0.20, nlTaxes, 0.015), out[1].price, 1e-9)
    }

    @Test
    fun `applyAllIn preserves ordering so the cheapest slot stays cheapest`() {
        val base = ZonedDateTime.parse("2026-07-11T00:00+02:00")
        val prices = listOf(
            PriceSlot(base, 0.30, 60),
            PriceSlot(base.plusHours(1), 0.05, 60),
            PriceSlot(base.plusHours(2), 0.20, 60)
        )
        val out = AllInPricing.applyAllIn(prices, nlTaxes, 0.015)
        // The monotonic transform must keep the cheapest slot (index 1) the cheapest.
        assertEquals(1, out.indices.minByOrNull { out[it].price })
    }

    @Test
    fun `components plus VAT-inclusive spot tip sums to the all-in total`() {
        val c = AllInPricing.components(nlTaxes, 0.015)
        // 0.21 VAT multiplier applied to spot as well; fixed block + spot tip must equal marginal.
        for (spot in listOf(-0.12, 0.0, 0.05, 0.15066, 0.30)) {
            val spotTip = spot * 1.21
            assertEquals(AllInPricing.marginal(spot, nlTaxes, 0.015), c.fixedTotal + spotTip, 1e-9)
        }
    }

    @Test
    fun `components fold VAT into each fixed part`() {
        val c = AllInPricing.components(nlTaxes, 0.015)
        assertEquals(0.09161 * 1.21, c.energyTax, 1e-9)
        assertEquals(0.015 * 1.21, c.surcharge, 1e-9)
        assertEquals((0.09161 + 0.015) * 1.21, c.fixedTotal, 1e-9)
    }

    @Test
    fun `components multiply percentage taxes as a product`() {
        val taxes = listOf(
            TaxComponent("et", "Energy tax", TaxComponent.TYPE_PER_KWH, 0.10, null),
            TaxComponent("a", "A", TaxComponent.TYPE_PERCENTAGE, 0.10, null),
            TaxComponent("b", "B", TaxComponent.TYPE_PERCENTAGE, 0.05, null)
        )
        // 0.10 energy tax × 1.10 × 1.05; 0.02 surcharge × 1.10 × 1.05.
        val c = AllInPricing.components(taxes, 0.02)
        assertEquals(0.10 * 1.155, c.energyTax, 1e-9)
        assertEquals(0.02 * 1.155, c.surcharge, 1e-9)
    }

    @Test
    fun `components are zero with no taxes and no surcharge`() {
        val c = AllInPricing.components(emptyList(), 0.0)
        assertEquals(0.0, c.energyTax, 1e-9)
        assertEquals(0.0, c.surcharge, 1e-9)
        assertEquals(0.0, c.fixedTotal, 1e-9)
    }

    @Test
    fun `spot tip goes negative below the fixed block while the fixed block stays constant`() {
        val c = AllInPricing.components(nlTaxes, 0.015)
        // A negative spot yields a negative VAT-inclusive tip, but the fixed block is unchanged.
        val negativeSpotTip = -0.12 * 1.21
        assertEquals(true, negativeSpotTip < 0)
        assertEquals(AllInPricing.marginal(-0.12, nlTaxes, 0.015), c.fixedTotal + negativeSpotTip, 1e-9)
    }
}
