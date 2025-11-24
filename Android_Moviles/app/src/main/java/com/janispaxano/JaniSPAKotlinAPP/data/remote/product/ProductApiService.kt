package com.janispaxano.JaniSPAKotlinAPP.data.remote.product

import com.janispaxano.JaniSPAKotlinAPP.ui.models.Product
import com.janispaxano.JaniSPAKotlinAPP.data.remote.products.UpdateProductRequest
import com.janispaxano.JaniSPAKotlinAPP.data.remote.products.CreateProductRequest
import com.janispaxano.JaniSPAKotlinAPP.data.remote.products.ImageUploadResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ProductApiService {
    @GET("product")
    suspend fun getProducts(): Response<List<Product>>

    @GET("product/{product_id}")
    suspend fun getProduct(@Path("product_id") productId: Int): Response<Product>

    @PATCH("product/{product_id}")
    suspend fun updateProduct(
        @Path("product_id") productId: Int,
        @Body product: UpdateProductRequest
    ): Response<Product>

    @DELETE("product/{product_id}")
    suspend fun deleteProduct(@Path("product_id") productId: Int): Response<Unit>

    @Multipart
    @POST("upload/image")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): Response<ImageUploadResponse>

    @POST("product")
    suspend fun createProduct(
        @Body product: CreateProductRequest
    ): Response<Product>
}
