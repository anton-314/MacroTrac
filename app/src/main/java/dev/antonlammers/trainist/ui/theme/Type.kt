package dev.antonlammers.trainist.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import dev.antonlammers.trainist.R

// ─────────────────────────────────────────────────────────────────────────────
// "Ink & Paper" type system — three Google (downloadable) font families:
//   • Serif (Newsreader)        → screen titles, big numbers, section/dialog titles
//   • Sans  (Hanken Grotesk)    → body text, list items, buttons, field values
//   • Mono  (JetBrains Mono)    → uppercase micro-labels with wide tracking
// Fonts are fetched at runtime via the Google Fonts provider (see res/values/
// font_certs.xml); no binary assets are bundled.
// ─────────────────────────────────────────────────────────────────────────────

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

val NewsreaderSerif = FontFamily(
    Font(googleFont = GoogleFont("Newsreader"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Newsreader"), fontProvider = provider, weight = FontWeight.Normal, style = FontStyle.Italic),
    Font(googleFont = GoogleFont("Newsreader"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Newsreader"), fontProvider = provider, weight = FontWeight.SemiBold),
)

val HankenGroteskSans = FontFamily(
    Font(googleFont = GoogleFont("Hanken Grotesk"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Hanken Grotesk"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Hanken Grotesk"), fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Hanken Grotesk"), fontProvider = provider, weight = FontWeight.Bold),
)

val JetBrainsMono = FontFamily(
    Font(googleFont = GoogleFont("JetBrains Mono"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("JetBrains Mono"), fontProvider = provider, weight = FontWeight.Medium),
)

val Typography = Typography(
    // Display — big serif numbers (kcal, kg).
    displayLarge = TextStyle(
        fontFamily = NewsreaderSerif, fontWeight = FontWeight.Medium,
        fontSize = 46.sp, lineHeight = 52.sp, letterSpacing = (-0.5).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = NewsreaderSerif, fontWeight = FontWeight.Medium,
        fontSize = 38.sp, lineHeight = 44.sp, letterSpacing = (-0.25).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = NewsreaderSerif, fontWeight = FontWeight.Medium,
        fontSize = 30.sp, lineHeight = 36.sp,
    ),
    // Headline / screen titles — serif.
    headlineLarge = TextStyle(
        fontFamily = NewsreaderSerif, fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp, lineHeight = 34.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = NewsreaderSerif, fontWeight = FontWeight.SemiBold,
        fontSize = 25.sp, lineHeight = 30.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = NewsreaderSerif, fontWeight = FontWeight.Medium,
        fontSize = 22.sp, lineHeight = 28.sp,
    ),
    // Title / section + dialog names — serif.
    titleLarge = TextStyle(
        fontFamily = NewsreaderSerif, fontWeight = FontWeight.Medium,
        fontSize = 20.sp, lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = NewsreaderSerif, fontWeight = FontWeight.Medium,
        fontSize = 16.sp, lineHeight = 22.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = NewsreaderSerif, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp,
    ),
    // Body — sans.
    bodyLarge = TextStyle(
        fontFamily = HankenGroteskSans, fontWeight = FontWeight.Normal,
        fontSize = 15.sp, lineHeight = 22.sp, letterSpacing = 0.1.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = HankenGroteskSans, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = HankenGroteskSans, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.2.sp,
    ),
    // Button / actionable labels — sans.
    labelLarge = TextStyle(
        fontFamily = HankenGroteskSans, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 18.sp, letterSpacing = 0.1.sp,
    ),
    // Micro mono-caps labels (PROTEIN (G), KCAL, axis/chip labels) — wide tracking.
    labelMedium = TextStyle(
        fontFamily = JetBrainsMono, fontWeight = FontWeight.Medium,
        fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 1.6.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = JetBrainsMono, fontWeight = FontWeight.Medium,
        fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 1.5.sp,
    ),
)
