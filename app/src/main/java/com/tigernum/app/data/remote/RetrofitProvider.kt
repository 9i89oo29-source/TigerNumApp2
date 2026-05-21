package com.tigernum.app.data.remote

import com.tigernum.app.BuildConfig
import com.tigernum.app.data.remote.api.BotApiService
import com.tigernum.app.data.remote.interceptor.DeviceAuthInterceptor
import com.tigernum.app.data.remote.interceptor.RetryInterceptor
import com.tigernum.app.util.DeviceIdProvider
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.util.concurrent.TimeUnit

object RetrofitProvider {

    private const val CONNECT_TIMEOUT = 15L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 30L

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    // يمكن إضافة CertificatePinner عند الحاجة

    private fun createOkHttpClient(
        deviceIdProvider: DeviceIdProvider,
        tokenProvider: () -> String?
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

        return OkHttpClient.Builder()
            .addInterceptor(DeviceAuthInterceptor(deviceIdProvider, tokenProvider))
            .addInterceptor(RetryInterceptor(maxRetries = 2))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    private var apiService: BotApiService? = null

    fun getApiService(
        deviceIdProvider: DeviceIdProvider,
        tokenProvider: () -> String? = { null }
    ): BotApiService {
        if (apiService == null) {
            val client = createOkHttpClient(deviceIdProvider, tokenProvider)
            val retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.BOT_API_BASE_URL)
                .client(client)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
            apiService = retrofit.create(BotApiService::class.java)
        }
        return apiService!!
    }
}
