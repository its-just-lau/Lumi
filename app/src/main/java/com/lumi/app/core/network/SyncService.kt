package com.lumi.app.core.network

import retrofit2.http.GET

interface SyncService {

    @GET("sync/state")
    suspend fun getSyncState(): SyncStateResponse
}

data class SyncStateResponse(
    val serverTime: String,
    val syncRunToken: String,
    val expiresAt: String,
    val collections: List<SyncCollection>
)

data class SyncCollection(
    val name: String,
    val order: Int
)
