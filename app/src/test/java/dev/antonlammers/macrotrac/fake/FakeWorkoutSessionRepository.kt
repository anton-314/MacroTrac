package dev.antonlammers.macrotrac.fake

import dev.antonlammers.macrotrac.domain.model.WorkoutSession
import dev.antonlammers.macrotrac.domain.repository.WorkoutSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeWorkoutSessionRepository : WorkoutSessionRepository {

    private val _sessions = MutableStateFlow<List<WorkoutSession>>(emptyList())
    private var nextId = 1L

    override fun sessions(): Flow<List<WorkoutSession>> =
        _sessions.map { list -> list.sortedWith(compareByDescending<WorkoutSession> { it.date }.thenByDescending { it.startedAtMs }) }

    override fun session(id: Long): Flow<WorkoutSession?> =
        _sessions.map { list -> list.firstOrNull { it.id == id } }

    override fun activeSession(): Flow<WorkoutSession?> =
        _sessions.map { list -> list.firstOrNull { it.isActive } }

    override suspend fun save(session: WorkoutSession): Long {
        // Mirrors Room's unique(stableId) + REPLACE: a fresh insert (id == 0) whose stableId already
        // exists (e.g. re-importing the same backup) replaces that row instead of adding a new one.
        val conflict = if (session.id == 0L) _sessions.value.firstOrNull { it.stableId == session.stableId } else null
        val id = conflict?.id ?: if (session.id == 0L) nextId++ else session.id
        _sessions.update { list ->
            val others = list.filterNot { it.id == id }
            // Encode the invariant the app relies on: at most one active session at a time.
            val cleared = if (session.isActive) others.map { it.copy(isActive = false) } else others
            cleared + session.copy(id = id)
        }
        return id
    }

    override suspend fun delete(id: Long) {
        _sessions.update { it.filterNot { s -> s.id == id } }
    }
}
