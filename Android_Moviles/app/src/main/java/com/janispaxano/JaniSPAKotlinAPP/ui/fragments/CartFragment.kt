package com.janispaxano.JaniSPAKotlinAPP.ui.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.janispaxano.JaniSPAKotlinAPP.R
import com.janispaxano.JaniSPAKotlinAPP.data.local.CartManager
import com.janispaxano.JaniSPAKotlinAPP.data.remote.order.OrderRepository
import com.janispaxano.JaniSPAKotlinAPP.ui.adapters.CartAdapter
import com.janispaxano.JaniSPAKotlinAPP.ui.models.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class CartFragment : Fragment() {

    private lateinit var cartManager: CartManager
    private lateinit var orderRepository: OrderRepository
    private lateinit var cartAdapter: CartAdapter

    private lateinit var rvCartItems: RecyclerView
    private lateinit var layoutEmptyCart: LinearLayout
    private lateinit var cvCartSummary: androidx.cardview.widget.CardView
    private lateinit var tvTotalItems: TextView
    private lateinit var tvTotalPrice: TextView
    private lateinit var btnCheckout: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initComponents(view)
        setupRecyclerView()
        setupClickListeners()
        loadCartItems()
    }

    private fun initComponents(view: View) {
        cartManager = CartManager(requireContext())
        orderRepository = OrderRepository()

        rvCartItems = view.findViewById(R.id.rv_cart_items)
        layoutEmptyCart = view.findViewById(R.id.layout_empty_cart)
        cvCartSummary = view.findViewById(R.id.cv_cart_summary)
        tvTotalItems = view.findViewById(R.id.tv_total_items)
        tvTotalPrice = view.findViewById(R.id.tv_total_price)
        btnCheckout = view.findViewById(R.id.btn_checkout)
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            cartItems = mutableListOf(),
            onQuantityChanged = { cartItem, newQuantity ->
                cartManager.updateQuantity(cartItem.productId, newQuantity)
                loadCartItems()
            },
            onRemoveItem = { cartItem ->
                cartManager.removeFromCart(cartItem.productId)
                loadCartItems()
                Toast.makeText(context, "Producto eliminado del carrito", Toast.LENGTH_SHORT).show()
            }
        )

        rvCartItems.layoutManager = LinearLayoutManager(context)
        rvCartItems.adapter = cartAdapter
    }

    private fun setupClickListeners() {
        btnCheckout.setOnClickListener {
            showCheckoutDialog()
        }
    }

    private fun loadCartItems() {
        val cartItems = cartManager.getCartItems()

        if (cartItems.isEmpty()) {
            showEmptyCart()
        } else {
            showCartWithItems(cartItems)
        }
    }

    private fun showEmptyCart() {
        rvCartItems.visibility = View.GONE
        cvCartSummary.visibility = View.GONE
        layoutEmptyCart.visibility = View.VISIBLE
    }

    private fun showCartWithItems(cartItems: List<CartItem>) {
        layoutEmptyCart.visibility = View.GONE
        rvCartItems.visibility = View.VISIBLE
        cvCartSummary.visibility = View.VISIBLE

        cartAdapter.updateItems(cartItems)
        updateCartSummary(cartItems)
    }

    private fun updateCartSummary(cartItems: List<CartItem>) {
        val totalItems = cartItems.sumOf { it.quantity }
        val totalPrice = cartItems.sumOf { it.getTotalPrice() }
        val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "CL"))

        tvTotalItems.text = totalItems.toString()
        tvTotalPrice.text = numberFormat.format(totalPrice)
    }

    private fun showCheckoutDialog() {
        val cartItems = cartManager.getCartItems()
        if (cartItems.isEmpty()) return

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_checkout, null)
        val tvTotalItemsDialog = dialogView.findViewById<TextView>(R.id.tv_total_items)
        val tvTotalPriceDialog = dialogView.findViewById<TextView>(R.id.tv_total_price)

        val totalItems = cartItems.sumOf { it.quantity }
        val totalPrice = cartItems.sumOf { it.getTotalPrice() }
        val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "CL"))

        tvTotalItemsDialog.text = "Total de productos: $totalItems"
        tvTotalPriceDialog.text = "Total a pagar: ${numberFormat.format(totalPrice)}"

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btn_confirm).setOnClickListener {
            dialog.dismiss()
            processOrder(cartItems, totalPrice)
        }

        dialog.show()
    }

    private fun processOrder(cartItems: List<CartItem>, totalPrice: Double) {
        // Mostrar loading
        btnCheckout.isEnabled = false
        btnCheckout.text = "Procesando..."

        lifecycleScope.launch {
            try {
                // Obtener user_id del SharedPreferences de autenticación
                val authPrefs = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                val userId = authPrefs.getInt("user_id", -1)

                if (userId == -1) {
                    Toast.makeText(context, "Error: Usuario no autenticado", Toast.LENGTH_LONG).show()
                    return@launch
                }

                // Crear la orden
                val orderRequest = CreateOrderRequest(
                    total = totalPrice,
                    status = "en espera",
                    user_id = userId
                )

                val orderResponse = orderRepository.createOrder(orderRequest)

                if (orderResponse.isSuccessful && orderResponse.body() != null) {
                    val order = orderResponse.body()!!

                    // Crear los items de la orden
                    var allItemsCreated = true
                    for (cartItem in cartItems) {
                        val orderItemRequest = CreateOrderItemRequest(
                            quantity = cartItem.quantity,
                            price = cartItem.productPrice,
                            order_id = order.id!!,
                            product_id = cartItem.productId
                        )

                        val itemResponse = orderRepository.createOrderItem(orderItemRequest)
                        if (!itemResponse.isSuccessful) {
                            allItemsCreated = false
                            break
                        }
                    }

                    if (allItemsCreated) {
                        // Limpiar el carrito local
                        cartManager.clearCart()
                        loadCartItems()

                        Toast.makeText(context, "¡Pedido realizado exitosamente!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Error al crear algunos items del pedido", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(context, "Error al crear el pedido", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                btnCheckout.isEnabled = true
                btnCheckout.text = "Finalizar Compra"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadCartItems()
    }
}
