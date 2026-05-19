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

/**
 * Single source of truth for all bot backend data.
 * Uses the bot API through Retrofit and maps DTOs to domain models.
 */
class BotRepository(
    private val deviceIdProvider: DeviceIdProvider
) {
    private val api = RetrofitProvider.getApiService(deviceIdProvider)

    /**
     * Generic wrapper that catches network exceptions and returns [NetworkResult].
     */
    private suspend fun <T> safeApiCall(call: suspend () -> T): NetworkResult<T> {
        return try {
            NetworkResult.Success(call())
        } catch (e: Exception) {
            NetworkResult.Error(NetworkException.fromThrowable(e))
        }
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
        return safeApiCall { api.buyNumber(request) }.let { result ->
            when (result) {
                is NetworkResult.Success -> {
                    // BuyResponse fields: orderId, phoneNumber, status, expiresAt
                    val response = result.data
                    NetworkResult.Success(
                        Order(
                            orderId = response.orderId,
                            phoneNumber = response.phoneNumber,
                            serviceName = "", // not returned; we can fill later or ignore
                            status = OrderStatus.PENDING,
                            smsCode = null,
                            createdAt = "",
                            expiresAt = response.expiresAt
                        )
                    )
                }
                is NetworkResult.Error -> NetworkResult.Error(result.exception)
            }
        }
    }

    // --------------- SMS Polling ---------------
    suspend fun getSmsStatus(orderId: String): NetworkResult<SmsMessage> {
        return safeApiCall { api.getSms(orderId).toDomain() }
    }

    /**
     * Polls the backend for SMS code until received or timeout.
     * @param orderId The order identifier to poll.
     * @param intervalMillis Polling interval.
     * @param maxAttempts Maximum attempts before timeout.
     * @return Flow emitting loading, success or error.
     */
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
