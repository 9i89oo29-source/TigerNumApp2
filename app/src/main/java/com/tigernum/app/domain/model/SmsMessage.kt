package com.tigernum.app.domain.model

data class SmsMessage(
    val orderId: String,
    val smsCode: String?,
    val status: String
)
