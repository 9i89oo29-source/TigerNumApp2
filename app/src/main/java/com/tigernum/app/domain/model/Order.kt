package com.tigernum.app.domain.model

data class Order(
    val orderId: String,
    val phoneNumber: String,
    val serviceName: String,
    val status: OrderStatus,
    val smsCode: String?,
    val createdAt: String,
    val expiresAt: String?
)

enum class OrderStatus {
    PENDING,
    COMPLETED,
    CANCELLED,
    UNKNOWN
}
