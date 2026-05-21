package com.tigernum.app.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tigernum.app.data.local.DeviceManager
import com.tigernum.app.data.remote.NetworkException
import com.tigernum.app.data.remote.NetworkResult
import com.tigernum.app.data.remote.RetrofitProvider
import com.tigernum.app.data.remote.api.BotApiService
import com.tigernum.app.data.remote.api.DeviceAuthRequest
import com.tigernum.app.data.remote.dto.BalanceDto
import com.tigernum.app.data.remote.dto.CountryDto
import com.tigernum.app.data.remote.dto.ProviderDto
import com.tigernum.app.data.remote.dto.ServiceDto
import com.tigernum.app.domain.model.Country
import com.tigernum.app.domain.model.Provider
import com.tigernum.app.domain.model.Service
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

    // إنشاء api مع tokenProvider الذي يقرأ التوكن من DeviceManager في كل مرة
    private val api: BotApiService = RetrofitProvider.getApiService(
        deviceIdProvider = deviceIdProvider,
        tokenProvider = { deviceManager.getString("jwt_token", null) }
    )

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        Log.d("TigerNumApp", "HomeViewModel init – calling authenticateAndLoad")
        authenticateAndLoad()
    }

    private fun authenticateAndLoad() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // 1. محاولة المصادقة
            try {
                Log.d("TigerNumApp", "Getting hashed fingerprint...")
                val fingerprint = deviceIdProvider.getHashedFingerprint()
                Log.d("TigerNumApp", "Fingerprint: ${fingerprint.substring(0, 16)}...")

                val response = api.authDevice(
                    DeviceAuthRequest(fingerprint = fingerprint)
                )
                deviceManager.saveString("jwt_token", response.accessToken)
                Log.d("TigerNumApp", "Auth SUCCESS – token saved")
            } catch (e: Exception) {
                Log.e("TigerNumApp", "Auth FAILED: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "فشلت المصادقة: ${e.message}"
                    )
                }
                return@launch  // توقف عن تحميل البيانات إذا فشلت المصادقة
            }

            // 2. تحميل البيانات
            loadData()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            val providersDeferred = async { safeApiCall { api.getProviders().map { it.toDomain() } } }
            val countriesDeferred = async { safeApiCall { api.getCountries().map { it.toDomain() } } }
            val servicesDeferred = async { safeApiCall { api.getServices().map { it.toDomain() } } }
            val balanceDeferred = async { safeApiCall { api.getBalance().balance } }

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

    private suspend fun <T> safeApiCall(call: suspend () -> T): NetworkResult<T> {
        return try {
            NetworkResult.Success(call())
        } catch (e: Exception) {
            NetworkResult.Error(NetworkException.fromThrowable(e))
        }
    }

    fun refresh() {
        authenticateAndLoad()
    }
}

// دوال التحويل
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
