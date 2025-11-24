package com.janispaxano.JaniSPAKotlinAPP.data.remote.auth

import android.content.Context
import android.content.SharedPreferences
import com.janispaxano.JaniSPAKotlinAPP.data.local.CartManager

class TokenManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val cartManager = CartManager(context)

    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_ROLE = "user_role"
    }

    /**
     * Guarda el token de autenticación
     */
    fun saveToken(token: String) {
        sharedPreferences.edit()
            .putString(KEY_TOKEN, token)
            .apply()
    }

    /**
     * Obtiene el token de autenticación
     */
    fun getToken(): String? {
        return sharedPreferences.getString(KEY_TOKEN, null)
    }

    /**
     * Guarda los datos del usuario
     */
    fun saveUserData(user: User) {
        sharedPreferences.edit()
            .putInt(KEY_USER_ID, user.id)
            .putString(KEY_USER_NAME, user.name)
            .putString(KEY_USER_EMAIL, user.email)
            .putString(KEY_USER_ROLE, user.role)
            .apply()
    }

    /**
     * Guarda los datos del usuario individualmente
     */
    fun saveUserData(name: String?, email: String?, userId: Int = -1, role: String? = null) {
        val editor = sharedPreferences.edit()
        if (userId != -1) editor.putInt(KEY_USER_ID, userId)
        if (name != null) editor.putString(KEY_USER_NAME, name)
        if (email != null) editor.putString(KEY_USER_EMAIL, email)
        if (role != null) editor.putString(KEY_USER_ROLE, role)
        editor.apply()
    }

    /**
     * Obtiene el nombre del usuario
     */
    fun getUserName(): String? {
        return sharedPreferences.getString(KEY_USER_NAME, null)
    }

    /**
     * Obtiene el email del usuario
     */
    fun getUserEmail(): String? {
        return sharedPreferences.getString(KEY_USER_EMAIL, null)
    }

    /**
     * Obtiene el ID del usuario
     */
    fun getUserId(): Int {
        return sharedPreferences.getInt(KEY_USER_ID, -1)
    }

    /**
     * Obtiene el rol del usuario
     */
    fun getUserRole(): String? {
        return sharedPreferences.getString(KEY_USER_ROLE, null)
    }

    /**
     * Obtiene todos los datos del usuario como objeto User
     */
    fun getUser(): User? {
        val id = getUserId()
        val name = getUserName()
        val email = getUserEmail()
        val role = getUserRole()

        return if (id != -1) {
            User(id, name, email, role)
        } else null
    }

    /**
     * Verifica si el usuario está autenticado (tiene token válido)
     */
    fun isLoggedIn(): Boolean {
        return !getToken().isNullOrEmpty()
    }

    /**
     * Limpia todos los datos de autenticación y usuario (incluyendo carrito)
     */
    fun clearData() {
        sharedPreferences.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_ROLE)
            .apply()

        // Limpiar carrito automáticamente al cerrar sesión
        cartManager.clearCart()
    }

    /**
     * Limpia solo el token (mantiene datos del usuario)
     */
    fun clearToken() {
        sharedPreferences.edit()
            .remove(KEY_TOKEN)
            .apply()
    }

    /**
     * Guarda la respuesta completa del login
     */
    fun saveLoginResponse(response: LoginResponse) {
        response.authToken?.let { saveToken(it) }
        response.user?.let { saveUserData(it) }
    }

    /**
     * Verifica si el usuario es admin
     */
    fun isAdmin(): Boolean {
        return getUserRole()?.lowercase() == "admin"
    }

    /**
     * Verifica si el usuario es cliente
     */
    fun isCliente(): Boolean {
        return getUserRole()?.lowercase() == "cliente"
    }
}