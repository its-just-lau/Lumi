package com.lumi.app.data.models.store

import com.lumi.app.data.models.tasks.WalletResponse

data class StoreItemResponse(
    val id: String,
    val name: String,
    val description: String? = null,
    val priceGems: Int = 0,
    val type: String? = null
)

data class InventoryItemResponse(
    val itemId: String? = null,
    val quantity: Int = 0
)

data class PurchaseRequest(
    val itemId: String,
    val quantity: Int = 1
)

data class PurchaseResponse(
    val wallet: WalletResponse? = null,
    val inventory: InventoryItemResponse? = null
)
