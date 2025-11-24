package com.janispaxano.JaniSPAKotlinAPP.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.janispaxano.JaniSPAKotlinAPP.R
import com.janispaxano.JaniSPAKotlinAPP.databinding.ItemProductAdminBinding
import com.janispaxano.JaniSPAKotlinAPP.ui.models.Product
import java.text.NumberFormat
import java.util.Locale

class ProductAdminAdapter(
    private var products: List<Product>,
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdminAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(val binding: ItemProductAdminBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "CL"))

        with(holder.binding) {
            txtProductName.text = product.name
            txtProductPrice.text = numberFormat.format(product.price)
            txtProductStock.text = "Stock: ${product.stock}"
            txtProductDescription.text = product.description

            // Cargar la URL de la primera imagen
            Glide.with(root.context)
                .load(product.getImageUrl())
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(imgProduct)

            // Click en botón editar
            btnEdit.setOnClickListener {
                onEditClick(product)
            }

            // Click en botón eliminar
            btnDelete.setOnClickListener {
                onDeleteClick(product)
            }
        }
    }

    override fun getItemCount(): Int = products.size

    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}
