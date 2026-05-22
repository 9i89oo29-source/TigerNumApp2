package com.tigernum.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class BuyRequest(
    val serviceId: String,
    val countryCode: String,
    val providerSlug: String? = null
)
