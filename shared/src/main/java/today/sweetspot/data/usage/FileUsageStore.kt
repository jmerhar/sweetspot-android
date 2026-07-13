package today.sweetspot.data.usage

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import today.sweetspot.model.ApplianceUsage
import java.io.File

/**
 * On-disk snapshot of the usage store: the adopted reset token plus the cumulative map.
 */
@Serializable
internal data class UsageFile(
    val token: Long = 0,
    val usage: Map<String, ApplianceUsage> = emptyMap(),
)

/**
 * File-backed [UsageStore] persisting a single JSON document in [dir].
 *
 * Stored in cache: usage is best-effort telemetry, so losing it if the system clears the cache
 * only costs some tap history. Reads that hit malformed content fall back to an empty store.
 *
 * @param dir Directory to hold `appliance_usage.json`.
 */
class FileUsageStore(dir: File) : UsageStore {

    private val file = File(dir, "appliance_usage.json")
    private val json = Json { ignoreUnknownKeys = true }
    private val lock = Any()

    private fun read(): UsageFile = synchronized(lock) {
        if (!file.exists()) return UsageFile()
        try {
            json.decodeFromString<UsageFile>(file.readText())
        } catch (_: Exception) {
            UsageFile()
        }
    }

    private fun write(data: UsageFile) = synchronized(lock) {
        try {
            file.writeText(json.encodeToString(data))
        } catch (_: Exception) {
            // Best-effort; a failed write only loses unsynced tap history.
        }
    }

    override fun record(id: String, nowMs: Long) {
        val current = read()
        val existing = current.usage[id]
        val updated = current.usage + (id to ApplianceUsage((existing?.count ?: 0) + 1, nowMs))
        write(current.copy(usage = updated))
    }

    override fun snapshot(): Map<String, ApplianceUsage> = read().usage

    override fun reset(token: Long) = write(UsageFile(token = token))

    override fun token(): Long = read().token
}
