package si.merhar.sweetspot.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import si.merhar.sweetspot.R
import si.merhar.sweetspot.model.Country

/** Country settings section showing the current country selection. */
@Composable
internal fun CountrySection(
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

/** Full-screen country picker with search. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CountryPickerScreen(
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
