package com.tigernum.app.data.remote.api

import com.tigernum.app.data.remote.dto.*
import retrofit2.http.*

interface BotApiService {

    // نقطة نهاية المصادقة
    @POST("auth/device")
    suspend fun authDevice(@Body request: DeviceAuthRequest): AuthResponse

    @GET("services")
    suspend fun getServices(): List<ServiceDto>

    @GET("countries")
    suspend fun getCountries(): List<CountryDto>

    @GET("providers")
    suspend fun getProviders(): List<ProviderDto>

    @GET("balance")
    suspend fun getBalance(): BalanceDto

    @POST("orders")  // الملاحظة: تم تغيير "buy" إلى "orders" حسب الخادم
    suspend fun buyNumber(@Body request: BuyRequest): BuyResponse

    @GET("sms")
    suspend fun getSms(@Query("order_id") orderId: String): SmsResponse

    @GET("orders")
    suspend fun getOrders(): List<OrderDto>
}

// نماذج طلب واستجابة المصادقة
data class DeviceAuthRequest(
    val fingerprint: String,
    val deviceId: String? = null,
    val appSignature: String? = null,
    val installTimestamp: String? = null
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserInfo,
    val device: DeviceInfo
)

data class UserInfo(
    val id: String,
    val telegramId: Long?,
    val role: String,
    val balance: Double,
    val subscription: String
)

data class DeviceInfo(
    val fingerprint: String,
    val isNew: Boolean
)
