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

/**
 * Provides singleton Retrofit instance for the bot backend.
 */
object RetrofitProvider {

    private const val CONNECT_TIMEOUT = 15L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 30L

    // JSON configuration – ignores unknown fields from backend
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    // ⚠️ SSL Pinning: In production, replace the trust manager with a CertificatePinner.
    // Example of SSL pinning (commented) – you must add real pins for your backend.
    // private val certificatePinner = CertificatePinner.Builder()
    //     .add("your-bot-backend.com", "sha256/AAAA...")
    //     .build()

    // For development only – do NOT use in production (bypasses SSL verification)
    private fun unsafeOkHttpClient(): OkHttpClient {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .build()
    }

    // Production-ready client with SSL pinning (uncomment pinning above and remove unsafe fallback)
    private fun createOkHttpClient(deviceIdProvider: DeviceIdProvider): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

        return OkHttpClient.Builder()
            .addInterceptor(DeviceAuthInterceptor(deviceIdProvider))
            .addInterceptor(RetryInterceptor(maxRetries = 2))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            // .certificatePinner(certificatePinner)   // Enable for production
            .build()
    }

    private var apiService: BotApiService? = null

    fun getApiService(deviceIdProvider: DeviceIdProvider): BotApiService {
        if (apiService == null) {
            val client = createOkHttpClient(deviceIdProvider)
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
