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
}
