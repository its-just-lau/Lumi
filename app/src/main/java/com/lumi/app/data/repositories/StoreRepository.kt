package com.lumi.app.data.repositories

import com.lumi.app.core.network.StoreService
import com.lumi.app.data.models.store.InventoryItemResponse
import com.lumi.app.data.models.store.PurchaseRequest
import com.lumi.app.data.models.store.PurchaseResponse
import com.lumi.app.data.models.store.StoreItemResponse
import kotlinx.coroutines.CancellationException

class StoreRepository(
    private val storeService: StoreService
) {

    suspend fun getItems(): Result<List<StoreItemResponse>> {
        return safeCall { storeService.getItems().items.orEmpty() }
    }

    suspend fun getInventory(): Result<List<InventoryItemResponse>> {
        return safeCall { storeService.getInventory().items.orEmpty() }
    }

    suspend fun purchase(
        itemId: String,
        idempotencyKey: String,
        quantity: Int = 1
    ): Result<PurchaseResponse> {
        return safeCall {
            storeService.purchase(idempotencyKey, PurchaseRequest(itemId, quantity))
        }
    }

    private suspend fun <T> safeCall(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}
