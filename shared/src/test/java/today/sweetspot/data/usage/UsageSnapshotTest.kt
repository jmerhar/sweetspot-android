package today.sweetspot.data.usage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import today.sweetspot.model.ApplianceUsage

/**
 * Tests for [UsageSnapshot] byte encoding/decoding.
 */
class UsageSnapshotTest {

    @Test
    fun `round-trips a usage map`() {
        val map = mapOf("a" to ApplianceUsage(3, 1000), "b" to ApplianceUsage(1, 50))
        assertEquals(map, UsageSnapshot.decodeFromBytes(UsageSnapshot.encodeToBytes(map)))
    }

    @Test
    fun `round-trips an empty map`() {
        assertTrue(UsageSnapshot.decodeFromBytes(UsageSnapshot.encodeToBytes(emptyMap())).isEmpty())
    }

    @Test
    fun `malformed bytes decode to empty`() {
        assertTrue(UsageSnapshot.decodeFromBytes("not json".toByteArray()).isEmpty())
        assertTrue(UsageSnapshot.decodeFromBytes(ByteArray(0)).isEmpty())
    }
}
