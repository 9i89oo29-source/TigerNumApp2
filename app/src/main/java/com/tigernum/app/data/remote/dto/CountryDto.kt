package com.tigernum.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CountryDto(
    val code: String,         // e.g., "+20"
    val name: String,         // e.g., "مصر"
    val flag: String,         // e.g., "🇪🇬"
    @SerialName("dial_code") val dialCode: String = code
)
