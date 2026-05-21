package com.tigernum.app.ui.buy

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tigernum.app.data.remote.NetworkResult
import com.tigernum.app.data.remote.NetworkException
import com.tigernum.app.data.remote.RetrofitProvider
import com.tigernum.app.data.remote.api.BotApiService
import com.tigernum.app.data.remote.dto.BuyRequest
import com.tigernum.app.data.remote.dto.SmsResponse
import com.tigernum.app.data.local.DeviceManager
import com.tigernum.app.domain.model.Order
import com.tigernum.app.domain.model.OrderStatus
import com.tigernum.app.domain.model.SmsMessage
import com.tigernum.app.util.DeviceIdProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BuyNumberUiState(
    val order: Order? = null,
    val smsMessage: SmsMessage? = null,
    val isBuying: Boolean = false,
    val isPolling: Boolean = false,
    val error: String? = null
)

class BuyNumberViewModel(application: Application) : AndroidViewModel(application) {

    private val deviceIdProvider = DeviceIdProvider(application)
    private val deviceManager = DeviceManager(application)

    private val api: BotApiService = RetrofitProvider.getApiService(
        deviceIdProvider = deviceIdProvider,
        tokenProvider = { deviceManager.getString("jwt_token", null) }
    )

    private val _uiState = MutableStateFlow(BuyNumberUiState())
    val uiState: StateFlow<BuyNumberUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null

    fun buyNumber(provider: String, serviceId: String, countryCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBuying = true, error = null, order = null, smsMessage = null) }

            val result = try {
                val response = api.buyNumber(BuyRequest(serviceId = serviceId, countryCode = countryCode))
                NetworkResult.Success(response)
            } catch (e: Exception) {
                NetworkResult.Error(NetworkException.fromThrowable(e))
            }

            when (result) {
                is NetworkResult.Success -> {
                    val buyResp = result.data
                    val order = Order(
                        orderId = buyResp.orderId,
                        phoneNumber = buyResp.phoneNumber,
                        serviceName = "",
                        status = OrderStatus.PENDING,
                        smsCode = null,
                        createdAt = "",
                        expiresAt = buyResp.expiresAt
                    )
                    _uiState.update { it.copy(isBuying = false, order = order) }
                    startPolling(order.orderId)
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isBuying = false, error = result.exception.message) }
                }
                is NetworkResult.Loading -> { /* حالة تحميل – لا نفعل شيئاً */ }
            }
        }
    }

    private fun startPolling(orderId: String) {
        pollingJob?.cancel()
        _uiState.update { it.copy(isPolling = true, error = null, smsMessage = null) }

        pollingJob = viewModelScope.launch {
            pollSms(orderId).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _uiState.update { it.copy(isPolling = false, smsMessage = result.data) }
                        pollingJob?.cancel()
                    }
                    is NetworkResult.Error -> {
                        _uiState.update { it.copy(isPolling = false, error = result.exception.message) }
                        pollingJob?.cancel()
                    }
                    is NetworkResult.Loading -> { /* waiting */ }
                }
            }
        }
    }

    private fun pollSms(
        orderId: String,
        intervalMillis: Long = 5000L,
        maxAttempts: Int = 24
    ): Flow<NetworkResult<SmsMessage>> = flow {
        emit(NetworkResult.Loading)
        repeat(maxAttempts) {
            val result = try {
                val smsResp = api.getSms(orderId)
                NetworkResult.Success(smsResp)
            } catch (e: Exception) {
                NetworkResult.Error(NetworkException.fromThrowable(e))
            }

            when (result) {
                is NetworkResult.Success -> {
                    val sms = result.data
                    if (sms.smsCode != null && sms.status == "RECEIVED") {
                        emit(NetworkResult.Success(SmsMessage(
                            orderId = orderId,
                            smsCode = sms.smsCode,
                            status = sms.status
                        )))
                        return@flow
                    }
                }
                is NetworkResult.Error -> {
                    emit(NetworkResult.Error(result.exception))
                    return@flow
                }
                is NetworkResult.Loading -> { /* continue */ }
            }
            delay(intervalMillis)
        }
        emit(NetworkResult.Error(NetworkException.Timeout()))
    }

    fun retry() {
        val currentState = _uiState.value
        if (currentState.order != null) {
            startPolling(currentState.order.orderId)
        } else {
            _uiState.update { it.copy(error = null) }
        }
    }

    fun cancelPolling() {
        pollingJob?.cancel()
        _uiState.update { it.copy(isPolling = false) }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
