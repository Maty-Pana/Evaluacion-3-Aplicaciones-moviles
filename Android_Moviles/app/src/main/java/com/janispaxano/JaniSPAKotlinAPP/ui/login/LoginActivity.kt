package com.janispaxano.JaniSPAKotlinAPP.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.janispaxano.JaniSPAKotlinAPP.R
import com.janispaxano.JaniSPAKotlinAPP.data.remote.auth.LoginRequest
import com.janispaxano.JaniSPAKotlinAPP.data.remote.auth.RetrofitClient
import com.janispaxano.JaniSPAKotlinAPP.data.remote.auth.TokenManager
import com.janispaxano.JaniSPAKotlinAPP.data.remote.user.UserRetrofitClient
import com.janispaxano.JaniSPAKotlinAPP.databinding.ActivityLoginBinding
import com.janispaxano.JaniSPAKotlinAPP.ui.admin.AdminHomeActivity
import com.janispaxano.JaniSPAKotlinAPP.ui.cliente.ClienteHomeActivity
import com.janispaxano.JaniSPAKotlinAPP.ui.models.UserManagement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar TokenManager
        tokenManager = TokenManager(this)

        // Verificar si ya está logueado
        if (tokenManager.isLoggedIn()) {
            goToHomeBasedOnRole()
            return
        }

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarLogin)
        setSupportActionBar(toolbar)

        // Asegurar que el título sea blanco
        supportActionBar?.setDisplayShowTitleEnabled(true)
        toolbar.setTitleTextColor(android.graphics.Color.WHITE)

        // 🟢 Acción del botón Iniciar Sesión
        binding.btnLogin.setOnClickListener {
            val email = binding.etUsuario.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Llamada a Xano
            loginUser(email, password)
        }
    }

    private fun loginUser(email: String, password: String) {
        val request = LoginRequest(email, password)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Paso 1: Login para obtener el token
                val loginResponse = RetrofitClient.api.login(request)

                if (loginResponse.isSuccessful) {
                    val loginData = loginResponse.body()
                    if (loginData != null && loginData.authToken != null) {

                        // Guardar el token primero
                        tokenManager.saveToken(loginData.authToken)

                        // ---------- NUEVO: validar status del usuario ANTES de completar login ----------
                        // Intentar obtener userId desde la respuesta de login
                        val userIdFromLogin: Int = loginData.user?.id ?: -1

                        if (userIdFromLogin != -1) {
                            // Consultar el endpoint de usuarios por ID (UserRetrofitClient) con auth
                            val userByIdResponse = UserRetrofitClient.apiWithAuth(loginData.authToken).getUserById(userIdFromLogin)

                            withContext(Dispatchers.Main) {
                                if (userByIdResponse.isSuccessful && userByIdResponse.body() != null) {
                                    val userMgmt: UserManagement = userByIdResponse.body()!!

                                    // Validar campo status (ej: "habilitado" / "bloqueado")
                                    val status = userMgmt.status.lowercase().trim()
                                    if (status != "habilitado") {
                                        // Usuario no habilitado -> cancelar login
                                        tokenManager.clearToken()
                                        Toast.makeText(
                                            this@LoginActivity,
                                            "Usuario bloqueado o inhabilitado (status: $status)",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        return@withContext
                                    }

                                    // Si está habilitado, guardar datos esenciales del usuario y continuar
                                    tokenManager.saveUserData(userMgmt.name, userMgmt.email, userMgmt.id, userMgmt.role)

                                    Toast.makeText(this@LoginActivity, "Login exitoso", Toast.LENGTH_SHORT).show()
                                    goToHomeBasedOnRole()

                                } else {
                                    // No se pudo obtener info por id -> limpiar token y notificar
                                    tokenManager.clearToken()
                                    Toast.makeText(this@LoginActivity, "Error al verificar estado del usuario", Toast.LENGTH_SHORT).show()
                                }
                            }

                        } else {
                            // Si el login no devolvió user.id, intentar con /auth/me como antes
                            val apiWithAuth = RetrofitClient.apiWithAuth(loginData.authToken)
                            val userResponse = apiWithAuth.getUser()

                            withContext(Dispatchers.Main) {
                                if (userResponse.isSuccessful && userResponse.body() != null) {
                                    val userData = userResponse.body()!!

                                    // Intentar obtener la versión completa del usuario por ID para revisar status
                                    try {
                                        // Llamada a UserRetrofitClient para obtener status/otros campos
                                        val userMgmtResponse = UserRetrofitClient.apiWithAuth(loginData.authToken).getUserById(userData.id)

                                        if (userMgmtResponse.isSuccessful && userMgmtResponse.body() != null) {
                                            val userMgmt = userMgmtResponse.body()!!
                                            val status = userMgmt.status.lowercase().trim()
                                            if (status != "habilitado") {
                                                tokenManager.clearToken()
                                                Toast.makeText(
                                                    this@LoginActivity,
                                                    "Usuario bloqueado o inhabilitado (status: $status)",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                return@withContext
                                            }

                                            // Guardar datos desde UserManagement (más completos)
                                            tokenManager.saveUserData(userMgmt.name, userMgmt.email, userMgmt.id, userMgmt.role)
                                            Toast.makeText(this@LoginActivity, "Login exitoso", Toast.LENGTH_SHORT).show()
                                            goToHomeBasedOnRole()

                                        } else {
                                            // No pudimos obtener la info completa por id -> guardar lo básico y continuar
                                            tokenManager.saveUserData(userData)
                                            Toast.makeText(this@LoginActivity, "Login exitoso", Toast.LENGTH_SHORT).show()
                                            goToHomeBasedOnRole()
                                        }

                                    } catch (e: Exception) {
                                        // En caso de error al consultar byId, limpiar token y notificar
                                        tokenManager.clearToken()
                                        Toast.makeText(this@LoginActivity, "Error al verificar estado del usuario: ${e.message}", Toast.LENGTH_LONG).show()
                                    }

                                } else {
                                    tokenManager.clearToken()
                                    Toast.makeText(this@LoginActivity, "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        // ---------- FIN NUEVO ---------------------------------------------------------

                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@LoginActivity, "Error: No se recibió token de autenticación", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@LoginActivity, "Error en login: ${loginResponse.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun goToHomeBasedOnRole() {
        val userRole = tokenManager.getUserRole()
        val userId = tokenManager.getUserId()
        val userEmail = tokenManager.getUserEmail()

        // Debug: Imprimir información del usuario
        println("=== DEBUG LOGIN ===")
        println("Usuario ID: $userId")
        println("Usuario Email: $userEmail")
        println("Usuario Rol: '$userRole'")
        println("Rol es null: ${userRole == null}")
        println("Rol es vacío: ${userRole.isNullOrEmpty()}")
        println("==================")

        // Lógica de navegación simplificada y robusta
        try {
            val intent: Intent

            if (userRole != null && userRole.lowercase().trim() == "admin") {
                println("Navegando a AdminHomeActivity")
                intent = Intent(this, AdminHomeActivity::class.java)
                Toast.makeText(this, "Bienvenido Admin", Toast.LENGTH_SHORT).show()
            } else {
                println("Navegando a ClienteHomeActivity")
                intent = Intent(this, ClienteHomeActivity::class.java)
                Toast.makeText(this, "Bienvenido Cliente", Toast.LENGTH_SHORT).show()
            }

            // Siempre navegar
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            println("Navegación iniciada exitosamente")
            finish()

        } catch (e: Exception) {
            println("Error en navegación: ${e.message}")
            // Si hay cualquier error, forzar navegación a ClienteHomeActivity
            val fallbackIntent = Intent(this, ClienteHomeActivity::class.java)
            fallbackIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(fallbackIntent)
            finish()
        }
    }
}