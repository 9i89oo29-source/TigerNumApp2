package com.tigernum.app.data.remote

import java.net.ConnectException
import java.net.SocketTimeoutException

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val exception: NetworkException) : NetworkResult<Nothing>()
}

sealed class NetworkException(override val message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NoInternet(cause: Throwable? = null) : NetworkException("لا يوجد اتصال بالإنترنت", cause)
    class Timeout(cause: Throwable? = null) : NetworkException("انتهت مهلة الاتصال", cause)
    class ServerError(val code: Int, message: String) : NetworkException(message)
    class ClientError(val code: Int, message: String) : NetworkException(message)
    class SerializationError(cause: Throwable? = null) : NetworkException("خطأ في تنسيق البيانات", cause)
    class Unknown(cause: Throwable? = null) : NetworkException("خطأ غير معروف", cause)

    companion object {
        fun fromThrowable(throwable: Throwable): NetworkException {
            return when (throwable) {
                is NetworkException -> throwable
                is ConnectException -> NoInternet(throwable)
                is SocketTimeoutException -> Timeout(throwable)
                is retrofit2.HttpException -> {
                    val code = throwable.code()
                    val msg = throwable.message() ?: "HTTP Error"
                    if (code in 400..499) ClientError(code, msg) else ServerError(code, msg)
                }
                is kotlinx.serialization.SerializationException -> SerializationError(throwable)
                else -> Unknown(throwable)
            }
        }
    }
}
