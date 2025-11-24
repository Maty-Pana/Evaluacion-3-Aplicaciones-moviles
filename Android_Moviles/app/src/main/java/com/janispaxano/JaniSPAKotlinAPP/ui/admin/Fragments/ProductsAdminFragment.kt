package com.janispaxano.JaniSPAKotlinAPP.ui.admin.Fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.janispaxano.JaniSPAKotlinAPP.data.remote.product.ProductRetrofitClient
import com.janispaxano.JaniSPAKotlinAPP.data.remote.products.UpdateProductRequest
import com.janispaxano.JaniSPAKotlinAPP.databinding.DialogEditProductBinding
import com.janispaxano.JaniSPAKotlinAPP.databinding.FragmentProductsAdminBinding
import com.janispaxano.JaniSPAKotlinAPP.ui.adapters.ProductAdminAdapter
import com.janispaxano.JaniSPAKotlinAPP.ui.models.Product
import kotlinx.coroutines.launch
import java.util.Locale

class ProductsAdminFragment : Fragment() {

    companion object {
        private const val TAG = "ProductsAdminFragment"
    }

    private var _binding: FragmentProductsAdminBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdminAdapter
    private var allProducts: List<Product> = emptyList()
    private var filteredProducts: List<Product> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductsAdminBinding.inflate(inflater, container, false)

