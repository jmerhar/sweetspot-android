package si.merhar.sweetspot.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import si.merhar.sweetspot.R
import si.merhar.sweetspot.SweetSpotViewModel
import si.merhar.sweetspot.ui.components.BreakdownTable
import si.merhar.sweetspot.ui.components.DurationInput
import si.merhar.sweetspot.ui.components.ErrorBox
import si.merhar.sweetspot.ui.components.PriceBarChart
import si.merhar.sweetspot.ui.components.ResultSummary
import si.merhar.sweetspot.ui.theme.SubtitleGray

@Composable
fun SweetSpotScreen(viewModel: SweetSpotViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SweetSpot",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Text(
            text = "Find the cheapest time to run your appliance",
            fontSize = 14.sp,
            color = SubtitleGray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        DurationInput(
            value = state.durationInput,
            onValueChange = viewModel::onDurationChanged,
            onFind = viewModel::onFindClicked,
            isLoading = state.isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        state.error?.let { error ->
            ErrorBox(message = error)
            Spacer(modifier = Modifier.height(16.dp))
        }

        state.result?.let { result ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Cheapest Window",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    ResultSummary(result = result)

                    Spacer(modifier = Modifier.height(16.dp))

                    BreakdownTable(breakdown = result.breakdown)

                    if (state.allPrices.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Next 24 hours",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        PriceBarChart(
                            prices = state.allPrices,
                            result = result
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Costs shown are per 1 kW load. Prices do not include energy tax and supplier fee.",
                        fontSize = 12.sp,
                        color = SubtitleGray
                    )
                }
            }
        }
    }
}
