package com.janispaxano.JaniSPAKotlinAPP.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.janispaxano.JaniSPAKotlinAPP.R
import com.janispaxano.JaniSPAKotlinAPP.ui.models.OrderItem
import com.janispaxano.JaniSPAKotlinAPP.data.remote.product.ProductRetrofitClient
import com.janispaxano.JaniSPAKotlinAPP.data.remote.product.ProductApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.*

class OrderItemAdapter(
    private var orderItems: MutableList<OrderItem>
) : RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder>() {

    class OrderItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtProductName: TextView = itemView.findViewById(R.id.txtProductName)
        val txtQuantity: TextView = itemView.findViewById(R.id.txtQuantity)
        val txtPrice: TextView = itemView.findViewById(R.id.txtPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_detail, parent, false)
        return OrderItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
        val orderItem = orderItems[position]
        val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
        val subtotal = orderItem.price * orderItem.quantity

        // Mostrar ID del producto inicialmente mientras carga
        holder.txtProductName.text = "Cargando..."
        holder.txtQuantity.text = "Cantidad: ${orderItem.quantity}"
        holder.txtPrice.text = numberFormat.format(subtotal)

        // Cargar nombre del producto desde la API
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val productName = withContext(Dispatchers.IO) {
                    val response = ProductRetrofitClient.api.getProduct(orderItem.product_id)
                    if (response.isSuccessful && response.body() != null) {
                        response.body()!!.name
                    } else {
                        "Producto ID: ${orderItem.product_id}"
                    }
                }
                holder.txtProductName.text = productName
            } catch (e: Exception) {
                holder.txtProductName.text = "Producto ID: ${orderItem.product_id}"
            }
        }
    }

    override fun getItemCount(): Int = orderItems.size

    fun updateOrderItems(newOrderItems: List<OrderItem>) {
        orderItems.clear()
        orderItems.addAll(newOrderItems)
        notifyDataSetChanged()
    }
}
