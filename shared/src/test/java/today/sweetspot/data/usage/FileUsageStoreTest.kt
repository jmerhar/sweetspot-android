package today.sweetspot.data.usage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * Tests for [FileUsageStore] persistence, recording, and reset-token handling.
 */
class FileUsageStoreTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun store() = FileUsageStore(tmp.root)

    @Test
    fun `record increments count and sets last used`() {
        val s = store()
        s.record("a", 100)
        s.record("a", 250)
        s.record("b", 300)
        assertEquals(2, s.snapshot()["a"]!!.count)
        assertEquals(250L, s.snapshot()["a"]!!.lastUsedMs)
        assertEquals(1, s.snapshot()["b"]!!.count)
    }

    @Test
    fun `snapshot persists across instances`() {
        store().record("a", 100)
        assertEquals(1, store().snapshot()["a"]!!.count)
    }

    @Test
    fun `empty store has empty snapshot and zero token`() {
        val s = store()
        assertTrue(s.snapshot().isEmpty())
        assertEquals(0L, s.token())
    }

    @Test
    fun `reset zeroes usage and adopts token`() {
        val s = store()
        s.record("a", 100)
        s.reset(42)
        assertTrue(s.snapshot().isEmpty())
        assertEquals(42L, s.token())
        // Recording after reset keeps the adopted token.
        s.record("b", 200)
        assertEquals(42L, s.token())
        assertEquals(1, s.snapshot()["b"]!!.count)
    }

    @Test
    fun `corrupt file falls back to empty`() {
        tmp.newFile("appliance_usage.json").writeText("{ not valid")
        val s = FileUsageStore(tmp.root)
        assertTrue(s.snapshot().isEmpty())
        assertEquals(0L, s.token())
    }
}
