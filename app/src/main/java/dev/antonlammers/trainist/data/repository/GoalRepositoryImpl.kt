package dev.antonlammers.trainist.data.repository

import dev.antonlammers.trainist.data.local.dao.DailyGoalDao
import dev.antonlammers.trainist.data.local.entity.DailyGoalEntity
import dev.antonlammers.trainist.domain.model.DailyGoal
import dev.antonlammers.trainist.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GoalRepositoryImpl @Inject constructor(
    private val dao: DailyGoalDao,
) : GoalRepository {

    override fun goal(): Flow<DailyGoal> = dao.goal().map { entity ->
        entity?.toDomain() ?: DailyGoal()
    }

    override suspend fun save(goal: DailyGoal) = dao.save(goal.toEntity())

    private fun DailyGoalEntity.toDomain() = DailyGoal(
        kcal = kcal,
        proteinG = proteinG,
        carbsG = carbsG,
        fatG = fatG,
        targetWeightKg = targetWeightKg,
    )

    private fun DailyGoal.toEntity() = DailyGoalEntity(
        kcal = kcal,
        proteinG = proteinG,
        carbsG = carbsG,
        fatG = fatG,
        targetWeightKg = targetWeightKg,
    )
}
