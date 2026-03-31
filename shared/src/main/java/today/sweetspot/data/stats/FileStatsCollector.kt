package today.sweetspot.data.stats

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.io.File
import java.io.FileOutputStream

/**
 * File-backed [StatsCollector] using an append-only binary format.
 *
 * Each record is appended individually to avoid reading and rewriting the entire file.
 * The file is stored in [cacheDir] as `api_stats_v2.bin` — using cache storage is intentional
 * since the data is transient (meant to be sent and deleted). If the system clears it,
 * only unreported stats are lost, which is acceptable.
 *
 * Binary format per record: see [StatsRecord.writeTo].
 *
 * @param cacheDir The directory to store the stats file in.
 */
class FileStatsCollector(private val cacheDir: File) : StatsCollector {

    private val file = File(cacheDir, "api_stats_v2.bin")
    private val legacyFile = File(cacheDir, "api_stats.bin")
    private val lock = Any()

    init {
        // Delete incompatible v1 stats file (no durationMs field).
        if (legacyFile.exists()) legacyFile.delete()
    }

    override fun record(record: StatsRecord) {
        synchronized(lock) {
            DataOutputStream(FileOutputStream(file, true).buffered()).use { out ->
                StatsRecord.writeTo(record, out)
            }
        }
    }

    override fun readAll(): List<StatsRecord> {
        synchronized(lock) {
            if (!file.exists()) return emptyList()
            return try {
                DataInputStream(file.inputStream().buffered()).use { input ->
                    val records = mutableListOf<StatsRecord>()
                    while (true) {
                        try {
                            records.add(StatsRecord.readFrom(input))
                        } catch (_: EOFException) {
                            break
                        } catch (_: Exception) {
                            break // partial corruption — keep records read so far
                        }
                    }
                    records
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    override fun clear() {
        synchronized(lock) {
            file.delete()
        }
    }
}
