package com.example.productsadder.notification

import com.example.meangene.notification.model.NotificationInfo
import retrofit2.http.Body
import retrofit2.http.POST

interface NotificationRetrofitAPI {
    @POST("api/sendnotification")
    suspend fun sendNotification(
        @Body notification: NotificationInfo
    ): NotificationInfo


}