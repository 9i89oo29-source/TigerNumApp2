package com.tigernum.app.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.tigernum.app.data.local.DeviceManager
import com.tigernum.app.util.DeviceIdProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SettingsUiState(
    val deviceId: String = "",
    val isDarkMode: Boolean = false,
    val appVersion: String = ""
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val deviceManager = DeviceManager(application)
    private val deviceIdProvider = DeviceIdProvider(application)

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            deviceId = deviceIdProvider.getHashedFingerprint(),
            isDarkMode = deviceManager.getBoolean("dark_mode", false),
            appVersion = getAppVersion(application)
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun toggleDarkMode(enabled: Boolean) {
        deviceManager.saveBoolean("dark_mode", enabled)
        _uiState.update { it.copy(isDarkMode = enabled) }
    }

    fun resetAllData() {
        deviceManager.resetAllData()
        // بعد إعادة التعيين، نعيد تحميل البصمة الجديدة
        _uiState.update {
            it.copy(deviceId = deviceIdProvider.getHashedFingerprint())
        }
    }

    private fun getAppVersion(context: Application): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }
}
