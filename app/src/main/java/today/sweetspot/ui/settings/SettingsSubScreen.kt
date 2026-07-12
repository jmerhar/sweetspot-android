package today.sweetspot.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import today.sweetspot.R

/**
 * Shared scaffold for a settings sub-screen: a [TopAppBar] with the given [title] and a back arrow
 * (invoking [onBack]), and a vertically-scrolling [Column] body. Mirrors the structure of every
 * full-screen composable in the settings package.
 *
 * @param title Screen title shown in the top bar.
 * @param onBack Called when the back arrow is tapped.
 * @param modifier Modifier for the [Scaffold].
 * @param snackbarHostState Optional snackbar host (e.g. for the Total price screen's exit reminder).
 * @param content Body of the screen, laid out in a scrolling [Column].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsSubScreen(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    // System back returns to the settings menu (or runs the caller's gate, e.g. the Total price
    // screen's incomplete-setup check). Composed only while this screen's own content is shown — a
    // sub-screen that opens a picker early-returns before reaching here, so the picker's BackHandler
    // takes precedence.
    BackHandler { onBack() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(title) },
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
        },
        snackbarHost = { if (snackbarHostState != null) SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            content = content
        )
    }
}
