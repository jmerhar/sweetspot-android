package si.merhar.sweetspot.model

import si.merhar.sweetspot.data.BiddingZone

/**
 * A specific electricity pricing zone within a country.
 *
 * Most countries have a single zone, but some (DK, IT, NO, SE) are split
 * into multiple bidding zones with independent day-ahead prices.
 *
 * @property id Short identifier, e.g. `"NL"`, `"DK1"`, `"IT_NORD"`.
 * @property label Human-readable label, e.g. `"Netherlands"`, `"DK1 — West Denmark"`.
 * @property eicCode EIC code for the ENTSO-E API (see [BiddingZone]).
 * @property timeZoneId IANA timezone, e.g. `"Europe/Amsterdam"`.
 */
data class PriceZone(
    val id: String,
    val label: String,
    val eicCode: String,
    val timeZoneId: String
)

/**
 * A country that participates in European electricity markets.
 *
 * @property code ISO 3166-1 alpha-2 code, e.g. `"NL"`, `"DK"`.
 * @property name Display name, e.g. `"Netherlands"`, `"Denmark"`.
 * @property zones Bidding zones within this country (1 for most, multiple for DK/IT/NO/SE).
 */
data class Country(
    val code: String,
    val name: String,
    val zones: List<PriceZone>
)

/**
 * Registry of all supported countries and their bidding zones.
 *
 * Contains 30 countries covering 43 European bidding zones on the ENTSO-E
 * Transparency Platform. DE and LU share the same bidding zone (`DE_LU`).
 * IE uses the all-island Single Electricity Market (SEM).
 */
object Countries {

