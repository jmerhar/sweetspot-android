package si.merhar.sweetspot.ui

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
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
 * Settings screen with appliance management, country/zone selection, and timezone selection.
 * Manages its own sub-navigation: tapping rows opens picker screens for country, zone, or timezone.
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
    priceZone: PriceZone,
    countries: List<Country>,
    onCountrySelected: (String) -> Unit,
    onPriceZoneSelected: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showTimezonePicker by rememberSaveable { mutableStateOf(false) }
    var showCountryPicker by rememberSaveable { mutableStateOf(false) }
    var showZonePicker by rememberSaveable { mutableStateOf(false) }
    var editingAppliance by remember { mutableStateOf<Appliance?>(null) }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    if (showTimezonePicker) {
        TimezonePickerScreen(
            currentTimeZoneId = currentTimeZoneId,
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
        CountryPickerScreen(
            countries = countries,
            currentCountryCode = countryCode,
            onCountrySelected = { code ->
                onCountrySelected(code)
                showCountryPicker = false
            },
            onBack = { showCountryPicker = false }
        )
        return
    }

    if (showZonePicker) {
        val country = Countries.findByCode(countryCode)
        if (country != null && country.zones.size > 1) {
            PriceZonePickerScreen(
                zones = country.zones,
                currentPriceZoneId = priceZone.id,
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
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
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

            CountrySection(
                countryName = currentCountry?.name ?: "Unknown",
                onClick = { showCountryPicker = true }
            )

            if (isMultiZone) {
                PriceZoneSection(
                    zoneLabel = priceZone.label,
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
        text = "Country",
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

/** Price zone sub-section for multi-zone countries, showing the current zone. */
@Composable
private fun PriceZoneSection(
    zoneLabel: String,
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
                text = zoneLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
    Text(
        text = "Appliances",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )

    Text(
        text = "Add your home appliances here for easy access. You can also add specific programmes, e.g. Dishwasher Eco, Dishwasher Quick, Washing Machine Cotton.",
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
                    text = formatDuration(appliance.durationHours, appliance.durationMinutes),
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
            text = "Add appliance",
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
        text = "Timezone",
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
                text = if (isUsingDefaultTimezone) "Auto (from country)" else currentTimeZoneId.id.replace('_', ' '),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = if (isUsingDefaultTimezone) currentTimeZoneId.id.replace('_', ' ') else "Custom timezone",
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
                Text(if (appliance == null) "Add appliance" else "Edit appliance")
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text(
                            text = "Delete",
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
                    label = { Text("Name") },
                    placeholder = { Text("e.g. Washing machine") },
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
                    text = "Icon",
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
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/** Full-screen picker listing all available timezones with a search field and system-default option. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimezonePickerScreen(
    currentTimeZoneId: ZoneId,
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
                title = { Text("Timezone") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
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
                placeholder = { Text("Search timezones") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // Auto (from country) option
                item {
                    PickerRow(
                        label = "Auto (from country)",
                        subtitle = currentTimeZoneId.id.replace('_', ' '),
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

    val filteredCountries = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            countries
        } else {
            val query = searchQuery.lowercase()
            countries.filter { it.name.lowercase().contains(query) || it.code.lowercase().contains(query) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Country") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
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
                placeholder = { Text("Search countries") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredCountries) { country ->
                    val zoneCount = country.zones.size
                    PickerRow(
                        label = country.name,
                        subtitle = if (zoneCount > 1) "$zoneCount zones" else null,
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
                title = { Text("Zone") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
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
                    label = zone.label,
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
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
