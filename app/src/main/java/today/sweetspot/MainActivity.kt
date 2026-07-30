package today.sweetspot

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModelProvider
import today.sweetspot.data.api.DataSources
import today.sweetspot.ui.PaywallScreen
import today.sweetspot.ui.onboarding.OnboardingScreen
import today.sweetspot.ui.settings.SettingsScreen
import today.sweetspot.ui.SweetSpotScreen
import today.sweetspot.ui.share.ImportPreviewScreen
import today.sweetspot.ui.theme.SweetSpotTheme
import today.sweetspot.data.repository.SettingsRepository

/**
 * Entry point for the phone app.
 *
 * Hosts the [SweetSpotTheme] and switches between [SweetSpotScreen] and [SettingsScreen]
 * based on [SweetSpotViewModel] state. Also shows a one-time stats opt-in dialog.
 * Re-queries subscription state on every resume to detect expiry.
 */
class MainActivity : AppCompatActivity() {

    private val viewModel: SweetSpotViewModel by lazy {
        ViewModelProvider(this)[SweetSpotViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Apply stored theme before setContent so the first frame uses the correct mode
        val themeMode = ThemeMode.fromKey(SettingsRepository(this).getThemeMode())
        AppCompatDelegate.setDefaultNightMode(themeMode.nightMode)

        // A cold start from a scanned/tapped setup link arrives as the launch intent.
        handleImportIntent(intent)

        setContent {
            SweetSpotTheme {
                val vm = viewModel
                val state by vm.uiState.collectAsState()

                when {
                    state.importPreview != null -> {
                        ImportPreviewScreen(
                            setup = state.importPreview!!,
                            onImport = vm::onImportConfirmed,
                            onCancel = vm::onDismissImport
                        )
                    }
                    // First-launch gate: the intro replaces the home screen (home isn't composed
                    // underneath). When replayed from Settings › Help the settings screen stays
                    // composed and the intro is overlaid on top instead (see the settings branch),
                    // so finishing the slides returns to the exact Help screen it was launched from.
                    state.showOnboarding && !state.showSettings -> {
                        OnboardingScreen(onFinish = vm::onOnboardingComplete)
                    }
                    state.showPaywall -> {
                        PaywallScreen(
                            productPrice = state.productPrice,
                            onPurchaseClicked = { vm.onPurchaseClicked(this@MainActivity) },
                            onRestorePurchases = vm::onRestorePurchases
                        )
                    }
                    state.showSettings -> {
                      // Keep the settings screen composed while the replayed intro is showing so its
                      // navigation (the Help sub-route) survives; the intro is overlaid below.
                      Box {
                        // System back is handled inside SettingsScreen so it can gate leaving (e.g. an
                        // incomplete all-in setup) and close its own picker sub-screens first.
                        SettingsScreen(
                            themeMode = state.themeMode,
                            onThemeModeChanged = vm::onThemeModeChanged,
                            currentTimeZoneId = state.timeZoneId,
                            isUsingDefaultTimezone = state.isUsingDefaultTimezone,
                            onTimezoneSelected = vm::onTimezoneSelected,
                            appliances = state.appliances,
                            sortedAppliances = state.sortedAppliances,
                            applianceSort = state.applianceSort,
                            usage = state.usage,
                            onAddAppliance = vm::onAddAppliance,
                            onUpdateAppliance = vm::onUpdateAppliance,
                            onDeleteAppliance = vm::onDeleteAppliance,
                            onApplianceSortChanged = vm::onApplianceSortChanged,
                            onReorderAppliances = vm::onReorderAppliances,
                            onResetUsage = vm::onPurgeUsage,
                            applianceGrouping = state.applianceGrouping,
                            onApplianceGroupingChanged = vm::onApplianceGroupingChanged,
                            evHomeChargerKw = state.evHomeChargerKw,
                            evDefaultTargetSoc = state.evDefaultTargetSoc,
                            onEvHomeChargerChanged = vm::onEvHomeChargerChanged,
                            onEvDefaultTargetChanged = vm::onEvDefaultTargetChanged,
                            searchVehicles = vm::searchEvVehicles,
                            onAddVehicle = vm::onAddVehicle,
                            evPosition = state.evPosition,
                            evSeparate = state.evSeparate,
                            onEvPositionChanged = vm::onEvPositionChanged,
                            onEvSeparateChanged = vm::onEvSeparateChanged,
                            countryCode = state.countryCode,
                            priceZone = state.priceZone,
                            countries = state.countries,
                            onCountrySelected = vm::onCountrySelected,
                            onPriceZoneSelected = vm::onPriceZoneSelected,
                            allInSupported = state.allInSupported,
                            allInEnabled = state.allInEnabled,
                            allInSuppliers = state.allInTariff?.suppliers ?: emptyList(),
                            selectedSupplierId = state.supplierId,
                            manualSurcharge = state.manualSurcharge,
                            allInCurrency = state.allInCurrency,
                            onAllInEnabledChanged = vm::onAllInEnabledChanged,
                            onSupplierSelected = vm::onSupplierSelected,
                            onManualSurchargeChanged = vm::onManualSurchargeChanged,
                            sourceOrder = state.sourceOrder,
                            disabledSources = state.disabledSources,
                            availableSources = DataSources.defaultsForZone(state.priceZone?.id ?: ""),
                            onSourceOrderChanged = vm::onSourceOrderChanged,
                            onDisabledSourcesChanged = vm::onDisabledSourcesChanged,
                            onResetSourceOrder = vm::onResetSourceOrder,
                            onLanguageChanged = vm::onLanguageChanged,
                            onShareSetup = vm::onShareSetup,
                            onReplayOnboarding = vm::onReplayOnboarding,
                            reportSubmission = state.reportSubmission,
                            myReports = state.myReports,
                            reportThread = state.thread,
                            replySubmission = state.replySubmission,
                            onSubmitReport = vm::onSubmitReport,
                            onDismissReportResult = vm::onDismissReportResult,
                            onLoadMyReports = vm::loadMyReports,
                            onFlushOutbox = vm::flushOutbox,
                            onFlushReplyOutbox = vm::flushReplyOutbox,
                            onOpenThread = vm::onOpenThread,
                            onCloseThread = vm::onCloseThread,
                            onSendReply = vm::onSendReply,
                            onClearCache = vm::onClearCache,
                            isStatsEnabled = state.isStatsEnabled,
                            onStatsEnabledChanged = vm::onStatsEnabledChanged,
                            isUnlocked = state.isUnlocked,
                            trialDaysRemaining = state.trialDaysRemaining,
                            productPrice = state.productPrice,
                            onPurchaseClicked = { vm.onPurchaseClicked(this@MainActivity) },
                            devOptionsEnabled = state.devOptionsEnabled,
                            isCooldownDisabled = state.isCooldownDisabled,
                            onDevOptionsUnlocked = vm::onDevOptionsUnlocked,
                            onDevResetUnlock = vm::onDevResetUnlock,
                            onDevCooldownDisabledChanged = vm::onDevCooldownDisabledChanged,
                            isDevUnlocked = state.isDevUnlocked,
                            onDevUnlockChanged = vm::onDevUnlockChanged,
                            onDevResetStatsTimer = vm::onDevResetStatsTimer,
                            onDevResetCoachMarks = vm::onDevResetCoachMarks,
                            timeOverrideMs = state.timeOverrideMs,
                            onDevTimeOverrideChanged = vm::onDevTimeOverrideChanged,
                            useProductionLogo = state.useProductionLogo,
                            onDevUseProductionLogoChanged = vm::onDevUseProductionLogoChanged,
                            onBack = vm::onHideSettings
                        )
                        if (state.showOnboarding) {
                            OnboardingScreen(onFinish = vm::onOnboardingComplete)
                        }
                      }
                    }
                    else -> {
                        SweetSpotScreen(viewModel = vm)
                    }
                }

                // Overlay dialogs shown on any screen (except the paywall and the onboarding intro,
                // so nothing stacks on top of the first-launch flow).
                if (!state.showPaywall && !state.showOnboarding) {
                    ThankYouDialog(state, vm)
                    StatsPromptDialog(state, vm)
                    ImportErrorDialog(state, vm)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    /**
     * A warm start from a scanned/tapped setup link delivers the new intent here (the activity is
     * `singleTask`). Update the retained intent and forward it to the import handler.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleImportIntent(intent)
    }

    /**
     * Forwards a `VIEW` intent carrying a setup deep link to the ViewModel for decoding. Ignores the
     * plain `MAIN`/`LAUNCHER` launch (no data), so a normal open never triggers an import.
     */
    private fun handleImportIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW && intent.data != null) {
            viewModel.onImportLink(intent.data)
        }
    }
}

/** Shows a thank-you dialog after a successful in-app purchase. */
@Composable
private fun ThankYouDialog(state: UiState, vm: SweetSpotViewModel) {
    if (!state.showThankYou) return
    AlertDialog(
        onDismissRequest = vm::onThankYouDismissed,
        title = { Text(stringResource(R.string.thank_you_title)) },
        text = { Text(stringResource(R.string.thank_you_message)) },
        confirmButton = {
            TextButton(onClick = vm::onThankYouDismissed) {
                Text(stringResource(android.R.string.ok))
            }
        }
    )
}

/**
 * Shows a friendly dialog when a scanned/tapped setup link couldn't be imported: a prompt to
 * update the app for a newer payload, or a "couldn't read this" message for a corrupt one.
 */
@Composable
private fun ImportErrorDialog(state: UiState, vm: SweetSpotViewModel) {
    val error = state.importError ?: return
    val message = when (error) {
        ImportError.TOO_NEW -> R.string.import_error_too_new
        ImportError.MALFORMED -> R.string.import_error_malformed
    }
    AlertDialog(
        onDismissRequest = vm::onDismissImport,
        title = { Text(stringResource(R.string.import_error_title)) },
        text = { Text(stringResource(message)) },
        confirmButton = {
            TextButton(onClick = vm::onDismissImport) {
                Text(stringResource(android.R.string.ok))
            }
        }
    )
}

/** Shows a one-time opt-in prompt for anonymous API statistics. */
@Composable
private fun StatsPromptDialog(state: UiState, vm: SweetSpotViewModel) {
    if (!state.showStatsPrompt) return
    AlertDialog(
        onDismissRequest = vm::onStatsPromptDismissed,
        title = { Text(stringResource(R.string.stats_prompt_title)) },
        text = { Text(stringResource(R.string.stats_prompt_message)) },
        confirmButton = {
            TextButton(onClick = vm::onStatsPromptEnabled) {
                Text(stringResource(R.string.stats_prompt_enable))
            }
        },
        dismissButton = {
            TextButton(onClick = vm::onStatsPromptDismissed) {
                Text(stringResource(R.string.stats_prompt_dismiss))
            }
        }
    )
}
