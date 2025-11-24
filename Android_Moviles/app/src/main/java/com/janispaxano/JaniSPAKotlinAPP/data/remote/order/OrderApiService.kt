package com.janispaxano.JaniSPAKotlinAPP.data.remote.order

import com.janispaxano.JaniSPAKotlinAPP.ui.models.*
import retrofit2.Response
import retrofit2.http.*

interface OrderApiService {

    // Endpoints de productos
    @GET("product")
    suspend fun getProducts(): Response<List<Product>>

    @GET("product/{id}")
    suspend fun getProduct(@Path("id") id: Int): Response<Product>

    @POST("product")
    suspend fun createProduct(@Body product: Product): Response<Product>

    @PATCH("product/{id}")
    suspend fun updateProduct(@Path("id") id: Int, @Body product: Product): Response<Product>

    @DELETE("product/{id}")
    suspend fun deleteProduct(@Path("id") id: Int): Response<Unit>

    // Endpoints de órdenes
    @GET("order")
    suspend fun getOrders(): Response<List<Order>>

    @GET("order/{order_id}")
    suspend fun getOrder(@Path("order_id") orderId: Int): Response<Order>

    @POST("order")
    suspend fun createOrder(@Body orderRequest: CreateOrderRequest): Response<Order>

    @PATCH("order/{order_id}")
    suspend fun updateOrder(@Path("order_id") orderId: Int, @Body order: Order): Response<Order>

    @DELETE("order/{order_id}")
    suspend fun deleteOrder(@Path("order_id") orderId: Int): Response<Unit>

    // Endpoints de items de orden
    @GET("order_item")
    suspend fun getOrderItems(): Response<List<OrderItem>>

    @GET("order_item/{order_item_id}")
    suspend fun getOrderItem(@Path("order_item_id") orderItemId: Int): Response<OrderItem>

    @POST("order_item")
    suspend fun createOrderItem(@Body orderItemRequest: CreateOrderItemRequest): Response<OrderItem>

    @PATCH("order_item/{order_item_id}")
    suspend fun updateOrderItem(@Path("order_item_id") orderItemId: Int, @Body orderItem: OrderItem): Response<OrderItem>

    @DELETE("order_item/{order_item_id}")
    suspend fun deleteOrderItem(@Path("order_item_id") orderItemId: Int): Response<Unit>
}
