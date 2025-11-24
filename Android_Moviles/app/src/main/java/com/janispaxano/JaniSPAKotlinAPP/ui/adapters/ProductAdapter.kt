package com.janispaxano.JaniSPAKotlinAPP.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.janispaxano.JaniSPAKotlinAPP.R
import com.janispaxano.JaniSPAKotlinAPP.databinding.ItemProductBinding
import com.janispaxano.JaniSPAKotlinAPP.ui.models.Product
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    private var products: List<Product>, // Cambia a var
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "CL"))

        holder.binding.txtProductName.text = product.name
        holder.binding.txtProductPrice.text = numberFormat.format(product.price)
        holder.binding.txtProductStock.text = "Stock: ${product.stock}"

        // Cargar la URL de la primera imagen
        val imageUrl = product.getImageUrl()
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_launcher_foreground)
            .into(holder.binding.imgProduct)

        // ✅ SOLO llamar al callback - NO lanzar el Intent aquí
        holder.itemView.setOnClickListener {
            onItemClick(product)
        }
    }

    override fun getItemCount(): Int = products.size

    // 🔹 AGREGAR ESTE MÉTODO PARA LA BÚSQUEDA EN TIEMPO REAL
    fun updateProducts(newProducts: List<Product>) {
        this.products = newProducts
        notifyDataSetChanged()
    }
}
