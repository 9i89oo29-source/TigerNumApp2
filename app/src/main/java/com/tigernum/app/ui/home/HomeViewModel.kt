package com.tigernum.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tigernum.app.data.remote.NetworkResult
import com.tigernum.app.data.repository.BotRepository
import com.tigernum.app.domain.model.*
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

class HomeViewModel(
    private val repository: BotRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val providersDeferred = async { repository.getProviders() }
            val countriesDeferred = async { repository.getCountries() }
            val servicesDeferred = async { repository.getServices() }
            val balanceDeferred = async { repository.getBalance() }

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
        loadData()
    }
}
