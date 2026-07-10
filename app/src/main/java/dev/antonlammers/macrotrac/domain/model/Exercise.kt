package dev.antonlammers.macrotrac.domain.model

/**
 * An exercise definition — either a bundled catalog entry or a user-created custom one.
 *
 * [stableId] is the key everything else references (templates, sessions, backups) — **never** the
 * Room row id. Catalog exercises reuse the stable id from free-exercise-db; custom exercises get a
 * generated UUID. This keeps cross-device import consistent even when auto-increment ids differ.
 */
data class Exercise(
    val stableId: String,
    val name: String,
    val type: ExerciseType,
    val isCustom: Boolean,
    val primaryMuscles: List<String> = emptyList(),
    val secondaryMuscles: List<String> = emptyList(),
    val equipment: String? = null,
    val mechanic: Mechanic? = null,
    val category: String? = null,
    val instructions: List<String> = emptyList(),
    /** Optional per-exercise rest-timer override in seconds; null = use the global default. */
    val restSeconds: Int? = null,
)
