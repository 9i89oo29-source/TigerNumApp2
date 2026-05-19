package com.tigernum.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServiceDto(
    val id: String,
    val name: String,
    val price: Double,
    val available: Boolean
)
