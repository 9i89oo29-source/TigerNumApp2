package com.tigernum.app.data.remote.interceptor

import com.tigernum.app.util.DeviceIdProvider
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adds device authentication header and JWT token to every API request.
 */
class DeviceAuthInterceptor(
    private val deviceIdProvider: DeviceIdProvider,
    private val tokenProvider: () -> String?
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val hashedFingerprint = deviceIdProvider.getHashedFingerprint()

        val builder = originalRequest.newBuilder()
            .header("X-Device-Fingerprint", hashedFingerprint)

        // إضافة رمز JWT إذا كان موجوداً
        val token = tokenProvider()
        if (!token.isNullOrBlank()) {
            builder.header("Authorization", "Bearer $token")
        }

        return chain.proceed(builder.build())
    }
}
