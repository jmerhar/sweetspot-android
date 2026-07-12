@file:Suppress("AssignedValueIsNeverRead")

package today.sweetspot.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import today.sweetspot.R
import today.sweetspot.model.Countries
import today.sweetspot.model.Country
import today.sweetspot.model.PriceZone
import java.time.ZoneId

/**
 * Sub-screen for region settings: country, price zone (multi-zone countries only), and timezone. Owns
 * the country/zone/timezone picker sub-screens and preserves the country→zone auto-advance (selecting a
 * multi-zone country opens the zone picker).
 *
 * @param countryCode Current ISO country code.
 * @param priceZone Current price zone, or null.
 * @param countries All selectable countries.
 * @param onCountrySelected Called with the chosen country code.
 * @param onPriceZoneSelected Called with the chosen price zone id.
 * @param currentTimeZoneId Effective timezone.
 * @param isUsingDefaultTimezone Whether the timezone is derived from the country (not overridden).
 * @param onTimezoneSelected Called with a timezone, or null to revert to the country default.
 * @param onBack Called to return to the settings menu.
 */
@Composable
internal fun RegionSettingsScreen(
    countryCode: String,
    priceZone: PriceZone?,
    countries: List<Country>,
    onCountrySelected: (String) -> Unit,
    onPriceZoneSelected: (String) -> Unit,
    currentTimeZoneId: ZoneId,
    isUsingDefaultTimezone: Boolean,
    onTimezoneSelected: (ZoneId?) -> Unit,
    onBack: () -> Unit
) {
    var showCountryPicker by rememberSaveable { mutableStateOf(false) }
    var showZonePicker by rememberSaveable { mutableStateOf(false) }
    var showTimezonePicker by rememberSaveable { mutableStateOf(false) }

    val defaultTimeZoneId = remember(priceZone) {
        priceZone?.timeZoneId?.let { ZoneId.of(it) } ?: ZoneId.systemDefault()
    }

    if (showCountryPicker) {
        BackHandler { showCountryPicker = false }
        CountryPickerScreen(
            countries = countries,
            currentCountryCode = countryCode,
            onCountrySelected = { code ->
                onCountrySelected(code)
                showCountryPicker = false
                val selected = Countries.findByCode(code)
                if (selected != null && selected.zones.size > 1) {
                    showZonePicker = true
                }
            },
            onBack = { showCountryPicker = false }
        )
        return
    }

    if (showZonePicker) {
        BackHandler { showZonePicker = false }
        val country = Countries.findByCode(countryCode)
        if (country != null && country.zones.size > 1) {
            PriceZonePickerScreen(
                zones = country.zones,
                currentPriceZoneId = priceZone?.id ?: "",
                onPriceZoneSelected = { priceZoneId ->
                    onPriceZoneSelected(priceZoneId)
                    showZonePicker = false
                },
                onBack = { showZonePicker = false }
            )
            return
        } else {
            showZonePicker = false
        }
    }

    if (showTimezonePicker) {
        BackHandler { showTimezonePicker = false }
        TimezonePickerScreen(
            currentTimeZoneId = currentTimeZoneId,
            defaultTimeZoneId = defaultTimeZoneId,
            isUsingDefaultTimezone = isUsingDefaultTimezone,
            onTimezoneSelected = { timeZoneId ->
                onTimezoneSelected(timeZoneId)
                showTimezonePicker = false
            },
            onBack = { showTimezonePicker = false }
        )
        return
    }

    val currentCountry = remember(countryCode) { Countries.findByCode(countryCode) }
    val isMultiZone = (currentCountry?.zones?.size ?: 0) > 1

    SettingsSubScreen(title = stringResource(R.string.settings_region_title), onBack = onBack) {
        CountrySection(
            countryName = currentCountry?.let { stringResource(it.nameRes) }
                ?: stringResource(R.string.settings_unknown_country),
            onClick = { showCountryPicker = true }
        )

        if (isMultiZone) {
            PriceZoneSection(
                zoneLabel = priceZone?.let { stringResource(it.labelRes) },
                onClick = { showZonePicker = true }
            )
        }

        TimezoneSection(
            currentTimeZoneId = currentTimeZoneId,
            isUsingDefaultTimezone = isUsingDefaultTimezone,
            onClick = { showTimezonePicker = true }
        )
    }
}
