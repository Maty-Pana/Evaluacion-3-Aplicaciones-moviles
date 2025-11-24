package com.janispaxano.JaniSPAKotlinAPP.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.janispaxano.JaniSPAKotlinAPP.R
import com.janispaxano.JaniSPAKotlinAPP.ui.models.CartItem
import java.text.NumberFormat
import java.util.*

class CartAdapter(
    private var cartItems: MutableList<CartItem>,
    private val onQuantityChanged: (CartItem, Int) -> Unit,
    private val onRemoveItem: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProductImage: ImageView = itemView.findViewById(R.id.iv_product_image)
        val tvProductName: TextView = itemView.findViewById(R.id.tv_product_name)
        val tvProductPrice: TextView = itemView.findViewById(R.id.tv_product_price)
        val tvQuantity: TextView = itemView.findViewById(R.id.tv_quantity)
        val btnDecrease: ImageButton = itemView.findViewById(R.id.btn_decrease)
        val btnIncrease: ImageButton = itemView.findViewById(R.id.btn_increase)
        val btnRemove: ImageButton = itemView.findViewById(R.id.btn_remove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = cartItems[position]
        val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "CL"))

        holder.tvProductName.text = cartItem.productName
        holder.tvProductPrice.text = numberFormat.format(cartItem.productPrice)
        holder.tvQuantity.text = cartItem.quantity.toString()

        // Cargar imagen del producto
        Glide.with(holder.itemView.context)
            .load(cartItem.productImage)
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_launcher_foreground)
            .into(holder.ivProductImage)

        // Botón para disminuir cantidad
        holder.btnDecrease.setOnClickListener {
            val newQuantity = cartItem.quantity - 1
            if (newQuantity > 0) {
                onQuantityChanged(cartItem, newQuantity)
            }
        }

        // Botón para aumentar cantidad
        holder.btnIncrease.setOnClickListener {
            val newQuantity = cartItem.quantity + 1
            onQuantityChanged(cartItem, newQuantity)
        }

        // Botón para eliminar item
        holder.btnRemove.setOnClickListener {
            onRemoveItem(cartItem)
        }
    }

    override fun getItemCount(): Int = cartItems.size

    fun updateItems(newItems: List<CartItem>) {
        cartItems.clear()
        cartItems.addAll(newItems)
        notifyDataSetChanged()
    }
}
