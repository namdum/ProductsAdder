package com.example.productsadder.notification

import dagger.Module
import dagger.Provides


@Module
class NotificationModelProvider {
    @Provides
    fun provideNotificationViewModel(
        notificationRepository: NotificationRepository
    ): NotificationViewModel {
        return NotificationViewModel(
            notificationRepository
        )
    }

}
