package si.merhar.sweetspot.ui

import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import si.merhar.sweetspot.R
import si.merhar.sweetspot.data.api.DataSource
import si.merhar.sweetspot.data.api.DataSources
import si.merhar.sweetspot.model.Appliance
import si.merhar.sweetspot.model.Countries
import si.merhar.sweetspot.model.Country
import si.merhar.sweetspot.model.PriceZone
import si.merhar.sweetspot.model.applianceIconFor
import si.merhar.sweetspot.model.applianceIcons
import si.merhar.sweetspot.ui.components.DurationPicker
import si.merhar.sweetspot.util.formatDuration
import java.time.ZoneId

/**
 * Available language options for the language picker.
 *
 * @property tag BCP 47 language tag (empty string = system default).
 * @property displayName Human-readable language name shown in the picker.
 */
private data class LanguageOption(val tag: String, val displayName: String)

private val languageOptions = listOf(
    LanguageOption("", ""),    // System default — label resolved from string resource
    LanguageOption("en", "English"),
    LanguageOption("nl", "Nederlands"),
    LanguageOption("de", "Deutsch"),
    LanguageOption("fr", "Français")
)

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
    var showTimezonePicker by rememberSaveable { mutableStateOf(false) }
    var showCountryPicker by rememberSaveable { mutableStateOf(false) }
    var showZonePicker by rememberSaveable { mutableStateOf(false) }
    var editingAppliance by remember { mutableStateOf<Appliance?>(null) }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    val defaultTimeZoneId = remember(priceZone) {
        priceZone?.timeZoneId?.let { ZoneId.of(it) } ?: ZoneId.systemDefault()
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

            LanguageSection(onLanguageChanged = onLanguageChanged)

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

/**
 * Language settings section with a selectable list of supported languages.
 *
 * Uses [AppCompatDelegate.setApplicationLocales] for per-app locale switching.
 * The activity automatically recreates when the locale changes.
 *
 * @param onLanguageChanged Callback to sync the new language tag to the watch.
 */
@Composable
private fun LanguageSection(onLanguageChanged: (String) -> Unit) {
    val systemDefaultLabel = stringResource(R.string.settings_language_system_default)
    val currentLocales = AppCompatDelegate.getApplicationLocales()
    val currentTag = if (currentLocales.isEmpty) "" else currentLocales.toLanguageTags()

    Text(
        text = stringResource(R.string.settings_language),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )

    languageOptions.forEach { option ->
        val displayName = if (option.tag.isEmpty()) systemDefaultLabel else option.displayName
        val isSelected = option.tag == currentTag

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onLanguageChanged(option.tag)
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(option.tag)
                    )
                }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.cd_selected),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/** Country settings section showing the current country selection. */
