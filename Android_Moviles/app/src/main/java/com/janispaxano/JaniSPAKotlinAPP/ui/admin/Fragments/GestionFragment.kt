package com.janispaxano.JaniSPAKotlinAPP.ui.admin.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.janispaxano.JaniSPAKotlinAPP.R
import com.janispaxano.JaniSPAKotlinAPP.ui.admin.GestorOrdenesActivity
import com.janispaxano.JaniSPAKotlinAPP.ui.admin.GestorProductosActivity
import com.janispaxano.JaniSPAKotlinAPP.ui.admin.GestorUsuaiosActivity

class GestionFragment : Fragment() {

    private lateinit var cardGestionProductos: CardView
    private lateinit var cardGestionUsuarios: CardView
    private lateinit var cardGestionOrdenes: CardView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_gestion, container, false)

        // Inicializar las vistas
        cardGestionProductos = view.findViewById(R.id.cardGestionProductos)
        cardGestionUsuarios = view.findViewById(R.id.cardGestionUsuarios)
        cardGestionOrdenes = view.findViewById(R.id.cardGestionOrdenes)

        // Configurar los listeners de click
        setupClickListeners()

        return view
    }

    private fun setupClickListeners() {
        cardGestionProductos.setOnClickListener {
            val intent = Intent(requireContext(), GestorProductosActivity::class.java)
            startActivity(intent)
        }

        cardGestionUsuarios.setOnClickListener {
            val intent = Intent(requireContext(), GestorUsuaiosActivity::class.java)
            startActivity(intent)
        }

        cardGestionOrdenes.setOnClickListener {
            val intent = Intent(requireContext(), GestorOrdenesActivity::class.java)
            startActivity(intent)
        }
    }
}