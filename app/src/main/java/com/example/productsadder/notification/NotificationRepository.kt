package com.example.productsadder.notification

import com.example.meangene.notification.model.NotificationInfo

class NotificationRepository (private val notificationRetrofitAPI: NotificationRetrofitAPI
) {
    suspend fun sendNotification(notificationInfo: NotificationInfo): NotificationInfo {
        return notificationRetrofitAPI.sendNotification(notificationInfo)
    }

}