@Composable
private fun CountrySection(
    countryName: String,
    onClick: () -> Unit
) {
    Text(
        text = stringResource(R.string.settings_country),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = countryName,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/** Price zone sub-section for multi-zone countries, showing the current zone or a prompt to select one. */
@Composable
private fun PriceZoneSection(
    zoneLabel: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = zoneLabel ?: stringResource(R.string.settings_select_zone),
                style = MaterialTheme.typography.bodyMedium,
                color = if (zoneLabel != null) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * Data sources settings section for configuring API priority order.
 *
 * Shows each available source with a toggle switch and up/down reorder buttons.
 * Sources maintain their position when toggled — disabling a source does not move it.
 * The last enabled source's switch is disabled to prevent disabling all sources.
 * A "Reset to defaults" button appears when the configuration differs from zone defaults.
 */
@Composable
private fun DataSourcesSection(
    sourceOrder: List<String>?,
    disabledSources: Set<String>,
    availableSources: List<DataSource>,
    onSourceOrderChanged: (List<String>) -> Unit,
    onDisabledSourcesChanged: (Set<String>) -> Unit,
    onResetSourceOrder: () -> Unit
) {
    val defaults = availableSources.map { it.id }
    val displayOrder = sourceOrder?.filter { id -> availableSources.any { it.id == id } } ?: defaults
    val enabledIds = displayOrder.filter { it !in disabledSources }

    Text(
        text = stringResource(R.string.settings_data_sources),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )

    Text(
        text = stringResource(R.string.settings_data_sources_description),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp)
    )

    displayOrder.forEachIndexed { index, sourceId ->
        val source = availableSources.find { it.id == sourceId } ?: return@forEachIndexed
        val isEnabled = sourceId !in disabledSources
        val isLastEnabled = isEnabled && enabledIds.size == 1

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = isEnabled,
                onCheckedChange = { checked ->
                    if (checked) {
                        onDisabledSourcesChanged(disabledSources - sourceId)
                    } else if (!isLastEnabled) {
                        onDisabledSourcesChanged(disabledSources + sourceId)
                    }
                },
                enabled = !isLastEnabled
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = source.displayName,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                color = if (isEnabled) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
            IconButton(
                onClick = {
                    if (index > 0) {
                        val reordered = displayOrder.toMutableList()
                        reordered.removeAt(index)
                        reordered.add(index - 1, sourceId)
                        onSourceOrderChanged(reordered)
                    }
                },
                enabled = index > 0
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = stringResource(R.string.cd_move_up)
                )
            }
            IconButton(
                onClick = {
                    if (index < displayOrder.size - 1) {
                        val reordered = displayOrder.toMutableList()
                        reordered.removeAt(index)
                        reordered.add(index + 1, sourceId)
                        onSourceOrderChanged(reordered)
                    }
                },
                enabled = index < displayOrder.size - 1
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.cd_move_down)
                )
            }
        }
    }

    if (sourceOrder != null || disabledSources.isNotEmpty()) {
        val isCustomized = (sourceOrder != null && sourceOrder != defaults) || disabledSources.isNotEmpty()
        if (isCustomized) {
            TextButton(
                onClick = onResetSourceOrder,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(stringResource(R.string.settings_reset_defaults))
            }
        }
    }
}

