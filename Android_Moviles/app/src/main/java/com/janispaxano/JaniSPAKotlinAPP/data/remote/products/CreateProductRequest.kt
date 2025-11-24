package com.janispaxano.JaniSPAKotlinAPP.data.remote.products

import com.google.gson.annotations.SerializedName

data class CreateProductRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("price")
    val price: Double,

    @SerializedName("stock")
    val stock: Int,

    @SerializedName("category")
    val category: String,

    @SerializedName("image")
    val image: List<ImageUploadResponse>? = null
)
