package dev.antonlammers.macrotrac.fake

import dev.antonlammers.macrotrac.domain.model.WorkoutTemplate
import dev.antonlammers.macrotrac.domain.repository.WorkoutTemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeWorkoutTemplateRepository : WorkoutTemplateRepository {

    private val _templates = MutableStateFlow<List<WorkoutTemplate>>(emptyList())
    private var nextId = 1L

    override fun templates(): Flow<List<WorkoutTemplate>> = _templates.map { it.sortedBy { t -> t.name } }

    override fun template(id: Long): Flow<WorkoutTemplate?> =
        _templates.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun save(template: WorkoutTemplate): Long {
        val id = if (template.id == 0L) nextId++ else template.id
        val normalized = template.copy(
            id = id,
            exercises = template.exercises.mapIndexed { index, e -> e.copy(position = index) },
        )
        _templates.update { list -> list.filterNot { it.id == id } + normalized }
        return id
    }

    override suspend fun delete(id: Long) {
        _templates.update { it.filterNot { t -> t.id == id } }
    }
}
