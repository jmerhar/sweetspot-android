package today.sweetspot.data.stats

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class FileStatsCollectorTest {

    private lateinit var tempDir: File
    private lateinit var collector: FileStatsCollector

    @Before
    fun setUp() {
        tempDir = File(System.getProperty("java.io.tmpdir"), "sweetspot_stats_test_${System.nanoTime()}")
        tempDir.mkdirs()
        collector = FileStatsCollector(tempDir)
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `readAll returns empty list when no file exists`() {
        assertTrue(collector.readAll().isEmpty())
    }

    @Test
    fun `record and readAll round-trip single record`() {
        val record = StatsRecord(1700000000L, "NL", "entsoe", "phone", true, "", 450)
        collector.record(record)

        val result = collector.readAll()
        assertEquals(1, result.size)
        assertEquals(record, result[0])
    }

    @Test
    fun `multiple records are appended and read back in order`() {
        val r1 = StatsRecord(1700000000L, "NL", "entsoe", "phone", true, "", 200)
        val r2 = StatsRecord(1700003600L, "NL", "energyzero", "phone", false, "TIMEOUT", 10000)
        val r3 = StatsRecord(1700007200L, "DE_LU", "entsoe", "watch", false, "HTTP_503", 5000)

        collector.record(r1)
        collector.record(r2)
        collector.record(r3)

        val result = collector.readAll()
        assertEquals(3, result.size)
        assertEquals(r1, result[0])
        assertEquals(r2, result[1])
        assertEquals(r3, result[2])
    }

    @Test
    fun `clear deletes file`() {
        collector.record(StatsRecord(1700000000L, "NL", "entsoe", "phone", true, "", 300))
        assertTrue(collector.readAll().isNotEmpty())

        collector.clear()
        assertTrue(collector.readAll().isEmpty())
    }

    @Test
    fun `clear on empty file is a no-op`() {
        collector.clear()
        assertTrue(collector.readAll().isEmpty())
    }

    @Test
    fun `records survive across collector instances`() {
        val record = StatsRecord(1700000000L, "FI", "spothinta", "watch", false, "DNS", 8000)
        collector.record(record)

        val collector2 = FileStatsCollector(tempDir)
        val result = collector2.readAll()
        assertEquals(1, result.size)
        assertEquals(record, result[0])
    }

    @Test
    fun `record after clear starts fresh`() {
        collector.record(StatsRecord(1700000000L, "NL", "entsoe", "phone", true, "", 100))
        collector.clear()
        collector.record(StatsRecord(1700003600L, "AT", "awattar", "phone", false, "CONNECTION", 15000))

        val result = collector.readAll()
        assertEquals(1, result.size)
        assertEquals("AT", result[0].zone)
        assertEquals(15000L, result[0].durationMs)
    }

    @Test
    fun `corrupted file returns empty list`() {
        val file = File(tempDir, "api_stats_v2.bin")
        file.writeBytes(byteArrayOf(0, 1, 2, 3))

        assertTrue(collector.readAll().isEmpty())
    }
}
