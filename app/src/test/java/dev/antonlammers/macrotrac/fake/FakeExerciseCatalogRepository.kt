package dev.antonlammers.macrotrac.fake

import dev.antonlammers.macrotrac.domain.model.Exercise
import dev.antonlammers.macrotrac.domain.repository.ExerciseCatalogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeExerciseCatalogRepository : ExerciseCatalogRepository {

    private val _exercises = MutableStateFlow<List<Exercise>>(emptyList())

    override fun exercises(): Flow<List<Exercise>> = _exercises.map { it.sortedBy { e -> e.name } }

    override fun exercise(stableId: String): Flow<Exercise?> =
        _exercises.map { list -> list.firstOrNull { it.stableId == stableId } }

    override suspend fun upsertAll(exercises: List<Exercise>) {
        _exercises.update { current ->
            val byStable = current.associateBy { it.stableId }.toMutableMap()
            exercises.forEach { byStable[it.stableId] = it }
            byStable.values.toList()
        }
    }

    override suspend fun delete(stableId: String) {
        _exercises.update { it.filterNot { e -> e.stableId == stableId } }
    }
}
