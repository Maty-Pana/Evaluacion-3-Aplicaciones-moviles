package com.janispaxano.JaniSPAKotlinAPP.ui.cliente.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.R
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.janispaxano.JaniSPAKotlinAPP.data.remote.product.ProductRetrofitClient
import com.janispaxano.JaniSPAKotlinAPP.databinding.FragmentProductsBinding
import com.janispaxano.JaniSPAKotlinAPP.ui.adapters.ProductAdapter
import com.janispaxano.JaniSPAKotlinAPP.ui.cliente.ProductDetailActivity
import com.janispaxano.JaniSPAKotlinAPP.ui.models.Product
import kotlinx.coroutines.launch
import java.util.Locale

class ProductsFragment : Fragment() {

    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter
    private var allProducts: List<Product> = emptyList()
    private var filteredProducts: List<Product> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProductsBinding.inflate(inflater, container, false)

        binding.recyclerProducts.layoutManager = LinearLayoutManager(requireContext())

        setupSearchView()
        fetchProducts()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupSearchView() {
        val searchView: SearchView = binding.searchView
        val searchIcon = searchView.findViewById<ImageView>(R.id.search_mag_icon)
        searchIcon.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.black))

        val closeIcon = searchView.findViewById<ImageView>(R.id.search_close_btn)
        closeIcon.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.black))

        val searchText = searchView.findViewById<EditText>(R.id.search_src_text)
        searchText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
        searchText.setHintTextColor(ContextCompat.getColor(requireContext(), com.janispaxano.JaniSPAKotlinAPP.R.color.colorBackground))

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterProducts(newText.orEmpty())
                return true
            }
        })
    }

    private fun filterProducts(query: String) {
        filteredProducts = if (query.isEmpty()) {
            allProducts
        } else {
            allProducts.filter { product ->
                product.name?.lowercase(Locale.getDefault())?.contains(query.lowercase(Locale.getDefault())) == true ||
                        product.description?.lowercase(Locale.getDefault())?.contains(query.lowercase(
                            Locale.getDefault())) == true
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

                    productAdapter = ProductAdapter(filteredProducts) { selectedProduct ->
                        showProductDetails(selectedProduct)
                    }
                    binding.recyclerProducts.adapter = productAdapter
                } else {
                    Toast.makeText(requireContext(), "Error al obtener productos", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun showProductDetails(product: Product) {
        val intent = Intent(requireContext(), ProductDetailActivity::class.java)
        intent.putExtra("id", product.id)
        intent.putExtra("name", product.name)
        intent.putExtra("description", product.description)
        intent.putExtra("price", product.price)
        intent.putExtra("stock", product.stock)
        intent.putExtra("imageUrl", product.getImageUrl())
        startActivity(intent)
    }
}