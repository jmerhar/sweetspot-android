package today.sweetspot.model

import org.junit.Assert.assertEquals
import org.junit.Test
import today.sweetspot.shared.R

/**
 * Tests for [applianceIconFor] icon resolution.
 */
class ApplianceIconTest {

    @Test
    fun `known ID returns correct drawable`() {
        assertEquals(R.drawable.ic_iron, applianceIconFor("iron"))
    }

    @Test
    fun `unknown ID returns electricity fallback drawable`() {
        assertEquals(R.drawable.ic_electricity, applianceIconFor("nonexistent"))
    }

    @Test
    fun `empty ID returns electricity fallback drawable`() {
        assertEquals(R.drawable.ic_electricity, applianceIconFor(""))
    }

    @Test
    fun `EV appliance resolves to the car drawable`() {
        val ev = Appliance(id = "1", name = "Kia EV9", ev = EvSpec(99.8, 11.0))
        assertEquals(R.drawable.ic_car, applianceIconFor(ev))
    }

    @Test
    fun `EV appliance shows a car even if a stale icon is stored`() {
        val ev = Appliance(id = "1", name = "Kia EV9", icon = "ev_charger", ev = EvSpec(99.8, 11.0))
        assertEquals(R.drawable.ic_car, applianceIconFor(ev))
    }

    @Test
    fun `non-EV appliance resolves its configured icon`() {
        val washer = Appliance(id = "2", name = "Cotton", icon = "washing_machine")
        assertEquals(R.drawable.ic_washing_machine, applianceIconFor(washer))
    }

    @Test
    fun `appliance with no icon falls back to electricity`() {
        val plain = Appliance(id = "3", name = "Mystery", icon = null)
        assertEquals(R.drawable.ic_electricity, applianceIconFor(plain))
    }
}
