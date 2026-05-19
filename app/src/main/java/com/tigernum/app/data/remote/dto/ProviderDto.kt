package com.tigernum.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProviderDto(
    val id: String,
    val name: String
)
