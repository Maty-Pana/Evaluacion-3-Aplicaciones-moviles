package com.janispaxano.JaniSPAKotlinAPP.data.remote.products

import com.google.gson.annotations.SerializedName

data class ImageUploadResponse(
    @SerializedName("access")
    val access: String,

    @SerializedName("path")
    val path: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("size")
    val size: Int,

    @SerializedName("mime")
    val mime: String,

    @SerializedName("meta")
    val meta: ImageMeta,

    @SerializedName("url")
    val url: String? = null
) {
    // Función helper para obtener la URL completa de la imagen
    fun getFullUrl(): String {
        return url ?: "https://x8ki-letl-twmt.n7.xano.io$path"
    }
}

data class ImageMeta(
    @SerializedName("width")
    val width: Int,

    @SerializedName("height")
    val height: Int
)
