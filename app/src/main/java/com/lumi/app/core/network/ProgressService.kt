package com.lumi.app.core.network

import com.lumi.app.data.models.tasks.StreakResponse
import com.lumi.app.data.models.tasks.WalletResponse
import com.google.gson.JsonObject
import retrofit2.http.GET

interface ProgressService {

    @GET("wallet")
    suspend fun getWallet(): WalletResponse

    @GET("progress/streak")
    suspend fun getStreak(): StreakResponse

    // El contrato aún no define los campos; se lee de forma tolerante (ver ProgressRepository)
    @GET("progress")
    suspend fun getProgress(): JsonObject

    @GET("progress/weekly")
    suspend fun getWeekly(): JsonObject
}
