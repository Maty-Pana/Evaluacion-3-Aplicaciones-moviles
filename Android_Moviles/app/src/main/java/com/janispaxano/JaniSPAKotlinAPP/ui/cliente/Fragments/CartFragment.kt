package com.janispaxano.JaniSPAKotlinAPP.ui.cliente.Fragments

import android.app.AlertDialog
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
import com.janispaxano.JaniSPAKotlinAPP.data.remote.auth.TokenManager
import com.janispaxano.JaniSPAKotlinAPP.data.remote.order.OrderRepository
import com.janispaxano.JaniSPAKotlinAPP.ui.adapters.CartAdapter
import com.janispaxano.JaniSPAKotlinAPP.ui.models.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class CartFragment : Fragment() {

    private lateinit var cartManager: CartManager
    private lateinit var tokenManager: TokenManager
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
        return try {
            inflater.inflate(R.layout.fragment_cart, container, false)
        } catch (e: Exception) {
            // Fallback: crear una vista simple si no se encuentra el layout
            val scrollView = ScrollView(requireContext())
            val linearLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(32, 32, 32, 32)
            }

            val titleView = TextView(requireContext()).apply {
                text = "Carrito de Compras"
                textSize = 24f
                setPadding(0, 0, 0, 16)
            }

            val emptyView = TextView(requireContext()).apply {
                text = "Tu carrito está vacío"
                textSize = 16f
                setPadding(0, 16, 0, 16)
            }

            linearLayout.addView(titleView)
            linearLayout.addView(emptyView)
            scrollView.addView(linearLayout)
            scrollView
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            initComponents(view)
            setupRecyclerView()
            setupClickListeners()
            loadCartItems()
        } catch (e: Exception) {
            // Si hay error en la inicialización, mostrar mensaje básico
            showBasicCartMessage(view)
        }
    }

    private fun showBasicCartMessage(view: View) {
        // Crear vista básica si no se pueden inicializar los componentes
        val textView = TextView(requireContext()).apply {
            text = "Carrito de compras\n\nPor favor, agrega productos desde la tienda."
            textSize = 16f
            setPadding(32, 32, 32, 32)
        }

        if (view is ViewGroup) {
            view.removeAllViews()
            view.addView(textView)
        }
    }

    private fun initComponents(view: View) {
        cartManager = CartManager(requireContext())
        tokenManager = TokenManager(requireContext())
        orderRepository = OrderRepository()

        rvCartItems = view.findViewById(R.id.rv_cart_items) ?: throw Exception("rv_cart_items not found")
        layoutEmptyCart = view.findViewById(R.id.layout_empty_cart) ?: LinearLayout(requireContext())
        cvCartSummary = view.findViewById(R.id.cv_cart_summary) ?: androidx.cardview.widget.CardView(requireContext())
        tvTotalItems = view.findViewById(R.id.tv_total_items) ?: TextView(requireContext())
        tvTotalPrice = view.findViewById(R.id.tv_total_price) ?: TextView(requireContext())
        btnCheckout = view.findViewById(R.id.btn_checkout) ?: Button(requireContext())
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
        val totalPrice = cartItems.sumOf { it.productPrice * it.quantity }

        tvTotalItems.text = "Items: $totalItems"
        tvTotalPrice.text = NumberFormat.getCurrencyInstance(Locale("es", "CL")).format(totalPrice)
    }

    private fun showCheckoutDialog() {
        val cartItems = cartManager.getCartItems()
        if (cartItems.isEmpty()) {
            Toast.makeText(context, "El carrito está vacío", Toast.LENGTH_SHORT).show()
            return
        }

        val totalPrice = cartItems.sumOf { it.productPrice * it.quantity }
        val formattedPrice = NumberFormat.getCurrencyInstance(Locale("es", "CL")).format(totalPrice)

        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar Pedido")
            .setMessage("Total a pagar: $formattedPrice\n\n¿Deseas proceder con el pedido?")
            .setPositiveButton("Confirmar") { _, _ ->
                processOrder(cartItems, totalPrice)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun processOrder(cartItems: List<CartItem>, totalPrice: Double) {
        // Mostrar loading
        btnCheckout.isEnabled = false
        btnCheckout.text = "Procesando..."

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Obtener el ID del usuario
                val userId = tokenManager.getUserId()
                if (userId == -1) {
                    Toast.makeText(context, "Error: Usuario no autenticado", Toast.LENGTH_LONG).show()
                    btnCheckout.isEnabled = true
                    btnCheckout.text = "Finalizar Compra"
                    return@launch
                }

                // 1. Crear la orden en Xano
                val orderRequest = CreateOrderRequest(
                    total = totalPrice,
                    status = "en espera",
                    user_id = userId
                )

                val orderResponse = orderRepository.createOrder(orderRequest)

                if (!orderResponse.isSuccessful || orderResponse.body() == null) {
                    throw Exception("Error al crear la orden: ${orderResponse.message()}")
                }

                val createdOrder = orderResponse.body()!!
                val orderId = createdOrder.id ?: throw Exception("No se recibió el ID de la orden")

                // 2. Crear los order_items para cada producto en el carrito
                var itemsCreated = 0
                for (cartItem in cartItems) {
                    val orderItemRequest = CreateOrderItemRequest(
                        quantity = cartItem.quantity,
                        price = cartItem.productPrice,
                        order_id = orderId,
                        product_id = cartItem.productId
                    )

                    val itemResponse = orderRepository.createOrderItem(orderItemRequest)

                    if (itemResponse.isSuccessful) {
                        itemsCreated++
                    } else {
                        // Log del error pero continuar con los otros items
                        android.util.Log.e("CartFragment", "Error creando item: ${itemResponse.message()}")
                    }
                }

                // 3. Verificar que se crearon todos los items
                if (itemsCreated == cartItems.size) {
                    // Éxito total
                    cartManager.clearCart()
                    loadCartItems()

                    Toast.makeText(
                        context,
                        "✓ Pedido #$orderId creado exitosamente\nTotal: ${NumberFormat.getCurrencyInstance(Locale("es", "CL")).format(totalPrice)}",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    // Éxito parcial
                    cartManager.clearCart()
                    loadCartItems()

                    Toast.makeText(
                        context,
                        "Pedido creado con advertencias ($itemsCreated/${cartItems.size} items)",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Error al procesar el pedido: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                android.util.Log.e("CartFragment", "Error procesando orden", e)
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
