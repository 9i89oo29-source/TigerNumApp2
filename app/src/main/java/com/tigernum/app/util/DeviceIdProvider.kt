package com.tigernum.app.util

import android.content.Context
import android.content.pm.PackageManager
import com.tigernum.app.data.local.DeviceManager
import java.security.MessageDigest

/**
 * يوفّر بصمة جهاز مشفّرة (SHA-256) تتكون من:
 * - ANDROID_ID
 * - توقيع التطبيق (App Signature)
 * - طابع زمني لأول تثبيت
 */
class DeviceIdProvider(private val context: Context) {

    private val deviceManager = DeviceManager(context)

    /**
     * يعيد بصمة الجهاز النهائية (hex string) لاستخدامها في الطلبات.
     */
    fun getHashedFingerprint(): String {
        val androidId = deviceManager.getAndroidId()
        val signature = getAppSignature()
        val installTimestamp = deviceManager.getOrCreateInstallTimestamp().toString()

        val raw = "$androidId|$signature|$installTimestamp"
        return hashString(raw)
    }

    /**
     * يستخرج توقيع التطبيق (SHA-256) من شهادة التوقيع.
     */
    private fun getAppSignature(): String {
        return try {
            val packageInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
            }

            val signatures: Array<out java.security.Signature>? = 
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    packageInfo.signingInfo?.apkContentsSigners
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.signatures
                }

            val signatureBytes = signatures?.firstOrNull()?.toByteArray() ?: ByteArray(0)
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(signatureBytes)
            digest.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "signature_unavailable"
        }
    }

    private fun hashString(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
