package dev.antonlammers.trainist.ui.workout

import dev.antonlammers.trainist.domain.model.ExerciseType
import dev.antonlammers.trainist.domain.model.Mechanic
import java.util.Locale

/**
 * Shared display formatting for exercise metadata, so the catalog and the exercise-detail screen
 * render types / mechanics / muscle names identically.
 */

internal fun String.titleCase(): String = split(" ").joinToString(" ") { word ->
    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() }
}

internal fun ExerciseType.displayName(): String = when (this) {
    ExerciseType.WEIGHT_REPS -> "Gewicht × Reps"
    ExerciseType.BODYWEIGHT -> "Körpergewicht"
}

internal fun Mechanic.displayName(): String = when (this) {
    Mechanic.COMPOUND -> "Verbund"
    Mechanic.ISOLATION -> "Isolation"
}
