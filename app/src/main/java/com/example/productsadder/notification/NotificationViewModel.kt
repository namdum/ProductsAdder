package com.example.productsadder.notification


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.meangene.notification.model.NotificationInfo
import com.example.productsadder.ui.BasicViewModel
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val chatRepository: NotificationRepository
) : BasicViewModel() {

    private val _createChatRoomState = MutableLiveData<CreateNotificationViewState>()
    val createChatRoomState: LiveData<CreateNotificationViewState> get() = _createChatRoomState

    fun sendNotification(notificationInfo: NotificationInfo) {
        viewModelScope.launch {
            _createChatRoomState.postValue(CreateNotificationViewState.LoadingState(true))
            val response = chatRepository.sendNotification(notificationInfo)
            _createChatRoomState.postValue(CreateNotificationViewState.LoadingState(false))

            if (response != null ) {

                _createChatRoomState.postValue(CreateNotificationViewState.CreateRoomSuccess(response))
            } else {
                _createChatRoomState.postValue(CreateNotificationViewState.ErrorMessage("No data available or error occurred"))
            }
        }
    }


}

sealed class CreateNotificationViewState {
    data class ErrorMessage(val errorMessage: String) : CreateNotificationViewState()
    data class SuccessMessage(val successMessage: String) : CreateNotificationViewState()
    data class LoadingState(val isLoading: Boolean) : CreateNotificationViewState()
    data class CreateRoomSuccess(val chatRoomInfo: NotificationInfo) : CreateNotificationViewState()
}