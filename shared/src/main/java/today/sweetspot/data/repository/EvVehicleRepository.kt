package today.sweetspot.data.repository

import today.sweetspot.util.sweetSpotJson
import today.sweetspot.model.EvVehicle

/**
 * Read-only access to the bundled EV vehicle database.
 *
 * The database is a normalised JSON array produced at build time by `bin/build-ev-db.py`
 * (see `app/src/main/assets/ev-vehicles.json`). This repository is Android-free: it takes the
 * raw JSON string so it can be unit-tested without a `Context`. The caller (the phone app)
 * reads the asset and constructs the repository once, since parsing is done eagerly.
 *
 * @param json Raw contents of the bundled `ev-vehicles.json` asset.
 */
class EvVehicleRepository(json: String) {

    /** All vehicles, sorted by brand then model (the order they appear in the asset). */
    val vehicles: List<EvVehicle> = parse(json)

    /** Distinct brands present in the database, alphabetically ordered (case-insensitive). */
    fun brands(): List<String> =
        vehicles.map { it.brand }.distinct().sortedBy { it.lowercase() }

    /**
     * Returns all vehicles for a given brand, preserving model order.
     *
     * @param brand Brand name to filter by (case-insensitive).
     */
    fun models(brand: String): List<EvVehicle> =
        vehicles.filter { it.brand.equals(brand, ignoreCase = true) }

    /**
     * Filters vehicles by a free-text query matched against brand, model, and variant.
     *
     * The query is split on whitespace; every term must match somewhere in the vehicle's
     * brand/model/variant text (AND semantics), so "vw id3" or "id.3 pro" both narrow as
     * expected. A blank query returns all vehicles.
     *
     * @param query Free-text search string.
     * @return Matching vehicles in database order.
     */
    fun search(query: String): List<EvVehicle> {
        val terms = query.trim().lowercase().split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (terms.isEmpty()) return vehicles
        return vehicles.filter { vehicle ->
            val haystack = buildString {
                append(vehicle.brand.lowercase()).append(' ')
                append(vehicle.model.lowercase())
                vehicle.variant?.let { append(' ').append(it.lowercase()) }
            }
            terms.all { haystack.contains(it) }
        }
    }

    private companion object {
        /** Lenient parser that ignores unknown fields for forward compatibility. */
        val parser = sweetSpotJson

        /**
         * Parses the bundled JSON array into [EvVehicle]s.
         *
         * @param json Raw JSON array string.
         * @return Parsed vehicles, or an empty list if the content is malformed.
         */
        fun parse(json: String): List<EvVehicle> = try {
            parser.decodeFromString<List<EvVehicle>>(json)
        } catch (_: Exception) {
            emptyList()
        }
    }
}
