package com.janispaxano.JaniSPAKotlinAPP.ui.cliente

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.janispaxano.JaniSPAKotlinAPP.R
import com.janispaxano.JaniSPAKotlinAPP.data.remote.order.OrderRetrofitClient
import com.janispaxano.JaniSPAKotlinAPP.data.remote.order.OrderApiService
import com.janispaxano.JaniSPAKotlinAPP.ui.adapters.MyOrdersAdapter
import com.janispaxano.JaniSPAKotlinAPP.ui.models.GroupedOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyOrdersActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var txtNoOrders: TextView
    private lateinit var adapter: MyOrdersAdapter
    private val orders = mutableListOf<GroupedOrder>()
    private var isLoading: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_orders)

        // Configurar toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Inicializar vistas
        recyclerView = findViewById(R.id.recyclerViewOrders)
        progressBar = findViewById(R.id.progressBar)
        txtNoOrders = findViewById(R.id.txtNoOrders)

        // Configurar RecyclerView con click listener
        adapter = MyOrdersAdapter(orders) { groupedOrder ->
            // Navegar a detalles de la orden
            val intent = Intent(this, OrderDetailClientActivity::class.java)
            intent.putExtra("order_id", groupedOrder.orderId)
            intent.putExtra("order_status", groupedOrder.status)
            intent.putExtra("order_date", groupedOrder.createdAt)
            intent.putExtra("order_total", groupedOrder.totalPrice)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Cargar órdenes
        loadOrders()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun loadOrders() {
        // Evitar múltiples cargas simultáneas
        if (isLoading) return

        lifecycleScope.launch {
            try {
                isLoading = true
                progressBar.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                txtNoOrders.visibility = View.GONE

                val api = OrderRetrofitClient.instance.create(OrderApiService::class.java)

                // Cargar órdenes y order items en paralelo
                val ordersResponse = withContext(Dispatchers.IO) {
                    api.getOrders()
                }
                val itemsResponse = withContext(Dispatchers.IO) {
                    api.getOrderItems()
                }

                if (ordersResponse.isSuccessful && itemsResponse.isSuccessful) {
                    val ordersList = ordersResponse.body() ?: emptyList()
                    val itemsList = itemsResponse.body() ?: emptyList()

                    // Limitar el procesamiento para evitar sobrecarga
                    if (ordersList.size > 1000 || itemsList.size > 5000) {
                        Log.w("MyOrdersActivity", "Cantidad excesiva de datos. Orders: ${ordersList.size}, Items: ${itemsList.size}")
                    }

                    // Agrupar items por order_id de forma más eficiente
                    val itemsMap = itemsList.groupBy { it.order_id }
                    val ordersMap = ordersList.associateBy { it.id }

                    val groupedOrders = itemsMap.map { (orderId, items) ->
                        val order = ordersMap[orderId]

                        GroupedOrder(
                            orderId = orderId,
                            status = order?.status ?: "Desconocido",
                            createdAt = items.firstOrNull()?.created_at ?: 0L,
                            items = items,
                            totalProducts = items.sumOf { it.quantity },
                            totalPrice = items.sumOf { it.price * it.quantity }
                        )
                    }.sortedByDescending { it.createdAt }

                    if (groupedOrders.isEmpty()) {
                        txtNoOrders.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        orders.clear()
                        orders.addAll(groupedOrders)
                        adapter.updateOrders(orders)
                        recyclerView.visibility = View.VISIBLE
                        txtNoOrders.visibility = View.GONE
                    }
                } else {
                    Log.e("MyOrdersActivity", "Error al cargar órdenes: ${ordersResponse.code()}")
                    Toast.makeText(
                        this@MyOrdersActivity,
                        "Error al cargar las órdenes",
                        Toast.LENGTH_SHORT
                    ).show()
                    txtNoOrders.visibility = View.VISIBLE
                }
            } catch (e: OutOfMemoryError) {
                Log.e("MyOrdersActivity", "Error de memoria al cargar órdenes", e)
                Toast.makeText(
                    this@MyOrdersActivity,
                    "Demasiados datos para cargar",
                    Toast.LENGTH_SHORT
                ).show()
                txtNoOrders.visibility = View.VISIBLE
            } catch (e: Exception) {
                Log.e("MyOrdersActivity", "Excepción al cargar órdenes", e)
                Toast.makeText(
                    this@MyOrdersActivity,
                    "Error de conexión: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                txtNoOrders.visibility = View.VISIBLE
            } finally {
                progressBar.visibility = View.GONE
                isLoading = false
            }
        }
    }
}
