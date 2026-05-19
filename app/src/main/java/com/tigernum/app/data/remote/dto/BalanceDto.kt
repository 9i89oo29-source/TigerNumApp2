package com.tigernum.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class BalanceDto(
    val balance: Double,
    val currency: String = "USD"
)
