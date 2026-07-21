package dev.antonlammers.trainist.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.antonlammers.trainist.BuildConfig
import dev.antonlammers.trainist.R

/**
 * Donation + feedback, opened as a sheet from the settings hub. Static UI, no ViewModel.
 *
 * [DonateButton] is deliberately also placed on the hub itself: a donation the user has to go
 * looking for is a donation that doesn't happen.
 */
@Composable
fun SupportSheet(onDismiss: () -> Unit) {
    SettingsSheet(
        title = stringResource(R.string.settings_support_row_title),
        onDismiss = onDismiss,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = SHEET_CONTENT_PADDING),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.settings_donation_title), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.settings_donation_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            DonateButton()

            Text(
                stringResource(R.string.settings_contact_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                stringResource(R.string.settings_contact_prompt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FeedbackButton()
        }
    }
}

/** PayPal donation — used both in [SupportSheet] and directly on the settings hub. */
@Composable
fun DonateButton(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Button(
        onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(DONATION_URL))) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
    ) {
        Icon(
            Icons.Rounded.VolunteerActivism,
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp),
        )
        Text(stringResource(R.string.settings_donation_cta))
    }
}

/**
 * Opens a prefilled mail draft. Subject and body both name what is being asked for — feedback and
 * concrete suggestions — so an empty compose window doesn't have to be filled from a blank page;
 * the body also carries [BuildConfig.VERSION_NAME] so a report arrives with its version attached.
 */
@Composable
private fun FeedbackButton() {
    val context = LocalContext.current
    // Resolved here (not inside the onClick lambda below, which isn't a @Composable context).
    val subject = stringResource(R.string.settings_contact_email_subject)
    val body = stringResource(R.string.settings_contact_email_body, BuildConfig.VERSION_NAME)

    OutlinedButton(
        onClick = {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$DEVELOPER_CONTACT_EMAIL")).apply {
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
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
}

/** Support inbox for feedback, bugs and feature suggestions. */
private const val DEVELOPER_CONTACT_EMAIL = "lammy.google.develop.flatness494@passmail.net"

private const val DONATION_URL = "https://paypal.me/antonlamm"
