package com.tigernum.app.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Retries the request up to [maxRetries] times when a network failure
 * or 5xx server error occurs.
 */
class RetryInterceptor(private val maxRetries: Int = 2) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response
        var retryCount = 0

        do {
            response = try {
                chain.proceed(request)
            } catch (e: IOException) {
                if (retryCount < maxRetries) {
                    retryCount++
                    continue
                } else {
                    throw e
                }
            }

            if (response.isSuccessful || response.code !in 500..599) {
                break
            } else if (retryCount < maxRetries) {
                retryCount++
                response.close()
            }
        } while (true)

        return response
    }
}
