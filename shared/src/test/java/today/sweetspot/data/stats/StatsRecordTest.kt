package today.sweetspot.data.stats

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for [StatsRecord]'s binary encode/decode used by Data Layer transfer and file storage,
 * including the graceful handling of empty and corrupt input.
 */
class StatsRecordTest {

    private fun rec(i: Int, success: Boolean = true, err: String = "") =
        StatsRecord(1000L + i, "NL", "entsoe", "phone", success, err, 42L + i)

    @Test
    fun `single record round-trips through bytes`() {
        val r = rec(0, success = false, err = "TIMEOUT")
        assertEquals(listOf(r), StatsRecord.decodeFromBytes(StatsRecord.encodeToBytes(listOf(r))))
    }

    @Test
    fun `multiple records round-trip preserving order`() {
        val list = listOf(rec(1), rec(2, success = false, err = "HTTP_503"), rec(3))
        assertEquals(list, StatsRecord.decodeFromBytes(StatsRecord.encodeToBytes(list)))
    }

    @Test
    fun `empty list encodes to empty bytes and decodes to empty`() {
        val bytes = StatsRecord.encodeToBytes(emptyList())
        assertEquals(0, bytes.size)
        assertEquals(emptyList<StatsRecord>(), StatsRecord.decodeFromBytes(bytes))
    }

    @Test
    fun `garbage bytes decode to an empty list`() {
        assertEquals(emptyList<StatsRecord>(), StatsRecord.decodeFromBytes(byteArrayOf(1, 2, 3)))
    }

    @Test
    fun `partial corruption keeps the records decoded so far`() {
        val bytes = StatsRecord.encodeToBytes(listOf(rec(1), rec(2)))
        // Chop the tail of the second record; the first is fully intact and must survive.
        val truncated = bytes.copyOf(bytes.size - 3)
        assertEquals(listOf(rec(1)), StatsRecord.decodeFromBytes(truncated))
    }
}
