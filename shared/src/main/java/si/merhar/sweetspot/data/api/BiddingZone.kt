package si.merhar.sweetspot.data.api

/**
 * EIC codes for European bidding zones on the ENTSO-E Transparency Platform.
 *
 * Each constant maps a short country/zone identifier to the official
 * Energy Identification Code (EIC) used across European energy markets
 * (ENTSO-E, EPEX SPOT, Nord Pool, etc.).
 */
object BiddingZone {
    // Western Europe
    const val NL = "10YNL----------L"
    const val BE = "10YBE----------2"
    const val FR = "10YFR-RTE------C"
    const val DE_LU = "10Y1001A1001A82H"
    const val AT = "10YAT-APG------L"
    const val CH = "10YCH-SWISSGRIDZ"

    // Iberian Peninsula
    const val ES = "10YES-REE------0"
    const val PT = "10YPT-REN------W"

    // Central Europe
    const val PL = "10YPL-AREA-----S"
    const val CZ = "10YCZ-CEPS-----N"
    const val SK = "10YSK-SEPS-----K"
    const val HU = "10YHU-MAVIR----U"

    // Nordic
    const val DK1 = "10YDK-1--------W"
    const val DK2 = "10YDK-2--------M"
    const val FI = "10YFI-1--------U"
    const val NO1 = "10YNO-1--------2"
    const val NO2 = "10YNO-2--------T"
    const val NO3 = "10YNO-3--------J"
    const val NO4 = "10YNO-4--------9"
    const val NO5 = "10Y1001A1001A48H"
    const val SE1 = "10Y1001A1001A44P"
    const val SE2 = "10Y1001A1001A45N"
    const val SE3 = "10Y1001A1001A46L"
    const val SE4 = "10Y1001A1001A47J"

    // Baltic
    const val EE = "10Y1001A1001A39I"
    const val LV = "10YLV-1001A00074"
    const val LT = "10YLT-1001A0008Q"

    // Southeastern Europe
    const val BG = "10YCA-BULGARIA-R"
    const val GR = "10YGR-HTSO-----Y"
    const val HR = "10YHR-HEP------M"
    const val RO = "10YRO-TEL------P"
    const val SI = "10YSI-ELES-----O"
    const val RS = "10YCS-SERBIATSOV"
    const val ME = "10YCS-CG-TSO---S"
    const val MK = "10YMK-MEPSO----8"

    // Italy
    const val IT_NORD = "10Y1001A1001A73I"
    const val IT_CNOR = "10Y1001A1001A70O"
    const val IT_CSUD = "10Y1001A1001A71M"
    const val IT_SUD = "10Y1001A1001A788"
    const val IT_CALA = "10Y1001C--00096J"
    const val IT_SICI = "10Y1001A1001A75E"
    const val IT_SARD = "10Y1001A1001A74G"
    // Ireland (all-island Single Electricity Market)
    const val IE_SEM = "10Y1001A1001A59C"
}
