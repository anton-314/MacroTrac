package dev.antonlammers.macrotrac.domain.repository

import dev.antonlammers.macrotrac.domain.model.WorkoutSession
import kotlinx.coroutines.flow.Flow

interface WorkoutSessionRepository {
    /** All sessions, newest first — the history feed. */
    fun sessions(): Flow<List<WorkoutSession>>
    fun session(id: Long): Flow<WorkoutSession?>

    /** The single in-progress session, if any (for resume-on-launch). */
    fun activeSession(): Flow<WorkoutSession?>

    /** Inserts a new session (id == 0) or replaces an existing one, rewriting its exercise/set graph. */
    suspend fun save(session: WorkoutSession): Long
    suspend fun delete(id: Long)
}
