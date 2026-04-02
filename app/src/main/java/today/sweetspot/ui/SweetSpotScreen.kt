package today.sweetspot.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import today.sweetspot.R
import today.sweetspot.AppError
import today.sweetspot.SweetSpotViewModel
import today.sweetspot.ui.components.BreakdownTable
import today.sweetspot.ui.components.DurationInput
import today.sweetspot.ui.components.ErrorBox
import today.sweetspot.ui.components.PriceBarChart
import today.sweetspot.ui.components.ResultSummary
import today.sweetspot.util.formatDuration

/**
 * Top-level screen that delegates to [FormScreen] or [ResultScreen] based on whether
 * a cheapest-window result is available. Also handles system back to clear results
 * and shows network errors as snackbar messages.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SweetSpotScreen(viewModel: SweetSpotViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val hasResults = state.result != null

    BackHandler(enabled = hasResults) {
        viewModel.onClearResult()
    }

    LaunchedEffect(state.error) {
        val error = state.error
        if (error is AppError.Network) {
            snackbarHostState.showSnackbar(error.message)
        }
    }

    if (hasResults) {
        val priceZoneName = state.priceZone?.let { zone ->
            val zoneName = stringResource(zone.labelRes)
            val country = state.countries.find { it.code == state.countryCode }
            if (country != null && country.zones.size > 1) {
                "${stringResource(country.nameRes)} — $zoneName"
            } else {
                zoneName
            }
        }

        ResultScreen(
            result = state.result!!,
            allPrices = state.allPrices,
            resultLabel = state.resultLabel ?: formatDuration(state.durationHours, state.durationMinutes),
            now = state.now,
            priceSource = state.priceSource,
            priceZoneName = priceZoneName,
            isLoading = state.isLoading,
            onRefresh = viewModel::onRefreshResults,
            onBack = viewModel::onClearResult,
            snackbarHostState = snackbarHostState,
            modifier = modifier
        )
    } else {
        FormScreen(
            viewModel = viewModel,
            snackbarHostState = snackbarHostState,
            modifier = modifier
        )
    }
}

/** Main form view with duration picker, appliance chips, and quick-duration buttons. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormScreen(
    viewModel: SweetSpotViewModel,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val logoBg = if (state.useProductionLogo) Color(0xFFF5F7FA)
                            else colorResource(R.color.ic_launcher_background)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(logoBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_launcher_foreground),
                                contentDescription = null,
                                modifier = Modifier.requiredSize(52.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("SweetSpot")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::onShowSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.cd_settings)
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
                .padding(horizontal = 16.dp)
        ) {
            DurationInput(
                hours = state.durationHours,
                minutes = state.durationMinutes,
                onDurationChanged = viewModel::onDurationChanged,
                onFind = viewModel::onFindClicked,
                onQuickDuration = viewModel::onQuickDuration,
                appliances = state.appliances,
                onApplianceTap = viewModel::onApplianceDuration,
                onAddAppliancesTap = viewModel::onShowSettings,
                isLoading = state.isLoading
            )

            if (state.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }

            val validationError = state.error as? AppError.Validation
            if (validationError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                ErrorBox(message = validationError.message)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.main_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            if (state.trialDaysRemaining in 1..14 && !state.isUnlocked) {
                val days = state.trialDaysRemaining
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = pluralStringResource(R.plurals.trial_days_remaining, days, days),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/** Result view showing the cheapest window summary, hourly breakdown, and price bar chart. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResultScreen(
    result: today.sweetspot.model.WindowResult,
    allPrices: List<today.sweetspot.model.PriceSlot>,
    resultLabel: String,
    now: java.time.ZonedDateTime,
    priceSource: String?,
    priceZoneName: String?,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(resultLabel) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh, enabled = !isLoading) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.cd_refresh)
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
        ) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.result_cheapest_window),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                ResultSummary(result = result, now = now)

                Spacer(modifier = Modifier.height(16.dp))

                BreakdownTable(breakdown = result.breakdown)

                if (allPrices.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.result_upcoming_prices),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    PriceBarChart(
                        prices = allPrices,
                        result = result
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.result_disclaimer) +
                        (if (priceSource != null) stringResource(R.string.result_data_source, priceSource) else "") +
                        (if (priceZoneName != null) stringResource(R.string.result_price_zone, priceZoneName) else ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
