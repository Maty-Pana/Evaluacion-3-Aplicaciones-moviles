package com.janispaxano.JaniSPAKotlinAPP.ui.admin

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

class OrderDetailActivity : AppCompatActivity() {

    private lateinit var tvOrderTitle: TextView
    private lateinit var tvOrderDate: TextView
    private lateinit var tvOrderStatus: TextView
    private lateinit var tvOrderTotal: TextView
    private lateinit var tvCustomerInfo: TextView
    private lateinit var rvOrderItems: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnAcceptOrder: Button
    private lateinit var btnRejectOrder: Button
    private lateinit var btnBack: Button

    private var orderId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)

        // Configurar action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detalle de Orden"

        orderId = intent.getIntExtra("order_id", 0)

        if (orderId == 0) {
            Toast.makeText(this, "Error: ID de orden no válido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initComponents()
        setupRecyclerView()
        setupClickListeners()
        loadOrderDetails()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun initComponents() {
        tvOrderTitle = findViewById(R.id.tv_order_title)
        tvOrderDate = findViewById(R.id.tv_order_date)
        tvOrderStatus = findViewById(R.id.tv_order_status)
        tvOrderTotal = findViewById(R.id.tv_order_total)
        tvCustomerInfo = findViewById(R.id.tv_customer_info)
        rvOrderItems = findViewById(R.id.rv_order_items)
        progressBar = findViewById(R.id.progressBar)
        btnAcceptOrder = findViewById(R.id.btn_accept_order)
        btnRejectOrder = findViewById(R.id.btn_reject_order)
        btnBack = findViewById(R.id.btn_back)
    }

    private fun setupRecyclerView() {
        rvOrderItems.layoutManager = LinearLayoutManager(this)
    }

    private fun setupClickListeners() {
        btnAcceptOrder.setOnClickListener {
            updateOrderStatus("enviado", "aceptado")
        }

        btnRejectOrder.setOnClickListener {
            updateOrderStatus("rechazado", "rechazado")
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadOrderDetails() {
        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                rvOrderItems.visibility = View.GONE

                val api = OrderRetrofitClient.instance.create(OrderApiService::class.java)

                // Cargar órdenes, items y productos en paralelo
                val orderResponse = withContext(Dispatchers.IO) {
                    api.getOrder(orderId)
                }
                val itemsResponse = withContext(Dispatchers.IO) {
                    api.getOrderItems()
                }
                val productsResponse = withContext(Dispatchers.IO) {
                    api.getProducts()
                }

                if (orderResponse.isSuccessful && itemsResponse.isSuccessful && productsResponse.isSuccessful) {
                    val order = orderResponse.body()
                    val allItems = itemsResponse.body() ?: emptyList()
                    val orderItems = allItems.filter { it.order_id == orderId }
                    val allProducts = productsResponse.body() ?: emptyList()

                    // Crear un mapa de ID a nombre de producto (filtrando productos con ID null)
                    val productNamesMap = allProducts
                        .filter { it.id != null }
                        .associate { it.id!! to it.name }

                    if (order != null) {
                        tvOrderTitle.text = "Orden #$orderId"
                        tvCustomerInfo.text = "Cliente ID: ${order.user_id}"
                        tvOrderStatus.text = order.status

                        val date = Date(order.created_at ?: 0L)
                        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        tvOrderDate.text = format.format(date)

                        val formatCurrency = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
                        tvOrderTotal.text = formatCurrency.format(order.total)

                        // Configurar el badge de estado con colores dinámicos
                        when (order.status.lowercase()) {
                            "en espera" -> {
                                tvOrderStatus.setBackgroundResource(R.drawable.rounded_background_orange)
                                tvOrderStatus.setTextColor(getColor(android.R.color.white))
                            }
                            "enviado" -> {
                                tvOrderStatus.setBackgroundResource(R.drawable.rounded_background_green)
                                tvOrderStatus.setTextColor(getColor(android.R.color.white))
                            }
                            "rechazado" -> {
                                tvOrderStatus.setBackgroundResource(R.drawable.rounded_background_red)
                                tvOrderStatus.setTextColor(getColor(android.R.color.white))
                            }
                            else -> {
                                tvOrderStatus.setBackgroundResource(R.drawable.rounded_background_gray)
                                tvOrderStatus.setTextColor(getColor(android.R.color.white))
                            }
                        }

                        if (order.status.lowercase() == "en espera") {
                            btnAcceptOrder.visibility = View.VISIBLE
                            btnRejectOrder.visibility = View.VISIBLE
                        } else {
                            btnAcceptOrder.visibility = View.GONE
                            btnRejectOrder.visibility = View.GONE
                        }
                    }

                    if (orderItems.isNotEmpty()) {
                        // Pasar el mapa de nombres al adaptador
                        val adapter = OrderDetailAdapter(orderItems, productNamesMap)
                        rvOrderItems.adapter = adapter
                        rvOrderItems.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(this@OrderDetailActivity, "No hay productos en esta orden", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorMsg = "Error al cargar la orden: ${orderResponse.code()}"
                    Log.e("OrderDetailActivity", errorMsg)
                    Toast.makeText(this@OrderDetailActivity, "Error al cargar la orden", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e("OrderDetailActivity", "Error al cargar orden: ${e.message}", e)
                Toast.makeText(this@OrderDetailActivity, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun updateOrderStatus(newStatus: String, action: String) {
        btnAcceptOrder.isEnabled = false
        btnRejectOrder.isEnabled = false

        lifecycleScope.launch {
            try {
                val api = OrderRetrofitClient.instance.create(OrderApiService::class.java)

                val orderResponse = withContext(Dispatchers.IO) {
                    api.getOrder(orderId)
                }

                if (orderResponse.isSuccessful && orderResponse.body() != null) {
                    val order = orderResponse.body()!!

                    val updatedOrder = order.copy(status = newStatus)
                    val updateResponse = withContext(Dispatchers.IO) {
                        api.updateOrder(orderId, updatedOrder)
                    }

                    if (updateResponse.isSuccessful) {
                        val message = when (action) {
                            "aceptado" -> "✓ Pago aceptado - Pedido marcado como enviado"
                            "rechazado" -> "✗ Pago rechazado - Pedido cancelado"
                            else -> "Estado actualizado"
                        }
                        Toast.makeText(this@OrderDetailActivity, message, Toast.LENGTH_LONG).show()

                        loadOrderDetails()
                    } else {
                        Toast.makeText(this@OrderDetailActivity, "Error al actualizar estado", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("OrderDetailActivity", "Error al actualizar estado", e)
                Toast.makeText(this@OrderDetailActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            } finally {
                btnAcceptOrder.isEnabled = true
                btnRejectOrder.isEnabled = true
            }
        }
    }
}
