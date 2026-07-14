package dev.antonlammers.trainist.domain.repository

import dev.antonlammers.trainist.domain.model.Food

interface FoodSearchRepository {
    suspend fun getByBarcode(barcode: String): Result<Food?>
}
