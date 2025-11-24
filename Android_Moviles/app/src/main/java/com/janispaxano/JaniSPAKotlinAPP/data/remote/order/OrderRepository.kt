package com.janispaxano.JaniSPAKotlinAPP.data.remote.order

import com.janispaxano.JaniSPAKotlinAPP.ui.models.*
import retrofit2.Response

class OrderRepository {

    private val apiService = OrderRetrofitClient.instance.create(OrderApiService::class.java)

    suspend fun createOrder(orderRequest: CreateOrderRequest): Response<Order> {
        return apiService.createOrder(orderRequest)
    }

    suspend fun createOrderItem(orderItemRequest: CreateOrderItemRequest): Response<OrderItem> {
        return apiService.createOrderItem(orderItemRequest)
    }

    suspend fun getOrders(): Response<List<Order>> {
        return apiService.getOrders()
    }

    suspend fun getOrder(orderId: Int): Response<Order> {
        return apiService.getOrder(orderId)
    }

    suspend fun updateOrderStatus(orderId: Int, status: String): Response<Order> {
        val orderUpdate = Order(
            id = orderId,
            total = 0.0, // Este valor será ignorado por el PATCH
            status = status,
            user_id = 0 // Este valor será ignorado por el PATCH
        )
        return apiService.updateOrder(orderId, orderUpdate)
    }

    suspend fun getOrderItems(): Response<List<OrderItem>> {
        return apiService.getOrderItems()
    }
}
