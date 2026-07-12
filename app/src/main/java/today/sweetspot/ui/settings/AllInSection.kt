package today.sweetspot.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import today.sweetspot.R
import today.sweetspot.model.SupplierTariff
import today.sweetspot.util.currencySymbol
import today.sweetspot.util.formatPrice

/**
 * All-in price settings section: a toggle to show the approximate all-in consumer price, plus (when
 * enabled) the user's supplier selection and an optional manual per-kWh surcharge override.
 *
 * Shown only when the selected country has a usable tariff feed (the caller gates on that). All-in
 * requires an explicit surcharge — a chosen supplier or a manual value — so when enabled with neither
 * set, the supplier row shows an error-coloured prompt.
 *
 * @param enabled Whether the all-in display is on.
 * @param suppliers Suppliers from the tariff feed (for the picker and to resolve the selected name).
 * @param selectedSupplierId The chosen supplier id, or null.
 * @param manualSurcharge Manual per-kWh surcharge override (ex-VAT), or null. Overrides the supplier.
 * @param currencyCode ISO 4217 currency of the tariff feed, used to label the surcharge field.
 * @param onEnabledChange Called when the toggle flips.
 * @param onSupplierClick Called to open the supplier picker.
 * @param onManualSurchargeChange Called with the parsed override, or null when cleared.
 */
@Composable
internal fun AllInSection(
    enabled: Boolean,
    suppliers: List<SupplierTariff>,
    selectedSupplierId: String?,
    manualSurcharge: Double?,
    currencyCode: String,
    onEnabledChange: (Boolean) -> Unit,
    onSupplierClick: () -> Unit,
    onManualSurchargeChange: (Double?) -> Unit
) {
    Text(
        text = stringResource(R.string.settings_all_in_title),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )

    // Enable toggle (label + description + Switch), matching the stats opt-in row.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEnabledChange(!enabled) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.settings_all_in_toggle), style = MaterialTheme.typography.bodyLarge)
            Text(
                text = stringResource(R.string.settings_all_in_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = enabled, onCheckedChange = onEnabledChange)
    }

    if (enabled) {
        val selectedName = suppliers.firstOrNull { it.id == selectedSupplierId }?.name

        // Supplier row — chosen supplier, "Custom surcharge" once a value is typed, or an
        // error-coloured prompt when neither is set.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onSupplierClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.all_in_supplier), style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = when {
                        selectedName != null -> selectedName
                        manualSurcharge != null -> stringResource(R.string.all_in_supplier_custom)
                        else -> stringResource(R.string.all_in_supplier_prompt)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selectedName == null && manualSurcharge == null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }

        // Surcharge field — the effective per-kWh surcharge. Picking a supplier prefills it (its local
        // text is keyed on the selected supplier, so a pick re-seeds it); editing it clears the supplier.
        var manualText by rememberSaveable(selectedSupplierId) {
            mutableStateOf(manualSurcharge?.toString() ?: "")
        }
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = manualText,
                onValueChange = { text ->
                    manualText = text
                    if (text.isBlank()) {
                        onManualSurchargeChange(null)
                    } else {
                        text.replace(',', '.').toDoubleOrNull()?.let { onManualSurchargeChange(it) }
                    }
                },
                label = { Text(stringResource(R.string.all_in_surcharge_custom)) },
                // Currency comes from the feed (e.g. "€/kWh") so the unit is unambiguous.
                suffix = { Text("${currencySymbol(currencyCode)}${stringResource(R.string.result_per_kwh)}") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
        }
    }
}

/**
 * Full-screen supplier picker: search + a list showing each supplier's per-kWh surcharge.
 *
 * @param currencyCode ISO 4217 currency of the feed, used to format each supplier's surcharge.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SupplierPickerScreen(
    suppliers: List<SupplierTariff>,
    selectedSupplierId: String?,
    currencyCode: String,
    onSupplierSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val perKwh = stringResource(R.string.result_per_kwh)

    val sorted = suppliers.sortedBy { it.name.lowercase() }
    val filtered = if (searchQuery.isBlank()) {
        sorted
    } else {
        val query = searchQuery.lowercase()
        sorted.filter { it.name.lowercase().contains(query) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.picker_supplier_title)) },
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
                placeholder = { Text(stringResource(R.string.picker_supplier_search)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            HorizontalDivider()
            if (searchQuery.isNotBlank() && filtered.isEmpty()) {
                // No match — nudge the user to type a custom surcharge (like the vehicle search does).
                Text(
                    text = stringResource(R.string.all_in_no_supplier),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filtered) { supplier ->
                    PickerRow(
                        label = supplier.name,
                        subtitle = "${formatPrice(supplier.surchargePerKwh, 4, currencyCode)}$perKwh",
                        isSelected = supplier.id == selectedSupplierId,
                        onClick = { onSupplierSelected(supplier.id) }
                    )
                }
            }
        }
    }
}
