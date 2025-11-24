package com.janispaxano.JaniSPAKotlinAPP.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.janispaxano.JaniSPAKotlinAPP.R
import com.janispaxano.JaniSPAKotlinAPP.data.remote.auth.RetrofitClient
import com.janispaxano.JaniSPAKotlinAPP.data.remote.auth.TokenManager
import com.janispaxano.JaniSPAKotlinAPP.data.remote.user.UserRetrofitClient
import com.janispaxano.JaniSPAKotlinAPP.ui.login.LoginActivity
import com.janispaxano.JaniSPAKotlinAPP.ui.cliente.MyOrdersActivity
import com.janispaxano.JaniSPAKotlinAPP.ui.models.UpdateUserRequest
import com.janispaxano.JaniSPAKotlinAPP.ui.models.UserManagement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

    private lateinit var tokenManager: TokenManager
    private var txtUserName: TextView? = null
    private var txtUserEmail: TextView? = null
    private var txtUserRole: TextView? = null
    private var progressBar: ProgressBar? = null
    private var btnViewOrders: Button? = null
    private var btnEditProfile: Button? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Inicializar TokenManager
        tokenManager = TokenManager(requireContext())

        // Obtener referencias a las vistas
        txtUserName = view.findViewById(R.id.txtUserName)
        txtUserEmail = view.findViewById(R.id.txtUserEmail)
        txtUserRole = view.findViewById(R.id.txtUserRole)
        progressBar = view.findViewById(R.id.progressBar)
        btnViewOrders = view.findViewById(R.id.btnViewOrders)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)

        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        // Mostrar botón de órdenes solo para clientes
        val userRole = tokenManager.getUserRole()
        if (userRole == "cliente") {
            btnViewOrders?.visibility = View.VISIBLE
        }

        // Cargar datos del usuario
        loadUserData()

        btnViewOrders?.setOnClickListener {
            val intent = Intent(requireContext(), MyOrdersActivity::class.java)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            logout()
        }

        btnEditProfile?.setOnClickListener {
            openEditProfileDialog()
        }

        return view
    }

    private fun loadUserData() {
        // Mostrar datos del TokenManager local
        val userName = tokenManager.getUserName()
        val userEmail = tokenManager.getUserEmail()
        val userId = tokenManager.getUserId()
        val userRole = tokenManager.getUserRole()

        if (!userName.isNullOrEmpty()) {
            txtUserName?.text = userName
        } else {
            txtUserName?.text = getString(R.string.user_placeholder, userId)
        }

        txtUserRole?.text = userRole ?: "Rol no disponible"

        if (!userEmail.isNullOrEmpty()) {
            txtUserEmail?.text = userEmail
        } else {
            txtUserEmail?.text = getString(R.string.email_not_available)
        }

        // Opcional: Cargar datos actualizados del servidor
        loadUserDataFromServer()
    }

    private fun loadUserDataFromServer() {
        val authToken = tokenManager.getToken()
        if (authToken == null) {
            Log.e("ProfileFragment", "No auth token available")
            return
        }

        lifecycleScope.launch {
            try {
                progressBar?.visibility = View.VISIBLE

                withContext(Dispatchers.IO) {
                    val api = RetrofitClient.apiWithAuth(authToken)
                    val response = api.getUser()

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful && response.body() != null) {
                            val user = response.body()!!
                            txtUserName?.text = user.name
                            txtUserEmail?.text = user.email

                            // Actualizar datos locales
                            tokenManager.saveUserData(user)

                            // Intentar actualizar rol desde tokenManager (si existe)
                            txtUserRole?.text = tokenManager.getUserRole() ?: "-"
                        } else {
                            Log.e("ProfileFragment", "Error loading user data: ${response.code()}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileFragment", "Exception loading user data", e)
            } finally {
                progressBar?.visibility = View.GONE
            }
        }
    }

    private fun openEditProfileDialog() {
        val authToken = tokenManager.getToken()
        val userId = tokenManager.getUserId()

        if (authToken.isNullOrEmpty() || userId == -1) {
            Toast.makeText(requireContext(), getString(R.string.cannot_edit_user), Toast.LENGTH_SHORT).show()
            return
        }

        // Primero obtener la info completa del usuario para prellenar
        lifecycleScope.launch {
            progressBar?.visibility = View.VISIBLE
            try {
                val userResponse = withContext(Dispatchers.IO) {
                    UserRetrofitClient.apiWithAuth(authToken).getUserById(userId)
                }

                if (userResponse.isSuccessful && userResponse.body() != null) {
                    val userMgmt = userResponse.body()!!

                    // Mostrar diálogo y prellenar campos
                    showEditDialogWithUser(userMgmt, authToken)

                } else {
                    Toast.makeText(requireContext(), getString(R.string.could_not_get_user_for_edit), Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al obtener usuario: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                progressBar?.visibility = View.GONE
            }
        }
    }

    private fun showEditDialogWithUser(userMgmt: UserManagement, authToken: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null)
        val etFirstName = dialogView.findViewById<EditText>(R.id.etFirstName)
        val etLastName = dialogView.findViewById<EditText>(R.id.etLastName)
        val etEmail = dialogView.findViewById<EditText>(R.id.etEmail)

        // Prefill
        etFirstName.setText(userMgmt.first_name)
        etLastName.setText(userMgmt.last_name)
        etEmail.setText(userMgmt.email)

        // Crear dialog custom
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Referencias a botones del layout custom
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelDialog)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveDialog)

        // Asegurar textos desde resources
        btnCancel.text = getString(R.string.cancel).uppercase()
        btnSave.text = getString(R.string.save).uppercase()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val newFirst = etFirstName.text.toString().trim()
            val newLast = etLastName.text.toString().trim()
            val newEmail = etEmail.text.toString().trim()

            if (newFirst.isEmpty() || newLast.isEmpty() || newEmail.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Construir UpdateUserRequest (mantener role/status actuales)
            val updateRequest = UpdateUserRequest(
                name = "${newFirst} ${newLast}",
                email = newEmail,
                first_name = newFirst,
                last_name = newLast,
                role = userMgmt.role,
                status = userMgmt.status,
                shipping_address = userMgmt.shipping_address,
                phone_number = userMgmt.phone_number
            )

            // Llamar a la API para actualizar
            lifecycleScope.launch {
                progressBar?.visibility = View.VISIBLE
                try {
                    val updateResponse = withContext(Dispatchers.IO) {
                        UserRetrofitClient.apiWithAuth(authToken).updateUser(userMgmt.id, updateRequest)
                    }

                    if (updateResponse.isSuccessful && updateResponse.body() != null) {
                        val updatedUser = updateResponse.body()!!

                        // Actualizar UI y TokenManager
                        txtUserName?.text = updatedUser.name
                        txtUserEmail?.text = updatedUser.email
                        txtUserRole?.text = updatedUser.role

                        tokenManager.saveUserData(updatedUser.name, updatedUser.email, updatedUser.id, updatedUser.role)

                        Toast.makeText(requireContext(), getString(R.string.data_updated), Toast.LENGTH_SHORT).show()
                        dialog.dismiss()

                    } else {
                        Toast.makeText(requireContext(), "Error al actualizar datos: ${updateResponse.code()}", Toast.LENGTH_LONG).show()
                    }

                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error al actualizar: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    progressBar?.visibility = View.GONE
                }
            }
        }

        dialog.show()

        // Ajustes estéticos: fondo transparente y ancho match_parent para que el card luzca como overlay
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    private fun logout() {
        try {
            // Limpiar datos de autenticación y carrito usando el método original
            tokenManager.clearData()

            // Navegar a LoginActivity
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            // Cerrar la actividad actual
            requireActivity().finish()

            Toast.makeText(requireContext(), "Sesión cerrada exitosamente", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error during logout", e)
            Toast.makeText(requireContext(), "Error al cerrar sesión", Toast.LENGTH_SHORT).show()
        }
    }
}