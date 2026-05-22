package com.tigernum.app.data.remote.api

import com.tigernum.app.data.remote.dto.*
import kotlinx.serialization.Serializable
import retrofit2.http.*

interface BotApiService {

    @POST("auth/device")
    suspend fun authDevice(@Body request: DeviceAuthRequest): AuthResponse

    @GET("services")
    suspend fun getServices(): ApiResponse<List<ServiceDto>>

    @GET("countries")
    suspend fun getCountries(): ApiResponse<List<CountryDto>>

    @GET("providers")
    suspend fun getProviders(): ApiResponse<List<ProviderDto>>

    @GET("balance")
    suspend fun getBalance(): ApiResponse<BalanceDto>

    @POST("orders")
    suspend fun buyNumber(@Body request: BuyRequest): ApiResponse<BuyResponse>

    @GET("sms")
    suspend fun getSms(@Query("order_id") orderId: String): ApiResponse<SmsResponse>

    @GET("orders")
    suspend fun getOrders(): ApiResponse<List<OrderDto>>
}

@Serializable
data class ApiResponse<T>(
    val status: String,
    val data: T,
    val results: Int? = null
)

@Serializable
data class DeviceAuthRequest(
    val fingerprint: String,
    val deviceId: String? = null,
    val appSignature: String? = null,
    val installTimestamp: String? = null
)

@Serializable
data class AuthResponse(
    val status: String,
    val data: AuthResponseData
)

@Serializable
data class AuthResponseData(
    val accessToken: String,
    val refreshToken: String,
    val user: UserInfo,
    val device: DeviceInfo
)

@Serializable
data class UserInfo(
    val id: String,
    val telegramId: Long? = null,
    val role: String,
    val balance: Double,
    val subscription: String
)

@Serializable
data class DeviceInfo(
    val fingerprint: String,
    val isNew: Boolean
)
