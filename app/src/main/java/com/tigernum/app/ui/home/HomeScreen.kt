package com.tigernum.app.ui.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tigernum.app.R
import com.tigernum.app.domain.model.Country
import com.tigernum.app.domain.model.Provider
import com.tigernum.app.domain.model.Service
import com.tigernum.app.ui.components.LoadingSkeleton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onBuyClick: (provider: Provider, country: Country, service: Service) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedProvider by remember { mutableStateOf<Provider?>(null) }
    var selectedCountry by remember { mutableStateOf<Country?>(null) }
    var selectedService by remember { mutableStateOf<Service?>(null) }

    var providerExpanded by remember { mutableStateOf(false) }
    var countryExpanded by remember { mutableStateOf(false) }
    var serviceExpanded by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // عند تحميل البيانات لأول مرة، اختيار العنصر الأول من كل قائمة إن وجدت
    LaunchedEffect(uiState.providers, uiState.countries, uiState.services) {
        if (selectedProvider == null && uiState.providers.isNotEmpty()) {
            selectedProvider = uiState.providers.first()
        }
        if (selectedCountry == null && uiState.countries.isNotEmpty()) {
            selectedCountry = uiState.countries.first()
        }
        if (selectedService == null && uiState.services.isNotEmpty()) {
            selectedService = uiState.services.first()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // العنوان
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        if (uiState.isLoading) {
            LoadingSkeleton()
        } else if (uiState.error != null) {
            // عرض خطأ مع زر إعادة المحاولة
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = uiState.error ?: stringResource(R.string.error_unknown),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "إعادة المحاولة")
                }
            }
        } else {
            // بطاقة الرصيد
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "الرصيد: ${uiState.balance?.let { "%.2f $"   .format(it) } ?: "غير متوفر"}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // قائمة المزودين (Provider)
            ExposedDropdownMenuBox(
                expanded = providerExpanded,
                onExpandedChange = { providerExpanded = !providerExpanded }
            ) {
                OutlinedTextField(
                    value = selectedProvider?.name ?: stringResource(R.string.select_provider),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.provider)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = providerExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = providerExpanded,
                    onDismissRequest = { providerExpanded = false }
                ) {
                    uiState.providers.forEach { provider ->
                        DropdownMenuItem(
                            text = { Text(provider.name) },
                            onClick = {
                                selectedProvider = provider
                                providerExpanded = false
                            }
                        )
                    }
                }
            }

            // قائمة الدول (مع العلم)
            ExposedDropdownMenuBox(
                expanded = countryExpanded,
                onExpandedChange = { countryExpanded = !countryExpanded }
            ) {
                OutlinedTextField(
                    value = selectedCountry?.let { "${it.flag}  ${it.name}  (${it.dialCode})" } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.country)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = countryExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = countryExpanded,
                    onDismissRequest = { countryExpanded = false }
                ) {
                    uiState.countries.forEach { country ->
                        DropdownMenuItem(
                            text = { Text("${country.flag}  ${country.name}  (${country.dialCode})") },
                            onClick = {
                                selectedCountry = country
                                countryExpanded = false
                            }
                        )
                    }
                }
            }

            // قائمة الخدمات
            ExposedDropdownMenuBox(
                expanded = serviceExpanded,
                onExpandedChange = { serviceExpanded = !serviceExpanded }
            ) {
                OutlinedTextField(
                    value = selectedService?.name ?: stringResource(R.string.select_service),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.service)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = serviceExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = serviceExpanded,
                    onDismissRequest = { serviceExpanded = false }
                ) {
                    uiState.services.forEach { service ->
                        DropdownMenuItem(
                            text = { Text(service.name) },
                            onClick = {
                                selectedService = service
                                serviceExpanded = false
                            }
                        )
                    }
                }
            }

            // زر الشراء
            Button(
                onClick = {
                    selectedProvider?.let { provider ->
                        selectedCountry?.let { country ->
                            selectedService?.let { service ->
                                onBuyClick(provider, country, service)
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = selectedProvider != null && selectedCountry != null && selectedService != null
            ) {
                Text(
                    text = stringResource(R.string.buy_number),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
