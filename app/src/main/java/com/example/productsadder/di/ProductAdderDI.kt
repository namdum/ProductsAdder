package com.example.productsadder.di

import android.app.Application
import android.content.Context
import com.example.productsadder.notification.NetworkModule
import com.example.productsadder.application.ProductAdder
import com.example.productsadder.network.NotificationModule
import com.example.productsadder.notification.NotificationModelProvider
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class FuelAppModule(val app: Application) {
    @Provides
    @Singleton
    fun provideApplication(): Application {
        return app
    }

    @Provides
    @Singleton
    fun provideContext(): Context {
        return app
    }
}

@Singleton
@Component(
    modules = [
        FuelAppModule::class,
        NotificationModule::class,
        NetworkModule::class,
        NotificationModelProvider::class,
        ProductAdderModelModule::class
    ]
)

interface FuelAppComponent : BaseAppComponent {
    fun inject(app: ProductAdder)
}