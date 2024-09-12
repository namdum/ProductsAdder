package com.example.meangene.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.meangene.notification.model.NotificationViewModel
import javax.inject.Inject

class NotificationViewModelFactory (
    private val notificationApiService: NotificationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificationViewModel(notificationApiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}