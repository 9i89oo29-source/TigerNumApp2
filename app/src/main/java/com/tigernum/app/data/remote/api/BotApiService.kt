package com.tigernum.app.data.remote.api

import com.tigernum.app.data.remote.dto.*
import retrofit2.http.*

interface BotApiService {

    @GET("services")
    suspend fun getServices(): List<ServiceDto>

    @GET("countries")
    suspend fun getCountries(): List<CountryDto>

    @GET("balance")
    suspend fun getBalance(): BalanceDto

    @POST("buy")
    suspend fun buyNumber(@Body request: BuyRequest): BuyResponse

    @GET("sms")
    suspend fun getSms(@Query("order_id") orderId: String): SmsResponse

    @GET("orders")
    suspend fun getOrders(): List<OrderDto>
}
