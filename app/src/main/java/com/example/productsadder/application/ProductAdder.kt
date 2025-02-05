package com.example.productsadder.application

import android.app.Activity
import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.productsadder.di.DaggerProductAdderAppComponent
import com.example.productsadder.di.ProductAdderAppComponent
import com.example.productsadder.di.ProductAdderAppModule

class ProductAdder : ProductAdderApplication() {
    companion object {
        operator fun get(app: Application): ProductAdder {
            return app as ProductAdder
        }

        operator fun get(activity: Activity): ProductAdder {
            return activity.application as ProductAdder
        }

        lateinit var component: ProductAdderAppComponent
            private set

    }



    override fun onCreate() {
        super.onCreate()
        try {
            component = DaggerProductAdderAppComponent.builder()
                .productAdderAppModule(ProductAdderAppModule(this))
                .build()
            component.inject(this)
            super.setAppComponent(component)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

}
