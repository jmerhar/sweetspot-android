package today.sweetspot.util

import kotlinx.serialization.json.Json

/**
 * The app's shared kotlinx-serialization [Json] instance.
 *
 * `ignoreUnknownKeys = true` so that any external payload — an API response, a cached blob, a share
 * link, the tariff feed — gaining a new field never breaks decoding. Using one instance everywhere
 * keeps the decoding config consistent (no class can forget the flag) and avoids re-creating the
 * parser per class.
 */
val sweetSpotJson: Json = Json { ignoreUnknownKeys = true }
