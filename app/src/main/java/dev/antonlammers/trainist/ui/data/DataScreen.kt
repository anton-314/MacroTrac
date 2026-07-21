package dev.antonlammers.trainist.ui.data

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import dev.antonlammers.trainist.R

/**
 * Backup export/import — reached from the settings hub.
 *
 * The daily-reminder toggle shares [DataViewModel] but stays on the hub itself: it is a single
 * switch, and a whole screen for one control would be pure indirection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataScreen(
    navController: NavController,
    viewModel: DataViewModel = hiltViewModel(),
) {
    val snackbar = remember { SnackbarHostState() }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it.toDisplayString(context))
            viewModel.clearMessage()
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.import(it.toString()) }
    }

    // Resolved here (not inside the onClick lambda below, which isn't a @Composable context).
    val exportChooserTitle = stringResource(R.string.settings_export_chooser_title)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_data_row_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(R.string.settings_data_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Button(
                onClick = {
                    viewModel.export { uri ->
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/zip"
                            putExtra(Intent.EXTRA_STREAM, Uri.parse(uri))
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, exportChooserTitle))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading,
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(Icons.Rounded.FileDownload, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text(stringResource(R.string.settings_export_button))
            }

            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf("application/zip", "text/csv", "*/*")) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading,
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(Icons.Rounded.FileUpload, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text(stringResource(R.string.settings_import_button))
            }
        }
    }
}
