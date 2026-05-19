package com.tigernum.app.util.security

import java.io.File

/**
 * يفحص وجود أدلة على صلاحيات الروت.
 */
object RootDetector {
    fun isDeviceRooted(): Boolean {
        val suPaths = listOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
        return suPaths.any { File(it).exists() } || checkBuildTags()
    }

    private fun checkBuildTags(): Boolean {
        return android.os.Build.TAGS?.contains("test-keys") == true
    }
}
