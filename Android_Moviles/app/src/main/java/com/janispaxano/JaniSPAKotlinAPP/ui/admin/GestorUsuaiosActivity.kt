package com.janispaxano.JaniSPAKotlinAPP.ui.admin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.janispaxano.JaniSPAKotlinAPP.R
import com.janispaxano.JaniSPAKotlinAPP.data.remote.user.UserRetrofitClient
import com.janispaxano.JaniSPAKotlinAPP.ui.adapters.UserAdapter
import com.janispaxano.JaniSPAKotlinAPP.ui.models.CreateUserRequest
import com.janispaxano.JaniSPAKotlinAPP.ui.models.UpdateUserRequest
import com.janispaxano.JaniSPAKotlinAPP.ui.models.UserManagement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GestorUsuaiosActivity : AppCompatActivity() {

    private lateinit var rvUsers: RecyclerView
    private lateinit var etSearchUser: EditText
    private lateinit var btnCreateUser: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var userAdapter: UserAdapter

    private var allUsers = listOf<UserManagement>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_gestor_usuaios)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupRecyclerView()
        setupSearchFilter()
        setupListeners()
        loadUsers()
    }

    private fun initViews() {
        rvUsers = findViewById(R.id.rvUsers)
        etSearchUser = findViewById(R.id.etSearchUser)
        btnCreateUser = findViewById(R.id.btnCreateUser)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(
            users = emptyList(),
            onEditClick = { user -> showEditUserDialog(user) },
            onDeleteClick = { user -> showDeleteConfirmation(user) },
            onToggleStatusClick = { user -> toggleUserStatus(user) }
        )

        rvUsers.apply {
            layoutManager = LinearLayoutManager(this@GestorUsuaiosActivity)
            adapter = userAdapter
        }
    }

    private fun setupSearchFilter() {
        etSearchUser.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterUsers(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterUsers(query: String) {
        val filteredUsers = if (query.isEmpty()) {
            allUsers
        } else {
            allUsers.filter { user ->
                user.name.contains(query, ignoreCase = true) ||
                user.email?.contains(query, ignoreCase = true) == true ||
                user.role.contains(query, ignoreCase = true)
            }
        }
        userAdapter.updateUsers(filteredUsers)
    }

    private fun setupListeners() {
        btnCreateUser.setOnClickListener {
            showCreateUserDialog()
        }
    }

    private fun loadUsers() {
        showLoading(true)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = UserRetrofitClient.api.getAllUsers()
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    if (response.isSuccessful) {
                        allUsers = response.body() ?: emptyList()
                        userAdapter.updateUsers(allUsers)
                    } else {
                        showError("Error al cargar usuarios: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showError("Error de conexión: ${e.message}")
                }
            }
        }
    }

    private fun showCreateUserDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_user, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        setupUserDialog(dialogView, dialog, null)
        dialog.show()
    }

    private fun showEditUserDialog(user: UserManagement) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_user, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        setupUserDialog(dialogView, dialog, user)
        dialog.show()
    }

    private fun setupUserDialog(dialogView: View, dialog: AlertDialog, user: UserManagement?) {
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val etUserName = dialogView.findViewById<TextInputEditText>(R.id.etUserName)
        val etEmail = dialogView.findViewById<TextInputEditText>(R.id.etEmail)
        val etFirstName = dialogView.findViewById<TextInputEditText>(R.id.etFirstName)
        val etLastName = dialogView.findViewById<TextInputEditText>(R.id.etLastName)
        val spinnerRole = dialogView.findViewById<Spinner>(R.id.spinnerRole)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)

        // Configurar spinner de roles
        val roles = arrayOf("admin", "cliente")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole.adapter = adapter

        // Si estamos editando, rellenar los campos
        user?.let {
            tvTitle.text = "Editar Usuario"
            etUserName.setText(it.name)
            etEmail.setText(it.email)
            etFirstName.setText(it.first_name)
            etLastName.setText(it.last_name)

            val rolePosition = roles.indexOf(it.role)
            if (rolePosition >= 0) {
                spinnerRole.setSelection(rolePosition)
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val userName = etUserName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val role = spinnerRole.selectedItem.toString()

            if (userName.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos requeridos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (user == null) {
                createUser(userName, email.ifEmpty { null }, firstName, lastName, role)
            } else {
                updateUser(user.id, userName, email.ifEmpty { null }, firstName, lastName, role, user.status)
            }

            dialog.dismiss()
        }
    }

    private fun createUser(
        userName: String,
        email: String?,
        firstName: String,
        lastName: String,
        role: String
    ) {
        showLoading(true)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = CreateUserRequest(
                    name = userName,
                    email = email,
                    first_name = firstName,
                    last_name = lastName,
                    role = role,
                    status = "habilitado",
                    shipping_address = null,
                    phone_number = null
                )

                val response = UserRetrofitClient.api.createUser(request)
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    if (response.isSuccessful) {
                        Toast.makeText(this@GestorUsuaiosActivity, "Usuario creado exitosamente", Toast.LENGTH_SHORT).show()
                        loadUsers()
                    } else {
                        showError("Error al crear usuario: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showError("Error de conexión: ${e.message}")
                }
            }
        }
    }

    private fun updateUser(
        userId: Int,
        userName: String,
        email: String?,
        firstName: String,
        lastName: String,
        role: String,
        status: String
    ) {
        showLoading(true)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = UpdateUserRequest(
                    name = userName,
                    email = email,
                    first_name = firstName,
                    last_name = lastName,
                    role = role,
                    status = status,
                    shipping_address = null,
                    phone_number = null
                )

                val response = UserRetrofitClient.api.updateUser(userId, request)
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    if (response.isSuccessful) {
                        Toast.makeText(this@GestorUsuaiosActivity, "Usuario actualizado exitosamente", Toast.LENGTH_SHORT).show()
                        loadUsers()
                    } else {
                        showError("Error al actualizar usuario: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showError("Error de conexión: ${e.message}")
                }
            }
        }
    }

    private fun showDeleteConfirmation(user: UserManagement) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Usuario")
            .setMessage("¿Estás seguro de que deseas eliminar al usuario ${user.name}?")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteUser(user.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteUser(userId: Int) {
        showLoading(true)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = UserRetrofitClient.api.deleteUser(userId)
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    if (response.isSuccessful) {
                        Toast.makeText(this@GestorUsuaiosActivity, "Usuario eliminado exitosamente", Toast.LENGTH_SHORT).show()
                        loadUsers()
                    } else {
                        showError("Error al eliminar usuario: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showError("Error de conexión: ${e.message}")
                }
            }
        }
    }

    private fun toggleUserStatus(user: UserManagement) {
        val newStatus = if (user.status.equals("bloqueado", ignoreCase = true)) {
            "habilitado"
        } else {
            "bloqueado"
        }

        val actionText = if (newStatus == "bloqueado") "bloquear" else "desbloquear"

        AlertDialog.Builder(this)
            .setTitle("Cambiar Estado")
            .setMessage("¿Estás seguro de que deseas $actionText al usuario ${user.name}?")
            .setPositiveButton("Confirmar") { _, _ ->
                updateUserStatus(user, newStatus)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun updateUserStatus(user: UserManagement, newStatus: String) {
        showLoading(true)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = UpdateUserRequest(
                    name = user.name,
                    email = user.email,
                    first_name = user.first_name,
                    last_name = user.last_name,
                    role = user.role,
                    status = newStatus,
                    shipping_address = user.shipping_address,
                    phone_number = user.phone_number
                )

                val response = UserRetrofitClient.api.updateUser(user.id, request)
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    if (response.isSuccessful) {
                        val statusText = if (newStatus == "bloqueado") "bloqueado" else "desbloqueado"
                        Toast.makeText(this@GestorUsuaiosActivity, "Usuario $statusText exitosamente", Toast.LENGTH_SHORT).show()
                        loadUsers()
                    } else {
                        showError("Error al cambiar estado: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showError("Error de conexión: ${e.message}")
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}