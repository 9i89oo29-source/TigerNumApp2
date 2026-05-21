package com.tigernum.app.ui.orders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tigernum.app.data.local.DeviceManager
import com.tigernum.app.data.remote.NetworkResult
import com.tigernum.app.data.remote.NetworkException
import com.tigernum.app.data.remote.RetrofitProvider
import com.tigernum.app.data.remote.api.BotApiService
import com.tigernum.app.data.remote.dto.OrderDto
import com.tigernum.app.domain.model.Order
import com.tigernum.app.domain.model.OrderStatus
import com.tigernum.app.util.DeviceIdProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class OrdersUiState(
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class OrdersViewModel(application: Application) : AndroidViewModel(application) {

    private val deviceIdProvider = DeviceIdProvider(application)
    private val deviceManager = DeviceManager(application)

    // نستخدم api جديدة مع tokenProvider
    private val api: BotApiService = RetrofitProvider.getApiService(
        deviceIdProvider = deviceIdProvider,
        tokenProvider = { deviceManager.getString("jwt_token", null) }
    )

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = try {
                val list = api.getOrders()
                NetworkResult.Success(list)
            } catch (e: Exception) {
                NetworkResult.Error(NetworkException.fromThrowable(e))
            }

            when (result) {
                is NetworkResult.Success -> {
                    val orders = result.data.map { it.toDomain() }
                    _uiState.update { it.copy(isLoading = false, orders = orders) }
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                }
            }
        }
    }

    private fun OrderDto.toDomain() = Order(
        orderId = orderId,
        phoneNumber = phoneNumber,
        serviceName = serviceName,
        status = when (status.uppercase()) {
            "PENDING" -> OrderStatus.PENDING
            "COMPLETED" -> OrderStatus.COMPLETED
            "CANCELLED" -> OrderStatus.CANCELLED
            else -> OrderStatus.UNKNOWN
        },
        smsCode = smsCode,
        createdAt = createdAt,
        expiresAt = expiresAt
    )
}
