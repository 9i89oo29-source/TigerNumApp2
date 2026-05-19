package com.tigernum.app.ui.orders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tigernum.app.data.remote.NetworkResult
import com.tigernum.app.data.repository.BotRepository
import com.tigernum.app.domain.model.Order
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
    private val repository = BotRepository(deviceIdProvider)

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getOrders()) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, orders = result.data) }
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.exception.message)
                    }
                }
            }
        }
    }
}
