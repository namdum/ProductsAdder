package com.example.productsadder.application

import android.app.Activity
import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.productsadder.di.DaggerFuelAppComponent
import com.example.productsadder.di.FuelAppComponent
import com.example.productsadder.di.FuelAppModule

class ProductAdder : ProductAdderApplication() {
    companion object {
        operator fun get(app: Application): ProductAdder {
            return app as ProductAdder
        }

        operator fun get(activity: Activity): ProductAdder {
            return activity.application as ProductAdder
        }

        lateinit var component: FuelAppComponent
            private set

    }



    override fun onCreate() {
        super.onCreate()
        try {
            component = DaggerFuelAppComponent.builder()
                .fuelAppModule(FuelAppModule(this))
                .build()
            component.inject(this)
            super.setAppComponent(component)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

}
