package com.tigernum.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderDto(
    @SerialName("order_id") val orderId: String,
    @SerialName("phone_number") val phoneNumber: String,
    @SerialName("service_name") val serviceName: String,
    val status: String,                      // "PENDING", "COMPLETED", "CANCELLED"
    @SerialName("sms_code") val smsCode: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("expires_at") val expiresAt: String? = null
)
