package com.janispaxano.JaniSPAKotlinAPP.data.remote.order

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object OrderRetrofitClient {
    private const val BASE_URL = "https://x8ki-letl-twmt.n7.xano.io/api:VM5130cM/"

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
