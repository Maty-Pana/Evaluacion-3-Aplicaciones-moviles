package com.janispaxano.JaniSPAKotlinAPP.ui.models

data class CartItem(
    val productId: Int,
    val productName: String,
    val productPrice: Double,
    val productImage: String,
    var quantity: Int
) {
    fun getTotalPrice(): Double = productPrice * quantity
}

// Modelos para las órdenes en la nube
data class Order(
    val id: Int? = null,
    val created_at: Long? = null,
    val total: Double,
    val status: String,
    val user_id: Int
)

data class OrderItem(
    val id: Int? = null,
    val created_at: Long? = null,
    val quantity: Int,
    val price: Double,
    val order_id: Int,
    val product_id: Int
)

// Modelo para agrupar órdenes por order_id
data class GroupedOrder(
    val orderId: Int,
    val status: String,
    val createdAt: Long,
    val items: List<OrderItem>,
    val totalProducts: Int,
    val totalPrice: Double
)

// Requests para la API
data class CreateOrderRequest(
    val total: Double,
    val status: String,
    val user_id: Int
)

data class CreateOrderItemRequest(
    val quantity: Int,
    val price: Double,
    val order_id: Int,
    val product_id: Int
)
