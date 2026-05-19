package com.tigernum.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SmsResponse(
    @SerialName("order_id") val orderId: String,
    val status: String,                  // "WAITING", "RECEIVED"
    @SerialName("sms_code") val smsCode: String? = null,
    @SerialName("received_at") val receivedAt: String? = null
)
