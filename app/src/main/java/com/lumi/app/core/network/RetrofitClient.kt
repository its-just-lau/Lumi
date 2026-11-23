package com.lumi.app.core.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // Dirección temporal para pruebas locales.
    // Se reemplazará cuando Mike confirme la dirección del backend.
    private const val BASE_URL = "http://192.168.1.2:4010/"

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
