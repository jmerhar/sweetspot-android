package today.sweetspot.data.share

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import today.sweetspot.model.Appliance
import today.sweetspot.model.EvSpec

/**
 * Tests for [SetupShare.mergeAppliances] merge semantics and id re-minting.
 */
class SetupMergeTest {

    /** Deterministic id factory: n1, n2, n3, … so re-minted ids are predictable. */
    private fun counterIds(): () -> String {
        var n = 0
        return { "n${++n}" }
    }

    private val existing = listOf(
        Appliance("x1", "Dishwasher", 3, 0, icon = "dishwasher"),
        Appliance("x2", "Car", ev = EvSpec(60.0, 11.0)),
    )

    @Test
    fun `replace adopts incoming appliances with fresh ids`() {
        val incoming = listOf(Appliance("src", "Dryer", 2, 0, icon = "dryer"))
        val merged = SetupShare.mergeAppliances(existing, incoming, replace = true, newId = counterIds())
        assertEquals(1, merged.size)
        assertEquals("Dryer", merged[0].name)
        assertEquals("n1", merged[0].id)
        assertNotEquals("src", merged[0].id)
    }

    @Test
    fun `add appends incoming and re-mints their ids`() {
        val incoming = listOf(Appliance("src", "Dryer", 2, 0, icon = "dryer"))
        val merged = SetupShare.mergeAppliances(existing, incoming, replace = false, newId = counterIds())
        assertEquals(listOf("x1", "x2", "n1"), merged.map { it.id })
        assertEquals("Dryer", merged.last().name)
    }

    @Test
    fun `add skips content-duplicates regardless of id`() {
        // Same content as existing "Dishwasher" but a different id — should not be added twice.
        val incoming = listOf(
            Appliance("other", "Dishwasher", 3, 0, icon = "dishwasher"),
            Appliance("new", "Kettle", 0, 15, icon = "kettle"),
        )
        val merged = SetupShare.mergeAppliances(existing, incoming, replace = false, newId = counterIds())
        assertEquals(listOf("Dishwasher", "Car", "Kettle"), merged.map { it.name })
    }

    @Test
    fun `add carries EV appliances with their specs`() {
        val incoming = listOf(Appliance("src", "Van", ev = EvSpec(80.0, 22.0)))
        val merged = SetupShare.mergeAppliances(existing, incoming, replace = false, newId = counterIds())
        val added = merged.last()
        assertTrue(added.isEv)
        assertEquals(EvSpec(80.0, 22.0), added.ev)
    }

    @Test
    fun `add keeps a same-name appliance that differs by duration`() {
        val incoming = listOf(Appliance("other", "Dishwasher", 2, 30, icon = "dishwasher"))
        val merged = SetupShare.mergeAppliances(existing, incoming, replace = false, newId = counterIds())
        assertEquals(listOf("Dishwasher", "Car", "Dishwasher"), merged.map { it.name })
    }

    @Test
    fun `add keeps a same-name appliance that differs by power`() {
        val incoming = listOf(Appliance("other", "Dishwasher", 3, 0, icon = "dishwasher", powerKw = 2.0))
        val merged = SetupShare.mergeAppliances(existing, incoming, replace = false, newId = counterIds())
        assertEquals(3, merged.size) // existing Dishwasher has null power, so this is distinct
    }

    @Test
    fun `add differentiates EV appliances of the same name by specs`() {
        // The existing "Car" has a 60 kWh battery; an incoming "Car" with different specs is new.
        val incoming = listOf(Appliance("src", "Car", ev = EvSpec(75.0, 11.0)))
        val merged = SetupShare.mergeAppliances(existing, incoming, replace = false, newId = counterIds())
        assertEquals(3, merged.size)
        assertEquals(EvSpec(75.0, 11.0), merged.last().ev)
    }
}
