package dev.antonlammers.trainist.ui.data

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.antonlammers.trainist.R
import dev.antonlammers.trainist.ui.settings.SHEET_CONTENT_PADDING
import dev.antonlammers.trainist.ui.settings.SettingsSheet

/**
 * Backup export/import, opened as a sheet from the settings hub.
 *
 * The result message is deliberately *not* shown here — a snackbar would render behind the modal
 * sheet. The hub already owns [DataViewModel] (it also drives the reminder toggle), so it observes
 * the message, closes this sheet and reports there.
 */
@Composable
fun DataSheet(
    viewModel: DataViewModel,
    onDismiss: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.import(it.toString()) }
    }

    // Resolved here (not inside the onClick lambda below, which isn't a @Composable context).
    val exportChooserTitle = stringResource(R.string.settings_export_chooser_title)

    SettingsSheet(
        title = stringResource(R.string.settings_data_row_title),
        onDismiss = onDismiss,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = SHEET_CONTENT_PADDING),
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
