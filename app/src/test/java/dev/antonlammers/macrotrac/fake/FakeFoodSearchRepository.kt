package dev.antonlammers.macrotrac.fake

import dev.antonlammers.macrotrac.domain.model.Food
import dev.antonlammers.macrotrac.domain.repository.FoodSearchRepository

class FakeFoodSearchRepository(
    private val barcodeResult: Result<Food?> = Result.success(null),
) : FoodSearchRepository {

    var lastBarcode: String? = null

    override suspend fun getByBarcode(barcode: String): Result<Food?> {
        lastBarcode = barcode
        return barcodeResult
    }
}
