package com.janispaxano.JaniSPAKotlinAPP.ui.admin

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.janispaxano.JaniSPAKotlinAPP.R
import com.janispaxano.JaniSPAKotlinAPP.databinding.ActivityAdminHomeBinding
import com.janispaxano.JaniSPAKotlinAPP.ui.fragments.ProfileFragment
import com.janispaxano.JaniSPAKotlinAPP.ui.admin.Fragments.AddProductFragment
import com.janispaxano.JaniSPAKotlinAPP.ui.admin.Fragments.GestionFragment
import com.janispaxano.JaniSPAKotlinAPP.data.remote.auth.TokenManager

class AdminHomeActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "AdminHomeActivity"
    }

    private lateinit var binding: ActivityAdminHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar ViewBinding y usar binding.root como content view
        binding = ActivityAdminHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar toolbar (si existe en el layout)
        val toolbar = binding.toolbarHome
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        toolbar.setTitleTextColor(Color.WHITE)

        // BottomNavigation setup: inflar menú admin explícitamente para evitar confusiones
        val bottomNav: BottomNavigationView = binding.bottomNavigation
        bottomNav.menu.clear()
        bottomNav.inflateMenu(R.menu.bottom_nav_menu_admin)

        // Log para debugging: confirmar que se cargó el menú admin
        val adminMenu = bottomNav.menu
        Log.d(TAG, "AdminBottomNav itemCount=${adminMenu.size()}")
        for (i in 0 until adminMenu.size()) {
            val it = adminMenu.getItem(i)
            Log.d(TAG, "AdminBottomNav item[$i]: id=${it.itemId}, title=${it.title}")
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> {
                    Log.d(TAG, "Seleccionado: perfil")
                    replaceFragment(ProfileFragment())
                    true
                }
                R.id.nav_products -> {
                    replaceFragment(GestionFragment())
                    true
                }
                R.id.nav_add -> {
                    replaceFragment(AddProductFragment())
                    true
                }
                else -> false
            }
        }

        // Optional: mostrar fragment por defecto (perfil)
        if (savedInstanceState == null) {
            replaceFragment(ProfileFragment())
            bottomNav.selectedItemId = R.id.nav_profile
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        try {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        } catch (e: Exception) {
            Log.e(TAG, "Error al reemplazar fragment: ${e.message}")
        }
    }
}
