package dev.antonlammers.trainist.ui.settings

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.antonlammers.trainist.BuildConfig
import dev.antonlammers.trainist.R

/** Donation + developer contact — static UI, no ViewModel. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(navController: NavController) {
    val context = LocalContext.current
    // Resolved here (not inside the onClick lambdas below, which aren't @Composable contexts).
    val emailSubject = stringResource(R.string.settings_contact_email_subject)
    // Prefilled so a bug report carries the version without the user having to look it up.
    val emailBody = stringResource(R.string.settings_contact_email_body, BuildConfig.VERSION_NAME)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_support_section_header)) },
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.settings_donation_title), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.settings_donation_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://paypal.me/antonlamm"))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(
                    Icons.Rounded.VolunteerActivism,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Text(stringResource(R.string.settings_donation_cta))
            }

            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$DEVELOPER_CONTACT_EMAIL")).apply {
                        putExtra(Intent.EXTRA_SUBJECT, emailSubject)
                        putExtra(Intent.EXTRA_TEXT, emailBody)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(
                    Icons.Rounded.MailOutline,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Text(stringResource(R.string.settings_contact_developer_button))
            }

            SettingsBottomSpacer()
        }
    }
}

/** Support inbox for feedback, bugs and feature suggestions. */
private const val DEVELOPER_CONTACT_EMAIL = "lammy.google.develop.flatness494@passmail.net"
