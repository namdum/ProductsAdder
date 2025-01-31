package com.example.productsadder.notification

import dagger.Module
import dagger.Provides


@Module
class NotificationModelProvider {
    @Provides
    fun provideChatViewModel(
        chatRepository: NotificationRepository
    ): NotificationViewModel {
        return NotificationViewModel(
            chatRepository
        )
    }

}
