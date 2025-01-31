package com.example.productsadder.notification

import android.content.Context
import com.example.productsadder.network.extension.getAPIBaseUrl
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import io.grpc.android.BuildConfig
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class  NetworkModule {
    @Provides
    @Singleton
    fun provideWristBandInterceptorHeaders(): NotificationHeaders {
        return NotificationHeaders()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        context: Context,
        meetUrFriendInterceptorHeaders: NotificationHeaders,
    ): OkHttpClient {
        val cacheSize = 10 * 1024 * 1024 // 10 MiB
        val cacheDir = File(context.cacheDir, "HttpCache")
        val cache = Cache(cacheDir, cacheSize.toLong())
        val builder = OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(240, TimeUnit.SECONDS)
            .cache(cache)
            .addInterceptor(meetUrFriendInterceptorHeaders)
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(loggingInterceptor)
        }
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(getAPIBaseUrl())
            .client(okHttpClient)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync()) // Using create async means all api calls are automatically created asynchronously using OkHttp's thread pool
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder()
                        .setDateFormat("yyyy-MM-dd HH:mm:ss")
                        .enableComplexMapKeySerialization()
                        .create()
                )
            )
            .build()
    }

}