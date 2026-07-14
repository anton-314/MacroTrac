package dev.antonlammers.trainist.domain.repository

import dev.antonlammers.trainist.domain.model.DailyGoal
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun goal(): Flow<DailyGoal>
    suspend fun save(goal: DailyGoal)
}
