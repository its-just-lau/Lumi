package com.lumi.app.core.network

import retrofit2.http.GET
import retrofit2.http.Header

interface SyncService {

    @GET("sync/state")
    suspend fun getSyncState(
        @Header("Authorization") authorization: String
    ): SyncStateResponse
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
