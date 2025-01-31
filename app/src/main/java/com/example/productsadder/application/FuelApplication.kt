package com.example.productsadder.application

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.AssetManager
import com.example.productsadder.di.BaseAppComponent
import com.example.productsadder.di.BaseUiApp


@SuppressLint("Registered")
open class FuelApplication : BaseUiApp() {


    companion object {
        var assetManager: AssetManager? = null
        lateinit var component: BaseAppComponent

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        initInstallTime()
    }



    /**
     * start install-time delivery mode
     */
    private fun initInstallTime() {
        try {
            val context = createPackageContext("com.meetfriend.app", 0)
            assetManager = context.assets
        } catch (e: PackageManager.NameNotFoundException) {
//            Timber.e("AssetManager $e")
        }
    }

    override fun getAppComponent(): BaseAppComponent {
        return component
    }

    override fun setAppComponent(baseAppComponent: BaseAppComponent) {
        component = baseAppComponent
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
//        MultiDex.install(this)
    }
}