package today.sweetspot.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import today.sweetspot.R
import today.sweetspot.model.SupplierTariff

/**
 * Sub-screen for the all-in "total price" setting: the enable toggle, supplier selection (opens
 * [SupplierPickerScreen]), and the manual per-kWh surcharge field.
 *
 * Reached only when the country has a usable tariff feed (the menu row is gated on `allInSupported`),
 * so the exit guard here fires when all-in is enabled but has neither a supplier nor a surcharge:
 * leaving is blocked and a snackbar reminds the user to pick one (or turn the total price off).
 *
 * @param enabled Whether the all-in display is on.
 * @param suppliers Suppliers from the tariff feed.
 * @param selectedSupplierId Chosen supplier id, or null.
 * @param manualSurcharge Manual per-kWh surcharge override, or null.
 * @param currencyCode ISO 4217 currency of the feed (labels the surcharge field).
 * @param onEnabledChange Called when the toggle flips.
 * @param onSupplierSelected Called with the chosen supplier id.
 * @param onManualSurchargeChanged Called with the parsed surcharge, or null when cleared.
 * @param onBack Called to return to the settings menu (blocked while the setup is incomplete).
 */
@Composable
internal fun TotalPriceSettingsScreen(
    enabled: Boolean,
    suppliers: List<SupplierTariff>,
    selectedSupplierId: String?,
    manualSurcharge: Double?,
    currencyCode: String,
    onEnabledChange: (Boolean) -> Unit,
    onSupplierSelected: (String) -> Unit,
    onManualSurchargeChanged: (Double?) -> Unit,
    onBack: () -> Unit
) {
    var showSupplierPicker by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Enabled but nothing to apply (no supplier, no surcharge) — block leaving until resolved.
    val incomplete = enabled && selectedSupplierId == null && manualSurcharge == null
    val incompleteMessage = stringResource(R.string.all_in_incomplete)
    val attemptBack: () -> Unit = {
        if (incomplete) {
            coroutineScope.launch { snackbarHostState.showSnackbar(incompleteMessage) }
        } else {
            onBack()
        }
    }

    if (showSupplierPicker) {
        BackHandler { showSupplierPicker = false }
        SupplierPickerScreen(
            suppliers = suppliers,
            selectedSupplierId = selectedSupplierId,
            currencyCode = currencyCode,
            onSupplierSelected = { id ->
                onSupplierSelected(id)
                showSupplierPicker = false
            },
            onBack = { showSupplierPicker = false }
        )
        return
    }

    // Back is gated by `attemptBack` — SettingsSubScreen wires it to both the arrow and system back.
    SettingsSubScreen(
        title = stringResource(R.string.settings_all_in_title),
        onBack = attemptBack,
        snackbarHostState = snackbarHostState
    ) {
        AllInSection(
            enabled = enabled,
            suppliers = suppliers,
            selectedSupplierId = selectedSupplierId,
            manualSurcharge = manualSurcharge,
            currencyCode = currencyCode,
            onEnabledChange = onEnabledChange,
            onSupplierClick = { showSupplierPicker = true },
            onManualSurchargeChange = onManualSurchargeChanged
        )
    }
}
