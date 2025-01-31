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

    private val _createChatRoomState = MutableLiveData<CreateChatRoomViewState>()
    val createChatRoomState: LiveData<CreateChatRoomViewState> get() = _createChatRoomState

    fun sendNotification(notificationInfo: NotificationInfo) {
        viewModelScope.launch {
            _createChatRoomState.postValue(CreateChatRoomViewState.LoadingState(true))
            val response = chatRepository.sendNotification(notificationInfo)
            _createChatRoomState.postValue(CreateChatRoomViewState.LoadingState(false))

            if (response != null ) {

                _createChatRoomState.postValue(CreateChatRoomViewState.CreateRoomSuccess(response))
            } else {
                _createChatRoomState.postValue(CreateChatRoomViewState.ErrorMessage("No data available or error occurred"))
            }
        }
    }


}

sealed class CreateChatRoomViewState {
    data class ErrorMessage(val errorMessage: String) : CreateChatRoomViewState()
    data class SuccessMessage(val successMessage: String) : CreateChatRoomViewState()
    data class LoadingState(val isLoading: Boolean) : CreateChatRoomViewState()
    data class CreateRoomSuccess(val chatRoomInfo: NotificationInfo) : CreateChatRoomViewState()
}