/** Appliances settings section with description, appliance list, and add button. */
@Composable
private fun AppliancesSection(
    appliances: List<Appliance>,
    onApplianceClick: (Appliance) -> Unit,
    onAddClick: () -> Unit
) {
    val resources = LocalContext.current.resources

    Text(
        text = stringResource(R.string.settings_appliances),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )

    Text(
        text = stringResource(R.string.settings_appliances_description),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp)
    )

    appliances.forEach { appliance ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {}
                .clickable { onApplianceClick(appliance) }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = applianceIconFor(appliance.icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appliance.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = formatDuration(appliance.durationHours, appliance.durationMinutes, resources),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAddClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.settings_add_appliance),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/** Timezone settings section showing the current timezone selection. */
@Composable
private fun TimezoneSection(
    currentTimeZoneId: ZoneId,
    isUsingDefaultTimezone: Boolean,
    onClick: () -> Unit
) {
    Text(
        text = stringResource(R.string.settings_timezone),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isUsingDefaultTimezone) stringResource(R.string.settings_auto_timezone) else currentTimeZoneId.id.replace('_', ' '),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = if (isUsingDefaultTimezone) currentTimeZoneId.id.replace('_', ' ') else stringResource(R.string.settings_custom_timezone),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Dialog for creating or editing an appliance with name, duration, and icon fields. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ApplianceDialog(
    appliance: Appliance?,
    onSave: (name: String, durationHours: Int, durationMinutes: Int, icon: String) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf(appliance?.name ?: "") }
    var pickerHours by rememberSaveable { mutableStateOf(appliance?.durationHours ?: 1) }
    var pickerMinutes by rememberSaveable { mutableStateOf(appliance?.durationMinutes ?: 0) }
    var selectedIcon by rememberSaveable { mutableStateOf(appliance?.icon ?: "bolt") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (appliance == null) stringResource(R.string.dialog_add_appliance) else stringResource(R.string.dialog_edit_appliance))
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text(
                            text = stringResource(R.string.action_delete),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.dialog_name)) },
                    placeholder = { Text(stringResource(R.string.dialog_name_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                DurationPicker(
                    hours = pickerHours,
                    minutes = pickerMinutes,
                    onChanged = { h, m -> pickerHours = h; pickerMinutes = m }
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.dialog_icon),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    applianceIcons.forEach { entry ->
                        val isSelected = entry.id == selectedIcon
                        val shape = RoundedCornerShape(8.dp)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(shape)
                                .then(
                                    if (isSelected) Modifier.border(
                                        2.dp,
                                        MaterialTheme.colorScheme.primary,
                                        shape
                                    ) else Modifier
                                )
                                .clickable { selectedIcon = entry.id },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = entry.icon,
                                contentDescription = entry.label,
                                modifier = Modifier.size(22.dp),
                                tint = if (isSelected) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name.trim(), pickerHours, pickerMinutes, selectedIcon) },
                enabled = name.isNotBlank() && (pickerHours > 0 || pickerMinutes > 0)
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

/** Full-screen picker listing all available timezones with a search field and system-default option. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimezonePickerScreen(
    currentTimeZoneId: ZoneId,
    defaultTimeZoneId: ZoneId,
    isUsingDefaultTimezone: Boolean,
    onTimezoneSelected: (ZoneId?) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val allZoneIds = remember {
        ZoneId.getAvailableZoneIds()
            .filter { it.contains('/') }
            .sorted()
    }

    val filteredZones = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            allZoneIds
        } else {
            val query = searchQuery.lowercase()
            allZoneIds.filter {
                val lower = it.lowercase()
                lower.contains(query) || lower.replace('_', ' ').contains(query)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.picker_timezone_title)) },
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
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(stringResource(R.string.picker_timezone_search)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // Auto (from country) option
                item {
                    PickerRow(
                        label = stringResource(R.string.settings_auto_timezone),
                        subtitle = defaultTimeZoneId.id.replace('_', ' '),
                        isSelected = isUsingDefaultTimezone,
                        onClick = { onTimezoneSelected(null) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }

                items(filteredZones) { zoneIdStr ->
                    val isSelected = !isUsingDefaultTimezone && currentTimeZoneId.id == zoneIdStr
                    PickerRow(
                        label = zoneIdStr.replace('_', ' '),
                        subtitle = null,
                        isSelected = isSelected,
                        onClick = { onTimezoneSelected(ZoneId.of(zoneIdStr)) }
                    )
                }
            }
        }
    }
}

/** Full-screen country picker with search. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountryPickerScreen(
    countries: List<Country>,
    currentCountryCode: String,
    onCountrySelected: (String) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current

    val sortedCountries = remember(countries) {
        countries.sortedBy { context.getString(it.nameRes).lowercase() }
    }

    val filteredCountries = remember(searchQuery, sortedCountries) {
        if (searchQuery.isBlank()) {
            sortedCountries
        } else {
            val query = searchQuery.lowercase()
            sortedCountries.filter {
                context.getString(it.nameRes).lowercase().contains(query) || it.code.lowercase().contains(query)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.picker_country_title)) },
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
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(stringResource(R.string.picker_country_search)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredCountries) { country ->
                    val zoneCount = country.zones.size
                    PickerRow(
                        label = stringResource(country.nameRes),
                        subtitle = if (zoneCount > 1) stringResource(R.string.picker_zones_count, zoneCount) else null,
                        isSelected = country.code == currentCountryCode,
                        onClick = { onCountrySelected(country.code) }
                    )
                }
            }
        }
    }
}

/** Price zone picker for multi-zone countries. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PriceZonePickerScreen(
    zones: List<PriceZone>,
    currentPriceZoneId: String,
    onPriceZoneSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.picker_zone_title)) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(zones) { zone ->
                PickerRow(
                    label = stringResource(zone.labelRes),
                    subtitle = null,
                    isSelected = zone.id == currentPriceZoneId,
                    onClick = { onPriceZoneSelected(zone.id) }
                )
            }
        }
    }
}

/** Single row in a picker showing a label, optional subtitle, and a check icon when selected. */
@Composable
private fun PickerRow(
    label: String,
    subtitle: String?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (isSelected) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.cd_selected),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
