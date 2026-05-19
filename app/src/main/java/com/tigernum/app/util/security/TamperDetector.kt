package com.tigernum.app.util.security

import android.content.Context
import android.content.pm.PackageManager
import java.security.MessageDigest

/**
 * يتحقق من عدم التلاعب بتوقيع التطبيق.
 */
object TamperDetector {
    private const val EXPECTED_SIGNATURE = "EXPECTED_SIGNATURE_HASH" // استبدل بالتجزئة الحقيقية في الإصدار النهائي

    fun isAppTampered(context: Context): Boolean {
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

            val signatures = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.signingInfo.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            val md = MessageDigest.getInstance("SHA-256")
            val signatureBytes = signatures.firstOrNull()?.toByteArray() ?: return true
            val digest = md.digest(signatureBytes)
            val signatureHex = digest.joinToString(":") { "%02X".format(it) }

            signatureHex != EXPECTED_SIGNATURE
        } catch (e: Exception) {
            true // لا يمكن التحقق -> نفترض التلاعب
        }
    }
}
