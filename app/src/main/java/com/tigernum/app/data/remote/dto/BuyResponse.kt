package com.tigernum.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BuyResponse(
    @SerialName("order_id") val orderId: String,
    @SerialName("phone_number") val phoneNumber: String,
    val status: String,
    @SerialName("expires_at") val expiresAt: String? = null
)
