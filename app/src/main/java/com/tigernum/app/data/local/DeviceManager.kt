package com.tigernum.app.data.local

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * يدير تعريف الجهاز والتخزين الآمن محلياً.
 * - يستخدم ANDROID_ID كمعرّف جهاز فريد (متوافق مع سياسات Google Play).
 * - يُخزّن البيانات الحساسة (مثل المفاتيح، التوكنات) في EncryptedSharedPreferences بتشفير AES-256.
 * - لا يستخدم IMEI أو أي معرّفات أجهزة محظورة.
 * - يدعم إعادة التعيين (Factory reset) وحذف بيانات محلية.
 */
class DeviceManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "tiger_num_secure_prefs"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_INSTALL_TIMESTAMP = "install_timestamp"
        // يمكن إضافة مفاتيح أخرى حسب الحاجة (مفاتيح API، إعدادات)
    }

    // مفتاح رئيسي للتشفير AES-256 يتم توليده مرة واحدة ويُخزّن في Android Keystore.
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    // نسخة مشفّرة من SharedPreferences – القراءة والكتابة تكون مشفّرة تلقائياً.
    private val securePrefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * يعيد معرّف الجهاز ANDROID_ID.
     * يتم تخزينه مؤقتاً في التخزين المشفر لتسريع الوصول.
     * لا يستخدم IMEI أو أي معرّف ممنوع.
     */
    @SuppressLint("HardwareIds")
    fun getAndroidId(): String {
        // التحقق من الذاكرة المؤقتة أولاً
        securePrefs.getString(KEY_DEVICE_ID, null)?.let { return it }

        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "unknown_device"

        // تخزينه مؤقتاً
        securePrefs.edit().putString(KEY_DEVICE_ID, androidId).apply()
        return androidId
    }

    /**
     * يحفظ طابعاً زمنياً لتاريخ أول تثبيت (يُستخدم في البصمة).
     * يستدعى مرة واحدة عند التشغيل الأول للتطبيق.
     */
    fun getOrCreateInstallTimestamp(): Long {
        val existing = securePrefs.getLong(KEY_INSTALL_TIMESTAMP, 0L)
        if (existing == 0L) {
            val now = System.currentTimeMillis()
            securePrefs.edit().putLong(KEY_INSTALL_TIMESTAMP, now).apply()
            return now
        }
        return existing
    }

    // ---- دوال تخزين عامة مساعدة (تُستخدم مع AbusePrevention وإعدادات المستخدم) ----

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

    // ---- إعادة تعيين كامل ----

    /**
     * يمسح جميع البيانات المخزنة محلياً (المعرّفات، الإعدادات).
     * مفيد عند رغبة المستخدم في "مسح بيانات التطبيق".
     */
    fun resetAllData() {
        securePrefs.edit().clear().apply()
    }
}
