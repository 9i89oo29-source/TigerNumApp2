package com.tigernum.app.data.mapper

import com.tigernum.app.data.remote.dto.*
import com.tigernum.app.domain.model.*

fun ServiceDto.toDomain() = Service(
    id = id,
    name = name,
    price = price,
    available = available
)

fun CountryDto.toDomain() = Country(
    code = code,
    name = name,
    flag = flag,
    dialCode = dialCode
)

fun ProviderDto.toDomain() = Provider(
    id = id,
    name = name
)

fun OrderDto.toDomain() = Order(
    orderId = orderId,
    phoneNumber = phoneNumber,
    serviceName = serviceName,
    status = when (status.uppercase()) {
        "PENDING" -> OrderStatus.PENDING
        "COMPLETED" -> OrderStatus.COMPLETED
        "CANCELLED" -> OrderStatus.CANCELLED
        else -> OrderStatus.UNKNOWN
    },
    smsCode = smsCode,
    createdAt = createdAt,
    expiresAt = expiresAt
)

fun SmsResponse.toDomain() = SmsMessage(
    orderId = orderId,
    smsCode = smsCode,
    status = status
)
