package com.janispaxano.JaniSPAKotlinAPP.ui.models

data class UserManagement(
    val id: Int,
    val created_at: Long,
    val name: String,
    val email: String?,
    val first_name: String,
    val last_name: String,
    val role: String,
    val status: String,
    val shipping_address: String?,
    val phone_number: String?
)

data class CreateUserRequest(
    val name: String,
    val email: String?,
    val first_name: String,
    val last_name: String,
    val role: String,
    val status: String,
    val shipping_address: String?,
    val phone_number: String?
)

data class UpdateUserRequest(
    val name: String,
    val email: String?,
    val first_name: String,
    val last_name: String,
    val role: String,
    val status: String,
    val shipping_address: String?,
    val phone_number: String?
)

