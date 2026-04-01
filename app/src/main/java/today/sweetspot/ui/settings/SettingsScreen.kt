@file:Suppress("AssignedValueIsNeverRead")

package today.sweetspot.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import today.sweetspot.BuildConfig
import kotlinx.coroutines.launch
import today.sweetspot.R
import today.sweetspot.data.api.DataSource
import today.sweetspot.model.Appliance
import today.sweetspot.model.Countries
import today.sweetspot.model.Country
import today.sweetspot.model.PriceZone
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
    onClearCache: () -> String,
    isStatsEnabled: Boolean,
    onStatsEnabledChanged: (Boolean) -> Unit,
    isUnlocked: Boolean,
    trialDaysRemaining: Int,
    productPrice: String?,
    onPurchaseClicked: () -> Unit,
    devOptionsEnabled: Boolean,
    isCooldownDisabled: Boolean,
    onDevOptionsUnlocked: () -> Unit,
    onDevResetUnlock: () -> Unit,
    onDevCooldownDisabledChanged: (Boolean) -> Unit,
    onDevResetStatsTimer: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAdvancedSettings by rememberSaveable { mutableStateOf(false) }
    var showLanguagePicker by rememberSaveable { mutableStateOf(false) }
    var showTimezonePicker by rememberSaveable { mutableStateOf(false) }
    var showCountryPicker by rememberSaveable { mutableStateOf(false) }
    var showZonePicker by rememberSaveable { mutableStateOf(false) }
    var editingAppliance by remember { mutableStateOf<Appliance?>(null) }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val defaultTimeZoneId = remember(priceZone) {
        priceZone?.timeZoneId?.let { ZoneId.of(it) } ?: ZoneId.systemDefault()
    }

    if (showAdvancedSettings) {
        BackHandler { showAdvancedSettings = false }
        AdvancedSettingsScreen(
            sourceOrder = sourceOrder,
            disabledSources = disabledSources,
            availableSources = availableSources,
            onSourceOrderChanged = onSourceOrderChanged,
            onDisabledSourcesChanged = onDisabledSourcesChanged,
            onResetSourceOrder = onResetSourceOrder,
            onClearCache = onClearCache,
            devOptionsEnabled = devOptionsEnabled,
            isCooldownDisabled = isCooldownDisabled,
            onDevCooldownDisabledChanged = onDevCooldownDisabledChanged,
            onDevResetUnlock = onDevResetUnlock,
            onDevResetStatsTimer = onDevResetStatsTimer,
            onBack = { showAdvancedSettings = false }
        )
        return
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            if (!isUnlocked) {
                UnlockSection(
                    trialDaysRemaining = trialDaysRemaining,
                    productPrice = productPrice,
                    onPurchaseClicked = onPurchaseClicked
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }

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

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            TimezoneSection(
                currentTimeZoneId = currentTimeZoneId,
                isUsingDefaultTimezone = isUsingDefaultTimezone,
                onClick = { showTimezonePicker = true }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { onStatsEnabledChanged(!isStatsEnabled) })
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_stats_title),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = stringResource(R.string.settings_stats_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isStatsEnabled,
                    onCheckedChange = onStatsEnabledChanged
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { showAdvancedSettings = true })
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_advanced),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = stringResource(R.string.settings_advanced_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            var versionTapCount by remember { mutableIntStateOf(0) }

            Text(
                text = "SweetSpot v${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (!devOptionsEnabled) {
                            versionTapCount++
                            if (versionTapCount >= 7) {
                                onDevOptionsUnlocked()
                                coroutineScope.launch { snackbarHostState.showSnackbar("Developer options enabled") }
                            }
                        }
                    }
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}
