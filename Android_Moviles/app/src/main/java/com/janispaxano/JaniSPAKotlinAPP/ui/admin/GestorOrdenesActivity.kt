package com.janispaxano.JaniSPAKotlinAPP.ui.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.janispaxano.JaniSPAKotlinAPP.R
import com.janispaxano.JaniSPAKotlinAPP.data.remote.order.OrderApiService
import com.janispaxano.JaniSPAKotlinAPP.data.remote.order.OrderRetrofitClient
import com.janispaxano.JaniSPAKotlinAPP.ui.adapters.MyOrdersAdapter
import com.janispaxano.JaniSPAKotlinAPP.ui.models.GroupedOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GestorOrdenesActivity : AppCompatActivity() {

    private lateinit var rvOrders: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutEmptyOrders: LinearLayout
    private lateinit var btnFilterAll: Button
    private lateinit var btnFilterPending: Button
    private lateinit var btnFilterSent: Button
    private lateinit var btnFilterRejected: Button
    private lateinit var orderAdapter: MyOrdersAdapter

    private var allOrders: List<GroupedOrder> = emptyList()
    private var currentFilter: String = "all"
    private var isLoading: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestor_ordenes)

        // Configurar action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Gestión de Órdenes"

        initComponents()
        setupRecyclerView()
        setupClickListeners()
        loadOrders()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun initComponents() {
        rvOrders = findViewById(R.id.rv_orders)
        progressBar = findViewById(R.id.progressBar)
        layoutEmptyOrders = findViewById(R.id.layout_empty_orders)
        btnFilterAll = findViewById(R.id.btn_filter_all)
        btnFilterPending = findViewById(R.id.btn_filter_pending)
        btnFilterSent = findViewById(R.id.btn_filter_sent)
        btnFilterRejected = findViewById(R.id.btn_filter_rejected)
    }

    private fun setupRecyclerView() {
        orderAdapter = MyOrdersAdapter(
            orders = emptyList(),
            onOrderClick = { groupedOrder ->
                // Navegar al detalle de la orden
                val intent = Intent(this, OrderDetailActivity::class.java)
                intent.putExtra("order_id", groupedOrder.orderId)
                intent.putExtra("order_status", groupedOrder.status)
                startActivity(intent)
            }
        )

        rvOrders.layoutManager = LinearLayoutManager(this)
        rvOrders.adapter = orderAdapter
    }

    private fun setupClickListeners() {
        btnFilterAll.setOnClickListener { applyFilter("all") }
        btnFilterPending.setOnClickListener { applyFilter("en espera") }
        btnFilterSent.setOnClickListener { applyFilter("enviado") }
        btnFilterRejected.setOnClickListener { applyFilter("rechazado") }
    }

    private fun loadOrders() {
        // Evitar múltiples cargas simultáneas
        if (isLoading) return

        lifecycleScope.launch {
            try {
                isLoading = true
                progressBar.visibility = View.VISIBLE
                rvOrders.visibility = View.GONE
                layoutEmptyOrders.visibility = View.GONE

                val api = OrderRetrofitClient.instance.create(OrderApiService::class.java)

                // Cargar órdenes y order items
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
                        Log.w("GestorOrdenesActivity", "Cantidad excesiva de datos. Orders: ${ordersList.size}, Items: ${itemsList.size}")
                    }

                    // Agrupar items por order_id de forma más eficiente
                    val itemsMap = itemsList.groupBy { it.order_id }
                    val ordersMap = ordersList.associateBy { it.id }

                    allOrders = itemsMap.map { (orderId, items) ->
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

                    applyFilter(currentFilter)
                } else {
                    Log.e("GestorOrdenesActivity", "Error al cargar órdenes: ${ordersResponse.code()}")
                    Toast.makeText(this@GestorOrdenesActivity, "Error al cargar órdenes", Toast.LENGTH_SHORT).show()
                    showEmptyState()
                }
            } catch (e: OutOfMemoryError) {
                Log.e("GestorOrdenesActivity", "Error de memoria al cargar órdenes", e)
                Toast.makeText(this@GestorOrdenesActivity, "Demasiados datos para cargar", Toast.LENGTH_SHORT).show()
                showEmptyState()
            } catch (e: Exception) {
                Log.e("GestorOrdenesActivity", "Excepción al cargar órdenes", e)
                Toast.makeText(this@GestorOrdenesActivity, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                showEmptyState()
            } finally {
                progressBar.visibility = View.GONE
                isLoading = false
            }
        }
    }

    private fun applyFilter(filter: String) {
        currentFilter = filter
        updateFilterButtons()

        val filteredOrders = when (filter) {
            "all" -> allOrders
            else -> allOrders.filter { it.status.lowercase() == filter.lowercase() }
        }

        if (filteredOrders.isEmpty()) {
            showEmptyState()
        } else {
            showOrdersList(filteredOrders)
        }
    }

    private fun updateFilterButtons() {
        resetButtonStyle(btnFilterAll)
        resetButtonStyle(btnFilterPending)
        resetButtonStyle(btnFilterSent)
        resetButtonStyle(btnFilterRejected)

        val activeButton = when (currentFilter) {
            "all" -> btnFilterAll
            "en espera" -> btnFilterPending
            "enviado" -> btnFilterSent
            "rechazado" -> btnFilterRejected
            else -> btnFilterAll
        }

        activeButton.setBackgroundColor(getColor(R.color.colorPrimary))
        activeButton.setTextColor(getColor(android.R.color.white))
    }

    private fun resetButtonStyle(button: Button) {
        button.setBackgroundColor(getColor(android.R.color.transparent))
        button.setTextColor(getColor(R.color.colorPrimary))
    }

    private fun showEmptyState() {
        rvOrders.visibility = View.GONE
        layoutEmptyOrders.visibility = View.VISIBLE
    }

    private fun showOrdersList(orders: List<GroupedOrder>) {
        layoutEmptyOrders.visibility = View.GONE
        rvOrders.visibility = View.VISIBLE
        orderAdapter.updateOrders(orders)
    }

    override fun onResume() {
        super.onResume()
        // Solo recargar si no hay datos o si venimos de un detalle
        if (allOrders.isEmpty()) {
            loadOrders()
        }
    }
}