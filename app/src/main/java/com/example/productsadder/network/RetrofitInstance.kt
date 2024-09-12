package com.example.productsadder.network

import com.example.meangene.notification.NotificationRetrofitAPI
import com.example.productsadder.util.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val BASE_URL = Constants.BASE_URL
    val okHttpBuilder = OkHttpClient.Builder()
    val httpLoggingInterceptor = HttpLoggingInterceptor()


    val api: NotificationRetrofitAPI by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpBuilder.build())
            .build()
            .create(NotificationRetrofitAPI::class.java)
    }
}