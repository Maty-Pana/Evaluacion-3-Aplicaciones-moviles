package com.janispaxano.JaniSPAKotlinAPP.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.janispaxano.JaniSPAKotlinAPP.R
import com.janispaxano.JaniSPAKotlinAPP.ui.models.Order
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class OrderAdapter(
    private var orders: MutableList<Order>,
    private val onOrderClick: (Order) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cvOrder: CardView = itemView.findViewById(R.id.cv_order)
        val tvOrderId: TextView = itemView.findViewById(R.id.tv_order_id)
        val tvOrderDate: TextView = itemView.findViewById(R.id.tv_order_date)
        val tvOrderTotal: TextView = itemView.findViewById(R.id.tv_order_total)
        val tvOrderStatus: TextView = itemView.findViewById(R.id.tv_order_status)
        val tvUserId: TextView = itemView.findViewById(R.id.tv_user_id)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_admin, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        val context = holder.itemView.context
        val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        holder.tvOrderId.text = "Pedido #${order.id}"
        holder.tvOrderTotal.text = numberFormat.format(order.total)
        holder.tvOrderStatus.text = order.status
        holder.tvUserId.text = "Cliente ID: ${order.user_id}"

        // Formatear fecha
        if (order.created_at != null) {
            val date = Date(order.created_at)
            holder.tvOrderDate.text = dateFormat.format(date)
        }

        // Cambiar color según el estado
        val statusColor = when (order.status.lowercase()) {
            "en espera" -> android.R.color.holo_orange_dark
            "enviado" -> android.R.color.holo_green_dark
            "rechazado" -> android.R.color.holo_red_dark
            else -> android.R.color.darker_gray
        }

        holder.tvOrderStatus.setTextColor(context.getColor(statusColor))

        holder.cvOrder.setOnClickListener {
            onOrderClick(order)
        }
    }

    override fun getItemCount(): Int = orders.size

    fun updateOrders(newOrders: List<Order>) {
        orders.clear()
        orders.addAll(newOrders)
        notifyDataSetChanged()
    }
}
