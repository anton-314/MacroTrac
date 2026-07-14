package dev.antonlammers.trainist.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
// "Ink & Paper" redesign — fixed monochrome palettes, color reserved for data.
// Dark mode = "Ink", light mode = "Paper". Accent + data colors are identical in
// both modes.
// ─────────────────────────────────────────────────────────────────────────────

// Dark mode ("Ink")
val InkPaper    = Color(0xFF0D0D0C) // app chrome / scaffold background
val InkBg       = Color(0xFF161615) // screen background
val InkSurface  = Color(0xFF1E1E1C) // cards, sheets, nav bar
val InkSurface2 = Color(0xFF282826) // chart tracks, inset fields, progress tracks
val InkLine     = Color(0x14FFFFFF) // hairline dividers / card borders — rgba(255,255,255,.08)
val InkText     = Color(0xFFEDECE8) // primary text — headings, values
val InkText2    = Color(0xFF9C9A94) // secondary text — supporting labels
val InkText3    = Color(0xFF68665F) // tertiary text — micro-labels, placeholders, inactive nav

// Light mode ("Paper")
val PaperPaper    = Color(0xFFE7DECC)
val PaperBg       = Color(0xFFF5F0E4)
val PaperSurface  = Color(0xFFFCF8EF)
val PaperSurface2 = Color(0xFFEDE4D2)
val PaperLine     = Color(0x1F282012) // hairline — rgba(40,32,18,.12)
val PaperText     = Color(0xFF26211A)
val PaperText2    = Color(0xFF6E6455)
val PaperText3    = Color(0xFF9A8F7C)

// Accent — fixed pastel lilac in both modes (user-selected default).
val AccentLilac = Color(0xFFB79FCB)
// On-accent ("ink") — dark text/icon color for filled accent buttons/FAB.
val OnAccentInk = Color(0xFF1B1409)

// Macro nutrient colors (data — identical in both modes).
val CalorieColor = Color(0xFFFFAB76)
val ProteinColor = Color(0xFF82C8F5)
val CarbsColor   = Color(0xFF82D48A)
val FatColor     = Color(0xFFE8A4D4)

// Clean-eating food tags: green (healthy) / amber (neutral) / red (unhealthy).
// Untagged foods use the theme's surfaceVariant (grey), so no color is needed here.
val TagHealthyColor   = Color(0xFF7CC47F)
val TagNeutralColor   = Color(0xFFE3A85A)
val TagUnhealthyColor = Color(0xFFE07670)
