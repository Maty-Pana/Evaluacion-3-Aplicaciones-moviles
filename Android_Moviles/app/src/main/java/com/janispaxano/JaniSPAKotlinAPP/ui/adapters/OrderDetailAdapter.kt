package com.janispaxano.JaniSPAKotlinAPP.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.janispaxano.JaniSPAKotlinAPP.R
import com.janispaxano.JaniSPAKotlinAPP.ui.models.OrderItem
import java.text.NumberFormat
import java.util.*

class OrderDetailAdapter(
    private val items: List<OrderItem>,
    private val productNames: Map<Int, String>
) : RecyclerView.Adapter<OrderDetailAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtProductName: TextView = itemView.findViewById(R.id.txtProductName)
        val txtQuantity: TextView = itemView.findViewById(R.id.txtQuantity)
        val txtPrice: TextView = itemView.findViewById(R.id.txtPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_detail, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
        val totalPrice = item.price * item.quantity

        // Obtener el nombre del producto del mapa precargado
        val productName = productNames[item.product_id] ?: "Producto ID: ${item.product_id}"

        holder.txtProductName.text = productName
        holder.txtQuantity.text = "Cantidad: ${item.quantity}"
        holder.txtPrice.text = numberFormat.format(totalPrice)
    }

    override fun getItemCount(): Int = items.size
}
