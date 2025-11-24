package com.janispaxano.JaniSPAKotlinAPP.data.local

import android.content.Context

class CartSessionManager(private val context: Context) {

    private val cartManager = CartManager(context)

    /**
     * Limpia el carrito cuando el usuario cierra sesión
     * Este método debe ser llamado desde el logout del sistema de autenticación
     */
    fun clearCartOnLogout() {
        cartManager.clearCart()
    }

    /**
     * Verifica si hay items en el carrito
     */
    fun hasCartItems(): Boolean {
        return cartManager.getCartItems().isNotEmpty()
    }
}
