package com.tigernum.app.util.security

import android.content.Context
import android.content.pm.PackageManager
import java.security.MessageDigest

object TamperDetector {
    private const val EXPECTED_SIGNATURE = "EXPECTED_SIGNATURE_HASH"

    fun isAppTampered(context: Context): Boolean {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )

            val signatures: Array<out android.content.pm.Signature>? =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    packageInfo.signingInfo?.apkContentsSigners
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.signatures
                }

            val md = MessageDigest.getInstance("SHA-256")
            val signatureBytes = signatures?.firstOrNull()?.toByteArray() ?: return true
            val digest = md.digest(signatureBytes)
            val signatureHex = digest.joinToString(":") { "%02X".format(it) }

            signatureHex != EXPECTED_SIGNATURE
        } catch (e: Exception) {
            true
        }
    }
}
