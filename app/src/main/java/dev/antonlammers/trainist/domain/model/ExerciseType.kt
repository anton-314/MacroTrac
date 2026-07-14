package dev.antonlammers.trainist.domain.model

/**
 * How a set of an exercise is measured. [WEIGHT_REPS] = external weight × reps; [BODYWEIGHT] = reps
 * against body weight (with optional added weight, resolved to an effective weight later).
 *
 * Kept deliberately small; time-held and cardio/distance types are out of v1 but can be added
 * additively (the data model does not assume only these two). Persisted by [name]; [parse] reads it
 * back defensively so unknown/missing values fall back to [WEIGHT_REPS].
 */
enum class ExerciseType {
    WEIGHT_REPS, BODYWEIGHT;

    companion object {
        fun parse(raw: String?): ExerciseType =
            raw?.trim()?.uppercase()?.let { v -> entries.firstOrNull { it.name == v } } ?: WEIGHT_REPS
    }
}
