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

    /**
     * يبدأ عملية شراء رقم جديد ثم ينتقل تلقائياً إلى استقبال SMS.
     */
    fun buyNumber(provider: String, serviceId: String, countryCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBuying = true, error = null, order = null, smsMessage = null) }

            when (val result = repository.buyNumber(serviceId, countryCode)) {
                is NetworkResult.Success -> {
                    val order = result.data
                    _uiState.update { it.copy(isBuying = false, order = order) }
                    // بدء استطلاع SMS فوراً
                    startPolling(order.orderId)
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isBuying = false, error = result.exception.message) }
                }
            }
        }
    }

    /**
     * يستطلع الخادم الخلفي حتى وصول SMS أو حدوث خطأ/مهلة.
     */
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
                    is NetworkResult.Loading -> { /* ما زلنا ننتظر */ }
                }
            }
        }
    }

    /**
     * إعادة المحاولة: إما شراء الرقم مرة أخرى (إذا لم يكن لدينا Order بعد) أو متابعة الاستطلاع.
     */
    fun retry() {
        val currentState = _uiState.value
        if (currentState.order != null) {
            // توجد طلبية، نعيد الاستطلاع
            startPolling(currentState.order.orderId)
        } else {
            // لا توجد طلبية – لا يمكننا إعادة المحاولة تلقائياً بدون معرفة المدخلات السابقة
            // يمكن إضافة دالة retryBuy(provider, serviceId, countryCode) إذا لزم الأمر
            _uiState.update { it.copy(error = null) }
        }
    }

    /**
     * إيقاف الاستطلاع (عند مغادرة الشاشة).
     */
    fun cancelPolling() {
        pollingJob?.cancel()
        _uiState.update { it.copy(isPolling = false) }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
