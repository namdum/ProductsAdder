package com.example.meangene.notification

import com.example.meangene.notification.model.NotificationInfo
import com.example.meangene.notification.model.NotificationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface NotificationRetrofitAPI {
    @POST("api/sendnotification")
    suspend fun sendNotification(
        @Body notification: NotificationInfo
    ): Response<NotificationResponse>
}