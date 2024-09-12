package com.example.meangene.notification

import com.example.meangene.notification.model.NotificationInfo
import com.example.productsadder.network.RetrofitInstance

class NotificationRepository {
    suspend fun sendNotification(notification: NotificationInfo): Result<Unit> {
        return try {
            val response = RetrofitInstance.api.sendNotification(notification)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}