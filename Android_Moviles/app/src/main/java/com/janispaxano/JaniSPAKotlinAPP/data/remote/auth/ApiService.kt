package com.janispaxano.JaniSPAKotlinAPP.data.remote.auth

import retrofit2.Response
import retrofit2.http.*

// Modelo de solicitud (login)
data class LoginRequest(
    val email: String,
    val password: String
)

// Modelo de respuesta
data class LoginResponse(
    val authToken: String?,
    val user: User?
)

// Modelo de usuario (agregando role)
data class User(
    val id: Int,
    val name: String?,
    val email: String?,
    val role: String? = null
)

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("auth/me")
    suspend fun getUser(): Response<User>
}
