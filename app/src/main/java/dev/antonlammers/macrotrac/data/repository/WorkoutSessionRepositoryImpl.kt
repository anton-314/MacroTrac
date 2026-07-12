package dev.antonlammers.macrotrac.data.repository

import dev.antonlammers.macrotrac.data.local.dao.WorkoutSessionDao
import dev.antonlammers.macrotrac.data.repository.WorkoutMappers.setEntities
import dev.antonlammers.macrotrac.data.repository.WorkoutMappers.toDomain
import dev.antonlammers.macrotrac.data.repository.WorkoutMappers.toEntity
import dev.antonlammers.macrotrac.domain.model.WorkoutSession
import dev.antonlammers.macrotrac.domain.repository.WorkoutSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WorkoutSessionRepositoryImpl @Inject constructor(
    private val dao: WorkoutSessionDao,
    private val runner: TransactionRunner,
) : WorkoutSessionRepository {

    override fun sessions(): Flow<List<WorkoutSession>> =
        dao.allSessions().map { list -> list.map { it.toDomain() } }

    override fun session(id: Long): Flow<WorkoutSession?> =
        dao.sessionById(id).map { it?.toDomain() }

    override fun activeSession(): Flow<WorkoutSession?> =
        dao.activeSession().map { it?.toDomain() }

    override suspend fun save(session: WorkoutSession): Long = runner.transaction {
        val sessionId = dao.insertSession(session.toEntity())
        // Rewrite the whole exercise/set graph (the replace/FK cascade clears the old rows).
        dao.deleteSessionExercises(sessionId)
        session.exercises.forEachIndexed { index, exercise ->
            val sessionExerciseId = dao.insertSessionExercise(exercise.toEntity(sessionId, index))
            dao.insertSets(exercise.setEntities(sessionExerciseId))
        }
        sessionId
    }

    override suspend fun delete(id: Long) = dao.deleteSession(id)
}
