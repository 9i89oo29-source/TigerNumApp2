package com.tigernum.app.data.local

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class DeviceManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "tiger_num_secure_prefs"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_INSTALL_TIMESTAMP = "install_timestamp"
    }

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val securePrefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @SuppressLint("HardwareIds")
    fun getAndroidId(): String {
        securePrefs.getString(KEY_DEVICE_ID, null)?.let { return it }

        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "unknown_device"

        securePrefs.edit().putString(KEY_DEVICE_ID, androidId).apply()
        return androidId
    }

    fun getOrCreateInstallTimestamp(): Long {
        val existing = securePrefs.getLong(KEY_INSTALL_TIMESTAMP, 0L)
        if (existing == 0L) {
            val now = System.currentTimeMillis()
            securePrefs.edit().putLong(KEY_INSTALL_TIMESTAMP, now).apply()
            return now
        }
        return existing
    }

    fun saveLong(key: String, value: Long) {
        securePrefs.edit().putLong(key, value).apply()
    }

    fun getLong(key: String, defaultValue: Long): Long {
        return securePrefs.getLong(key, defaultValue)
    }

    fun saveString(key: String, value: String) {
        securePrefs.edit().putString(key, value).apply()
    }

    fun getString(key: String, defaultValue: String?): String? {
        return securePrefs.getString(key, defaultValue)
    }

    fun saveBoolean(key: String, value: Boolean) {
        securePrefs.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return securePrefs.getBoolean(key, defaultValue)
    }

    fun resetAllData() {
        securePrefs.edit().clear().apply()
    }
}
