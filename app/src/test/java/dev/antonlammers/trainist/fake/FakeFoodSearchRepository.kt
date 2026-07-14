package dev.antonlammers.trainist.fake

import dev.antonlammers.trainist.domain.model.Food
import dev.antonlammers.trainist.domain.repository.FoodSearchRepository

class FakeFoodSearchRepository(
    private val barcodeResult: Result<Food?> = Result.success(null),
) : FoodSearchRepository {

    var lastBarcode: String? = null

    override suspend fun getByBarcode(barcode: String): Result<Food?> {
        lastBarcode = barcode
        return barcodeResult
    }
}
