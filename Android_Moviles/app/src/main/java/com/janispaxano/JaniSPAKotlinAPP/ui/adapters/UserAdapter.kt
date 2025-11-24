package com.janispaxano.JaniSPAKotlinAPP.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.janispaxano.JaniSPAKotlinAPP.R
import com.janispaxano.JaniSPAKotlinAPP.ui.models.UserManagement

class UserAdapter(
    private var users: List<UserManagement>,
    private val onEditClick: (UserManagement) -> Unit,
    private val onDeleteClick: (UserManagement) -> Unit,
    private val onToggleStatusClick: (UserManagement) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    fun updateUsers(newUsers: List<UserManagement>) {
        users = newUsers
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvUserName)
        private val tvRole: TextView = itemView.findViewById(R.id.tvUserRole)
        private val btnEdit: Button = itemView.findViewById(R.id.btnEditUser)
        private val btnDelete: Button = itemView.findViewById(R.id.btnDeleteUser)
        private val btnToggleStatus: Button = itemView.findViewById(R.id.btnToggleStatus)

        fun bind(user: UserManagement) {
            tvName.text = user.name
            tvRole.text = user.role.replaceFirstChar { it.uppercase() }

            // Configurar botón de bloqueo/desbloqueo
            val isBlocked = user.status.equals("bloqueado", ignoreCase = true)
            if (isBlocked) {
                btnToggleStatus.text = "Desbloquear"
                btnToggleStatus.setBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.orange)
                )
            } else {
                btnToggleStatus.text = "Bloquear"
                btnToggleStatus.setBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.red)
                )
            }

            btnEdit.setOnClickListener { onEditClick(user) }
            btnDelete.setOnClickListener { onDeleteClick(user) }
            btnToggleStatus.setOnClickListener { onToggleStatusClick(user) }
        }
    }
}

