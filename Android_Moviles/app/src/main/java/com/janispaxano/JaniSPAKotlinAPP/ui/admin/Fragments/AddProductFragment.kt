package com.janispaxano.JaniSPAKotlinAPP.ui.admin.Fragments

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.janispaxano.JaniSPAKotlinAPP.data.remote.auth.TokenManager
import com.janispaxano.JaniSPAKotlinAPP.data.remote.product.ProductRetrofitClient
import com.janispaxano.JaniSPAKotlinAPP.data.remote.products.CreateProductRequest
import com.janispaxano.JaniSPAKotlinAPP.data.remote.products.ImageUploadResponse
import com.janispaxano.JaniSPAKotlinAPP.databinding.FragmentAddProductBinding
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class AddProductFragment : Fragment() {

    companion object {
        private const val TAG = "AddProductFragment"
    }

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!

    private lateinit var tokenManager: TokenManager

    private var selectedImageUri: Uri? = null
    private var uploadedImageResponse: ImageUploadResponse? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openImagePicker()
        } else {
            Toast.makeText(context, "Permiso denegado para acceder a imágenes", Toast.LENGTH_SHORT).show()
        }
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                binding.ivProductImage.setImageURI(uri)
                binding.btnSelectImage.text = "Cambiar Imagen"
                Log.d(TAG, "Imagen seleccionada: $uri")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinner()
        setupClickListeners()

        tokenManager = TokenManager(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupSpinner() {
        val categories = arrayOf("Hogar", "Personal", "Otro")
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnSelectImage.setOnClickListener {
            checkPermissionAndOpenPicker()
        }

        binding.btnCreateProduct.setOnClickListener {
            createProduct()
        }
    }

    private fun checkPermissionAndOpenPicker() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED -> {
                openImagePicker()
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun createProduct() {
        if (!validateFields()) {
            return
        }

        val name = binding.etProductName.text.toString().trim()
        val description = binding.etProductDescription.text.toString().trim()
        val price = binding.etProductPrice.text.toString().toDoubleOrNull() ?: 0.0
        val stock = binding.etProductStock.text.toString().toIntOrNull() ?: 0
        val category = binding.spinnerCategory.selectedItem.toString()

        Log.d(TAG, "Iniciando creación de producto: $name")
        showLoading(true)

        lifecycleScope.launch {
            try {
                // Primero subir la imagen si se seleccionó una
                if (selectedImageUri != null) {
                    Log.d(TAG, "Subiendo imagen...")
                    val imageResponse = uploadImage()
                    if (imageResponse != null) {
                        Log.d(TAG, "Imagen subida exitosamente: ${imageResponse.path}")
                        uploadedImageResponse = imageResponse
                        // Crear producto con imagen
                        createProductWithData(name, description, price, stock, category, listOf(imageResponse))
                    } else {
                        showLoading(false)
                        Toast.makeText(context, "Error al subir la imagen. Intente nuevamente.", Toast.LENGTH_LONG).show()
                        Log.e(TAG, "Error al subir imagen: respuesta nula")
                    }
                } else {
                    // Crear producto sin imagen
                    Log.d(TAG, "Creando producto sin imagen")
                    createProductWithData(name, description, price, stock, category, emptyList())
                }
            } catch (e: Exception) {
                showLoading(false)
                val errorMsg = "Error al crear producto: ${e.message}"
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                Log.e(TAG, errorMsg, e)
            }
        }
    }

    private suspend fun uploadImage(): ImageUploadResponse? {
        return try {
            selectedImageUri?.let { uri ->
                Log.d(TAG, "==================== INICIO SUBIDA DE IMAGEN ====================")
                Log.d(TAG, "URI seleccionada: $uri")

                // Obtener el tipo MIME real de la imagen
                val mimeType = requireContext().contentResolver.getType(uri) ?: "image/jpeg"
                Log.d(TAG, "Tipo MIME detectado: $mimeType")

                val inputStream = requireContext().contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    Log.e(TAG, "ERROR: No se pudo abrir el InputStream de la URI")
                    return null
                }

                // Determinar la extensión del archivo
                val extension = when {
                    mimeType.contains("png") -> ".png"
                    mimeType.contains("jpg") || mimeType.contains("jpeg") -> ".jpg"
                    mimeType.contains("webp") -> ".webp"
                    else -> ".jpg"
                }

                val file = File(requireContext().cacheDir, "temp_image_${System.currentTimeMillis()}$extension")

                // Comprimir la imagen antes de subirla
                inputStream.use { input ->
                    val bitmap = android.graphics.BitmapFactory.decodeStream(input)
                    FileOutputStream(file).use { output ->
                        // Comprimir al 70% de calidad para reducir el tamaño
                        val format = if (mimeType.contains("png")) {
                            android.graphics.Bitmap.CompressFormat.PNG
                        } else {
                            android.graphics.Bitmap.CompressFormat.JPEG
                        }
                        bitmap.compress(format, 70, output)
                        bitmap.recycle()
                    }
                }

                if (!file.exists()) {
                    Log.e(TAG, "ERROR: El archivo temporal no existe después de copiarlo")
                    return null
                }

                Log.d(TAG, "Archivo preparado (comprimido):")
                Log.d(TAG, "  - Nombre: ${file.name}")
                Log.d(TAG, "  - Tamaño original: 2002764 bytes")
                Log.d(TAG, "  - Tamaño comprimido: ${file.length()} bytes")
                Log.d(TAG, "  - Reducción: ${((1 - file.length().toFloat() / 2002764) * 100).toInt()}%")
                Log.d(TAG, "  - Tipo MIME: $mimeType")
                Log.d(TAG, "  - Ruta: ${file.absolutePath}")

                val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("content", file.name, requestFile)

                Log.d(TAG, "Enviando imagen al servidor...")
                Log.d(TAG, "  - Endpoint: ${ProductRetrofitClient.api.javaClass.name}")
                Log.d(TAG, "  - URL: upload/image")
                Log.d(TAG, "  - Parámetro: content")

                val response = ProductRetrofitClient.api.uploadImage(body)

                // Limpiar archivo temporal
                file.delete()
                Log.d(TAG, "Archivo temporal eliminado")

                Log.d(TAG, "Respuesta recibida:")
                Log.d(TAG, "  - Código HTTP: ${response.code()}")
                Log.d(TAG, "  - Mensaje: ${response.message()}")
                Log.d(TAG, "  - Es exitoso: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    val imageResponse = response.body()
                    if (imageResponse != null) {
                        Log.d(TAG, "✓✓✓ IMAGEN SUBIDA EXITOSAMENTE ✓✓✓")
                        Log.d(TAG, "  - Access: ${imageResponse.access}")
                        Log.d(TAG, "  - Path: ${imageResponse.path}")
                        Log.d(TAG, "  - Name: ${imageResponse.name}")
                        Log.d(TAG, "  - Type: ${imageResponse.type}")
                        Log.d(TAG, "  - Size: ${imageResponse.size}")
                        Log.d(TAG, "  - Mime: ${imageResponse.mime}")
                        Log.d(TAG, "  - Meta Width: ${imageResponse.meta.width}")
                        Log.d(TAG, "  - Meta Height: ${imageResponse.meta.height}")
                        Log.d(TAG, "==================== FIN SUBIDA EXITOSA ====================")
                        imageResponse
                    } else {
                        Log.e(TAG, "✗✗✗ ERROR: Respuesta exitosa pero body es null")
                        Log.d(TAG, "==================== FIN CON ERROR ====================")
                        null
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "✗✗✗ ERROR EN LA SUBIDA DE IMAGEN ✗✗✗")
                    Log.e(TAG, "  - Código HTTP: ${response.code()}")
                    Log.e(TAG, "  - Mensaje: ${response.message()}")
                    Log.e(TAG, "  - Error Body: $errorBody")
                    Log.e(TAG, "  - Headers: ${response.headers()}")
                    Log.d(TAG, "==================== FIN CON ERROR ====================")
                    null
                }
            } ?: run {
                Log.e(TAG, "ERROR: selectedImageUri es null")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗✗✗ EXCEPCIÓN AL SUBIR IMAGEN ✗✗✗")
            Log.e(TAG, "  - Tipo: ${e.javaClass.simpleName}")
            Log.e(TAG, "  - Mensaje: ${e.message}")
            Log.e(TAG, "  - Stack trace:")
            e.printStackTrace()
            Log.d(TAG, "==================== FIN CON EXCEPCIÓN ====================")
            null
        }
    }

    private suspend fun createProductWithData(
        name: String,
        description: String,
        price: Double,
        stock: Int,
        category: String,
        image: List<ImageUploadResponse>?
    ) {
        try {
            Log.d(TAG, "Creando request de producto...")

            val productRequest = CreateProductRequest(
                name = name,
                description = description,
                price = price,
                stock = stock,
                category = category,
                image = image
            )

            Log.d(TAG, "Enviando producto al servidor: $productRequest")
            val response = ProductRetrofitClient.api.createProduct(productRequest)

            showLoading(false)

            if (response.isSuccessful) {
                val product = response.body()
                Log.d(TAG, "Producto creado exitosamente: $product")
                Toast.makeText(context, "Producto creado exitosamente", Toast.LENGTH_SHORT).show()
                clearForm()
                // Navegar de vuelta al fragment de productos
                parentFragmentManager.popBackStack()
            } else {
                val errorMsg = "Error al crear producto: ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMsg)
                Log.e(TAG, "Body error: ${response.errorBody()?.string()}")
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            showLoading(false)
            val errorMsg = "Excepción al crear producto: ${e.message}"
            Log.e(TAG, errorMsg, e)
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
        }
    }

    private fun validateFields(): Boolean {
        val name = binding.etProductName.text.toString().trim()
        val description = binding.etProductDescription.text.toString().trim()
        val priceText = binding.etProductPrice.text.toString().trim()
        val stockText = binding.etProductStock.text.toString().trim()

        when {
            name.isEmpty() -> {
                binding.etProductName.error = "El nombre es requerido"
                binding.etProductName.requestFocus()
                return false
            }
            description.isEmpty() -> {
                binding.etProductDescription.error = "La descripción es requerida"
                binding.etProductDescription.requestFocus()
                return false
            }
            priceText.isEmpty() -> {
                binding.etProductPrice.error = "El precio es requerido"
                binding.etProductPrice.requestFocus()
                return false
            }
            priceText.toDoubleOrNull() == null || priceText.toDouble() <= 0 -> {
                binding.etProductPrice.error = "Ingrese un precio válido"
                binding.etProductPrice.requestFocus()
                return false
            }
            stockText.isEmpty() -> {
                binding.etProductStock.error = "El stock es requerido"
                binding.etProductStock.requestFocus()
                return false
            }
            stockText.toIntOrNull() == null || stockText.toInt() < 0 -> {
                binding.etProductStock.error = "Ingrese un stock válido"
                binding.etProductStock.requestFocus()
                return false
            }
        }

        return true
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnCreateProduct.isEnabled = !show
        binding.btnSelectImage.isEnabled = !show
    }

    private fun clearForm() {
        binding.etProductName.text.clear()
        binding.etProductDescription.text.clear()
        binding.etProductPrice.text.clear()
        binding.etProductStock.text.clear()
        binding.spinnerCategory.setSelection(0)
        selectedImageUri = null
        uploadedImageResponse = null
        binding.ivProductImage.setImageResource(com.janispaxano.JaniSPAKotlinAPP.R.drawable.ic_add_photo)
        binding.btnSelectImage.text = "Seleccionar Imagen"
    }
}