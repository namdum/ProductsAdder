package com.example.meangene.notification.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meangene.notification.NotificationRepository
import com.example.productsadder.util.Resource
import kotlinx.coroutines.launch

class NotificationViewModel(private val repository: NotificationRepository) : ViewModel() {


    private val _notificationResult = MutableLiveData<Resource<String>>()
    val notificationResult: LiveData<Resource<String>> get() = _notificationResult

//    private val _notificationResult = MutableStateFlow<Resource<NotificationInfo>>(Resource.Unspecified())
//    val notificationResult = _notificationResult.asStateFlow()

    fun sendNotification(notificationInfo: NotificationInfo) {
        _notificationResult.value = Resource.Loading() // Set loading state

        viewModelScope.launch {
            try {
                val response = repository.sendNotification(notificationInfo)
                if (response.isSuccess) {
                    _notificationResult.value = Resource.Success("Send Notification successful") // Success result

                } else {
                    _notificationResult.value = Resource.Error("Failed to send notification: ${response.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _notificationResult.value = Resource.Error("An error occurred: ${e.message}")
            }
        }
    }
}