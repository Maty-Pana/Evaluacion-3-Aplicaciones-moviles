package com.janispaxano.JaniSPAKotlinAPP.data.remote.product

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Retrofit Client para productos
object ProductRetrofitClient {
    private const val BASE_URL = "https://x8ki-letl-twmt.n7.xano.io/api:VM5130cM/"
    private const val TAG = "ProductRetrofitClient"

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()
        .also {
            Log.d(TAG, "OkHttpClient creado con timeouts de 120 segundos")
        }

    val api: ProductApiService by lazy {
        Log.d(TAG, "Inicializando ProductApiService con BASE_URL: $BASE_URL")
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ProductApiService::class.java)
    }
}
