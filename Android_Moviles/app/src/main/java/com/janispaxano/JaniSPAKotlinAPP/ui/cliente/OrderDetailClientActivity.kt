package com.janispaxano.JaniSPAKotlinAPP.ui.cliente

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.janispaxano.JaniSPAKotlinAPP.R
import com.janispaxano.JaniSPAKotlinAPP.data.remote.order.OrderRetrofitClient
import com.janispaxano.JaniSPAKotlinAPP.data.remote.order.OrderApiService
import com.janispaxano.JaniSPAKotlinAPP.ui.adapters.OrderDetailAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class OrderDetailClientActivity : AppCompatActivity() {

    private lateinit var txtOrderTitle: TextView
    private lateinit var txtOrderStatus: TextView
    private lateinit var txtOrderDate: TextView
    private lateinit var txtOrderTotal: TextView
    private lateinit var recyclerViewItems: RecyclerView

    private var orderId: Int = 0
    private var orderStatus: String = ""
    private var orderDate: Long = 0L
    private var orderTotal: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail_client)

        // Configurar toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Obtener datos del intent
        orderId = intent.getIntExtra("order_id", 0)
        orderStatus = intent.getStringExtra("order_status") ?: "Desconocido"
        orderDate = intent.getLongExtra("order_date", 0L)
        orderTotal = intent.getDoubleExtra("order_total", 0.0)

        // Inicializar vistas
        txtOrderTitle = findViewById(R.id.txtOrderTitle)
        txtOrderStatus = findViewById(R.id.txtOrderStatus)
        txtOrderDate = findViewById(R.id.txtOrderDate)
        txtOrderTotal = findViewById(R.id.txtOrderTotal)
        recyclerViewItems = findViewById(R.id.recyclerViewItems)

        recyclerViewItems.layoutManager = LinearLayoutManager(this)

        // Mostrar datos básicos
        txtOrderTitle.text = "Orden #$orderId"
        txtOrderStatus.text = orderStatus

        // Formatear fecha
        val date = Date(orderDate)
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        txtOrderDate.text = format.format(date)

        // Formatear precio
        val formatCurrency = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
        txtOrderTotal.text = formatCurrency.format(orderTotal)

        // Cambiar color según el estado
        val statusColor = when (orderStatus.lowercase()) {
            "en espera" -> android.R.color.holo_orange_dark
            "enviado" -> android.R.color.holo_green_dark
            "rechazado" -> android.R.color.holo_red_dark
            else -> android.R.color.darker_gray
        }
        txtOrderStatus.setTextColor(getColor(statusColor))

        // Cargar items de la orden
        loadOrderItems()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadOrderItems() {
        lifecycleScope.launch {
            try {
                val api = OrderRetrofitClient.instance.create(OrderApiService::class.java)

                // Cargar items y productos en paralelo
                val itemsResponse = withContext(Dispatchers.IO) {
                    api.getOrderItems()
                }
                val productsResponse = withContext(Dispatchers.IO) {
                    api.getProducts()
                }

                if (itemsResponse.isSuccessful && itemsResponse.body() != null &&
                    productsResponse.isSuccessful && productsResponse.body() != null) {

                    val allItems = itemsResponse.body()!!
                    val allProducts = productsResponse.body()!!

                    // Filtrar items de esta orden
                    val orderItems = allItems.filter { it.order_id == orderId }

                    // Crear mapa de nombres de productos
                    val productNamesMap = allProducts
                        .filter { it.id != null }
                        .associate { it.id!! to it.name }

                    if (orderItems.isNotEmpty()) {
                        val adapter = OrderDetailAdapter(orderItems, productNamesMap)
                        recyclerViewItems.adapter = adapter
                    }
                } else {
                    Log.e("OrderDetailClient", "Error al cargar items: ${itemsResponse.code()}")
                }
            } catch (e: Exception) {
                Log.e("OrderDetailClient", "Excepción al cargar items", e)
            }
        }
    }
}
