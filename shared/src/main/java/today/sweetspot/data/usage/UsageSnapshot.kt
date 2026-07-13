package today.sweetspot.data.usage

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import today.sweetspot.model.ApplianceUsage

/**
 * Serializes a per-appliance usage map for transfer across the Wearable Data Layer.
 *
 * The watch sends its whole cumulative map (not deltas), so re-delivery is harmless; the phone
 * merges it against its own via [today.sweetspot.util.combineUsage].
 */
object UsageSnapshot {

    private val json = Json { ignoreUnknownKeys = true }

    /** Encodes a usage map to JSON bytes. */
    fun encodeToBytes(usage: Map<String, ApplianceUsage>): ByteArray =
        json.encodeToString(usage).toByteArray(Charsets.UTF_8)

    /** Decodes a usage map from JSON bytes, returning an empty map on any malformed input. */
    fun decodeFromBytes(bytes: ByteArray): Map<String, ApplianceUsage> =
        try {
            json.decodeFromString<Map<String, ApplianceUsage>>(bytes.toString(Charsets.UTF_8))
        } catch (_: Exception) {
            emptyMap()
        }
}
