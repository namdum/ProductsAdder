package com.example.productsadder.network


import com.example.productsadder.notification.NotificationRepository
import com.example.productsadder.notification.NotificationRetrofitAPI
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
class NotificationModule {

    @Provides
    @Singleton
    fun provideChatRetrofitAPI(retrofit: Retrofit): NotificationRetrofitAPI {
        return retrofit.create(NotificationRetrofitAPI::class.java)
    }
    @Provides
    @Singleton
    fun provideChatRepository(
        notificationRetrofitAPI: NotificationRetrofitAPI
    ): NotificationRepository {
        return NotificationRepository(notificationRetrofitAPI)
    }

}