package today.sweetspot.data.share

import kotlinx.serialization.encodeToString
import today.sweetspot.util.sweetSpotJson
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import today.sweetspot.model.Appliance
import today.sweetspot.model.SharedSetup
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * Outcome of decoding a shared-setup payload.
 */
sealed interface DecodeResult {
    /** The payload decoded to a usable setup. */
    data class Success(val setup: SharedSetup) : DecodeResult

    /**
     * The payload was written by a newer app whose schema this version doesn't understand.
     *
     * @property schemaVersion The (higher-than-supported) schema version found in the payload.
     */
    data class TooNew(val schemaVersion: Int) : DecodeResult

    /** The payload was unreadable (bad Base64/gzip/JSON, or a blank link fragment). */
    data object Malformed : DecodeResult
}

/**
 * Encodes and decodes a [SharedSetup] as a compact, offline deep link for household sharing.
 *
 * The pipeline is JSON → gzip → URL-safe Base64 (no padding), carried in the **fragment** of
 * `https://sweetspot.today/import#<payload>` so the data never leaves the device (a fragment is
 * not sent to the server). The same encoded string backs both the QR code and the shared link.
 *
 * Mirrors the structure of [today.sweetspot.data.usage.UsageSnapshot]: a stateless object with a
 * shared lenient [sweetSpotJson] and pure functions, unit-testable without Android.
 */
object SetupShare {

    /** Highest payload schema this app can read; a higher one decodes to [DecodeResult.TooNew]. */
    const val CURRENT_SCHEMA = 1

    /** Base URL whose fragment carries the encoded payload. */
    const val IMPORT_BASE = "https://sweetspot.today/import"

    private val json = sweetSpotJson

    /** Encodes a setup to the URL-safe Base64 payload string (JSON → gzip → Base64). */
    fun encode(setup: SharedSetup): String {
        val bytes = json.encodeToString(setup).toByteArray(Charsets.UTF_8)
        val gzipped = ByteArrayOutputStream().also { out ->
            GZIPOutputStream(out).use { it.write(bytes) }
        }.toByteArray()
        return Base64.getUrlEncoder().withoutPadding().encodeToString(gzipped)
    }

    /** Builds the shareable deep link (QR + sharesheet) carrying [setup] in its fragment. */
    fun toLink(setup: SharedSetup): String = "$IMPORT_BASE#${encode(setup)}"

    /**
     * Decodes a raw payload string.
     *
     * The schema version is read from the JSON before a full decode, so a future payload that
     * merely renames or requires new fields is reported as [DecodeResult.TooNew] rather than
     * [DecodeResult.Malformed].
     */
    fun decode(payload: String): DecodeResult =
        try {
            val gzipped = Base64.getUrlDecoder().decode(payload)
            val jsonText = GZIPInputStream(ByteArrayInputStream(gzipped))
                .use { it.readBytes() }
                .toString(Charsets.UTF_8)
            val schema = json.parseToJsonElement(jsonText)
                .jsonObject["schemaVersion"]?.jsonPrimitive?.intOrNull ?: 1
            if (schema > CURRENT_SCHEMA) {
                DecodeResult.TooNew(schema)
            } else {
                DecodeResult.Success(json.decodeFromString<SharedSetup>(jsonText))
            }
        } catch (_: Exception) {
            DecodeResult.Malformed
        }

    /** Decodes the payload carried in a link's URI fragment (the part after `#`). */
    fun fromLink(fragment: String?): DecodeResult =
        if (fragment.isNullOrBlank()) DecodeResult.Malformed else decode(fragment)

    /**
     * Merges incoming appliances into an existing list, re-minting every imported appliance's id
     * so it never collides with a local one.
     *
     * @param existing The receiver's current appliances.
     * @param incoming The appliances to import (for a "pick" import, pass only the chosen subset).
     * @param replace When true, adopt [incoming] wholesale; when false, append the incoming
     *        appliances that aren't already present by content (name, duration, EV specs, power,
     *        icon), keeping the receiver's own list.
     * @param newId Factory for fresh ids (production passes `UUID.randomUUID().toString()`; tests
     *        inject a deterministic counter).
     */
    fun mergeAppliances(
        existing: List<Appliance>,
        incoming: List<Appliance>,
        replace: Boolean,
        newId: () -> String,
    ): List<Appliance> {
        val reIded = incoming.map { it.copy(id = newId()) }
        if (replace) return reIded
        val fresh = reIded.filter { candidate ->
            existing.none { sameContent(it, candidate) }
        }
        return existing + fresh
    }

    /** Content equality for dedupe, ignoring the (per-device) id. */
    private fun sameContent(a: Appliance, b: Appliance): Boolean =
        a.name == b.name &&
            a.durationHours == b.durationHours &&
            a.durationMinutes == b.durationMinutes &&
            a.ev == b.ev &&
            a.powerKw == b.powerKw &&
            a.icon == b.icon
}
