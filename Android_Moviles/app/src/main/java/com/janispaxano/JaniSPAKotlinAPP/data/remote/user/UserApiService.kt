package com.janispaxano.JaniSPAKotlinAPP.data.remote.user

import com.janispaxano.JaniSPAKotlinAPP.ui.models.CreateUserRequest
import com.janispaxano.JaniSPAKotlinAPP.ui.models.UpdateUserRequest
import com.janispaxano.JaniSPAKotlinAPP.ui.models.UserManagement
import retrofit2.Response
import retrofit2.http.*

interface UserApiService {

    @GET("user")
    suspend fun getAllUsers(): Response<List<UserManagement>>

    @GET("user/{user_id}")
    suspend fun getUserById(@Path("user_id") userId: Int): Response<UserManagement>

    @POST("user")
    suspend fun createUser(@Body request: CreateUserRequest): Response<UserManagement>

    @PATCH("user/{user_id}")
    suspend fun updateUser(
        @Path("user_id") userId: Int,
        @Body request: UpdateUserRequest
    ): Response<UserManagement>

    @DELETE("user/{user_id}")
    suspend fun deleteUser(@Path("user_id") userId: Int): Response<Unit>
}

