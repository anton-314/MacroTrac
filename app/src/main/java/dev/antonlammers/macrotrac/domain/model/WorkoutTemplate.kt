package dev.antonlammers.macrotrac.domain.model

/**
 * A reusable workout plan ("Push Day"): an ordered list of exercises, each with a planned sequence of
 * sets (each carrying its [SetType] — warmup, working, drop, failure). Weight and reps are **not**
 * part of a template — they come from the live session (pre-filled from the exercise's inline
 * history). [stableId] is the backup-stable key (UUID).
 */
data class WorkoutTemplate(
    val id: Long = 0,
    val stableId: String,
    val name: String,
    val exercises: List<TemplateExercise> = emptyList(),
)

/**
 * One exercise slot in a [WorkoutTemplate]. References the exercise by its [exerciseStableId] (never
 * the row id). [position] is the order within the template; [setTypes] the planned sets in order
 * (its size is the planned set count).
 */
data class TemplateExercise(
    val exerciseStableId: String,
    val position: Int,
    val setTypes: List<SetType>,
)
