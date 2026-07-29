package today.sweetspot.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import today.sweetspot.R
import today.sweetspot.ThemeMode
import today.sweetspot.data.api.DataSource
import today.sweetspot.model.Appliance
import today.sweetspot.model.Country
import today.sweetspot.model.PriceZone
import today.sweetspot.shared.R as SharedR
import today.sweetspot.util.UiText
import java.time.ZoneId

/** The settings sub-screen currently shown: the root menu, or one grouped category screen. */
private enum class SettingsRoute { Menu, Appliances, Ev, TotalPrice, Region, Appearance, Share, Advanced, Help }

/**
 * Settings root. Shows a short menu of category rows (icon + title + description); each opens a focused
 * sub-screen (appliances, EV charging, total price, region, appearance, advanced). The active screen is
 * tracked by a single [SettingsRoute] rather than a navigation library, matching the app's state-based
 * navigation. The statistics opt-in stays as an inline toggle row on the menu; the version footer and
 * its 7-tap developer-options unlock live on the Help & feedback sub-screen (About).
 *
 * The parameter list is unchanged from the previous single-screen version — [today.sweetspot.MainActivity]
 * still passes the full set and this composable distributes each slice to the relevant sub-screen.
 * [modifier] applies to the root menu (each category sub-screen supplies its own Scaffold).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit,
    currentTimeZoneId: ZoneId,
    isUsingDefaultTimezone: Boolean,
    onTimezoneSelected: (ZoneId?) -> Unit,
    appliances: List<Appliance>,
    sortedAppliances: List<Appliance>,
    applianceSort: today.sweetspot.model.ApplianceSort,
    usage: Map<String, today.sweetspot.model.ApplianceUsage>,
    onAddAppliance: (name: String, durationHours: Int, durationMinutes: Int, icon: String, powerKw: Double?) -> Unit,
    onUpdateAppliance: (Appliance) -> Unit,
    onDeleteAppliance: (id: String) -> Unit,
    onApplianceSortChanged: (today.sweetspot.model.ApplianceSort) -> Unit,
    onReorderAppliances: (List<Appliance>) -> Unit,
    onResetUsage: () -> Unit,
    applianceGrouping: today.sweetspot.model.ApplianceGrouping,
    onApplianceGroupingChanged: (today.sweetspot.model.ApplianceGrouping) -> Unit,
    evHomeChargerKw: Double,
    evDefaultTargetSoc: Int,
    onEvHomeChargerChanged: (Double) -> Unit,
    onEvDefaultTargetChanged: (Int) -> Unit,
    searchVehicles: (String) -> List<today.sweetspot.model.EvVehicle>,
    onAddVehicle: (name: String, batteryKwh: Double, acPowerKw: Double) -> Unit,
    evPosition: today.sweetspot.model.EvPosition,
    evSeparate: Boolean,
    onEvPositionChanged: (today.sweetspot.model.EvPosition) -> Unit,
    onEvSeparateChanged: (Boolean) -> Unit,
    countryCode: String,
    priceZone: PriceZone?,
    countries: List<Country>,
    onCountrySelected: (String) -> Unit,
    onPriceZoneSelected: (String) -> Unit,
    allInSupported: Boolean,
    allInEnabled: Boolean,
    allInSuppliers: List<today.sweetspot.model.SupplierTariff>,
    selectedSupplierId: String?,
    manualSurcharge: Double?,
    allInCurrency: String,
    onAllInEnabledChanged: (Boolean) -> Unit,
    onSupplierSelected: (String) -> Unit,
    onManualSurchargeChanged: (Double?) -> Unit,
    sourceOrder: List<String>?,
    disabledSources: Set<String>,
    availableSources: List<DataSource>,
    onSourceOrderChanged: (List<String>) -> Unit,
    onDisabledSourcesChanged: (Set<String>) -> Unit,
    onResetSourceOrder: () -> Unit,
    onLanguageChanged: (String) -> Unit,
    onShareSetup: () -> String,
    onReplayOnboarding: () -> Unit,
    reportSubmission: today.sweetspot.ReportSubmission,
    myReports: List<today.sweetspot.MyReportView>,
    reportThread: today.sweetspot.ThreadState?,
    replySubmission: today.sweetspot.ReplyState,
    onSubmitReport: (today.sweetspot.model.ReportCategory, String, String, String?) -> Unit,
    onDismissReportResult: () -> Unit,
    onLoadMyReports: () -> Unit,
    onFlushOutbox: () -> Unit,
    onOpenThread: (Int) -> Unit,
    onCloseThread: () -> Unit,
    onSendReply: (Int, String) -> Unit,
    onClearCache: () -> UiText,
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
    isDevUnlocked: Boolean,
    onDevUnlockChanged: (Boolean) -> Unit,
    onDevResetStatsTimer: () -> Unit,
    onDevResetCoachMarks: () -> Unit,
    timeOverrideMs: Long?,
    onDevTimeOverrideChanged: (Long?) -> Unit,
    useProductionLogo: Boolean,
    onDevUseProductionLogoChanged: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var route by rememberSaveable { mutableStateOf(SettingsRoute.Menu) }
    val toMenu = { route = SettingsRoute.Menu }

    when (route) {
        SettingsRoute.Appliances -> AppliancesSettingsScreen(
            appliances = sortedAppliances,
            sort = applianceSort,
            usage = usage,
            onAddAppliance = onAddAppliance,
            onUpdateAppliance = onUpdateAppliance,
            onDeleteAppliance = onDeleteAppliance,
            onSortChanged = onApplianceSortChanged,
            onReorder = onReorderAppliances,
            onResetUsage = onResetUsage,
            grouping = applianceGrouping,
            onGroupingChanged = onApplianceGroupingChanged,
            onBack = toMenu
        )

        SettingsRoute.Ev -> EvSettingsScreen(
            vehicles = appliances.filter { it.isEv },
            homeChargerKw = evHomeChargerKw,
            defaultTargetSoc = evDefaultTargetSoc,
            onHomeChargerChanged = onEvHomeChargerChanged,
            onDefaultTargetChanged = onEvDefaultTargetChanged,
            searchVehicles = searchVehicles,
            onAddVehicle = onAddVehicle,
            onUpdateAppliance = onUpdateAppliance,
            onDeleteAppliance = onDeleteAppliance,
            evPosition = evPosition,
            evSeparate = evSeparate,
            sortIsCustom = applianceSort.isCustom,
            onEvPositionChanged = onEvPositionChanged,
            onEvSeparateChanged = onEvSeparateChanged,
            onBack = toMenu
        )

        SettingsRoute.TotalPrice -> TotalPriceSettingsScreen(
            enabled = allInEnabled,
            suppliers = allInSuppliers,
            selectedSupplierId = selectedSupplierId,
            manualSurcharge = manualSurcharge,
            currencyCode = allInCurrency,
            onEnabledChange = onAllInEnabledChanged,
            onSupplierSelected = onSupplierSelected,
            onManualSurchargeChanged = onManualSurchargeChanged,
            onBack = toMenu
        )

        SettingsRoute.Region -> RegionSettingsScreen(
            countryCode = countryCode,
            priceZone = priceZone,
            countries = countries,
            onCountrySelected = onCountrySelected,
            onPriceZoneSelected = onPriceZoneSelected,
            currentTimeZoneId = currentTimeZoneId,
            isUsingDefaultTimezone = isUsingDefaultTimezone,
            onTimezoneSelected = onTimezoneSelected,
            onBack = toMenu
        )

        SettingsRoute.Appearance -> AppearanceSettingsScreen(
            themeMode = themeMode,
            onThemeModeChanged = onThemeModeChanged,
            onLanguageChanged = onLanguageChanged,
            onBack = toMenu
        )

        SettingsRoute.Share -> today.sweetspot.ui.share.ShareSetupScreen(
            shareLink = onShareSetup,
            onBack = toMenu
        )

        SettingsRoute.Advanced -> AdvancedSettingsScreen(
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
            isDevUnlocked = isDevUnlocked,
            onDevUnlockChanged = onDevUnlockChanged,
            onDevResetUnlock = onDevResetUnlock,
            onDevResetStatsTimer = onDevResetStatsTimer,
            timeOverrideMs = timeOverrideMs,
            onDevTimeOverrideChanged = onDevTimeOverrideChanged,
            timeZoneId = currentTimeZoneId,
            useProductionLogo = useProductionLogo,
            onDevUseProductionLogoChanged = onDevUseProductionLogoChanged,
            onBack = toMenu
        )

        SettingsRoute.Help -> HelpSettingsScreen(
            reportSubmission = reportSubmission,
            myReports = myReports,
            thread = reportThread,
            replySubmission = replySubmission,
            allInSupported = allInSupported,
            devOptionsEnabled = devOptionsEnabled,
            onReplayOnboarding = onReplayOnboarding,
            onResetCoachMarks = onDevResetCoachMarks,
            onSubmitReport = onSubmitReport,
            onDismissReportResult = onDismissReportResult,
            onLoadMyReports = onLoadMyReports,
            onFlushOutbox = onFlushOutbox,
            onOpenThread = onOpenThread,
            onCloseThread = onCloseThread,
            onSendReply = onSendReply,
            onDevOptionsUnlocked = onDevOptionsUnlocked,
            onBack = toMenu
        )

        SettingsRoute.Menu -> SettingsMenu(
            modifier = modifier,
            isUnlocked = isUnlocked,
            trialDaysRemaining = trialDaysRemaining,
            productPrice = productPrice,
            onPurchaseClicked = onPurchaseClicked,
            allInSupported = allInSupported,
            isStatsEnabled = isStatsEnabled,
            onStatsEnabledChanged = onStatsEnabledChanged,
            onOpen = { route = it },
            onBack = onBack
        )
    }
}

/** The root settings menu: subscribe card, category rows (incl. Help & feedback), and the stats toggle. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsMenu(
    modifier: Modifier,
    isUnlocked: Boolean,
    trialDaysRemaining: Int,
    productPrice: String?,
    onPurchaseClicked: () -> Unit,
    allInSupported: Boolean,
    isStatsEnabled: Boolean,
    onStatsEnabledChanged: (Boolean) -> Unit,
    onOpen: (SettingsRoute) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

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
            if (!isUnlocked) {
                UnlockSection(
                    trialDaysRemaining = trialDaysRemaining,
                    productPrice = productPrice,
                    onPurchaseClicked = onPurchaseClicked
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            SettingsMenuRow(
                iconRes = SharedR.drawable.ic_device,
                title = stringResource(R.string.settings_appliances),
                description = stringResource(R.string.settings_appliances_menu_desc),
                onClick = { onOpen(SettingsRoute.Appliances) },
                modifier = Modifier.testTag("menu_appliances")
            )
            SettingsMenuRow(
                iconRes = SharedR.drawable.ic_ev_charger,
                title = stringResource(R.string.settings_ev_title),
                description = stringResource(R.string.settings_ev_menu_desc),
                onClick = { onOpen(SettingsRoute.Ev) },
                modifier = Modifier.testTag("menu_ev")
            )
            if (allInSupported) {
                SettingsMenuRow(
                    iconRes = SharedR.drawable.ic_price,
                    title = stringResource(R.string.settings_all_in_title),
                    description = stringResource(R.string.settings_all_in_menu_desc),
                    onClick = { onOpen(SettingsRoute.TotalPrice) }
                )
            }
            SettingsMenuRow(
                iconRes = SharedR.drawable.ic_region,
                title = stringResource(R.string.settings_region_title),
                description = stringResource(R.string.settings_region_desc),
                onClick = { onOpen(SettingsRoute.Region) }
            )
            SettingsMenuRow(
                iconRes = SharedR.drawable.ic_appearance,
                title = stringResource(R.string.settings_appearance_title),
                description = stringResource(R.string.settings_appearance_desc),
                onClick = { onOpen(SettingsRoute.Appearance) },
                modifier = Modifier.testTag("menu_appearance")
            )
            SettingsMenuRow(
                iconRes = SharedR.drawable.ic_share,
                title = stringResource(R.string.settings_share_title),
                description = stringResource(R.string.settings_share_menu_desc),
                onClick = { onOpen(SettingsRoute.Share) },
                modifier = Modifier.testTag("menu_share")
            )

            // Statistics opt-in stays inline on the menu (toggle row).
            SettingsMenuRow(
                iconRes = SharedR.drawable.ic_stats,
                title = stringResource(R.string.settings_stats_title),
                description = stringResource(R.string.settings_stats_description),
                onClick = { onStatsEnabledChanged(!isStatsEnabled) },
                trailing = { Switch(checked = isStatsEnabled, onCheckedChange = onStatsEnabledChanged) }
            )

            SettingsMenuRow(
                iconRes = SharedR.drawable.ic_advanced,
                title = stringResource(R.string.settings_advanced),
                description = stringResource(R.string.settings_advanced_description),
                onClick = { onOpen(SettingsRoute.Advanced) }
            )

            // Help & feedback hosts the guidance actions (How it works, Reset tips), the report/feedback
            // form, "My reports", support links, and the version footer + 7-tap developer-options unlock.
            SettingsMenuRow(
                iconRes = SharedR.drawable.ic_help,
                title = stringResource(R.string.settings_help_title),
                description = stringResource(R.string.settings_help_desc),
                onClick = { onOpen(SettingsRoute.Help) },
                modifier = Modifier.testTag("menu_help")
            )
        }
    }
}
