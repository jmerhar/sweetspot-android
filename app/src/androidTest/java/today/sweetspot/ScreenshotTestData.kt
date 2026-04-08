package today.sweetspot

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
import androidx.test.platform.app.InstrumentationRegistry
import java.io.DataOutputStream
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Pre-populates SharedPreferences and price cache with realistic test data
 * for screenshot automation.
 *
 * Sets up 3 translated appliances, NL country/zone, unlocked state, production logo,
 * a fixed time override, and ~48h of 15-minute resolution prices. The price curve
 * has midday as the cheapest period so the cheapest window (green) appears prominently.
 */
object ScreenshotTestData {

    private val ZONE_ID = ZoneId.of("Europe/Amsterdam")

    /**
     * Fixed "current time" used for all screenshots: 08:00 today in Amsterdam.
     *
     * This ensures the cheapest window (midday) appears a few hours in the future,
     * placing the green bars near the top of the result screen chart.
     */
    private fun fixedNow(): ZonedDateTime =
        LocalDate.now(ZONE_ID).atTime(8, 0).atZone(ZONE_ID)

    /**
     * Translated appliance names per screengrab locale.
     *
     * Each entry maps a locale tag (e.g. "nl-NL") to a triple of
     * (Washing machine, Dishwasher eco, Dishwasher quick).
     */
    private val applianceNames = mapOf(
        "en-GB" to Triple("Washing machine", "Dishwasher (eco)", "Dishwasher (quick)"),
        "bg-BG" to Triple("Пералня", "Съдомиялна (еко)", "Съдомиялна (бърза)"),
        "cs-CZ" to Triple("Pračka", "Myčka (eko)", "Myčka (rychlá)"),
        "da-DK" to Triple("Vaskemaskine", "Opvaskemaskine (øko)", "Opvaskemaskine (hurtig)"),
        "de-DE" to Triple("Waschmaschine", "Spülmaschine (Eco)", "Spülmaschine (Schnell)"),
        "el-GR" to Triple("Πλυντήριο", "Πλυντήριο πιάτων (οικο)", "Πλυντήριο πιάτων (γρήγορο)"),
        "es-ES" to Triple("Lavadora", "Lavavajillas (eco)", "Lavavajillas (rápido)"),
        "et-EE" to Triple("Pesumasin", "Nõudepesumasin (öko)", "Nõudepesumasin (kiire)"),
        "fi-FI" to Triple("Pesukone", "Astianpesukone (eko)", "Astianpesukone (pika)"),
        "fr-FR" to Triple("Lave-linge", "Lave-vaisselle (éco)", "Lave-vaisselle (rapide)"),
        "hr-HR" to Triple("Perilica rublja", "Perilica posuđa (eko)", "Perilica posuđa (brza)"),
        "hu-HU" to Triple("Mosógép", "Mosogatógép (eko)", "Mosogatógép (gyors)"),
        "it-IT" to Triple("Lavatrice", "Lavastoviglie (eco)", "Lavastoviglie (rapido)"),
        "lt-LT" to Triple("Skalbyklė", "Indaplovė (eko)", "Indaplovė (greitoji)"),
        "lv-LV" to Triple("Veļasmašīna", "Trauku mašīna (eko)", "Trauku mašīna (ātrā)"),
        "mk-MK" to Triple("Машина за перење", "Машина за садови (еко)", "Машина за садови (брза)"),
        "nb-NO" to Triple("Vaskemaskin", "Oppvaskmaskin (øko)", "Oppvaskmaskin (rask)"),
        "nl-NL" to Triple("Wasmachine", "Vaatwasser (eco)", "Vaatwasser (snel)"),
        "pl-PL" to Triple("Pralka", "Zmywarka (eko)", "Zmywarka (szybka)"),
        "pt-PT" to Triple("Máquina de lavar", "Máquina de lavar louça (eco)", "Máquina de lavar louça (rápido)"),
        "ro-RO" to Triple("Mașină de spălat", "Mașină de spălat vase (eco)", "Mașină de spălat vase (rapid)"),
        "sk-SK" to Triple("Práčka", "Umývačka (eko)", "Umývačka (rýchla)"),
        "sl-SI" to Triple("Pralni stroj", "Pomivalni stroj (eko)", "Pomivalni stroj (hitri)"),
        "sr-RS" to Triple("Машина за прање", "Машина за судове (еко)", "Машина за судове (брза)"),
        "sv-SE" to Triple("Tvättmaskin", "Diskmaskin (eko)", "Diskmaskin (snabb)")
    )

    /**
     * Returns the translated washing machine name for the current test locale.
     *
     * Used by the test to find the correct chip to tap for the result screenshot.
     */
    fun washingMachineName(): String {
        val locale = InstrumentationRegistry.getArguments().getString("testLocale") ?: "en-GB"
        return applianceNames[locale]?.first ?: applianceNames["en-GB"]!!.first
    }

    /**
     * Populates all SharedPreferences and writes a binary price cache file.
     *
     * Call this before launching the Activity so the ViewModel reads
     * pre-populated data on its first init. Does NOT set the locale —
     * call [applyTestLocale] separately after the Activity is running.
     */
    fun populate(context: Context) {
        populateSettings(context)
        populateCache(context)
    }

