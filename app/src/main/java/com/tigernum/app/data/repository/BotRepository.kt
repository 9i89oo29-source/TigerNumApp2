package com.tigernum.app.data.repository

import com.tigernum.app.data.mapper.toDomain
import com.tigernum.app.data.remote.NetworkException
import com.tigernum.app.data.remote.NetworkResult
import com.tigernum.app.data.remote.RetrofitProvider
import com.tigernum.app.data.remote.dto.BuyRequest
import com.tigernum.app.domain.model.*
import com.tigernum.app.util.DeviceIdProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class BotRepository(
    private val deviceIdProvider: DeviceIdProvider
) {
    private val api = RetrofitProvider.getApiService(deviceIdProvider)

    private suspend fun <T> safeApiCall(call: suspend () -> T): NetworkResult<T> {
        return try {
            NetworkResult.Success(call())
        } catch (e: Exception) {
            NetworkResult.Error(NetworkException.fromThrowable(e))
        }
    }

    // --------------- Providers ---------------
    suspend fun getProviders(): NetworkResult<List<Provider>> {
        return safeApiCall { api.getProviders().map { it.toDomain() } }
    }

    // --------------- Services ---------------
    suspend fun getServices(): NetworkResult<List<Service>> {
        return safeApiCall { api.getServices().map { it.toDomain() } }
    }

    // --------------- Countries ---------------
    suspend fun getCountries(): NetworkResult<List<Country>> {
        return safeApiCall { api.getCountries().map { it.toDomain() } }
    }

    // --------------- Balance ---------------
    suspend fun getBalance(): NetworkResult<Double> {
        return safeApiCall { api.getBalance().balance }
    }

    // --------------- Buy Number ---------------
    suspend fun buyNumber(serviceId: String, countryCode: String): NetworkResult<Order> {
        val request = BuyRequest(serviceId = serviceId, countryCode = countryCode)
        return when (val apiResult = safeApiCall { api.buyNumber(request) }) {
            is NetworkResult.Success -> {
                val response = apiResult.data
                NetworkResult.Success(
                    Order(
                        orderId = response.orderId,
                        phoneNumber = response.phoneNumber,
                        serviceName = "",
                        status = OrderStatus.PENDING,
                        smsCode = null,
                        createdAt = "",
                        expiresAt = response.expiresAt
                    )
                )
            }
            is NetworkResult.Error -> NetworkResult.Error(apiResult.exception)
            is NetworkResult.Loading -> NetworkResult.Loading
        }
    }

    // --------------- SMS Status ---------------
    suspend fun getSmsStatus(orderId: String): NetworkResult<SmsMessage> {
        return safeApiCall { api.getSms(orderId).toDomain() }
    }

    fun pollSms(
        orderId: String,
        intervalMillis: Long = 5000L,
        maxAttempts: Int = 24
    ): Flow<NetworkResult<SmsMessage>> = flow {
        emit(NetworkResult.Loading)
        repeat(maxAttempts) {
            when (val result = getSmsStatus(orderId)) {
                is NetworkResult.Success -> {
                    val sms = result.data
                    if (sms.smsCode != null && sms.status == "RECEIVED") {
                        emit(NetworkResult.Success(sms))
                        return@flow
                    }
                }
                is NetworkResult.Error -> {
                    emit(NetworkResult.Error(result.exception))
                    return@flow
                }
                is NetworkResult.Loading -> { }
            }
            delay(intervalMillis)
        }
        emit(NetworkResult.Error(NetworkException.Timeout()))
    }

    // --------------- Orders History ---------------
    suspend fun getOrders(): NetworkResult<List<Order>> {
        return safeApiCall { api.getOrders().map { it.toDomain() } }
    }
}
