package dev.antonlammers.trainist.data.repository

import dev.antonlammers.trainist.data.local.dao.ExerciseDao
import dev.antonlammers.trainist.data.repository.WorkoutMappers.toDomain
import dev.antonlammers.trainist.data.repository.WorkoutMappers.toEntity
import dev.antonlammers.trainist.domain.model.Exercise
import dev.antonlammers.trainist.domain.repository.ExerciseCatalogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExerciseCatalogRepositoryImpl @Inject constructor(
    private val dao: ExerciseDao,
) : ExerciseCatalogRepository {

    override fun exercises(): Flow<List<Exercise>> =
        dao.allExercises().map { list -> list.map { it.toDomain() } }

    override fun exercise(stableId: String): Flow<Exercise?> =
        dao.exerciseByStableId(stableId).map { it?.toDomain() }

    override suspend fun upsertAll(exercises: List<Exercise>) =
        dao.upsertAll(exercises.map { it.toEntity() })

    override suspend fun delete(stableId: String) = dao.delete(stableId)
}