        setupRecyclerView()
        setupSearchView()
        fetchProducts()

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.recyclerProducts.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                filterProducts(newText ?: "")
                return true
            }
        })
    }

    private fun filterProducts(query: String) {
        filteredProducts = if (query.isEmpty()) {
            allProducts
        } else {
            allProducts.filter { product ->
                product.name.lowercase(Locale.getDefault()).contains(query.lowercase(Locale.getDefault())) ||
                        product.description.lowercase(Locale.getDefault()).contains(query.lowercase(Locale.getDefault()))
            }
        }
        productAdapter.updateProducts(filteredProducts)
    }

    private fun fetchProducts() {
        binding.progressBar.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ProductRetrofitClient.api.getProducts()
                if (response.isSuccessful) {
                    allProducts = response.body() ?: emptyList()
                    filteredProducts = allProducts

                    productAdapter = ProductAdminAdapter(
                        products = filteredProducts,
                        onEditClick = { product -> showEditProductDialog(product) },
                        onDeleteClick = { product -> confirmDeleteProduct(product) }
                    )

                    binding.recyclerProducts.adapter = productAdapter
                    binding.progressBar.visibility = View.GONE

                    if (allProducts.isEmpty()) {
                        binding.emptyView.visibility = View.VISIBLE
                        binding.recyclerProducts.visibility = View.GONE
                    } else {
                        binding.emptyView.visibility = View.GONE
                        binding.recyclerProducts.visibility = View.VISIBLE
                    }
                } else {
                    showError("Error al cargar productos: ${response.code()}")
                }
            } catch (e: Exception) {
                showError("Error de conexión: ${e.message}")
            }
        }
    }

    private fun showEditProductDialog(product: Product) {
        val dialogBinding = DialogEditProductBinding.inflate(layoutInflater)

        // Configurar spinner de categorías
        val categories = arrayOf("Hogar", "Personal", "Otro")
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerCategory.adapter = adapter

        // Llenar los campos con los datos actuales del producto
        dialogBinding.etProductName.setText(product.name)
        dialogBinding.etProductDescription.setText(product.description)
        dialogBinding.etProductPrice.setText(product.price.toString())
        dialogBinding.etProductStock.setText(product.stock.toString())

        // Seleccionar la categoría actual del producto
        val currentCategoryIndex = categories.indexOf(product.category)
        if (currentCategoryIndex != -1) {
            dialogBinding.spinnerCategory.setSelection(currentCategoryIndex)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSave.setOnClickListener {
            val name = dialogBinding.etProductName.text.toString().trim()
            val description = dialogBinding.etProductDescription.text.toString().trim()
            val priceText = dialogBinding.etProductPrice.text.toString().trim()
            val stockText = dialogBinding.etProductStock.text.toString().trim()
            val category = dialogBinding.spinnerCategory.selectedItem.toString()

            // Validar campos
            if (name.isEmpty()) {
                dialogBinding.etProductName.error = "El nombre es requerido"
                return@setOnClickListener
            }

            if (priceText.isEmpty()) {
                dialogBinding.etProductPrice.error = "El precio es requerido"
                return@setOnClickListener
            }

            if (stockText.isEmpty()) {
                dialogBinding.etProductStock.error = "El stock es requerido"
                return@setOnClickListener
            }

            val price = priceText.toDoubleOrNull()
            val stock = stockText.toIntOrNull()

            if (price == null || price < 0) {
                dialogBinding.etProductPrice.error = "Precio inválido"
                return@setOnClickListener
            }

            if (stock == null || stock < 0) {
                dialogBinding.etProductStock.error = "Stock inválido"
                return@setOnClickListener
            }

            dialog.dismiss()
            updateProduct(product, name, description, price, stock, category)
        }

        dialog.show()
    }

    private fun updateProduct(
        product: Product,
        name: String,
        description: String,
        price: Double,
        stock: Int,
        category: String
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE

                // Validar que el producto tenga ID
                val productId = product.id
                if (productId == null) {
                    Toast.makeText(requireContext(), "Error: Producto sin ID válido", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                    return@launch
                }

                Log.d(TAG, "Actualizando producto ID: $productId")

                val updateRequest = UpdateProductRequest(
                    name = name,
                    description = description,
                    price = price,
                    stock = stock,
                    category = category,
                    image = product.image
                )

                Log.d(TAG, "Request de actualización: $updateRequest")
                val response = ProductRetrofitClient.api.updateProduct(productId, updateRequest)

                if (response.isSuccessful) {
                    Log.d(TAG, "Producto actualizado exitosamente")
                    Toast.makeText(requireContext(), "Producto actualizado exitosamente", Toast.LENGTH_SHORT).show()

                    // Actualizar la lista local
                    val updatedProduct = response.body()
                    if (updatedProduct != null) {
                        allProducts = allProducts.map {
                            if (it.id == product.id) updatedProduct else it
                        }
                        filteredProducts = filteredProducts.map {
                            if (it.id == product.id) updatedProduct else it
                        }
                        productAdapter.updateProducts(filteredProducts)
                    }
                } else {
                    val errorMsg = "Error al actualizar producto: ${response.code()} - ${response.message()}"
                    Log.e(TAG, errorMsg)
                    Log.e(TAG, "Body error: ${response.errorBody()?.string()}")
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepción al actualizar producto", e)
                Toast.makeText(requireContext(), "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun confirmDeleteProduct(product: Product) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Producto")
            .setMessage("¿Estás seguro de que deseas eliminar '${product.name}'?\n\nEsta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteProduct(product)
            }
            .setNegativeButton("Cancelar", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun deleteProduct(product: Product) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE

                // Validar que el producto tenga ID
                val productId = product.id
                if (productId == null) {
                    Toast.makeText(requireContext(), "Error: Producto sin ID válido", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                    return@launch
                }

                val response = ProductRetrofitClient.api.deleteProduct(productId)

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Producto '${product.name}' eliminado exitosamente", Toast.LENGTH_SHORT).show()

                    // Actualizar la lista local removiendo el producto
                    allProducts = allProducts.filter { it.id != product.id }
                    filteredProducts = filteredProducts.filter { it.id != product.id }
                    productAdapter.updateProducts(filteredProducts)

                    // Mostrar vista vacía si no hay más productos
                    if (filteredProducts.isEmpty()) {
                        binding.emptyView.visibility = View.VISIBLE
                        binding.recyclerProducts.visibility = View.GONE
                    }
                } else {
                    showError("Error al eliminar producto: ${response.code()}")
                }
            } catch (e: Exception) {
                showError("Error de conexión: ${e.message}")
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}