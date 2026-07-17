package dev.antonlammers.trainist.ui.workout

import dev.antonlammers.trainist.ui.util.currentAppLocale

/**
 * Shared number ↔ text helpers for the workout set-entry UI (live session and history editing), so
 * both screens parse and render weights/reps identically.
 */

/** Formats a kg value: whole numbers without decimals, otherwise rounded to one decimal. */
internal fun formatKg(value: Double): String {
    val rounded = kotlin.math.round(value * 10.0) / 10.0
    // Explicit locale: "%.1f".format(rounded) without one uses the JVM default locale, which can
    // diverge from the app's chosen language below API 33 (e.g. a German-region device set to
    // English in-app would still render a comma decimal separator).
    return if (rounded % 1.0 == 0.0) rounded.toInt().toString() else "%.1f".format(currentAppLocale(), rounded)
}

internal fun parseWeight(text: String): Double = text.replace(',', '.').toDoubleOrNull() ?: 0.0
internal fun parseReps(text: String): Int = text.filter { it.isDigit() }.toIntOrNull() ?: 0

/** The editable text a weight/reps field seeds from — empty for a zero (so the placeholder shows). */
internal fun weightToText(weightKg: Double): String =
    if (weightKg == 0.0) "" else if (weightKg % 1.0 == 0.0) weightKg.toInt().toString() else weightKg.toString()

internal fun repsToText(reps: Int): String = if (reps == 0) "" else reps.toString()
