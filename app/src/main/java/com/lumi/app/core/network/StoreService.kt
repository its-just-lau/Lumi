package com.lumi.app.core.network

import com.lumi.app.data.models.learning.ItemsResponse
import com.lumi.app.data.models.store.InventoryItemResponse
import com.lumi.app.data.models.store.PurchaseRequest
import com.lumi.app.data.models.store.PurchaseResponse
import com.lumi.app.data.models.store.StoreItemResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface StoreService {

    @GET("store/items")
    suspend fun getItems(): ItemsResponse<StoreItemResponse>

    @GET("store/inventory")
    suspend fun getInventory(): ItemsResponse<InventoryItemResponse>

    @POST("store/purchases")
    suspend fun purchase(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body body: PurchaseRequest
    ): PurchaseResponse
}
