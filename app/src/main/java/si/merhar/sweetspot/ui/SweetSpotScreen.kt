package si.merhar.sweetspot.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import si.merhar.sweetspot.R
import si.merhar.sweetspot.SweetSpotViewModel
import si.merhar.sweetspot.ui.components.BreakdownTable
import si.merhar.sweetspot.ui.components.DurationInput
import si.merhar.sweetspot.ui.components.ErrorBox
import si.merhar.sweetspot.ui.components.PriceBarChart
import si.merhar.sweetspot.ui.components.ResultSummary
import si.merhar.sweetspot.util.formatDuration

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
        if (error != null && !isValidationError(error)) {
            snackbarHostState.showSnackbar(error)
        }
    }

    if (hasResults) {
        ResultScreen(
            result = state.result!!,
            allPrices = state.allPrices,
            resultLabel = state.resultLabel ?: formatDuration(state.durationHours, state.durationMinutes),
            zoneId = state.zoneId,
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
                        Image(
                            painter = painterResource(R.drawable.ic_launcher_foreground),
                            contentDescription = null,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SweetSpot")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::onShowSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
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

            state.error?.let { error ->
                if (isValidationError(error)) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ErrorBox(message = error)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "SweetSpot helps you save money by finding the cheapest time to run your appliances. Just pick how long your appliance needs to run, and it will scan the next 24 hours of dynamic electricity prices to find the best window. You can also save your favourite appliances in settings for quick access.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/** Result view showing the cheapest window summary, hourly breakdown, and price bar chart. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResultScreen(
    result: si.merhar.sweetspot.model.WindowResult,
    allPrices: List<si.merhar.sweetspot.model.HourlyPrice>,
    resultLabel: String,
    zoneId: java.time.ZoneId,
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
                            contentDescription = "Back"
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
            Text(
                text = "Cheapest Window",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            ResultSummary(result = result, zoneId = zoneId)

            Spacer(modifier = Modifier.height(16.dp))

            BreakdownTable(breakdown = result.breakdown)

            if (allPrices.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Upcoming Prices",
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
                text = "Costs shown are per 1 kW load. Prices do not include VAT, energy tax, and supplier fee." +
                    " Tomorrow\u2019s prices are usually available after 13:00 CET.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/** Returns true if [error] is a user-facing validation message (shown inline, not as a snackbar). */
private fun isValidationError(error: String): Boolean {
    return error.startsWith("Please select a duration") || error.startsWith("Not enough price data")
}
