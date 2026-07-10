package dev.antonlammers.macrotrac.data.repository

import dev.antonlammers.macrotrac.data.local.dao.ExerciseDao
import dev.antonlammers.macrotrac.data.repository.WorkoutMappers.toDomain
import dev.antonlammers.macrotrac.data.repository.WorkoutMappers.toEntity
import dev.antonlammers.macrotrac.domain.model.Exercise
import dev.antonlammers.macrotrac.domain.repository.ExerciseCatalogRepository
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
