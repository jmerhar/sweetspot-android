package today.sweetspot.data.stats

import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException

/**
 * A single API request outcome for stats collection.
 *
 * Records the result of one price-fetching attempt, including which zone and source
 * were used, whether it succeeded, and what category of error occurred on failure.
 *
 * @property epochSecond UTC timestamp of when the request was made.
 * @property zone Bidding zone identifier (e.g. "NL", "DE_LU").
 * @property source Data source identifier (e.g. "entsoe", "energyzero").
 * @property device Device type: "phone" or "watch".
 * @property success Whether the request returned usable price data.
 * @property errorCategory Empty string for success, or a category like "TIMEOUT", "HTTP_503", etc.
 */
data class StatsRecord(
    val epochSecond: Long,
    val zone: String,
    val source: String,
    val device: String,
    val success: Boolean,
    val errorCategory: String
) {
    companion object {
        /**
         * Writes a single [StatsRecord] to a [DataOutputStream].
         *
         * Binary format: epochSecond (Long) + zone (UTF) + source (UTF) +
         * device (UTF) + success (Boolean) + errorCategory (UTF).
         *
         * @param record The record to write.
         * @param out The output stream to write to.
         */
        fun writeTo(record: StatsRecord, out: DataOutputStream) {
            out.writeLong(record.epochSecond)
            out.writeUTF(record.zone)
            out.writeUTF(record.source)
            out.writeUTF(record.device)
            out.writeBoolean(record.success)
            out.writeUTF(record.errorCategory)
        }

        /**
         * Reads a single [StatsRecord] from a [DataInputStream].
         *
         * @param input The input stream to read from.
         * @return The decoded record.
         * @throws EOFException if the stream has no more data.
         */
        fun readFrom(input: DataInputStream): StatsRecord = StatsRecord(
            epochSecond = input.readLong(),
            zone = input.readUTF(),
            source = input.readUTF(),
            device = input.readUTF(),
            success = input.readBoolean(),
            errorCategory = input.readUTF()
        )

        /**
         * Encodes a list of [StatsRecord]s to a byte array.
         *
         * Used for Data Layer transfer (watch → phone) and binary file storage.
         *
         * @param records The records to encode.
         * @return Binary-encoded byte array.
         */
        fun encodeToBytes(records: List<StatsRecord>): ByteArray {
            val baos = ByteArrayOutputStream()
            DataOutputStream(baos).use { out ->
                for (record in records) {
                    writeTo(record, out)
                }
            }
            return baos.toByteArray()
        }

        /**
         * Decodes a byte array into a list of [StatsRecord]s.
         *
         * Reads until EOF. On partial corruption, returns the records decoded so far.
         *
         * @param bytes Binary-encoded stats data.
         * @return Decoded list of records, or empty list on error.
         */
        fun decodeFromBytes(bytes: ByteArray): List<StatsRecord> {
            return try {
                DataInputStream(bytes.inputStream()).use { input ->
                    val records = mutableListOf<StatsRecord>()
                    while (true) {
                        try {
                            records.add(readFrom(input))
                        } catch (_: EOFException) {
                            break
                        } catch (_: Exception) {
                            break // partial corruption — keep records decoded so far
                        }
                    }
                    records
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
}
