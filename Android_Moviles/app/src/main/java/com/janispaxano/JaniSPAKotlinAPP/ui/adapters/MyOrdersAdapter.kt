package com.janispaxano.JaniSPAKotlinAPP.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.janispaxano.JaniSPAKotlinAPP.R
import com.janispaxano.JaniSPAKotlinAPP.ui.models.GroupedOrder
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class MyOrdersAdapter(
    private var orders: List<GroupedOrder>,
    private val onOrderClick: (GroupedOrder) -> Unit
) : RecyclerView.Adapter<MyOrdersAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtOrderId: TextView = itemView.findViewById(R.id.txtOrderId)
        val txtOrderStatus: TextView = itemView.findViewById(R.id.txtOrderStatus)
        val txtOrderDate: TextView = itemView.findViewById(R.id.txtOrderDate)
        val txtProductCount: TextView = itemView.findViewById(R.id.txtProductCount)
        val txtOrderPrice: TextView = itemView.findViewById(R.id.txtOrderPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        val context = holder.itemView.context

        holder.txtOrderId.text = "Orden #${order.orderId}"
        holder.txtOrderStatus.text = order.status
        holder.txtProductCount.text = "Productos: ${order.totalProducts}"

        // Formatear precio
        val formatCurrency = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
        holder.txtOrderPrice.text = formatCurrency.format(order.totalPrice)

        // Formatear fecha
        val date = Date(order.createdAt)
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        holder.txtOrderDate.text = format.format(date)

        // Cambiar color según el estado
        val statusColor = when (order.status.lowercase()) {
            "en espera" -> android.R.color.holo_orange_dark
            "enviado" -> android.R.color.holo_green_dark
            "rechazado" -> android.R.color.holo_red_dark
            else -> android.R.color.darker_gray
        }
        holder.txtOrderStatus.setTextColor(context.getColor(statusColor))

        // Click en la orden
        holder.itemView.setOnClickListener {
            onOrderClick(order)
        }
    }

    override fun getItemCount(): Int = orders.size

    fun updateOrders(newOrders: List<GroupedOrder>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}
