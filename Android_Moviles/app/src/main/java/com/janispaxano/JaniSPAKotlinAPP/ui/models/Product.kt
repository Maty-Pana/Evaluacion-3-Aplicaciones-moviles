package com.janispaxano.JaniSPAKotlinAPP.ui.models

import com.google.gson.annotations.SerializedName
import com.janispaxano.JaniSPAKotlinAPP.data.remote.products.ImageUploadResponse

data class Product(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("created_at")
    val created_at: Long? = null,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("price")
    val price: Double,

    @SerializedName("image")
    val image: List<ImageUploadResponse> = emptyList(),

    @SerializedName("stock")
    val stock: Int = 0,

    @SerializedName("category")
    val category: String = ""
) {
    // Función helper para obtener la URL completa de la primera imagen
    fun getImageUrl(): String? {
        return image.firstOrNull()?.getFullUrl()
    }
}
