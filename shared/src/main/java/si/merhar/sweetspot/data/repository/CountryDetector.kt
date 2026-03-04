package si.merhar.sweetspot.data.repository

import android.content.Context
import android.telephony.TelephonyManager
import si.merhar.sweetspot.model.Countries
import si.merhar.sweetspot.model.Country
import java.util.Locale
import java.util.TimeZone

/**
 * Zero-permission utility that guesses the user's country on first launch.
 *
 * Used only for the initial default — the user can always override in settings.
 * No runtime permissions are required: [TelephonyManager.getSimCountryIso] and
 * [TelephonyManager.getNetworkCountryIso] are available without `READ_PHONE_STATE`.
 */
object CountryDetector {

    /**
     * Maps common IANA timezone IDs to ISO 3166-1 alpha-2 country codes.
     * More reliable than locale (which reflects language preference, not location).
     */
    private val timezoneToCountry = mapOf(
        // Western Europe
        "Europe/Amsterdam" to "NL",
        "Europe/Brussels" to "BE",
        "Europe/Paris" to "FR",
        "Europe/Berlin" to "DE",
        "Europe/Luxembourg" to "LU",
        "Europe/Vienna" to "AT",
        "Europe/Zurich" to "CH",
        // Iberian Peninsula
        "Europe/Madrid" to "ES",
        "Europe/Lisbon" to "PT",
        // Central Europe
        "Europe/Warsaw" to "PL",
        "Europe/Prague" to "CZ",
        "Europe/Bratislava" to "SK",
        "Europe/Budapest" to "HU",
        // Nordic
        "Europe/Copenhagen" to "DK",
        "Europe/Oslo" to "NO",
        "Europe/Stockholm" to "SE",
        "Europe/Helsinki" to "FI",
        // Baltic
        "Europe/Tallinn" to "EE",
        "Europe/Riga" to "LV",
        "Europe/Vilnius" to "LT",
        // Southeastern Europe
        "Europe/Sofia" to "BG",
        "Europe/Athens" to "GR",
        "Europe/Zagreb" to "HR",
        "Europe/Bucharest" to "RO",
        "Europe/Ljubljana" to "SI",
        "Europe/Belgrade" to "RS",
        "Europe/Podgorica" to "ME",
        "Europe/Skopje" to "MK",
        // Italy
        "Europe/Rome" to "IT",
        // Ireland
        "Europe/Dublin" to "IE"
    )

    /**
     * Detects the user's country by checking, in order:
     * 1. SIM card country ([TelephonyManager.getSimCountryIso])
     * 2. Network operator country ([TelephonyManager.getNetworkCountryIso])
     * 3. System timezone → country mapping (more reliable than locale)
     * 4. System locale ([Locale.getDefault]) — reflects language, not location
     * 5. Fallback: NL
     *
     * @param context Android context for accessing [TelephonyManager].
     * @return The detected [Country], or Netherlands as fallback.
     */
    fun detect(context: Context): Country {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager

        // 1. SIM country
        val simCountry = tm?.simCountryIso?.uppercase()?.ifEmpty { null }
        resolve(simCountry)?.let { return it }

        // 2. Network country
        val networkCountry = tm?.networkCountryIso?.uppercase()?.ifEmpty { null }
        resolve(networkCountry)?.let { return it }

        // 3. Timezone (reflects physical location better than locale)
        val tzId = TimeZone.getDefault().id
        val tzCountryCode = timezoneToCountry[tzId]
        resolve(tzCountryCode)?.let { return it }

        // 4. System locale (reflects language preference, not necessarily location)
        val localeCountry = Locale.getDefault().country.ifEmpty { null }
        resolve(localeCountry)?.let { return it }

        // 5. Fallback
        return Countries.defaultCountry()
    }

    /**
     * Resolves an ISO country code to a [Country] in the registry.
     * Unsupported countries (e.g. `"US"`, `"GB"`) fall through to return `null`.
     */
    private fun resolve(code: String?): Country? {
        if (code == null) return null
        return Countries.findByCode(code)
    }
}
