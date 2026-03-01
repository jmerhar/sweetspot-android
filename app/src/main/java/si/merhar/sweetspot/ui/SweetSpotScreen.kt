package si.merhar.sweetspot.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import si.merhar.sweetspot.R
import si.merhar.sweetspot.SweetSpotViewModel
import si.merhar.sweetspot.model.HourlyPrice
import si.merhar.sweetspot.model.WindowResult
import si.merhar.sweetspot.ui.components.BreakdownTable
import si.merhar.sweetspot.ui.components.DurationInput
import si.merhar.sweetspot.ui.components.ErrorBox
import si.merhar.sweetspot.ui.components.PriceBarChart
import si.merhar.sweetspot.ui.components.ResultSummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SweetSpotScreen(viewModel: SweetSpotViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show API/network errors as Snackbar
    LaunchedEffect(state.error) {
        val error = state.error
        if (error != null && !isValidationError(error)) {
            snackbarHostState.showSnackbar(error)
        }
    }

    // Cache last successful result so AnimatedVisibility exit animation has content
    var cachedResult by remember { mutableStateOf<WindowResult?>(null) }
    var cachedPrices by remember { mutableStateOf<List<HourlyPrice>>(emptyList()) }
    if (state.result != null) {
        cachedResult = state.result
        cachedPrices = state.allPrices
    }

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
                value = state.durationInput,
                onValueChange = viewModel::onDurationChanged,
                onFind = viewModel::onFindClicked,
                isLoading = state.isLoading
            )

            if (state.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }

            // Validation errors shown inline
            state.error?.let { error ->
                if (isValidationError(error)) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ErrorBox(message = error)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visible = state.result != null,
                enter = fadeIn() + slideInVertically { it / 4 },
            ) {
                cachedResult?.let { result ->
                    Column {
                        Text(
                            text = "Cheapest Window",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        ResultSummary(result = result, zoneId = state.zoneId)

                        Spacer(modifier = Modifier.height(16.dp))

                        BreakdownTable(breakdown = result.breakdown)

                        if (cachedPrices.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Next 24 Hours",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            PriceBarChart(
                                prices = cachedPrices,
                                result = result
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Costs shown are per 1 kW load. Prices do not include energy tax and supplier fee.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

private fun isValidationError(error: String): Boolean {
    return error.startsWith("Invalid duration") || error.startsWith("Not enough price data")
}
