package com.example.productsadder.application

import android.app.Activity
import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.productsadder.di.DaggerFuelAppComponent
import com.example.productsadder.di.FuelAppComponent
import com.example.productsadder.di.FuelAppModule
import okhttp3.Cache

class Fuel : FuelApplication() {
    companion object {
        operator fun get(app: Application): Fuel {
            return app as Fuel
        }

        operator fun get(activity: Activity): Fuel {
            return activity.application as Fuel
        }

        lateinit var component: FuelAppComponent
            private set

        lateinit var cache: Cache
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
