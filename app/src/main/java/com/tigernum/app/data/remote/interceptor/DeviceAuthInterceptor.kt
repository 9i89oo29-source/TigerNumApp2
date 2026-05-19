package com.tigernum.app.data.remote.interceptor

import com.tigernum.app.util.DeviceIdProvider
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adds device authentication header to every API request.
 * The backend uses this hashed fingerprint to identify the device/user.
 */
class DeviceAuthInterceptor(private val deviceIdProvider: DeviceIdProvider) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val hashedFingerprint = deviceIdProvider.getHashedFingerprint()

        val newRequest = originalRequest.newBuilder()
            .header("X-Device-Fingerprint", hashedFingerprint)
            .build()

        return chain.proceed(newRequest)
    }
}
