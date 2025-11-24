package com.janispaxano.JaniSPAKotlinAPP.ui.cliente

import android.os.Bundle
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.janispaxano.JaniSPAKotlinAPP.ui.fragments.ProfileFragment
import com.janispaxano.JaniSPAKotlinAPP.ui.cliente.Fragments.ProductsFragment
import com.janispaxano.JaniSPAKotlinAPP.ui.cliente.Fragments.CartFragment
import com.janispaxano.JaniSPAKotlinAPP.R
import com.janispaxano.JaniSPAKotlinAPP.data.remote.auth.TokenManager

class ClienteHomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Usar el layout específico para Cliente
        setContentView(R.layout.activity_cliente_home)

        // Obtener la toolbar desde el layout (no se estaba usando ViewBinding aquí)
        val toolbar = findViewById<Toolbar>(R.id.toolbarHome)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        toolbar.setTitleTextColor(Color.WHITE)

        // Configurar BottomNavigation para reemplazar fragments
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        // Forzar inflado del menú cliente por si hay confusión
        bottomNav.menu.clear()
        bottomNav.inflateMenu(R.menu.bottom_nav_menu_cliente)
        // Log para debugging: confirmar que se cargó el menú cliente
        val clientMenu = bottomNav.menu
        android.util.Log.d("ClienteHomeActivity", "ClienteBottomNav itemCount=${clientMenu.size()}")
        for (i in 0 until clientMenu.size()) {
            val it = clientMenu.getItem(i)
            android.util.Log.d("ClienteHomeActivity", "ClienteBottomNav item[$i]: id=${it.itemId}, title=${it.title}")
        }
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                R.id.nav_products -> {
                    replaceFragment(ProductsFragment())
                    true
                }
                // En cliente, el botón derecho abre Carrito
                R.id.nav_add -> {
                    replaceFragment(CartFragment())
                    true
                }
                else -> false
            }
        }

        // Mostrar fragment por defecto (perfil)
        if (savedInstanceState == null) {
            replaceFragment(ProfileFragment())
            bottomNav.selectedItemId = R.id.nav_profile
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
