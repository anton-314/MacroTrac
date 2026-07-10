package dev.antonlammers.macrotrac.domain.repository

import dev.antonlammers.macrotrac.domain.model.Exercise
import kotlinx.coroutines.flow.Flow

/**
 * Access to the exercise catalog (bundled entries + custom ones), keyed by [Exercise.stableId].
 * The concrete seed/parse of the bundled snapshot lives in `data/` behind this abstraction, so the
 * source can be swapped without touching feature code.
 */
interface ExerciseCatalogRepository {
    fun exercises(): Flow<List<Exercise>>
    fun exercise(stableId: String): Flow<Exercise?>

    /** Insert-or-replace by [Exercise.stableId] (used for seeding and custom-exercise edits). */
    suspend fun upsertAll(exercises: List<Exercise>)
    suspend fun delete(stableId: String)
}
