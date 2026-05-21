package com.tigernum.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CountryDto(
    val code: String,
    val name: String,
    @SerialName("nameAr") val nameAr: String? = null,
    val flag: String? = null,
    @SerialName("dialCode") val dialCode: String = ""
)
