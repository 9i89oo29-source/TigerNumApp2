package com.tigernum.app.ui.home

import com.tigernum.app.data.remote.api.DeviceAuthRequest
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tigernum.app.data.remote.NetworkResult
import com.tigernum.app.data.remote.RetrofitProvider
import com.tigernum.app.data.remote.api.BotApiService
import com.tigernum.app.data.remote.dto.*
import com.tigernum.app.data.local.DeviceManager
import com.tigernum.app.domain.model.*
import com.tigernum.app.util.DeviceIdProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val providers: List<Provider> = emptyList(),
    val countries: List<Country> = emptyList(),
    val services: List<Service> = emptyList(),
    val balance: Double? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val deviceManager = DeviceManager(application)
    private val deviceIdProvider = DeviceIdProvider(application)

    private val api: BotApiService = RetrofitProvider.getApiService(
        deviceIdProvider = deviceIdProvider,
        tokenProvider = { deviceManager.getString("jwt_token", null) }
    )

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        authenticateAndLoad()
    }

    private fun authenticateAndLoad() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // 1. محاولة المصادقة
            try {
                val fingerprint = deviceIdProvider.getHashedFingerprint()
                val response = api.authDevice(
                    DeviceAuthRequest(fingerprint = fingerprint)
                )
                deviceManager.saveString("jwt_token", response.accessToken)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Auth failed: ${e.message}") }
            }

            // 2. تحميل البيانات
            loadData()
        }
    }

    private suspend fun safeApiCall2() {
        // دمجت في الخطوة التالية
    }

    private fun loadData() {
        viewModelScope.launch {
            val providersDeferred = async {
                try {
                    val list = api.getProviders()
                    NetworkResult.Success(list.map { it.toDomain() })
                } catch (e: Exception) {
                    NetworkResult.Error(com.tigernum.app.data.remote.NetworkException.fromThrowable(e))
                }
            }
            val countriesDeferred = async {
                try {
                    val list = api.getCountries()
                    NetworkResult.Success(list.map { it.toDomain() })
                } catch (e: Exception) {
                    NetworkResult.Error(com.tigernum.app.data.remote.NetworkException.fromThrowable(e))
                }
            }
            val servicesDeferred = async {
                try {
                    val list = api.getServices()
                    NetworkResult.Success(list.map { it.toDomain() })
                } catch (e: Exception) {
                    NetworkResult.Error(com.tigernum.app.data.remote.NetworkException.fromThrowable(e))
                }
            }
            val balanceDeferred = async {
                try {
                    val balanceDto = api.getBalance()
                    NetworkResult.Success(balanceDto.balance)
                } catch (e: Exception) {
                    NetworkResult.Error(com.tigernum.app.data.remote.NetworkException.fromThrowable(e))
                }
            }

            val providersResult = providersDeferred.await()
            val countriesResult = countriesDeferred.await()
            val servicesResult = servicesDeferred.await()
            val balanceResult = balanceDeferred.await()

            val errors = listOfNotNull(
                (providersResult as? NetworkResult.Error)?.exception?.message,
                (countriesResult as? NetworkResult.Error)?.exception?.message,
                (servicesResult as? NetworkResult.Error)?.exception?.message,
                (balanceResult as? NetworkResult.Error)?.exception?.message
            )

            val providers = (providersResult as? NetworkResult.Success)?.data ?: emptyList()
            val countries = (countriesResult as? NetworkResult.Success)?.data ?: emptyList()
            val services = (servicesResult as? NetworkResult.Success)?.data ?: emptyList()
            val balance = (balanceResult as? NetworkResult.Success)?.data

            _uiState.update {
                it.copy(
                    providers = providers,
                    countries = countries,
                    services = services,
                    balance = balance,
                    isLoading = false,
                    error = errors.firstOrNull()
                )
            }
        }
    }

    fun refresh() {
        authenticateAndLoad()
    }
}

// دوال التحويل للنماذج (إن لم تكن موجودة)
fun ServiceDto.toDomain() = Service(
    id = providerServiceId,
    name = name,
    price = price.toDouble(),
    available = available
)

fun CountryDto.toDomain() = Country(
    code = code,
    name = nameAr ?: name,
    flag = flag ?: "",
    dialCode = dialCode
)

fun ProviderDto.toDomain() = Provider(
    id = id,
    name = name
)
