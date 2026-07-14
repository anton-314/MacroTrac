package dev.antonlammers.trainist.domain.repository

import dev.antonlammers.trainist.domain.model.Food
import kotlinx.coroutines.flow.Flow

interface CustomFoodRepository {
    fun allFoods(): Flow<List<Food>>
    suspend fun save(food: Food): Food
    suspend fun update(food: Food)
    suspend fun delete(id: Long)
}
