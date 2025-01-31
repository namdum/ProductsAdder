package com.example.productsadder.notification


import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class NotificationHeaders(
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val requestBuilder = original.newBuilder()

        val response: Response
        try {
            response = chain.proceed(requestBuilder.build())

        } catch (t: Throwable) {
//            Timber.e("error in InterceptorHeaders:\n${t.message}")
            throw IOException(t.message)
        }
        return response
    }
}