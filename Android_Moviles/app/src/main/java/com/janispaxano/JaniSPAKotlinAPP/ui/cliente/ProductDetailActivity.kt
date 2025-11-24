package com.janispaxano.JaniSPAKotlinAPP.ui.cliente

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.janispaxano.JaniSPAKotlinAPP.R
import com.janispaxano.JaniSPAKotlinAPP.databinding.ActivityProductDetailBinding
import com.janispaxano.JaniSPAKotlinAPP.data.local.CartManager
import com.janispaxano.JaniSPAKotlinAPP.ui.models.CartItem

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private lateinit var cartManager: CartManager
    private var quantity = 1
    private var stock = 0
    private var productId = 0
    private var productName = ""
    private var productPrice = 0.0
    private var productImageUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar CartManager
        cartManager = CartManager(this)

        // ✅ Configurar Toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarDetail)
        setSupportActionBar(toolbar)

        // Asegurar que el título sea blanco
        supportActionBar?.setDisplayShowTitleEnabled(true)
        toolbar.setTitleTextColor(android.graphics.Color.WHITE)

        // ✅ Recibir datos del Intent
        productId = intent.getIntExtra("id", 0)
        productName = intent.getStringExtra("name") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        productPrice = intent.getDoubleExtra("price", 0.0)
        stock = intent.getIntExtra("stock", 0)
        productImageUrl = intent.getStringExtra("imageUrl") ?: ""

        // ✅ Mostrar datos en la UI
        binding.txtDetailName.text = productName
        binding.txtDetailDescription.text = description
        binding.txtDetailPrice.text = "$$productPrice"
        binding.txtDetailStock.text = "Stock: $stock"

        Glide.with(this)
            .load(productImageUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .into(binding.imgDetailProduct)

        // ✅ Configurar controles de cantidad
        setupQuantityControls(stock)

        // ✅ Botón de agregar al carrito
        binding.btnAddToCart.setOnClickListener {
            addToCart()
        }

        // ✅ Botón de volver
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupQuantityControls(stock: Int) {
        // Actualizar display inicial de cantidad
        binding.txtQuantity.text = quantity.toString()

        // Botón incrementar
        binding.btnIncrease.setOnClickListener {
            if (quantity < stock) {
                quantity++
                binding.txtQuantity.text = quantity.toString()
            } else {
                Toast.makeText(this, "Stock máximo alcanzado", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón decrementar
        binding.btnDecrease.setOnClickListener {
            if (quantity > 1) {
                quantity--
                binding.txtQuantity.text = quantity.toString()
            }
        }
    }

    private fun addToCart() {
        // Validar stock
        if (stock <= 0) {
            Toast.makeText(this, "Producto sin stock disponible", Toast.LENGTH_SHORT).show()
            return
        }

        if (quantity > stock) {
            Toast.makeText(this, "La cantidad supera el stock disponible", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear item del carrito
        val cartItem = CartItem(
            productId = productId,
            productName = productName,
            productPrice = productPrice,
            productImage = productImageUrl,
            quantity = quantity
        )

        // Agregar al carrito
        cartManager.addToCart(cartItem)

        Toast.makeText(
            this,
            "✓ $quantity x $productName agregado al carrito",
            Toast.LENGTH_SHORT
        ).show()

        // Resetear cantidad a 1
        quantity = 1
        binding.txtQuantity.text = quantity.toString()
    }
}