    /** All supported countries, sorted alphabetically by name. */
    val all: List<Country> = listOf(
        Country(
            code = "AT", name = "Austria",
            zones = listOf(PriceZone("AT", "Austria", BiddingZone.AT, "Europe/Vienna"))
        ),
        Country(
            code = "BE", name = "Belgium",
            zones = listOf(PriceZone("BE", "Belgium", BiddingZone.BE, "Europe/Brussels"))
        ),
        Country(
            code = "BG", name = "Bulgaria",
            zones = listOf(PriceZone("BG", "Bulgaria", BiddingZone.BG, "Europe/Sofia"))
        ),
        Country(
            code = "HR", name = "Croatia",
            zones = listOf(PriceZone("HR", "Croatia", BiddingZone.HR, "Europe/Zagreb"))
        ),
        Country(
            code = "CZ", name = "Czechia",
            zones = listOf(PriceZone("CZ", "Czechia", BiddingZone.CZ, "Europe/Prague"))
        ),
        Country(
            code = "DK", name = "Denmark",
            zones = listOf(
                PriceZone("DK1", "DK1 \u2014 West Denmark", BiddingZone.DK1, "Europe/Copenhagen"),
                PriceZone("DK2", "DK2 \u2014 East Denmark", BiddingZone.DK2, "Europe/Copenhagen")
            )
        ),
        Country(
            code = "EE", name = "Estonia",
            zones = listOf(PriceZone("EE", "Estonia", BiddingZone.EE, "Europe/Tallinn"))
        ),
        Country(
            code = "FI", name = "Finland",
            zones = listOf(PriceZone("FI", "Finland", BiddingZone.FI, "Europe/Helsinki"))
        ),
        Country(
            code = "FR", name = "France",
            zones = listOf(PriceZone("FR", "France", BiddingZone.FR, "Europe/Paris"))
        ),
        Country(
            code = "DE", name = "Germany",
            zones = listOf(PriceZone("DE_LU", "Germany / Luxembourg", BiddingZone.DE_LU, "Europe/Berlin"))
        ),
        Country(
            code = "GR", name = "Greece",
            zones = listOf(PriceZone("GR", "Greece", BiddingZone.GR, "Europe/Athens"))
        ),
        Country(
            code = "HU", name = "Hungary",
            zones = listOf(PriceZone("HU", "Hungary", BiddingZone.HU, "Europe/Budapest"))
        ),
        Country(
            code = "IE", name = "Ireland",
            zones = listOf(PriceZone("IE_SEM", "Ireland (SEM)", BiddingZone.IE_SEM, "Europe/Dublin"))
        ),
        Country(
            code = "IT", name = "Italy",
            zones = listOf(
                PriceZone("IT_NORD", "North", BiddingZone.IT_NORD, "Europe/Rome"),
                PriceZone("IT_CNOR", "Centre-North", BiddingZone.IT_CNOR, "Europe/Rome"),
                PriceZone("IT_CSUD", "Centre-South", BiddingZone.IT_CSUD, "Europe/Rome"),
                PriceZone("IT_SUD", "South", BiddingZone.IT_SUD, "Europe/Rome"),
                PriceZone("IT_CALA", "Calabria", BiddingZone.IT_CALA, "Europe/Rome"),
                PriceZone("IT_SICI", "Sicily", BiddingZone.IT_SICI, "Europe/Rome"),
                PriceZone("IT_SARD", "Sardinia", BiddingZone.IT_SARD, "Europe/Rome")
            )
        ),
        Country(
            code = "LV", name = "Latvia",
            zones = listOf(PriceZone("LV", "Latvia", BiddingZone.LV, "Europe/Riga"))
        ),
        Country(
            code = "LT", name = "Lithuania",
            zones = listOf(PriceZone("LT", "Lithuania", BiddingZone.LT, "Europe/Vilnius"))
        ),
        Country(
            code = "LU", name = "Luxembourg",
            zones = listOf(PriceZone("DE_LU", "Germany / Luxembourg", BiddingZone.DE_LU, "Europe/Luxembourg"))
        ),
        Country(
            code = "ME", name = "Montenegro",
            zones = listOf(PriceZone("ME", "Montenegro", BiddingZone.ME, "Europe/Podgorica"))
        ),
        Country(
            code = "NL", name = "Netherlands",
            zones = listOf(PriceZone("NL", "Netherlands", BiddingZone.NL, "Europe/Amsterdam"))
        ),
        Country(
            code = "MK", name = "North Macedonia",
            zones = listOf(PriceZone("MK", "North Macedonia", BiddingZone.MK, "Europe/Skopje"))
        ),
        Country(
            code = "NO", name = "Norway",
            zones = listOf(
                PriceZone("NO1", "NO1 \u2014 Southeast Norway", BiddingZone.NO1, "Europe/Oslo"),
                PriceZone("NO2", "NO2 \u2014 Southwest Norway", BiddingZone.NO2, "Europe/Oslo"),
                PriceZone("NO3", "NO3 \u2014 Central Norway", BiddingZone.NO3, "Europe/Oslo"),
                PriceZone("NO4", "NO4 \u2014 North Norway", BiddingZone.NO4, "Europe/Oslo"),
                PriceZone("NO5", "NO5 \u2014 West Norway", BiddingZone.NO5, "Europe/Oslo")
            )
        ),
        Country(
            code = "PL", name = "Poland",
            zones = listOf(PriceZone("PL", "Poland", BiddingZone.PL, "Europe/Warsaw"))
        ),
        Country(
            code = "PT", name = "Portugal",
            zones = listOf(PriceZone("PT", "Portugal", BiddingZone.PT, "Europe/Lisbon"))
        ),
        Country(
            code = "RO", name = "Romania",
            zones = listOf(PriceZone("RO", "Romania", BiddingZone.RO, "Europe/Bucharest"))
        ),
        Country(
            code = "RS", name = "Serbia",
            zones = listOf(PriceZone("RS", "Serbia", BiddingZone.RS, "Europe/Belgrade"))
        ),
        Country(
            code = "SK", name = "Slovakia",
            zones = listOf(PriceZone("SK", "Slovakia", BiddingZone.SK, "Europe/Bratislava"))
        ),
        Country(
            code = "SI", name = "Slovenia",
            zones = listOf(PriceZone("SI", "Slovenia", BiddingZone.SI, "Europe/Ljubljana"))
        ),
        Country(
            code = "ES", name = "Spain",
            zones = listOf(PriceZone("ES", "Spain", BiddingZone.ES, "Europe/Madrid"))
        ),
        Country(
            code = "SE", name = "Sweden",
            zones = listOf(
                PriceZone("SE1", "SE1 \u2014 North Sweden", BiddingZone.SE1, "Europe/Stockholm"),
                PriceZone("SE2", "SE2 \u2014 North-Central Sweden", BiddingZone.SE2, "Europe/Stockholm"),
                PriceZone("SE3", "SE3 \u2014 South-Central Sweden", BiddingZone.SE3, "Europe/Stockholm"),
                PriceZone("SE4", "SE4 \u2014 South Sweden", BiddingZone.SE4, "Europe/Stockholm")
            )
        ),
        Country(
            code = "CH", name = "Switzerland",
            zones = listOf(PriceZone("CH", "Switzerland", BiddingZone.CH, "Europe/Zurich"))
        )
    )

    /** Lookup map by country code for O(1) access. */
    private val byCode: Map<String, Country> = all.associateBy { it.code }

    /** Lookup map by zone ID for O(1) access. */
    private val priceZoneById: Map<String, PriceZone> = all.flatMap { it.zones }.associateBy { it.id }

    /**
     * Finds a country by its ISO code.
     *
     * @param code ISO 3166-1 alpha-2 country code (e.g. `"NL"`, `"DE"`).
     * @return The matching [Country], or `null` if not supported.
     */
    fun findByCode(code: String): Country? = byCode[code]

    /**
     * Finds a price zone by its zone ID.
     *
     * @param id Zone identifier (e.g. `"NL"`, `"DE_LU"`, `"IT_NORD"`).
     * @return The matching [PriceZone], or `null` if not found.
     */
    fun findPriceZoneById(id: String): PriceZone? = priceZoneById[id]

    /**
     * Returns the default country (Netherlands).
     */
    fun defaultCountry(): Country = byCode.getValue("NL")
}