    /**
     * Sets the app's per-app locale to match the test locale from screengrab.
     *
     * On API 33+, changing the system locale no longer affects apps that use
     * AppCompat's per-app language. Instead, we read the `-e testLocale` argument
     * from screengrab's instrumentation runner and set it explicitly via
     * [AppCompatDelegate.setApplicationLocales].
     *
     * Must be called from the main thread after an Activity is running, because
     * on API 33+ the platform's [android.app.LocaleManager] triggers an Activity
     * recreation that requires an existing Activity.
     */
    fun applyTestLocale() {
        val args = InstrumentationRegistry.getArguments()
        val testLocale = args.getString("testLocale")
        if (testLocale != null) {
            // Use just the language part (e.g. "nl" from "nl-NL") so that
            // LanguageSection can match it against its simple-tag options
            // and display the native name (e.g. "Nederlands") instead of
            // the raw BCP 47 tag.
            val languageTag = java.util.Locale.forLanguageTag(testLocale).language
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag))
        } else {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
        }
    }

    private fun populateSettings(context: Context) {
        val locale = InstrumentationRegistry.getArguments().getString("testLocale") ?: "en-GB"
        val (washer, dishEco, dishQuick) = applianceNames[locale] ?: applianceNames["en-GB"]!!

        // Escape for JSON — handle quotes in translated names
        fun esc(s: String) = s.replace("\\", "\\\\").replace("\"", "\\\"")

        val appliancesJson = """[{"id":"a1","name":"${esc(washer)}","durationHours":2,"durationMinutes":30,"icon":"washing_machine"},{"id":"a2","name":"${esc(dishEco)}","durationHours":4,"durationMinutes":0,"icon":"dishwasher"},{"id":"a3","name":"${esc(dishQuick)}","durationHours":1,"durationMinutes":15,"icon":"dishwasher"}]"""

        context.getSharedPreferences("sweetspot_settings", Context.MODE_PRIVATE).edit {
            putString("country_code", "NL")
            putString("price_zone_id", "NL")
            putString("appliances", appliancesJson)
            putBoolean("unlocked", true)
            putBoolean("stats_prompt_shown", true)
            putBoolean("dev_options", true)
            putBoolean("use_production_logo", true)
            putLong("first_launch_ms", System.currentTimeMillis())
            putLong("time_override", fixedNow().toInstant().toEpochMilli())
        }
    }

    /**
     * Writes a v3 binary price cache file with ~48h of 15-minute resolution prices.
     *
     * The price curve has midday as the cheapest period (solar surplus pattern)
     * and morning/evening peaks as the most expensive.
     */
    private fun populateCache(context: Context) {
        val today = LocalDate.now(ZONE_ID)
        val start = today.atStartOfDay(ZONE_ID)
        val end = start.plusDays(2)

        val prices = generatePrices(start, end)

        val file = File(context.cacheDir, "prices_NL.bin")
        DataOutputStream(file.outputStream().buffered()).use { out ->
            out.writeByte(3) // version
            out.writeUTF("ENTSO-E") // source name
            out.writeInt(prices.size) // count
            for ((epochSecond, price) in prices) {
                out.writeLong(epochSecond)
                out.writeShort(15) // 15-minute resolution
                out.writeDouble(price)
            }
        }

        // Set cache cooldown so the repository doesn't re-fetch
        context.getSharedPreferences("sweetspot_cache", Context.MODE_PRIVATE).edit {
            putLong("last_fetch_ms", System.currentTimeMillis())
        }
    }

    /**
     * Generates 2 days of 15-minute resolution prices by repeating the
     * [DAILY_PRICES] template (96 slots per day).
     *
     * @return List of (epochSecond, priceEurPerKwh) pairs.
     */
    private fun generatePrices(
        start: ZonedDateTime,
        end: ZonedDateTime
    ): List<Pair<Long, Double>> {
        val prices = mutableListOf<Pair<Long, Double>>()
        var current = start
        var i = 0
        while (current.isBefore(end)) {
            prices.add(current.toInstant().epochSecond to DAILY_PRICES[i % DAILY_PRICES.size])
            current = current.plusMinutes(15)
            i++
        }
        return prices
    }

    /**
     * 96 independent 15-minute price slots (EUR/kWh) forming one full day.
     *
     * Solar-surplus pattern: expensive mornings and evenings, cheap midday.
     * Each slot has its own price — no hourly grouping. Values flow naturally
     * from one slot to the next with realistic variation.
     *
     * The cheapest window falls around 11:00–13:30 to align with the 2.5h
     * washing machine, placing the green highlight near the top of the chart
     * when the time override is 08:00.
     */
    private val DAILY_PRICES = doubleArrayOf(
        0.083, 0.076, 0.082, 0.071, 0.078, 0.068, 0.075, 0.070,  // 00:00–01:59
        0.064, 0.072, 0.060, 0.066, 0.056, 0.065, 0.053, 0.062,  // 02:00–03:59
        0.067, 0.058, 0.072, 0.081, 0.086, 0.098, 0.091, 0.115,  // 04:00–05:59
        0.128, 0.119, 0.145, 0.155, 0.164, 0.185, 0.176, 0.202,  // 06:00–07:59
        0.218, 0.209, 0.234, 0.225, 0.241, 0.221, 0.208, 0.195,  // 08:00–09:59
        0.182, 0.161, 0.173, 0.142, 0.125, 0.109, 0.094, 0.078,  // 10:00–11:59
        0.065, 0.052, 0.058, 0.038, 0.032, 0.027, 0.035, 0.029,  // 12:00–13:59
        0.041, 0.055, 0.047, 0.072, 0.088, 0.098, 0.115, 0.132,  // 14:00–15:59
        0.151, 0.165, 0.175, 0.196, 0.208, 0.219, 0.232, 0.244,  // 16:00–17:59
        0.252, 0.243, 0.255, 0.241, 0.248, 0.231, 0.222, 0.215,  // 18:00–19:59
        0.201, 0.189, 0.197, 0.178, 0.168, 0.155, 0.163, 0.141,  // 20:00–21:59
        0.131, 0.121, 0.114, 0.108, 0.098, 0.094, 0.089, 0.086   // 22:00–23:59
    )

}
