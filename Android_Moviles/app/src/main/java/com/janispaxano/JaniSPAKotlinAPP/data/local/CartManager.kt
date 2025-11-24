package com.janispaxano.JaniSPAKotlinAPP.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.janispaxano.JaniSPAKotlinAPP.ui.models.CartItem

class CartManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("cart_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val CART_ITEMS_KEY = "cart_items"
    }

    fun addToCart(cartItem: CartItem) {
        val cartItems = getCartItems().toMutableList()
        val existingItemIndex = cartItems.indexOfFirst { it.productId == cartItem.productId }

        if (existingItemIndex != -1) {
            // Si el producto ya existe, aumentar la cantidad
            cartItems[existingItemIndex].quantity += cartItem.quantity
        } else {
            // Si no existe, agregar nuevo item
            cartItems.add(cartItem)
        }

        saveCartItems(cartItems)
    }

    fun removeFromCart(productId: Int) {
        val cartItems = getCartItems().toMutableList()
        cartItems.removeAll { it.productId == productId }
        saveCartItems(cartItems)
    }

    fun updateQuantity(productId: Int, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(productId)
            return
        }

        val cartItems = getCartItems().toMutableList()
        val itemIndex = cartItems.indexOfFirst { it.productId == productId }

        if (itemIndex != -1) {
            cartItems[itemIndex].quantity = newQuantity
            saveCartItems(cartItems)
        }
    }

    fun getCartItems(): List<CartItem> {
        val cartItemsJson = sharedPreferences.getString(CART_ITEMS_KEY, "[]")
        val type = object : TypeToken<List<CartItem>>() {}.type
        return gson.fromJson(cartItemsJson, type) ?: emptyList()
    }

    fun getCartTotal(): Double {
        return getCartItems().sumOf { it.getTotalPrice() }
    }

    fun getCartItemCount(): Int {
        return getCartItems().sumOf { it.quantity }
    }

    fun clearCart() {
        sharedPreferences.edit().remove(CART_ITEMS_KEY).apply()
    }

    private fun saveCartItems(cartItems: List<CartItem>) {
        val cartItemsJson = gson.toJson(cartItems)
        sharedPreferences.edit().putString(CART_ITEMS_KEY, cartItemsJson).apply()
    }
}
