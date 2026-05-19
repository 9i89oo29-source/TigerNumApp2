package com.tigernum.app.ui.buy

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tigernum.app.data.remote.NetworkResult
import com.tigernum.app.data.repository.BotRepository
import com.tigernum.app.domain.model.Order
import com.tigernum.app.domain.model.SmsMessage
import com.tigernum.app.util.DeviceIdProvider
import kotlinx.coroutines.Job
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
    private val repository = BotRepository(deviceIdProvider)

    private val _uiState = MutableStateFlow(BuyNumberUiState())
    val uiState: StateFlow<BuyNumberUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null

    fun buyNumber(provider: String, serviceId: String, countryCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBuying = true, error = null, order = null, smsMessage = null) }

            when (val result = repository.buyNumber(serviceId, countryCode)) {
                is NetworkResult.Success -> {
                    val order = result.data
                    _uiState.update { it.copy(isBuying = false, order = order) }
                    startPolling(order.orderId)
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isBuying = false, error = result.exception.message) }
                }
                is NetworkResult.Loading -> { /* still buying */ }
            }
        }
    }

    private fun startPolling(orderId: String) {
        pollingJob?.cancel()
        _uiState.update { it.copy(isPolling = true, error = null, smsMessage = null) }

        pollingJob = viewModelScope.launch {
            repository.pollSms(orderId).collect { result ->
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
