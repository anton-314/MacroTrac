package dev.antonlammers.trainist.fake

import dev.antonlammers.trainist.domain.model.Food
import dev.antonlammers.trainist.domain.repository.CustomFoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeCustomFoodRepository : CustomFoodRepository {

    private val _foods = MutableStateFlow<List<Food>>(emptyList())
    private var nextId = 1L

    override fun allFoods(): Flow<List<Food>> =
        _foods.map { it.sortedBy { f -> f.name } }

    override suspend fun save(food: Food): Food {
        val saved = food.copy(id = (nextId++).toString())
        _foods.update { it + saved }
        return saved
    }

    override suspend fun update(food: Food) {
        _foods.update { list -> list.map { if (it.id == food.id) food else it } }
    }

    override suspend fun delete(id: Long) {
        _foods.update { it.filterNot { f -> f.id == id.toString() } }
    }
}
