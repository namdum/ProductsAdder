package com.example.meangene.notification.model

import androidx.annotation.Keep

@Keep
data class NotificationInfo(
    val title: String = "",
    val message: String = "",
    val notificationType: String = "",
    val receiverId: String = "",
    val senderId: String = ""
)
data class NotificationResponse(
    val success: Boolean,
    val message: String
)