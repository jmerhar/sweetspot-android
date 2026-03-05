package si.merhar.sweetspot.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import si.merhar.sweetspot.R
import si.merhar.sweetspot.data.api.DataSource
import si.merhar.sweetspot.model.Appliance
import si.merhar.sweetspot.model.Countries
import si.merhar.sweetspot.model.Country
import si.merhar.sweetspot.model.PriceZone
import java.time.ZoneId

/**
 * Settings screen with appliance management, language selection, country/zone selection,
 * data source preferences, and timezone selection. Manages its own sub-navigation: tapping rows
 * opens picker screens for country, zone, or timezone.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentTimeZoneId: ZoneId,
    isUsingDefaultTimezone: Boolean,
    onTimezoneSelected: (ZoneId?) -> Unit,
    appliances: List<Appliance>,
    onAddAppliance: (name: String, durationHours: Int, durationMinutes: Int, icon: String) -> Unit,
    onUpdateAppliance: (Appliance) -> Unit,
    onDeleteAppliance: (id: String) -> Unit,
    countryCode: String,
    priceZone: PriceZone?,
    countries: List<Country>,
    onCountrySelected: (String) -> Unit,
    onPriceZoneSelected: (String) -> Unit,
    sourceOrder: List<String>?,
    disabledSources: Set<String>,
    availableSources: List<DataSource>,
    onSourceOrderChanged: (List<String>) -> Unit,
    onDisabledSourcesChanged: (Set<String>) -> Unit,
    onResetSourceOrder: () -> Unit,
    onLanguageChanged: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLanguagePicker by rememberSaveable { mutableStateOf(false) }
    var showTimezonePicker by rememberSaveable { mutableStateOf(false) }
    var showCountryPicker by rememberSaveable { mutableStateOf(false) }
    var showZonePicker by rememberSaveable { mutableStateOf(false) }
    var editingAppliance by remember { mutableStateOf<Appliance?>(null) }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    val defaultTimeZoneId = remember(priceZone) {
        priceZone?.timeZoneId?.let { ZoneId.of(it) } ?: ZoneId.systemDefault()
    }

    if (showLanguagePicker) {
        BackHandler { showLanguagePicker = false }
        LanguagePickerScreen(
            onLanguageChanged = { tag ->
                onLanguageChanged(tag)
                showLanguagePicker = false
            },
            onBack = { showLanguagePicker = false }
        )
        return
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

    if (showAddDialog) {
        ApplianceDialog(
            appliance = null,
            onSave = { name, durationHours, durationMinutes, icon ->
                onAddAppliance(name, durationHours, durationMinutes, icon)
                showAddDialog = false
            },
            onDelete = null,
            onDismiss = { showAddDialog = false }
        )
    }

    editingAppliance?.let { appliance ->
        ApplianceDialog(
            appliance = appliance,
            onSave = { name, durationHours, durationMinutes, icon ->
                onUpdateAppliance(appliance.copy(name = name, durationHours = durationHours, durationMinutes = durationMinutes, icon = icon))
                editingAppliance = null
            },
            onDelete = {
                onDeleteAppliance(appliance.id)
                editingAppliance = null
            },
            onDismiss = { editingAppliance = null }
        )
    }

    val currentCountry = remember(countryCode) { Countries.findByCode(countryCode) }
    val isMultiZone = (currentCountry?.zones?.size ?: 0) > 1

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            AppliancesSection(
                appliances = appliances,
                onApplianceClick = { editingAppliance = it },
                onAddClick = { showAddDialog = true }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            LanguageSection(onClick = { showLanguagePicker = true })

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

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

            if (availableSources.size >= 2) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                DataSourcesSection(
                    sourceOrder = sourceOrder,
                    disabledSources = disabledSources,
                    availableSources = availableSources,
                    onSourceOrderChanged = onSourceOrderChanged,
                    onDisabledSourcesChanged = onDisabledSourcesChanged,
                    onResetSourceOrder = onResetSourceOrder
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            TimezoneSection(
                currentTimeZoneId = currentTimeZoneId,
                isUsingDefaultTimezone = isUsingDefaultTimezone,
                onClick = { showTimezonePicker = true }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}
