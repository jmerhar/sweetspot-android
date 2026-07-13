package today.sweetspot.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for [EvPosition.fromKey] resolution.
 */
class EvPositionTest {

    @Test
    fun `known keys resolve`() {
        assertEquals(EvPosition.INTERLEAVED, EvPosition.fromKey("interleaved"))
        assertEquals(EvPosition.FIRST, EvPosition.fromKey("first"))
        assertEquals(EvPosition.LAST, EvPosition.fromKey("last"))
    }

    @Test
    fun `unknown and null default to interleaved`() {
        assertEquals(EvPosition.INTERLEAVED, EvPosition.fromKey("nonsense"))
        assertEquals(EvPosition.INTERLEAVED, EvPosition.fromKey(null))
    }
}
