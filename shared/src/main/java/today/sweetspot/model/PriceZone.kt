package today.sweetspot.model

import androidx.annotation.StringRes
import today.sweetspot.data.api.BiddingZone
import today.sweetspot.shared.R

/**
 * A specific electricity pricing zone within a country.
 *
 * Most countries have a single zone, but some (DK, IT, NO, SE) are split
 * into multiple bidding zones with independent day-ahead prices.
 *
 * @property id Short identifier, e.g. `"NL"`, `"DK1"`, `"IT_NORD"`.
 * @property labelRes String resource ID for the human-readable label.
 * @property eicCode EIC code for the ENTSO-E API (see [BiddingZone]).
 * @property timeZoneId IANA timezone, e.g. `"Europe/Amsterdam"`.
 */
data class PriceZone(
    val id: String,
    @param:StringRes val labelRes: Int,
    val eicCode: String,
    val timeZoneId: String
)

/**
 * A country that participates in European electricity markets.
 *
 * @property code ISO 3166-1 alpha-2 code, e.g. `"NL"`, `"DK"`.
 * @property nameRes String resource ID for the display name.
 * @property zones Bidding zones within this country (1 for most, multiple for DK/IT/NO/SE).
 */
data class Country(
    val code: String,
    @param:StringRes val nameRes: Int,
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

    /** All supported countries, sorted alphabetically by English name. */
    val all: List<Country> = listOf(
        Country(
            code = "AT", nameRes = R.string.country_at,
            zones = listOf(PriceZone("AT", R.string.zone_at, BiddingZone.AT, "Europe/Vienna"))
        ),
        Country(
            code = "BE", nameRes = R.string.country_be,
            zones = listOf(PriceZone("BE", R.string.zone_be, BiddingZone.BE, "Europe/Brussels"))
        ),
        Country(
            code = "BG", nameRes = R.string.country_bg,
            zones = listOf(PriceZone("BG", R.string.zone_bg, BiddingZone.BG, "Europe/Sofia"))
        ),
        Country(
            code = "HR", nameRes = R.string.country_hr,
            zones = listOf(PriceZone("HR", R.string.zone_hr, BiddingZone.HR, "Europe/Zagreb"))
        ),
        Country(
            code = "CZ", nameRes = R.string.country_cz,
            zones = listOf(PriceZone("CZ", R.string.zone_cz, BiddingZone.CZ, "Europe/Prague"))
        ),
        Country(
            code = "DK", nameRes = R.string.country_dk,
            zones = listOf(
                PriceZone("DK1", R.string.zone_dk1, BiddingZone.DK1, "Europe/Copenhagen"),
                PriceZone("DK2", R.string.zone_dk2, BiddingZone.DK2, "Europe/Copenhagen")
            )
        ),
        Country(
            code = "EE", nameRes = R.string.country_ee,
            zones = listOf(PriceZone("EE", R.string.zone_ee, BiddingZone.EE, "Europe/Tallinn"))
        ),
        Country(
            code = "FI", nameRes = R.string.country_fi,
            zones = listOf(PriceZone("FI", R.string.zone_fi, BiddingZone.FI, "Europe/Helsinki"))
        ),
        Country(
            code = "FR", nameRes = R.string.country_fr,
            zones = listOf(PriceZone("FR", R.string.zone_fr, BiddingZone.FR, "Europe/Paris"))
        ),
        Country(
            code = "DE", nameRes = R.string.country_de,
            zones = listOf(PriceZone("DE_LU", R.string.zone_de_lu, BiddingZone.DE_LU, "Europe/Berlin"))
        ),
        Country(
            code = "GR", nameRes = R.string.country_gr,
            zones = listOf(PriceZone("GR", R.string.zone_gr, BiddingZone.GR, "Europe/Athens"))
        ),
        Country(
            code = "HU", nameRes = R.string.country_hu,
            zones = listOf(PriceZone("HU", R.string.zone_hu, BiddingZone.HU, "Europe/Budapest"))
        ),
        Country(
            code = "IE", nameRes = R.string.country_ie,
            zones = listOf(PriceZone("IE_SEM", R.string.zone_ie_sem, BiddingZone.IE_SEM, "Europe/Dublin"))
        ),
        Country(
            code = "IT", nameRes = R.string.country_it,
            zones = listOf(
                PriceZone("IT_NORD", R.string.zone_it_nord, BiddingZone.IT_NORD, "Europe/Rome"),
                PriceZone("IT_CNOR", R.string.zone_it_cnor, BiddingZone.IT_CNOR, "Europe/Rome"),
                PriceZone("IT_CSUD", R.string.zone_it_csud, BiddingZone.IT_CSUD, "Europe/Rome"),
                PriceZone("IT_SUD", R.string.zone_it_sud, BiddingZone.IT_SUD, "Europe/Rome"),
                PriceZone("IT_CALA", R.string.zone_it_cala, BiddingZone.IT_CALA, "Europe/Rome"),
                PriceZone("IT_SICI", R.string.zone_it_sici, BiddingZone.IT_SICI, "Europe/Rome"),
                PriceZone("IT_SARD", R.string.zone_it_sard, BiddingZone.IT_SARD, "Europe/Rome")
            )
        ),
        Country(
            code = "LV", nameRes = R.string.country_lv,
            zones = listOf(PriceZone("LV", R.string.zone_lv, BiddingZone.LV, "Europe/Riga"))
        ),
        Country(
            code = "LT", nameRes = R.string.country_lt,
            zones = listOf(PriceZone("LT", R.string.zone_lt, BiddingZone.LT, "Europe/Vilnius"))
        ),
        Country(
            code = "LU", nameRes = R.string.country_lu,
            zones = listOf(PriceZone("DE_LU", R.string.zone_de_lu, BiddingZone.DE_LU, "Europe/Luxembourg"))
        ),
        Country(
            code = "ME", nameRes = R.string.country_me,
            zones = listOf(PriceZone("ME", R.string.zone_me, BiddingZone.ME, "Europe/Podgorica"))
        ),
        Country(
            code = "NL", nameRes = R.string.country_nl,
            zones = listOf(PriceZone("NL", R.string.zone_nl, BiddingZone.NL, "Europe/Amsterdam"))
        ),
        Country(
            code = "MK", nameRes = R.string.country_mk,
            zones = listOf(PriceZone("MK", R.string.zone_mk, BiddingZone.MK, "Europe/Skopje"))
        ),
        Country(
            code = "NO", nameRes = R.string.country_no,
            zones = listOf(
                PriceZone("NO1", R.string.zone_no1, BiddingZone.NO1, "Europe/Oslo"),
                PriceZone("NO2", R.string.zone_no2, BiddingZone.NO2, "Europe/Oslo"),
                PriceZone("NO3", R.string.zone_no3, BiddingZone.NO3, "Europe/Oslo"),
                PriceZone("NO4", R.string.zone_no4, BiddingZone.NO4, "Europe/Oslo"),
                PriceZone("NO5", R.string.zone_no5, BiddingZone.NO5, "Europe/Oslo")
            )
        ),
        Country(
            code = "PL", nameRes = R.string.country_pl,
            zones = listOf(PriceZone("PL", R.string.zone_pl, BiddingZone.PL, "Europe/Warsaw"))
        ),
        Country(
            code = "PT", nameRes = R.string.country_pt,
            zones = listOf(PriceZone("PT", R.string.zone_pt, BiddingZone.PT, "Europe/Lisbon"))
        ),
        Country(
            code = "RO", nameRes = R.string.country_ro,
            zones = listOf(PriceZone("RO", R.string.zone_ro, BiddingZone.RO, "Europe/Bucharest"))
        ),
        Country(
            code = "RS", nameRes = R.string.country_rs,
            zones = listOf(PriceZone("RS", R.string.zone_rs, BiddingZone.RS, "Europe/Belgrade"))
        ),
        Country(
            code = "SK", nameRes = R.string.country_sk,
            zones = listOf(PriceZone("SK", R.string.zone_sk, BiddingZone.SK, "Europe/Bratislava"))
        ),
        Country(
            code = "SI", nameRes = R.string.country_si,
            zones = listOf(PriceZone("SI", R.string.zone_si, BiddingZone.SI, "Europe/Ljubljana"))
        ),
        Country(
            code = "ES", nameRes = R.string.country_es,
            zones = listOf(PriceZone("ES", R.string.zone_es, BiddingZone.ES, "Europe/Madrid"))
        ),
        Country(
            code = "SE", nameRes = R.string.country_se,
            zones = listOf(
                PriceZone("SE1", R.string.zone_se1, BiddingZone.SE1, "Europe/Stockholm"),
                PriceZone("SE2", R.string.zone_se2, BiddingZone.SE2, "Europe/Stockholm"),
                PriceZone("SE3", R.string.zone_se3, BiddingZone.SE3, "Europe/Stockholm"),
                PriceZone("SE4", R.string.zone_se4, BiddingZone.SE4, "Europe/Stockholm")
            )
        ),
        Country(
            code = "CH", nameRes = R.string.country_ch,
            zones = listOf(PriceZone("CH", R.string.zone_ch, BiddingZone.CH, "Europe/Zurich"))
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
